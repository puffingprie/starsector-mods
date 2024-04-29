package data.missions.ii_losttotheinferno;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import data.scripts.everyframe.II_PluginStarter.ChargedNebulaPlugin;
import java.awt.Color;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "His Imperial Majesty's hunter-killers");
        api.setFleetTagline(FleetSide.ENEMY, "Pather infiltrators");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat the enemy fleet.");
        api.addBriefingItem("You and your fellow officers are highly skilled.");

        // Set up the player's fleet (48 DP)
        OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
        FactionAPI imperium = Global.getSettings().createBaseFaction("interstellarimperium");
        FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "ii_lynx_sta", FleetMemberType.SHIP, "Terribilis", true); // 18
        PersonAPI officer = imperium.createRandomPerson();
        officer.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        officer.getStats().addXP(plugin.getXPForLevel(5));
        officer.setPersonality(Personalities.AGGRESSIVE);
        officer.setFaction("interstellarimperium");
        member.setCaptain(officer);
        member = api.addToFleet(FleetSide.PLAYER, "ii_libritor_eli", FleetMemberType.SHIP, "Ad Tenebras", false); // 11
        officer = imperium.createRandomPerson();
        officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        officer.getStats().addXP(plugin.getXPForLevel(5));
        officer.setPersonality(Personalities.AGGRESSIVE);
        officer.setFaction("interstellarimperium");
        member.setCaptain(officer);
        member = api.addToFleet(FleetSide.PLAYER, "ii_libritor_eli", FleetMemberType.SHIP, "Interfectorem", false); // 11
        officer = imperium.createRandomPerson();
        officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        officer.getStats().addXP(plugin.getXPForLevel(5));
        officer.setPersonality(Personalities.AGGRESSIVE);
        officer.setFaction("interstellarimperium");
        member.setCaptain(officer);
        member = api.addToFleet(FleetSide.PLAYER, "ii_maximus_eli", FleetMemberType.SHIP, "Diabolus", false); // 8
        officer = imperium.createRandomPerson();
        officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        officer.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
        officer.getStats().addXP(plugin.getXPForLevel(5));
        officer.setPersonality(Personalities.AGGRESSIVE);
        officer.setFaction("interstellarimperium");
        member.setCaptain(officer);
        member.getRepairTracker().setCR(0.85f);

        // Set up the enemy fleet (55 DP)
        FactionAPI luddicPath = Global.getSettings().createBaseFaction(Factions.LUDDIC_PATH);
        member = api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, false); // 9
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, false); // 9
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "sunder_Overdriven", FleetMemberType.SHIP, false); // 11
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "hammerhead_Overdriven", FleetMemberType.SHIP, false); // 10
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "brawler_pather_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);
        member = api.addToFleet(FleetSide.ENEMY, "lasher_luddic_path_Raider", FleetMemberType.SHIP, false); // 4
        member.setShipName(luddicPath.pickRandomShipName());
        member.getCaptain().setPersonality(Personalities.RECKLESS);

        api.setNebulaTex(Global.getSettings().getSpriteName("terrain", "ii_chargednebula"));
        api.setNebulaMapTex(Global.getSettings().getSpriteName("terrain", "ii_chargednebula_map"));
        api.setBackgroundGlowColor(new Color(255, 10, 50, 40));

        // Set up the map.
        float width = 18000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        api.addAsteroidField(0f, 0f, 0f, width, 40f, 120f, 200);

        for (int i = 0; i < 100; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 400f + (float) Math.random() * 800f;
            api.addNebula(x, y, radius);
        }

        api.addPlugin(new ChargedNebulaPlugin());
    }
}
