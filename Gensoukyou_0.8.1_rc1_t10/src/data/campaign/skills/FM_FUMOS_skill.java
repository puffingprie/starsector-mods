package data.campaign.skills;

import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.utils.I18nUtil;
import org.lwjgl.util.vector.Vector2f;

public class FM_FUMOS_skill {
    public static final float SHIELD_ACC = 100f;
    public static final float MISSILE_DAMAGE_MULT = 0.75f;
    public static final float HIGH_DPH_MULT = 0.5f;
    public static final float DPH_CEIL = 250f;

    public static class Level1 implements ShipSkillEffect {

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_ACC);
            stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_ACC);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getShieldTurnRateMult().unmodify(id);
            stats.getShieldUnfoldRateMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "+" + (int) SHIELD_ACC + "%" + I18nUtil.getString("skill", "FM_FUMOSInfo0");
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

    }

    public static class Level1X implements ShipSkillEffect {

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getMissileShieldDamageTakenMult().modifyMult(id, MISSILE_DAMAGE_MULT);
            stats.getBeamShieldDamageTakenMult().modifyMult(id, MISSILE_DAMAGE_MULT);
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getMissileShieldDamageTakenMult().unmodify(id);
            stats.getBeamShieldDamageTakenMult().unmodify(id);
        }

        @Override
        public String getEffectDescription(float level) {
            return "-" + (int) (100f - 100f * MISSILE_DAMAGE_MULT) + "%" + I18nUtil.getString("skill", "FM_FUMOSInfo1");

        }

        @Override
        public String getEffectPerLevelDescription() {
            return null;
        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level2 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

//        public String getEffectDescription(float level) {
//            return I18nUtil.getString("skill","FM_FUMOSInfo1") + DPH_CEIL + I18nUtil.getString("skill","FM_FUMOSInfo2");
//        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            String[] hlight = {"" + (int) DPH_CEIL, "" + (int) (HIGH_DPH_MULT * 100f) + "%"};
            info.addPara(I18nUtil.getString("skill", "FM_FUMOSInfo2"), 0f, hc, hc, hlight);
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener(new FUMOSListener(ship));
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(FUMOSListener.class);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

        }
    }

    public static class FUMOSListener implements DamageTakenModifier {
        protected ShipAPI ship;

        public FUMOSListener(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (shieldHit) {
                if (!damage.isDps() && damage.computeDamageDealt(damage.getDamage()) >= DPH_CEIL && damage.getType() == DamageType.KINETIC) {
                    float damageCheck = (damage.getDamage() - DPH_CEIL) * 0.5f;

                    damage.getModifier().modifyFlat(ship.getId() + "_FUMOSListener", -damageCheck/damage.getDamage());
                    //damage.getModifier().modifyMult(ship.getId() + "_FUMOSListener", HIGH_DPH_MULT);
//                    if (Global.getCombatEngine() != null){
//                        FM_DiamondParticle3DTest manager = FM_ParticleManager.getDiamondParticleManager(Global.getCombatEngine());
//                        for (int i = 0; i < 12; i = i + 1){
//                            manager.addDiamondParticle(
//                                    point,
//                                    MathUtils.getRandomPointInCircle(FM_Misc.ZERO,225f),
//                                    MathUtils.getRandomNumberInRange(10f,14f),
//                                    0.03f,
//                                    0.37f,
//                                    Color.WHITE,
//                                    7f,
//                                    MathUtils.getRandomNumberInRange(0,360f),
//                                    MathUtils.getRandomNumberInRange(180f,540f),MathUtils.getRandomNumberInRange(180f,540f),Math.random() < 0.5f
//                            );
//                        }
//                    }
                }
            }
            return null;
        }
    }
}
