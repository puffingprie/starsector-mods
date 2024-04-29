package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;

public class II_ArbalestLoaderStats extends BaseShipSystemScript {

    /* Default system does not enhance the bolt */
    public static final float DURATION_STANDARD = 0f;
    public static final float DURATION_ARMOR = 5f;
    public static final float DURATION_TARGETING = 5f;
    public static final float DURATION_ELITE = 8f;

    public static final int USES_OVERRIDE = 3;
    public static final float REGEN_OVERRIDE = 0.125f;
    public static final float OUT_OVERRIDE = 1f;
    public static final int ARMOR_USES_OVERRIDE = 2;
    public static final float ARMOR_REGEN_OVERRIDE = 0.08f;
    public static final float ARMOR_FLUX_MULT = 2f;
    public static final float ARMOR_OUT_OVERRIDE = 1f;
    public static final int TARGETING_USES_OVERRIDE = 2;
    public static final float TARGETING_REGEN_OVERRIDE = 0.08f;
    public static final float TARGETING_FLUX_MULT = 1.5f;
    public static final float TARGETING_OUT_OVERRIDE = 1f;
    public static final int ELITE_USES_OVERRIDE = 3;
    public static final float ELITE_REGEN_OVERRIDE = 0.125f;
    public static final float ELITE_FLUX_MULT = 1.25f;
    public static final float ELITE_OUT_OVERRIDE = 0.5f;

    public static final Color JITTER_COLOR_STANDARD = new Color(255, 150, 50, 100);
    public static final Color GLOW_COLOR_STANDARD = new Color(255, 180, 75);
    public static final Color JITTER_COLOR_ARMOR = new Color(255, 205, 50, 100);
    public static final Color GLOW_COLOR_ARMOR = new Color(255, 225, 75);
    public static final Color JITTER_COLOR_TARGETING = new Color(50, 155, 255, 100);
    public static final Color GLOW_COLOR_TARGETING = new Color(75, 150, 255);
    public static final Color JITTER_COLOR_ELITE = new Color(185, 50, 255, 100);
    public static final Color GLOW_COLOR_ELITE = new Color(210, 75, 255);

    private static final String DATA_KEY_ID = "II_ArbalestLoaderStats";
    private final Object STATEKEY = new Object();
    private final Object STATUSKEY1 = new Object();

    private boolean fired = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        ArbalestLoaderData alData = null;
        if (data instanceof ArbalestLoaderData) {
            alData = (ArbalestLoaderData) data;
        }
        if ((alData == null) || (STATEKEY != alData.stateKey)) {
            alData = new ArbalestLoaderData(STATEKEY);
            Global.getCombatEngine().getCustomData().put(DATA_KEY_ID + "_" + ship.getId(), alData);
            alData.armed = 0;
            alData.active = 0f;
        }

        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame();
        objectiveAmount *= Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            objectiveAmount = 0f;
        }

        Color JITTER_COLOR = JITTER_COLOR_STANDARD;
        float underIntensity = 1f;
        float DURATION = DURATION_STANDARD;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ARMOR;
            DURATION = DURATION_ARMOR;
            underIntensity = 1.5f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_TARGETING;
            DURATION = DURATION_TARGETING;
            underIntensity = 1.5f;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ELITE;
            DURATION = DURATION_ELITE;
            underIntensity = 1.5f;
        }

        if (alData.armed <= 0) {
            alData.active = 0f;
        }

        if ((state == State.COOLDOWN) || (state == State.IDLE)) {
            fired = false;
        }

        WeaponAPI magnaFulmen = null;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getId().contentEquals("ii_magna_fulmen")) {
                magnaFulmen = weapon;
                break;
            }
        }

        if (ship.controlsLocked()) {
            alData.armed = 0;
            alData.active = 0f;
        } else {
            alData.active -= objectiveAmount;
            if (alData.active <= 0f) {
                boolean delayEnd = false;
                if (magnaFulmen != null) {
                    if (magnaFulmen.isFiring() && (magnaFulmen.getChargeLevel() >= 1f)) {
                        delayEnd = true;
                    }
                }
                if (!delayEnd) {
                    alData.armed = 0;
                }

                alData.active = 0f;
            }
        }

        if (((state == State.ACTIVE) || (state == State.OUT)) && !fired) {
            fired = true;
            alData.active = DURATION;

            if (magnaFulmen != null) {
                if (!magnaFulmen.isDisabled() && !magnaFulmen.isPermanentlyDisabled()) {
                    if ((!magnaFulmen.isFiring() || (magnaFulmen.getChargeLevel() < 1f)) && (magnaFulmen.getCooldownRemaining() >= 0.2f)) {
                        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                            magnaFulmen.setRemainingCooldownTo(Math.max(0.2f, magnaFulmen.getCooldownRemaining() - 2f));
                        } else {
                            magnaFulmen.setRemainingCooldownTo(0.2f);
                        }
                    }

                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        alData.armed = 1;
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                        alData.armed = 1;
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                        alData.armed = Math.min(4, alData.armed + 1);

                        if ((magnaFulmen.getCooldownRemaining() + 2f) > alData.active) {
                            alData.active = magnaFulmen.getCooldownRemaining() + 2f;
                        }
                    } else {
                        alData.armed = 0;
                    }
                } else {
                    alData.armed = 0;
                }
            } else {
                alData.armed = 0;
            }

            Global.getSoundPlayer().playSound("ii_arbalest_loader", 1f + (Math.max(0, alData.armed - 1) * 0.1f), 1f, ship.getLocation(), ship.getVelocity());
        }

        if (Global.getCombatEngine().getPlayerShip() == ship) {
            if (alData.armed > 0) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                        ship.getSystem().getDisplayName(), String.format("%.1f seconds", alData.active), false);
            }
        }

        if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT) || (alData.armed > 0)) {
            float jitterUnderIntensity = 0.75f * II_Util.lerp(underIntensity * (1f + (alData.armed * 0.25f)), underIntensity, effectLevel) * II_Util.lerp(alData.armed * 0.25f, 1f, effectLevel);
            ship.setJitterUnder(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                    1f, Math.round(25 * underIntensity), 0f, 10f * underIntensity * (float) Math.sqrt(jitterUnderIntensity));
        } else {
            ship.setJitterUnder(this, JITTER_COLOR, 0f, 25, 0f, 10f * underIntensity);
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (ship == null) {
            return false;
        }

        if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * ARMOR_FLUX_MULT);
        } else if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * TARGETING_FLUX_MULT);
        } else if ((system != null) && ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            system.setFluxPerUse(ship.getHullSpec().getFluxCapacity() * 0.1f * ELITE_FLUX_MULT);
        }

        return getPotential(ship) > 0f;
    }

    public static int getArmed(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof ArbalestLoaderData) {
            ArbalestLoaderData alData = (ArbalestLoaderData) data;
            return alData.armed;
        } else {
            return 0;
        }
    }

    public static void setArmed(ShipAPI ship, int armed) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof ArbalestLoaderData) {
            ArbalestLoaderData alData = (ArbalestLoaderData) data;
            alData.armed = armed;
        }
    }

    public static float getActiveTimeLeft(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof ArbalestLoaderData) {
            ArbalestLoaderData alData = (ArbalestLoaderData) data;
            return alData.active;
        } else {
            return 0;
        }
    }

    /* Standard
       0 = system would do nothing
       1 = system would reset the full duration of the cooldown
       Armor/Targeting
       0   = system would do nothing
       0.5 = system would enhance the weapon
       1   = system would reset the full duration of the cooldown and enhance the weapon
       Elite
       0   = system would do nothing
       0.8 = system would enhance the weapon
       1   = system would reduce the cooldown duration by 2 seconds and enhance the weapon
     */
    public static float getPotential(ShipAPI ship) {
        if (ship == null) {
            return 0;
        }

        float potential = 0;
        float maxPotential = 0;
        boolean firing = false;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getId().contentEquals("ii_magna_fulmen")) {
                if (!weapon.isPermanentlyDisabled()) {
                    if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                        maxPotential += 2f;
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                        maxPotential += 2f;
                    } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                        maxPotential += 1f + (2f / weapon.getCooldown());
                    } else {
                        maxPotential += 1f;
                    }

                    if (!weapon.isDisabled()) {
                        if ((!weapon.isFiring() || (weapon.getChargeLevel() < 1f)) && (!weapon.isFiring() || (weapon.getCooldownRemaining() > 0f))) {
                            int armed = getArmed(ship);
                            if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                if (armed == 0) {
                                    potential += 1f;
                                }
                            } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                if (armed == 0) {
                                    potential += 1f;
                                }
                            } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                if (armed < 4) {
                                    potential += 1f;
                                }
                            }

                            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                potential += Math.min(2f, weapon.getCooldownRemaining()) / weapon.getCooldown();
                            } else {
                                potential += weapon.getCooldownRemaining() / weapon.getCooldown();
                            }
                        } else {
                            firing = true;
                        }
                    }
                }
                break;
            }
        }

        if (firing) {
            /* Don't allow system to be used in the middle of a firing cycle */
            return 0f;
        } else {
            return potential / maxPotential;
        }
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        int armed = getArmed(ship);
        if (armed > 0) {
            if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                switch (armed) {
                    case 1:
                    case 2:
                    case 3:
                        return "CHARGED x" + armed;
                    default:
                    case 4:
                        return "CHARGED MAX";
                }
            } else {
                return "CHARGED";
            }
        }
        if (!isUsable(system, ship)) {
            boolean disabled = false;
            boolean firing = false;

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getId().contentEquals("ii_magna_fulmen")) {
                    if (!weapon.isPermanentlyDisabled() && !weapon.isDisabled()) {
                        if (weapon.isFiring() && (weapon.getChargeLevel() >= 1f)) {
                            firing = true;
                        }
                    } else {
                        disabled = true;
                    }
                    break;
                }
            }

            if (disabled) {
                return "DISABLED";
            } else if (firing) {
                return "BUSY";
            } else {
                return "NOT READY";
            }
        }
        return null;
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
        return OUT_OVERRIDE;
    }

    @Override
    public float getRegenOverride(ShipAPI ship) {
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            return ARMOR_REGEN_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            return TARGETING_REGEN_OVERRIDE;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
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

    private static class ArbalestLoaderData {

        final Object stateKey;
        int armed;
        float active;

        ArbalestLoaderData(Object stateKey) {
            this.stateKey = stateKey;
        }
    }
}
