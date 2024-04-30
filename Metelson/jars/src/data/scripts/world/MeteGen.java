package data.scripts.world;


import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;

import data.scripts.world.systems.Rock;
import data.scripts.world.systems.metelsonCorvus;
import data.scripts.world.systems.metelsonValhalla;
import data.scripts.world.systems.metelsonTyle;


public class MeteGen {
    
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("metelson");
        initFactionRelationships(sector);
            (new Rock()).generate(sector);
            (new metelsonCorvus()).generate(sector);
            (new metelsonValhalla()).generate(sector);
            (new metelsonTyle()).generate(sector);
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
        FactionAPI metelson = sector.getFaction("metelson");
        
        player.setRelationship(metelson.getId(), 0);
        
        metelson.setRelationship(hegemony.getId(), 0.2f);
        metelson.setRelationship(pirates.getId(), -0.6f);
        metelson.setRelationship(diktat.getId(), 0.1f);
        
        metelson.setRelationship(tritachyon.getId(), -0.5f);
        
        metelson.setRelationship(independent.getId(), 0.9f);
        metelson.setRelationship(league.getId(), 0.5f);
        
        church.setRelationship(metelson.getId(), 0f);
        path.setRelationship(metelson.getId(), -0.9f);
        kol.setRelationship(metelson.getId(), -0.3f);
        
                // Mod factions list yoinked from tiandong with permission.
        metelson.setRelationship("tiandong", RepLevel.FAVORABLE);                // The enemy of my enemy is my friend.
        metelson.setRelationship("blackrock_driveyards", RepLevel.NEUTRAL);       // They ot some nice tech to trade
        metelson.setRelationship("br_consortium", RepLevel.NEUTRAL);              // Black Rock's defenders, treat them the same
        metelson.setRelationship("6eme_bureau", RepLevel.NEUTRAL);                // They leave us alone, so we return the favor
        metelson.setRelationship("dassault_mikoyan", RepLevel.FAVORABLE);         // They trade with us sometimes and are all right
        metelson.setRelationship("united_security", RepLevel.NEUTRAL);       // Competing mercs should be watched carefully
        metelson.setRelationship("blade_breakers", RepLevel.VENGEFUL);            // AGI was a mistake, neural links are even worse
        metelson.setRelationship("diableavionics", RepLevel.NEUTRAL);        // Don't like them but hey, they create jobs for us
        metelson.setRelationship("exigency", RepLevel.SUSPICIOUS);                // Space Assholes™, but they like to trade as well...
        metelson.setRelationship("exipirated", RepLevel.HOSTILE);                 // Pirates, but worse
        //metelson.setRelationship("fob", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        metelson.setRelationship("gmda", RepLevel.NEUTRAL);                       // Who the hell decided they should be the Space Police?
        metelson.setRelationship("gmda_patrol", RepLevel.NEUTRAL);                // Who the hell decided they should be the Space Police redux?
        metelson.setRelationship("draco", RepLevel.HOSTILE);                      // Space Vampire Pirates? Not in my Sector!
        metelson.setRelationship("fang", RepLevel.HOSTILE);                       // Psycho Werewolves, kill with nuclear fire
        metelson.setRelationship("HMI", RepLevel.INHOSPITABLE);                   // "Legitimate" Pirates that feed the Sector
        metelson.setRelationship("mess", RepLevel.VENGEFUL);                      // AGI was a mistake, gray goo is WORSE
        metelson.setRelationship("interstellarimperium", RepLevel.FAVORABLE);     // Trusted trading partners
        metelson.setRelationship("ii_imperial_guard", RepLevel.FAVORABLE);        // Their elite guard, treat them the same
        metelson.setRelationship("junk_pirates", RepLevel.HOSTILE);               // Space Pirates in junk heaps
        metelson.setRelationship("junk_pirates_hounds", RepLevel.HOSTILE);        // Space anarchists in junk heaps
        metelson.setRelationship("junk_pirates_junkboys", RepLevel.HOSTILE);      // Space anarchists in better junk heaps
        metelson.setRelationship("junk_pirates_technicians", RepLevel.HOSTILE);   // Space anarchists in the best junk heaps
        metelson.setRelationship("pack", RepLevel.FAVORABLE);                     // In their isolation, they learned to be humble
        metelson.setRelationship("syndicate_asp", RepLevel.WELCOMING);            // Space FedEx is wise to cooperate with
        metelson.setRelationship("syndicate_asp_familia", RepLevel.WELCOMING);    // Space FedEx's elite guard, treat them the same
        metelson.setRelationship("al_ars", RepLevel.SUSPICIOUS);                  // Nobody is quite sure what they are up to...
        metelson.setRelationship("mayorate", RepLevel.INHOSPITABLE);              // Rumor is they are working on their own AGI
        metelson.setRelationship("ORA", RepLevel.FAVORABLE);                      // Pays for help killing Pirates
        metelson.setRelationship("SCY", RepLevel.SUSPICIOUS);                     // Intel trading is useful, but can't be trusted
        metelson.setRelationship("shadow_industry", RepLevel.FAVORABLE);          // A positive force in the Sector and trading partners
        metelson.setRelationship("the_cartel", RepLevel.HOSTILE);                 // Pirates with a new coat of paint
        metelson.setRelationship("nullorder", RepLevel.HOSTILE);                  // Religious fanatics with highly dangerous technology
        metelson.setRelationship("sylphon", RepLevel.WELCOMING);                  // Rumors of cooperative ship building projects abound
        metelson.setRelationship("templars", RepLevel.HOSTILE);                   // Space Assholes™ with ostensibly alien tech
        metelson.setRelationship("Coalition", RepLevel.WELCOMING);                // Rumors of cooperative ship building projects abound
        metelson.setRelationship("cabal", RepLevel.HOSTILE);                      // Space Assholes™ with too much money
        //metelson.setRelationship("nomads", RepLevel.NEUTRAL);                   // Will let them decide in their code / I don't know!
        //metelson.setRelationship("approlight", RepLevel.NEUTRAL);               // Will let them decide in their code / I don't know!
        //metelson.setRelationship("immortallight", RepLevel.NEUTRAL);            // Will let them decide in their code / I don't know!
        //metelson.setRelationship("lte_boss", RepLevel.NEUTRAL);                 // Will let them decide in their code / I don't know!
        //metelson.setRelationship("Lte", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("noir", RepLevel.NEUTRAL);                     // Will let them decide in their code / I don't know!
        //metelson.setRelationship("crystanite", RepLevel.NEUTRAL);               // Will let them decide in their code / I don't know!
        metelson.setRelationship("crystanite_pir", RepLevel.HOSTILE);             // Sounds like Pirates and they should explode
        //metelson.setRelationship("exlane", RepLevel.NEUTRAL);                   // Will let them decide in their code / I don't know!
        metelson.setRelationship("infected", RepLevel.HOSTILE);                   // Even mercs will not tolerate Space Zombies
        //metelson.setRelationship("neutrinocorp", RepLevel.NEUTRAL);             // Will let them decide in their code / I don't know!
        //metelson.setRelationship("oculus", RepLevel.NEUTRAL);                   // Will let them decide in their code / I don't know!
        //metelson.setRelationship("thulelegacy", RepLevel.NEUTRAL);              // Will let them decide in their code / I don't know!
        //metelson.setRelationship("corvus_scavengers", RepLevel.NEUTRAL);        // Will let them decide in their code / I don't know!
        metelson.setRelationship("new_galactic_order", RepLevel.VENGEFUL);         // Fuck Space Nazis, too dirty even for mercs
        metelson.setRelationship("TF7070_D3C4", RepLevel.HOSTILE);                // Rogue terraformer ships, what could possibly go wrong?
        //metelson.setRelationship("mayasura", RepLevel.NEUTRAL);                 // Will let them decide in their code / I don't know!
        //metelson.setRelationship("sad", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        metelson.setRelationship("cmc", RepLevel.NEUTRAL);                        // They leave us be, so we leave them be
        metelson.setRelationship("kadur_remnant", RepLevel.SUSPICIOUS);           // Shifty relic of a bygone age, keep an eye on them
        //metelson.setRelationship("vesperon", RepLevel.NEUTRAL);                 // Will let them decide in their code / I don't know!
        //metelson.setRelationship("almighty_dollar", RepLevel.NEUTRAL);          // Will let them decide in their code / I don't know!
        //metelson.setRelationship("communist_clouds", RepLevel.NEUTRAL);         // Will let them decide in their code / I don't know!
        //metelson.setRelationship("science_fuckers", RepLevel.NEUTRAL);          // Will let them decide in their code / I don't know!
        //metelson.setRelationship("warhawk_republic", RepLevel.NEUTRAL);         // Will let them decide in their code / I don't know!
        //metelson.setRelationship("pulseindustry", RepLevel.NEUTRAL);            // Will let them decide in their code / I don't know!
        //metelson.setRelationship("ae_ixbattlegroup", RepLevel.NEUTRAL);         // Will let them decide in their code / I don't know!
        //metelson.setRelationship("omicron_faction", RepLevel.NEUTRAL);          // Will let them decide in their code / I don't know!
        //metelson.setRelationship("fringe_defence_syndicate", RepLevel.NEUTRAL); // Will let them decide in their code / I don't know!
        //metelson.setRelationship("gladiator", RepLevel.NEUTRAL);                // Will let them decide in their code / I don't know!
        //metelson.setRelationship("andorian", RepLevel.NEUTRAL);                 // Will let them decide in their code / I don't know!
        //metelson.setRelationship("united_federation", RepLevel.NEUTRAL);        // Will let them decide in their code / I don't know!
        //metelson.setRelationship("klingon_empire", RepLevel.NEUTRAL);           // Will let them decide in their code / I don't know!
        //metelson.setRelationship("romulan_empire", RepLevel.NEUTRAL);           // Will let them decide in their code / I don't know!
        //metelson.setRelationship("vulcan", RepLevel.NEUTRAL);                   // Will let them decide in their code / I don't know!
        //metelson.setRelationship("borg", RepLevel.NEUTRAL);                     // Will let them decide in their code / I don't know!
        //metelson.setRelationship("tahlan_greathouses", RepLevel.NEUTRAL);       // Will let them decide in their code / I don't know!
        //metelson.setRelationship("tahlan_legioinfernalis", RepLevel.NEUTRAL);   // Will let them decide in their code / I don't know!
        metelson.setRelationship("OCI", RepLevel.INHOSPITABLE);                   // Newcomers to the Sector that are creeping about
        //metelson.setRelationship("prv", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("rb", RepLevel.NEUTRAL);                       // Will let them decide in their code / I don't know!
        metelson.setRelationship("magellan_protectorate", RepLevel.SUSPICIOUS);   // Tread carefully... Not very pleasent.
        //metelson.setRelationship("sun_ice", RepLevel.NEUTRAL);                  // Will let them decide in their code / I don't know!
        //metelson.setRelationship("sun_ici", RepLevel.NEUTRAL);                  // Will let them decide in their code / I don't know!
        metelson.setRelationship("GKSec", RepLevel.NEUTRAL);                      // Some sort of security contractors, shoot if needed.
        //metelson.setRelationship("hiigaran_descendants", RepLevel.NEUTRAL);     // Will let them decide in their code / I don't know!
        //metelson.setRelationship("AI", RepLevel.NEUTRAL);                       // Will let them decide in their code / I don't know!
        //metelson.setRelationship("ALIENS", RepLevel.NEUTRAL);                   // Will let them decide in their code / I don't know!
        //metelson.setRelationship("FFS", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("FTG", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("ISA", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("MAR", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("ROCK", RepLevel.NEUTRAL);                     // Will let them decide in their code / I don't know!
        //metelson.setRelationship("RSF", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("UIN", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("VNS", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("WDW", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
        //metelson.setRelationship("XLE", RepLevel.NEUTRAL);                      // Will let them decide in their code / I don't know!
         
        
    }
}
