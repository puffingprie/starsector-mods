package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.EnumSet;

public class II_MagnumSalvoStats extends BaseShipSystemScript {

    public static final float DURATION = 5f;

    public static final Color JITTER_COLOR_STANDARD = new Color(255, 150, 50, 100);
    public static final Color GLOW_COLOR_STANDARD = new Color(255, 150, 25);
    public static final Color JITTER_COLOR_ARMOR = new Color(255, 200, 100, 110);
    public static final Color GLOW_COLOR_ARMOR = new Color(255, 200, 75);
    public static final Color JITTER_COLOR_TARGETING = new Color(150, 200, 255, 80);
    public static final Color GLOW_COLOR_TARGETING = new Color(175, 225, 255);
    public static final Color JITTER_COLOR_ELITE = new Color(175, 75, 255, 100);
    public static final Color GLOW_COLOR_ELITE = new Color(225, 100, 255);

    private static final String DATA_KEY_ID = "II_MagnumSalvoStats";
    private final Object STATEKEY = new Object();
    private final Object STATUSKEY1 = new Object();

    private boolean fired = false;
    private float active = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        MagnumSalvoData msData = null;
        if (data instanceof MagnumSalvoData) {
            msData = (MagnumSalvoData) data;
        }
        if ((msData == null) || (STATEKEY != msData.stateKey)) {
            msData = new MagnumSalvoData(STATEKEY);
            Global.getCombatEngine().getCustomData().put(DATA_KEY_ID + "_" + ship.getId(), msData);
            msData.armed = 0;
            active = 0f;
        }

        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame();
        objectiveAmount *= Global.getCombatEngine().getTimeMult().getModifiedValue();
        if (Global.getCombatEngine().isPaused()) {
            objectiveAmount = 0f;
        }

        Color JITTER_COLOR = JITTER_COLOR_STANDARD;
        Color GLOW_COLOR = GLOW_COLOR_STANDARD;
        float underIntensity = 1.5f;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ARMOR;
            GLOW_COLOR = GLOW_COLOR_ARMOR;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_TARGETING;
            GLOW_COLOR = GLOW_COLOR_TARGETING;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            JITTER_COLOR = JITTER_COLOR_ELITE;
            GLOW_COLOR = GLOW_COLOR_ELITE;
        }

        if (msData.armed <= 0) {
            active = 0f;
        }

        if ((state == State.COOLDOWN) || (state == State.IDLE)) {
            fired = false;
        }

        if (ship.controlsLocked()) {
            msData.armed = 0;
            active = 0f;
        } else {
            active -= objectiveAmount;
            if (active <= 0f) {
                boolean delayEnd = false;
                for (WeaponAPI weapon : ship.getAllWeapons()) {
                    if (weapon.getId().startsWith("ii_armageddon")) {
                        if (weapon.isFiring() && (weapon.getChargeLevel() >= 1f)) {
                            delayEnd = true;
                        }
                    }
                }
                if (!delayEnd) {
                    msData.armed = 0;
                }

                active = 0f;
            }
        }

        if (((state == State.ACTIVE) || (state == State.OUT)) && !fired) {
            fired = true;
            active = DURATION;
            msData.armed = 0;

            Global.getSoundPlayer().playSound("ii_magnum_salvo", 1f, 1f, ship.getLocation(), ship.getVelocity());

            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (weapon.getId().startsWith("ii_armageddon")) {
                    if (!weapon.isDisabled() && !weapon.isPermanentlyDisabled()) {
                        int currAmmo = weapon.getAmmo();
                        int maxAmmo = weapon.getMaxAmmo();
                        int salvo = weapon.getSpec().getBurstSize();

                        weapon.setAmmo(Math.min(maxAmmo, currAmmo + salvo));
                        if ((!weapon.isFiring() || (weapon.getChargeLevel() < 1f)) && (weapon.getCooldownRemaining() >= 0.25f)) {
                            weapon.setRemainingCooldownTo(0.25f);
                        }
                        msData.armed += salvo;
                    }
                }
            }
        }

        if (Global.getCombatEngine().getPlayerShip() == ship) {
            if (msData.armed > 0) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, ship.getSystem().getSpecAPI().getIconSpriteName(),
                        ship.getSystem().getDisplayName(), String.format("%.1f seconds", active), false);
            }
        }

        if (msData.armed > 0) {
            ship.setWeaponGlow(1f, GLOW_COLOR, EnumSet.of(WeaponType.MISSILE));
        } else {
            ship.setWeaponGlow(0f, GLOW_COLOR, EnumSet.of(WeaponType.MISSILE));
        }

        if ((state == State.IN) || (state == State.ACTIVE) || (state == State.OUT)) {
            float jitterUnderIntensity = 0.75f * underIntensity * effectLevel;
            ship.setJitterUnder(this,
                    new Color(JITTER_COLOR.getRed(), JITTER_COLOR.getGreen(), JITTER_COLOR.getBlue(), II_Util.clamp255(Math.round(JITTER_COLOR.getAlpha() * jitterUnderIntensity))),
                    1f, Math.round(25 * underIntensity), 0f, 10f * underIntensity * (float) Math.sqrt(jitterUnderIntensity));
        } else {
            ship.setJitterUnder(this, JITTER_COLOR, 0f, 25, 0f, 10f * underIntensity);
        }
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return getPotential(ship) > 0f;
    }

    public static int getArmed(ShipAPI ship) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return 0;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof MagnumSalvoData) {
            MagnumSalvoData msData = (MagnumSalvoData) data;
            return msData.armed;
        } else {
            return 0;
        }
    }

    public static void setArmed(ShipAPI ship, int armed) {
        if ((ship == null) || (ship.getSystem() == null)) {
            return;
        }

        Object data = Global.getCombatEngine().getCustomData().get(DATA_KEY_ID + "_" + ship.getId());
        if (data instanceof MagnumSalvoData) {
            MagnumSalvoData msData = (MagnumSalvoData) data;
            msData.armed = armed;
        }
    }

    /* 0   = system would do nothing
       0.4 = system would enhance 10 missiles
       0.6 = system would replenish 10 ammo and enhance 10 missiles
       0.8 = system would reset the full duration of both cooldowns and enhance 10 missiles
       1   = system would replenish 10 ammo, reset the full duration of both cooldowns, and enhance 10 missiles
     */
    public static float getPotential(ShipAPI ship) {
        if (ship == null) {
            return 0;
        }

        float potential = 0;
        float maxPotential = 0;
        boolean firing = false;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getId().startsWith("ii_armageddon")) {
                if (!weapon.isPermanentlyDisabled()) {
                    int salvo = weapon.getSpec().getBurstSize();
                    maxPotential += salvo + (salvo * 0.5f) + salvo;

                    if (!weapon.isDisabled()) {
                        if (!weapon.isFiring() || (weapon.getChargeLevel() < 1f)) {
                            int currAmmo = weapon.getAmmo();
                            int maxAmmo = weapon.getMaxAmmo();
                            potential += salvo;
                            potential += Math.min(salvo, maxAmmo - currAmmo) * 0.5f;
                            potential += salvo * (weapon.getCooldownRemaining() / weapon.getCooldown());
                        } else {
                            firing = true;
                        }
                    }
                }
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
            return "" + armed + " ARMED";
        }
        if (!isUsable(system, ship)) {
            return "BUSY";
        }
        return null;
    }

    private static class MagnumSalvoData {

        final Object stateKey;
        int armed;

        MagnumSalvoData(Object stateKey) {
            this.stateKey = stateKey;
        }
    }
}
