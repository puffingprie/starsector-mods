package data.missions.M3;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class FM_Mission3DeploySetting extends BaseEveryFrameCombatPlugin {

    CombatEngineAPI engine;

    private int numOfLumen = 0;
    private int numOfGlimmer = 0;
    private int numOfBomberCarrier = 0;
    private int numOfFighterCarrier = 0;

    private int numOfDD = 0;

//    private int numOfGMShips = 0;

    @Override
    public void init(CombatEngineAPI engine) {
        super.init(engine);
        this.engine = engine;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        super.advance(amount, events);
        CombatFleetManagerAPI enemyFleetManager = engine.getFleetManager(FleetSide.ENEMY);
//        CombatFleetManagerAPI playerFleetManager = engine.getFleetManager(FleetSide.PLAYER);

        List<FleetMemberAPI> enemyReserves = enemyFleetManager.getReservesCopy();
//        List<FleetMemberAPI> playerReserves = playerFleetManager.getDeployedCopy();

        int deployNumber = enemyFleetManager.getReservesCopy().size();
        if (deployNumber > 0) {
            for (FleetMemberAPI enemyReserve : enemyReserves) {
                Vector2f deployLocation = new Vector2f(0, -engine.getMapHeight() * 0.5f);
                float deployFacing = 90f;
                if (enemyReserve.getHullId().equals("lumen")) {
                    deployFacing = -90f;
                    deployLocation = new Vector2f((-0.25f + numOfLumen / 6f) * engine.getMapWidth(), engine.getMapHeight() * 0.4f);
                    numOfLumen = numOfLumen + 1;
                }
                if (enemyReserve.getHullId().equals("glimmer")) {
                    deployFacing = 90f;
                    deployLocation = new Vector2f((-0.25f + numOfGlimmer / 6f) * engine.getMapWidth(), -engine.getMapHeight() * 0.44f);
                    numOfGlimmer = numOfGlimmer + 1;
                }
                if (enemyReserve.getVariant().getHullVariantId().equals("scintilla_Strike")) {
                    deployFacing = -90f;
                    deployLocation = new Vector2f((-0.25f + numOfBomberCarrier / 10f) * engine.getMapWidth(), engine.getMapHeight() * 0.07f);
                    numOfBomberCarrier = numOfBomberCarrier + 1;
                }
                if (enemyReserve.getVariant().getHullVariantId().equals("scintilla_Support")) {
                    deployFacing = 90f;
                    deployLocation = new Vector2f((-0.25f + numOfFighterCarrier / 2f) * engine.getMapWidth(), -engine.getMapHeight() * 0.47f);
                    numOfFighterCarrier = numOfFighterCarrier + 1;
                }
                if (enemyReserve.getHullId().equals("fulgent")) {
                    deployFacing = 90f;
                    deployLocation = new Vector2f((-0.25f + numOfDD / 2f) * engine.getMapWidth(), -engine.getMapHeight() * 0.41f);
                    numOfDD = numOfDD + 1;
                }
                deployNumber = deployNumber - 1;
                enemyFleetManager.spawnFleetMember(enemyReserve, deployLocation, deployFacing, 1f);
            }
        }
//        for (FleetMemberAPI playerReserve : playerReserves){
//            if (playerReserve.getShipName().startsWith("GMS")){
//                ShipAPI GMShip = playerFleetManager.spawnFleetMember(playerReserve,
//                        MathUtils.getPoint(new Vector2f(0,engine.getMapHeight() * 0.4f),800f,numOfGMShips * 90f - 60f),
//                        -90f,
//                        1.5f);
//                playerFleetManager.removeFromReserves(playerReserve);
//                if (GMShip.getName().equals("GMS Bhavaagra")){
//                    engine.setPlayerShipExternal(GMShip);
//                }
//                numOfGMShips = numOfGMShips + 1;
//            }
//        }

        engine.removePlugin(this);
    }
}
