package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LuddEnhance_Bombardment extends BaseHullMod {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant
    public static final float GROUND_BONUS = 250;
	private static final float CR_PENALTY = 0.3f;
    private static final float PARA_PAD = 10f;

	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static
	{
        BLOCKED_HULLMODS.add("efficiency_overhaul");
    }
	
	private float check=0;
	private String id, ERROR="LE_IncompatibleHullmodWarning";
	
    public static float getCRPenalty(ShipVariantAPI variant) {
        float scale = 1f;

        Collection<String> hullMods = variant.getHullMods();
        for (String hullMod : hullMods) {
            HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(hullMod);
            if (modSpec.hasTag(Tags.HULLMOD_DMOD)) {
                scale /= CompromisedStructure.DEPLOYMENT_COST_MULT;
            }
        }

        return scale * CR_PENALTY;
    }
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, GROUND_BONUS);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
				if (check>0) {
					check-=1;
					if (check<1){
						ship.getVariant().removeMod(ERROR);
					}
				}

				for (String tmp : BLOCKED_HULLMODS) {
					if (ship.getVariant().getHullMods().contains(tmp)) {
						ship.getVariant().removeMod(tmp);
						ship.getVariant().addMod(ERROR);
						check=3;
					}
				}
	}

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        updateDecoWeapons(ship);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        switch (index) {
            case 0:
                return "" + (int) GROUND_BONUS;
            case 1:
                return "" + (int) Math.round(getCRPenalty(ship.getVariant()) * 100f) + "%";
            default:
                break;
        }
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        if (getCRPenalty(ship.getVariant()) > CR_PENALTY) {
            float penaltyScalePct = 100f * ((getCRPenalty(ship.getVariant()) / CR_PENALTY) - 1f);
            LabelAPI label = tooltip.addPara("CR cost is penalized by %s due to hull defects.", PARA_PAD, Misc.getNegativeHighlightColor(), "" + Math.round(penaltyScalePct) + "%");
        }

        float CR = ship.getCurrentCR();
        if (ship.getFleetMember() != null) {
            CR = ship.getFleetMember().getRepairTracker().getBaseCR();
        }
        if (CR < getCRPenalty(ship.getVariant())) {
            LabelAPI label = tooltip.addPara("Insufficient CR for Light deployment!", Misc.getNegativeHighlightColor(), PARA_PAD);
        }

        HullModSpecAPI efficiencyOverhaul = Global.getSettings().getHullModSpec("efficiency_overhaul");
        if (efficiencyOverhaul != null) {
            LabelAPI label = tooltip.addPara("Incompatible with " + efficiencyOverhaul.getDisplayName() + ".", PARA_PAD);
            label.setHighlightColor(Misc.getNegativeHighlightColor());
            label.setHighlight(efficiencyOverhaul.getDisplayName());
        }
    }

    protected void updateDecoWeapons(ShipAPI ship) {
        boolean mightWeaponHasAmmo = false;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.getId().contentEquals("le_dram_w")) {
                if (weapon.getAmmo() > 0) {
                    mightWeaponHasAmmo = true;
                }
                break;
            }
        }
        float CR = ship.getCurrentCR();
        if (ship.getFleetMember() != null) {
            CR = ship.getFleetMember().getRepairTracker().getBaseCR();
        }
        if (CR < getCRPenalty(ship.getVariant())) {
            mightWeaponHasAmmo = false;
        }

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            int frame;

            switch (weapon.getId()) {
                case "le_dram_deco":
                    if (!mightWeaponHasAmmo) {
                        frame = 2;
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

}
