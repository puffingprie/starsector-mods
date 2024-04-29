package data.missions.ii_test2;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.campaign.II_IGFleetInflater;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class MissionDefinition implements MissionDefinitionPlugin {

    public static final List<String> PLAYER_FACTIONS = new ArrayList<>();
    public static final List<String> ENEMY_FACTIONS = new ArrayList<>();
    public static final String[] OBJECTIVE_TYPES = {
        "sensor_array", "nav_buoy", "comm_relay"
    };
    public static final List<String> PLANETS = new ArrayList<>();

    private static final Random rand = new Random();
    private static int size = 30;
    private static boolean first = true;

    private static String playerFaction;
    private static int playerFactionIndex;
    private static int playerQuality;
    private static String enemyFaction;
    private static int enemyFactionIndex;
    private static int enemyAdvantage;
    private static int enemyQuality;
    private static boolean balanceFleets;
    private static boolean boostTime;
    private static boolean autoshit;

    private static long bestPlayerFleetSeed = 0L;
    private static long bestEnemyFleetSeed = 0L;
    private static float bestDistance = Float.MAX_VALUE;

    static {
        PLAYER_FACTIONS.add("interstellarimperium");
        PLAYER_FACTIONS.add("ii_imperial_guard");

        ENEMY_FACTIONS.add(Factions.HEGEMONY);
        ENEMY_FACTIONS.add(Factions.DIKTAT);
        ENEMY_FACTIONS.add(Factions.INDEPENDENT);
        ENEMY_FACTIONS.add(Factions.LIONS_GUARD);
        ENEMY_FACTIONS.add(Factions.LUDDIC_CHURCH);
        ENEMY_FACTIONS.add(Factions.LUDDIC_PATH);
        ENEMY_FACTIONS.add(Factions.PERSEAN);
        ENEMY_FACTIONS.add(Factions.PIRATES);
        ENEMY_FACTIONS.add(Factions.SCAVENGERS);
        ENEMY_FACTIONS.add(Factions.TRITACHYON);
        ENEMY_FACTIONS.add(Factions.DERELICT);
        ENEMY_FACTIONS.add(Factions.REMNANTS);
        ENEMY_FACTIONS.add("interstellarimperium");
        ENEMY_FACTIONS.add("ii_imperial_guard");
    }

    private void init() {
        for (PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
            PLANETS.add(spec.getPlanetType());
        }
    }

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        boolean refreshPlayer = false;
        boolean refreshEnemy = false;

        if (first || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            init();
            first = false;
            playerFaction = PLAYER_FACTIONS.get(0);
            playerFactionIndex = PLAYER_FACTIONS.indexOf(playerFaction);
            enemyFaction = ENEMY_FACTIONS.get(0);
            enemyFactionIndex = ENEMY_FACTIONS.indexOf(enemyFaction);
            size = 30;
            enemyAdvantage = 100;
            playerQuality = 125;
            enemyQuality = 125;
            balanceFleets = true;
            boostTime = true;
            autoshit = false;
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            size = Math.max(size - 1, 1);
            refreshPlayer = true;
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            size = Math.min(size + 1, 250);
            refreshPlayer = true;
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            playerFactionIndex = (playerFactionIndex + 1) % PLAYER_FACTIONS.size();
            playerFaction = PLAYER_FACTIONS.get(playerFactionIndex);
            refreshPlayer = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
            enemyFactionIndex = (enemyFactionIndex + 1) % ENEMY_FACTIONS.size();
            enemyFaction = ENEMY_FACTIONS.get(enemyFactionIndex);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
            enemyAdvantage = Math.max(enemyAdvantage - 5, 5);
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            enemyAdvantage = Math.min(enemyAdvantage + 5, 1000);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            playerQuality = Math.max(playerQuality - 5, -50);
            refreshPlayer = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
            playerQuality = Math.min(playerQuality + 5, 150);
            refreshPlayer = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            enemyQuality = Math.max(enemyQuality - 5, -50);
            refreshEnemy = true;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
            enemyQuality = Math.min(enemyQuality + 5, 150);
            refreshEnemy = true;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
            balanceFleets = !balanceFleets;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
            boostTime = !boostTime;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_GRAVE)) {
            autoshit = !autoshit;
            refreshPlayer = true;
            refreshEnemy = true;
        }

        float playerSize = size * 5f;
        float enemySize = size * 5f * (enemyAdvantage / 100f);

        int smaller = Math.round(Math.min(playerSize, enemySize));

        int maxFP = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");
        boolean withObjectives = smaller > maxFP;

        int numObjectives = 0;
        if (withObjectives) {
            if ((playerSize + enemySize) > (maxFP + 70)) {
                numObjectives = 3 + (int) (Math.random() * 2.0);
            } else {
                numObjectives = 2 + (int) (Math.random() * 2.0);
            }
        }
        if (numObjectives > 4) {
            numObjectives = 4;
        }

        float width = 18000f;
        float height = 18000f;
        if (withObjectives) {
            width = 24000f;
            if (numObjectives == 2) {
                height = 14000f;
            } else {
                height = 18000f;
            }
        }

        api.initFleet(FleetSide.PLAYER, "A", FleetGoal.ATTACK, true, size / 8);
        api.initFleet(FleetSide.ENEMY, "B", FleetGoal.ATTACK, true, size / 8);

        switch (numObjectives) {
            case 0:
                api.addBriefingItem("Battle size: " + size + "  -  " + (int) width + "x" + (int) height);
                break;
            case 1:
                api.addBriefingItem("Battle size: " + size + "  -  " + numObjectives + " objective" + "  -  " + (int) width + "x" + (int) height);
                break;
            default:
                api.addBriefingItem("Battle size: " + size + "  -  " + numObjectives + " objectives" + "  -  " + (int) width + "x" + (int) height);
                break;
        }

        api.setFleetTagline(FleetSide.PLAYER, playerFaction + " (" + Math.round(playerSize) + " points) (Q " + playerQuality + "%)");
        api.setFleetTagline(FleetSide.ENEMY, enemyFaction + " (" + Math.round(enemySize) + " points) (Q " + enemyQuality + "%)");

        int reps = 1;
        if (refreshPlayer || refreshEnemy) {
            bestDistance = Float.MAX_VALUE;
            if (balanceFleets) {
                reps = 1000;
            }
        }

        CampaignFleetAPI bestPlayerFleet = null;
        CampaignFleetAPI bestEnemyFleet = null;
        for (int i = 0; i < reps; i++) {
            CampaignFleetAPI playerFleet;
            long playerFleetSeed;
            if ((i == 0) || refreshPlayer) {
                FleetParamsV3 params = new FleetParamsV3(null,
                        new Vector2f(0, 0),
                        playerFaction,
                        playerQuality / 100f, // qualityOverride
                        "missionFleet",
                        playerSize, // combatPts
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f); // qualityMod
                params.withOfficers = false;
                params.ignoreMarketFleetSizeMult = true;
                params.forceAllowPhaseShipsEtc = true;
                params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;

                if (refreshPlayer) {
                    playerFleetSeed = rand.nextLong();
                } else {
                    playerFleetSeed = bestPlayerFleetSeed;
                }
                params.random = new Random(playerFleetSeed);

                playerFleet = FleetFactoryV3.createFleet(params);
                if (!refreshPlayer) {
                    bestPlayerFleet = playerFleet;
                }
            } else {
                playerFleet = bestPlayerFleet;
                playerFleetSeed = bestPlayerFleetSeed;
            }

            CampaignFleetAPI enemyFleet;
            long enemyFleetSeed;
            if ((i == 0) || refreshEnemy) {
                FleetParamsV3 params = new FleetParamsV3(null,
                        new Vector2f(0, 0),
                        enemyFaction,
                        enemyQuality / 100f, // qualityOverride
                        "missionFleet",
                        enemySize, // combatPts
                        0f, // freighterPts
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f); // qualityMod
                params.withOfficers = false;
                params.ignoreMarketFleetSizeMult = true;
                params.forceAllowPhaseShipsEtc = true;
                params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;

                if (refreshEnemy) {
                    enemyFleetSeed = rand.nextLong();
                } else {
                    enemyFleetSeed = bestEnemyFleetSeed;
                }
                params.random = new Random(enemyFleetSeed);

                enemyFleet = FleetFactoryV3.createFleet(params);
                if (!refreshEnemy) {
                    bestEnemyFleet = enemyFleet;
                }
            } else {
                enemyFleet = bestEnemyFleet;
                enemyFleetSeed = bestEnemyFleetSeed;
            }

            if ((playerFleet == null) || (enemyFleet == null)) {
                continue;
            }

            float friendlyDP = 0f;
            float friendlyFP = 0f;
            for (FleetMemberAPI member : playerFleet.getFleetData().getMembersInPriorityOrder()) {
                friendlyDP += member.getDeploymentPointsCost();
                friendlyFP += member.getFleetPointCost();
            }

            float enemyDP = 0f;
            float enemyFP = 0f;
            for (FleetMemberAPI member : enemyFleet.getFleetData().getMembersInPriorityOrder()) {
                enemyDP += member.getDeploymentPointsCost();
                enemyFP += member.getFleetPointCost();
            }

            float distance = Math.abs(enemyDP - friendlyDP) + Math.abs(enemyFP - friendlyFP);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPlayerFleetSeed = playerFleetSeed;
                bestPlayerFleet = playerFleet;
                bestEnemyFleetSeed = enemyFleetSeed;
                bestEnemyFleet = enemyFleet;
            }

            if (Math.round(distance) <= 0) {
                break;
            }
        }

        if ((bestPlayerFleet == null) || (bestEnemyFleet == null)) {
            return;
        }

        api.addBriefingItem("Match inequality: " + Math.round(bestDistance));

        if (boostTime) {
            api.addBriefingItem("Time acceleration applied!");
        }

        if (autoshit) {
            api.addBriefingItem("Crappy autofit enabled!");
        }

        if (refreshPlayer) {
            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = playerQuality / 100f;
            p.seed = MathUtils.getRandom().nextLong();
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.factionId = playerFaction;

            FleetInflater inflater;
            if (playerFaction.contentEquals("ii_imperial_guard")) {
                inflater = new II_IGFleetInflater(p);
            } else {
                inflater = new DefaultFleetInflater(p);
            }
            inflater.inflate(bestPlayerFleet);
        }

        for (FleetMemberAPI member : bestPlayerFleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.PLAYER, member);
        }

        if (refreshEnemy) {
            DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
            p.quality = enemyQuality / 100f;
            p.seed = MathUtils.getRandom().nextLong();
            p.mode = ShipPickMode.PRIORITY_THEN_ALL;
            p.allWeapons = !autoshit;
            p.factionId = enemyFaction;

            FleetInflater inflater;
            if (enemyFaction.contentEquals("ii_imperial_guard")) {
                inflater = new II_IGFleetInflater(p);
            } else {
                inflater = new DefaultFleetInflater(p);
            }
            inflater.inflate(bestEnemyFleet);
        }

        for (FleetMemberAPI member : bestEnemyFleet.getFleetData().getMembersInPriorityOrder()) {
            api.addFleetMember(FleetSide.ENEMY, member);
        }

        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        int numNebula = 15;
        boolean inNebula = Math.random() > 0.5;
        if (inNebula) {
            numNebula = 100;
        }

        for (int i = 0; i < numNebula; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            if (inNebula) {
                radius += 100f + 500f * (float) Math.random();
            }
            api.addNebula(x, y, radius);
        }

        float numAsteroidsWithinRange = Math.max(0f, MathUtils.getRandomNumberInRange(-10f, 20f));
        int numAsteroids = Math.min(400, (int) ((numAsteroidsWithinRange + 1f) * 20f));

        api.addAsteroidField(0, 0, (float) Math.random() * 360f, width, 20f, 70f, numAsteroids);

        int numRings = 0;
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (Math.random() > 0.75) {
            numRings++;
        }
        if (numRings > 0) {
            int numRingAsteroids = (int) (numRings * 300 + (numRings * 600f) * (float) Math.random());
            if (numRingAsteroids > 1500) {
                numRingAsteroids = 1500;
            }
            api.addRingAsteroids(0, 0, (float) Math.random() * 360f, width, 100f, 200f, numRingAsteroids);
        }

        String planet = PLANETS.get((int) (Math.random() * PLANETS.size()));
        float radius = 25f + (float) Math.random() * (float) Math.random() * 500f;

        api.addPlanet(0, 0, radius, planet, 0f, true);
        if (planet.contentEquals("wormholeUnder")) {
            api.addPlanet(0, 0, radius, "wormholeA", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeB", 0f, true);
            api.addPlanet(0, 0, radius, "wormholeC", 0f, true);
        }

        api.getContext().aiRetreatAllowed = false;
        api.getContext().enemyDeployAll = true;
        api.getContext().fightToTheLast = true;
        if (withObjectives) {
            String COMM = "comm_relay";
            String SENSOR = "sensor_array";
            String NAV = "nav_buoy";

            List<String> objs = new ArrayList<>(Arrays.asList(new String[]{
                SENSOR,
                SENSOR,
                NAV,
                NAV,
                COMM,
                COMM,}));

            prevXDir = 0;
            prevYDir = 0;

            float r;
            switch (numObjectives) {
                case 2:
                    objs = new ArrayList<>(Arrays.asList(new String[]{
                        SENSOR,
                        SENSOR,
                        NAV,
                        NAV,
                        COMM,}));
                    addObjectiveAt(0.25f, 0.5f, 0f, 0f, width, height, api, objs);
                    addObjectiveAt(0.75f, 0.5f, 0f, 0f, width, height, api, objs);
                    break;
                case 3:
                    r = (float) Math.random();
                    if (r < 0.33f) {
                        addObjectiveAt(0.25f, 0.7f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.25f, 0.3f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.5f, 1f, 1f, width, height, api, objs);
                    } else if (r < 0.67f) {
                        addObjectiveAt(0.25f, 0.7f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.25f, 0.3f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.7f, 1f, 1f, width, height, api, objs);
                    } else {
                        addObjectiveAt(0.25f, 0.5f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.5f, 0.5f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.5f, 1f, 1f, width, height, api, objs);
                    }
                    break;
                case 4:
                    r = (float) Math.random();
                    if (r < 0.33f) {
                        addObjectiveAt(0.25f, 0.25f, 2f, 1f, width, height, api, objs);
                        addObjectiveAt(0.25f, 0.75f, 2f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.25f, 2f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.75f, 2f, 1f, width, height, api, objs);
                    } else if (r < 0.67f) {
                        addObjectiveAt(0.25f, 0.5f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.5f, 0.75f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.75f, 0.5f, 1f, 1f, width, height, api, objs);
                        addObjectiveAt(0.5f, 0.25f, 1f, 1f, width, height, api, objs);
                    } else {
                        addObjectiveAt(0.2f, 0.5f, 1f, 2f, width, height, api, objs);
                        addObjectiveAt(0.4f, 0.5f, 0f, 3f, width, height, api, objs);
                        addObjectiveAt(0.6f, 0.5f, 0f, 3f, width, height, api, objs);
                        addObjectiveAt(0.8f, 0.5f, 1f, 2f, width, height, api, objs);
                    }
                    break;
                default:
                    break;
            }
            api.getContext().setStandoffRange(height - 4500f);
        } else {
            api.getContext().setStandoffRange(6000f);
        }

        if (boostTime) {
            api.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void init(CombatEngineAPI engine) {
                    engine.getContext().aiRetreatAllowed = false;
                    engine.getContext().enemyDeployAll = true;
                    engine.getContext().fightToTheLast = true;
                }

                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    if (Global.getCombatEngine().isPaused()) {
                        return;
                    }

                    float trueFrameTime = Global.getCombatEngine().getElapsedInLastFrame();
                    float trueFPS = 1 / trueFrameTime;
                    float newTimeMult = Math.max(1f, trueFPS / 30f);
                    Global.getCombatEngine().getTimeMult().modifyMult("ii_tester", newTimeMult);
                }
            });
        }
    }

    private static float prevXDir = 0;
    private static float prevYDir = 0;

    private static void addObjectiveAt(float xMult, float yMult, float xOff, float yOff, float width, float height, MissionDefinitionAPI api, List<String> objs) {
        String type = pickAny();
        if ((objs != null) && !objs.isEmpty()) {
            int index = (int) (Math.random() * objs.size());
            type = objs.remove(index);
        }

        float xPad = 2000f;
        float yPad = 3000f;

        float minX = -width / 2 + xPad;
        float minY = -height / 2 + yPad;

        float x = (width - xPad * 2f) * xMult + minX;
        float y = (height - yPad * 2f) * yMult + minY;

        x = ((int) x / 1000) * 1000f;
        y = ((int) y / 1000) * 1000f;

        float offsetX = Math.round((Math.random() - 0.5f) * xOff * 2f) * 1000f;
        float offsetY = Math.round((Math.random() - 0.5f) * yOff * 2f) * 1000f;

        float xDir = (float) Math.signum(offsetX);
        float yDir = (float) Math.signum(offsetY);

        if (xDir == prevXDir && xOff > 0) {
            xDir = -xDir;
            offsetX = Math.abs(offsetX) * -prevXDir;
        }

        if (yDir == prevYDir && yOff > 0) {
            yDir = -yDir;
            offsetY = Math.abs(offsetY) * -prevYDir;
        }

        prevXDir = xDir;
        prevYDir = yDir;

        x += offsetX;
        y += offsetY;

        api.addObjective(x, y, type);

        if ((float) Math.random() > 0.6f) {
            float nebulaSize = (float) Math.random() * 1500f + 500f;
            api.addNebula(x, y, nebulaSize);
        }
    }

    private static String pickAny() {
        float r = (float) Math.random();
        if (r < 0.33f) {
            return "nav_buoy";
        } else if (r < 0.67f) {
            return "sensor_array";
        } else {
            return "comm_relay";
        }
    }
}
