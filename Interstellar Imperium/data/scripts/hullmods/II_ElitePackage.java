package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.II_ImpulseBoosterStats;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_EliteLightsEveryFrame;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class II_ElitePackage extends II_BasePackage {

    public static final float ROF_BONUS = 25f;
    public static final float RANGE_MULT = 0.8f;
    public static final float FLUX_MULT = 1.5f;
    public static final float SHIELD_STRENGTH_MULT = 1f / FLUX_MULT;
    public static final float OVERLOAD_TIME_MULT = 0.5f;
    public static final float MANEUVERABILITY_BONUS = 0.5f;

    public static final float CARGO_MOD_CARGO_RATIO = 0.5f;

    public static final float CR_PENALTY = 0.5f;
    public static final Map<HullSize, Float> SPEED_BONUS = new HashMap<>(4);
    public static final Map<HullSize, Float> SPEED_PENALTY = new HashMap<>(4);
    public static final Map<HullSize, Float> SMOD_DP_INCREASE = new HashMap<>(4);

    static {
        SPEED_BONUS.put(HullSize.FRIGATE, 40f);
        SPEED_BONUS.put(HullSize.DESTROYER, 30f);
        SPEED_BONUS.put(HullSize.CRUISER, 20f);
        SPEED_BONUS.put(HullSize.CAPITAL_SHIP, 15f);

        SPEED_PENALTY.put(HullSize.FRIGATE, 20f);
        SPEED_PENALTY.put(HullSize.DESTROYER, 15f);
        SPEED_PENALTY.put(HullSize.CRUISER, 10f);
        SPEED_PENALTY.put(HullSize.CAPITAL_SHIP, 10f);

        SMOD_DP_INCREASE.put(HullSize.FRIGATE, 1f);
        SMOD_DP_INCREASE.put(HullSize.DESTROYER, 2f);
        SMOD_DP_INCREASE.put(HullSize.CRUISER, 3f);
        SMOD_DP_INCREASE.put(HullSize.CAPITAL_SHIP, 5f);
    }

    private static final String DATA_KEY = "II_ElitePackage";

    private static final Color ENGINE_COLOR = new Color(190, 150, 230, 160);
    private static final Color ENGINE_CONTRAIL_COLOR = new Color(75, 60, 90, 40);
    private static final Color ENGINE_SO_COLOR = new Color(235, 170, 235, 200);
    private static final Color ENGINE_SO_CONTRAIL_COLOR = new Color(110, 65, 110, 50);
    private static final Color VENT_CORE_COLOR = new Color(255, 255, 255, 255);
    private static final Color VENT_FRINGE_COLOR = new Color(205, 0, 175, 235);
    private static final Color SHIELD_INNER_COLOR = new Color(210, 145, 240, 80);
    private static final Color SHIELD_RING_COLOR = new Color(255, 255, 255, 235);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Object[]> uiKey = localData.uiKey;

        if (ship.getVariant().getHullMods().contains(HullMods.SAFETYOVERRIDES)) {
            ship.getEngineController().fadeToOtherColor(this, ENGINE_SO_COLOR, ENGINE_SO_CONTRAIL_COLOR, 1f, 1f);
        } else {
            ship.getEngineController().fadeToOtherColor(this, ENGINE_COLOR, ENGINE_CONTRAIL_COLOR, 1f, 1f);
        }
        ship.setVentCoreColor(VENT_CORE_COLOR);
        ship.setVentFringeColor(VENT_FRINGE_COLOR);
        if (ship.getShield() != null) {
            ship.getShield().setInnerColor(SHIELD_INNER_COLOR);
            ship.getShield().setRingColor(SHIELD_RING_COLOR);
        }

        float forwardDir = ship.getFacing();
        float currDir = VectorUtils.getFacing(ship.getVelocity());
        float reverseScale = Math.abs(MathUtils.getShortestRotation(currDir, forwardDir) / 90f);
        if (ship.getVelocity().length() < 1f) {
            reverseScale = 0f;
        }
        float fullBonus = SPEED_BONUS.get(ship.getHullSize());
        float fullPenalty = SPEED_PENALTY.get(ship.getHullSize());
        float penalty;
        if (reverseScale > 1f) {
            /* Going backwards */
            penalty = fullBonus + ((reverseScale - 1f) * fullPenalty);
        } else {
            /* Going forwards */
            penalty = reverseScale * fullBonus;
        }
        ship.getMutableStats().getMaxSpeed().modifyFlat("II_ElitePackage_penalty", -penalty);
        if (ship == engine.getPlayerShip()) {
            if (!uiKey.containsKey(ship)) {
                Object[] array = new Object[1];
                for (int i = 0; i < array.length; i++) {
                    array[i] = new Object();
                }
                uiKey.put(ship, array);
            }

            if (penalty > fullBonus) {
                engine.maintainStatusForPlayerShip(uiKey.get(ship)[0], "graphics/icons/tactical/engine_boost.png",
                        "Elite Package", "-" + Math.round(penalty - fullBonus) + " su/second", true);
            } else {
                engine.maintainStatusForPlayerShip(uiKey.get(ship)[0], "graphics/icons/tactical/engine_boost.png",
                        "Elite Package", "+" + Math.round(fullBonus - penalty) + " su/second", false);
            }
        }

        if (penalty < fullBonus) {
            ship.getEngineController().extendFlame(this, 0f, 0.25f, 0.25f);
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().modifyPercent(id, ROF_BONUS);
        stats.getMissileRoFMult().modifyPercent(id, ROF_BONUS);
        stats.getEnergyRoFMult().modifyPercent(id, ROF_BONUS);
        stats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_MULT);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_MULT);

        stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
        stats.getFluxDissipation().modifyMult(id, FLUX_MULT);

        stats.getShieldDamageTakenMult().modifyMult(id, 1f / SHIELD_STRENGTH_MULT);
        stats.getPhaseCloakActivationCostBonus().modifyMult(id, 1f / SHIELD_STRENGTH_MULT);
        stats.getPhaseCloakUpkeepCostBonus().modifyMult(id, 1f / SHIELD_STRENGTH_MULT);

        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_TIME_MULT);

        stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS.get(hullSize));

        stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
        stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS * 100f);

        stats.getPeakCRDuration().modifyMult(id, 1f - CR_PENALTY);
        stats.getCRLossPerSecondPercent().modifyMult(id, 1f + CR_PENALTY);

        if (isSMod(stats)) {
            stats.getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).modifyFlat(id, SMOD_DP_INCREASE.get(hullSize));
            stats.getSuppliesToRecover().modifyFlat(id, SMOD_DP_INCREASE.get(hullSize));
        }

        if (stats.getVariant() != null) {
            String shipId = II_Util.getNonDHullId(stats.getVariant().getHullSpec());
            switch (shipId) {
                case "ii_carrum":
                case "ii_barrus": {
                    float cargoMod = stats.getVariant().getHullSpec().getCargo() - (stats.getVariant().getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
                    float fuelMod = cargoMod / 2f;
                    float crewMod = cargoMod / 2f;
                    stats.getCargoMod().modifyFlat(id, -cargoMod);
                    stats.getFuelMod().modifyFlat(id, fuelMod);
                    stats.getMaxCrewMod().modifyFlat(id, crewMod);
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    protected String getHullModId() {
        return ELITE_PACKAGE;
    }

    @Override
    protected String getAltSpriteSuffix() {
        return "_elite";
    }

    @Override
    protected void updateDecoWeapons(ShipAPI ship) {
        boolean titanWeaponHasAmmo = false;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getId().contentEquals("ii_titan_w")) {
                if (weapon.getAmmo() > 0) {
                    titanWeaponHasAmmo = true;
                }
                break;
            }
        }
        float CR = ship.getCurrentCR();
        if (ship.getFleetMember() != null) {
            CR = ship.getFleetMember().getRepairTracker().getBaseCR();
        }
        if (CR < II_TitanBombardment.getCRPenalty(ship.getVariant())) {
            titanWeaponHasAmmo = false;
        }

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            int frame;

            if (weapon.getId().startsWith("ii_elite_lights_")) {
                II_EliteLightsEveryFrame plugin = (II_EliteLightsEveryFrame) weapon.getEffectPlugin();
                if (plugin.t <= 0.0) {
                    float alpha = 0.6f;
                    weapon.getAnimation().setFrame(2);
                    Color newColor = new Color(255, 255, 255,
                            II_Util.clamp255(Math.round(255f * alpha)));
                    weapon.getSprite().setAdditiveBlend();
                    weapon.getSprite().setColor(newColor);
                }
            }

            switch (weapon.getId()) {
                case "ii_titan_deco":
                    if (!titanWeaponHasAmmo) {
                        frame = 4;
                    } else {
                        frame = 3;
                    }
                    break;
                case "ii_titan_armor_door":
                    frame = 0;
                    break;
                case "ii_titan_targeting_door":
                    frame = 0;
                    break;
                default:
                    continue;
            }

            weapon.getAnimation().setFrame(frame);
        }
    }

    @Override
    protected String getFlavorText() {
        return "A radical overhaul of the ship's flux management scheme puts great strain on its systems, inflicting "
                + "a terrible penalty on long-term reliability. In return, offensive performance is broadly enhanced, "
                + "significantly boosting the ship's capacity for doing damage. This modification suite also tunes "
                + "thrusters for tight maneuvers and forward acceleration, sacrificing reverse-vectoring power.";
    }

    @Override
    public boolean isSModEffectAPenalty() {
        return true;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) Math.round(SMOD_DP_INCREASE.get(HullSize.FRIGATE));
        }
        if (index == 1) {
            return "" + (int) Math.round(SMOD_DP_INCREASE.get(HullSize.DESTROYER));
        }
        if (index == 2) {
            return "" + (int) Math.round(SMOD_DP_INCREASE.get(HullSize.CRUISER));
        }
        if (index == 3) {
            return "" + (int) Math.round(SMOD_DP_INCREASE.get(HullSize.CAPITAL_SHIP));
        }
        return null;
    }

    @Override
    protected void addPrimaryDescription(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Rate of fire %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(ROF_BONUS) + "%");
        bullet = tooltip.addPara("Weapon range %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round((1f - RANGE_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Flux capacity and dissipation %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round((FLUX_MULT - 1f) * 100f) + "%");
        bullet = tooltip.addPara("Shield/phase efficiency %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round((1f - SHIELD_STRENGTH_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Overload duration %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "-" + Math.round((1f - OVERLOAD_TIME_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Forward top speed %s/%s/%s/%s (by hull size)", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(SPEED_BONUS.get(HullSize.FRIGATE)), "+" + Math.round(SPEED_BONUS.get(HullSize.DESTROYER)), "+" + Math.round(SPEED_BONUS.get(HullSize.CRUISER)), "+" + Math.round(SPEED_BONUS.get(HullSize.CAPITAL_SHIP)));
        bullet = tooltip.addPara("Reverse top speed %s/%s/%s/%s (by hull size)", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round(SPEED_PENALTY.get(HullSize.FRIGATE)), "-" + Math.round(SPEED_PENALTY.get(HullSize.DESTROYER)), "-" + Math.round(SPEED_PENALTY.get(HullSize.CRUISER)), "-" + Math.round(SPEED_PENALTY.get(HullSize.CAPITAL_SHIP)));
        bullet = tooltip.addPara("Maneuverability %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(MANEUVERABILITY_BONUS * 100f) + "%");
        bullet = tooltip.addPara("Peak performance time %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round(CR_PENALTY * 100f) + "%");
        bullet = tooltip.addPara("CR degradation rate %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "+" + Math.round(CR_PENALTY * 100f) + "%");
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptySysModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Celerity Drive (Fast Charge)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Command Center (Turbo-Net)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Imperial Flares (Strike)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Impulse Booster (Ultra)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lux Finis (Infinitus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magnum Salvo (Sidewinder)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Micro-Forge (Flux Ejector)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Overdrive (Exceed)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Shock Buster (Unfettered)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Turbofeeder (Boost Jets)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Arbalest Loader (Supercharge)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptyMiscModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Cargo (Hybrid Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lightspear (Ionic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Titan (EMP)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magna Fulmen (Ionic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addImperialFlaresSysModText(TooltipMakerAPI text) {
        text.addTitle("Imperial Flares (Strike)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Launches %s as many flares, but the flares become %s that deal %s energy and %s EMP damage.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "half", "homing missiles", "200", "400");
    }

    @Override
    protected void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Micro-Forge (Flux Ejector)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s activation, and instantly %s when activated.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Faster", "resets flux");
    }

    @Override
    protected void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Turbofeeder (Boost Jets)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also increases %s and %s while active.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "speed", "maneuverability");
    }

    @Override
    protected void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        if (!II_ImpulseBoosterStats.USES_OVERRIDE.containsKey(ship.getHullSize())) {
            return;
        }
        int baseCharges = II_ImpulseBoosterStats.USES_OVERRIDE.get(ship.getHullSize());

        text.addTitle("Impulse Booster (Ultra)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system has %s charges instead of " + II_Util.NUM_NAMES[baseCharges] + ", and they regenerate %s more quickly. %s cooldown.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"),
                II_Util.NUM_NAMES[baseCharges * 2], Math.round(100f * (II_ImpulseBoosterStats.ELITE_REGEN_MULT.get(ship.getHullSize()) - 1f)) + "%", "Reduced");
    }

    @Override
    protected void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Overdrive (Exceed)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system redlines at %s gauge instead of 33%% gauge, and can be activated %s. Does not degrade %s unless redlined. Increases redline effects by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "50%", "while redlined", "combat readiness", "50%");
    }

    @Override
    protected void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magnum Salvo (Sidewinder)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Enhanced Apocalypse MRMs are %s missiles that deal %s and %s damage, %s to weapons and engines, and have a chance to %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "heat-seeking", "energy", "heavy EMP", "arc", "pierce shields");
    }

    @Override
    protected void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Command Center (Turbo-Net)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s mode also increases fighter rate of fire %s and reduces fighter flux use by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Attack", "25%", "20%");
    }

    @Override
    protected void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Shock Buster (Unfettered)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system has %s charges, but has %s targeting range and %s effectiveness. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "unlimited", "lower", "reduced", "Reduced");
    }

    @Override
    protected void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Celerity Drive (Fast Charge)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system charges up in %s the time, is active for %s of the time, and recharges in %s of the time.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "half", "two thirds", "one third");
    }

    @Override
    protected void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lux Finis (Infinitus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system %s at %s gauge for up to %s improved performance, and can be activated %s. Does not degrade %s unless redlined. Increased %s while redlined.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "redlines", "50%", "100%", "while redlined", "combat readiness", "Lightspear output");
    }

    @Override
    protected void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Arbalest Loader (Supercharge)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Using the system %s the Magna Fulmen's cooldown rather than eliminating it, but each use of the system charges the Magna Fulmen with %s, up to %s. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "shortens", "additional power", "four stacks", "Slightly increased");
    }

    @Override
    protected void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        float cargoMod = ship.getHullSpec().getCargo() - (ship.getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
        float fuelMod = cargoMod / 2f;
        float crewMod = cargoMod / 2f;
        text.addTitle("Cargo (Hybrid Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Maximum cargo capacity is decreased by %s, but maximum fuel capacity is increased by %s and maximum crew capacity is increased by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "" + (int) Math.round(cargoMod), "" + (int) Math.round(fuelMod), "" + (int) Math.round(crewMod));
    }

    @Override
    protected void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lightspear (Ionic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Lightspear %s when it hits, dealing extra EMP damage. Hits on shields have a chance to generate a shield-penetrating arc based on the target's hard flux level.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "arcs to weapons and engines");
    }

    @Override
    protected void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Titan (EMP)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Titan is equipped with an %s that deals %s, but inflicts %s and %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "EMP warhead", "reduced energy damage", "extreme EMP damage", "overloads nearby ships");
    }

    @Override
    protected void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magna Fulmen (Ionic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Magna Fulmen %s when it hits, dealing extra EMP damage. Hits on shields have a chance to generate a shield-penetrating arc based on the target's hard flux level.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "arcs to weapons and engines");
    }

    @Override
    public Color getBorderColor() {
        return new Color(225, 150, 255);
    }

    @Override
    public Color getNameColor() {
        return new Color(225, 150, 255);
    }

    private static final class LocalData {

        final Map<ShipAPI, Object[]> uiKey = new HashMap<>(50);
    }
}
