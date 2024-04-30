package data.scripts.ix.listeners;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;

import data.scripts.ix.NameListUtil;
import lunalib.lunaSettings.LunaSettings;

//gives chance to add Panoptic Interface to encountered IX Battlegroup ships
public class IXEncounterListener extends BaseCampaignEventListener {
	
	private static String COMMAND_MOD_ID = "ix_panoptic_command";
	private static String STRATEGIC_MOD_ID = "ix_panoptic_strategic";
	private static String TACTICAL_MOD_ID = "ix_panoptic_tactical";

	private static String IX_MOD_ID = "ix_ninth";
	private static String IX_ELITE_MOD_ID = "ix_smod_handler";
	private static String IX_BOSS_MOD_ID = "ix_hvb_handler";
	private static String CHECKER_MOD_ID = "ix_panoptic_checker";	
	
	private static float ODDS_REGULAR = 0.1f;
	private static float ODDS_LEADER = 0.3f;
	private static float ODDS_ELITE_BONUS = 0.2f;
	
	public IXEncounterListener() {
		super(true);
	}
	
	@Override
	public void reportShownInteractionDialog(InteractionDialogAPI dialog) {
		CampaignFleetAPI otherFleet = null;
		CampaignFleetAPI allFleet = null;
		boolean isValidFleet = true;	
		try { 
			otherFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
			if (otherFleet.isPlayerFleet()) isValidFleet = false;
		}
		catch (Exception e) {
			isValidFleet = false;
		} 
		finally {
			if (isValidFleet) {
				otherFleet = (CampaignFleetAPI) dialog.getInteractionTarget();
				if (otherFleet.getBattle() != null) allFleet = otherFleet.getBattle().getNonPlayerCombined();
				equipInterfaceToFleet(allFleet);
			}
		}
	}	
	
	private void equipInterfaceToFleet (CampaignFleetAPI fleet) {
		if (fleet == null) return;
		
		//Lunalib settings
		boolean isInterfaceEnabled = true;
		
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			isInterfaceEnabled = LunaSettings.getBoolean("EmergentThreats_IX_Revival", "ix_interface_enabled");
		}

		boolean isOnlyCore = true;
		boolean isFirstRename = true;
		
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();
		for (FleetMemberAPI member : fleetList) {
			ShipVariantAPI var = member.getVariant();

			boolean isWithoutCaptain = (member.getCaptain() == null || member.getCaptain().isDefault());
			
			if (var.hasHullMod(IX_BOSS_MOD_ID)) {
				if (HullSize.CAPITAL_SHIP.equals(var.getHullSize())
						&& var.hasHullMod(IX_BOSS_MOD_ID) 
						&& !Global.getSector().getMemoryWithoutUpdate().getBoolean("$ix_biochip_looted")) {
					CargoAPI cargo = Global.getFactory().createCargo(true);
					if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")) {
						SectorEntityToken carrier = (SectorEntityToken) fleet;
						cargo.addHullmods("vice_interdiction_array", 1);
						cargo.addHullmods("vice_adaptive_entropy_arrester", 1);
						cargo.addHullmods("vice_adaptive_flux_dissipator", 1);
						BaseSalvageSpecial.addExtraSalvage(carrier, cargo);
						Global.getSector().getMemoryWithoutUpdate().set("$ix_biochip_looted", true);
					}
				}
				
				if (HullSize.CRUISER.equals(var.getHullSize())) {
					if (member.getShipName().startsWith("TTDS") && (isFirstRename)) {
						member.setShipName(NameListUtil.HGS_Vindicator);
						isFirstRename = false;
					}
					else if ((member.getShipName().startsWith("TTDS") && (!isFirstRename))) {
						member.setShipName(NameListUtil.HGS_Vanquisher);
					}
				}
			}
			
			if (var.hasHullMod(IX_MOD_ID) && !var.hasHullMod(IX_BOSS_MOD_ID)) {
				//clear interface mods if lunalib setting is off
				if (!isInterfaceEnabled) {
					var.getPermaMods().remove(COMMAND_MOD_ID);
					var.getPermaMods().remove(TACTICAL_MOD_ID);
					var.getPermaMods().remove(STRATEGIC_MOD_ID);
					var.getHullMods().remove(COMMAND_MOD_ID);
					var.getHullMods().remove(TACTICAL_MOD_ID);
					var.getHullMods().remove(STRATEGIC_MOD_ID);
				}
				
				//do nothing if ship already has checker
				else if (!var.hasHullMod(CHECKER_MOD_ID)) {
					float odds = member.isFlagship() ? ODDS_LEADER : ODDS_REGULAR;
					if (var.hasHullMod(IX_ELITE_MOD_ID)) odds += ODDS_ELITE_BONUS;
					if (Math.random() < odds) {
						String mod = "";
						//only one command interface max per encounter
						if (isWithoutCaptain && isOnlyCore) {
							mod = COMMAND_MOD_ID;
							isOnlyCore = false;
						}
						else mod = Math.random() <= 0.5f ? TACTICAL_MOD_ID : STRATEGIC_MOD_ID;
						var.addPermaMod(mod);
					}
					//always add checker even if interface is not added so ship is not checked again
					var.addPermaMod(CHECKER_MOD_ID);
				}
			}
		}
	}
}