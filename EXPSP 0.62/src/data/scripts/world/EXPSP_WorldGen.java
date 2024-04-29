package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.EXPSP_Osmium;
import data.scripts.world.systems.EXPSP_Vanadium;
public class EXPSP_WorldGen implements SectorGeneratorPlugin {
    //this script will be used to do campaign generation for this mod. typically used to set up faction relationships, spawn multiple systems etc.
    @Override
    public void generate(SectorAPI sector) {
        new EXPSP_Osmium().generate(sector);
		 new EXPSP_Vanadium().generate(sector);
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("MVS");
        FactionAPI mvs = sector.getFaction("MVS");
        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI persean = sector.getFaction(Factions.PERSEAN);
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);
        FactionAPI remnant = sector.getFaction(Factions.REMNANTS);
        FactionAPI derelict = sector.getFaction(Factions.DERELICT);

        mvs.setRelationship(independent.getId(), RepLevel.COOPERATIVE);
       
        mvs.setRelationship(pirates.getId(), RepLevel.VENGEFUL);
  
        mvs.setRelationship(path.getId(), RepLevel.HOSTILE);
       
       mvs.setRelationship(hegemony.getId(),RepLevel.FAVORABLE);
       mvs.setRelationship(persean.getId(),RepLevel.SUSPICIOUS);
        mvs.setRelationship(tritachyon.getId(),RepLevel.INHOSPITABLE);
     

       //environmental factions
        mvs.setRelationship(remnant.getId(),RepLevel.HOSTILE);
	
        mvs.setRelationship(derelict.getId(),RepLevel.FAVORABLE);

        //Modded factions
        mvs.setRelationship("tahlan_greathouses",RepLevel.FRIENDLY);
      
		
        mvs.setRelationship("ORA",RepLevel.WELCOMING);
        
        mvs.setRelationship("tiandong",RepLevel.WELCOMING);
      
        mvs.setRelationship("roider",RepLevel.WELCOMING);
       
        mvs.setRelationship("brighton",RepLevel.WELCOMING);

        mvs.setRelationship("uaf",RepLevel.WELCOMING);

	    mvs.setRelationship("ironshell",RepLevel.WELCOMING);
		
        mvs.setRelationship("shadow_industry",RepLevel.FAVORABLE);
     
        mvs.setRelationship("scalartech",RepLevel.FAVORABLE);
      
        mvs.setRelationship("mayasura",RepLevel.FAVORABLE);
     
        mvs.setRelationship("vic",RepLevel.SUSPICIOUS);
        
        mvs.setRelationship("ae_ixbattlegroup",RepLevel.INHOSPITABLE);
   
        mvs.setRelationship("diableavionics",RepLevel.INHOSPITABLE);
        
        mvs.setRelationship("SCY",RepLevel.INHOSPITABLE);
    
        mvs.setRelationship("al_ars",RepLevel.INHOSPITABLE);
       
        mvs.setRelationship("new_galactic_order",RepLevel.INHOSPITABLE);
       
        mvs.setRelationship("HMI",RepLevel.HOSTILE);
      
        mvs.setRelationship("communist_clouds",RepLevel.VENGEFUL);
      
        mvs.setRelationship("tahlan_legioinfernalis",RepLevel.VENGEFUL);
      
        mvs.setRelationship("templars",RepLevel.VENGEFUL);
        //2 Corinthians 11:14.
        mvs.setRelationship("cabal",RepLevel.VENGEFUL);
        


    }
}
