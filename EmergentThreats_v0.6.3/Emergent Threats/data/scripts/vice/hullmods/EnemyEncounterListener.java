package data.scripts.vice.hullmods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

import lunalib.lunaSettings.LunaSettings;

//adds adaptive subsystems to Remnant ships encountered by the player that are not part of this mod
public class EnemyEncounterListener extends BaseCampaignEventListener {
	
	private static String HANDLER_HULLMOD = "vice_interdiction_handler";
	private static String PENALTY_HULLMOD = "vice_adaptive_malfunction";
	
	public EnemyEncounterListener() {
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
				equipSubsystemsToFleet(allFleet);
			}
		}
	}	
	private void equipSubsystemsToFleet (CampaignFleetAPI fleet) {
		if (fleet == null) return;
		
		//Lunalib settings
		boolean isAbyssalEnabled = true;
		boolean isMessEnabled = true;
		boolean isVriEnabled = true;
		String remnantDifficulty = "Challenging";
		
		if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
			isAbyssalEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_abyssalEnabled");
			isMessEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_messEnabled");
			isVriEnabled = LunaSettings.getBoolean("EmergentThreats_Vice", "vice_vriEnabled");
			remnantDifficulty = LunaSettings.getString("EmergentThreats_Vice", "vice_remnantDifficulty");
		}
		
		List<FleetMemberAPI> fleetList = fleet.getMembersWithFightersCopy();
		for (FleetMemberAPI member : fleetList) {
			//adaptive tactical core check to see if ship has no captain.
			//doing it here since it can't easily be done with ShipVariantAPI
			boolean isWithoutCaptain = (member.getCaptain() == null || member.getCaptain().isDefault());

			ShipVariantAPI var = member.getVariant();
			boolean addNone = false;
			boolean isModule = false; //lets shipwide integration handle adaptive subsystems
			
			//add Interdictor Pulse handler to non-IX Paragons
			addParagonInterdictorHandler(var);
			
			if (var.getHullSpec().getSuppliesPerMonth() == 0 && var.getHullSpec().getEngineSpec().getMaxSpeed() == 0) isModule = true;
			boolean isRadiantIX = var.getHullSpec().getHullId().startsWith("radiant_ix");
			String maker = var.getHullSpec().getManufacturer();
			if (!var.isFighter() && !isModule && (isRadiantIX 
										|| maker.equals("Remnant") 
										|| maker.equals("Remnant Mess Object") 
										|| maker.equals("Dustkeeper Proxies") 
										|| maker.equals("Dustkeeper Contingency") 
										|| maker.equals("Abyssal") 
										|| maker.equals("Seraph") 
										|| maker.equals("Volantian Remnant Conversion") 
										|| maker.equals("XIV Remnant"))) {
				String modToDelete = null;
				String vriMod = null;
				for (String mod : var.getHullMods()) {
					//if ship has adaptive mod or setting is not Challenging, do not add new mod
					if (mod.startsWith("vice_adaptive") || !remnantDifficulty.equals("Challenging")) addNone = true;
					//if ship has adaptive mod and setting is not Challenging, delete adaptive mod
					if (mod.startsWith("vice_adaptive") && !remnantDifficulty.equals("Challenging")) modToDelete = mod;
					if (mod.startsWith("vice_adaptive")) vriMod = mod;
				}
				if (modToDelete != null) var.removeMod(modToDelete);
				
				//if setting is Easy, add penalty mod, else remove penalty mod
				if (remnantDifficulty.equals("Easy")) {
					var.addMod(PENALTY_HULLMOD);
					addNone = true;
				}
				else var.removeMod(PENALTY_HULLMOD);
				
				//clear disabled crossmod hullmods
				if (!isAbyssalEnabled) var.removeMod("vice_adaptive_entropy_projector_abyssal");
				if (!isMessEnabled) var.removeMod("vice_adaptive_metastatic_growth");
				if (maker.equals("Volantian Remnant Conversion") && !isVriEnabled && vriMod != null) {
					var.removeMod(vriMod);
					addNone = true;
				}
				if (addNone) continue;
				var.addMod(modPicker(var, isWithoutCaptain));
			}
		}
	}
	
	private void addParagonInterdictorHandler(ShipVariantAPI var) {
		String hullId = var.getHullSpec().getHullId();
		boolean isParagon = hullId.contains("paragon");
		boolean isParagonIX = hullId.startsWith("paragon_ix");
		
		//do nothing for non-Paragons or IX Paragons
		if (!isParagon || isParagonIX) return;

		//add handler to Paragon skins
		if (isParagon && !var.hasHullMod(HANDLER_HULLMOD)) var.addPermaMod(HANDLER_HULLMOD);
		
		//add handler to Zeus
		if (var.getHullSpec().getHullId().startsWith("swp_boss_paragon") && !var.hasHullMod(HANDLER_HULLMOD)) var.addPermaMod(HANDLER_HULLMOD);
	}
	
	private String modPicker (ShipVariantAPI var, boolean isWithoutCaptain) {
		ShipHullSpecAPI spec = var.getHullSpec();
		Collection<String> mods = var.getHullMods();
		
		//special faction hullmods
		if (spec.getManufacturer().equals("Abyssal")) return "vice_adaptive_entropy_projector_abyssal";
		if (spec.getManufacturer().equals("Seraph")) return "vice_adaptive_entropy_projector_abyssal";
		else if (spec.getManufacturer().equals("Remnant Mess Object")) return "vice_adaptive_metastatic_growth";
		else if (var.getHullSpec().getHullId().startsWith("radiant_ix")) return "vice_adaptive_flux_dissipator";
		
		//if ship has no shields and no conflicting phase mods, add adaptive phase coils (otherwise make it potential hullmod)
		boolean noShields = (spec.getShieldType().equals(ShieldType.PHASE) || spec.getShieldType().equals(ShieldType.NONE));
		if (noShields && (!var.hasHullMod("phase_anchor") && !var.hasHullMod("adaptive_coils"))) return "vice_adaptive_phase_coils";
		
		//get number of pulse and long range beam weapons
		List<String> weaponSlots = var.getNonBuiltInWeaponSlots();
		int beamCount = 0;
		int pulseCount = 0;
		for (String slot : weaponSlots) {
			WeaponSpecAPI weapon = var.getWeaponSpec(slot);
			if (weapon.getType().equals(WeaponType.ENERGY)) {
				if (weapon.isBeam() && weapon.getMaxRange() >= 900f) beamCount++;
				else if (!weapon.isBeam()) pulseCount++;
			}
		}
		
		//list of valid mods to add, does not include drone bay, flight command, or gravity drive
		List<String> MODLIST = new ArrayList<String>();
		MODLIST.add("vice_adaptive_phase_coils");
		MODLIST.add("vice_adaptive_reactor_chamber");
		
		//if 4 beam weapons and no optics mods, add adaptive emitter diodes, else if 1-3 make it potential hullmod
		boolean hasOptics = (mods.contains("advancedoptics") 
							|| mods.contains("high_scatter_amp")
							|| mods.contains("vice_attuned_emitter_diodes")
							|| mods.contains("ix_laser_collimator"));
		if (beamCount > 3 && !hasOptics) return "vice_adaptive_emitter_diodes";
		else if (beamCount >= 1 && beamCount <= 3 && !hasOptics) MODLIST.add("vice_adaptive_emitter_diodes");
		
		//if 4 pulse weapons and no coherer, add adaptive emitter diodes, else if 1-3 no coherer make it potential hullmod
		if ((pulseCount > 3) && !var.hasHullMod("coherer")) return "vice_adaptive_pulse_resonator";
		else if ((pulseCount >= 1 && beamCount <= 3) && !var.hasHullMod("coherer")) MODLIST.add("vice_adaptive_pulse_resonator");
		
		//if ship has no AI core, make adaptive tactical core potential mod, else make adaptive neural net potential hullmod
		if (isWithoutCaptain) MODLIST.add("vice_adaptive_tactical_core");
		else MODLIST.add("vice_adaptive_neural_net");
		
		//if ship has shields, make adaptive temporal shell potential mod
		if (!noShields) MODLIST.add("vice_adaptive_temporal_shell");
		
		//if ship has no other engine upgrades, make adaptive thruster control potential mod
		boolean hasEngineUpgrade = (mods.contains("auxiliarythrusters") || mods.contains("unstable_injector"));
		if (!hasEngineUpgrade) MODLIST.add("vice_adaptive_thruster_control");
		
		Random rand = new Random();
		int modIndex = rand.nextInt(MODLIST.size());
		
		return(MODLIST.get(modIndex).toString());
	}
}