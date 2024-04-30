package data.scripts.ix.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class VampyrEffect implements OnFireEffectPlugin, OnHitEffectPlugin {

	private static float BOLT_BASE_DAMAGE = 1000f;
	private static float BOLT_BASE_EMP = 1000f;
	private static int BOLT_COUNT = 3;
	
	public void onFire(DamagingProjectileAPI proj, WeaponAPI weapon, CombatEngineAPI engine) {
		proj.setDamageAmount(0); //damage dealt from bolts only
	}
	
	public void onHit(DamagingProjectileAPI proj, CombatEntityAPI target, Vector2f point, boolean shieldHit, 
						ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		
		ShipAPI thisShip = proj.getSource();
		float missileDamageMult = thisShip.getMutableStats().getMissileWeaponDamageMult().getMult();
		float damage = BOLT_BASE_DAMAGE * missileDamageMult;
		float empDamage = BOLT_BASE_EMP * missileDamageMult;
		
		//initialize all shield pierces as false if shield hit then determine odds for each hit
		boolean[] isPierce = { false, false, false };
		if (!shieldHit) isPierce = new boolean[] { true, true, true };
		else if (target instanceof ShipAPI && shieldHit) {
			ShipAPI hitShip = (ShipAPI) target;
			float pierceChance = hitShip.getHardFluxLevel() - 0.1f;
			pierceChance *= hitShip.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT);
			for (int i = 0; i < BOLT_COUNT; i++) {
				if (Math.random() < pierceChance) isPierce[i] = true;
			}
		}
		
		for (int i = 0; i < BOLT_COUNT; i++) {
			spawnEMP(thisShip, point, target, damage, empDamage, engine, isPierce[i]);
		}
		Global.getSoundPlayer().playSound("vampyr_ix_emp", 1f, 1f, point, new Vector2f());
	}
			
	private void spawnEMP (ShipAPI source, Vector2f point, CombatEntityAPI target, 
							float damage, float empDamage, CombatEngineAPI engine, boolean isPierce) {
		if (isPierce) {
			engine.spawnEmpArcPierceShields(
								source, point, target, target,
								DamageType.ENERGY, 
								damage, 
								empDamage,
								100000f, // max range 
								"",
								20f, // thickness
								new Color(50,255,50,200), // fringe
								new Color(200,255,200,180) // core color
								);
		}
		else engine.applyDamage(target, point, damage, DamageType.ENERGY, empDamage, false, false, source);
	}
}