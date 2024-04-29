package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.everyframe.II_BlockedHullmodDisplayScript;
import data.scripts.util.II_Util;
import data.scripts.weapons.II_BlinkerEveryFrame;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class II_TargetingPackage extends II_BasePackage {

    public static final float TURRET_TURN_MULT = 2f / 3f;
    public static final float SHOT_SPEED_MULT = 2f / 3f;
    public static final float AUTOAIM_BONUS = 1f / 3f;
    public static final float RECOIL_MULT = 2f / 3f;
    public static final float SPEED_PENALTY = 15f;
    public static final float ZFB_SPEED_BONUS = 30f;

    public static final float CARGO_MOD_CARGO_RATIO = 0.5f;

    public static final Map<HullSize, Float> RANGE_BONUS = new HashMap<>(4);
    public static final Map<HullSize, Float> RANGE_BONUS_PD = new HashMap<>(4);

    static {
        RANGE_BONUS.put(HullSize.FRIGATE, 150f);
        RANGE_BONUS.put(HullSize.DESTROYER, 200f);
        RANGE_BONUS.put(HullSize.CRUISER, 250f);
        RANGE_BONUS.put(HullSize.CAPITAL_SHIP, 300f);

        RANGE_BONUS_PD.put(HullSize.FRIGATE, 125f);
        RANGE_BONUS_PD.put(HullSize.DESTROYER, 150f);
        RANGE_BONUS_PD.put(HullSize.CRUISER, 175f);
        RANGE_BONUS_PD.put(HullSize.CAPITAL_SHIP, 200f);
    }

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("diableavionics_mount");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("swp_pdconversion");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS.get(hullSize));
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -RANGE_BONUS.get(hullSize));
        stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, RANGE_BONUS_PD.get(hullSize) - RANGE_BONUS.get(hullSize));

        stats.getWeaponTurnRateBonus().modifyMult(id, TURRET_TURN_MULT);
        stats.getProjectileSpeedMult().modifyMult(id, SHOT_SPEED_MULT);

        stats.getAutofireAimAccuracy().modifyFlat(id, AUTOAIM_BONUS);
        stats.getMaxRecoilMult().modifyMult(id, RECOIL_MULT);
        stats.getRecoilPerShotMult().modifyMult(id, RECOIL_MULT);

        stats.getMaxSpeed().modifyFlat(id, -SPEED_PENALTY);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZFB_SPEED_BONUS);

        if (stats.getVariant() != null) {
            String shipId = II_Util.getNonDHullId(stats.getVariant().getHullSpec());
            switch (shipId) {
                case "ii_carrum":
                case "ii_barrus": {
                    float cargoMod = stats.getVariant().getHullSpec().getCargo() - (stats.getVariant().getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
                    float fuelMod = cargoMod;
                    stats.getCargoMod().modifyFlat(id, -cargoMod);
                    stats.getFuelMod().modifyFlat(id, fuelMod);
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);

        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getNonBuiltInHullmods().contains(tmp) && !ship.getVariant().getSMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
                II_BlockedHullmodDisplayScript.showBlocked(ship);
            }
        }
    }

    @Override
    protected void addCompatibilityStatement(TooltipMakerAPI tooltip) {
        HullModSpecAPI dampenedMount = Global.getSettings().getHullModSpec("diableavionics_mount");
        HullModSpecAPI pdAssaultConversion = Global.getSettings().getHullModSpec("swp_pdconversion");
        HullModSpecAPI unstableInjector = Global.getSettings().getHullModSpec("unstable_injector");
        if (dampenedMount != null) {
            if (pdAssaultConversion != null) {
                LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed. Incompatible with "
                        + unstableInjector.getDisplayName() + ", " + dampenedMount.getDisplayName() + ", or " + pdAssaultConversion.getDisplayName() + ".", PARA_PAD);
                label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"),
                        Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
                label.setHighlight("Imperium", "Imperial Package", unstableInjector.getDisplayName(), dampenedMount.getDisplayName(), pdAssaultConversion.getDisplayName());
            } else {
                LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed. Incompatible with "
                        + unstableInjector.getDisplayName() + " or " + dampenedMount.getDisplayName() + ".", PARA_PAD);
                label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"),
                        Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
                label.setHighlight("Imperium", "Imperial Package", unstableInjector.getDisplayName(), dampenedMount.getDisplayName());
            }
        } else {
            if (pdAssaultConversion != null) {
                LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed. Incompatible with "
                        + unstableInjector.getDisplayName() + " or " + pdAssaultConversion.getDisplayName() + ".", PARA_PAD);
                label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"),
                        Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor());
                label.setHighlight("Imperium", "Imperial Package", unstableInjector.getDisplayName(), pdAssaultConversion.getDisplayName());
            } else {
                LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed. Incompatible with "
                        + unstableInjector.getDisplayName() + ".", PARA_PAD);
                label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"),
                        Misc.getNegativeHighlightColor());
                label.setHighlight("Imperium", "Imperial Package", unstableInjector.getDisplayName());
            }
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        String reason = super.getUnapplicableReason(ship);
        if (reason != null) {
            if ((ship != null) && ship.getVariant().getHullMods().contains("diableavionics_mount")) {
                HullModSpecAPI dampenedMount = Global.getSettings().getHullModSpec("diableavionics_mount");
                return "Incompatible with " + dampenedMount.getDisplayName();
            }
            if ((ship != null) && ship.getVariant().getHullMods().contains("swp_pdconversion")) {
                HullModSpecAPI pdAssaultConversion = Global.getSettings().getHullModSpec("swp_pdconversion");
                return "Incompatible with " + pdAssaultConversion.getDisplayName();
            }
            if ((ship != null) && ship.getVariant().getHullMods().contains("unstable_injector")) {
                HullModSpecAPI unstableInjector = Global.getSettings().getHullModSpec("unstable_injector");
                return "Incompatible with " + unstableInjector.getDisplayName();
            }
        }
        return reason;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (!super.isApplicableToShip(ship)) {
            return false;
        }
        return !((ship != null) && (ship.getVariant().getHullMods().contains("diableavionics_mount")
                || ship.getVariant().getHullMods().contains("swp_pdconversion") || ship.getVariant().getHullMods().contains("unstable_injector")));
    }

    @Override
    protected String getHullModId() {
        return TARGETING_PACKAGE;
    }

    @Override
    protected String getAltSpriteSuffix() {
        return "_targeting";
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

            if (weapon.getId().startsWith("ii_targeting_blinker")) {
                II_BlinkerEveryFrame plugin = (II_BlinkerEveryFrame) weapon.getEffectPlugin();
                if (plugin.t <= 0.0) {
                    weapon.getAnimation().setFrame(2);
                    weapon.getSprite().setNormalBlend();
                    weapon.setCurrAngle(0f);
                }
            }

            switch (weapon.getId()) {
                case "ii_titan_deco":
                    if (!titanWeaponHasAmmo) {
                        frame = 4;
                    } else {
                        frame = 2;
                    }
                    break;
                case "ii_titan_armor_door":
                    frame = 0;
                    break;
                case "ii_titan_targeting_door":
                    if (!titanWeaponHasAmmo) {
                        frame = 0;
                    } else {
                        frame = 1;
                    }
                    break;
                default:
                    continue;
            }

            weapon.getAnimation().setFrame(frame);
        }
    }

    @Override
    protected String getFlavorText() {
        return "Banks of sensor payloads and computing clusters spill out over the hull, supplementing existing "
                + "targeting systems. Weapon emplacements are retrofitted with advanced stabilizers that further improve "
                + "accuracy and extend usable range, at the cost of burdening turret servos. In addition to down-tuning "
                + "combat thrusters to avoid interference, this modification suite has the downside of increasing "
                + "projectile predictability, making it difficult to strike nimble ships.";
    }

    @Override
    protected void addPrimaryDescription(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Projectile non-PD weapon range %s/%s/%s/%s (flat, by hull size)", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + RANGE_BONUS.get(HullSize.FRIGATE).intValue(), "+" + RANGE_BONUS.get(HullSize.DESTROYER).intValue(), "+" + RANGE_BONUS.get(HullSize.CRUISER).intValue(), "+" + RANGE_BONUS.get(HullSize.CAPITAL_SHIP).intValue());
        bullet = tooltip.addPara("Projectile PD weapon range %s/%s/%s/%s (flat, by hull size)", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + RANGE_BONUS_PD.get(HullSize.FRIGATE).intValue(), "+" + RANGE_BONUS_PD.get(HullSize.DESTROYER).intValue(), "+" + RANGE_BONUS_PD.get(HullSize.CRUISER).intValue(), "+" + RANGE_BONUS_PD.get(HullSize.CAPITAL_SHIP).intValue());
        bullet = tooltip.addPara("Top speed %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round(SPEED_PENALTY));
        bullet = tooltip.addPara("Zero flux boost speed %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(ZFB_SPEED_BONUS));
        bullet = tooltip.addPara("Projectile speed %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round((1f - SHOT_SPEED_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Weapon recoil %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "-" + Math.round((1f - RECOIL_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Target leading %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(AUTOAIM_BONUS * 100f) + "%");
        bullet = tooltip.addPara("Non-beam turret turn rate %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round((1f - TURRET_TURN_MULT) * 100f) + "%");
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptySysModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Celerity Drive (Rift Shunts)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Command Center (Targeting Link)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Imperial Flares (Active)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Impulse Booster (Time Alter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lux Finis (Impetus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magnum Salvo (Kinetic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Micro-Forge (Extended)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Overdrive (Maximum Firepower)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Shock Buster (Empowered)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Turbofeeder (Recalibrated)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Arbalest Loader (Gravitic Shell)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptyMiscModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Cargo (Tanker Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lightspear (Lensed)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Titan (Apocalypse MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magna Fulmen (Repulsor)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addImperialFlaresSysModText(TooltipMakerAPI text) {
        text.addTitle("Imperial Flares (Active)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Launches %s as many flares, but the flares %s missiles.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "half", "actively seek");
    }

    @Override
    protected void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Micro-Forge (Extended)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system can be used %s instead of once.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "twice");
    }

    @Override
    protected void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Turbofeeder (Recalibrated)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also %s projectile speed while active.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "doubles");
    }

    @Override
    protected void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Impulse Booster (Time Alter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also briefly %s, but %s when used.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "accelerates time", "generates flux");
    }

    @Override
    protected void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Overdrive (Maximum Firepower)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Increases rate of fire by %s instead of 50%% and decreases gauge usage by %s. Also %s projectile speed and non-beam turret turn rate while active. %s speed or maneuverability.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "100%", "25%", "doubles", "Does not increase");
    }

    @Override
    protected void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magnum Salvo (Kinetic)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Enhanced Apocalypse MRMs are %s missiles that deal %s damage.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "two-stage", "kinetic");
    }

    @Override
    protected void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Command Center (Targeting Link)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s mode also increases fighter projectile range by a flat %s and increases fighter projectile speed by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Speed", "250", "50%");
    }

    @Override
    protected void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Shock Buster (Empowered)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system has %s targeting range and %s effectiveness, but has %s charges instead of three. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "greater", "increased", "two", "Increased");
    }

    @Override
    protected void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Celerity Drive (Rift Shunts)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also reduces weapon flux cost by up to %s while active.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "67%");
    }

    @Override
    protected void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lux Finis (Impetus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Increases flux capacity and dissipation by %s instead of 25%% and decreases gauge usage by %s. Also increases rate of fire by %s while active. %s speed or maneuverability.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "50%", "25%", "50%", "Does not increase");
    }

    @Override
    protected void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Arbalest Loader (Gravitic Shell)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Using the system charges Magna Fulmen to fire a %s projectile that detonates on impact with %s. The system has %s charges instead of three. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "slow", "extreme repulsive force", "two", "Increased");
    }

    @Override
    protected void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        float cargoMod = ship.getHullSpec().getCargo() - (ship.getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
        float fuelMod = cargoMod;
        text.addTitle("Cargo (Tanker Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Maximum cargo capacity is decreased by %s, but maximum fuel capacity is increased by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "" + (int) Math.round(cargoMod), "" + (int) Math.round(fuelMod));
    }

    @Override
    protected void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lightspear (Lensed)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Lightspear range is increased by a flat %s units.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "300");
    }

    @Override
    protected void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Titan (Apocalypse MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Titan is equipped with long-ranged %s missiles, dealing %s DPS for approximately %s seconds before reaching a sustained %s DPS.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Apocalypse-class", "1900", "60", "950");
    }

    @Override
    protected void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magna Fulmen (Repulsor)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Magna Fulmen %s when it hits, propelling the target away.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "inflicts knockback");
    }

    @Override
    public Color getBorderColor() {
        return new Color(150, 229, 250);
    }

    @Override
    public Color getNameColor() {
        return new Color(150, 229, 250);
    }
}
