package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_ProjectEffect;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicIncompatibleHullmods;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyKawausoMod extends BaseHullMod {


    public static final Color EFFECT_1 = new Color(31, 236, 157, 226);
    public static final Color EFFECT_2 = new Color(202, 246, 233, 226);
    public static final Color EMP = new Color(22, 239, 239, 205);
    public static final Color EMP_CORE = new Color(31, 236, 157, 119);

    //    public static final float NONPD_MULT= 0.8f;
    public static final float FLUX_MULT = 1f;
    public static final float ROTATE_SPEED_MULT = 0.9f;
    public static final float EMP_CD = 0.4f;
    public static final Map<ShipAPI.HullSize, Float> DAMAGEMap = new HashMap<>();
    public static final float EMP_AMOUNT = 200f;
    private static final Map<ShipAPI.HullSize, Float> mag = new HashMap();

    static {
        DAMAGEMap.put(ShipAPI.HullSize.FIGHTER, 0f);
        DAMAGEMap.put(ShipAPI.HullSize.FRIGATE, 25f);
        DAMAGEMap.put(ShipAPI.HullSize.DESTROYER, 50f);
        DAMAGEMap.put(ShipAPI.HullSize.CRUISER, 75f);
        DAMAGEMap.put(ShipAPI.HullSize.CAPITAL_SHIP, 100f);
    }

    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 100f);
        mag.put(ShipAPI.HullSize.DESTROYER, 125f);
        mag.put(ShipAPI.HullSize.CRUISER, 150f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 200f);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize,
                                               MutableShipStatsAPI stats, String id) {

        if (stats.getVariant().getHullMods().contains("swp_pdconversion")) {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(), "swp_pdconversion", "FantasyKawausoMod");
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
//        ship.addListener(new FantasyNonPDBaseRangeMult(NONPD_MULT));
        List<WeaponAPI> weapons = ship.getAllWeapons();
        for (WeaponAPI weapon : weapons) {
            //			if (weapon.hasAIHint(AIHints.PD)) {
//				weapon.get
//			}
            boolean sizeMatches = weapon.getSize() == WeaponAPI.WeaponSize.MEDIUM && ship.isCruiser();
            //sizeMatches |= weapon.getSize() == WeaponSize.MEDIUM;
            if (sizeMatches && weapon.getType() != WeaponAPI.WeaponType.MISSILE) {
                weapon.setPD(true);
                weapon.setPDAlso(true);
            }
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        if (!engine.getCustomData().containsKey("FantasyKawausoMod")) {
            engine.getCustomData().put("FantasyKawausoMod", new HashMap<>());
        }
        Map<ShipAPI, ModState> currState = (Map) engine.getCustomData().get("FantasyKawausoMod");
        if (!currState.containsKey(ship)) {
            currState.put(ship, new ModState());
        }
        if (!ship.getTravelDrive().isActive()) {
            currState.get(ship).timer = currState.get(ship).timer + amount;
            if (currState.get(ship).timer >= 1f) {
                currState.get(ship).timer = 1f;
            }
            currState.get(ship).isActive = true;
        }
        if (!currState.get(ship).isActive || !ship.isAlive()) {
            return;
        }
        List<DamagingProjectileAPI> projects = FM_ProjectEffect.ProjectsThisFrame;
        //初始状态
        if (currState.get(ship).sprites.isEmpty()) {
            for (int i = 0; i < 3; i = i + 1) {
                SpriteAPI sprite = Global.getSettings().getSprite("fx", "FM_modeffect_5");
                currState.get(ship).sprites.add(i, sprite);
                currState.get(ship).sprite_angle_0.put(sprite, 120f * i);
                currState.get(ship).trail_id.put(sprite, MagicTrailPlugin.getUniqueID());
                float angel_s = currState.get(ship).sprite_angle_0.get(sprite);
                currState.get(ship).sprite_loc.put(sprite, MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius() * 2f, angel_s));
            }
        }
        currState.get(ship).angle = currState.get(ship).angle + amount * ROTATE_SPEED_MULT;
        currState.get(ship).empArcTimer = currState.get(ship).empArcTimer + amount;

        if (currState.get(ship).angle == 360f) {
            currState.get(ship).angle = 0f;
        }
        for (SpriteAPI sprite : currState.get(ship).sprites) {
            float angel_s = currState.get(ship).angle * 50f + currState.get(ship).sprite_angle_0.get(sprite);
            sprite.setAlphaMult(currState.get(ship).timer);
            currState.get(ship).sprite_loc.put(sprite, MathUtils.getPoint(ship.getLocation(), ship.getCollisionRadius() * 2f, angel_s));
            if (currState.get(ship).timer >= 1) {
                MagicTrailPlugin.addTrailMemberAdvanced(
                        ship,
                        currState.get(ship).trail_id.get(sprite),
                        Global.getSettings().getSprite("fx", "FM_trail_2"),
                        currState.get(ship).sprite_loc.get(sprite),
                        0,
                        0,
                        angel_s - 90,
                        0f,
                        0f,
                        60f,
                        180f,
                        EFFECT_1,
                        EFFECT_2,
                        1f,
                        0.2f,
                        0.3f,
                        1f,
                        GL11.GL_BLEND_SRC,
                        GL11.GL_ONE_MINUS_CONSTANT_ALPHA,
                        256f,
                        10,
                        10f,
                        null,
                        null,
                        CombatEngineLayers.BELOW_SHIPS_LAYER,
                        60f
                );
            }
            //engine.addSmoothParticle(loc,new Vector2f(),10f,10f,-1f, 2f,EFFECT_1);
            MagicRender.singleframe(sprite, currState.get(ship).sprite_loc.get(sprite), new Vector2f(28, 28), angel_s,
                    EFFECT_2, true, CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        }

        if (currState.get(ship).empArcTimer >= EMP_CD) {
            if (ship.getFluxTracker().isOverloadedOrVenting()) return;
            for (DamagingProjectileAPI project : projects) {
                if (!MathUtils.isWithinRange(ship, project, ship.getCollisionRadius() * 4f)) continue;
                for (SpriteAPI sprite : currState.get(ship).sprites) {
                    Vector2f loc = currState.get(ship).sprite_loc.get(sprite);
                    if (!MathUtils.isWithinRange(loc, project.getLocation(), mag.get(ship.getHullSize()))) {
                        continue;
                    }
                    if (project instanceof MissileAPI && project.getOwner() != ship.getOwner()) {
                        if (ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux() < DAMAGEMap.get(ship.getHullSize()) * FLUX_MULT)
                            continue;
                        ship.getFluxTracker().increaseFlux(DAMAGEMap.get(ship.getHullSize()) * FLUX_MULT, false);
//                        engine.removeEntity(project);
                        engine.spawnEmpArc(
                                ship,
                                loc,
                                ship,
                                project,
                                DamageType.ENERGY,
                                DAMAGEMap.get(ship.getHullSize()),
                                EMP_AMOUNT,
                                1000f,
                                "tachyon_lance_emp_impact",
                                12f,
                                EMP,
                                EMP_CORE
                        );
//                        engine.addNebulaParticle(project.getLocation(),(Vector2f) project.getVelocity().scale(0.3f),project.getCollisionRadius() * 3f,
//                                3f,-1f,2f,3f,
//                                EFFECT_3);
                        //Global.getSoundPlayer().playSound("ui_drone_mode_deploy",2f,0.3f,project.getLocation(),new Vector2f());
                    }
                }
            }

            for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, ship.getCollisionRadius() * 4f)) {
                if (!enemy.isFighter() || !enemy.isAlive()) continue;
                for (SpriteAPI sprite : currState.get(ship).sprites) {
                    Vector2f loc = currState.get(ship).sprite_loc.get(sprite);
                    if (!MathUtils.isWithinRange(loc, enemy.getLocation(), mag.get(ship.getHullSize()))) {
                        continue;
                    }
                    if (ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux() < DAMAGEMap.get(ship.getHullSize()) * FLUX_MULT)
                        continue;
                    ship.getFluxTracker().increaseFlux(DAMAGEMap.get(ship.getHullSize()) * FLUX_MULT, false);
                    engine.spawnEmpArc(
                            ship,
                            loc,
                            ship,
                            enemy,
                            DamageType.ENERGY,
                            DAMAGEMap.get(ship.getHullSize()),
                            EMP_AMOUNT,
                            1000f,
                            "tachyon_lance_emp_impact",
                            12f,
                            EMP,
                            EMP_CORE
                    );

                }
            }


            currState.get(ship).empArcTimer -= EMP_CD;
        }

        //engine.addFloatingText(ship.getLocation(),String.valueOf(currState.get(ship).sprites.size()),20f,Color.WHITE,ship,1f,5f);
        //engine.addFloatingText(ship.getLocation(),String.valueOf(currState.get(ship).angle),20f,Color.WHITE,ship,1f,1f);


    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {


//        if (index == 0) return "" + (int)(NONPD_MULT * 100f) + "%";
//        if (index == 1) return "" + EMP_CD + "s";
//        if (index == 2) return "" + (int)(DAMAGE);
//        if (index == 3) return "" + (int)(EMP_AMOUNT);


        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD) && !ship.isCapital();
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        if (ship.isCapital()) {
            return I18nUtil.getHullModString("FantasyKawausoModUnapplicable");
        }

        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        String[] data1 = {String.valueOf(mag.get(ShipAPI.HullSize.FRIGATE).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.DESTROYER).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.CRUISER).intValue()),
                String.valueOf(mag.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue()),
                "" + EMP_CD + "s"};
        String[] data3 = {String.valueOf(DAMAGEMap.get(ShipAPI.HullSize.FRIGATE).intValue()),
                String.valueOf(DAMAGEMap.get(ShipAPI.HullSize.DESTROYER).intValue()),
                String.valueOf(DAMAGEMap.get(ShipAPI.HullSize.CRUISER).intValue()),
                String.valueOf(DAMAGEMap.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue()),
                "" + (int) (EMP_AMOUNT),
                "" + FLUX_MULT + "x"
        };
        tooltip.addSpacer(10f);
        //说明
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_0"), Misc.getHighlightColor(), 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_1"), 4f, Misc.getHighlightColor(), data1);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_2"), 4f, Misc.getHighlightColor(), data3);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_I_3"), Misc.getHighlightColor(), 4f);
        tooltip.addSpacer(10f);
        //描述与评价
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_DAE_1"), Misc.getGrayColor(), 4f);
    }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
    }
        tooltip.addPara(I18nUtil.getHullModString("FantasyKawausoMod_DAE_2"), Misc.getNegativeHighlightColor(), 4f);

    }

//    public static class FantasyNonPDBaseRangeMult implements WeaponBaseRangeModifier {
//        public float baseMult;
//        public FantasyNonPDBaseRangeMult(float baseMult) {
//            this.baseMult = baseMult;
//        }
//
//        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
//            return 0;
//        }
//        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
//            if (weapon.getSlot() == null || !weapon.hasAIHint(WeaponAPI.AIHints.PD)) {
//                return baseMult;
//            }
//            return 1f;
//        }
//        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
//            return 0f;
//        }
//    }

    private final static class ModState {
        boolean isActive;
        List<SpriteAPI> sprites;
        float timer;
        float empArcTimer;
        float angle;
        Map<SpriteAPI, Float> sprite_angle_0;
        Map<SpriteAPI, Vector2f> sprite_loc;
        Map<SpriteAPI, Float> trail_id;

        private ModState() {
            sprites = new ArrayList<>();
            isActive = false;
            timer = 0f;
            empArcTimer = 0f;
            angle = 0f;
            sprite_angle_0 = new HashMap<>();
            sprite_loc = new HashMap<>();
            trail_id = new HashMap<>();
        }
    }

}
