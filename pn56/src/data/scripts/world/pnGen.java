package data.scripts.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.tolp.Tolp;

public class pnGen implements SectorGeneratorPlugin {
    
    @Override
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("pn_colony");
        initFactionRelationships(sector);
        
        new Tolp().generate(sector);

    }
    
    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
	FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
	FactionAPI pirates = sector.getFaction(Factions.PIRATES);
	FactionAPI independent = sector.getFaction(Factions.INDEPENDENT);
	FactionAPI kol = sector.getFaction(Factions.KOL);
	FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
	FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
	FactionAPI player = sector.getFaction(Factions.PLAYER);
	FactionAPI diktat = sector.getFaction(Factions.DIKTAT);
        FactionAPI league = sector.getFaction(Factions.PERSEAN);
        FactionAPI pn_colony = sector.getFaction("pn_colony");
        
        player.setRelationship(pn_colony.getId(), 0);
        
        pn_colony.setRelationship(hegemony.getId(), 0f);
        pn_colony.setRelationship(pirates.getId(), -0.6f);
        pn_colony.setRelationship(diktat.getId(), 0.2f);
        
        pn_colony.setRelationship(tritachyon.getId(), -0.6f);
        
        pn_colony.setRelationship(independent.getId(), 0.6f);
        pn_colony.setRelationship(league.getId(), 0.2f);
        
        church.setRelationship(pn_colony.getId(), -0.6f);
        path.setRelationship(pn_colony.getId(), -0.9f);
        kol.setRelationship(pn_colony.getId(), -0.3f);
    }
}
