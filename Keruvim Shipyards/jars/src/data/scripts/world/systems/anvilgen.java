package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.systems.haven.Aphrodite;
import data.scripts.world.systems.haven.Haven;
import data.scripts.world.systems.haven.Skarseld;

public class anvilgen implements SectorGeneratorPlugin {
    public anvilgen() {
    }

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI hegemony = sector.getFaction("hegemony");
        FactionAPI tritachyon = sector.getFaction("tritachyon");
        FactionAPI pirates = sector.getFaction("pirates");
        FactionAPI church = sector.getFaction("luddic_church");
        FactionAPI path = sector.getFaction("luddic_path");
        FactionAPI indep = sector.getFaction("independent");
        FactionAPI keruvim = sector.getFaction("keruvim");
        FactionAPI diktat = sector.getFaction("sindrian_diktat");
        FactionAPI persean = sector.getFaction("persean");


        keruvim.setRelationship(path.getId(), RepLevel.VENGEFUL);
        keruvim.setRelationship(hegemony.getId(), RepLevel.FRIENDLY);
        keruvim.setRelationship(church.getId(), RepLevel.SUSPICIOUS);
        keruvim.setRelationship(pirates.getId(), RepLevel.VENGEFUL);
        keruvim.setRelationship(tritachyon.getId(), RepLevel.SUSPICIOUS);
        keruvim.setRelationship(indep.getId(), RepLevel.WELCOMING);
        keruvim.setRelationship(persean.getId(), RepLevel.FAVORABLE);
        keruvim.setRelationship(diktat.getId(), RepLevel.NEUTRAL);

        keruvim.setRelationship("mayasura", RepLevel.SUSPICIOUS);
        keruvim.setRelationship("draco", RepLevel.HOSTILE);
        keruvim.setRelationship("fang", RepLevel.HOSTILE);
        keruvim.setRelationship("metelson", RepLevel.FAVORABLE);
        keruvim.setRelationship("xlu", RepLevel.NEUTRAL);
        keruvim.setRelationship("new_galactic_order", RepLevel.VENGEFUL);
        keruvim.setRelationship("junk_pirates", RepLevel.HOSTILE);
        keruvim.setRelationship("junk_pirates_hounds", RepLevel.HOSTILE);
        keruvim.setRelationship("junk_pirates_junkboys", RepLevel.HOSTILE);
        keruvim.setRelationship("junk_pirates_technicians", RepLevel.HOSTILE);
        keruvim.setRelationship("blade_breakers", RepLevel.VENGEFUL);
        keruvim.setRelationship("cabal", RepLevel.HOSTILE);
        keruvim.setRelationship("mess", RepLevel.VENGEFUL);
        keruvim.setRelationship("exalted", RepLevel.FAVORABLE);



    }

    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("keruvim");
        initFactionRelationships(sector);
        (new Haven()).generate(sector);
        (new Skarseld()).generate(sector);
        (new Aphrodite()).generate(sector);
    }



}
