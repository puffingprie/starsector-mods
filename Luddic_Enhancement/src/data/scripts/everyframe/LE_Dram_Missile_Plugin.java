package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatAssignmentType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI.AssignmentInfo;
import com.fs.starfarer.api.combat.CombatTaskManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponGroupAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import data.hullmods.LuddEnhance_Bombardment;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import data.scripts.util.LE_AnamorphicFlare;
import data.scripts.util.LE_Util;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class LE_Dram_Missile_Plugin extends BaseEveryFrameCombatPlugin {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant
    public static final float CR_PENALTY = 0.3f;

    private static final Color MIGHT_COLOR_1 = new Color(255, 200, 150, 100);
    private static final Color MIGHT_COLOR_2 = new Color(255, 255, 255);
    private static final Color MIGHT_COLOR_3 = new Color(255, 255, 175);
    private static final Color MIGHT_COLOR_4 = new Color(255, 150, 0);
    private static final Color MIGHT_COLOR_5 = new Color(200, 100, 0);
    private static final Color MIGHT_SMOKE_COLOR = new Color(120, 140, 160, 200);

    private static final String DATA_KEY = "LE_Dram_Missile_Plugin";
    private final Object STATUSKEY1 = new Object();

    public static final float EXPANSION_BLAST_DPS = 10000f;
    public static final float EXPANSION_RATE = 185f;
    public static final float EXPANSION_TIME = 6f;
    public static final float INITIAL_BLAST_DAMAGE = 6000f;
    public static final float INITIAL_BLAST_RADIUS = 400f;

    public static final float EXPANSION_BLAST_DPS_PHAETON = 13000f;
    public static final float EXPANSION_RATE_PHAETON = 195f;
    public static final float EXPANSION_TIME_PHAETON = 8f;
    public static final float INITIAL_BLAST_DAMAGE_PHAETON = 8000f;
    public static final float INITIAL_BLAST_RADIUS_PHAETON = 500f;

    public static final float EXPANSION_BLAST_DPS_PROM = 16000f;
    public static final float EXPANSION_RATE_PROM = 205f;
    public static final float EXPANSION_TIME_PROM = 9f;
    public static final float INITIAL_BLAST_DAMAGE_PROM = 10000f;
    public static final float INITIAL_BLAST_RADIUS_PROM = 600f;

    public static final float EXPANSION_RATE_ELITE = 400f;
    public static final float INITIAL_BLAST_RADIUS_ELITE = 1750f;
    public static final Map<HullSize, Float> INITIAL_BLAST_EXTRA_OVERLOAD_ELITE = new HashMap<>(5); // percent

    static {
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.FIGHTER, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.FRIGATE, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.DESTROYER, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.DEFAULT, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.CRUISER, 150f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.CAPITAL_SHIP, 100f);
    }

    public static final Map<HullSize, Float> INITIAL_BLAST_EXTRA_OVERLOAD_BURST = new HashMap<>(5); // percent

    static {
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.FIGHTER, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.FRIGATE, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.DESTROYER, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.DEFAULT, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.CRUISER, 75f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.CAPITAL_SHIP, 50f);
    }


    private static final float ACCUM_INTERVAL = 0.05f;

    private static final Vector2f ZERO = new Vector2f();

    private SoundAPI sound = null;

    public static void explode(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> mightSource = localData.mightSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = mightSource.get(ship);

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.MIGHT));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

//        LE_AnamorphicFlare.createFlare(ship, loc, engine, 1f, 0.01f,
//               10f * attenuate, 40f * attenuate, 1f, MIGHT_COLOR_1, MIGHT_COLOR_2);

        engine.applyDamage(ship, loc, 200000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);

        List<ShipAPI> targets = LE_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS);

        float blastDamage = INITIAL_BLAST_DAMAGE * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS * attenuate;
        float expansionTime = EXPANSION_TIME * attenuate;
        float expansionRate = EXPANSION_RATE * (attenuate / (float) Math.sqrt(attenuate));


        for (ShipAPI target : targets) {

            if (target == ship) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }
            if (target.isPhased()) {
                continue;
            }

            for (int j = 0; j < 5; j++) {
                int k = 0;
                while (true) {
                    k++;
                    Vector2f point = new Vector2f(target.getLocation());
                    point.x += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
                    point.y += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);

                    if (CollisionUtils.isPointWithinBounds(point, target)) {
                        engine.applyDamage(target, point,
                                0.2f * blastDamage * (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius,
                                DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
                        break;
                    }

                    if (k >= 1000) {
                        break;
                    }
                }
            }
        }

        List<CombatEntityAPI> rocks = LE_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        List<MissileAPI> missiles = LE_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        for (int i = 0; i < 600 * attenuate; i++) {
            if (i % 24 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                35),
                        ((float) Math.random() * 200f + 100f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.5f);
            }
//            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
//                    ((float) Math.random() * 100f + 100f) * attenuate, 0.35f,
//                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.5f,
//                    new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f))));
 //           engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
 //                   ((float) Math.random() * 200f + 100f) * attenuate, 0.25f,
 //                   (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.5f,
 //                   new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f)),
 //                           LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f))));
        }

//        engine.addHitParticle(loc, new Vector2f(),
//                1000f * attenuate, 2f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
//               MIGHT_COLOR_3);
//        engine.addHitParticle(loc, new Vector2f(),
//                1500f * attenuate, 3f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
//                MIGHT_COLOR_3);
//        engine.spawnExplosion(loc, new Vector2f(),
//                MIGHT_COLOR_4, 1000f * attenuate, 5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                MIGHT_COLOR_1, 1500f * attenuate, 7f * (float) Math.sqrt(attenuate));
//        engine.spawnExplosion(loc, new Vector2f(),
//               MIGHT_COLOR_5, 2000f * attenuate, 10f * (float) Math.sqrt(attenuate));

        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_close", 1f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_distant", 1f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    public static void explodePhaeton(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> mightSource = localData.mightSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = mightSource.get(ship);

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.MIGHTPHAETON));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

//        LE_AnamorphicFlare.createFlare(ship, loc, engine, 1f, 0.01f,
//               10f * attenuate, 40f * attenuate, 1f, MIGHT_COLOR_1, MIGHT_COLOR_2);

        engine.applyDamage(ship, loc, 400000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);

        List<ShipAPI> targets = LE_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS_PHAETON);

        float blastDamage = INITIAL_BLAST_DAMAGE_PHAETON * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS_PHAETON * attenuate;
        float expansionTime = EXPANSION_TIME_PHAETON * attenuate;
        float expansionRate = EXPANSION_RATE_PHAETON * (attenuate / (float) Math.sqrt(attenuate));

        for (ShipAPI target : targets) {
            if (target == ship) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }
            if (target.isPhased()) {
                continue;
            }

            for (int j = 0; j < 5; j++) {
                int k = 0;
                while (true) {
                    k++;
                    Vector2f point = new Vector2f(target.getLocation());
                    point.x += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
                    point.y += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);

                    if (CollisionUtils.isPointWithinBounds(point, target)) {
                        engine.applyDamage(target, point,
                                0.2f * blastDamage * (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius,
                                DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
                        break;
                    }

                    if (k >= 1000) {
                        break;
                    }
                }
            }
        }

        List<CombatEntityAPI> rocks = LE_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        List<MissileAPI> missiles = LE_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        for (int i = 0; i < 600 * attenuate; i++) {
            if (i % 18 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                35),
                        ((float) Math.random() * 300f + 200f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.6f);
            }
//            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
//                    ((float) Math.random() * 200f + 200f) * attenuate, 0.35f,
//                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.6f,
//                    new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f))));
//            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
//                    ((float) Math.random() * 200f + 100f) * attenuate, 0.25f,
//                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.6f,
//                    new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f))));
        }

//        engine.addHitParticle(loc, new Vector2f(),
//                1000f * attenuate, 2f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
//                MIGHT_COLOR_3);
//        engine.addHitParticle(loc, new Vector2f(),
//                1500f * attenuate, 3f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
//                MIGHT_COLOR_3);
//        engine.spawnExplosion(loc, new Vector2f(),
//                MIGHT_COLOR_4, 1000f * attenuate, 7f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                MIGHT_COLOR_1, 1500f * attenuate, 10f * (float) Math.sqrt(attenuate));
//        engine.spawnExplosion(loc, new Vector2f(),
//               MIGHT_COLOR_5, 2000f * attenuate, 12f * (float) Math.sqrt(attenuate));
//      engine.spawnExplosion(loc, new Vector2f(),
//                MIGHT_COLOR_2, 2500f * attenuate, 15f * (float) Math.sqrt(attenuate));


        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_close", 1f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_distant", 1f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    public static void explodePrometheus(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> mightSource = localData.mightSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = mightSource.get(ship);

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.MIGHTPROM));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

//        LE_AnamorphicFlare.createFlare(ship, loc, engine, 1f, 0.01f,
//               10f * attenuate, 40f * attenuate, 1f, MIGHT_COLOR_1, MIGHT_COLOR_2);

        engine.applyDamage(ship, loc, 800000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);

        List<ShipAPI> targets = LE_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS_PROM);

        float blastDamage = INITIAL_BLAST_DAMAGE_PROM * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS_PROM * attenuate;
        float expansionTime = EXPANSION_TIME_PROM * attenuate;
        float expansionRate = EXPANSION_RATE_PROM * (attenuate / (float) Math.sqrt(attenuate));

        for (ShipAPI target : targets) {
            if (target == ship) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }
            if (target.isPhased()) {
                continue;
            }

            for (int j = 0; j < 5; j++) {
                int k = 0;
                while (true) {
                    k++;
                    Vector2f point = new Vector2f(target.getLocation());
                    point.x += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
                    point.y += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);

                    if (CollisionUtils.isPointWithinBounds(point, target)) {
                        engine.applyDamage(target, point,
                                0.2f * blastDamage * (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius,
                                DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
                        break;
                    }

                    if (k >= 1000) {
                        break;
                    }
                }
            }
        }

        List<CombatEntityAPI> rocks = LE_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        List<MissileAPI> missiles = LE_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source);
        }

        for (int i = 0; i < 600 * attenuate; i++) {
            if (i % 12 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                35),
                        ((float) Math.random() * 400f + 200f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.8f);
            }
//            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
//                    ((float) Math.random() * 300f + 200f) * attenuate, 0.35f,
//                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.8f,
//                    new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f))));
//            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
//                    ((float) Math.random() * 200f + 100f) * attenuate, 0.25f,
//                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.8f,
//                   new Color(LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f)),
//                            LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
//                           LE_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f))));
        }

//        engine.addHitParticle(loc, new Vector2f(),
//                1000f * attenuate, 2f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
 //               MIGHT_COLOR_3);
//        engine.addHitParticle(loc, new Vector2f(),
//                1500f * attenuate, 3f * (float) Math.sqrt(attenuate), 15f * (float) Math.sqrt(attenuate),
//                MIGHT_COLOR_3);
//        engine.spawnExplosion(loc, new Vector2f(),
//               MIGHT_COLOR_4, 1000f * attenuate, 7f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                MIGHT_COLOR_1, 1500f * attenuate, 10f * (float) Math.sqrt(attenuate));
//        engine.spawnExplosion(loc, new Vector2f(),
//                MIGHT_COLOR_5, 2000f * attenuate, 12f * (float) Math.sqrt(attenuate));
//        engine.spawnExplosion(loc, new Vector2f(),
//                MIGHT_COLOR_2, 2500f * attenuate, 15f * (float) Math.sqrt(attenuate));


        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_close", 1f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("le_might_explode_distant", 1f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    private CombatEngineAPI engine;
    private boolean setEndCombatFlag = false;
    private FleetMemberAPI dummyMember = null;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, ShipAPI> mightSource = localData.mightSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        for (DamagingProjectileAPI projectile : projectiles) {
            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            if (projectile.getProjectileSpecId().contentEquals("le_dram_missile")) {
                WeaponAPI source = projectile.getWeapon();

                Vector2f location = new Vector2f(projectile.getLocation());
                ShipAPI ship = projectile.getSource();
                float angle = projectile.getFacing();
                int owner = projectile.getOwner();

                if (source != null) {
                    source.setAmmo(0);
                }

                engine.removeEntity(projectile);

                FleetMemberAPI missileMember; {
                    missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "le_dram_missile_var");
                }
                missileMember.getRepairTracker().setCrashMothballed(false);
                missileMember.getRepairTracker().setMothballed(false);
                missileMember.getRepairTracker().setCR(ship.getCurrentCR());
                missileMember.setOwner(owner);
                missileMember.setAlly(ship.isAlly());
                missileMember.setShipName("Might of Ludd");
                boolean suppress = engine.getFleetManager(owner).isSuppressDeploymentMessages();
                engine.getFleetManager(owner).setSuppressDeploymentMessages(true);
                ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 0f);
                missile.setCollisionClass(CollisionClass.FIGHTER);
                missile.getVelocity().set(ship.getVelocity());
                missile.setAngularVelocity(ship.getAngularVelocity());
                for (ShipAPI child : missile.getChildModulesCopy()) {
                    child.setCollisionClass(CollisionClass.FIGHTER);
                }
                mightSource.put(missile, ship);
                missile.setInvalidTransferCommandTarget(true);
                engine.getFleetManager(owner).setSuppressDeploymentMessages(suppress);

                for (ShipEngineAPI thruster : missile.getEngineController().getShipEngines()) {
                    Vector2f loc = thruster.getLocation();
                    engine.addSmokeParticle(loc, ship.getVelocity(), thruster.getEngineSlot().getWidth() * 4f, 1f, MathUtils.getRandomNumberInRange(2f, 4f), MIGHT_SMOKE_COLOR);
                }

                ship.setCurrentCR(Math.max(0f, ship.getCurrentCR() - LuddEnhance_Bombardment.getCRPenalty(ship.getVariant())));
                if (ship.getFleetMember() != null) {
                    ship.getFleetMember().getRepairTracker().applyCREvent(-LuddEnhance_Bombardment.getCRPenalty(ship.getVariant()), "Deployed Light in battle");
                }
                ship.setMass(ship.getMass() - missile.getMassWithModules());

                float distanceToHead = MathUtils.getDistance(missile, Global.getCombatEngine().getViewport().getCenter());
                if (distanceToHead <= 2500f) {
                    float refDist = 1500f;
                    float vol = refDist / Math.max(refDist, distanceToHead);{
                        Global.getSoundPlayer().playUISound("le_might_launch_close", 1f, vol);
                    }
                } else {
                    float refDist = 3000f;
                    float vol = refDist / Math.max(refDist, distanceToHead);
                        Global.getSoundPlayer().playUISound("le_might_launch_distant", 1f, vol);
                }
                break;
            }
        }

        Iterator<Map.Entry<String, ExplosionData>> iter = explodingShips.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ExplosionData> entry = iter.next();
            ExplosionData ed = entry.getValue();
            ShipAPI ship = ed.ship;

            ShipAPI source = mightSource.get(ship);
            if (source == null) {
                source = ship;
            }

            setEndCombatFlag = true;
            engine.setDoNotEndCombat(true);
            engine.getFleetManager(FleetSide.ENEMY).getTaskManager(false).setPreventFullRetreat(true);

            ed.accum += amount;
            while (ed.accum >= ACCUM_INTERVAL) {
                ed.accum -= ACCUM_INTERVAL;

                if ((ed.type == ExplosionType.MIGHT) || (ed.type == ExplosionType.MIGHTPHAETON) || (ed.type == ExplosionType.MIGHTPROM)) {
                    float expansionTime;
                    float expansionRate;
                    float expansionDPS;
                    if (ed.type == ExplosionType.MIGHT) {
                        expansionTime = EXPANSION_TIME * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS * (float) Math.sqrt(ed.atten);
                    } else if (ed.type == ExplosionType.MIGHTPHAETON) {
                        expansionTime = EXPANSION_TIME_PHAETON * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE_PHAETON * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS_PHAETON * (float) Math.sqrt(ed.atten);
                    } else {
                        expansionTime = EXPANSION_TIME_PROM * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE_PROM * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS_PROM * (float) Math.sqrt(ed.atten);
                    }

                    float distance = ed.t * expansionRate;
                    float damage = expansionDPS * ACCUM_INTERVAL * (expansionTime - ed.t * 0.75f) / expansionTime;

                    if (ed.t <= 0.1f) {
                        engine.applyDamage(ship, ship.getLocation(), 200000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);
                    }

                    List<ShipAPI> targets = LE_Util.getShipsWithinRange(ed.loc, distance);

                    Iterator<ShipAPI> iter2 = targets.iterator();
                    while (iter2.hasNext()) {
                        ShipAPI target = iter2.next();

                        if (target.getCollisionClass() == CollisionClass.NONE) {
                            iter2.remove();
                            continue;
                        }

                        float distanceToLoc = MathUtils.getDistance(target.getLocation(), ed.loc);
                        if ((distanceToLoc > (distance + target.getCollisionRadius()))
                                || (distanceToLoc < (distance - target.getCollisionRadius()))) {
                            iter2.remove();
                        }
                    }

                    while (iter2.hasNext()) {
                        ShipAPI target = iter2.next();

                        boolean remove = false;
                        for (ShipAPI s : targets) {
                            if ((s.getShield() != null) && (s != target)) {
                                if (s.getShield().isWithinArc(target.getLocation()) && s.getShield().isOn()
                                        && (MathUtils.getDistance(target, s.getShield().getLocation()) <= s.getShield().getRadius())) {
                                    remove = true;
                                    break;
                                }
                            }
                        }

                        if (remove) {
                            iter2.remove();
                        }
                    }

                    for (ShipAPI target : targets) {
                        if (target == ship) {
                            continue;
                        }

                        for (int i = 0; i < 50; i++) {
                            Vector2f damageLoc = MathUtils.getPointOnCircumference(ed.loc, distance, VectorUtils.getAngleStrict(ed.loc, target.getLocation()));
                            Vector2f.add(damageLoc, MathUtils.getRandomPointInCircle(null, target.getCollisionRadius()), damageLoc);
                            if (((target.getShield() != null) && target.getShield().isWithinArc(damageLoc) && target.getShield().isOn()
                                    && (MathUtils.getDistance(damageLoc, target.getShield().getLocation()) <= target.getShield().getRadius()))
                                    || CollisionUtils.isPointWithinBounds(damageLoc, target)) {
                                engine.applyDamage(target, damageLoc, damage, DamageType.FRAGMENTATION, 0f, false, true, source, true);
                                break;
                            }
                        }
                    }

                    ed.t += ACCUM_INTERVAL;
                    if (ed.t >= expansionTime) {
                        iter.remove();
                        break;
                    }
                }
            }
        }

        if (explodingShips.isEmpty()) {
            if (setEndCombatFlag) {
                setEndCombatFlag = false;
                if (!engine.isSimulation()) {
                    engine.setDoNotEndCombat(false);
                }
                engine.getFleetManager(FleetSide.ENEMY).getTaskManager(false).setPreventFullRetreat(false);
                if (dummyMember != null) {
                    engine.getFleetManager(FleetSide.ENEMY).removeFromReserves(dummyMember);
                    ShipAPI dummyShip = engine.getFleetManager(FleetSide.ENEMY).getShipFor(dummyMember);
                    if (dummyShip != null) {
                        engine.removeEntity(dummyShip);
                    }
                    dummyMember = null;
                }
            }
        }

        for (ShipAPI ship : engine.getShips()) {
            if (!ship.isAlive()) {
                switch (LE_Util.getNonDHullId(ship.getHullSpec())) {
                    case "le_prometheus3":
                        List<WeaponAPI> weapons = ship.getAllWeapons();
                        for (WeaponAPI weapon : weapons) {
                            if (weapon.getId().contentEquals("le_dram_w")) {
                                if (weapon.getAmmo() > 0) {
                                    ship.setMass(ship.getMass() - 800f);
                                    weapon.setAmmo(0);
                                }
                                break;
                            }
                        }
                        break;
                    case "lp_dram_missile":
                        ship.setCollisionClass(CollisionClass.SHIP);
                        break;
                    default:
                        break;
                }
            } else if (LE_Util.getNonDHullId(ship.getHullSpec()).contentEquals("le_prometheus3")) {
                if (ship.getCurrentCR() < LuddEnhance_Bombardment.getCRPenalty(ship.getVariant())) {
                    List<WeaponAPI> weapons = ship.getAllWeapons();
                    for (WeaponAPI weapon : weapons) {
                        if (weapon.getId().contentEquals("le_dram_w")) {
                            if (weapon.getAmmo() > 0) {
                                ship.setMass(ship.getMass() - 800f);
                                weapon.setAmmo(0);
                            }
                            break;
                        }
                    }
                }

                if (ship.getShipAI() != null) {
                    boolean shouldFire = false;

                    float range = 5000f;

                    List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, range);
                    if (!enemies.isEmpty()) {
                        shouldFire = true;
                    }

                    if (shouldFire) {
                        WeaponGroupAPI mightGroup = null;
                        for (WeaponAPI weapon : ship.getUsableWeapons()) {
                            if (weapon.getId().contentEquals("le_dram_w")) {
                                if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled() && (weapon.getAmmo() > 0)) {
                                    mightGroup = ship.getWeaponGroupFor(weapon);
                                    break;
                                }
                            }
                        }

                        if (mightGroup != null) {
                            int groupNum = 0;
                            boolean foundGroup = false;
                            for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                                if (group == mightGroup) {
                                    foundGroup = true;
                                    break;
                                } else {
                                    groupNum++;
                                }
                            }
                            if (foundGroup) {
                                if (ship.getSelectedGroupAPI() != mightGroup) {
                                    ship.giveCommand(ShipCommand.SELECT_GROUP, null, groupNum);
                                }
                                if (ship.getSelectedGroupAPI() == mightGroup) {
                                    ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), groupNum);
                                }
                            }
                        }
                    }
                }
            }  else {
                boolean nuke = false;
                boolean might = false;
                switch (LE_Util.getNonDHullId(ship.getHullSpec())) {
                    case "lp_dram_missile":
                        nuke = true;
                        might = true;
                        break;
                    default:
                        break;
                }

                if (might && (ship.getOwner() == 0)) {
                    if (!engine.getFogOfWar(1).isVisible(ship)) {
                        continue;
                    }
                    CombatFleetManagerAPI fleetManager = engine.getFleetManager(1);
                    if (fleetManager == null) {
                        continue;
                    }
                    CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(false);
                    if (taskManager == null) {
                        continue;
                    }
                    if (taskManager.getCommandPointsLeft() <= 0) {
                        continue;
                    }
                    CombatFleetManagerAPI playerFleetManager = engine.getFleetManager(0);
                    DeployedFleetMemberAPI dfm = playerFleetManager.getDeployedFleetMember(ship);
                    if (dfm == null) {
                        continue;
                    }
                    boolean assigned = false;
                    List<AssignmentInfo> assignments = taskManager.getAllAssignments();
                    for (AssignmentInfo assignment : assignments) {
                        if ((assignment.getTarget() == dfm) && (assignment.getType() == CombatAssignmentType.INTERCEPT)) {
                            assigned = true;
                            break;
                        }
                    }
                    if (assigned) {
                        continue;
                    }
                    taskManager.createAssignment(CombatAssignmentType.INTERCEPT, dfm, true);
                }

                if (nuke) {
                    float dangerRadius;
                    if (LE_Util.getNonDHullId(ship.getHullSpec()).contentEquals("lp_dram_missile")) {
                        dangerRadius = INITIAL_BLAST_RADIUS + (EXPANSION_RATE * 5f);
                    } else {
                        dangerRadius = INITIAL_BLAST_RADIUS_ELITE + (EXPANSION_RATE_ELITE * 5f);
                    }

                    for (ShipAPI otherShip : engine.getShips()) {
                        if ((otherShip == ship) || (otherShip.getOwner() == 100) || (otherShip.getOwner() == ship.getOwner()) || !otherShip.isAlive()
                                || otherShip.isDrone() || otherShip.isFighter() || otherShip.isShuttlePod() || otherShip.isStation() || otherShip.isStationModule()) {
                            continue;
                        }
                        if (otherShip.getAIFlags() == null) {
                            continue;
                        }
                        float dist = MathUtils.getDistance(ship, otherShip);
                        if (dist > dangerRadius) {
                            continue;
                        }
                        CombatFleetManagerAPI fleetManager = engine.getFleetManager(otherShip.getOwner());
                        if (fleetManager == null) {
                            continue;
                        }
                        CombatTaskManagerAPI taskManager = fleetManager.getTaskManager(otherShip.isAlly());
                        if (taskManager == null) {
                            continue;
                        }
                        /* Don't run away from the nuke if ordered to engage it */
                        AssignmentInfo assignment = taskManager.getAssignmentFor(otherShip);
                        if ((assignment != null) && (assignment.getTarget() != null)) {
                            if ((assignment.getType() == CombatAssignmentType.INTERCEPT)
                                    && (MathUtils.getDistance(assignment.getTarget().getLocation(), ship.getLocation()) < 100f)) {
                                continue;
                            }
                        }
                        otherShip.getAIFlags().setFlag(AIFlags.RUN_QUICKLY, 1f);
                        otherShip.getAIFlags().setFlag(AIFlags.MANEUVER_TARGET, 1f, ship);
                        otherShip.getAIFlags().setFlag(AIFlags.HAS_INCOMING_DAMAGE, 1f);
                        otherShip.getAIFlags().setFlag(AIFlags.IN_CRITICAL_DPS_DANGER, 1f);
                        otherShip.getAIFlags().setFlag(AIFlags.KEEP_SHIELDS_ON, 1f);
                        otherShip.getAIFlags().setFlag(AIFlags.DO_NOT_PURSUE, 1f);
                        otherShip.getAIFlags().setFlag(AIFlags.BACK_OFF, 1f);
                        otherShip.getAIFlags().unsetFlag(AIFlags.HARASS_MOVE_IN);
                        otherShip.getAIFlags().unsetFlag(AIFlags.MAINTAINING_STRIKE_RANGE);
                        otherShip.getAIFlags().unsetFlag(AIFlags.DO_NOT_USE_SHIELDS);
                        otherShip.getAIFlags().unsetFlag(AIFlags.PURSUING);
                        otherShip.getAIFlags().unsetFlag(AIFlags.DO_NOT_BACK_OFF);
                        otherShip.getAIFlags().unsetFlag(AIFlags.SAFE_FROM_DANGER_TIME);
                        otherShip.getAIFlags().unsetFlag(AIFlags.PHASE_ATTACK_RUN);
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class ExplosionData {

        final ShipAPI ship;
        final float atten;
        float t;
        float accum;
        final Vector2f loc = new Vector2f();
        final ExplosionType type;

        ExplosionData(ShipAPI ship, float atten, Vector2f loc, ExplosionType type) {
            this.ship = ship;
            this.t = 0f;
            this.accum = ACCUM_INTERVAL;
            this.atten = atten;
            this.loc.set(loc);
            this.type = type;
        }
    }

    private static enum ExplosionType {
        MIGHT,
        MIGHTPHAETON,
        MIGHTPROM,
    }

    private static final class LocalData {

        final Map<ShipAPI, ShipAPI> mightSource = new LinkedHashMap<>(10);
        final Map<String, ExplosionData> explodingShips = new LinkedHashMap<>(10);
    }
}
