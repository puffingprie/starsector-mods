// display a Limina (D) in the current dialogue
package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LiminaShowWreck extends BaseCommandPlugin

{
    protected Random random;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        final MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        if (localMemory == null) return false;
        CampaignFleetAPI recoverable = Global.getFactory().createEmptyFleet(Factions.NEUTRAL, FleetTypes.PATROL_SMALL, true);

        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "hmi_limina_std");
        recoverable.getFleetData().addFleetMember(member);
        for (int i = 0; i < 12; i++) {
            member.getStatus().applyDamage(5000f);
        }
        member.getRepairTracker().setCR(0);
        member.getStatus().setHullFraction(0.1f);
        dialog.getVisualPanel().showFleetMemberInfo(recoverable.getFleetData().getMembersListCopy().get(0), true);

        return true;
    }
}
