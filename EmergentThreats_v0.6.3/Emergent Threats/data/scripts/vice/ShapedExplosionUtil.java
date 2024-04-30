package data.scripts.vice;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class ShapedExplosionUtil {

	//for Hellfire SDEM (large), Balefire Emitter (small), deadeye plasma seeker (deadeye)
	public static void spawnShapedExplosion(Vector2f loc, float angle, float shipSpeed, Color pc, boolean isSmall) {
		String type = isSmall ? "small" : "large";
		spawnShapedExplosion(loc, angle, shipSpeed, pc, type);
	}
	
	public static void spawnShapedExplosion(Vector2f loc, float angle, float shipSpeed, Color pc, String type) {		
		if (Global.getCombatEngine().getViewport().isNearViewport(loc, 800f)) {
			
			int numParticles = 200;
			float minSize = 20f;
			float maxSize = 30f;
			
			float minDur = 0.6f;
			float maxDur = 1.2f;
			
			float arc = 120f;
			float scatter = 100f;
			float minVel = 20f + shipSpeed;
			float maxVel = 60f + shipSpeed;
			
			float endSizeMin = 1f;
			float endSizeMax = 2f;
			
			if (type.equals("small")) {
				numParticles = 100;
				minSize = 15f;
				maxSize = 20f;
			
				arc = 30f;
				scatter = 50f;
			}
			
			else if (type.equals("deadeye")) {
				numParticles = 50;
				minSize = 10f;
				maxSize = 15f;
				
				minDur = 0.4f;
				maxDur = 0.7f;
			
				arc = 120f;
				scatter = 20f;
				minVel = 10f + shipSpeed;
				maxVel = 30f + shipSpeed;
			}
			
			Vector2f spawnPoint = new Vector2f(loc);
			for (int i = 0; i < numParticles; i++) {
				float angleOffset = (float) Math.random();
				if (angleOffset > 0.2f) {
					angleOffset *= angleOffset;
				}
				float speedMult = 1f - angleOffset;
				speedMult = 0.5f + speedMult * 0.5f;
				angleOffset *= Math.signum((float) Math.random() - 0.5f);
				angleOffset *= arc/2f;
				float theta = (float) Math.toRadians(angle + angleOffset);
				float r = (float) (Math.random() * Math.random() * scatter);
				float x = (float) Math.cos(theta) * r;
				float y = (float) Math.sin(theta) * r;
				Vector2f pLoc = new Vector2f(spawnPoint.x + x, spawnPoint.y + y);
				
				float speed = minVel + (maxVel - minVel) * (float) Math.random();
				speed *= speedMult;
				
				Vector2f pVel = Misc.getUnitVectorAtDegreeAngle((float) Math.toDegrees(theta));
				pVel.scale(speed);
				
				float pSize = minSize + (maxSize - minSize) * (float) Math.random();
				float pDur = minDur + (maxDur - minDur) * (float) Math.random();
				float endSize = endSizeMin + (endSizeMax - endSizeMin) * (float) Math.random();
				Global.getCombatEngine().addNebulaParticle(pLoc, pVel, pSize, endSize, 0.1f, 0.5f, pDur, pc);
			}
		}
	}
}
