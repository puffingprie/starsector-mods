package data.scripts.campaign.terrain;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.EnumSet;

public class II_TachyonFieldTerrainPlugin extends BaseRingTerrain {

    public static float BURN_MULT_MAX = 1.5f;
    public static final Color TACHYON_COLOR = new Color(165, 100, 255);
    public static final Color TACHYON_ENGINE = new Color(80, 50, 120, 100);

    protected final FaderUtil fader = new FaderUtil(1f, 1f, 1f, true, true);
    protected TachyonFieldParams tachParams;

    @Override
    public void advance(float amount) {
        super.advance(amount);
        fader.advance(amount);
    }

    @Override
    public void applyEffect(SectorEntityToken entity, float days) {
        if (entity instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) entity;

            float effect = getEffectMult(fleet);
            fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_1", "Inside tachyon field", 1f + (BURN_MULT_MAX
                    - 1f) * effect,
                    fleet.getStats().getFleetwideMaxBurnMod());

            for (FleetMemberViewAPI view : fleet.getViews()) {
                view.getContrailColor().shift(getModId(), TACHYON_ENGINE, 1f, 1f, effect * 0.75f);
                view.getEngineGlowSizeMult().shift(getModId(), 1f + effect * 0.75f, 1f, 1f, 1f);
                view.getEngineHeightMult().shift(getModId(), 1f + effect * 2f, 1f, 1f, 1f);
                view.getEngineWidthMult().shift(getModId(), 1f + effect * 1f, 1f, 1f, 1f);
            }
        }
    }

    @Override
    public boolean canPlayerHoldStationIn() {
        return false;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        float pad = 10f;
        float small = 5f;
        Color highlight = Misc.getHighlightColor();

        tooltip.addTitle("Tachyon Field");
        tooltip.addPara(Global.getSettings().getDescription(getTerrainId(), Type.TERRAIN).getText1(), pad);

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();

        float burnMult = 1f + (BURN_MULT_MAX - 1f) * getEffectMult(player);

        String extra = "";
        float nextPad = pad;
        if (expanded) {
            extra = " This effect increases the closer your fleet is to the local barycenter.";
            tooltip.addSectionHeading("Travel", Alignment.MID, pad);
            nextPad = small;
        }

        tooltip.addPara("Your fleet's speed is increased by %s." + extra, pad,
                highlight, "" + (int) ((burnMult - 1f) * 100) + "%"
        );

        if (expanded) {
            tooltip.addSectionHeading("Combat", Alignment.MID, pad);
            tooltip.addPara("No combat effects.", nextPad);
        }
    }

    @Override
    public String getEffectCategory() {
        return "tachyon_field-like";
    }

    @Override
    public Color getNameColor() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        float mult = getEffectMult(player);
        fader.setDuration(1f / (0.25f + mult), 1f / (0.25f + mult));
        return Misc.interpolateColor(super.getNameColor(), Misc.interpolateColor(super.getNameColor(), TACHYON_COLOR,
                mult), fader.getBrightness() * 1f);
    }

    @Override
    public String getTerrainName() {
        return tachParams.name;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void init(String terrainId, SectorEntityToken entity, Object param) {
        super.init(terrainId, entity, param);
        this.tachParams = (TachyonFieldParams) param;
        if (tachParams.name == null) {
            tachParams.name = "Tachyon Field";
        }
        fader.fadeOut();
        readResolve();
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }

    protected float getEffectMult(SectorEntityToken entity) {
        float distFromEdge = Misc.getDistance(entity.getLocation(), this.entity.getLocation()) - tachParams.innerRadius;
        return Math.max(0f, 1f - (distFromEdge / (tachParams.outerRadius - tachParams.innerRadius)));
    }

    @Override
    protected Object readResolve() {
        layers = EnumSet.noneOf(CampaignEngineLayers.class);
        return this;
    }

    Object writeReplace() {
        return this;
    }

    public static class TachyonFieldParams extends RingParams {

        public float innerRadius;
        public float outerRadius;

        public TachyonFieldParams(float bandWidthInEngine, float middleRadius, SectorEntityToken relatedEntity) {
            super(bandWidthInEngine, middleRadius, relatedEntity);
            innerRadius = middleRadius - bandWidthInEngine / 2f;
            outerRadius = middleRadius + bandWidthInEngine / 2f;
        }
    }
}
