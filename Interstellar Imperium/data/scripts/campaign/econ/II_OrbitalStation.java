package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import data.scripts.IIModPlugin;
import exerelin.campaign.SectorManager;

public class II_OrbitalStation extends OrbitalStation {

    @Override
    public boolean isAvailableToBuild() {
        boolean canBuild = super.isAvailableToBuild();

        SectorAPI sector = Global.getSector();

        FactionAPI player = sector.getFaction(Factions.PLAYER);
        FactionAPI imperium = sector.getFaction("interstellarimperium");

        /* If we have a battlestation already, that means we're checking if we can downgrade -- which we should be able
         * to do, regardless of reputation.
         */
        if (!market.hasIndustry("ii_battlestation")) {
            if ((market.getPlanetEntity() != null) && !(player.getRelationshipLevel(imperium).isAtWorst(RepLevel.WELCOMING)
                    || Global.getSector().getPlayerFaction().knowsIndustry(getId()))) {
                canBuild = false;
            }

            boolean needsIndustryBP = true;
            if (IIModPlugin.isExerelin) {
                if (!SectorManager.getManager().isCorvusMode()) {
                    needsIndustryBP = false;
                }
            }
            if (needsIndustryBP && !player.knowsIndustry(getId())) {
                canBuild = false;
            }
        }

        return canBuild;
    }

    @Override
    public String getUnavailableReason() {
        if (!super.isAvailableToBuild()) {
            return super.getUnavailableReason();
        }
        return "Station type unavailable.";
    }

    @Override
    public boolean showWhenUnavailable() {
        if (!super.showWhenUnavailable()) {
            return false;
        }
        if (IIModPlugin.isExerelin) {
            if (!SectorManager.getManager().isCorvusMode()) {
                return false;
            }
        }
        return Global.getSector().getPlayerFaction().knowsIndustry(getId());
    }
}
