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
import data.scripts.shipsystems.II_CelerityDriveStats;
import data.scripts.shipsystems.II_MicroForgeStats;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class II_ArmorPackage extends II_BasePackage {

    public static final float ARMOR_AMOUNT_MULT = 2f;
    public static final float ARMOR_STRENGTH_MULT = 0.5f;
    public static final float ARMOR_MAX_REDUCTION_PENALTY = 0.15f;
    public static final float SUBSYSTEM_HEALTH_BONUS = 50f;
    public static final float EMP_TAKEN_MULT = 0.5f;
    public static final float ACCEL_MULT = 2f / 3f;
    public static final float ARMOR_BONUS = 150f;

    public static final float CARGO_MOD_CARGO_RATIO = 0.5f;

    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(1);

    static {
        BLOCKED_HULLMODS.add("diableavionics_mount");
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyFlat(id, Math.round(ARMOR_BONUS / ARMOR_AMOUNT_MULT));
        stats.getArmorBonus().modifyMult(id, ARMOR_AMOUNT_MULT);
        stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_STRENGTH_MULT);
        stats.getMinArmorFraction().modifyMult(id, ARMOR_STRENGTH_MULT);
        stats.getMaxArmorDamageReduction().modifyFlat(id, -ARMOR_MAX_REDUCTION_PENALTY);
        stats.getEmpDamageTakenMult().modifyMult(id, EMP_TAKEN_MULT);
        stats.getWeaponHealthBonus().modifyPercent(id, SUBSYSTEM_HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyPercent(id, SUBSYSTEM_HEALTH_BONUS);
        stats.getAcceleration().modifyMult(id, ACCEL_MULT);
        stats.getDeceleration().modifyMult(id, ACCEL_MULT);
        stats.getTurnAcceleration().modifyMult(id, ACCEL_MULT);

        if (stats.getVariant() != null) {
            String shipId = II_Util.getNonDHullId(stats.getVariant().getHullSpec());
            switch (shipId) {
                case "ii_carrum":
                case "ii_barrus": {
                    float cargoMod = stats.getVariant().getHullSpec().getCargo() - (stats.getVariant().getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
                    float crewMod = cargoMod;
                    stats.getCargoMod().modifyFlat(id, -cargoMod);
                    stats.getMaxCrewMod().modifyFlat(id, crewMod);
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
        if (dampenedMount != null) {
            LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed. Incompatible with " + dampenedMount.getDisplayName() + ".", PARA_PAD);
            label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"), Misc.getNegativeHighlightColor());
            label.setHighlight("Imperium", "Imperial Package", dampenedMount.getDisplayName());
        } else {
            super.addCompatibilityStatement(tooltip);
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        String reason = super.getUnapplicableReason(ship);
        if (reason != null) {
            HullModSpecAPI dampenedMount = Global.getSettings().getHullModSpec("diableavionics_mount");
            if ((ship != null) && ship.getVariant().getHullMods().contains("diableavionics_mount")) {
                return "Incompatible with " + dampenedMount.getDisplayName();
            }
        }
        return reason;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (!super.isApplicableToShip(ship)) {
            return false;
        }
        return !((ship != null) && ship.getVariant().getHullMods().contains("diableavionics_mount"));
    }

    @Override
    protected String getHullModId() {
        return ARMOR_PACKAGE;
    }

    @Override
    protected String getAltSpriteSuffix() {
        return "_armor";
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

            switch (weapon.getId()) {
                case "ii_titan_deco":
                    if (!titanWeaponHasAmmo) {
                        frame = 4;
                    } else {
                        frame = 1;
                    }
                    break;
                case "ii_titan_armor_door":
                    if (!titanWeaponHasAmmo) {
                        frame = 0;
                    } else {
                        frame = 1;
                    }
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
        return "Standard Imperium armor is replaced with bulky layers of ablative plating, while weaker sections and "
                + "vulnerable subsystems are reinforced with supplemental protection. This modification suite sacrifices "
                + "maneuverability due to the increased mass, but greatly reduces the danger of traditionally high-risk "
                + "threats like high explosive missiles.";
    }

    @Override
    protected void addPrimaryDescription(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Armor points %s and %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "doubled", "+" + Math.round(ARMOR_BONUS));
        bullet = tooltip.addPara("Effective armor strength for damage reduction %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "halved");
        bullet = tooltip.addPara("Maximum armor damage reduction reduced to %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "" + Math.round(100f * (Global.getSettings().getFloat("maxArmorDamageReduction") - ARMOR_MAX_REDUCTION_PENALTY)) + "%");
        bullet = tooltip.addPara("EMP damage taken %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "-" + Math.round((1f - EMP_TAKEN_MULT) * 100f) + "%");
        bullet = tooltip.addPara("Weapon and engine health %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getPositiveHighlightColor(),
                "+" + Math.round(SUBSYSTEM_HEALTH_BONUS) + "%");
        bullet = tooltip.addPara("Maneuverability %s", BULLET_PAD, Global.getSettings().getColor("standardTextColor"), Misc.getNegativeHighlightColor(),
                "-" + Math.round((1f - ACCEL_MULT) * 100f) + "%");
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptySysModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Celerity Drive (Temporal Damper)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Command Center (Micro-Damper)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Imperial Flares (Enhanced)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Impulse Booster (Jump-Starter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lux Finis (Munitus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magnum Salvo (Fundae MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Micro-Forge (Damper Field)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Overdrive (Safety Limiter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Shock Buster (Burst)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Turbofeeder (Lazarus Mounts)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Arbalest Loader (Split Shot)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addEmptyMiscModText(TooltipMakerAPI tooltip) {
        LabelAPI bullet;
        tooltip.setBulletedListMode("    • ");
        bullet = tooltip.addPara("Cargo (Transport Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Lightspear (Hardened)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Titan (Fundae MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        bullet = tooltip.addPara("Magna Fulmen (Smart Flak)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), BULLET_PAD);
        tooltip.setBulletedListMode(null);
    }

    @Override
    protected void addImperialFlaresSysModText(TooltipMakerAPI text) {
        text.addTitle("Imperial Flares (Enhanced)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Launches %s more flares, which linger for %s as long and have a %s larger effective radius.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "50%", "twice", "50%");
    }

    @Override
    protected void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship) {
        int reductionPct = Math.round(100f * (1f - II_MicroForgeStats.ARMOR_DAMAGE_REDUCTION.get(ship.getHullSize())));
        text.addTitle("Micro-Forge (Damper Field)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also reduces damage taken by %s while active. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "" + reductionPct + "%", "Reduced");
    }

    @Override
    protected void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Turbofeeder (Lazarus Mounts)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s all weapons when activated. Also decreases weapon damage taken by %s while active.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Instantly repairs", "75%");
    }

    @Override
    protected void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Impulse Booster (Jump-Starter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s all engines when activated. Also briefly decreases engine damage taken by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Instantly repairs", "75%");
    }

    @Override
    protected void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Overdrive (Safety Limiter)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system %s and %s at 0%% gauge. Degrades combat readiness at %s the normal rate instead of 300%%. Increases gauge recovery rate by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "does not redline", "does not overload", "200%", "50%");
    }

    @Override
    protected void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magnum Salvo (Fundae MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Enhanced Apocalypse MRMs %s into %s Fundae SRMs.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "split", "four");
    }

    @Override
    protected void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Command Center (Micro-Damper)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("%s mode also reduces fighter damage taken by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Refit", "25%");
    }

    @Override
    protected void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Shock Buster (Burst)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The system %s, but has %s targeting range, %s effectiveness against ships, and %s charges instead of three. %s range against missiles. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "hits all nearby hostiles", "lower", "reduced", "two", "Increased", "Increased");
    }

    @Override
    protected void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
        int reductionPct = Math.round(100f * (1f - II_CelerityDriveStats.ARMOR_DAMAGE_REDUCTION.get(ship.getHullSize())));
        text.addTitle("Celerity Drive (Temporal Damper)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Also reduces damage taken by %s while charging up.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "" + reductionPct + "%");
    }

    @Override
    protected void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lux Finis (Munitus)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Reduces damage by %s instead of 67%% while the Lightspear is firing.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "90%");
    }

    @Override
    protected void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Arbalest Loader (Split Shot)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Using the system charges Magna Fulmen to fire %s in a cone. The system has %s charges instead of three. %s flux cost.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "four additional smaller projectiles", "two", "Greatly increased");
    }

    @Override
    protected void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        float cargoMod = ship.getHullSpec().getCargo() - (ship.getHullSpec().getCargo() * CARGO_MOD_CARGO_RATIO);
        float crewMod = cargoMod;
        text.addTitle("Cargo (Transport Conversion)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Maximum cargo capacity is decreased by %s, but maximum crew capacity is increased by %s.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "" + (int) Math.round(cargoMod), "" + (int) Math.round(crewMod));
    }

    @Override
    protected void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Lightspear (Hardened)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("Lightspear performance drops by %s instead of 30%% per disabled Photon Blaster.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "15%");
    }

    @Override
    protected void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Titan (Fundae MIRV)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Titan is equipped with medium-ranged %s missiles, dealing %s HE-DPS and %s frag-DPS for approximately %s seconds before reaching a sustained %s HE-DPS and %s frag-DPS.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "Fundae-class", "768", "3360", "60", "384", "1680");
    }

    @Override
    protected void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship) {
        text.addTitle("Magna Fulmen (Smart Flak)", Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"));
        text.addPara("The Magna Fulmen %s when it hits, dealing extra fragmentation damage to nearby enemy ships, fighters, and missiles.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"), Global.getSettings().getColor("hColor"), "detonates");
    }

    @Override
    public Color getBorderColor() {
        return new Color(255, 225, 150);
    }

    @Override
    public Color getNameColor() {
        return new Color(255, 225, 150);
    }
}
