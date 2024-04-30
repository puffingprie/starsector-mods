package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;
import java.util.List;

public class FM_Nocturne extends BaseShipSystemScript {

    public static final float RANGE = 1000f;
    public static final float EFFECT_RANGE = 200f;

    public static final Color JITTER_COLOR = new Color(253, 77, 209, 255);
    public static final Color JITTER_UNDER_COLOR = new Color(182, 65, 255, 255);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        ShipAPI ship;
        //boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        float jitterLevel = effectLevel;
        if (state == State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float maxRangeBonus = 25f;
        float jitterRangeBonus = jitterLevel * maxRangeBonus;

        ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 11, 0f, 3f + jitterRangeBonus);
        ship.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);

        if (state == State.IN) {
        } else if (effectLevel >= 1) {
            Vector2f target = null;
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                target = ship.getMouseTarget();
            }
            if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS)) {
                target = (Vector2f) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
            }
            if (target != null) {
                float dist = Misc.getDistance(ship.getLocation(), target);
                float max = getSystemRange(ship) + ship.getCollisionRadius();
                if (dist > max) {
                    float dir = Misc.getAngleInDegrees(ship.getLocation(), target);
                    target = Misc.getUnitVectorAtDegreeAngle(dir);
                    target.scale(max);
                    Vector2f.add(target, ship.getLocation(), target);
                }

                if (target != null) {
                    antiMissileEffect(ship, target);
                }
            }

        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
    }

    public float getSystemRange(ShipAPI ship) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                RANGE
        );
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

        Vector2f target = ship.getMouseTarget();
        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target);
            float max = getSystemRange(ship) + ship.getCollisionRadius();
            if (dist > max) {
                return "OUT OF RANGE";
            } else {
                return "READY";
            }
        }
        return null;
    }


    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return ship.getMouseTarget() != null;
    }

    private void antiMissileEffect(ShipAPI ship, Vector2f target) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;

        List<MissileAPI> missiles = CombatUtils.getMissilesWithinRange(target, EFFECT_RANGE);
        for (MissileAPI missile : missiles) {
            if (missile instanceof ShipAPI || missile.isMine() || missile.isMinePrimed()) {
                missile.explode();
            }

            //missile.setMaxFlightTime(0.1f);
            missile.flameOut();
        }

        MagicLensFlare.createSharpFlare(engine, ship, target, 10f, 400f, 0f, JITTER_COLOR, Color.WHITE);
        engine.spawnExplosion(target, new Vector2f(), JITTER_COLOR, 160f, 0.55f);

        for (int i = 0; i < 20; i = i + 1) {
            engine.addHitParticle(target, MathUtils.getRandomPointInCircle(new Vector2f(), 600f), 4f, 10f, 0.7f, JITTER_UNDER_COLOR);
        }

        Global.getSoundPlayer().playSound("FM_Nightbugs_expand_1", 2f, 0.5f, target, new Vector2f());
        Global.getSoundPlayer().playSound("FM_icebreak_se", 1f, 2f, target, new Vector2f());

        RippleDistortion ripple = new RippleDistortion();
        ripple.setArc(0, 360f);
        ripple.setSize(EFFECT_RANGE);
        ripple.setIntensity(90f);
        ripple.setLocation(target);
        ripple.fadeInIntensity(0.35f);
        ripple.fadeOutSize(0.45f);
        DistortionShader.addDistortion(ripple);
        if (ripple.getIntensity() < 0) {
            DistortionShader.removeDistortion(ripple);
        }
    }

}
