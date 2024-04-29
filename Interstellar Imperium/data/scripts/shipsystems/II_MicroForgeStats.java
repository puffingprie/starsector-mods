package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class II_MicroForgeStats extends BaseShipSystemScript {

    private static final Color JITTER_COLOR_STANDARD = new Color(255, 150, 50, 100);
    private static final Color GLOW_COLOR_STANDARD = new Color(255, 150, 25);
    private static final Color JITTER_COLOR_ARMOR = new Color(255, 225, 50, 100);
    private static final Color GLOW_COLOR_ARMOR = new Color(255, 225, 25);
    private static final Color JITTER_COLOR_TARGETING = new Color(50, 200, 255, 100);
    private static final Color GLOW_COLOR_TARGETING = new Color(25, 225, 255);
    private static final Color JITTER_COLOR_ELITE = new Color(155, 50, 255, 100);
    private static final Color GLOW_COLOR_ELITE = new Color(225, 25, 255);

    private final Object STATUSKEY1 = new Object();

    private boolean started = false;
    private boolean armed = false;
    private boolean fired = false;
    private int ammoCounter = -1;

    public static final Map<HullSize, Float> ARMOR_DAMAGE_REDUCTION = new HashMap<>();
    public static final float ARMOR_FLUX_PER_SECOND = 0.25f;
    public static final float ARMOR_FLUX_PER_USE = 0.25f;
    public static final int TARGETING_CHARGE_MULT = 2;
    public static final float ELITE_FLUX_PER_SECOND = 0f;
    public static final float ELITE_FLUX_PER_USE = 0f;
    public static final float ELITE_IN_OVERRIDE = 1f;

    private static final Map<HullSize, Float> SPARK_SCALE = new HashMap<>();

    static {
        ARMOR_DAMAGE_REDUCTION.put(HullSize.FRIGATE, 0.33f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.DESTROYER, 0.33f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.CRUISER, 0.5f);
        ARMOR_DAMAGE_REDUCTION.put(HullSize.CAPITAL_SHIP, 0.5f);

        SPARK_SCALE.put(HullSize.FRIGATE, 1f);
        SPARK_SCALE.put(HullSize.DESTROYER, 1.5f);
        SPARK_SCALE.put(HullSize.CRUISER, 2f);
        SPARK_SCALE.put(HullSize.CAPITAL_SHIP, 2.5f);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (ammoCounter == -1) {
            ammoCounter = ship.getSystem().getAmmo();
        }

        Color JITTER_COLOR = JITTER_COLOR_STANDARD;
        Color GLOW_COLOR = GLOW_COLOR_STANDARD;
        float intensity = 1f;
        float underIntensity = 0f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ARMOR;
            GLOW_COLOR = GLOW_COLOR_ARMOR;
            underIntensity = 1f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_TARGETING;
            GLOW_COLOR = GLOW_COLOR_TARGETING;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ELITE;
            GLOW_COLOR = GLOW_COLOR_ELITE;
            intensity = 2f;
        }

        if (!started) {
            started = true;

            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                Global.getSoundPlayer().playSound("ii_micro_forge_elite_startup", 1f, 1f, ship.getLocation(), ship.getVelocity());
            }
        }

        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            float damperEffectLevel;
            if (state == State.OUT) {
                damperEffectLevel = effectLevel;
            } else {
                damperEffectLevel = II_Util.lerp(0f, 1f, Math.min(1f, effectLevel * 7f / 0.25f));
            }
            float mult = II_Util.lerp(1f, ARMOR_DAMAGE_REDUCTION.get(ship.getHullSize()), damperEffectLevel);
            stats.getArmorDamageTakenMult().modifyMult(id, mult);
            stats.getHullDamageTakenMult().modifyMult(id, mult);
            if (Global.getCombatEngine().getPlayerShip() == ship) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                        ship.getSystem().getDisplayName(), "hull/armor damage taken -" + (int) Math.round((1f - mult) * 100f) + "%", false);
            }

            if (!Global.getCombatEngine().isPaused()) {
                Global.getSoundPlayer().playLoop("ii_micro_forge_armor_loop", ship, 1f, damperEffectLevel, ship.getLocation(), ship.getVelocity());
            }
        }

        if ((effectLevel >= 0.9f) && (state == State.IN)) {
            armed = true;
        }

        if (((state == State.ACTIVE) || (state == State.OUT)) && armed) {
            armed = false;
            fired = true;
            ammoCounter--;

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getType() == WeaponType.MISSILE) {
                    if (weapon.usesAmmo()) {
                        /* Don't replenish ammo that was gained by EMR & similar buffs */
                        int baseAmmo = weapon.getSpec().getMaxAmmo();
                        int currAmmo = weapon.getAmmo();
                        int maxAmmo = weapon.getMaxAmmo();
                        weapon.setAmmo(Math.min(maxAmmo, currAmmo + baseAmmo));
                    }
                }
            }

            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                int numSparks = Math.round(ship.getFluxLevel() * 5f * SPARK_SCALE.get(ship.getHullSize())) + 2;
                float mag = ship.getFluxLevel();
                ship.getFluxTracker().setHardFlux(0f);
                ship.getFluxTracker().setCurrFlux(0f);
                Global.getCombatEngine().spawnExplosion(ship.getLocation(), ship.getVelocity(),
                        new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), II_Util.clamp255(100 + Math.round(mag * 155f))),
                        ship.getCollisionRadius() * 3f, 0.15f);
                for (int i = 0; i < numSparks; i++) {
                    Vector2f point = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius() * (1f + mag));
                    Global.getCombatEngine().spawnEmpArcPierceShields(ship, point, ship, ship, DamageType.ENERGY,
                            0f, 0f, ship.getCollisionRadius() * 3f, null, 5f + (mag * 5f), JITTER_COLOR, GLOW_COLOR);
                }

                Global.getSoundPlayer().playSound("ii_micro_forge_elite_fire", 1f, 0.5f + (0.5f * mag), ship.getLocation(), ship.getVelocity());
            }

            Global.getSoundPlayer().playSound("ii_micro_forge_fire", 1f, 1f, ship.getLocation(), ship.getVelocity());
        }

        if (fired) {
            float jitterIntensity = II_Util.lerp(0f, 0.25f, effectLevel) * intensity;
            ship.setJitter(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterIntensity))),
                    1f, Math.round(5 * intensity), 0f, 20f * intensity * (float) Math.sqrt(jitterIntensity));
            if (underIntensity > 0f) {
                float jitterUnderIntensity = 0.75f * underIntensity * effectLevel;
                ship.setJitterUnder(this,
                        new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                        1f, Math.round(25 * underIntensity), 0f, 10f * underIntensity * (float) Math.sqrt(jitterUnderIntensity));
            }
            ship.setWeaponGlow(effectLevel, GLOW_COLOR, EnumSet.of(WeaponType.MISSILE));
        } else {
            float jitterIntensity = II_Util.lerp(0.1f, 0.2f, effectLevel) * intensity;
            ship.setJitter(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterIntensity))),
                    1f, Math.round(5 * intensity), 2f * intensity * jitterIntensity, (5f + (effectLevel * 2f)) * intensity * (float) Math.sqrt(jitterIntensity));
            if (underIntensity > 0f) {
                float jitterUnderIntensity = 0.75f * underIntensity;
                ship.setJitterUnder(this,
                        new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                        1f, Math.round(25 * underIntensity), 0f, 10f * underIntensity * (float) Math.sqrt(jitterUnderIntensity));
            }

            /* Refund the system's charge if the system wasn't activated */
            if (state == State.OUT) {
                ship.getSystem().setAmmo(ammoCounter);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (ammoCounter == -1) {
            ammoCounter = ship.getSystem().getAmmo();
        }

        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);

        /* Make sure we deduct the system's charge if the system was activated */
        if (fired) {
            ship.getSystem().setAmmo(ammoCounter);
        }

        armed = false;
        fired = false;
        started = false;

        ship.setJitter(this, JITTER_COLOR_STANDARD, 0f, 5, 0f, 20f);
        ship.setJitterUnder(this, JITTER_COLOR_STANDARD, 0f, 25, 0f, 10f);
        ship.setWeaponGlow(0f, GLOW_COLOR_STANDARD, EnumSet.of(WeaponType.MISSILE));
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if ((ship != null) && (system != null) && ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            system.setFluxPerSecond(ship.getHullSpec().getFluxDissipation() * ARMOR_FLUX_PER_SECOND);
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * ARMOR_FLUX_PER_USE);
        }

        if ((ship != null) && (system != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            system.setFluxPerSecond(ship.getHullSpec().getFluxDissipation() * ELITE_FLUX_PER_SECOND);
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * ELITE_FLUX_PER_USE);
        }

        return true;
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            return ELITE_IN_OVERRIDE;
        }
        return -1;
    }

    @Override
    public int getUsesOverride(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            return 1 * TARGETING_CHARGE_MULT;
        }
        return -1;
    }
}
