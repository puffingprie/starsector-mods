// adds a ship to the player's fleet, with appropriate text
package com.fs.starfarer.api.impl.campaign.rulecmd.salvage;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class LiminaAddShip extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    protected Random random;
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        final MemoryAPI localMemory = memoryMap.get(MemKeys.LOCAL);
        if (localMemory == null) return false;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        entity = dialog.getInteractionTarget();


        FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "hmi_limina_std");
        playerFleet.getFleetData().addFleetMember(member);
        DModManager.addDMods(member, true, 5, random);
        AddRemoveCommodity.addFleetMemberGainText(member, dialog.getTextPanel());
        for (int i = 0; i < 12; i++) {
            member.getStatus().applyDamage(5000f);
        }
        member.getRepairTracker().setCR(0);
        member.getStatus().setHullFraction(0.1f);
        member.getRepairTracker().setMothballed(true);

        Misc.fadeAndExpire(entity, 1f);

        return true;
    }
}
