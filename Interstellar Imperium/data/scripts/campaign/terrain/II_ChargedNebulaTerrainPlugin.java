package data.scripts.campaign.terrain;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import static com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.STORM_DAMAGE_FRACTION;
import static com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.STORM_MAX_STRIKE_DAMAGE;
import static com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.STORM_MAX_TIMEOUT;
import static com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.STORM_MIN_STRIKE_DAMAGE;
import static com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin.STORM_MIN_TIMEOUT;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl.NebulaTextureProvider;
import com.fs.starfarer.api.loading.Description.Type;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class II_ChargedNebulaTerrainPlugin extends HyperspaceTerrainPlugin implements NebulaTextureProvider {

    public static final float CHARGED_SENSOR_RANGE_MULT = 0.5f;
    public static final float CHARGED_STORM_SENSOR_RANGE_MULT = 0.5f;
    public static final float CHARGED_STORM_VISIBILITY_FLAT = 0f;

    @Override
    public void applyEffect(SectorEntityToken entity, float days) {
        if (entity instanceof CampaignFleetAPI) {
            CampaignFleetAPI fleet = (CampaignFleetAPI) entity;

            boolean inCloud = isInClouds(fleet);
            int[] tile = getTilePreferStorm(fleet.getLocation(), fleet.getRadius());
            CellStateTracker cell = null;
            if (tile != null) {
                cell = activeCells[tile[0]][tile[1]];
            }

            if (!inCloud || fleet.isInHyperspaceTransition()) {
                // open, do nothing
            } else {
                // deep
                fleet.getStats().removeTemporaryMod(getModId() + "_storm_sensor");
                if (CHARGED_SENSOR_RANGE_MULT != 1) {
                    fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_sensor",
                            "Inside charged nebula", CHARGED_SENSOR_RANGE_MULT,
                            fleet.getStats().getSensorRangeMod());
                }

                float penalty = Misc.getBurnMultForTerrain(fleet);
                fleet.getStats().removeTemporaryMod(getModId() + "_storm_speed");
                fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_speed",
                        "Inside charged nebula", penalty,
                        fleet.getStats().getFleetwideMaxBurnMod());
                if (cell != null && cell.isSignaling() && cell.signal < 0.2f) {
                    cell.signal = 0; // go to storm as soon as a fleet enters, if it's close to storming already
                }

                if (cell != null && cell.isStorming() && !Misc.isSlowMoving(fleet)) {
                    // storm
                    fleet.getStats().removeTemporaryMod(getModId() + "_sensor");
                    fleet.getStats().addTemporaryModMult(0.1f, getModId() + "_storm_sensor",
                            "Inside charged nebula (storm)",
                            CHARGED_STORM_SENSOR_RANGE_MULT,
                            fleet.getStats().getSensorRangeMod());
                    applyStormStrikes(cell, fleet, days);
                }
            }
        }
    }

    @Override
    protected void applyStormStrikes(CellStateTracker cell, CampaignFleetAPI fleet, float days) {
        if (cell.flicker != null && cell.flicker.getWait() > 0) {
            cell.flicker.setNumBursts(0);
            cell.flicker.setWait(0);
            cell.flicker.newBurst();
        }

        if (cell.flicker == null || !cell.flicker.isPeakFrame()) {
            return;
        }

        fleet.addScript(new II_ChargedStormBoost(cell, fleet));

        String key = "$ii_chargedStormStrikeTimeout";
        MemoryAPI mem = fleet.getMemoryWithoutUpdate();
        if (mem.contains(key)) {
            return;
        }
        mem.set(key, true, (float) (STORM_MIN_TIMEOUT + (STORM_MAX_TIMEOUT - STORM_MIN_TIMEOUT) * Math.random()));

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        if (members.isEmpty()) {
            return;
        }

        float totalValue = 0;
        for (FleetMemberAPI member : members) {
            totalValue += member.getStats().getSuppliesToRecover().getModifiedValue();
        }
        if (totalValue <= 0) {
            return;
        }

        float strikeValue = totalValue * STORM_DAMAGE_FRACTION * (0.5f + (float) Math.random() * 0.5f);

        WeightedRandomPicker<FleetMemberAPI> picker = new WeightedRandomPicker<>();
        for (FleetMemberAPI member : members) {
            float w = 1f;
            if (member.isMothballed()) {
                w *= 0.1f;
            }
            picker.add(member, w);
        }
        FleetMemberAPI member = picker.pick();
        if (member == null) {
            return;
        }

        float crPerDep = member.getDeployCost();
        float suppliesPerDep = member.getStats().getSuppliesToRecover().getModifiedValue();
        if (suppliesPerDep <= 0 || crPerDep <= 0) {
            return;
        }

        float strikeDamage = crPerDep * strikeValue / suppliesPerDep;
        if (strikeDamage < STORM_MIN_STRIKE_DAMAGE) {
            strikeDamage = STORM_MIN_STRIKE_DAMAGE;
        }

        float resistance = member.getStats().getDynamic().getValue(Stats.CORONA_EFFECT_MULT);
        strikeDamage *= resistance;

        if (strikeDamage > STORM_MAX_STRIKE_DAMAGE) {
            strikeDamage = STORM_MAX_STRIKE_DAMAGE;
        }

        float currCR = member.getRepairTracker().getBaseCR();
        float crDamage = Math.min(currCR, strikeDamage);
        if (crDamage > 0) {
            member.getRepairTracker().applyCREvent(-crDamage, "ii_chargedstorm", "Charged nebula storm strike");
        }

        float hitStrength = member.getStats().getArmorBonus().computeEffective(member.getHullSpec().getArmorRating());
        hitStrength *= strikeDamage / crPerDep;
        if (hitStrength > 0) {
            member.getStatus().applyDamage(hitStrength);
            if (member.getStatus().getHullFraction() < 0.01f) {
                member.getStatus().setHullFraction(0.01f);
            }
        }

        if (fleet.isPlayerFleet()) {
            String verb = "suffers";
            Color c = Misc.getNegativeHighlightColor();
            if (hitStrength <= 0) {
                verb = "avoids";
                c = Misc.getTextColor();
            }
            Global.getSector().getCampaignUI().addMessage(
                    member.getShipName() + " " + verb + " damage from the storm", c);
        }
    }

    @Override
    public boolean containsEntity(SectorEntityToken other) {
        return isInClouds(other);
    }

    @Override
    public boolean containsPoint(Vector2f test, float r) {
        return isInClouds(test, r);
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        float pad = 10f;
        float small = 5f;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        int[] tile = getTilePreferStorm(player.getLocation(), player.getRadius());
        CellStateTracker cell = null;
        if (tile != null) {
            cell = activeCells[tile[0]][tile[1]];
        }

        tooltip.addTitle(getTerrainName());
        if (cell == null || !cell.isStorming()) {
            // deep
            tooltip.addPara(Global.getSettings().getDescription(getTerrainId() + "_normal", Type.TERRAIN).getText1(),
                    pad);
        } else if (cell.isStorming()) {
            // storm
            tooltip.addPara(Global.getSettings().getDescription(getTerrainId() + "_storm", Type.TERRAIN).getText1(), pad);
        }

        float nextPad = pad;
        if (expanded) {
            tooltip.addSectionHeading("Travel", Alignment.MID, pad);
            nextPad = small;
        }

        tooltip.addPara("Reduces the range at which fleets inside can be detected by %s.",
                pad,
                highlight,
                "" + (int) ((1f - CHARGED_SENSOR_RANGE_MULT) * 100) + "%"
        );

        tooltip.addPara("Reduces the speed of fleets inside by up to %s. Larger fleets are slowed down more.",
                nextPad,
                highlight,
                "" + (int) ((Misc.BURN_PENALTY_MULT) * 100f) + "%"
        );

        float penalty = Misc.getBurnMultForTerrain(Global.getSector().getPlayerFleet());
        tooltip.addPara("Your fleet's speed is reduced by %s.", pad,
                highlight,
                "" + (int) Math.round((1f - penalty) * 100) + "%"
        );

        tooltip.addSectionHeading("Charged storms", Alignment.MID, pad);

        Color stormDescColor = Misc.getTextColor();
        if (cell != null && cell.isStorming()) {
            stormDescColor = bad;
        }
        tooltip.addPara("Being caught in a storm causes storm strikes to damage ships "
                + "and reduce their combat readiness. "
                + "Larger fleets attract more damaging strikes.", stormDescColor, pad);

        tooltip.addPara("In addition, storm strikes toss the fleet's drive bubble about "
                + "with great violence, often causing a loss of control.", Misc.getTextColor(), pad);

        tooltip.addPara("\"Slow-moving\" fleets do not attract storm strikes.", Misc.getTextColor(), pad);

        if (expanded) {
            tooltip.addSectionHeading("Combat", Alignment.MID, pad);
            tooltip.addPara("Numerous patches of nebula present on the battlefield, but the medium is not dense enough to affect ships moving at combat speeds.", small);
        }
    }

    @Override
    public int getNumMapSamples() {
        return 3;
    }

    @Override
    public String getTerrainName() {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        int[] tile = getTilePreferStorm(player.getLocation(), player.getRadius());
        CellStateTracker cell = null;
        if (tile != null) {
            cell = activeCells[tile[0]][tile[1]];
        }

        String n = "Charged Nebula";
        if (cell != null && cell.isStorming()) {
            n = "Charged Nebula (Storm)";
        }
        return n;
    }

    @Override
    protected float[] getThetaAndRadius(Random rand, float width, float height) {
        if (temp == null) {
            temp = new float[2];
        }

        float speedFactor = 0.10f;

        float time = elapsed * Global.getSector().getClock().getSecondsPerDay();
        float min = -360f * (rand.nextFloat() * 3f + 1f) * Misc.RAD_PER_DEG;
        float max = 360f * (rand.nextFloat() * 3f + 1f) * Misc.RAD_PER_DEG;
        float rate = (30f + 70f * rand.nextFloat()) * Misc.RAD_PER_DEG;
        rate *= speedFactor;
        float period = 2f * (max - min) / rate;
        float progress = rand.nextFloat() + time / period;
        progress -= (int) progress;

        float theta, radius;
        if (progress < 0.5f) {
            theta = min + (max - min) * progress * 2f;
        } else {
            theta = min + (max - min) * (1f - progress) * 2f;
        }
        temp[0] = theta;

        min = 0f;
        max = (width + height) * 0.03f;
        rate = max * 0.5f;
        rate *= speedFactor;

        period = 2f * (max - min) / rate;
        progress = rand.nextFloat() + time / period;
        progress -= (int) progress;
        if (progress < 0.5f) {
            radius = min + (max - min) * progress * 2f;
        } else {
            radius = min + (max - min) * (1f - progress) * 2f;
        }
        temp[1] = radius;

        return temp;
    }

    @Override
    public float getAbyssalDepth(Vector2f loc) {
        return 0f;
    }

    @Override
    public float getAbyssalDepth(SectorEntityToken other) {
        return 0f;
    }

    @Override
    public boolean isInAbyss(SectorEntityToken other) {
        return false;
    }

    @Override
    public List<StarSystemAPI> getAbyssalSystems() {
        return new ArrayList<>();
    }
}
