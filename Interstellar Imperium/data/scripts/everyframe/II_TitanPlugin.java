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
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.IIModPlugin;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.hullmods.II_ElitePackage;
import data.scripts.hullmods.II_TitanBombardment;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_TitanPlugin extends BaseEveryFrameCombatPlugin {

    public static final float CR_PENALTY = 0.3f;

    private static final Color TITAN_COLOR_1 = new Color(255, 200, 150);
    private static final Color TITAN_COLOR_2 = new Color(255, 255, 175);
    private static final Color TITAN_COLOR_3 = new Color(255, 225, 75);
    private static final Color TITAN_COLOR_4 = new Color(255, 175, 50);
    private static final Color TITAN_COLOR_5 = new Color(255, 150, 0);
    private static final Color TITAN_COLOR_6 = new Color(200, 100, 0);
    private static final Color TITAN_SMOKE_COLOR = new Color(120, 140, 160, 200);
    private static final Color TITAN_COLOR_1_ELITE = new Color(200, 150, 255);
    private static final Color TITAN_COLOR_2_ELITE = new Color(255, 255, 255);
    private static final Color TITAN_COLOR_3_ELITE = new Color(255, 175, 255);
    private static final Color TITAN_COLOR_4_ELITE = new Color(225, 75, 255);
    private static final Color TITAN_COLOR_5_ELITE = new Color(175, 50, 255);
    private static final Color TITAN_COLOR_6_ELITE = new Color(150, 0, 255);
    private static final Color TITAN_COLOR_7_ELITE = new Color(100, 0, 200);

    private static final String DATA_KEY = "II_TitanPlugin";
    private static final String STAT_KEY = "ii_titan_elite_overload_debuff";
    private static final String TITANX_STAT_KEY = "ii_titanx_stat";
    private static final String TITANX_EXPLODED_KEY = "ii_titanx_exploded";
    private final Object STATUSKEY1 = new Object();

    public static final float EXPANSION_BLAST_DPS = 20000f;
    public static final float EXPANSION_RATE = 250f;
    public static final float EXPANSION_TIME = 25f;
    public static final float INITIAL_BLAST_DAMAGE = 20000f;
    public static final float INITIAL_BLAST_RADIUS = 1500f;

    public static final float EXPANSION_BLAST_DPS_TITAN_X = 20000f;
    public static final float EXPANSION_RATE_TITAN_X = 300f;
    public static final float EXPANSION_TIME_TITAN_X = 40f;
    public static final float INITIAL_BLAST_DAMAGE_TITAN_X = 30000f;
    public static final float INITIAL_BLAST_RADIUS_TITAN_X = 2500f;

    public static final float EXPANSION_BLAST_DPS_ELITE = 10000f;
    public static final float EXPANSION_BLAST_EPS_ELITE = 15000f;
    public static final float EXPANSION_BLAST_FPS_ELITE = 10000f;
    public static final float EXPANSION_RATE_ELITE = 400f;
    public static final float EXPANSION_TIME_ELITE = 10f;
    public static final float INITIAL_BLAST_DAMAGE_ELITE = 15000f;
    public static final float INITIAL_BLAST_EMP_ELITE = 15000f;
    public static final float INITIAL_BLAST_FLUX_ELITE = 40000f;
    public static final float INITIAL_BLAST_RADIUS_ELITE = 1750f;
    public static final Map<HullSize, Float> INITIAL_BLAST_EXTRA_OVERLOAD_ELITE = new HashMap<>(5); // percent
    public static final float INITIAL_BLAST_EXTRA_OVERLOAD_FALLOFF = 20f; // percent/sec

    static {
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.FIGHTER, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.FRIGATE, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.DESTROYER, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.DEFAULT, 200f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.CRUISER, 150f);
        INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.put(HullSize.CAPITAL_SHIP, 100f);
    }

    public static final float EXPANSION_BLAST_DPS_BURST = 10000f;
    public static final float EXPANSION_BLAST_EPS_BURST = 15000f;
    public static final float EXPANSION_BLAST_FPS_BURST = 5000f;
    public static final float EXPANSION_RATE_BURST = 600f;
    public static final float EXPANSION_TIME_BURST = 5f;
    public static final float INITIAL_BLAST_DAMAGE_BURST = 10000f;
    public static final float INITIAL_BLAST_EMP_BURST = 10000f;
    public static final float INITIAL_BLAST_FLUX_BURST = 20000f;
    public static final float INITIAL_BLAST_RADIUS_BURST = 1000f;
    public static final Map<HullSize, Float> INITIAL_BLAST_EXTRA_OVERLOAD_BURST = new HashMap<>(5); // percent

    static {
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.FIGHTER, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.FRIGATE, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.DESTROYER, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.DEFAULT, 100f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.CRUISER, 75f);
        INITIAL_BLAST_EXTRA_OVERLOAD_BURST.put(HullSize.CAPITAL_SHIP, 50f);
    }

    private static final Color TITANX_JITTER_COLOR = new Color(255, 204, 0, 50);

    private static final float ACCUM_INTERVAL = 0.05f;

    private static final Vector2f ZERO = new Vector2f();

    private SoundAPI sound = null;

    public static void explode(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> titanSource = localData.titanSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = titanSource.get(ship);
        if (source == null) {
            source = ship;
        }

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.TITAN));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

        engine.applyDamage(ship, loc, 200000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null, false);

        List<ShipAPI> targets = II_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS);

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
                                DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, true);
                        break;
                    }

                    if (k >= 1000) {
                        break;
                    }
                }
            }
        }

        List<CombatEntityAPI> rocks = II_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, false);
        }

        List<MissileAPI> missiles = II_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, false);
        }

        for (int i = 0; i < 300 * attenuate; i++) {
            if (i % 6 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                50),
                        ((float) Math.random() * 500f + 300f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f);
            }
            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
                    ((float) Math.random() * 400f + 200f) * attenuate, 0.35f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f))));
            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
                    ((float) Math.random() * 500f + 200f) * attenuate, 0.25f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f))));
        }

        engine.addHitParticle(loc, new Vector2f(),
                1000f * attenuate, 3f * (float) Math.sqrt(attenuate), 20f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.addHitParticle(loc, new Vector2f(),
                1500f * attenuate, 4f * (float) Math.sqrt(attenuate), 20f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.addHitParticle(loc, new Vector2f(),
                2000f * attenuate, 5f * (float) Math.sqrt(attenuate), 20f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_3, 1500f * attenuate, 7.5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_1, 2000f * attenuate, 10f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_4, 2500f * attenuate, 12.5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_5, 3500f * attenuate, 15f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_6, 4500f * attenuate, 20f * (float) Math.sqrt(attenuate));

        if (IIModPlugin.hasGraphicsLib) {
            StandardLight light = new StandardLight(loc, ZERO, ZERO, null);

            light.setIntensity(5f * attenuate);
            light.setSize(6250f * attenuate);
            light.setColor(1f, 0.8f, 0.6f);
            light.fadeOut(20f * attenuate);

            LightShader.addLight(light);
        }

        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_close", 1f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_distant", 1f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    public static void explodeTitanX(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> titanSource = localData.titanSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = titanSource.get(ship);
        if (source == null) {
            source = ship;
        }

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.TITAN_X));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

        engine.applyDamage(ship, loc, 200000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null, false);

        List<ShipAPI> targets = II_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS_TITAN_X);

        float blastDamage = INITIAL_BLAST_DAMAGE_TITAN_X * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS_TITAN_X * attenuate;
        float expansionTime = EXPANSION_TIME_TITAN_X * attenuate;
        float expansionRate = EXPANSION_RATE_TITAN_X * (attenuate / (float) Math.sqrt(attenuate));

        for (ShipAPI target : targets) {
            if (target == ship) {
                continue;
            }

            if (!target.isAlive()) {
                continue;
            }

            for (int j = 0; j < 10; j++) {
                int k = 0;
                while (true) {
                    k++;
                    Vector2f point = new Vector2f(target.getLocation());
                    point.x += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);
                    point.y += target.getCollisionRadius() * (((float) Math.random() * 2f) - 1f);

                    if (CollisionUtils.isPointWithinBounds(point, target)) {
                        engine.applyDamage(target, point,
                                0.1f * blastDamage * (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius,
                                DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, true);
                        break;
                    }

                    if (k >= 1000) {
                        break;
                    }
                }
            }
        }

        List<CombatEntityAPI> rocks = II_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, false);
        }

        List<MissileAPI> missiles = II_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.HIGH_EXPLOSIVE, 0f, true, false, source, false);
        }

        for (int i = 0; i < 600 * attenuate; i++) {
            if (i % 6 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                50),
                        ((float) Math.random() * 750f + 600f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f);
            }
            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
                    ((float) Math.random() * 600f + 400f) * attenuate, 0.35f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f))));
            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
                    ((float) Math.random() * 750f + 400f) * attenuate, 0.25f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f))));
        }

        engine.addHitParticle(loc, new Vector2f(),
                2000f * attenuate, 4f * (float) Math.sqrt(attenuate), 35f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.addHitParticle(loc, new Vector2f(),
                3000f * attenuate, 5f * (float) Math.sqrt(attenuate), 35f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.addHitParticle(loc, new Vector2f(),
                4000f * attenuate, 6f * (float) Math.sqrt(attenuate), 35f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_2);
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_3, 2000f * attenuate, 10f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_1, 2750f * attenuate, 15f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_4, 3500f * attenuate, 20f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_5, 4750f * attenuate, 25f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_6, 6000f * attenuate, 30f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_6, 7250f * attenuate, 35f * (float) Math.sqrt(attenuate));

        if (IIModPlugin.hasGraphicsLib) {
            StandardLight light = new StandardLight(loc, ZERO, ZERO, null);

            light.setIntensity(10f * attenuate);
            light.setSize(12000f * attenuate);
            light.setColor(1f, 0.8f, 0.6f);
            light.fadeOut(35f * attenuate);

            LightShader.addLight(light);
        }

        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 6000f) {
            float refDist = Math.max(2000f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_close", 0.75f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(4000f, 15000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_distant", 0.75f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    public static void explodeElite(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<ShipAPI, ShipAPI> titanSource = localData.titanSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = titanSource.get(ship);
        if (source == null) {
            source = ship;
        }

        explodingShips.put(ship.getId(), new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.ELITE));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

        engine.applyDamage(ship, loc, 200000f, DamageType.ENERGY, 0f, true, false, null, false);

        List<ShipAPI> targets = II_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS_ELITE);

        float blastDamage = INITIAL_BLAST_DAMAGE_ELITE * (float) Math.sqrt(attenuate);
        float blastEMP = INITIAL_BLAST_EMP_ELITE * (float) Math.sqrt(attenuate);
        float blastFlux = INITIAL_BLAST_FLUX_ELITE * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS_ELITE * attenuate;
        float expansionTime = EXPANSION_TIME_ELITE * attenuate;
        float expansionRate = EXPANSION_RATE_ELITE * (attenuate / (float) Math.sqrt(attenuate));

        for (ShipAPI target : targets) {
            if (target == ship) {
                continue;
            }

            float extraOverload = INITIAL_BLAST_EXTRA_OVERLOAD_ELITE.get(target.getHullSize()) * (float) Math.sqrt(attenuate);

            if ((target.isStation() || target.isStationModule()) && !target.isShipWithModules()) {
                if (target.isStationModule()) {
                    if ((target.getParentStation() != null) && !target.getParentStation().isShipWithModules()) {
                        extraOverload = 0f;
                    }
                } else {
                    extraOverload = 0f;
                }
            }

            float distAtten = (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius;
            for (int j = 0; j < 5; j++) {
                Vector2f point = MathUtils.getRandomPointInCircle(loc, blastRadius * 0.33f);
                engine.spawnEmpArcPierceShields(source, point, null, target, DamageType.ENERGY,
                        0.2f * blastDamage * distAtten, 0.2f * blastEMP * distAtten,
                        blastRadius * 3f, null, 80f * (float) Math.sqrt(attenuate) * distAtten, TITAN_COLOR_1_ELITE, TITAN_COLOR_2_ELITE);
            }

            if (target.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                target.getMutableStats().getOverloadTimeMod().modifyPercent(STAT_KEY,
                        extraOverload * distAtten * II_ElitePackage.OVERLOAD_TIME_MULT * II_ElitePackage.OVERLOAD_TIME_MULT);
            } else {
                target.getMutableStats().getOverloadTimeMod().modifyPercent(STAT_KEY, extraOverload * distAtten);
            }

            if ((target.getFluxTracker().getMaxFlux() - target.getFluxTracker().getCurrFlux()) <= (blastFlux * distAtten)) {
                if (target.getEngineController() != null) {
                    target.getEngineController().forceFlameout();
                }
            }

            target.getFluxTracker().increaseFlux(blastFlux * distAtten, true);
        }

        List<CombatEntityAPI> rocks = II_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.ENERGY, 0f, true, false, source, false);
        }

        List<MissileAPI> missiles = II_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.ENERGY, 0f, true, false, source, false);
        }

        for (int i = 0; i < 300 * attenuate; i++) {
            if (i % 6 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                50),
                        ((float) Math.random() * 500f + 300f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f);
            }
            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
                    ((float) Math.random() * 400f + 200f) * attenuate, 0.35f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f))));
            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
                    ((float) Math.random() * 500f + 200f) * attenuate, 0.25f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f))));
        }

        engine.addHitParticle(loc, new Vector2f(),
                1250f * attenuate, 3f * (float) Math.sqrt(attenuate), 10f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.addHitParticle(loc, new Vector2f(),
                1750f * attenuate, 4f * (float) Math.sqrt(attenuate), 10f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.addHitParticle(loc, new Vector2f(),
                2250f * attenuate, 5f * (float) Math.sqrt(attenuate), 10f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_4_ELITE, 1750f * attenuate, 5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_1_ELITE, 2250f * attenuate, 5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_5_ELITE, 2750f * attenuate, 7.5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_6_ELITE, 3500f * attenuate, 7.5f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_7_ELITE, 4500f * attenuate, 10f * (float) Math.sqrt(attenuate));

        if (IIModPlugin.hasGraphicsLib) {
            StandardLight light = new StandardLight(loc, ZERO, ZERO, null);

            light.setIntensity(5f * attenuate);
            light.setSize(5000f * attenuate);
            light.setColor(0.8f, 0.6f, 1f);
            light.fadeOut(10f * attenuate);

            LightShader.addLight(light);
        }

        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_close", 1f / (float) Math.pow(attenuate, 0.25), vol);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_distant", 1f / (float) Math.pow(attenuate, 0.25), vol);
        }
    }

    public static void burstTitanX(ShipAPI ship, float attenuate) {
        final LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }

        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        ShipAPI source = ship;

        explodingShips.put(ship.getId() + "_burst", new ExplosionData(ship, attenuate, ship.getLocation(), ExplosionType.BURST));
        Vector2f loc = new Vector2f(ship.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();

        List<ShipAPI> targets = II_Util.getShipsWithinRange(loc, INITIAL_BLAST_RADIUS_BURST);

        float blastDamage = INITIAL_BLAST_DAMAGE_BURST * (float) Math.sqrt(attenuate);
        float blastEMP = INITIAL_BLAST_EMP_BURST * (float) Math.sqrt(attenuate);
        float blastFlux = INITIAL_BLAST_FLUX_BURST * (float) Math.sqrt(attenuate);
        float blastRadius = INITIAL_BLAST_RADIUS_BURST * attenuate;
        float expansionTime = EXPANSION_TIME_BURST * attenuate;
        float expansionRate = EXPANSION_RATE_BURST * (attenuate / (float) Math.sqrt(attenuate));

        for (ShipAPI target : targets) {
            if (target == ship) {
                continue;
            }

            float extraOverload = INITIAL_BLAST_EXTRA_OVERLOAD_BURST.get(target.getHullSize()) * (float) Math.sqrt(attenuate);

            if ((target.isStation() || target.isStationModule()) && !target.isShipWithModules()) {
                if (target.isStationModule()) {
                    if ((target.getParentStation() != null) && !target.getParentStation().isShipWithModules()) {
                        extraOverload = 0f;
                    }
                } else {
                    extraOverload = 0f;
                }
            }

            float distAtten = (blastRadius - MathUtils.getDistance(target, loc)) / blastRadius;
            for (int j = 0; j < 5; j++) {
                Vector2f point = MathUtils.getRandomPointInCircle(loc, blastRadius * 0.33f);
                engine.spawnEmpArcPierceShields(source, point, null, target, DamageType.ENERGY,
                        0.2f * blastDamage * distAtten, 0.2f * blastEMP * distAtten,
                        blastRadius * 3f, null, 80f * (float) Math.sqrt(attenuate) * distAtten, TITAN_COLOR_1_ELITE, TITAN_COLOR_2_ELITE);
            }

            if (target.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                target.getMutableStats().getOverloadTimeMod().modifyPercent(STAT_KEY,
                        extraOverload * distAtten * II_ElitePackage.OVERLOAD_TIME_MULT * II_ElitePackage.OVERLOAD_TIME_MULT);
            } else {
                target.getMutableStats().getOverloadTimeMod().modifyPercent(STAT_KEY, extraOverload * distAtten);
            }

            if ((target.getFluxTracker().getMaxFlux() - target.getFluxTracker().getCurrFlux()) <= (blastFlux * distAtten)) {
                if (target.getEngineController() != null) {
                    target.getEngineController().forceFlameout();
                }
            }

            target.getFluxTracker().increaseFlux(blastFlux * distAtten, true);
        }

        List<CombatEntityAPI> rocks = II_Util.getAsteroidsWithinRange(loc, blastRadius);
        for (CombatEntityAPI rock : rocks) {
            engine.applyDamage(rock, rock.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(rock, loc)) / blastRadius,
                    DamageType.ENERGY, 0f, true, false, source, false);
        }

        List<MissileAPI> missiles = II_Util.getMissilesWithinRange(loc, blastRadius);
        for (MissileAPI missile : missiles) {
            engine.applyDamage(missile, missile.getLocation(),
                    0.1f * blastDamage * (blastRadius - MathUtils.getDistance(missile, loc)) / blastRadius,
                    DamageType.ENERGY, 0f, true, false, source, false);
        }

        for (int i = 0; i < 240 * attenuate; i++) {
            if (i % 6 == 0) {
                engine.spawnExplosion(loc,
                        MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.95f, 1.05f)),
                        new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 200f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                                II_Util.clamp255((int) MathUtils.getRandomNumberInRange(200f, 255f)),
                                50),
                        ((float) Math.random() * 500f + 300f) * attenuate,
                        (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f);
            }
            engine.addHitParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.9f, 1.1f)),
                    ((float) Math.random() * 400f + 200f) * attenuate, 0.35f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(100f, 175f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(25f, 100f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(175f, 255f))));
            engine.addSmoothParticle(loc, MathUtils.getRandomPointOnCircumference(new Vector2f(), expansionRate * MathUtils.getRandomNumberInRange(0.85f, 1.15f)),
                    ((float) Math.random() * 500f + 200f) * attenuate, 0.25f,
                    (float) Math.random() * expansionTime * 0.2f + expansionTime * 0.9f,
                    new Color(II_Util.clamp255((int) MathUtils.getRandomNumberInRange(50f, 150f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(0f, 50f)),
                            II_Util.clamp255((int) MathUtils.getRandomNumberInRange(150f, 255f))));
        }

        engine.addHitParticle(loc, new Vector2f(),
                1250f * attenuate, 1f * (float) Math.sqrt(attenuate), 5f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.addHitParticle(loc, new Vector2f(),
                1500f * attenuate, 2f * (float) Math.sqrt(attenuate), 5f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.addHitParticle(loc, new Vector2f(),
                1750f * attenuate, 3f * (float) Math.sqrt(attenuate), 5f * (float) Math.sqrt(attenuate),
                TITAN_COLOR_3_ELITE);
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_4_ELITE, 750f * attenuate, 3f * (float) Math.sqrt(attenuate));
        engine.spawnExplosion(loc, new Vector2f(),
                TITAN_COLOR_1_ELITE, 1500f * attenuate, 3f * (float) Math.sqrt(attenuate));

        if (IIModPlugin.hasGraphicsLib) {
            StandardLight light = new StandardLight(loc, ZERO, ZERO, null);

            light.setIntensity(2f * attenuate);
            light.setSize(3000f * attenuate);
            light.setColor(0.8f, 0.6f, 1f);
            light.fadeOut(5f * attenuate);

            LightShader.addLight(light);
        }

        float distanceToHead = MathUtils.getDistance(loc, Global.getCombatEngine().getViewport().getCenter());
        if (distanceToHead <= 3000f) {
            float refDist = Math.max(500f, 2000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_close", 1.5f / (float) Math.pow(attenuate, 0.25), vol * 0.8f);
        } else {
            float refDist = Math.max(1500f, 4000f * attenuate);
            float vol = (float) Math.sqrt(attenuate) * refDist / Math.max(refDist, distanceToHead);
            Global.getSoundPlayer().playUISound("ii_titan_explode_distant", 1.5f / (float) Math.pow(attenuate, 0.25), vol * 0.8f);
        }
    }

    private CombatEngineAPI engine;
    private boolean setEndCombatFlag = false;
    private FleetMemberAPI dummyMember = null;
    private boolean activated = false;
    private final IntervalUtil inactiveInterval = new IntervalUtil(1f, 2f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (!activated) {
            inactiveInterval.advance(amount);
            if (!inactiveInterval.intervalElapsed()) {
                return;
            }
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, ShipAPI> titanSource = localData.titanSource;
        final Map<String, ExplosionData> explodingShips = localData.explodingShips;

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        for (DamagingProjectileAPI projectile : projectiles) {
            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            if (projectile.getProjectileSpecId().contentEquals("ii_apocalypse_mirv")
                    || projectile.getProjectileSpecId().contentEquals("ii_fundae_submissile")) {
                if (projectile.getSource() != null) {
                    ShipAPI olympus = titanSource.get(projectile.getSource());
                    if (olympus != null) {
                        projectile.setSource(olympus);
                    }
                }
            }

            if (projectile.getProjectileSpecId().contentEquals("ii_titan_missile")) {
                WeaponAPI source = projectile.getWeapon();

                activated = true;

                Vector2f location = new Vector2f(projectile.getLocation());
                ShipAPI ship = projectile.getSource();
                float angle = projectile.getFacing();
                int owner = projectile.getOwner();

                boolean armor = ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE);
                boolean targeting = ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE);
                boolean elite = ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE);

                if (source != null) {
                    source.setAmmo(0);
                }

                engine.removeEntity(projectile);

                FleetMemberAPI missileMember;
                if (armor) {
                    missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_titan_armor_var");
                } else if (targeting) {
                    missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_titan_targeting_var");
                } else if (elite) {
                    missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_titan_elite_var");
                } else {
                    missileMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_titan_var");
                }
                missileMember.getRepairTracker().setCrashMothballed(false);
                missileMember.getRepairTracker().setMothballed(false);
                missileMember.getRepairTracker().setCR(ship.getCurrentCR());
                missileMember.setOwner(owner);
                missileMember.setAlly(ship.isAlly());
                missileMember.setShipName("Titan");
                boolean suppress = engine.getFleetManager(owner).isSuppressDeploymentMessages();
                engine.getFleetManager(owner).setSuppressDeploymentMessages(true);
                ShipAPI missile = engine.getFleetManager(owner).spawnFleetMember(missileMember, location, angle, 0f);
                missile.setCollisionClass(CollisionClass.FIGHTER);
                missile.getVelocity().set(ship.getVelocity());
                missile.setAngularVelocity(ship.getAngularVelocity());
                for (ShipAPI child : missile.getChildModulesCopy()) {
                    child.setCollisionClass(CollisionClass.FIGHTER);
                }
                titanSource.put(missile, ship);
                missile.setInvalidTransferCommandTarget(true);
                engine.getFleetManager(owner).setSuppressDeploymentMessages(suppress);

                for (ShipEngineAPI thruster : missile.getEngineController().getShipEngines()) {
                    Vector2f loc = thruster.getLocation();
                    engine.addSmokeParticle(loc, ship.getVelocity(), thruster.getEngineSlot().getWidth() * 4f, 1f, MathUtils.getRandomNumberInRange(2f, 4f), TITAN_SMOKE_COLOR);
                }

                if (!engine.isSimulation()) {
                    if (ship.getFleetMember() != null) {
                        ship.getFleetMember().getRepairTracker().applyCREvent(-II_TitanBombardment.getCRPenalty(ship.getVariant()), "Deployed Titan in battle");
                    }
                }
                ship.setMass(ship.getMass() - missile.getMassWithModules());

                float distanceToHead = MathUtils.getDistance(missile, Global.getCombatEngine().getViewport().getCenter());
                if (distanceToHead <= 2500f) {
                    float refDist = 1500f;
                    float vol = refDist / Math.max(refDist, distanceToHead);
                    if (armor) {
                        Global.getSoundPlayer().playUISound("ii_apocalypse_launch_close", 1f, vol);
                    } else if (targeting) {
                        Global.getSoundPlayer().playUISound("ii_apocalypse_launch_close", 1f, vol);
                    } else if (elite) {
                        Global.getSoundPlayer().playUISound("ii_titan_launch_close", 1f, vol);
                    } else {
                        Global.getSoundPlayer().playUISound("ii_titan_launch_close", 1f, vol);
                    }
                } else {
                    float refDist = 3000f;
                    float vol = refDist / Math.max(refDist, distanceToHead);
                    if (armor) {
                        Global.getSoundPlayer().playUISound("ii_apocalypse_launch_distant", 1f, vol);
                    } else if (targeting) {
                        Global.getSoundPlayer().playUISound("ii_apocalypse_launch_distant", 1f, vol);
                    } else if (elite) {
                        Global.getSoundPlayer().playUISound("ii_titan_launch_distant", 1f, vol);
                    } else {
                        Global.getSoundPlayer().playUISound("ii_titan_launch_distant", 1f, vol);
                    }
                }
                break;
            }
        }

        Iterator<Map.Entry<String, ExplosionData>> iter = explodingShips.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ExplosionData> entry = iter.next();
            ExplosionData ed = entry.getValue();
            ShipAPI ship = ed.ship;

            activated = true;

            ShipAPI source = titanSource.get(ship);
            if (source == null) {
                source = ship;
            }

            setEndCombatFlag = true;
            engine.setDoNotEndCombat(true);
            engine.getFleetManager(FleetSide.ENEMY).getTaskManager(false).setPreventFullRetreat(true);

            ed.accum += amount;
            while (ed.accum >= ACCUM_INTERVAL) {
                ed.accum -= ACCUM_INTERVAL;

                if ((ed.type == ExplosionType.TITAN) || (ed.type == ExplosionType.TITAN_X)) {
                    float expansionTime;
                    float expansionRate;
                    float expansionDPS;
                    if (ed.type == ExplosionType.TITAN) {
                        expansionTime = EXPANSION_TIME * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS * (float) Math.sqrt(ed.atten);
                    } else {
                        expansionTime = EXPANSION_TIME_TITAN_X * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE_TITAN_X * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS_TITAN_X * (float) Math.sqrt(ed.atten);
                    }

                    float distance = ed.t * expansionRate;
                    float damage = expansionDPS * ACCUM_INTERVAL * (expansionTime - ed.t * 0.75f) / expansionTime;

                    if (ed.t <= 0.1f) {
                        engine.applyDamage(ship, ship.getLocation(), 200000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null, false);
                    }

                    List<ShipAPI> targets = II_Util.getShipsWithinRange(ed.loc, distance);

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

                    II_Util.filterObscuredTargets(null, ed.loc, targets, false, true, false);

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
                } else if ((ed.type == ExplosionType.ELITE) || (ed.type == ExplosionType.BURST)) {
                    float expansionTime;
                    float expansionRate;
                    float expansionDPS;
                    float expansionEPS;
                    float expansionFPS;

                    if (ed.type == ExplosionType.ELITE) {
                        expansionTime = EXPANSION_TIME_ELITE * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE_ELITE * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS_ELITE * (float) Math.sqrt(ed.atten);
                        expansionEPS = EXPANSION_BLAST_EPS_ELITE * (float) Math.sqrt(ed.atten);
                        expansionFPS = EXPANSION_BLAST_FPS_ELITE * (float) Math.sqrt(ed.atten);
                    } else {
                        expansionTime = EXPANSION_TIME_BURST * (float) Math.sqrt(ed.atten);
                        expansionRate = EXPANSION_RATE_BURST * (ed.atten / (float) Math.sqrt(ed.atten));
                        expansionDPS = EXPANSION_BLAST_DPS_BURST * (float) Math.sqrt(ed.atten);
                        expansionEPS = EXPANSION_BLAST_EPS_BURST * (float) Math.sqrt(ed.atten);
                        expansionFPS = EXPANSION_BLAST_FPS_BURST * (float) Math.sqrt(ed.atten);
                    }

                    float distance = ed.t * expansionRate;
                    float distAtten = (expansionTime - ed.t * 0.75f) / expansionTime;
                    float damage = expansionDPS * ACCUM_INTERVAL * distAtten;
                    float emp = expansionEPS * ACCUM_INTERVAL * distAtten;
                    float flux = expansionFPS * ACCUM_INTERVAL * distAtten;

                    if ((ed.t <= 0.1f) && (ed.type != ExplosionType.BURST)) {
                        engine.applyDamage(ship, ship.getLocation(), 200000f, DamageType.ENERGY, 0f, true, false, null, false);
                    }

                    int numArcs = MathUtils.getRandomNumberInRange(1, 5);
                    for (int i = 0; i < numArcs; i++) {
                        float angleDelta = II_Util.lerp(90f, 30f, ed.t / expansionTime);
                        float angle1 = MathUtils.getRandomNumberInRange(0f, 360f);
                        float angle2 = MathUtils.clampAngle(angle1 + MathUtils.getRandomNumberInRange(-angleDelta * 0.5f, angleDelta * 0.5f));
                        Vector2f loc1 = MathUtils.getPointOnCircumference(ed.loc, distance, angle1);
                        Vector2f loc2 = MathUtils.getPointOnCircumference(ed.loc, distance, angle2);
                        CombatEntityAPI entity = new SimpleEntity(loc2);
                        Color coreColor = new Color(TITAN_COLOR_6_ELITE.getRed(), TITAN_COLOR_6_ELITE.getGreen(), TITAN_COLOR_6_ELITE.getBlue(),
                                II_Util.clamp255(Math.round(TITAN_COLOR_6_ELITE.getAlpha() * distAtten)));
                        Color fringeColor = new Color(TITAN_COLOR_7_ELITE.getRed(), TITAN_COLOR_7_ELITE.getGreen(), TITAN_COLOR_7_ELITE.getBlue(),
                                II_Util.clamp255(Math.round(TITAN_COLOR_7_ELITE.getAlpha() * distAtten)));
                        engine.spawnEmpArc(source, loc1, entity, entity, DamageType.FRAGMENTATION, 0f, 0f, distance, null,
                                150f * (float) Math.sqrt(ed.atten) * (float) Math.sqrt(distAtten), fringeColor, coreColor);
                    }

                    List<ShipAPI> targets = II_Util.getShipsWithinRange(ed.loc, distance);

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

                    II_Util.filterObscuredTargets(null, ed.loc, targets, false, true, false);

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
                                if (target.isAlive()) {
                                    engine.spawnEmpArc(source, damageLoc, target, target, DamageType.FRAGMENTATION, damage, emp,
                                            distance, "ii_titan_elite_emp", 40f * (float) Math.sqrt(ed.atten) * distAtten, TITAN_COLOR_5_ELITE, TITAN_COLOR_4_ELITE);
                                    target.getFluxTracker().increaseFlux(flux, true);
                                } else {
                                    engine.applyDamage(target, damageLoc, damage, DamageType.FRAGMENTATION, emp, false, true, source, true);
                                }
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
            StatMod stat = ship.getMutableStats().getOverloadTimeMod().getPercentBonus(STAT_KEY);
            if (stat != null) {
                float newMod = stat.getValue() - (INITIAL_BLAST_EXTRA_OVERLOAD_FALLOFF * amount);
                if (newMod <= 0f) {
                    ship.getMutableStats().getOverloadTimeMod().unmodify(STAT_KEY);
                } else {
                    ship.getMutableStats().getOverloadTimeMod().modifyPercent(STAT_KEY, newMod);
                    if (Global.getCombatEngine().getPlayerShip() == ship) {
                        Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, Global.getSettings().getSpriteName("ui", "icon_tactical_overloaded"),
                                "Titan EMP", "overload duration +" + Math.round(newMod) + "%", true);
                    }
                }
            }

            if (!ship.isAlive()) {
                switch (II_Util.getNonDHullId(ship.getHullSpec())) {
                    case "ii_olympus":
                        List<WeaponAPI> weapons = ship.getAllWeapons();
                        for (WeaponAPI weapon : weapons) {
                            if (weapon.getId().contentEquals("ii_titan_w")) {
                                if (weapon.getAmmo() > 0) {
                                    ship.setMass(ship.getMass() - 800f);
                                    weapon.setAmmo(0);
                                }
                                break;
                            }
                        }
                        activated = true;
                        break;
                    case "ii_boss_titanx":
                        float effectLevel = ship.getMutableStats().getDynamic().getValue(TITANX_STAT_KEY, 0f);
                        if (effectLevel > 0f) {
                            if (effectLevel <= 1f) {
                                if (dummyMember == null) {
                                    dummyMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_dummy_var");
                                    dummyMember.setOwner(1);
                                }
                                engine.getFleetManager(FleetSide.ENEMY).addToReserves(dummyMember);
                                ship.getMutableStats().getDynamic().getMod(TITANX_EXPLODED_KEY).modifyFlat(TITANX_EXPLODED_KEY, 1f);
                                II_TitanPlugin.explodeTitanX(ship, effectLevel);
                            }
                            ship.getMutableStats().getDynamic().getMod(TITANX_STAT_KEY).modifyFlat(TITANX_STAT_KEY, 0f);
                            if (sound != null) {
                                sound.stop();
                            }
                        } else {
                            float exploded = ship.getMutableStats().getDynamic().getValue(TITANX_EXPLODED_KEY, 0f);
                            if (exploded == 0f) {
                                setEndCombatFlag = true;
                            }
                        }
                        activated = true;
                        break;
                    case "ii_titan":
                    case "ii_titan_armor":
                    case "ii_titan_targeting":
                    case "ii_titan_elite":
                    case "ii_titan_armor_door":
                    case "ii_titan_targeting_door":
                        ship.setCollisionClass(CollisionClass.SHIP);
                        activated = true;
                        break;
                    default:
                        break;
                }
            } else if (II_Util.getNonDHullId(ship.getHullSpec()).contentEquals("ii_olympus")) {
                activated = true;

                if (ship.getCurrentCR() < II_TitanBombardment.getCRPenalty(ship.getVariant())) {
                    List<WeaponAPI> weapons = ship.getAllWeapons();
                    for (WeaponAPI weapon : weapons) {
                        if (weapon.getId().contentEquals("ii_titan_w")) {
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
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        range = 2500f;
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                        range = 3500f;
                    }

                    List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship, range);
                    if (!enemies.isEmpty()) {
                        shouldFire = true;
                    }

                    if (shouldFire) {
                        WeaponGroupAPI titanGroup = null;
                        for (WeaponAPI weapon : ship.getUsableWeapons()) {
                            if (weapon.getId().contentEquals("ii_titan_w")) {
                                if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled() && (weapon.getAmmo() > 0)) {
                                    titanGroup = ship.getWeaponGroupFor(weapon);
                                    break;
                                }
                            }
                        }

                        if (titanGroup != null) {
                            int groupNum = 0;
                            boolean foundGroup = false;
                            for (WeaponGroupAPI group : ship.getWeaponGroupsCopy()) {
                                if (group == titanGroup) {
                                    foundGroup = true;
                                    break;
                                } else {
                                    groupNum++;
                                }
                            }
                            if (foundGroup) {
                                if (ship.getSelectedGroupAPI() != titanGroup) {
                                    ship.giveCommand(ShipCommand.SELECT_GROUP, null, groupNum);
                                }
                                if (ship.getSelectedGroupAPI() == titanGroup) {
                                    ship.giveCommand(ShipCommand.FIRE, ship.getMouseTarget(), groupNum);
                                }
                            }
                        }
                    }
                }
            } else if (II_Util.getNonDHullId(ship.getHullSpec()).contentEquals("ii_boss_titanx")) {
                activated = true;

                if (ship.getHullLevel() <= 1f / 3f) {
                    float defenseLevel = II_Util.lerp(2f, 4f, 1f - (ship.getHullLevel() * 3f));
                    ship.getMutableStats().getHullCombatRepairRatePercentPerSecond().modifyMult(TITANX_STAT_KEY, 0f);
                    ship.getMutableStats().getEmpDamageTakenMult().modifyMult(TITANX_STAT_KEY, 1f / defenseLevel);
                    ship.getMutableStats().getKineticDamageTakenMult().modifyMult(TITANX_STAT_KEY, 1f / defenseLevel);
                    ship.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult(TITANX_STAT_KEY, 1f / defenseLevel);
                    ship.getMutableStats().getEnergyDamageTakenMult().modifyMult(TITANX_STAT_KEY, 1f / defenseLevel);
                    ship.getMutableStats().getFragmentationDamageTakenMult().modifyMult(TITANX_STAT_KEY, 1f / defenseLevel);

                    float effectLevel = ship.getMutableStats().getDynamic().getValue(TITANX_STAT_KEY, 0f);
                    if (effectLevel < 2f) {
                        if (effectLevel == 0f) {
                            float distanceToHead = MathUtils.getDistance(ship, Global.getCombatEngine().getViewport().getCenter());
                            float refDist = 2500f;
                            float vol = refDist / Math.max(refDist, distanceToHead);
                            sound = Global.getSoundPlayer().playUISound("ii_titan_explode_charge", 0.5f, vol * 2f);

                            ship.getSystem().deactivate();
                            ship.setShipSystemDisabled(true);
                        }

                        effectLevel += amount / 10f;
                        ship.setJitter(ship, TITANX_JITTER_COLOR, effectLevel, 10 + Math.round(effectLevel * 15f), effectLevel * 10f, 20f + (effectLevel * 40f));

                        if (effectLevel >= 1f) {
                            if (dummyMember == null) {
                                dummyMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "ii_dummy_var");
                                dummyMember.setOwner(1);
                            }
                            engine.getFleetManager(FleetSide.ENEMY).addToReserves(dummyMember);
                            ship.getMutableStats().getDynamic().getMod(TITANX_EXPLODED_KEY).modifyFlat(TITANX_EXPLODED_KEY, 1f);
                            II_TitanPlugin.explodeTitanX(ship, effectLevel);
                            effectLevel = 3f;
                        }
                        ship.getMutableStats().getDynamic().getMod(TITANX_STAT_KEY).modifyFlat(TITANX_STAT_KEY, effectLevel);
                    }

                    engine.setDoNotEndCombat(true);
                    engine.getFleetManager(FleetSide.ENEMY).getTaskManager(false).setPreventFullRetreat(true);
                }
            } else {
                boolean nuke = false;
                boolean titan = false;
                switch (II_Util.getNonDHullId(ship.getHullSpec())) {
                    case "ii_titan":
                    case "ii_titan_elite":
                        nuke = true;
                        titan = true;
                        break;
                    case "ii_titan_armor":
                    case "ii_titan_targeting":
                        titan = true;
                        break;
                    default:
                        break;
                }

                if (nuke || titan) {
                    activated = true;
                }

                if (titan && (ship.getOwner() == 0)) {
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
                    if (II_Util.getNonDHullId(ship.getHullSpec()).contentEquals("ii_titan")) {
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
        TITAN,
        TITAN_X,
        ELITE,
        BURST
    }

    private static final class LocalData {

        final Map<ShipAPI, ShipAPI> titanSource = new LinkedHashMap<>(10);
        final Map<String, ExplosionData> explodingShips = new LinkedHashMap<>(10);
    }
}
