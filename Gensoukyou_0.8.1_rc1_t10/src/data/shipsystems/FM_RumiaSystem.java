package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class FM_RumiaSystem extends BaseShipSystemScript {

    public static final float POWER_CONSUMPTION = 0.1f;
    public static final float BASE_DAMAGE_MULT = 10f;

    private float TIMER = 0f;
    private boolean WEAPON_FIRED = false;
//    private WeaponAPI weapon = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);
        if (Global.getCombatEngine() == null) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!(stats.getEntity() instanceof ShipAPI)) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (!ship.getVariant().hasHullMod("FantasySpellMod")) return;
        if (!ship.isAlive()) return;

//        if (weapon == null){
//            for (WeaponAPI weaponCheck : ship.getAllWeapons()){
//                if (Objects.equals(weaponCheck.getId(), "FM_RumiaSystemWeapon")){
//                    weapon = weaponCheck;
//                    break;
//                }
//            }
//        }
        Vector2f loc = ship.getLocation();
        //特效内容
        if (state == State.IN) {
            TIMER = TIMER + engine.getElapsedInLastFrame();
            if (TIMER >= 0.1f) {
                engine.spawnEmpArcVisual(
                        loc,
                        ship,
                        MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius()),
                        ship,
                        3f,
                        FM_Colors.FM_RED_EMP_FRINGE,
                        FM_Colors.FM_RED_EMP_CORE
                );
                TIMER = 0f;
            }
        } else if (state == State.ACTIVE && !WEAPON_FIRED) {
            for (int i = 0; i < 10; i = i + 1) {
//            engine.addNegativeParticle(
//                    projectile.getLocation(),
//                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO,70f),
//                    MathUtils.getRandomNumberInRange(6f,9f),
//                    0.5f,
//                    MathUtils.getRandomNumberInRange(1f,1.5f),
//                    FM_Colors.FM_RED_EMP_FRINGE
//            );
                engine.addNegativeNebulaParticle(
                        ship.getLocation(),
                        MathUtils.getPointOnCircumference(new Vector2f(), 90f, MathUtils.getRandomNumberInRange(0, 360)),
                        MathUtils.getRandomNumberInRange(50f, 60f),
                        0.3f,
                        -0.25f,
                        0.5f,
                        2f,
                        FM_Colors.FM_GREEN_EMP_CORE
                );
            }
            engine.addHitParticle(loc, FM_Misc.ZERO, ship.getCollisionRadius() * 3f, 1f, 0.5f, FM_Colors.FM_RED_EMP_FRINGE);
            //engine.addHitParticle(point, vel, size * 3.0f, 1f, dur, p.color);
            engine.addHitParticle(loc, FM_Misc.ZERO, ship.getCollisionRadius() * 1.5f, 1f, 0.5f, Color.white);
            //engine.addHitParticle(point, vel, coreSize * 1f, 1f, dur, Color.white);

            WaveDistortion wave = new WaveDistortion(loc, FM_Misc.ZERO);
            wave.fadeInSize(0.2f);
            wave.setIntensity(50f);
            wave.setArc(0, 360);
            wave.fadeOutIntensity(0.5f);
            wave.setLifetime(0.7f);
            DistortionShader.addDistortion(wave);
            //射击与伤害调整
            DamagingProjectileAPI project = (DamagingProjectileAPI) engine.spawnProjectile(
                    ship,
                    null,
                    "FM_RumiaSystemWeapon",
                    loc,
                    ship.getFacing(),
                    FM_Misc.ZERO
            );
            Global.getSoundPlayer().playSound(
                    "FM_SilverBlade_fire", 1f, 1f, project.getLocation(), project.getVelocity()
            );
            float mult = (float) (1f + Math.sqrt(ship.getHardFluxLevel()) * (BASE_DAMAGE_MULT - 1f));
            project.getDamage().getModifier().modifyMult(
                    id, mult
            );
            //灵基框架相关
            FM_Misc.getSpellModState(Global.getCombatEngine(), ship).spellPower = FM_Misc.getSpellModState(Global.getCombatEngine(), ship).spellPower - POWER_CONSUMPTION;
            WEAPON_FIRED = true;
        }

        stats.getArmorDamageTakenMult().modifyMult(id, 0.5f);
        stats.getHullDamageTakenMult().modifyMult(id, 0.5f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
        stats.getArmorDamageTakenMult().unmodifyMult(id);
        stats.getHullDamageTakenMult().unmodifyMult(id);
        TIMER = 0f;
        WEAPON_FIRED = false;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;
        if (isUsable(system, ship)) {
            return "READY";
        } else {
            return "WEAPON LOCKED";
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (Global.getCombatEngine() == null) return false;
        if (!ship.getVariant().hasHullMod("FantasySpellMod")) return false;
        return (FM_Misc.getSpellModState(Global.getCombatEngine(), ship).spellPower >= 0.1f);

    }
}
