package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_LuxFinisStats;
import java.awt.Color;
import java.util.ArrayList;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.AnchoredEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_LightspearEveryFrame implements EveryFrameWeaponEffectPlugin {

    public static final float LIGHTSPEAR_RANGE_BONUS_TARGETING = 300f;

    private static final Vector2f ZERO = new Vector2f();

    private static final float ORIGINLIGHT_INTENSITY = 0.4f;
    private static final float ORIGINLIGHT_SIZE = 325f;

    private boolean charging = false;
    private boolean cooling = false;
    private boolean firing = false;
    private final IntervalUtil interval = new IntervalUtil(0.015f, 0.015f);
    private float level = 0f;
    private StandardLight originLight = null;
    private SoundAPI sound;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();
        Color GLOW_COLOR = II_LuxFinisStats.GLOW_COLOR_STANDARD;
        Color FRINGE_COLOR = II_LuxFinisStats.FRINGE_COLOR_STANDARD;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            GLOW_COLOR = II_LuxFinisStats.GLOW_COLOR_ARMOR;
            FRINGE_COLOR = II_LuxFinisStats.FRINGE_COLOR_ARMOR;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            GLOW_COLOR = II_LuxFinisStats.GLOW_COLOR_TARGETING;
            FRINGE_COLOR = II_LuxFinisStats.FRINGE_COLOR_TARGETING;
        } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            GLOW_COLOR = II_LuxFinisStats.GLOW_COLOR_ELITE;
            FRINGE_COLOR = II_LuxFinisStats.FRINGE_COLOR_ELITE;
        }

        Vector2f origin = new Vector2f(weapon.getLocation());
        Vector2f offset;
        if (weapon.getSlot().isHardpoint()) {
            offset = new Vector2f(5.75f, 0f);
        } else {
            offset = new Vector2f(-8.5f, 0f);
        }
        VectorUtils.rotate(offset, weapon.getCurrAngle(), offset);
        Vector2f.add(offset, origin, origin);

        ArrayList<WeaponAPI> blasters = new ArrayList<>(2);
        for (WeaponAPI shipWeapon : ship.getAllWeapons()) {
            if (shipWeapon.getId().startsWith("ii_photonblaster") && !shipWeapon.isDisabled() && !shipWeapon.isPermanentlyDisabled()) {
                blasters.add(shipWeapon);
            }
        }
        float loss;
        if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
            loss = (2 - blasters.size()) * II_LightspearBeamEffect.LIGHTSPEAR_ATTEN_PER_DISABLED_PB_ARMOR;
        } else {
            loss = (2 - blasters.size()) * II_LightspearBeamEffect.LIGHTSPEAR_ATTEN_PER_DISABLED_PB;
        }
        float scale = 1f - loss;
        float overlevel = II_LuxFinisStats.getOverlevel(ship);
        scale *= (float) Math.sqrt(overlevel);

        if (weapon.isDisabled()) {
            scale = 0.001f;
        }
        if (weapon.isFiring()) {
            ship.getMutableStats().getBeamWeaponFluxCostMult().modifyMult("ii_lightspear_bonus", scale, "Lightspear Bonus");
            ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("ii_lightspear_bonus", scale, "Lightspear Bonus");
        } else {
            ship.getMutableStats().getBeamWeaponFluxCostMult().unmodify("ii_lightspear_bonus");
            ship.getMutableStats().getBeamWeaponDamageMult().unmodify("ii_lightspear_bonus");
        }
        if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
            if (ship.getSystem().isActive()) {
                ship.getMutableStats().getBeamWeaponRangeBonus().modifyFlat("ii_lightspear_bonus", LIGHTSPEAR_RANGE_BONUS_TARGETING * scale * ship.getSystem().getEffectLevel(), "Lightspear Bonus");
            } else {
                ship.getMutableStats().getBeamWeaponRangeBonus().unmodify("ii_lightspear_bonus");
            }
        }

        if (!ship.getSystem().isOn()) {
            weapon.setAmmo(0);
        } else {
            weapon.setAmmo(1);
        }

        if (ship.isHulk() && !weapon.isDisabled()) {
            weapon.disable();
        }
        if ((!ship.getSystem().isOn() || ((ship.getPhaseCloak() != null) && (ship.getPhaseCloak().isOn())))
                && ((weapon.getChargeLevel() >= 1f) || charging || firing) && !weapon.isDisabled()) {
            engine.addFloatingText(ship.getLocation(), "Automatic shutdown!", 24f, Color.RED, ship, 2f, 1f);
            weapon.disable();
            Global.getSoundPlayer().playLoop("ii_lightspear_loop", ship, 1f * overlevel, 0f, origin, new Vector2f());
            charging = false;
            cooling = true;
            firing = false;
            if (sound != null) {
                sound.stop();
                sound = null;
            }
        }
        if (sound != null) {
            sound.setPitch(1f * overlevel);
        }

        if ((weapon.getChargeLevel() > 0f) && (charging || firing)) {
            if (originLight == null) {
                originLight = new StandardLight(origin, ZERO, ZERO, null);
                LightShader.addLight(originLight);
            } else {
                originLight.setLocation(origin);
            }
            originLight.setIntensity(ORIGINLIGHT_INTENSITY * weapon.getChargeLevel() * scale);
            originLight.setSize(ORIGINLIGHT_SIZE * weapon.getChargeLevel() * scale);
            originLight.setColor(GLOW_COLOR.getRed() / 255f, GLOW_COLOR.getGreen() / 255f, GLOW_COLOR.getBlue() / 255f);
        } else if (originLight != null) {
            LightShader.removeLight(originLight);
            originLight = null;
        }

        if (charging) {
            if (firing && (weapon.getChargeLevel() < 1f)) {
                charging = false;
                cooling = true;
                firing = false;
                if (sound != null) {
                    sound.stop();
                    sound = null;
                }
            } else if (weapon.getChargeLevel() < 1f) {
                interval.advance(amount);
                if (interval.intervalElapsed()) {
                    engine.addHitParticle(origin, ZERO, (float) Math.sqrt(scale) * (((float) Math.random() * 150f * weapon.getChargeLevel()) + 50f),
                            weapon.getChargeLevel() * 0.3f * scale, 0.25f, GLOW_COLOR);

                    float count = (amount * scale * 40f * (weapon.getChargeLevel() + 1f));
                    if (Math.random() < (count - Math.floor(count))) {
                        count += 1f;
                    }
                    for (int i = 0; i < (int) count; i++) {
                        Vector2f point = MathUtils.getPointOnCircumference(origin, scale * ((weapon.getChargeLevel() * 75f) + ((float) Math.random() * 50f)),
                                (float) Math.random() * 360f);
                        engine.addSmoothParticle(point, ZERO, (float) Math.sqrt(scale) * ((5f * weapon.getChargeLevel()) + 5f),
                                ((weapon.getChargeLevel() * 0.3f) + 0.2f) * scale, 0.3f, FRINGE_COLOR);
                    }

                    if (Math.random() < amount * 5f * scale) {
                        for (WeaponAPI blaster : blasters) {
                            Vector2f point = MathUtils.getPointOnCircumference(origin,
                                    scale * weapon.getChargeLevel() * 10f,
                                    (float) Math.random() * 360f);
                            engine.spawnEmpArc(ship, blaster.getLocation(), ship, new AnchoredEntity(ship, point),
                                    DamageType.ENERGY, 0, 0, 0, null, (weapon.getChargeLevel() * 5f) + 5f,
                                    FRINGE_COLOR, GLOW_COLOR);
                        }
                    }
                }
            } else {
                firing = true;
            }
        } else {
            if (cooling) {
                if (weapon.getChargeLevel() <= 0f) {
                    cooling = false;
                }
            } else if ((weapon.getChargeLevel() > level) && (ship.getFluxTracker().getFluxLevel() < 0.98f) && (weapon.getChargeLevel() < 0.9f)) {
                charging = true;
                sound = Global.getSoundPlayer().playSound("ii_lightspear_charge", 1f * overlevel, scale, origin, weapon.getShip().getVelocity());
            }
        }
        level = weapon.getChargeLevel();
    }
}
