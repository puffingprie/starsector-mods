package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.II_Util;
import java.awt.Color;
import org.dark.shaders.util.ShaderLib;

public abstract class II_BasePackage extends BaseHullMod {

    public static final String ARMOR_PACKAGE = "ii_armor_package";
    public static final String TARGETING_PACKAGE = "ii_targeting_package";
    public static final String ELITE_PACKAGE = "ii_elite_package";
    public static final String CORE_UPGRADES = "ii_core_upgrades";
    public static final String NO_PACKAGE = "ii_no_package";
    public static final String SWP_NO_PACKAGE = "swp_no_package";

    protected static final float PARA_PAD = 10f;
    protected static final float SECTION_PAD = 10f;
    protected static final float INTERNAL_PAD = 4f;
    protected static final float INTERNAL_PARA_PAD = 4f;
    protected static final float BULLET_PAD = 3f;

    private static final String ALT_SPRITES = "ii_alt_sprites";

    protected abstract String getHullModId();

    protected abstract String getAltSpriteSuffix();

    protected abstract void updateDecoWeapons(ShipAPI ship);

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (getAltSpriteSuffix() != null) {
            String spriteId = ship.getHullSpec().getBaseHullId() + getAltSpriteSuffix();
            SpriteAPI sprite;
            try {
                sprite = Global.getSettings().getSprite(ALT_SPRITES, spriteId, false);
            } catch (RuntimeException ex) {
                sprite = null;
            }

            if (sprite != null) {
                float x = ship.getSpriteAPI().getCenterX();
                float y = ship.getSpriteAPI().getCenterY();
                float alpha = ship.getSpriteAPI().getAlphaMult();
                float angle = ship.getSpriteAPI().getAngle();
                Color color = ship.getSpriteAPI().getColor();

                ship.setSprite(ALT_SPRITES, spriteId);
                ShaderLib.overrideShipTexture(ship, spriteId);

                ship.getSpriteAPI().setCenter(x, y);
                ship.getSpriteAPI().setAlphaMult(alpha);
                ship.getSpriteAPI().setAngle(angle);
                ship.getSpriteAPI().setColor(color);
            }
        }

        updateDecoWeapons(ship);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        updateDecoWeapons(ship);
    }

    protected abstract String getFlavorText();

    private void makeEmptySysModPostDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Improves and changes the behavior of the ship system when installed.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"));
        addEmptySysModText(tooltip);
    }

    protected abstract void addEmptySysModText(TooltipMakerAPI tooltip);

    private void makeEmptyMiscModPostDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("Potentially changes the behavior of other ship components when installed.",
                INTERNAL_PARA_PAD, Global.getSettings().getColor("standardTextColor"));
        addEmptyMiscModText(tooltip);
    }

    protected abstract void addEmptyMiscModText(TooltipMakerAPI tooltip);

    private void makeImperialFlaresPostDescription(TooltipMakerAPI tooltip) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/flare_launcher.png", 64f);
        addImperialFlaresSysModText(text);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addImperialFlaresSysModText(TooltipMakerAPI text);

    private void makeMicroForgePostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/missile_autoforge.png", 64f);
        addMicroForgeSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeTurbofeederPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/ammo_feeder.png", 64f);
        addTurbofeederSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeImpulseBoosterPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/infernium_injector.png", 64f);
        addImpulseBoosterSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeOverdrivePostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/maneuvering_jets.png", 64f);
        addOverdriveSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeMagnumSalvoPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/missile_racks.png", 64f);
        addMagnumSalvoSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeCommandCenterPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/reserve_deployment.png", 64f);
        addCommandCenterSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeShockBusterPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/quantum_disruptor.png", 64f);
        addShockBusterSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeCelerityDrivePostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/temporal_shell.png", 64f);
        addCelerityDriveSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeLuxFinisPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/high_energy_focus.png", 64f);
        addLuxFinisSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeArbalestLoaderPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/hullsys/emp_emitter.png", 64f);
        addArbalestLoaderSysModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeCargoPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/skills/fleet_logistics.png", 64f);
        addCargoMiscModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeLightspearPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/skills/applied_physics.png", 64f);
        addLightspearMiscModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeTitanPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/skills/missile_specialization.png", 64f);
        addTitanMiscModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship);

    private void makeMagnaFulmenPostDescription(TooltipMakerAPI tooltip, ShipAPI ship) {
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/icons/skills/target_analysis.png", 64f);
        addMagnaFulmenMiscModText(text, ship);
        tooltip.addImageWithText(INTERNAL_PAD);
    }

    protected abstract void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship);

    protected void addCompatibilityStatement(TooltipMakerAPI tooltip) {
        LabelAPI label = tooltip.addPara("Only compatible with Imperium hulls. Only one Imperial Package can be installed.", PARA_PAD);
        label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"), Global.getSettings().getDesignTypeColor("Imperium"));
        label.setHighlight("Imperium", "Imperial Package");
    }

    protected abstract void addPrimaryDescription(TooltipMakerAPI tooltip);

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        addPrimaryDescription(tooltip);

        if (getHullModId().contentEquals(NO_PACKAGE)) {
            LabelAPI label = tooltip.addPara("Imperial Packages cannot be installed on this hull.", 0f);
            label.setHighlightColors(Global.getSettings().getDesignTypeColor("Imperium"));
            label.setHighlight("Imperial Packages");

            if (getFlavorText() != null) {
                label = tooltip.addPara(getFlavorText(), Misc.getGrayColor(), PARA_PAD);
                label.setAlignment(Alignment.MID);
            }
        } else {
            addCompatibilityStatement(tooltip);

            if (getFlavorText() != null) {
                LabelAPI label = tooltip.addPara(getFlavorText(), Misc.getGrayColor(), PARA_PAD);
                label.setAlignment(Alignment.MID);
            }

            LabelAPI heading = tooltip.addSectionHeading("System Mod",
                    Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);

            if (isForModSpec || (ship == null) || (ship.getSystem() == null)) {
                makeEmptySysModPostDescription(tooltip);
            } else {
                String shipSystemId = ship.getSystem().getId();
                switch (shipSystemId) {
                    case "ii_flares":
                        makeImperialFlaresPostDescription(tooltip);
                        break;
                    case "ii_microforge":
                        makeMicroForgePostDescription(tooltip, ship);
                        break;
                    case "ii_turbofeeder":
                        makeTurbofeederPostDescription(tooltip, ship);
                        break;
                    case "ii_impulsebooster":
                        makeImpulseBoosterPostDescription(tooltip, ship);
                        break;
                    case "ii_overdrive":
                        makeOverdrivePostDescription(tooltip, ship);
                        break;
                    case "ii_magnumsalvo":
                    case "ii_magnumsalvo_station":
                        makeMagnumSalvoPostDescription(tooltip, ship);
                        break;
                    case "ii_commandcenter":
                        makeCommandCenterPostDescription(tooltip, ship);
                        break;
                    case "ii_shockbuster":
                        makeShockBusterPostDescription(tooltip, ship);
                        break;
                    case "ii_celeritydrive":
                        makeCelerityDrivePostDescription(tooltip, ship);
                        break;
                    case "ii_luxfinis":
                        makeLuxFinisPostDescription(tooltip, ship);
                        break;
                    case "ii_arbalestloader":
                        makeArbalestLoaderPostDescription(tooltip, ship);
                        break;
                    default:
                        makeEmptySysModPostDescription(tooltip);
                        break;
                }
            }

            if (isForModSpec || (ship == null) || (ship.getSystem() == null)) {
                heading = tooltip.addSectionHeading("Miscellaneous Mod",
                        Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
                makeEmptyMiscModPostDescription(tooltip);
            } else {
                String shipId = II_Util.getNonDHullId(ship.getHullSpec());
                switch (shipId) {
                    case "ii_carrum":
                    case "ii_barrus":
                        heading = tooltip.addSectionHeading("Miscellaneous Mod",
                                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
                        makeCargoPostDescription(tooltip, ship);
                        break;
                    case "ii_adamas":
                        heading = tooltip.addSectionHeading("Miscellaneous Mod",
                                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
                        makeLightspearPostDescription(tooltip, ship);
                        break;
                    case "ii_olympus":
                        heading = tooltip.addSectionHeading("Miscellaneous Mod",
                                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
                        makeTitanPostDescription(tooltip, ship);
                        break;
                    case "ii_libritor":
                        heading = tooltip.addSectionHeading("Miscellaneous Mod",
                                Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), Global.getSettings().getColor("buttonBgDark"), Alignment.TMID, SECTION_PAD);
                        makeMagnaFulmenPostDescription(tooltip, ship);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if ((ship != null) && ship.getVariant().getHullMods().contains(ARMOR_PACKAGE) && !getHullModId().contentEquals(ARMOR_PACKAGE)) {
            return "Incompatible with Imperial Armor Package";
        }
        if ((ship != null) && ship.getVariant().getHullMods().contains(TARGETING_PACKAGE) && !getHullModId().contentEquals(TARGETING_PACKAGE)) {
            return "Incompatible with Imperial Targeting Package";
        }
        if ((ship != null) && ship.getVariant().getHullMods().contains(ELITE_PACKAGE) && !getHullModId().contentEquals(ELITE_PACKAGE)) {
            return "Incompatible with Imperial Elite Package";
        }
        if ((ship != null) && ship.getVariant().getHullMods().contains(CORE_UPGRADES) && !getHullModId().contentEquals(CORE_UPGRADES)) {
            return "Incompatible with Imperial Core Upgrades";
        }
        if ((ship != null) && (ship.getVariant().getHullMods().contains(NO_PACKAGE) || ship.getVariant().getHullMods().contains(SWP_NO_PACKAGE))
                && !(getHullModId().contentEquals(NO_PACKAGE) || getHullModId().contentEquals(SWP_NO_PACKAGE))) {
            return "Imperial Packages cannot be installed";
        }
        if ((ship != null) && !ship.getHullSpec().getHullId().startsWith("ii_")) {
            return "Must be installed on an Imperium ship";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) {
            return false;
        }
        if (ship.getVariant().getHullMods().contains(ARMOR_PACKAGE) && !getHullModId().contentEquals(ARMOR_PACKAGE)) {
            return false;
        }
        if (ship.getVariant().getHullMods().contains(TARGETING_PACKAGE) && !getHullModId().contentEquals(TARGETING_PACKAGE)) {
            return false;
        }
        if (ship.getVariant().getHullMods().contains(ELITE_PACKAGE) && !getHullModId().contentEquals(ELITE_PACKAGE)) {
            return false;
        }
        if (ship.getVariant().getHullMods().contains(CORE_UPGRADES) && !getHullModId().contentEquals(CORE_UPGRADES)) {
            return false;
        }
        if ((ship.getVariant().getHullMods().contains(NO_PACKAGE) || ship.getVariant().getHullMods().contains(SWP_NO_PACKAGE))
                && !(getHullModId().contentEquals(NO_PACKAGE) || getHullModId().contentEquals(SWP_NO_PACKAGE))) {
            return false;
        }
        return ship.getHullSpec().getHullId().startsWith("ii_");
    }

    @Override
    public Color getBorderColor() {
        return new Color(200, 200, 200);
    }

    @Override
    public Color getNameColor() {
        return new Color(200, 200, 200);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        if (ship == null) {
            return false;
        }
        return ship.getHullSpec().getHullId().startsWith("ii_");
    }
}
