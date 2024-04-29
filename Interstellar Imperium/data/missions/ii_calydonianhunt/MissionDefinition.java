package data.missions.ii_calydonianhunt;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    private static Vector2f getSafeSpawn(FleetSide side, float mapX, float mapY) {
        Vector2f spawnLocation = new Vector2f();

        spawnLocation.x = MathUtils.getRandomNumberInRange(-mapX / 2, mapX / 2);
        if (side == FleetSide.PLAYER) {
            spawnLocation.y = (-mapY / 2f);

        } else {
            spawnLocation.y = mapY / 2;
        }

        return spawnLocation;
    }

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        api.initFleet(FleetSide.PLAYER, "HSS", FleetGoal.ATTACK, false, 0);
        api.initFleet(FleetSide.ENEMY, "ISA", FleetGoal.ATTACK, true, 3);

        api.setFleetTagline(FleetSide.PLAYER, "Lanta's privateers");
        api.setFleetTagline(FleetSide.ENEMY, "Imperial \"trade fleet\"");

        api.addBriefingItem("Defeat the enemy fleet.");
        api.addBriefingItem("The HSS Huntress must survive.");
        api.addBriefingItem("Captain Lanta's skills may be impressive, but...");

        OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");

        // (64 (54 w/o skills) DP)
        FactionAPI hegemony = Global.getSettings().createBaseFaction(Factions.HEGEMONY);
        FleetMemberAPI member;
        member = api.addToFleet(FleetSide.PLAYER, "enforcer_XIV_Elite", FleetMemberType.SHIP, "HSS Huntress", true); // 9+2 * 1.9
        PersonAPI officer = hegemony.createRandomPerson(Gender.FEMALE);
        officer.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
        officer.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        officer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
        officer.getStats().addXP(plugin.getXPForLevel(5));
        officer.getStats().setLevel(5);
        officer.getName().setFirst("Captain");
        officer.getName().setLast("Lanta");
        officer.setPortraitSprite("graphics/portraits/portrait39.png");
        officer.setFaction(Factions.HEGEMONY);
        member.setCaptain(officer);
        float maxCR = member.getRepairTracker().getMaxCR();
        member.getRepairTracker().setCR(maxCR);
        member = api.addToFleet(FleetSide.PLAYER, "sunder_Assault", FleetMemberType.SHIP, false); // 11
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_CS", FleetMemberType.SHIP, false); // 5
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "wolf_hegemony_PD", FleetMemberType.SHIP, false); // 5
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "lasher_CS", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "lasher_Strike", FleetMemberType.SHIP, false); // 4
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false); // 3+1
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "hound_hegemony_Standard", FleetMemberType.SHIP, false); // 3+1
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false); // 2+1
        member.setShipName(hegemony.pickRandomShipName());
        member = api.addToFleet(FleetSide.PLAYER, "kite_hegemony_Interceptor", FleetMemberType.SHIP, false); // 2+1
        member.setShipName(hegemony.pickRandomShipName());

        api.defeatOnShipLoss("HSS Huntress");

        // (115.5 (68 w/o skills) DP)
        FactionAPI imperium = Global.getSettings().createBaseFaction("interstellarimperium");
        member = api.addToFleet(FleetSide.ENEMY, "ii_barrus_sta", FleetMemberType.SHIP, "ISA Boar", false); // 25 * 2.9
        officer = imperium.createRandomPerson(Gender.FEMALE);
        officer.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
        officer.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
        officer.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
        officer.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
        officer.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
        officer.getStats().setSkillLevel(Skills.FLUX_REGULATION, 2);
        officer.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
        officer.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
        officer.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
        officer.getStats().addXP(plugin.getXPForLevel(9));
        officer.getStats().setLevel(9);
        officer.getName().setFirst("Justitia");
        officer.getName().setLast("");
        officer.getName().setGender(Gender.FEMALE);
        officer.setPortraitSprite("graphics/imperium/portraits/ii_justitia.png");
        officer.setFaction("interstellarimperium");
        officer.setPersonality(Personalities.RECKLESS);
        member.setCaptain(officer);
        maxCR = member.getRepairTracker().getMaxCR();
        member.getRepairTracker().setCR(maxCR);
        member = api.addToFleet(FleetSide.ENEMY, "ii_princeps_cs", FleetMemberType.SHIP, false); // 14
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_interrex_ass", FleetMemberType.SHIP, false); // 10
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_triarius_sta", FleetMemberType.SHIP, false); // 5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_triarius_sta", FleetMemberType.SHIP, false); // 5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "colossus_Standard", FleetMemberType.SHIP, false); // 6 * 0.5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "tarsus_Standard", FleetMemberType.SHIP, false); // 3 * 0.5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.setShipName(imperium.pickRandomShipName());
        member = api.addToFleet(FleetSide.ENEMY, "ii_carrum_sta", FleetMemberType.SHIP, false); // 3 * 0.5
        member.setShipName(imperium.pickRandomShipName());

        float width = 16000f;
        float height = 14000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        api.addAsteroidField(0f, 0f, 25f, width, 25f, 100f, 300);

        api.addObjective(minX + width * 0.25f, minY + height * 0.5f, "sensor_array");
        api.addObjective(minX + width * 0.5f, minY + height * 0.25f, "nav_buoy");
        api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "comm_relay");

        api.addPlugin(new Plugin(width, height));
    }

    private static final class Plugin extends BaseEveryFrameCombatPlugin {

        private boolean done = false;
        private final float mapX;
        private final float mapY;
        private float timer = 5f;

        private Plugin(float mapX, float mapY) {
            this.mapX = mapX;
            this.mapY = mapY;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (done || Global.getCombatEngine() == null || Global.getCombatEngine().isPaused()) {
                return;
            }

            timer -= amount;
            if (timer <= 0f) {
                for (FleetMemberAPI member : Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getReservesCopy()) {
                    if (!Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedCopy().contains(member)) {
                        Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).spawnFleetMember(member, getSafeSpawn(
                                FleetSide.ENEMY, mapX,
                                mapY), 270f, 1f);
                    }
                }
                done = true;
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
        }
    }
}
