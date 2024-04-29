package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

public class HMI_HorrorVanish extends BaseHullMod {

	public static Color JITTER_COLOR = new Color(100,100,255,50);

	public static float SPAWN_TIME = 4f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBreakProb().modifyMult(id, 0f);
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		CombatEngineAPI engine = Global.getCombatEngine();
		engine.setCombatNotOverForAtLeast(SPAWN_TIME + 1f);

		if (!ship.isHulk() || !engine.isEntityInPlay(ship)) return;
		Global.getCombatEngine().addPlugin(createShipFadeOutPlugin(ship, SPAWN_TIME));

	}

	protected EveryFrameCombatPlugin createShipFadeOutPlugin(final ShipAPI ship, final float fadeOutTime) {
		return new BaseEveryFrameCombatPlugin() {
			float elapsed = 0f;
			IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);

			@Override
			public void advance(float amount, List<InputEventAPI> events) {
				if (Global.getCombatEngine().isPaused()) return;

				elapsed += amount;


				float progress = elapsed / fadeOutTime;
				if (progress > 1f) progress = 1f;
				ship.setAlphaMult(1f - progress);

				//if (progress < 0.5f) {
				//}

				if (progress > 0.5f) {
					ship.setCollisionClass(CollisionClass.NONE);
				}

				float jitterLevel = progress;
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}

				float jitterRange = progress;
				//jitterRange = (float) Math.sqrt(jitterRange);
				float maxRangeBonus = 100f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = JITTER_COLOR;
				int alpha = c.getAlpha();
				alpha += 100f * progress;
				if (alpha > 255) alpha = 255;
				c = Misc.setAlpha(c, alpha);

				ship.setJitter(this, c, jitterLevel, 25, 0f, jitterRangeBonus);

				interval.advance(amount);
				if (interval.intervalElapsed() && elapsed < fadeOutTime * 0.75f) {
					CombatEngineAPI engine = Global.getCombatEngine();
					c = RiftLanceEffect.getColorForDarkening(RiftCascadeEffect.STANDARD_RIFT_COLOR);
					float baseDuration = 2f;
					Vector2f vel = new Vector2f(ship.getVelocity());
					float size = ship.getCollisionRadius() * 0.35f;
					for (int i = 0; i < 3; i++) {
						Vector2f point = new Vector2f(ship.getLocation());
						point = Misc.getPointWithinRadiusUniform(point, ship.getCollisionRadius() * 0.5f, Misc.random);
						float dur = baseDuration + baseDuration * (float) Math.random();
						float nSize = size;
						Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
						Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
						v.scale(nSize + nSize * (float) Math.random() * 0.5f);
						v.scale(0.2f);
						Vector2f.add(vel, v, v);

						float maxSpeed = nSize * 1.5f * 0.2f;
						float minSpeed = nSize * 1f * 0.2f;
						float overMin = v.length() - minSpeed;
						if (overMin > 0) {
							float durMult = 1f - overMin / (maxSpeed - minSpeed);
							if (durMult < 0.1f) durMult = 0.1f;
							dur *= 0.5f + 0.5f * durMult;
						}
						engine.addNegativeNebulaParticle(pt, v, nSize * 1f, 2f,
								0.5f / dur, 0f, dur, c);
					}
				}

				if (elapsed > fadeOutTime) {
					ship.getLocation().set (0, -1000000);
					ship.setHitpoints(0f);
					Global.getCombatEngine().removeEntity(ship);
					ship.setAlphaMult(0f);
					Global.getCombatEngine().removePlugin(this);
				}
			}
		};
	}
}








