package data.scripts.vice.weapons;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.dem.DEMScript;
import com.fs.starfarer.api.util.Misc;

import data.scripts.vice.DistanceUtil;

public class InterdictorOnFire implements OnFireEffectPlugin {

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		ShipAPI ship = projectile.getSource();
		ShipAPI target = null;
		if (ship == Global.getCombatEngine().getPlayerShip()) target = ship.getShipTarget();
		else target = DistanceUtil.getNearestEnemy(ship, 2000f);
		if (target == null) {
			projectile.setHitpoints(0);
			return;
		}
		MissileAPI missile = (MissileAPI) projectile;
		Vector2f point = missile.getLocation();
		float facing = Misc.getAngleInDegrees(point, target.getLocation());
		missile.setFacing(facing);
		DEMScript script = new DEMScript(missile, ship, weapon);
		Global.getCombatEngine().addPlugin(script);
		engine.addSmoothParticle(point, new Vector2f(), 220, 1.0f, 1.3f, new Color(25,100,255,255));
	}
}
