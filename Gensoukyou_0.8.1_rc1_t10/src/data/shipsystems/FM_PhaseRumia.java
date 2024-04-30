package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.FantasySpellMod;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.FM_ProjectEffect;
import data.utils.I18nUtil;
import data.utils.visual.FM_ParticleManager;
import data.utils.visual.FM_PhaseRumiaVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class FM_PhaseRumia extends BaseShipSystemScript {

//    public static Color JITTER_COLOR = new Color(255,175,255,255);
//    public static float JITTER_FADE_TIME = 0.5f;

    public static final float SHIP_ALPHA_MULT = 0.25f;
    public static final float MAX_TIME_MULT = 3f;
    public static final float BASE_FLUX_LEVEL_FOR_MIN_SPEED = 0.5f;
    public static final float MIN_SPEED_MULT = 0.33f;
    public static boolean FLUX_LEVEL_AFFECTS_SPEED = true;
    public static final float DAMAGE_TO_SPELL = 0.02f;

    protected Object STATUS1 = new Object();
    protected Object STATUS2 = new Object();

    private boolean explosion = false;
//    private WaveDistortion wave = null;
//    private float timer = 0.5f;
    //private boolean render = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        if (Global.getCombatEngine() == null) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!(stats.getEntity() instanceof ShipAPI)) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        //灵基框架是必须的.png
        if (!ship.getVariant().hasHullMod("FantasySpellMod")) return;
        //
        ShipSystemAPI system = ship.getPhaseCloak();
        if (system == null) {
            system = ship.getSystem();
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        boolean player = ship == engine.getPlayerShip();
        if (player) {
            maintainStatus(ship, effectLevel, system);
        }
        //相位效果与视觉
        if (state == State.IN || state == State.ACTIVE) {
            ship.setPhased(true);

            if (!explosion) {
                FM_PhaseRumiaVisual.PhaseRumiaParam visual = new FM_PhaseRumiaVisual.PhaseRumiaParam(ship, FM_Colors.FM_RED_EMP_FRINGE);
                visual.thickness = 55f;
                engine.addLayeredRenderingPlugin(new FM_PhaseRumiaVisual(visual));
                Global.getSoundPlayer().playSound("FM_Opposition_expand", 1f, 1f, ship.getLocation(), new Vector2f());

//                wave = new WaveDistortion(ship.getLocation(),FM_Misc.ZERO);
//                wave.setIntensity(40f);
//                DistortionShader.addDistortion(wave);

                explosion = true;
            }

//            if (wave != null){
//                wave.setSize(ship.getCollisionRadius());
//                wave.setLocation(ship.getLocation());
//            }

//            MagicRender.singleframe(
//                    Global.getSettings().getSprite("misc","FM_PetaFlare_project_sprite_p"),
//                    ship.getLocation(),
//                    new Vector2f(256,256),
//                    ship.getFacing() + 90f,
//                    ship.getVentCoreColor(),
//                    true,
//                    CombatEngineLayers.ABOVE_SHIPS_LAYER
//            );

//            timer = timer - engine.getElapsedInLastFrame();
//            if (timer <= 0f){
//                timer = 0.5f;
//            }
            ship.addAfterimage(FM_Colors.FM_DARK_BLUE, 0, 0, -ship.getVelocity().x, -ship.getVelocity().y, 0.2f * effectLevel, 1f, 0f, 0.5f, true, false, true);
        } else if (state == State.OUT) {
            ship.setPhased(effectLevel > 0.5f);
        }
//        ship.setAlphaMult(1f - 0.75f * effectLevel);
        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * effectLevel);
        ship.setApplyExtraAlphaToEngines(true);
        //ship.setJitter(ship, Misc.scaleAlpha(FM_Colors.FM_ORANGE_FLARE_FRINGE,0.33f),effectLevel,Math.round(20 * effectLevel),effectLevel * 20f);
        //时流效果
        float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * effectLevel;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }


        //相位状态下机动性降低
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (state == State.ACTIVE || state == State.OUT || state == State.IN) {
                float mult = getSpeedMult(ship, effectLevel);
                if (mult < 1f) {
                    stats.getMaxSpeed().modifyMult(id + "_maxSpeed", mult);
                } else {
                    stats.getMaxSpeed().unmodifyMult(id + "_maxSpeed");
                }
                ((PhaseCloakSystemAPI) system).setMinCoilJitterLevel(getDisruptionLevel(ship));

            }
        }

        if (state == State.COOLDOWN || state == State.IDLE) {
//            if (wave != null){
//                wave.fadeOutSize(0.3f);
//                if (wave.getSize() <= 1f){
//                    DistortionShader.removeDistortion(wave);
//                    wave = null;
//                }
//            }
            unapply(stats, id);
            return;
        }

        //看起来是不同的计算最高速度的内容……

        float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
        float accelPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).computeEffective(0f);
        stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);
        stats.getAcceleration().modifyPercent(id, accelPercentMod * effectLevel);
        stats.getDeceleration().modifyPercent(id, accelPercentMod * effectLevel);

        float speedMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).getMult();
        float accelMultMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_ACCEL_MOD).getMult();
        stats.getMaxSpeed().modifyMult(id, speedMultMod * effectLevel);
        stats.getAcceleration().modifyMult(id, accelMultMod * effectLevel);
        stats.getDeceleration().modifyMult(id, accelMultMod * effectLevel);

        //消弹和填充能量
        for (DamagingProjectileAPI project : FM_ProjectEffect.ProjectsThisFrame) {
            //Vector2f vel = project.getVelocity();
            if (project.getOwner() != ship.getOwner() && MathUtils.isWithinRange(project.getLocation(), ship.getLocation(), ship.getCollisionRadius())) {
//                engine.addSmoothParticle(project.getLocation(),
//                        MathUtils.getRandomPointInCone(FM_Misc.ZERO,vel.length() * 0.05f, VectorUtils.getFacing(vel)-5f,VectorUtils.getFacing(vel)+5f),
//                        project.getCollisionRadius(),
//                        255f,
//                        MathUtils.getRandomNumberInRange(0.5f,1.5f),
//                        FM_Colors.FM_RED_EMP_FRINGE
//                );


                FM_ParticleManager.getDiamondParticleManager(engine).addDiamondParticle(
                        project.getLocation(),
                        (Vector2f) project.getVelocity().scale(0.5f),
                        10f,
                        0.2f,
                        1.0f,
                        FM_Colors.FM_TEXT_RED,
                        6f,
                        MathUtils.getRandomNumberInRange(0, 360),
                        MathUtils.getRandomNumberInRange(240, 480),
                        MathUtils.getRandomNumberInRange(90, 180),
                        Math.random() < 0.5f
                );
                FantasySpellMod.SpellModState modState = FM_Misc.getSpellModState(engine, ship);
                modState.spellPower = modState.spellPower + project.getDamageAmount() * DAMAGE_TO_SPELL * 0.01f;
                engine.addHitParticle(project.getLocation(), FM_Misc.ZERO, Math.min(project.getDamageAmount(), 300f), 1f, 0.4f, Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE, 0.5f));
                engine.removeEntity(project);


            }

        }


    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);

        if (Global.getCombatEngine() == null) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!(stats.getEntity() instanceof ShipAPI)) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        ShipSystemAPI system = ship.getPhaseCloak();
        if (system == null) {
            system = ship.getSystem();
        }
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        engine.getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        stats.getMaxSpeed().unmodify(id);
        stats.getMaxSpeed().unmodifyMult(id + "_maxSpeed");
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);

        ((PhaseCloakSystemAPI) system).setMinCoilJitterLevel(0f);

        explosion = false;

        //render = false;


    }

    protected void maintainStatus(ShipAPI playerShip, float effectLevel, ShipSystemAPI system) {
        float f = 0.25f;

        if (effectLevel > f) {
            Global.getCombatEngine().maintainStatusForPlayerShip(STATUS1,
                    system.getSpecAPI().getIconSpriteName(), system.getDisplayName(), I18nUtil.getShipSystemString("FM_PhaseRumiaInfo0"), false);
        }

        if (FLUX_LEVEL_AFFECTS_SPEED) {
            if (effectLevel > f) {
                if (getDisruptionLevel(playerShip) <= 0f) {
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUS2,
                            system.getSpecAPI().getIconSpriteName(), I18nUtil.getShipSystemString("FM_PhaseRumiaTitle1"), I18nUtil.getShipSystemString("FM_PhaseRumiaInfo1"), false);
                } else {
                    String speedPercentStr = Math.round(getSpeedMult(playerShip, effectLevel) * 100f) + "%";
                    Global.getCombatEngine().maintainStatusForPlayerShip(STATUS2,
                            system.getSpecAPI().getIconSpriteName(),
                            I18nUtil.getShipSystemString("FM_PhaseRumiaTitle2"),
                            I18nUtil.getShipSystemString("FM_PhaseRumiaInfo2") + speedPercentStr, true);
                }
            }
        }
    }

    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    protected float getDisruptionLevel(ShipAPI ship) {
        if (FLUX_LEVEL_AFFECTS_SPEED) {
            float threshold = ship.getMutableStats().getDynamic().getMod(
                    Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).computeEffective(BASE_FLUX_LEVEL_FOR_MIN_SPEED);
            if (threshold <= 0) return 1f;
            float level = ship.getHardFluxLevel() / threshold;
            if (level > 1f) level = 1f;
            return level;
        }
        return 0f;
    }

    public float getSpeedMult(ShipAPI ship, float effectLevel) {
        if (getDisruptionLevel(ship) <= 0f) return 1f;
        return MIN_SPEED_MULT + (1f - MIN_SPEED_MULT) * (1f - getDisruptionLevel(ship) * effectLevel);
    }

}
