package data.missions.M3;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public void defineMission(MissionDefinitionAPI api) {

//		CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(Factions.LUDDIC_CHURCH,"CGR",false);
//		Global.getSector().setPlayerFleet(fleet);
//		if (fleet != null){
//			CargoAPI cargo = fleet.getCargo();
//			cargo.removeAll(cargo);
//		}

        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "CGR", FleetGoal.ESCAPE, false, 10);
        api.initFleet(FleetSide.ENEMY, "TTDS", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Hinanawi Tenshi's carrier squadron and remaining luddic fleet. ");
        api.setFleetTagline(FleetSide.ENEMY, "Remnant, also known as devil's toaster.");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Try to retreat as many church ships as possible, it is important to make use of these civilian ship by militarizing their loadouts");
        api.addBriefingItem("The only way out is to fight the enemy head on, however a good fleet coordination is needed");
        api.addBriefingItem("Enemy frigate and carrier with bombers have blockaded the escape route");
        api.addBriefingItem("GMS Bhavaagra and GMS Lightning Fish must survive");

        //set officers
        FactionAPI GM = Global.getSettings().createBaseFaction("fantasy_manufacturing");

        PersonAPI offierTS = GM.createRandomPerson();
        Global.getFactory().createOfficerData(offierTS);
        offierTS.getStats().setLevel(5);
        offierTS.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        offierTS.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        offierTS.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        offierTS.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        offierTS.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        offierTS.setPersonality("aggressive");
        offierTS.setFaction("fantasy_manufacturing");
        offierTS.setName(new FullName("Hinanawi", "Tenshi", FullName.Gender.FEMALE));
        offierTS.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Tenshi"));

        PersonAPI offierIK = GM.createRandomPerson();
        Global.getFactory().createOfficerData(offierIK);
        offierIK.getStats().setLevel(5);
        offierIK.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        offierIK.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        offierIK.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        offierIK.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        offierIK.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        offierIK.setPersonality("steady");
        offierIK.setFaction("fantasy_manufacturing");
        offierIK.setName(new FullName("Nagae", "Iku", FullName.Gender.FEMALE));
        offierIK.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Iku"));

        //set up weapon and fighter
        //Global.getLogger(this.getClass()).info(cargo);

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters

        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        //api.addToFleet(FleetSide.PLAYER, "afflictor_Strike", FleetMemberType.SHIP, "ISS Black Star", true, CrewXPLevel.VETERAN);
        //api.addToFleet(FleetSide.PLAYER, "station_small_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "FM_Hearn_Standard", FleetMemberType.SHIP, "GMS Bhavaagra", true).setCaptain(offierTS);
        api.addToFleet(FleetSide.PLAYER, "FM_Hearn_Standard", FleetMemberType.SHIP, "GMS Lightning Fish", false).setCaptain(offierIK);
        api.addToFleet(FleetSide.PLAYER, "FM_Puppeteer_Standard", FleetMemberType.SHIP, "GMS London", false);
        api.addToFleet(FleetSide.PLAYER, "FM_Puppeteer_Standard", FleetMemberType.SHIP, "GMS Paris", false);

        api.addToFleet(FleetSide.PLAYER, "dominator_AntiCV", FleetMemberType.SHIP, "CGR Joseph", false);
        api.addToFleet(FleetSide.PLAYER, "enforcer_Escort", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "enforcer_Escort", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "colossus_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "colossus_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "buffalo_luddic_church_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "buffalo_luddic_church_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "phaeton_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "phaeton_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "dram_Light", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);
        api.addToFleet(FleetSide.PLAYER, "dram_Light", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.TIMID);

        api.defeatOnShipLoss("GMS Bhavaagra");
        api.defeatOnShipLoss("GMS Lightning Fish");

        //fleetMember = api.addToFleet(FleetSide.PLAYER, "buffalo_tritachyon_Standard", FleetMemberType.SHIP, false);
        //fleetMember = api.addToFleet(FleetSide.PLAYER, "falcon_CS", FleetMemberType.SHIP, false);

//		api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
//		api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);

//		api.addToFleet(FleetSide.PLAYER, "enforcer_Assault", FleetMemberType.SHIP, "ISS Hamatsu", true);
//		api.addToFleet(FleetSide.PLAYER, "medusa_PD", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "hyperion_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "wolf_CS", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "medusa_Attack", FleetMemberType.SHIP, false);
//		api.addToFleet(FleetSide.PLAYER, "tempest_Attack", FleetMemberType.SHIP, false);

        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);
        //api.addToFleet(FleetSide.PLAYER, "longbow_wing", FleetMemberType.FIGHTER_WING, false);

        // Mark both ships as essential - losing either one results
        // in mission failure. Could also be set on an enemy ship,
        // in which case destroying it would result in a win.


        // Set up the enemy fleet.
        // It's got more ships than the player's, but they're not as strong.
        //api.addToFleet(FleetSide.ENEMY, "station_small_Standard", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
        FactionAPI RE = Global.getSettings().createBaseFaction(Factions.REMNANTS);
        PersonAPI ai_a = RE.createRandomPerson();
        PersonAPI ai_g = RE.createRandomPerson();
        ai_a.setAICoreId("alpha_cores");
        ai_g.setAICoreId("gamma_cores");
        ai_a.getStats().setLevel(8);
        ai_a.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
        ai_a.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
        ai_a.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        ai_a.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        ai_a.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        ai_a.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        ai_a.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        ai_a.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        ai_a.setPersonality(Personalities.RECKLESS);
        ai_g.getStats().setLevel(4);
        ai_g.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        ai_g.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        ai_g.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        ai_g.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        ai_g.setPersonality(Personalities.RECKLESS);


        api.addToFleet(FleetSide.ENEMY, "radiant_Assault", FleetMemberType.SHIP, false).setCaptain(ai_a);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Strike", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "fulgent_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "scintilla_Support", FleetMemberType.SHIP, false).getCaptain().setPersonality(Personalities.RECKLESS);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "lumen_Standard", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);
        api.addToFleet(FleetSide.ENEMY, "glimmer_Assault", FleetMemberType.SHIP, false).setCaptain(ai_g);

//		api.addToFleet(FleetSide.ENEMY, "piranha_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);
//		api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality(Personalities.AGGRESSIVE);


        //api.addToFleet(FleetSide.ENEMY, "hound_Standard", FleetMemberType.SHIP, false);
        //api.addToFleet(FleetSide.ENEMY, "broadsword_wing", FleetMemberType.FIGHTER_WING, false);

//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");
//		api.addToFleet(FleetSide.ENEMY, "mining_drone_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");


        //api.addToFleet(FleetSide.ENEMY, "talon_wing", FleetMemberType.FIGHTER_WING, false).getCaptain().setPersonality("suicidal");


        // Set up the map.
        float width = 18000f;
        float height = 30000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.

        // Add two big nebula clouds

        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.


        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        api.addObjective(minX + width * 0.1f, minY + height * 0.7f, "nav_buoy");
        api.addObjective(minX + width * 0.3f, minY + height * 0.6f, "sensor_array");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "comm_relay");
        api.addObjective(minX + width * 0.7f, minY + height * 0.6f, "sensor_array");
        api.addObjective(minX + width * 0.9f, minY + height * 0.7f, "nav_buoy");


        // Add an asteroid field going diagonally across the
        // battlefield, 2000 pixels wide, with a maximum of
        // 100 asteroids in it.
        // 20-70 is the range of asteroid speeds.
        api.addAsteroidField(minX + width * 0.5f, minY + height, 270, width,
                20f, 70f, 70);
        api.addAsteroidField(minX + width * 0.3f, minY + height * 0.7f, 150, 400f,
                200f, 400f, 80);
        api.addAsteroidField(minX + width * 0.3f, minY + height * 0.5f, 150, 400f,
                200f, 400f, 80);
        api.addAsteroidField(minX + width * 0.3f, minY + height * 0.3f, 150, 400f,
                200f, 400f, 80);
        api.addAsteroidField(minX + width * 0.8f, minY + height * 0.07f, 0, 2400f,
                100f, 320f, 200);


        // Add some planets.  These are defined in data/config/planets.json.
        api.addPlanet(512, 380, 400f, "terran", 600f, true);

        BattleCreationContext context = new BattleCreationContext(null, null, null, null);
        context.setInitialEscapeRange(7000f);
        context.aiRetreatAllowed = false;
        api.addPlugin(new EscapeRevealPlugin(context));
        api.addPlugin(new FM_Mission3DeploySetting());
    }

}






