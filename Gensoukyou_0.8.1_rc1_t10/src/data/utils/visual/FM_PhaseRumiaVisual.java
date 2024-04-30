package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Noise;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;

public class FM_PhaseRumiaVisual extends BaseCombatLayeredRenderingPlugin {

    public static class PhaseRumiaParam implements Cloneable {
        //public float fadeIn = 0.25f;
        public float fadeIn;
        public float fadeOut;
        public float spawnHitGlowAt = 0f;
        public float hitGlowSizeMult = 0.75f;
        public float radius;
        public float thickness = 25f;
        public float noiseMag = 1f;
        public float noisePeriod = 0.1f;
        public boolean withHitGlow = true;
        public Color color;
        public Color underglow = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
        public Color invertForDarkening = null;
        public ShipAPI ship;

        public PhaseRumiaParam(@NotNull ShipAPI ship, Color color) {
            this.ship = ship;
            this.radius = ship.getCollisionRadius();
            this.fadeIn = ship.getPhaseCloak().getChargeUpDur();
            this.fadeOut = ship.getPhaseCloak().getChargeDownDur();
            this.color = color;
        }

        @Override
        protected PhaseRumiaParam clone() throws CloneNotSupportedException {
            try {
                return (PhaseRumiaParam) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }

    }

    protected FaderUtil fader;
    protected SpriteAPI atmosphereTex;

    protected float[] noise;
    protected float[] noise1;
    protected float[] noise2;
    protected PhaseRumiaParam p;
    protected int segments;
    protected float noiseElapsed = 0f;

    protected boolean spawnedHitGlow = false;

    public FM_PhaseRumiaVisual(PhaseRumiaParam p) {
        this.p = p;
    }

    public float getRenderRadius() {
        return p.radius + 500f;
    }


    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER, CombatEngineLayers.ABOVE_PARTICLES);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        if (p.ship == null) return;
        if (p.ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.OUT) {
            fader.advance(amount / p.fadeIn);
            fader.fadeOut();
        } else if (p.ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.IN) {
            fader.advance(amount / p.fadeOut);
        }
        //Global.getCombatEngine().addFloatingText(p.ship.getLocation(),fader.getBrightness() + "",10f,Color.WHITE, p.ship,0f,0f);
//		if (p.ship.getPhaseCloak().getEffectLevel() > 0 && p.ship.getPhaseCloak().getEffectLevel() < 1f){
//			//Global.getCombatEngine().addFloatingText(p.ship.getLocation(),"TEST",10f,Color.WHITE, p.ship,0f,0f);
//		}
        //Global.getCombatEngine().addHitParticle(entity.getLocation(), FM_Misc.ZERO,10f,255f,0.5f,Color.CYAN);

        entity.getLocation().set(p.ship.getLocation());

        if (p.noiseMag > 0) {
            noiseElapsed += amount;
            if (noiseElapsed > p.noisePeriod) {
                noiseElapsed = 0;
                noise1 = Arrays.copyOf(noise2, noise2.length);
                noise2 = Noise.genNoise(segments, p.noiseMag);
            }
            float f = noiseElapsed / p.noisePeriod;
            for (int i = 0; i < noise.length; i++) {
                float n1 = noise1[i];
                float n2 = noise2[i];
                noise[i] = n1 + (n2 - n1) * f;
            }
        }

        if (!p.withHitGlow) return;

        float glowSpawnAt = 1f;
        if (!spawnedHitGlow && (!fader.isFadingIn() || fader.getBrightness() >= p.spawnHitGlowAt)) {
            float size = Math.min(p.radius * 7f, p.radius + 150f);
            float coreSize = Math.max(size, p.radius * 4f);
            if (coreSize > size) size = coreSize;

            size *= p.hitGlowSizeMult;
            coreSize *= p.hitGlowSizeMult;

            CombatEngineAPI engine = Global.getCombatEngine();
            Vector2f point = entity.getLocation();
            Vector2f vel = entity.getVelocity();
            float dur = fader.getDurationOut() * glowSpawnAt;
            engine.addHitParticle(point, vel, size * 3f, 1f, dur, p.color);
            //engine.addHitParticle(point, vel, size * 3.0f, 1f, dur, p.color);
            engine.addHitParticle(point, vel, coreSize * 1.5f, 1f, dur, Color.white);
            //engine.addHitParticle(point, vel, coreSize * 1f, 1f, dur, Color.white);

            Color invert = p.color;
            if (p.invertForDarkening != null) invert = p.invertForDarkening;
            Color c = new Color(255 - invert.getRed(),
                    255 - invert.getGreen(),
                    255 - invert.getBlue(), 127);
            c = Misc.interpolateColor(c, Color.white, 0.4f);
            //c = Misc.setAlpha(c, 80);
            //c = Misc.scaleAlpha(c, 0.5f);
            float durMult = 1f;
            for (int i = 0; i < 7; i++) {
                dur = 4f + 4f * (float) Math.random();
                //dur = p.fadeIn + p.fadeOut + 3f + (float) Math.random() * 2f;
                dur *= durMult;
                dur *= 0.5f;
                //float nSize = size * (1f + 0.0f * (float) Math.random());
                //float nSize = size * (0.75f + 0.5f * (float) Math.random());
                float nSize = size;
                Vector2f pt = Misc.getPointAtRadius(point, nSize * 0.5f);
                Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
                v.scale(nSize + nSize * (float) Math.random() * 0.5f);
                v.scale(0.15f);
                Vector2f.add(vel, v, v);

//				float maxSpeed = nSize * 1.5f * 0.2f; 
//				float minSpeed = nSize * 1f * 0.2f; 
//				float overMin = v.length() - minSpeed;
//				if (overMin > 0) {
//					float durMult2 = 1f - overMin / (maxSpeed - minSpeed);
//					if (durMult2 < 0.1f) durMult2 = 0.1f;
//					dur *= 0.5f + 0.5f * durMult2;
//				}
                v = new Vector2f(entity.getVelocity());
//				engine.addNegativeParticle(pt, v, nSize * 1f, p.fadeIn / dur, dur, c);
                engine.addNegativeNebulaParticle(pt, v, nSize, 2f,
                        p.fadeIn / dur, 0f, dur, c);
            }

            dur = p.fadeIn + p.fadeOut + 2f;
            dur *= durMult;
            float rampUp = (p.fadeIn + p.fadeOut) / dur;
            rampUp = 0f;
            //rampUp = rampUp + (1f - rampUp) * 0.25f;
            //float sizeMult = p.hitGlowSizeMult;

            c = p.underglow;
            //c = Misc.setAlpha(c, 255);
            for (int i = 0; i < 15; i++) {
                //rampUp = (float) Math.random() * 0.05f + 0.05f;
                Vector2f loc = new Vector2f(point);
                loc = Misc.getPointWithinRadius(loc, size);
                //loc = Misc.getPointAtRadius(loc, size * 1f);
                float s = size * 3f * (0.25f + (float) Math.random() * 0.25f);
                //s *= 0.5f;
//				engine.addSmoothParticle(loc, entity.getVelocity(), s, 1f, rampUp, dur, c);
                engine.addNebulaParticle(loc, entity.getVelocity(), s, 1.5f, rampUp, 0f, dur, c);
            }
            spawnedHitGlow = true;
        }
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
        entity.getLocation().set(p.ship.getLocation());

        fader = new FaderUtil(0f, p.fadeIn, p.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        atmosphereTex = Global.getSettings().getSprite("combat", "corona_hard");

        float perSegment = 2f;
        segments = (int) ((p.radius * 2f * 3.14f) / perSegment);
        if (segments < 8) segments = 8;

        noise1 = Noise.genNoise(segments, p.noiseMag);
        noise2 = Noise.genNoise(segments, p.noiseMag);
        noise = Arrays.copyOf(noise1, noise1.length);
    }

    public boolean isExpired() {
        return fader.isFadedOut() || p.ship.getPhaseCloak().isCoolingDown();
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        float f = fader.getBrightness();
        float alphaMult = viewport.getAlphaMult();
        if (f < 0.5f) {
            alphaMult *= f * 2f;
        }

        float r;
        float tSmall = p.thickness;

        if (fader.isFadingIn()) {
            r = (float) ((0.75f + Math.sqrt(f) * 0.25f) * p.radius);
        } else {
            r = (0.1f + 0.9f * f) * p.radius;
            tSmall = Math.min(r, p.thickness);
        }

//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glScalef(6f, 6f, 1f);
//		x = y = 0;

        //GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
            float a = 1f;
            renderAtmosphere(x, y, r, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
            renderAtmosphere(x, y, r - 2f, tSmall, alphaMult * a, segments, atmosphereTex, noise, p.color, true);
        } else if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
            float circleAlpha = 1f;
            if (alphaMult < 0.5f) {
                circleAlpha = alphaMult * 2f;
            }
            float tCircleBorder = 1f;
            renderCircle(x, y, r, circleAlpha, segments, Misc.scaleAlpha(Color.black, 1f));
            renderAtmosphere(x, y, r, tCircleBorder, circleAlpha, segments, atmosphereTex, noise, Color.black, false);
        }
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

//		GL11.glPopMatrix();
    }


    private void renderCircle(float x, float y, float radius, float alphaMult, int segments, Color color) {
        if (fader.isFadingIn()) alphaMult = 1f;

        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float spanRad = Misc.normalizeAngle(endRad - startRad);
        float anglePerSegment = spanRad / segments;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4ub((byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) ((float) color.getAlpha() * alphaMult));

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(0, 0);
        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            float m1 = 0.75f + 0.65f * noise[(int) i];
            if (p.noiseMag <= 0) {
                m1 = 1f;
            }

            float x1 = cos * radius * m1;
            float y1 = sin * radius * m1;

            GL11.glVertex2f(x1, y1);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();

    }


    private void renderAtmosphere(float x, float y, float radius, float thickness, float alphaMult, int segments, SpriteAPI tex, float[] noise, Color color, boolean additive) {

        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float spanRad = Misc.normalizeAngle(endRad - startRad);
        float anglePerSegment = spanRad / segments;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        tex.bindTexture();

        GL11.glEnable(GL11.GL_BLEND);
        if (additive) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        GL11.glColor4ub((byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) ((float) color.getAlpha() * alphaMult));
        float texX = 0f;
        float incr = 1f / segments;
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            float m1 = 0.75f + 0.65f * noise[(int) i];
            float m2 = m1;
            if (p.noiseMag <= 0) {
                m1 = 1f;
                m2 = 1f;
            }

            float x1 = cos * radius * m1;
            float y1 = sin * radius * m1;
            float x2 = cos * (radius + thickness * m2);
            float y2 = sin * (radius + thickness * m2);

            GL11.glTexCoord2f(0.5f, 0.05f);
            GL11.glVertex2f(x1, y1);

            GL11.glTexCoord2f(0.5f, 0.95f);
            GL11.glVertex2f(x2, y2);

            texX += incr;
            if (last) break;
        }

        GL11.glEnd();
        GL11.glPopMatrix();
    }
}


