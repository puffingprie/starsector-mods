package data.shipsystems.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class HMI_MoteParticleScript implements EveryFrameScript {

	protected float moteSpawnRate = 1f;
	protected SectorEntityToken entity;
	protected IntervalUtil moteSpawn = new IntervalUtil(0.01f, 0.1f);
	
	public HMI_MoteParticleScript(SectorEntityToken entity, float moteSpawnRate) {
		super();
		this.entity = entity;
		this.moteSpawnRate = moteSpawnRate;
	}

	public void advance(float amount) {
		float days = Misc.getDays(amount);
		moteSpawn.advance(days * moteSpawnRate);
		if (moteSpawn.intervalElapsed()) {
			spawnSpookMote(entity);
		}
	}

	
	public static void spawnSpookMote(SectorEntityToken from) {
		if (!from.isInCurrentLocation()) return;
		float dur = 1f + 2f * (float) Math.random();
		dur *= 2f;
		float size = 3f + (float) Math.random() * 5f;
		size *= 3f;
		Color color = new Color(253, 0, 72,175);
		
		Vector2f loc = Misc.getPointWithinRadius(from.getLocation(), from.getRadius());
		Vector2f vel = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
		vel.scale(5f + (float) Math.random() * 10f);
		vel.scale(0.25f);
		Vector2f.add(vel, from.getVelocity(), vel);
		Misc.addGlowyParticle(from.getContainingLocation(), loc, vel, size, 0.5f, dur, color);
	}

	public boolean isDone() {
		return false;
	}

	public boolean runWhilePaused() {
		return false;
	}
}












