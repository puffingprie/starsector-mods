package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

import static org.lazywizard.lazylib.MathUtils.getRandom;
import static org.lazywizard.lazylib.MathUtils.getRandomNumberInRange;

public class expsp_ArcFusilHit implements OnHitEffectPlugin {


	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target,
					  Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		int arcNum= getRandomNumberInRange(0,4);
		if (arcNum >=1 && target instanceof ShipAPI) {
			if(shieldHit&&arcNum>=2){
				arcNum=arcNum-1;
			}
			float emp = projectile.getEmpAmount()/4;
			float dam = projectile.getDamageAmount()/4;
			for(int arcsFired=0; arcsFired < arcNum; arcsFired++ ) {
				engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
						DamageType.ENERGY,
						dam,
						emp, // emp
						100000f, // max range
						"tachyon_lance_emp_impact",
						20f, // thickness
						new Color(25, 100, 155, 255),
						new Color(255, 255, 255, 255)
				);
			}
			//engine.spawnProjectile(null, null, "plasma", point, 0, new Vector2f(0, 0));
		}
	}
}
