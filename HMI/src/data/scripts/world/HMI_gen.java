package data.scripts.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unchecked")
    public class HMI_gen implements SectorGeneratorPlugin {
    @Override
    public void generate(SectorAPI sector) {
        new HMI_mercy().generate(sector);
        new HMI_hazard().generate(sector);
        new HMI_obsidian().generate(sector);
        new HMI_kamikaze().generate(sector);
        new HMI_opuntia().generate(sector);
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("HMI");
//    }
//    
//        public static void initFactionRelationships(SectorAPI sector) {

        FactionAPI hmi = sector.getFaction("HMI");
        FactionAPI mess = sector.getFaction("mess");
        FactionAPI mess_remnant = sector.getFaction("mess_remnant");
        FactionAPI knights_of_eva = sector.getFaction("knights_of_eva");
        FactionAPI hmi_nightmare = sector.getFaction("hmi_nightmare");
		FactionAPI legio = sector.getFaction("tahlan_legioinfernalis");
        FactionAPI hmi_hitech = sector.getFaction("hmi_obs_hitech");
        FactionAPI hmi_lowtech = sector.getFaction("hmi_obs_lowtech");
        FactionAPI hmi_midtech = sector.getFaction("hmi_obs_midtech");
        FactionAPI hmi_luddtech = sector.getFaction("hmi_obs_luddtech");

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI persean = sector.getFaction(Factions.PERSEAN);

        hmi.setRelationship("player_npc", RepLevel.NEUTRAL);
        hmi.setRelationship(Factions.PLAYER, RepLevel.NEUTRAL);
        hmi.setRelationship(hegemony.getId(), -0.2f);
        hmi.setRelationship(tritachyon.getId(), 0.2f);
        hmi.setRelationship(pirates.getId(), -0.7f);
        hmi.setRelationship(independent.getId(), -0.4f);
        hmi.setRelationship(persean.getId(), 0.1f);
        hmi.setRelationship(church.getId(), -0.7f);
        hmi.setRelationship(path.getId(), -0.7f);
        hmi.setRelationship(kol.getId(), 0f);
        hmi.setRelationship(diktat.getId(), 0f);
        hmi.setRelationship("exigency", -0.50f);
        hmi.setRelationship("shadow_industry", -0.4f);
        hmi.setRelationship("mayorate", -0.2f);
        hmi.setRelationship("blackrock", -0.4f);
        hmi.setRelationship("tiandong", -0.4f);
        hmi.setRelationship("SCY", -0.4f);
        hmi.setRelationship("neutrinocorp", 0.1f);
        hmi.setRelationship("interstellarimperium", -0.2f);
        hmi.setRelationship("diableavionics", -0.4f);
        hmi.setRelationship("ora", -1f);
        hmi.setRelationship("apex_design",-0.8f);


        List<FactionAPI> allFactions = sector.getAllFactions();
        for (FactionAPI curFaction : allFactions) {

            if (curFaction == mess || curFaction.isNeutralFaction()) {
                continue;
            }
            mess.setRelationship(curFaction.getId(), RepLevel.VENGEFUL);

            if (curFaction == knights_of_eva || curFaction.isNeutralFaction()) {
                continue;
            }
            knights_of_eva.setRelationship(curFaction.getId(), RepLevel.HOSTILE);


            if (curFaction == mess_remnant || curFaction.isNeutralFaction()) {
                continue;
            }
            mess_remnant.setRelationship(curFaction.getId(), RepLevel.VENGEFUL);

            if (curFaction == hmi_nightmare || curFaction.isNeutralFaction()) {
                continue;
            }
            hmi_nightmare.setRelationship(curFaction.getId(), RepLevel.VENGEFUL);

            if (curFaction == hmi_hitech || curFaction.isNeutralFaction())
            {
                continue;
            }
            hmi_hitech.setRelationship(curFaction.getId(), RepLevel.HOSTILE);

            if (curFaction == hmi_lowtech || curFaction.isNeutralFaction())
            {
                continue;
            }
            hmi_lowtech.setRelationship(curFaction.getId(), RepLevel.HOSTILE);

            if (curFaction == hmi_midtech || curFaction.isNeutralFaction())
            {
                continue;
            }
            hmi_midtech.setRelationship(curFaction.getId(), RepLevel.HOSTILE);

            if (curFaction == hmi_luddtech || curFaction.isNeutralFaction())
            {
                continue;
            }
            hmi_luddtech.setRelationship(curFaction.getId(), RepLevel.HOSTILE);
        }

        knights_of_eva.setRelationship(path.getId(), 0.7f);
        knights_of_eva.setRelationship(church.getId(), -0.2f);
        knights_of_eva.setRelationship("HMI", -1.0f);
        knights_of_eva.setRelationship(tritachyon.getId(), -1.0f);

		mess.setRelationship("legio", RepLevel.HOSTILE);
		mess_remnant.setRelationship("legio", RepLevel.HOSTILE);
		
        hmi_hitech.setRelationship(pirates.getId(), RepLevel.FRIENDLY);
        hmi_lowtech.setRelationship(pirates.getId(), RepLevel.FRIENDLY);
        hmi_midtech.setRelationship(pirates.getId(), RepLevel.FRIENDLY);
        hmi_luddtech.setRelationship(path.getId(), RepLevel.FRIENDLY);
		

    }
}

