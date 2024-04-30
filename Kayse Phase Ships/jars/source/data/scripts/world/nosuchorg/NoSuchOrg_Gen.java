package data.scripts.world.nosuchorg;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.nosuchorg.stars.nusquam;//Latin for nowhere


public class NoSuchOrg_Gen implements SectorGeneratorPlugin {

    public static void initFactionRelationships(SectorAPI sector) {
        
        
    }

    @Override
    public void generate(SectorAPI sector) {
        
        new nusquam().generate(sector);
        
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("nosuchorg");
        
        FactionAPI no_such_org = sector.getFaction("no_such_org");
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
        FactionAPI guard = sector.getFaction(Factions.LIONS_GUARD);

        no_such_org.setRelationship(player.getId(), 0.2f);	
        no_such_org.setRelationship(hegemony.getId(), -0.4f);
        no_such_org.setRelationship(tritachyon.getId(), 0.8f);
        no_such_org.setRelationship(pirates.getId(), -0.7f);
        no_such_org.setRelationship(independent.getId(), 0.5f);
        no_such_org.setRelationship(persean.getId(), 0.2f);	
        no_such_org.setRelationship(church.getId(), -0.1f);
        no_such_org.setRelationship(path.getId(), -0.5f);    
        no_such_org.setRelationship(kol.getId(), 0.2f);    
        no_such_org.setRelationship(diktat.getId(), 0.2f);
        no_such_org.setRelationship(guard.getId(), 0.3f);     
                

    }
}
