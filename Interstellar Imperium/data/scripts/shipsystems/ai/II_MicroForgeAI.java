package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import org.lwjgl.util.vector.Vector2f;

public class II_MicroForgeAI implements ShipSystemAIScript {

    private static final float TRIGGER_FRAC_OF_MAX_VALUE = 0.75f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX = 0.9f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_ARMOR = 0.85f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_ARMOR = 0.7f;
    private static final float TRIGGER_FRAC_MOD_INCOMING_DAMAGE_ARMOR = 0.8f;
    private static final float TRIGGER_FRAC_MOD_CRITICAL_DPS_ARMOR = 0.8f;
    private static final float TRIGGER_FRAC_MOD_LOW_HULL_ARMOR = 0.6f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_TARGETING = 0.6f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_TARGETING = 0.75f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_ELITE = 0.9f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_ELITE = 0.7f;
    private static final float TRIGGER_FRAC_OF_MAX_VALUE_VERY_HIGH_FLUX_ELITE = 0.5f;
    private static final float TRIGGER_FRAC_MOD_INCOMING_DAMAGE_ELITE = 0.8f;
    private static final float TRIGGER_FRAC_MOD_CRITICAL_DPS_ELITE = 0.8f;
    private static final float TRIGGER_FRAC_MOD_LOW_HULL_ELITE = 0.6f;

    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        tracker.advance(amount);

        if (tracker.intervalElapsed()) {
            if (ship.getFluxTracker().isOverloadedOrVenting() || (system.getAmmo() == 0) || system.isActive()) {
                return;
            }

            /* Red "panic!" flags that should make using the system too risky */
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                if (flags.hasFlag(AIFlags.HARASS_MOVE_IN)
                        || flags.hasFlag(AIFlags.SAFE_VENT)
                        || flags.hasFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT)) {
                    return;
                }
            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                if (flags.hasFlag(AIFlags.SAFE_VENT)) {
                    return;
                }
            } else {
                if (flags.hasFlag(AIFlags.RUN_QUICKLY)
                        || flags.hasFlag(AIFlags.PURSUING)
                        || flags.hasFlag(AIFlags.HARASS_MOVE_IN)
                        || flags.hasFlag(AIFlags.DO_NOT_USE_FLUX)
                        || flags.hasFlag(AIFlags.TURN_QUICKLY)
                        || flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)
                        || flags.hasFlag(AIFlags.BACKING_OFF)
                        || flags.hasFlag(AIFlags.KEEP_SHIELDS_ON)
                        || flags.hasFlag(AIFlags.OK_TO_CANCEL_SYSTEM_USE_TO_VENT)
                        || flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                    return;
                }
            }

            float fluxRemaining = ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
            if ((ship.getSystem().getFluxPerUse() > fluxRemaining) && !ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                return;
            }

            float basePPT = ship.getHullSpec().getNoCRLossTime();
            float valueToGain = 0f;
            float maxValueToGain = 0f;
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getType() == WeaponType.MISSILE) {
                    if (weapon.usesAmmo()) {
                        /* Can't replenish ammo that was gained by EMR & similar buffs */
                        int baseAmmo = weapon.getSpec().getMaxAmmo();
                        int currAmmo = weapon.getAmmo();
                        int maxAmmo = weapon.getMaxAmmo();

                        if (baseAmmo <= 0) {
                            continue;
                        }

                        /* Higher-OP weapons with less ammo are more important to replenish... unless they regen quickly */
                        float valueOfOneAmmo = weapon.getSpec().getOrdnancePointCost(null, null) / (float) baseAmmo;
                        if (weapon.getAmmoPerSecond() > 0f) {
                            float fracRegeneratedPerPPT = (weapon.getAmmoPerSecond() * basePPT) / (float) baseAmmo;
                            valueOfOneAmmo /= 1f + fracRegeneratedPerPPT;
                        }

                        int ammoToGain = Math.max(Math.min(baseAmmo, maxAmmo - currAmmo), 0);
                        valueToGain += (float) ammoToGain * valueOfOneAmmo;
                        maxValueToGain += (float) baseAmmo * valueOfOneAmmo;
                    }
                }
            }

            if (maxValueToGain <= 0f) {
                /* System w/o replenishable missiles still has some use for armor/elite */
                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                    valueToGain = 0.3f;
                    maxValueToGain = 1f;
                } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                    valueToGain = 0.3f;
                    maxValueToGain = 1f;
                } else {
                    /* But totally useless for standard/targeting */
                    return;
                }
            }

            float triggerLevel;
            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                float triggerFrac;
                if ((ship.getFluxLevel() >= 0.5f) && (ship.getHardFluxLevel() >= 0.25f)) {
                    triggerFrac = TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_ARMOR;
                } else {
                    triggerFrac = TRIGGER_FRAC_OF_MAX_VALUE_ARMOR;
                }
                if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
                    triggerFrac *= TRIGGER_FRAC_MOD_INCOMING_DAMAGE_ARMOR;
                }
                if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                    triggerFrac *= TRIGGER_FRAC_MOD_CRITICAL_DPS_ARMOR;
                }
                if (ship.getHullLevel() <= 0.6f) {
                    triggerFrac *= TRIGGER_FRAC_MOD_LOW_HULL_ARMOR;
                }
                triggerLevel = maxValueToGain * triggerFrac;
            } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                if ((ship.getFluxLevel() >= 0.5f) && (ship.getHardFluxLevel() >= 0.25f)) {
                    triggerLevel = maxValueToGain * TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_TARGETING;
                } else {
                    triggerLevel = maxValueToGain * TRIGGER_FRAC_OF_MAX_VALUE_TARGETING;
                }
            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                float triggerFrac;
                if ((ship.getFluxLevel() >= 0.85f) && (ship.getHardFluxLevel() >= 0.425f)) {
                    triggerFrac = TRIGGER_FRAC_OF_MAX_VALUE_VERY_HIGH_FLUX_ELITE;
                } else if ((ship.getFluxLevel() >= 0.5f) && (ship.getHardFluxLevel() >= 0.25f)) {
                    triggerFrac = TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX_ELITE;
                } else {
                    triggerFrac = TRIGGER_FRAC_OF_MAX_VALUE_ELITE;
                }
                if (flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
                    triggerFrac *= TRIGGER_FRAC_MOD_INCOMING_DAMAGE_ELITE;
                }
                if (flags.hasFlag(AIFlags.IN_CRITICAL_DPS_DANGER)) {
                    triggerFrac *= TRIGGER_FRAC_MOD_CRITICAL_DPS_ELITE;
                }
                if (ship.getHullLevel() <= 0.6f) {
                    triggerFrac *= TRIGGER_FRAC_MOD_LOW_HULL_ELITE;
                }
                triggerLevel = maxValueToGain * triggerFrac;
            } else {
                if ((ship.getFluxLevel() >= 0.5f) && (ship.getHardFluxLevel() >= 0.25f)) {
                    triggerLevel = maxValueToGain * TRIGGER_FRAC_OF_MAX_VALUE_HIGH_FLUX;
                } else {
                    triggerLevel = maxValueToGain * TRIGGER_FRAC_OF_MAX_VALUE;
                }
            }

            if (valueToGain >= triggerLevel) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.system = system;
        this.engine = engine;
    }
}
