package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.BattleAutoresolverPlugin;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

/**
 * Spawns ships from dedicated system slots if we are under a certain DP threshold
 * @author Nicke535
 * Implemented in Legacy of Arkgnesis by Gwyvern
 * Used with Permission by King Alfonzo
 */
public class HMI_RampantDerelictShipfactory extends BaseHullMod {
    //All the possible groups of ships to spawn with the hullmod. Also contains weights, so they can be of varying rarity
    private static final WeightedRandomPicker<List<String>> SPAWN_POSSIBILITIES = new WeightedRandomPicker<>();
    static {
        SPAWN_POSSIBILITIES.add(Arrays.asList("defender", "defender", "defender","defender"), 10f);
        SPAWN_POSSIBILITIES.add(Arrays.asList("picket", "picket", "picket","picket"), 10f);
        SPAWN_POSSIBILITIES.add(Arrays.asList("warden", "warden", "warden", "warden"), 10f);
        SPAWN_POSSIBILITIES.add(Arrays.asList("sentry", "sentry", "sentry", "sentry"), 10f);
    }

    //Minimum and maximum time between checking if we should start spawning a new group of ships
    private static final float MIN_WAVE_CHECK_DELAY = 15f;
    private static final float MAX_WAVE_CHECK_DELAY = 20f;

    //Minimum and maximum delay between spawning new ships in the same "wave"
    private static final float MIN_SPAWN_DELAY = 1f;
    private static final float MAX_SPAWN_DELAY = 4f;

    //If we have this much DP left to deploy on our side, we can spawn ships, otherwise we cannot
    private static final int EMPTY_DP_TO_SPAWN = 8;

    //The time it takes for a ship to "fade in" and properly enter combat (gaining collision and stuff)
    // The fade-in time is visual, while the collisionless time is how long it expects to be without collision
    private static final float SPAWN_FADE_IN_TIME = 0.3f;
    private static final float SPAWN_COLLISIONLESS_TIME = 5f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        //Run nothing in case we are dead
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!ship.isAlive()) {
            return;
        }

        //Gets our variables
        Object potData = engine.getCustomData().get(this.getClass().getName()+"CustomDataStorage");
        CustomDataStorage data;
        if (potData instanceof CustomDataStorage) {
            data = (CustomDataStorage) potData;
        } else {
            data = new CustomDataStorage();
            engine.getCustomData().put(this.getClass().getName()+"CustomDataStorage", data);
        }

        //Dumb way to find the correct fleetside
        FleetSide fleetSide = FleetSide.ENEMY;
        if (engine.getFleetManager(ship.getOwner()) == engine.getFleetManager(FleetSide.PLAYER)) {
            fleetSide = FleetSide.PLAYER;
        }

        //Are we currently in a deployment? In that case, run spawning code
        if (!data.spawnQueue.isEmpty()) {
            //Only spawn after our delay is up
            data.timerToNextSpawnInQueue -= amount;
            if (data.timerToNextSpawnInQueue > 0f) {
                return;
            }

            data.timerToNextSpawnInQueue = MathUtils.getRandomNumberInRange(MIN_SPAWN_DELAY, MAX_SPAWN_DELAY);

            //Suppresses extra messages until we are done with deployment
            engine.getFleetManager(fleetSide).setSuppressDeploymentMessages(true);

            //Spawns the next queued ship from our spawn location
            //Gets a random location to spawn the wing
            Vector2f loc = getSpawnLocation(ship);

            //Gets the next ship variant to spawn
            String variantToSpawn = getRandomVariantForHull(data.spawnQueue.poll());

            //Spawns the new ship, adjusts its side properly and then gives it our custom spawn-collision-prevention script
            ShipAPI newShip = CombatUtils.spawnShipOrWingDirectly(variantToSpawn, FleetMemberType.SHIP, fleetSide, 0.7f, loc, ship.getFacing());
            if (ship.isAlly()) {
                newShip.setAlly(true);
            }
            FleetMemberAPI member = newShip.getFleetMember();
            member.setOwner(fleetSide.ordinal());
            newShip.getVelocity().set(ship.getVelocity());
            engine.addPlugin(new SpawnCollisionPreventer(newShip));
            engine.getFleetManager(fleetSide).setSuppressDeploymentMessages(false);
        }

        //Otherwise, we check the battlefield now and then to see if we should start another deployment of ships
        else {
            data.waveCheckTimer.advance(amount);
            if (data.waveCheckTimer.intervalElapsed()) {
                //Add up all allied DP and compare to our allowance
                int totalDPDeployed = 0;
                for (FleetMemberAPI member : engine.getFleetManager(ship.getOwner()).getDeployedCopy()) {
                    // Vanilla by default ignores the stations for DP limits on one side, effectively making it a free deployment
                    if (member != ship.getFleetMember()) {
                        totalDPDeployed += member.getDeploymentPointsCost();
                    }
                }
                float dpMaxOnOurSide = Global.getCombatEngine().getFleetManager(fleetSide).getMaxStrength();
                if (totalDPDeployed < (dpMaxOnOurSide-EMPTY_DP_TO_SPAWN)) {
                    data.spawnQueue.addAll(pickGroupToSpawn());
                    data.timerToNextSpawnInQueue = MathUtils.getRandomNumberInRange(MIN_SPAWN_DELAY, MAX_SPAWN_DELAY);
                }
            }
        }
    }

    //Function for getting the spawn location, IE the system slot location
    //If multiple exists, choose one at random
    private Vector2f getSpawnLocation(ShipAPI ship) {
        WeightedRandomPicker<WeaponSlotAPI> picker = new WeightedRandomPicker<>();
        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            if (slot.isSystemSlot() && slot.getId().contains("Shipyard")) {
                picker.add(slot);
            }
        }
        if (picker.isEmpty()) {
            throw new RuntimeException("FATAL - LoA Ship Factory Script : Attempted to spawn a ship without a properly-named system slot!");
        } else {
            return picker.pick().computePosition(ship);
        }
    }

    //Picks one spawn group to spawn, and shuffles their internal spawn order
    private List<String> pickGroupToSpawn() {
        List<String> returnValue = new LinkedList<>(SPAWN_POSSIBILITIES.pick());
        Collections.shuffle(returnValue);
        return returnValue;
    }

    //Data storage class
    private static class CustomDataStorage {
        Queue<String> spawnQueue = new LinkedList<>();
        float timerToNextSpawnInQueue = MathUtils.getRandomNumberInRange(MIN_SPAWN_DELAY, MAX_SPAWN_DELAY);
        IntervalUtil waveCheckTimer = new IntervalUtil(MIN_WAVE_CHECK_DELAY, MAX_WAVE_CHECK_DELAY);
    }

    //Plugin for preventing collisions when spawning a ship
    private static class SpawnCollisionPreventer extends BaseEveryFrameCombatPlugin {
        float timeSpentSpawning = 0f;
        ShipAPI ship;
        private String id;
        SpawnCollisionPreventer(ShipAPI ship) {
            this.ship = ship;
            //To prevent one-frame stupidities, that may or may not happen
            ship.setCollisionClass(CollisionClass.NONE);
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            ship.blockCommandForOneFrame(ShipCommand.FIRE);
            ship.getShipAI().setDoNotFireDelay(SPAWN_COLLISIONLESS_TIME);
            id = "hmi_ship_factory_fire_prevention"+ship.getId();
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            timeSpentSpawning += amount;
            //Are we finished with spawning? If so, run cleanup
            if (timeSpentSpawning >= SPAWN_COLLISIONLESS_TIME) {
                ship.setCollisionClass(CollisionClass.SHIP);
                ship.setExtraAlphaMult(1f);
                ship.setApplyExtraAlphaToEngines(false);
                enableWeaponry();
                Global.getCombatEngine().removePlugin(this);
            }

            //Otherwise, handle spawn animation and stuff
            else {
                //Remove collision
                ship.setCollisionClass(CollisionClass.NONE);

                //Block our shipsystem and weapons from activating while spawning
                ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
                ship.blockCommandForOneFrame(ShipCommand.FIRE);
                disableWeaponry();

                //Handle opacity so we "fade into existance", similar to fighters taking off
                ship.setExtraAlphaMult(Math.min(1f, timeSpentSpawning/SPAWN_FADE_IN_TIME));
                ship.setApplyExtraAlphaToEngines(true);

                //Move the ship away from any and all things it is colliding with (will most likely just be its
                // mothership, but might be other ships as well that we don't wanna collide with)
                //      Note: it moves so it takes exactly its entire spawn duration to escape the area. Might be janky
                //            in some edge-cases, and need to be rewritten if it's too prevalent
                Vector2f desiredMoveVector = new Vector2f(0f, 0f);
                for (ShipAPI otherShip : CombatUtils.getShipsWithinRange(ship.getLocation(), ship.getCollisionRadius())) {
                    //Ignore ships without collision, and fighters
                    if (otherShip.getCollisionClass().equals(CollisionClass.NONE) || otherShip.getHullSize().equals(ShipAPI.HullSize.FIGHTER)) {
                        continue;
                    }

                    //Now, check distance to the other ship, this is important for our move vector
                    float distance = MathUtils.getDistance(otherShip.getLocation(), ship.getLocation());

                    //Then, convert the distance into an "overlap" based on our collision radii: this is what directly
                    //affects push strength
                    float overlap = -(distance-ship.getCollisionRadius()-otherShip.getCollisionRadius());
                    if (overlap < 0f) {
                        overlap = 0f;
                    }

                    //Finally, figure out a direction to push, and add our vector to the total move vector
                    Vector2f dir = VectorUtils.getDirectionalVector(otherShip.getLocation(), ship.getLocation());
                    desiredMoveVector.x += dir.x*overlap;
                    desiredMoveVector.y += dir.y*overlap;
                }
                ship.getLocation().x += desiredMoveVector.x*amount/(SPAWN_COLLISIONLESS_TIME-timeSpentSpawning);
                ship.getLocation().y += desiredMoveVector.y*amount/(SPAWN_COLLISIONLESS_TIME-timeSpentSpawning);
            }
        }

        //Since our normal approach above didn't seem to work, *brute force* the ship to not fire
        private Map<WeaponAPI, Integer> storedAmmo = new HashMap<WeaponAPI, Integer>();
        private void disableWeaponry() {
            for (WeaponAPI weap : ship.getAllWeapons()) {
                //If our "stored ammo" is -1, we have been disabled by this script already, and should be ignored
                if (storedAmmo.get(weap) != null && storedAmmo.get(weap) == -1) {continue;}

                //Disables all weapons with 4 different methods to *ensure* we disable them properly, unless some extremely weird script is on the weapons in question
                //The 2 other methods are done after this loop (flux cost and fire-rate)
                if (weap.getMaxAmmo() >= 1) {
                    if (storedAmmo.get(weap) != null) {
                        storedAmmo.put(weap, storedAmmo.get(weap) + weap.getAmmo());
                    } else {
                        storedAmmo.put(weap, weap.getAmmo());
                    }
                    weap.setAmmo(0);
                } else {
                    weap.setMaxAmmo(1);
                    weap.setAmmo(0);
                    storedAmmo.put(weap, -1);
                }

                if (weap.getCooldownRemaining() < 0.2f) {
                    weap.setRemainingCooldownTo(0.2f);
                }
            }

            //Modifies RoF and flux cost for weapons so they cannot fire even if the above code doesn't work
            ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyMult(id, 999999999999999999f);
            ship.getMutableStats().getEnergyRoFMult().modifyMult(id, 0.000000000000000001f);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyMult(id, 999999999999999999f);
            ship.getMutableStats().getBallisticRoFMult().modifyMult(id, 0.000000000000000001f);
            ship.getMutableStats().getMissileWeaponFluxCostMod().modifyMult(id, 999999999999999999f);
            ship.getMutableStats().getMissileRoFMult().modifyMult(id, 0.000000000000000001f);
        }
        private void enableWeaponry() {
            //Resets all the stats for the weapons
            ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(id);
            ship.getMutableStats().getEnergyRoFMult().unmodify(id);
            ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(id);
            ship.getMutableStats().getBallisticRoFMult().unmodify(id);
            ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(id);
            ship.getMutableStats().getMissileRoFMult().unmodify(id);
            for (WeaponAPI weap : ship.getAllWeapons()) {
                if (storedAmmo.get(weap) == null) {
                    continue;
                }

                if (storedAmmo.get(weap) == -1) {
                    weap.setAmmo(1);
                    weap.setMaxAmmo(0);
                } else {
                    weap.setAmmo(storedAmmo.get(weap));
                }
            }
            storedAmmo.clear();
        }
    }

    //Picks a random variant from a specific hull, as long as that variant is a "stock" variant (that is, not player-made)
    private static String getRandomVariantForHull(String hullID) {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (String id : Global.getSettings().getAllVariantIds()) {
            ShipVariantAPI variant = Global.getSettings().getVariant(id);
            if (variant != null && variant.isGoalVariant()) {
                if (variant.getHullSpec().getHullId().equals(hullID)) {
                    picker.add(id);
                }
            }
        }
        return picker.pick();
    }
}