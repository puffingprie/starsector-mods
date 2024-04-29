package data.scripts.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class II_NoPackage extends II_BasePackage {

    @Override
    protected String getHullModId() {
        return NO_PACKAGE;
    }

    @Override
    protected String getAltSpriteSuffix() {
        return null;
    }

    @Override
    protected void updateDecoWeapons(ShipAPI ship) {
    }

    @Override
    protected String getFlavorText() {
        return "This hull is already too specialized to be upgraded with an Imperial modification suite.";
    }

    @Override
    public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
        return false;
    }

    @Override
    protected void addEmptySysModText(TooltipMakerAPI tooltip) {
    }

    @Override
    protected void addEmptyMiscModText(TooltipMakerAPI tooltip) {
    }

    @Override
    protected void addImperialFlaresSysModText(TooltipMakerAPI text) {
    }

    @Override
    protected void addMicroForgeSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addTurbofeederSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addImpulseBoosterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addOverdriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addMagnumSalvoSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCommandCenterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addShockBusterSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCelerityDriveSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addLuxFinisSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addArbalestLoaderSysModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addCargoMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addLightspearMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addTitanMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addMagnaFulmenMiscModText(TooltipMakerAPI text, ShipAPI ship) {
    }

    @Override
    protected void addPrimaryDescription(TooltipMakerAPI tooltip) {
    }
}
