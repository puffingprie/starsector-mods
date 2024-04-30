package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.I18nUtil;

public class FantasyPlayerMod extends BaseHullMod {


//    public static final float DAMAGE_TAKEN = 10f;

    public static final float BASE_ANTI_ARMOR_EFFECT = 100f;
    public static final float FLUX_BOUND = 0.75f;

    public static final Object INFO6 = new Object();


    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
//        stats.getArmorDamageTakenMult().modifyPercent(id, DAMAGE_TAKEN);
//        stats.getHullDamageTakenMult().modifyPercent(id, DAMAGE_TAKEN);
//        stats.getShieldDamageTakenMult().modifyPercent(id,DAMAGE_TAKEN);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
//        ship.addListener(new FantasyPlayerModAntiArmor());
    }

    public void advanceInCombat(ShipAPI ship, float amount) {

//        List<FleetMemberAPI> re = Global.getCombatEngine().getFleetManager(ship.getOwner()).getRetreatedCopy();
//        for (FleetMemberAPI member : re){
//            Global.getLogger(this.getClass()).info("撤退后的FantasyPlayerMod正在advance" + "" + member.getId());
//        }

        float flux_level = ship.getFluxTracker().getFluxLevel();
        float effect = (flux_level / FLUX_BOUND) * BASE_ANTI_ARMOR_EFFECT;
        if (effect >= 100f) {
            effect = 100f;
        }
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine == null) return;

        if (ship == engine.getPlayerShip()) {
            engine.maintainStatusForPlayerShip(INFO6, Global.getSettings().getSpriteName("ui", "icon_energy"), I18nUtil.getHullModString("FantasyPlayerMod_Combat_0_T"), I18nUtil.getHullModString("FantasyPlayerMod_Combat_0_D") + Math.round(effect) + "%", false);
        }

        ship.getMutableStats().getHitStrengthBonus().modifyPercent(ship.getFleetMemberId() + "_FantasyPlayerMod", effect);
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship.getVariant().getHullMods().contains(FantasyBasicMod.FANTASYBASICMOD);
    }

    public String getUnapplicableReason(ShipAPI ship) {

        if (!ship.getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
            return I18nUtil.getHullModString("FM_HullModRequireBasicMod");
        }

        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyPlayerMod_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyPlayerMod_DAE_1"), Misc.getGrayColor(), 4f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) BASE_ANTI_ARMOR_EFFECT + "%";
        if (index == 1) return "" + (int) (100f * FLUX_BOUND) + "%";

        return null;
    }
//    public static class FantasyPlayerModAntiArmor implements DamageDealtModifier {
//
//        @Override
//        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
//            CombatEngineAPI engine = Global.getCombatEngine();
//            CombatEntityAPI entity = damage.getStats().getEntity();
//
//            engine.addFloatingText(entity.getLocation(),damage.getDamage() + "",10f, Color.WHITE,entity,0f,1f);
//
//            return null;
//        }
//    }
}
