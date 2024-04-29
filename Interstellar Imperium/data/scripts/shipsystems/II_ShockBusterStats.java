package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Multi;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_ShockBusterStats extends BaseShipSystemScript {

    public static final float REGEN_OVERRIDE = 1f / 12f;
    public static final int USES_OVERRIDE = 3;
    public static final int MAX_BOLTS = 10;
    public static final int MIN_BOLTS = 3;
    public static final float MAX_RANGE = 1000f;
    public static final float FALLOFF_RANGE = 3000f;
    public static final float RADIUS_PER_BOLT = 25f;
    public static final float FORCE_PER_BOLT = 400f;
    public static final float DAMAGE_PER_BOLT = 150f;
    public static final float EMP_PER_BOLT = 200f;
    public static final float BLAST_AREA_RADIUS_SCALE = 1.6f;
    public static final float BLAST_AREA_FLAT = 350f;
    public static final float BLAST_MISSILE_DAMAGE = 200f;
    public static final float BLAST_MAX_DAMAGE = BLAST_MISSILE_DAMAGE * 5f;
    public static final float BLAST_MISSILE_EMP = 200f;
    public static final int BLAST_MAX_MISSILE_TARGETS = 10;
    public static final float ARMOR_IN_OVERRIDE = 0.75f;
    public static final float ARMOR_OUT_OVERRIDE = 0.75f;
    public static final int ARMOR_USES_OVERRIDE = 2;
    public static final float ARMOR_FLUX_MULT = 2f;
    public static final float ARMOR_COOLDOWN_MULT = 1.5f;
    public static final int ARMOR_MAX_BOLTS = 5;
    public static final int ARMOR_MIN_BOLTS = 2;
    public static final float ARMOR_MAX_RANGE = 600f;
    public static final float ARMOR_FALLOFF_RANGE = 1800f;
    public static final float ARMOR_RADIUS_PER_BOLT = 50f;
    public static final float ARMOR_FORCE_PER_BOLT = 200f;
    public static final float ARMOR_DAMAGE_PER_BOLT = 100f;
    public static final float ARMOR_EMP_PER_BOLT = 150f;
    public static final float ARMOR_BLAST_AREA_RADIUS_SCALE = 2.5f;
    public static final float ARMOR_BLAST_AREA_FLAT = 400f;
    public static final float ARMOR_BLAST_MAX_DAMAGE = BLAST_MISSILE_DAMAGE * 10f;
    public static final int ARMOR_BLAST_MAX_MISSILE_TARGETS = 20;
    public static final float TARGETING_IN_OVERRIDE = 1f;
    public static final float TARGETING_OUT_OVERRIDE = 0.5f;
    public static final int TARGETING_USES_OVERRIDE = 2;
    public static final float TARGETING_FLUX_MULT = 1.75f;
    public static final float TARGETING_COOLDOWN_MULT = 2f;
    public static final int TARGETING_MAX_BOLTS = 15;
    public static final int TARGETING_MIN_BOLTS = 5;
    public static final float TARGETING_MAX_RANGE = 1500f;
    public static final float TARGETING_FALLOFF_RANGE = 3000f;
    public static final float TARGETING_RADIUS_PER_BOLT = 20f;
    public static final float TARGETING_FORCE_PER_BOLT = 450f;
    public static final float TARGETING_DAMAGE_PER_BOLT = 200f;
    public static final float TARGETING_EMP_PER_BOLT = 300f;
    public static final float TARGETING_BLAST_MISSILE_DAMAGE = 400f;
    public static final float TARGETING_BLAST_MAX_DAMAGE = TARGETING_BLAST_MISSILE_DAMAGE * 5f;
    public static final float TARGETING_BLAST_MISSILE_EMP = 400f;
    public static final float ELITE_IN_OVERRIDE = 0.25f;
    public static final float ELITE_OUT_OVERRIDE = 0.25f;
    public static final float ELITE_REGEN_OVERRIDE = -1;
    public static final int ELITE_USES_OVERRIDE = -1;
    public static final float ELITE_FLUX_MULT = 0.5f;
    public static final float ELITE_COOLDOWN_MULT = 0.5f;
    public static final int ELITE_MAX_BOLTS = 8;
    public static final int ELITE_MIN_BOLTS = 3;
    public static final float ELITE_MAX_RANGE = 600f;
    public static final float ELITE_FALLOFF_RANGE = 1200f;
    public static final float ELITE_RADIUS_PER_BOLT = 35f;
    public static final float ELITE_FORCE_PER_BOLT = 300f;
    public static final float ELITE_DAMAGE_PER_BOLT = 100f;
    public static final float ELITE_EMP_PER_BOLT = 125f;
    public static final float ELITE_BLAST_MISSILE_DAMAGE = 100f;
    public static final float ELITE_BLAST_MAX_DAMAGE = ELITE_BLAST_MISSILE_DAMAGE * 5f;
    public static final float ELITE_BLAST_MISSILE_EMP = 100f;
    public static final float STATION_MAX_RANGE = 2000f;
    public static final float STATION_FALLOFF_RANGE = 5000f;
    public static final float STATION_FORCE_PER_BOLT = 500f;
    public static final float STATION_DAMAGE_PER_BOLT = 200f;
    public static final float STATION_EMP_PER_BOLT = 250f;
    public static final float STATION_BLAST_MISSILE_DAMAGE = 300f;
    public static final float STATION_BLAST_MAX_DAMAGE = STATION_BLAST_MISSILE_DAMAGE * 5f;
    public static final float STATION_BLAST_MISSILE_EMP = 300f;
    public static final float STATION_ARC = 240f;

    private static final Color EMP_CORE_COLOR_STANDARD = new Color(150, 225, 255, 255);
    private static final Color EMP_FRINGE_COLOR_STANDARD = new Color(100, 200, 255, 200);
    private static final Color JITTER_UNDER_COLOR_STANDARD = new Color(150, 200, 255, 100);
    private static final Color EMP_CORE_COLOR_ARMOR = new Color(255, 255, 150, 255);
    private static final Color EMP_FRINGE_COLOR_ARMOR = new Color(255, 200, 100, 200);
    private static final Color JITTER_UNDER_COLOR_ARMOR = new Color(255, 235, 150, 100);
    private static final Color EMP_CORE_COLOR_TARGETING = new Color(205, 250, 255, 255);
    private static final Color EMP_FRINGE_COLOR_TARGETING = new Color(50, 125, 255, 255);
    private static final Color JITTER_UNDER_COLOR_TARGETING = new Color(150, 220, 255, 125);
    private static final Color EMP_CORE_COLOR_ELITE = new Color(255, 150, 255, 255);
    private static final Color EMP_FRINGE_COLOR_ELITE = new Color(165, 100, 255, 200);
    private static final Color JITTER_UNDER_COLOR_ELITE = new Color(220, 150, 255, 100);

    private static final FindShipFilter FILTER = new FindShipFilter() {
        @Override
        public boolean matches(ShipAPI ship) {
            return isTargetValid(null, ship);
        }
    };

    private final IntervalUtil interval = new IntervalUtil(0.25f, 0.33f);
    private boolean fired = false;

    public static boolean isTargetValid(ShipAPI ship, ShipAPI target) {
        if (target == null) {
            return false;
        }
        if (target.isPhased() || (target.getCollisionClass() == CollisionClass.NONE)) {
            return false;
        }
        if (ship != null) {
            if ((target.getOwner() == ship.getOwner()) && target.isAlive()) {
                return false;
            }
            if (ship.getVariant().hasHullMod("supercomputer")) {
                if (!Misc.isInArc(ship.getFacing(), STATION_ARC, ship.getLocation(), target.getLocation())) {
                    return false;
                }
            }
        }
        return !target.getVariant().hasHullMod(HullMods.VASTBULK);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Color JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_STANDARD;
        float jitterScale = 1f;
        float pitchScale = 1f;
        float soundVol = 0.75f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_ARMOR;
            jitterScale = 1.3f;
            pitchScale = 0.9f;
            soundVol = 1f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_TARGETING;
            jitterScale = 1.5f;
            pitchScale = 0.8f;
            soundVol = 1.1f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            JITTER_UNDER_COLOR = JITTER_UNDER_COLOR_ELITE;
            jitterScale = 0.8f;
            pitchScale = 1.2f;
            soundVol = 0.6f;
        } else if (ship.getVariant().hasHullMod("supercomputer")) {
            jitterScale = 1.2f;
            soundVol = 0.9f;
        }

        float jitterLevel = effectLevel;
        if (state == State.OUT) {
            jitterLevel *= jitterLevel;
        }
        float maxRangeBonus = 30f * jitterScale;
        float jitterRangeBonus = ((0.5f + jitterLevel) / 1.5f) * maxRangeBonus;

        Color jitterUnderColor = new Color(JITTER_UNDER_COLOR.getRed(), JITTER_UNDER_COLOR.getGreen(), JITTER_UNDER_COLOR.getBlue(),
                II_Util.clamp255(Math.round(jitterLevel * JITTER_UNDER_COLOR.getAlpha())));
        ship.setJitterUnder(this, jitterUnderColor, 1f, Math.round(20 * jitterScale), 0f, 3f + jitterRangeBonus);

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            String targetsKey = ship.getId() + "_buster_targets";
            Object foundTargets = Global.getCombatEngine().getCustomData().get(targetsKey);
            if (state == State.IN) {
                interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if (interval.intervalElapsed()) {
                    Global.getSoundPlayer().playSound("ii_active_armor_spark", MathUtils.getRandomNumberInRange(0.95f, 1.05f) * pitchScale,
                            soundVol, ship.getLocation(), ship.getVelocity());
                }
                if (foundTargets == null) {
                    List<ShipAPI> targets = findTargets(ship);
                    if (targets != null) {
                        Global.getCombatEngine().getCustomData().put(targetsKey, targets);
                    }
                }
                fired = false;
            } else if ((state == State.ACTIVE) && !fired) {
                if (foundTargets instanceof List) {
                    List<ShipAPI> targets = (List<ShipAPI>) foundTargets;
                    for (ShipAPI target : targets) {
                        applyEffectToTarget(ship, target);
                    }
                }
                zapNearbyMissiles(ship);
                fired = true;
            } else if ((state == State.OUT) && (foundTargets != null)) {
                Global.getCombatEngine().getCustomData().remove(targetsKey);
            }
        } else {
            String targetKey = ship.getId() + "_buster_target";
            Object foundTarget = Global.getCombatEngine().getCustomData().get(targetKey);
            if (state == State.IN) {
                interval.advance(Global.getCombatEngine().getElapsedInLastFrame());
                if (interval.intervalElapsed()) {
                    Global.getSoundPlayer().playSound("ii_active_armor_spark", MathUtils.getRandomNumberInRange(0.95f, 1.05f) * pitchScale,
                            soundVol, ship.getLocation(), ship.getVelocity());
                }
                if (foundTarget == null) {
                    ShipAPI target = findTarget(ship, null);
                    if (target != null) {
                        Global.getCombatEngine().getCustomData().put(targetKey, target);
                    }
                }
                fired = false;
            } else if ((state == State.ACTIVE) && !fired) {
                ShipAPI target = findTarget(ship, (ShipAPI) foundTarget);
                if (target != null) {
                    applyEffectToTarget(ship, target);
                }
                zapNearbyMissiles(ship);
                fired = true;
            } else if ((state == State.OUT) && (foundTarget != null)) {
                Global.getCombatEngine().getCustomData().remove(targetKey);
            }
        }
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != SystemState.IDLE) {
            return null;
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            List<ShipAPI> targets = findTargets(ship);
            if ((targets != null) && !targets.isEmpty()) {
                return "READY";
            }
        } else {
            ShipAPI target = findTarget(ship, null);
            if ((target != null) && (target != ship)) {
                return "READY";
            }
            if (((target == null) || (target == ship)) && (ship.getShipTarget() != null)) {
                if (isUsable(system, ship)) {
                    return "READY (OUT OF RANGE)";
                }
            }
        }

        if (isUsable(system, ship)) {
            return "READY (NO TARGET)";
        } else {
            return "NO TARGET";
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship == null) {
            return false;
        }

        if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * ARMOR_FLUX_MULT);
            system.setCooldown(1f * ARMOR_COOLDOWN_MULT);
        } else if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * TARGETING_FLUX_MULT);
            system.setCooldown(1f * TARGETING_COOLDOWN_MULT);
        } else if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * ELITE_FLUX_MULT);
            system.setCooldown(1f * ELITE_COOLDOWN_MULT);
        }

        boolean hasTarget;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            List<ShipAPI> targets = findTargets(ship);
            hasTarget = (targets != null) && !targets.isEmpty();
        } else {
            ShipAPI target = findTarget(ship, null);
            hasTarget = (target != null) && (target != ship);
        }

        if (!hasTarget) {
            float shipRadius = II_Util.effectiveRadius(ship);
            float blastArea = shipRadius * BLAST_AREA_RADIUS_SCALE + BLAST_AREA_FLAT;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                blastArea = shipRadius * ARMOR_BLAST_AREA_RADIUS_SCALE + ARMOR_BLAST_AREA_FLAT;
            }

            blastArea = ship.getMutableStats().getSystemRangeBonus().computeEffective(blastArea);

            List<MissileAPI> allMissiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), blastArea);
            II_Util.filterObscuredTargets(null, ship.getLocation(), allMissiles, false, true, false);
            for (MissileAPI missile : allMissiles) {
                if (missile.getOwner() != ship.getOwner()) {
                    hasTarget = true;
                    break;
                }
            }
        }

        return hasTarget;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    protected void zapNearbyMissiles(final ShipAPI ship) {
        float shipRadius = II_Util.effectiveRadius(ship);

        Color EMP_CORE_COLOR = EMP_CORE_COLOR_STANDARD;
        Color EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_STANDARD;
        float blastDamage = BLAST_MISSILE_DAMAGE;
        float blastEMP = BLAST_MISSILE_EMP;
        float blastArea = shipRadius * BLAST_AREA_RADIUS_SCALE + BLAST_AREA_FLAT;
        float blastMaxDamage = BLAST_MAX_DAMAGE;
        int blastMaxTargets = BLAST_MAX_MISSILE_TARGETS;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_ARMOR;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_ARMOR;
            blastArea = shipRadius * ARMOR_BLAST_AREA_RADIUS_SCALE + ARMOR_BLAST_AREA_FLAT;
            blastMaxDamage = ARMOR_BLAST_MAX_DAMAGE;
            blastMaxTargets = ARMOR_BLAST_MAX_MISSILE_TARGETS;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_TARGETING;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_TARGETING;
            blastDamage = TARGETING_BLAST_MISSILE_DAMAGE;
            blastEMP = TARGETING_BLAST_MISSILE_EMP;
            blastMaxDamage = TARGETING_BLAST_MAX_DAMAGE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_ELITE;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_ELITE;
            blastDamage = ELITE_BLAST_MISSILE_DAMAGE;
            blastEMP = ELITE_BLAST_MISSILE_EMP;
            blastMaxDamage = ELITE_BLAST_MAX_DAMAGE;
        } else if (ship.getVariant().hasHullMod("supercomputer")) {
            blastDamage = STATION_BLAST_MISSILE_DAMAGE;
            blastEMP = STATION_BLAST_MISSILE_EMP;
            blastMaxDamage = STATION_BLAST_MAX_DAMAGE;
        }

        blastArea = ship.getMutableStats().getSystemRangeBonus().computeEffective(blastArea);

        float totalDamage = 0f;
        int missileTargets = 0;
        List<MissileAPI> allMissiles = CombatUtils.getMissilesWithinRange(ship.getLocation(), blastArea);

        II_Util.filterObscuredTargets(null, ship.getLocation(), allMissiles, false, true, false);

        Collections.shuffle(allMissiles);
        for (MissileAPI missile : allMissiles) {
            if (missile.getOwner() != ship.getOwner()) {
                float contribution = 0.1f;
                float falloff = 1f - MathUtils.getDistance(ship, missile) / blastArea;
                if (missile.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                missileTargets++;
                totalDamage += Math.min(missile.getHitpoints(), blastDamage * falloff) * contribution;
                if (missileTargets >= blastMaxTargets) {
                    break;
                }
                if (totalDamage >= blastMaxDamage) {
                    break;
                }
            }
        }

        float attenuation = 1f;
        if (totalDamage > blastMaxDamage) {
            attenuation *= blastMaxDamage / totalDamage;
        }

        totalDamage = 0f;
        missileTargets = 0;
        for (MissileAPI missile : allMissiles) {
            if (missile.getOwner() != ship.getOwner()) {
                float falloff = 1f - MathUtils.getDistance(ship, missile) / blastArea;
                if (missile.getCollisionClass() == CollisionClass.NONE) {
                    continue;
                }

                missileTargets++;
                MissileAPI empTarget = missile;
                Vector2f point = null;
                int max = 10;
                while (point == null && max > 0) {
                    point = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                    if (!CollisionUtils.isPointWithinBounds(point, ship)) {
                        point = null;
                    }
                    max--;
                }
                if (point == null) {
                    point = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                }
                Global.getCombatEngine().spawnEmpArc(ship, point, ship, empTarget, DamageType.ENERGY,
                        blastDamage * falloff * attenuation, blastEMP * falloff * attenuation,
                        10000f, null, (float) Math.sqrt(blastDamage * falloff * attenuation), EMP_FRINGE_COLOR, EMP_CORE_COLOR);
                if (missileTargets >= blastMaxTargets) {
                    break;
                }
                if (totalDamage >= blastMaxDamage) {
                    break;
                }
            }
        }
    }

    protected void applyEffectToTarget(final ShipAPI ship, final ShipAPI target) {
        if ((target == ship) || !isTargetValid(ship, target)) {
            return;
        }

        float shipRadius = II_Util.effectiveRadius(ship);
        float targetRadius = II_Util.effectiveRadius(target);

        Color EMP_CORE_COLOR = EMP_CORE_COLOR_STANDARD;
        Color EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_STANDARD;
        int minBolts = MIN_BOLTS;
        int maxBolts = MAX_BOLTS;
        float falloffRange = FALLOFF_RANGE;
        float radiusPerBolt = RADIUS_PER_BOLT;
        float forcePerBolt = FORCE_PER_BOLT;
        float damagePerBolt = DAMAGE_PER_BOLT;
        float empPerBolt = EMP_PER_BOLT;
        int retries = 100;
        float empScale = 1f;
        int particlesPerBolt = 10;
        float particleScale = 1f;
        float volumeScale = 1f;
        float pitch = 1f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_ARMOR;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_ARMOR;
            minBolts = ARMOR_MIN_BOLTS;
            maxBolts = ARMOR_MAX_BOLTS;
            falloffRange = ARMOR_FALLOFF_RANGE;
            radiusPerBolt = ARMOR_RADIUS_PER_BOLT;
            forcePerBolt = ARMOR_FORCE_PER_BOLT;
            damagePerBolt = ARMOR_DAMAGE_PER_BOLT;
            empPerBolt = ARMOR_EMP_PER_BOLT;
            retries = 10;
            empScale = 0.75f;
            particlesPerBolt = 3;
            particleScale = 0.75f;
            pitch = 0.8f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_TARGETING;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_TARGETING;
            minBolts = TARGETING_MIN_BOLTS;
            maxBolts = TARGETING_MAX_BOLTS;
            falloffRange = TARGETING_FALLOFF_RANGE;
            radiusPerBolt = TARGETING_RADIUS_PER_BOLT;
            forcePerBolt = TARGETING_FORCE_PER_BOLT;
            damagePerBolt = TARGETING_DAMAGE_PER_BOLT;
            empPerBolt = TARGETING_EMP_PER_BOLT;
            empScale = 1.25f;
            particlesPerBolt = 12;
            particleScale = 1.2f;
            volumeScale = 1.25f;
            pitch = 0.9f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            EMP_CORE_COLOR = EMP_CORE_COLOR_ELITE;
            EMP_FRINGE_COLOR = EMP_FRINGE_COLOR_ELITE;
            minBolts = ELITE_MIN_BOLTS;
            maxBolts = ELITE_MAX_BOLTS;
            falloffRange = ELITE_FALLOFF_RANGE;
            radiusPerBolt = ELITE_RADIUS_PER_BOLT;
            forcePerBolt = ELITE_FORCE_PER_BOLT;
            damagePerBolt = ELITE_DAMAGE_PER_BOLT;
            empPerBolt = ELITE_EMP_PER_BOLT;
            empScale = 0.75f;
            particlesPerBolt = 8;
            particleScale = 0.75f;
            volumeScale = 0.75f;
            pitch = 1.1f;
        } else if (ship.getVariant().hasHullMod("supercomputer")) {
            falloffRange = STATION_FALLOFF_RANGE;
            forcePerBolt = STATION_FORCE_PER_BOLT;
            damagePerBolt = STATION_DAMAGE_PER_BOLT;
            empPerBolt = STATION_EMP_PER_BOLT;
            empScale = 1.15f;
            particlesPerBolt = 11;
            particleScale = 1.1f;
            volumeScale = 1.1f;
        }

        falloffRange = ship.getMutableStats().getSystemRangeBonus().computeEffective(falloffRange);

        float falloff = 1f - MathUtils.getDistance(ship, target.getLocation()) / falloffRange;
        int bolts = Math.max(minBolts, Math.min(maxBolts, Math.round(targetRadius / radiusPerBolt)));

        ShipAPI empTarget = target;
        for (int i = 0; i <= bolts; i++) {
            Vector2f point = null;
            int max = retries;
            while (point == null && max > 0) {
                point = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
                if (!CollisionUtils.isPointWithinBounds(point, ship)) {
                    point = null;
                }
                max--;
            }
            if (point == null) {
                point = MathUtils.getRandomPointInCircle(ship.getLocation(), shipRadius);
            }
            Global.getCombatEngine().spawnEmpArc(ship, point, ship, empTarget, DamageType.ENERGY,
                    damagePerBolt * falloff, empPerBolt * falloff, 10000f, null, 20f * empScale * falloff, EMP_FRINGE_COLOR, EMP_CORE_COLOR);
            for (int x = 0; x < particlesPerBolt; x++) {
                Global.getCombatEngine().addHitParticle(
                        MathUtils.getPointOnCircumference(target.getLocation(),
                                MathUtils.getRandomNumberInRange(0f, targetRadius * 0.5f) * particleScale,
                                MathUtils.getRandomNumberInRange(0f, 360f)),
                        MathUtils.getPointOnCircumference(null,
                                MathUtils.getRandomNumberInRange(100f, 400f) * particleScale,
                                MathUtils.getRandomNumberInRange(0f, 360f)),
                        7f * particleScale, 1f * falloff, MathUtils.getRandomNumberInRange(1f, 2f) * particleScale * falloff, EMP_CORE_COLOR);
            }
        }

        float volume = 1.5f * ((float) bolts / (float) maxBolts) * volumeScale * falloff;
        Vector2f midpoint = MathUtils.getMidpoint(ship.getLocation(), target.getLocation());
        Global.getSoundPlayer().playSound("ii_shock_buster_impact", pitch, volume, midpoint, target.getVelocity());

        /* Approximately three times the average turn rate for a ship class */
        float pushForceRatio;
        float maxAngVel;
        switch (II_Multi.getRoot(target).getHullSize()) {
            case FIGHTER:
                maxAngVel = 270f;
                if (ship.isPhased()) {
                    pushForceRatio = 0.4f;
                } else {
                    pushForceRatio = 1f;
                }
                break;
            case FRIGATE:
                maxAngVel = 210f;
                if (ship.isPhased()) {
                    pushForceRatio = 0.325f;
                } else {
                    pushForceRatio = 0.9f;
                }
                break;
            default:
            case DESTROYER:
                maxAngVel = 75f;
                if (ship.isPhased()) {
                    pushForceRatio = 0.25f;
                } else {
                    pushForceRatio = 0.75f;
                }
                break;
            case CRUISER:
                maxAngVel = 45f;
                if (ship.isPhased()) {
                    pushForceRatio = 0.175f;
                } else {
                    pushForceRatio = 0.6f;
                }
                break;
            case CAPITAL_SHIP:
                maxAngVel = 15f;
                if (ship.isPhased()) {
                    pushForceRatio = 0.1f;
                } else {
                    pushForceRatio = 0.45f;
                }
                break;
        }

        Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), target.getLocation());
        float totalForce = forcePerBolt * bolts * falloff;
        float pushForce = totalForce * pushForceRatio;
        float angToTarget = VectorUtils.getAngle(ship.getLocation(), II_Multi.getRoot(target).getLocation());
        float angVelDelta = totalForce * 50f * (1f - pushForceRatio) / II_Multi.getRoot(target).getMassWithModules();
        angVelDelta *= Math.signum(MathUtils.getShortestRotation(II_Multi.getRoot(target).getFacing(), angToTarget));
        float currAngVel = II_Multi.getRoot(target).getAngularVelocity();
        float newAngVel = currAngVel + angVelDelta;

        if (Math.abs(newAngVel) > Math.abs(maxAngVel)) {
            float extraPushForceRatio;
            if ((Math.abs(currAngVel) > Math.abs(maxAngVel)) && (Math.signum(currAngVel) == Math.signum(angVelDelta))) {
                /* Can't speed up a target that is already spinning as fast as possible */
                extraPushForceRatio = 1f - pushForceRatio;
                newAngVel = currAngVel;
            } else if ((Math.abs(currAngVel) > Math.abs(maxAngVel)) && (Math.signum(currAngVel) != Math.signum(angVelDelta))) {
                /* Futilely slowing down a target that's over the max spin speed */
                extraPushForceRatio = 0f;
            } else {
                extraPushForceRatio = ((Math.abs(newAngVel) - Math.abs(maxAngVel)) / Math.abs(angVelDelta)) * (1f - pushForceRatio);
                newAngVel = maxAngVel * Math.signum(newAngVel);
            }
            pushForce += totalForce * extraPushForceRatio;
        }

        II_Multi.getRoot(target).setAngularVelocity(newAngVel);
        II_Util.applyForce(target, dir, pushForce);
    }

    protected ShipAPI findTarget(ShipAPI ship, ShipAPI currTarget) {
        float range = MAX_RANGE;
        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            range = TARGETING_MAX_RANGE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            range = ELITE_MAX_RANGE;
        } else if (ship.getVariant().hasHullMod("supercomputer")) {
            range = II_ShockBusterStats.STATION_MAX_RANGE;
        }

        range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);

        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = currTarget;

        if (player && (ship.getShipTarget() != null)) {
            target = ship.getShipTarget();
            float dist = MathUtils.getDistance(ship, target);
            if (dist > range) {
                /* Revert to the old target if the new one is out of range */
                target = currTarget;
            }
        }

        if ((target == null) || !isTargetValid(ship, target)) {
            Vector2f mouseTarget = ship.getMouseTarget();
            if (player && (target != null) && (target == ship.getShipTarget()) && target.getVariant().hasHullMod(HullMods.VASTBULK)) {
                mouseTarget = target.getLocation();
            }
            if ((ship.getShipAI() != null) && ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
                mouseTarget = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
            }
            target = Misc.findClosestShipEnemyOf(ship, mouseTarget, HullSize.FRIGATE, range, true, FILTER);
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, mouseTarget, HullSize.FIGHTER, range, true, FILTER);
            }
        }

        if ((target == null) || !isTargetValid(ship, target)) {
            target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FRIGATE, range, true, FILTER);
            if (target == null) {
                target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), HullSize.FIGHTER, range, true, FILTER);
            }
        }

        return target;
    }

    protected List<ShipAPI> findTargets(ShipAPI ship) {
        float range = ARMOR_MAX_RANGE;
        List<ShipAPI> targets = new ArrayList<>();

        range = ship.getMutableStats().getSystemRangeBonus().computeEffective(range);

        for (ShipAPI target : II_Util.getShipsWithinRange(ship.getLocation(), range + ship.getCollisionRadius())) {
            if (target.isShuttlePod() || !target.isAlive()) {
                continue;
            }
            if ((ship.getOwner() != target.getOwner()) && (target.getOwner() != 100)) {
                if (!FILTER.matches(target)) {
                    continue;
                }
                targets.add(target);
            }
        }

        return targets;
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            return ARMOR_IN_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            return TARGETING_IN_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_IN_OVERRIDE;
        }
        return -1;
    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            return ARMOR_OUT_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            return TARGETING_OUT_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_OUT_OVERRIDE;
        }
        return -1;
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_REGEN_OVERRIDE;
        }
        return REGEN_OVERRIDE;
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            return ARMOR_USES_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            return TARGETING_USES_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_USES_OVERRIDE;
        }
        return USES_OVERRIDE;
    }
}
