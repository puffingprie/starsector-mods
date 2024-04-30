package data.hullmods.ix;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class OdysseyRetrofit extends BaseHullMod {

	private static float SHIELD_BONUS = 20f;
	private static String POINT_DEFENSE = "point defense";
	private static String DRONE_RELAY = "Terminus Relay";
	private static float CR_INCREASE = 11.1f;	
	
	private static float PD_MISSILE_BONUS = 50f;
	private static String PD_MOD = "pointdefenseai";
	private static String HANGAR_MOD = "converted_hangar";
	private static String RELAY = "ix_terminus_relay";
	private static String SENSORS = "hiressensors";
	private static String NEW_SPRITE = "odyssey_ix_no_bay";
	
	@Override	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI variant = stats.getVariant();
		
		variant.getHullMods().remove(PD_MOD);
		variant.getHullMods().remove(HANGAR_MOD);
		
		if (variant.hasHullMod(RELAY)) {
			variant.getHullMods().remove(SENSORS);
			stats.getNumFighterBays().modifyFlat(id, 1);
		}
		else {
			variant.addPermaMod(SENSORS); 
			stats.getNumFighterBays().modifyFlat(id, 0);
		}
		
		stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_BONUS * 0.01f);
		stats.getDynamic().getMod("deployment_points_mod").modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesToRecover().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getSuppliesPerMonth().modifyMult(id, 1f + CR_INCREASE * 0.01f);
		stats.getDamageToMissiles().modifyPercent(id, PD_MISSILE_BONUS);
		stats.getDynamic().getMod(Stats.PD_BEST_TARGET_LEADING).modifyFlat(id, 1f);
		stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
		
		//in case system reset removed built-in hullmod
		if (!variant.hasHullMod("eccm")) variant.addPermaMod("eccm"); 
		
		//weapon switcher
		variant.clearSlot("WS TORP");
		if (variant.hasHullMod("ix_antecedent")) {
			variant.addWeapon("WS TORP", "antecedent_ix");
		}
		else variant.addWeapon("WS TORP", "vampyr_ix");
	}
	
	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		if (!ship.getVariant().hasHullMod(RELAY)) {
			float x = ship.getSpriteAPI().getCenterX();
			float y = ship.getSpriteAPI().getCenterY();
			float alpha = ship.getSpriteAPI().getAlphaMult();
			float angle = ship.getSpriteAPI().getAngle();
			Color color = ship.getSpriteAPI().getColor();
			ship.setSprite("ix_ships", NEW_SPRITE);
			ship.getSpriteAPI().setCenter(x, y);
			ship.getSpriteAPI().setAlphaMult(alpha);
			ship.getSpriteAPI().setAngle(angle);
			ship.getSpriteAPI().setColor(color);	
		}
		
		List weapons = ship.getAllWeapons();
		Iterator iter = weapons.iterator();
		while (iter.hasNext()) {
			WeaponAPI weapon = (WeaponAPI)iter.next();
			boolean sizeMatches = weapon.getSize() == WeaponSize.SMALL;
			if (sizeMatches && weapon.getType() != WeaponType.MISSILE && !weapon.hasAIHint(AIHints.STRIKE)) weapon.setPD(true);
		}
		
		deleteWeaponInCargo("antecedent_ix");
		deleteWeaponInCargo("vampyr_ix");
	}
	
	private void deleteWeaponInCargo(String weapon) {
		try {
			CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
			for (CargoStackAPI s : cargo.getStacksCopy()) {
				if (s.isWeaponStack() && s.getWeaponSpecIfWeapon().getWeaponId().equals(weapon)) {
					cargo.removeStack(s);					
				}
			}
		}
		catch (Exception e) {}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_BONUS + "%";
		if (index == 1) return POINT_DEFENSE;
		if (index == 2) return DRONE_RELAY;
		if (index == 3) return "" + (int) CR_INCREASE + "%";
		return null;
	}
}