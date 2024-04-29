package data.missions.ii_tester;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.IIModPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "ISA", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Interstellar Armada");
        api.setFleetTagline(FleetSide.ENEMY, "The void");

        api.addToFleet(FleetSide.PLAYER, "ii_olympus_pb", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_olympus_ac", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_olympus_sup", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_olympus_fb", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_barrus_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_barrus_arm", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_barrus_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_barrus_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_matriarch_gra", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_matriarch_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_matriarch_tac", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_matriarch_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_caesar_sie", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "ii_caesar_ass", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "ii_caesar_sto", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "ii_caesar_eli", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "ii_dominus_bal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dominus_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dominus_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dominus_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_adamas_bal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_adamas_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_adamas_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_adamas_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ardea_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ardea_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ardea_str", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ardea_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dictator_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dictator_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dictator_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_dictator_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_sebastos_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_sebastos_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_sebastos_att", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_sebastos_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ixon_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ixon_str", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ixon_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_ixon_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_libritor_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_libritor_arm", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_libritor_sup", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_libritor_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_lynx_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_lynx_str", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_lynx_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_lynx_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_princeps_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_princeps_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_princeps_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_princeps_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_legionary_bal", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_legionary_arm", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_legionary_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_legionary_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_interrex_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_interrex_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_interrex_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_interrex_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_praetorian_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_praetorian_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_praetorian_art", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_praetorian_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_maximus_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_maximus_str", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_maximus_bla", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_maximus_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_triarius_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_triarius_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_triarius_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_triarius_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_decurion_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_decurion_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_decurion_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_decurion_eli", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_carrum_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_carrum_per", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_carrum_fue", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_carrum_fas", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_basileus_sta", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_basileus_ass", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_basileus_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_basileus_eli", FleetMemberType.SHIP, false);
        if (IIModPlugin.hasSWP) {
            api.addToFleet(FleetSide.PLAYER, "swp_boss_excelsior_cus", FleetMemberType.SHIP, false);
        }
        api.addToFleet(FleetSide.PLAYER, "ii_boss_dominus_cus", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "ii_boss_titanx_cus", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 20000f;
        float height = 20000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);
    }
}
