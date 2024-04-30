package data.ungp;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import ungp.api.rules.UNGP_BaseRuleEffect;
import ungp.api.rules.tags.UNGP_CombatTag;
import ungp.scripts.campaign.specialist.UNGP_SpecialistSettings;


public class FM_PointdeviceMode extends UNGP_BaseRuleEffect implements UNGP_CombatTag {

    private float RANGE_DEBUFF_PRE_DMOD;

    @Override
    public void updateDifficultyCache(UNGP_SpecialistSettings.Difficulty difficulty) {
        RANGE_DEBUFF_PRE_DMOD = getValueByDifficulty(0, difficulty);
    }

    @Override
    public float getValueByDifficulty(int index, UNGP_SpecialistSettings.Difficulty difficulty) {
        if (index == 0) return difficulty.getLinearValue(6f, 6f);
        return super.getValueByDifficulty(index, difficulty);
    }

    @Override
    public void advanceInCombat(CombatEngineAPI engine, float amount) {

    }

    @Override
    public void applyEnemyShipInCombat(float amount, ShipAPI enemy) {

    }

    @Override
    public void applyPlayerShipInCombat(float amount, CombatEngineAPI engine, ShipAPI ship) {
        FleetMemberAPI member = ship.getFleetMember();
        float numOfDMods = 0;
        if (member != null) {
            numOfDMods = DModManager.getNumDMods(member.getVariant());
        }
        if (numOfDMods > 0) {
            float rangeDebuff = numOfDMods * RANGE_DEBUFF_PRE_DMOD;
            ship.getMutableStats().getEnergyWeaponRangeBonus().modifyMult(buffID, 1f - (rangeDebuff / 100f));
            ship.getMutableStats().getBallisticWeaponRangeBonus().modifyMult(buffID, 1f - (rangeDebuff / 100f));
        } else {
            ship.getMutableStats().getEnergyWeaponRangeBonus().unmodifyMult(buffID);
            ship.getMutableStats().getBallisticWeaponRangeBonus().unmodifyMult(buffID);

        }
    }

    @Override
    public String getDescriptionParams(int index, UNGP_SpecialistSettings.Difficulty difficulty) {
        if (index == 0) return getPercentString(getValueByDifficulty(index, difficulty));
        return super.getDescriptionParams(index, difficulty);
    }
}
