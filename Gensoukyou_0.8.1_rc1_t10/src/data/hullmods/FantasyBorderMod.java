package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyBorderMod extends BaseHullMod {

    public static final float EFFECT_TIME = 1f;
    public static final float SHIELD_DAMAGE_WEIGHT = 0.25f;
    public static Map<ShipAPI.HullSize, Float> magDAMAGE = new HashMap();

    static {
        magDAMAGE.put(ShipAPI.HullSize.FIGHTER, 0f);
        magDAMAGE.put(ShipAPI.HullSize.FRIGATE, 3500f);
        magDAMAGE.put(ShipAPI.HullSize.DESTROYER, 7000f);
        magDAMAGE.put(ShipAPI.HullSize.CRUISER, 16000f);
        magDAMAGE.put(ShipAPI.HullSize.CAPITAL_SHIP, 32000f);
    }

    public static final float FLUX_LEVEL = 0.8f;
    public static final int NUM_OF_BOMBS = 2;
    public static final int UPPER_BOUND = 4;
    public static final float FLUX_REDUCE = 0.35f;

    public static final Color SHIP = new Color(151, 8, 53, 255);
    public static final Color EMP1 = new Color(213, 96, 96, 255);
    public static final Color EMP2 = new Color(123, 0, 0, 184);

    public static Object INFO2 = new Object();
    public static Object INFO1 = new Object();

    //FM_LocalData.FM_Data currdata = FM_LocalData.getCurrData();

//    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
//
//        if(ship == null)return;
//
//        currdata.numberOfBombs.put(ship,NUM_OF_BOMBS);
//    }


    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        if (!engine.getCustomData().containsKey("FantasyBorderMod")) {
            engine.getCustomData().put("FantasyBorderMod", new HashMap<>());
        }
        //一个全局的listener
        if (!engine.getListenerManager().hasListenerOfClass(FantasyBorderModDamageListener.class)) {
            engine.getListenerManager().addListener(new FantasyBorderModDamageListener(engine));
        }
        if (ship.isAlive()) {
            float fluxlevel = ship.getFluxTracker().getFluxLevel();
            Map<ShipAPI, ModState> currState = (Map) engine.getCustomData().get("FantasyBorderMod");
            if (!currState.containsKey(ship)) {
                currState.put(ship, new ModState());
            }
            //检测幅能
            if (currState.get(ship).num > 0) {
                if (ship.getAI() != null) {
                    ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF);
                    //ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);
                    ship.getAIFlags().unsetFlag(ShipwideAIFlags.AIFlags.DO_NOT_PURSUE);
                }
                if (fluxlevel > FLUX_LEVEL || ship.getFluxTracker().isOverloaded()) {
                    currState.get(ship).isActive = true;
                }
            } else {
                ship.getAIFlags().unsetFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF);
                //ship.getAIFlags().unsetFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON);
            }
            float base = ship.getMaxFlux() * (FLUX_LEVEL - FLUX_REDUCE);
            //测试效果
            if (currState.get(ship).isActive) {
                currState.get(ship).timer = currState.get(ship).timer + amount;
//                    ship.getMutableStats().getShieldDamageTakenMult().modifyMult(ship.getId() + "_FantasyBorderModFlux",0f);
                ship.getFluxTracker().setCurrFlux(base + (ship.getFluxTracker().getCurrFlux() - base) * (1f - currState.get(ship).timer * currState.get(ship).timer));
                if (ship.getHardFluxLevel() >= 1f - FLUX_REDUCE) {
                    ship.getFluxTracker().setHardFlux(base + (ship.getFluxTracker().getCurrFlux() - base) * (1f - currState.get(ship).timer * currState.get(ship).timer));
                }
                ship.setJitterUnder(ship, SHIP, 3f, 2, 3f);
                if (ship.getShield() != null){
                    ship.getShield().toggleOn();
                }
                Global.getSoundPlayer().playLoop("FM_bordermod_se", ship, 1f, 1f, ship.getLocation(), new Vector2f());

                //消弹
                float effect_range = FantasyBasicMod.magRANGE.get(ship.getHullSize());
                List<DamagingProjectileAPI> projects = engine.getProjectiles();
                for (DamagingProjectileAPI project : projects) {
                    if (project.getOwner() != ship.getOwner() && MathUtils.isWithinRange(project, ship, effect_range)) {
                        engine.spawnEmpArc(ship, ship.getLocation(), ship, project, DamageType.ENERGY, 0f, 0f, 100000f,
                                "tachyon_lance_emp_impact", 30f, EMP2, EMP1);
                        //engine.spawnExplosion(project.getLocation(), new Vector2f(), SHIP, project.getCollisionRadius(), 1f);

                        NegativeExplosionVisual.NEParams neEffect = new NegativeExplosionVisual.NEParams();
                        neEffect.color = SHIP;
                        neEffect.thickness = 8f;
                        neEffect.radius = Math.min(project.getCollisionRadius() * 0.6f, 14f);
                        neEffect.fadeOut = MathUtils.getRandomNumberInRange(0.25f, 0.6f);
                        neEffect.underglow = EMP2;
                        neEffect.invertForDarkening = EMP1;

                        CombatEntityAPI visual = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(neEffect));
                        visual.getLocation().set(project.getLocation());


                        engine.removeEntity(project);
                    }
                }

            }
            if (currState.get(ship).timer > EFFECT_TIME) {
                currState.get(ship).timer = 0f;
                currState.get(ship).isActive = false;
                ship.getFluxTracker().stopOverload();
//                    ship.getMutableStats().getShieldDamageTakenMult().unmodifyMult(ship.getId() + "_FantasyBorderModFlux");
                currState.get(ship).num = currState.get(ship).num - 1;
            }

            //debug
            //engine.addFloatingText(ship.getLocation(), String.valueOf(currState.get(ship).num),10, Color.WHITE,ship,1,1);
            //Global.getLogger(this.getClass()).info(currState.get(ship).num);

            if (ship == engine.getPlayerShip()) {
                engine.maintainStatusForPlayerShip(INFO2, Global.getSettings().getSpriteName("ui", "icon_energy"), I18nUtil.getHullModString("FantasyBorderMod_Combat_0_T"), I18nUtil.getHullModString("FantasyBorderMod_Combat_0_D") +
                                                                                                                                                                             currState.get(ship).num, false);
                engine.maintainStatusForPlayerShip(INFO1, Global.getSettings().getSpriteName("ui", "icon_energy"), I18nUtil.getHullModString("FantasyBorderMod_Combat_1_T"),
                        (int) (magDAMAGE.get(ship.getHullSize()) - currState.get(ship).damageRe) + I18nUtil.getHullModString("FantasyBorderMod_Combat_1_D"), false);
            }
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD) && ship.getShield() != null;
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }
        if (ship.getShield() == null) {
            return I18nUtil.getHullModString("FM_HullModRequireShield");
        }

        return null;
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) (FLUX_LEVEL * 100) + "%";
        if (index == 1) return "" + (int) EFFECT_TIME;
        if (index == 2) return "" + NUM_OF_BOMBS;
        if (index == 3) return "" + UPPER_BOUND;
        if (index == 4) return String.valueOf(magDAMAGE.get(ShipAPI.HullSize.FRIGATE).intValue());
        if (index == 5) return String.valueOf(magDAMAGE.get(ShipAPI.HullSize.DESTROYER).intValue());
        if (index == 6) return String.valueOf(magDAMAGE.get(ShipAPI.HullSize.CRUISER).intValue());
        if (index == 7) return String.valueOf(magDAMAGE.get(ShipAPI.HullSize.CAPITAL_SHIP).intValue());
        if (index == 8) return "" + (int) (100 * SHIELD_DAMAGE_WEIGHT) + "%";
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_Instruction"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_I_0"), 4f, Misc.getTextColor(), Misc.getHighlightColor(), "" + (int) (FLUX_REDUCE * 100f) + "%");
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyBorderMod_DAE_1"), Misc.getGrayColor(), 4f);
    }

    public static class FantasyBorderModDamageListener implements DamageListener {
        public CombatEngineAPI engine;
        public ShipAPI ship;

        public FantasyBorderModDamageListener(CombatEngineAPI engine) {
            this.engine = engine;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (engine == null) return;
            if (!(source instanceof ShipAPI)) return;
            if (!(target instanceof ShipAPI)) return;
            if (!((ShipAPI) source).getVariant().hasHullMod("FantasyBorderMod")) return;
            if (source == target) return;
            if (!((ShipAPI) target).isAlive()) return;
            if (result.isDps()) return;
            ShipAPI ship = (ShipAPI) source;
            if (!ship.isAlive()) return;
            if (target.getOwner() == ship.getOwner()) return;
            float damage = result.getDamageToShields() * SHIELD_DAMAGE_WEIGHT + result.getTotalDamageToArmor() + result.getDamageToHull();
//            Global.getLogger(this.getClass()).info(damage);
            if (engine.getCustomData().containsKey("FantasyBorderMod")) {
                Map<ShipAPI, ModState> currState = (Map) engine.getCustomData().get("FantasyBorderMod");
                if (currState.containsKey(ship)) {
                    if (currState.get(ship).num >= UPPER_BOUND) return;
                    currState.get(ship).damageRe = currState.get(ship).damageRe + damage;
                    if (currState.get(ship).damageRe >= magDAMAGE.get(ship.getHullSize())) {
                        currState.get(ship).num = currState.get(ship).num + 1;
                        currState.get(ship).damageRe = 0f;
                    }
                }
            }
            //engine.addFloatingText(ship.getLocation(),"" + damage,10f,Color.WHITE,ship,0f,0f);
        }
    }

    private final static class ModState {
        boolean isActive;
        float timer;
        float damageRe;
        int num;

        private ModState() {
            isActive = false;
            timer = 0f;
            damageRe = 0f;
            num = 3;
        }
    }
}
