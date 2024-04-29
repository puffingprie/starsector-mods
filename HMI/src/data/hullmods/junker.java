package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.util.HashSet;
import java.util.Set;

public class junker extends BaseHullMod {

	public static float DMOD_EFFECT_MULT = 0.5f;

	private float armourCheck=0;
	private float hullCheck=0;
	private float engCheck=0;
	private float gridCheck=0;
	private float struCheck=0;
	private float sensCheck=0;
	private float commsCheck=0;
	private float manuCheck=0;
	private float storeCheck=0;
	private float mainCheck=0;
	private float injecCheck=0;
	private float subCheck=0;
	private float deckCheck=0;

	private float V_WepCheck=0;
	private float V_autoCheck=0;
	private float V_EveryCheck=0;
	private float V_fightCheck=0;
	private float V_envCheck=0;
	private float V_gunneryCheck=0;
	private float V_lifeCheck=0;

	private float OP_SAVE=0;

	private String Junk1="junk1";
	private String Junk2="junk2";
	private String Junk3="junk3";
	private String Junk4="junk4";

	private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
	static
	{
		// These hullmods will automatically be removed
		BLOCKED_HULLMODS.add("vayra_collapsedcargo");
		BLOCKED_HULLMODS.add("vayra_collapsedcargo_free");
	}
	private float check=0;
	private String id, ERROR="IncompatibleHullmodWarning";
	private static final float ENGINE_MALFUNCTION_PROB = 0.08f;
	private static final float WEAPON_MALFUNCTION_PROB = 0.08f;
	private static final float REPAIR_TIME = 2f;
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getDynamic().getStat(Stats.DMOD_EFFECT_MULT).modifyMult(id, DMOD_EFFECT_MULT);
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		stats.getBreakProb().modifyMult(id, 0f);

	}
		@Override
		public void applyEffectsAfterShipCreation(ShipAPI ship, String id){

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

//			if (ship.getVariant().getHullMods().contains("comp_armor")) {
//				armourCheck = 1;
//			} else {
//				armourCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("comp_hull")) {
//				hullCheck = 1;
//			} else {
//				hullCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("degraded_engines")) {
//				engCheck = 1;
//			} else {
//				engCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("faulty_grid")) {
//				gridCheck = 1;
//			} else {
//				gridCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("comp_structure")) {
//				struCheck = 1;
//			} else {
//				struCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("glitched_sensors")) {
//				sensCheck = 1;
//			} else {
//				sensCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("malfunctioning_comms")) {
//				commsCheck = 1;
//			} else {
//				commsCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("defective_manufactory")) {
//				manuCheck = 1;
//			} else {
//				manuCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("fragile_subsystems")) {
//				subCheck = 1;
//			} else {
//				subCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("damaged_deck")) {
//				deckCheck = 1;
//			} else {
//				deckCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("comp_storage")) {
//				storeCheck = 1;
//			} else {
//				storeCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("increased_maintenance")) {
//				mainCheck = 1;
//			} else {
//				mainCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("erratic_injector")) {
//				injecCheck = 1;
//			} else {
//				injecCheck = 0;
//			}

			//Vayra D-Mods


//			if (ship.getVariant().getHullMods().contains("vayra_damaged_turrets")) {
//				V_WepCheck = 1;
//			} else {
//				V_WepCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_automation")) {
//				V_autoCheck = 1;
//			} else {
//				V_autoCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_everything")) {
//				V_EveryCheck = 2;
//			} else {
//				V_EveryCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_fighterweapons")) {
//				V_fightCheck = 1;
//			} else {
//				V_fightCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_environment")) {
//				V_envCheck = 1;
//			} else {
//				V_envCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_gunnery")) {
//				V_gunneryCheck = 1;
//			} else {
//				V_gunneryCheck = 0;
//			}
//			if (ship.getVariant().getHullMods().contains("vayra_damaged_lifesupport")) {
//				V_lifeCheck = 1;
//			} else {
//				V_lifeCheck = 0;
//			}

			//Extreme Mod Debuff D-Mods

			if (ship.getVariant().getHullMods().contains("swp_extrememods")) {
				ship.getMutableStats().getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALFUNCTION_PROB);
				ship.getMutableStats().getEngineMalfunctionChance().modifyFlat(id, ENGINE_MALFUNCTION_PROB);
				ship.getMutableStats().getCombatWeaponRepairTimeMult().modifyMult(id, REPAIR_TIME);
				ship.getMutableStats().getCombatEngineRepairTimeMult().modifyMult(id, REPAIR_TIME);
			}



//			OP_SAVE =armourCheck+hullCheck+engCheck+gridCheck+struCheck+sensCheck+subCheck+deckCheck+commsCheck+manuCheck+storeCheck+mainCheck+injecCheck+V_WepCheck+V_autoCheck+V_EveryCheck+V_fightCheck+V_envCheck+V_gunneryCheck+V_lifeCheck;
			OP_SAVE = DModManager.getNumNonBuiltInDMods(ship.getVariant());

				if (OP_SAVE <= 0) {
					ship.getVariant().removeMod(Junk1);
					ship.getVariant().removeMod(Junk2);
					ship.getVariant().removeMod(Junk3);
					ship.getVariant().removeMod(Junk4);
				}

				if (OP_SAVE == 1) {
					ship.getVariant().addMod(Junk1);
					ship.getVariant().removeMod(Junk2);
					ship.getVariant().removeMod(Junk3);
					ship.getVariant().removeMod(Junk4);
				}
				if (OP_SAVE == 2) {
					ship.getVariant().removeMod(Junk1);
					ship.getVariant().addMod(Junk2);
					ship.getVariant().removeMod(Junk3);
					ship.getVariant().removeMod(Junk4);
				}
				if (OP_SAVE == 3) {
					ship.getVariant().removeMod(Junk1);
					ship.getVariant().removeMod(Junk2);
					ship.getVariant().addMod(Junk3);
					ship.getVariant().removeMod(Junk4);
				}
				if (OP_SAVE >= 4) {
					ship.getVariant().removeMod(Junk1);
					ship.getVariant().removeMod(Junk2);
					ship.getVariant().removeMod(Junk3);
					ship.getVariant().addMod(Junk4);
				}
			}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + "increasing number of d-mods";
		if (index == 1) return "" + "increase in Ordinance Points";
		if (index == 2) return "" + "decrease in repair costs";
		if (index == 3) return "" + "50%";
		if (index == 4) return "" + "fighter quality and engagement range";
		if (index == 5) return "" + "Collapsed Cargo Holds";
		if (index == 6) return "" + "Extreme Modifications";
		if (index == 7) return "" + "doubled";
		return null;
	}

}





