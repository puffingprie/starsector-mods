package data.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Colors;
import data.utils.I18nUtil;

public class FM_WindOfGensokyo_skill {

    public static final float SPEED_BONUS_TIME = 60f;
    public static final float SPEED_BONUS_CRUISER = 35f;
    public static final float SPEED_BONUS_CAPITAL = 25f;

    public static Object Key = new Object();


    public static class Level1 extends BaseSkillEffectDescription implements AfterShipCreationSkillEffect {

        @Override
        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            if (ship.isCapital()){
                ship.addListener(new WindOfGensokyoListener(ship,SPEED_BONUS_CAPITAL));
            }
            if (ship.isCruiser()){
                ship.addListener(new WindOfGensokyoListener(ship,SPEED_BONUS_CRUISER));
            }
        }

        @Override
        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(WindOfGensokyoListener.class);
        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            String[] hlight = {"" + (int) SPEED_BONUS_CRUISER, "" + (int) (SPEED_BONUS_CAPITAL)};
            info.addPara(I18nUtil.getString("skill", "FM_WindOfGensokyoInfo0"), 0f, hc, hc, "" + (int)SPEED_BONUS_TIME);
            info.addPara(I18nUtil.getString("skill", "FM_WindOfGensokyoInfo1"), 0f, hc, hc, hlight);
        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

        }

        @Override
        public ScopeDescription getScopeDescription() {
            return ScopeDescription.ALL_SHIPS;
        }
    }

    public static class WindOfGensokyoListener implements AdvanceableListener{

        public ShipAPI ship;
        public float bonus;
        private float timer = 0;
        private float visualTimer = 0f;
        public WindOfGensokyoListener(ShipAPI ship, float bonus){
            this.ship = ship;
            this.bonus = bonus;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine() == null)return;
            if (ship.getTravelDrive().isOn())return;
            timer = timer + amount;
            if (timer <= SPEED_BONUS_TIME){
                ship.getMutableStats().getMaxSpeed().modifyFlat(ship.getId() + "_WindOfGensokyoListener",bonus);
                CombatEngineAPI engine = Global.getCombatEngine();
                if (ship == engine.getPlayerShip()){
                    engine.maintainStatusForPlayerShip(
                            Key,
                            ship.getTravelDrive().getSpecAPI().getIconSpriteName(),
                            I18nUtil.getString("skill","FM_WindOfGensokyoStateTitle"),
                            I18nUtil.getString("skill","FM_WindOfGensokyoStateData") + Misc.getRoundedValueOneAfterDecimalIfNotWhole(SPEED_BONUS_TIME - timer),
                            false
                    );
                }
                ship.getEngineController().getExtendGlowFraction().shift(
                        ship,
                        1.2f,
                        0.5f,
                        0.5f,
                        0.5f
                );
                ship.getEngineController().fadeToOtherColor(
                        ship,
                        FM_Colors.FM_GREEN_EMP_FRINGE,
                        Misc.scaleAlpha(FM_Colors.FM_GREEN_EMP_FRINGE,0.3f),
                        1f,
                        0.7f
                );
//                visualTimer = visualTimer + amount;
//                if (visualTimer >= 0.05f){
//                    for (int i = 0; i < 4; i = i + 1){
//                        engine.addHitParticle(
//                                MathUtils.getRandomPointInCircle(ship.getLocation(),ship.getCollisionRadius() * 0.25f),
//                                FM_Misc.ZERO,
//                                MathUtils.getRandomNumberInRange(4f,8f),
//                                MathUtils.getRandomNumberInRange(0.8f,1f),
//                                MathUtils.getRandomNumberInRange(1.9f,2.6f),
//                                ship.getEngineController().getShipEngines().get(1).getEngineColor()
//                        );
//                    }
//                    visualTimer = visualTimer - 0.1f;
//                }

            }else {
                ship.getMutableStats().getMaxSpeed().unmodify(ship.getId() + "_WindOfGensokyoListener");
            }
        }
    }
}
