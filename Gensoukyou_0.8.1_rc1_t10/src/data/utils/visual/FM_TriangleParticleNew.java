package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.util.FaderUtil;
import data.utils.FM_Misc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FM_TriangleParticleNew extends BaseCombatLayeredRenderingPlugin {

    public static class FM_TPParam implements Cloneable {
        public float fadeIn = 0.1f;
        public float fadeOut = 0.5f;
        public float radius = 20f;
        public float spin = 1f;
        public Color color = new Color(100, 100, 255);
        public Vector2f vel = new Vector2f();
        public Vector2f loc = new Vector2f();

        public float facing;
        public float alpha;
        public boolean spinDirection;
        public float r;
        public FaderUtil fader;

        public FM_TPParam() {
        }

        @Override
        protected FM_TPParam clone() throws CloneNotSupportedException {
            try {
                return (FM_TPParam) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    public static final String FM_TriangleParticleString = "FM_TriangleParticleString";
    //protected SpriteAPI tex;

    protected static List<FM_TPParam> params = new ArrayList<>();
    protected static List<FM_TPParam> deadParams = new ArrayList<>();
    protected int segments;


    public FM_TriangleParticleNew() {

    }

    public float getRenderRadius() {
        return Float.MAX_VALUE;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.CONTRAILS_LAYER);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        for (FM_TPParam param : params) {
            param.fader.advance(amount);

            if (param.spinDirection) {
                param.facing = param.facing + amount * param.spin;
            } else {
                param.facing = param.facing - amount * param.spin;
            }


            param.loc.x = param.loc.x + param.vel.x * amount;
            param.loc.y = param.loc.y + param.vel.y * amount;

            param.alpha = param.fader.getBrightness();

            if (param.fader.isFadingIn()) {
                param.r *= 0.75f + Math.sqrt(param.alpha) * 0.25f;
            } else {
                param.r *= 0.1f + 0.9f * param.alpha;
            }

            if (param.fader.isFadedOut()) {
                deadParams.add(param);
            }

        }

        params.removeAll(deadParams);
        deadParams.clear();

    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        entity.getLocation().set(FM_Misc.ZERO);

        segments = 3;

    }

    public boolean isExpired() {
        return false;
    }


    /**
     * @param loc           location
     * @param vel           velocity
     * @param radius        how large the particle is
     * @param fadeIn        fade in time
     * @param fadeOut       fade out time
     * @param color         color
     * @param facing        initial facing in degree
     * @param spinSpeed     how fast it rotates, in degree/s
     * @param spinDirection direction, clockwise or not
     */

    public void addTriangleParticle(Vector2f loc, Vector2f vel, float radius, float fadeIn, float fadeOut, Color color, float facing, float spinSpeed, boolean spinDirection) {
        FM_TPParam param = new FM_TPParam();
        param.loc = new Vector2f(loc);
        param.vel = new Vector2f(vel);

        param.radius = radius;
        param.fadeIn = fadeIn;
        param.fadeOut = fadeOut;
        param.color = color;

        param.facing = facing;
        param.spin = spinSpeed;
        param.spinDirection = spinDirection;

        param.alpha = 0f;

        param.fader = new FaderUtil(0f, param.fadeIn, param.fadeOut);
        param.fader.setBounceDown(true);
        param.fader.fadeIn();

        param.r = param.radius;

        params.add(param);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        for (FM_TPParam param : params) {
            renderGraph(param.loc.x, param.loc.y, param.r, param.facing, param.alpha, segments, param.color);
        }

//		GL11.glPopMatrix();
    }
    private void renderGraph(float x, float y, float radius,float facing, float alphaMult, int segments, Color color) {

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

        //空心和实心多边形（大雾

        GL11.glBegin(GL11.GL_LINE_STRIP);
//        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glVertex2f(radius, 0);
//        GL11.glVertex2f(0, 0);
        for (float i = 0; i < segments + 1; i++) {
            //记得从0开始算.aya
            boolean last = i == segments;

            if (last) i = 0;//在回到起点

            float theta = (float) Math.toRadians(60f * i + facing);
            float x1 = ((float) Math.cos(theta)) * radius;
            float y1 = ((float) Math.sin(theta)) * radius;

            GL11.glVertex2f(x1, y1);

            if (last) break;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }

}
