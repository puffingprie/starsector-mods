package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class FM_DiamondParticle3DTest extends BaseCombatLayeredRenderingPlugin {

    public static class FM_DP3DParams implements Cloneable {
        public float fadeIn = 0.1f;
        public float fadeOut = 0.5f;
        public float radius = 20f;
        public float spin = 1f;
        public float spinZ = 1f;
        public float thickness = 6f;
        public Color color = new Color(100, 100, 255);
        public Vector2f vel = new Vector2f();
        public Vector2f loc = new Vector2f();

        public float facing;
        public float zAngle;
        public float alpha;
        public boolean spinDirection;

        public float r;

        public FaderUtil fader;

        public FM_DP3DParams() {
        }

        @Override
        protected FM_DP3DParams clone() throws CloneNotSupportedException {
            try {
                return (FM_DP3DParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    public static final String FM_DiamondParticleString = "FM_DiamondParticleString";
    protected SpriteAPI tex;

    protected static List<FM_DP3DParams> params = new ArrayList<>();
    protected static List<FM_DP3DParams> deadParams = new ArrayList<>();
    protected int segments;


    public FM_DiamondParticle3DTest() {

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

        for (FM_DP3DParams param : params) {
            param.fader.advance(amount);

            if (param.spinDirection) {
                param.facing = param.facing + amount * param.spin;
                param.zAngle = param.zAngle + amount * param.spinZ;
            } else {
                param.facing = param.facing - amount * param.spin;
                param.zAngle = param.zAngle - amount * param.spinZ;
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

        tex = Global.getSettings().getSprite("fx", "FM_DiamondParticle3DTest_ring");

        entity.getLocation().set(FM_Misc.ZERO);

        segments = 4;

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
     * @param thickness     the thickness of the ring
     * @param facing        initial facing in degree
     * @param spinSpeed     how fast it rotates, in degree/s
     * @param spinZSpeed    how fast it rotates in z axis, in degree/s
     * @param spinDirection direction, clockwise or not
     */

    public void addDiamondParticle(Vector2f loc, Vector2f vel, float radius, float fadeIn, float fadeOut, Color color, float thickness, float facing, float spinSpeed, float spinZSpeed, boolean spinDirection) {
        FM_DP3DParams param = new FM_DP3DParams();
        param.loc = new Vector2f(loc);
        param.vel = new Vector2f(vel);

        param.radius = radius;
        param.fadeIn = fadeIn;
        param.fadeOut = fadeOut;
        param.color = color;
        param.thickness = thickness;

        param.facing = facing;
        param.spin = spinSpeed;
        param.spinDirection = spinDirection;
        param.spinZ = spinZSpeed;

        param.alpha = 0f;

        param.fader = new FaderUtil(0f, param.fadeIn, param.fadeOut);
        param.fader.setBounceDown(true);
        param.fader.fadeIn();

        param.r = param.radius;

        params.add(param);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

        //SpriteAPI test = Global.getSettings().getSprite("fx","FM_MomiziEffect_Test");

        for (FM_DP3DParams param : params) {
            renderAtmosphere(param.loc.x, param.loc.y, param.r, param.facing, param.zAngle, param.thickness, param.alpha, segments, tex, param.color, true);
            //renderMomiziTest(param.loc.x, param.loc.y, param.radius * 2, param.facing, param.zAngle, param.alpha, segments, test, Color.WHITE, true);
        }

//		GL11.glPopMatrix();
    }


    private void renderAtmosphere(float x, float y, float radius, float facing, float zAngle, float thickness, float alphaMult, int segments, SpriteAPI tex, Color color, boolean additive) {
        //角度与节点处理
        float radius2 = 0.5f * radius;

        Vector2f rotateAxis = Misc.getUnitVectorAtDegreeAngle(facing + 90f);

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(zAngle, rotateAxis.x, rotateAxis.y, 0);
        //纹理等
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();

        //混合颜色的方式.aya
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

        //一种特殊好用的四边形的绘制方法.png
        //重点系列
        //非常惊喜的独特顺序（）
        //例如如果用此法绘制一个矩形，顺序是 先一条边 再对角线连接至起点同侧 再另一条边
        //而不是一笔画下来的正常顺序
        //STRIP的原因（？
        //有他自己的好处
        //比如给两个同样方向的平行向量（）

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = (float) Math.toRadians(90f * i + facing);

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            //噪点和尖锐？
            float m1 = 1f;

            //同向平行.aya
            float x1;
            float y1;
            float x2;
            float y2;
            if (i % 2 != 0) {
                x1 = cos * radius * m1;
                y1 = sin * radius * m1;
                x2 = cos * (radius + thickness);
                y2 = sin * (radius + thickness);
            } else {
                x1 = cos * radius2 * m1;
                y1 = sin * radius2 * m1;
                x2 = cos * (radius2 + thickness);
                y2 = sin * (radius2 + thickness);

            }

//            x1 = cos * radius * m1;
//            y1 = sin * radius * m1;
//            x2 = cos * (radius + thickness);
//            y2 = sin * (radius + thickness);

            //其实肯定的还可以整点更劲的

            //正确的在四边形上表示纹理的方式：
            //图的各个顶点（整个的肯定是矩形啦）
            //与四边形的定点对应
            //因为是循环绘制所以起点对起点的意思（）
            //s与t也是对应x与y轴的
            GL11.glTexCoord2f(0.5f, 0.05f);
            GL11.glVertex2f(x1, y1);
            GL11.glTexCoord2f(0.5f, 0.95f);
            GL11.glVertex2f(x2, y2);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private void renderMomiziTest(float x, float y,float radius, float facing, float zAngle, float alphaMult, int segments, SpriteAPI tex, Color color, boolean additive) {
        //角度与节点处理

        Vector2f rotateAxis = Misc.getUnitVectorAtDegreeAngle(facing + 90f);

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(zAngle, rotateAxis.x, rotateAxis.y, 0);
        //纹理等
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();

        //混合颜色的方式.aya
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

        //一种特殊好用的四边形的绘制方法.png
        //重点系列
        //非常惊喜的独特顺序（）
        //例如如果用此法绘制一个矩形，顺序是 先一条边 再对角线连接至起点同侧 再另一条边
        //而不是一笔画下来的正常顺序
        //STRIP的原因（？
        //有他自己的好处
        //比如给两个同样方向的平行向量（）

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (float i = 0; i < segments + 2; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float k1 = i/segments;

            //噪点和尖锐？
            float m1 = 1f;

            //同向平行.aya
            float x1;
            float y1;
            float x2;
            float y2;

            x1 = k1 * radius;
            y1 = 0.5f * radius;
            x2 = k1 * radius;
            y2 = -0.5f * radius;

            Vector2f v1 = VectorUtils.rotate(new Vector2f(x1,y1),facing,new Vector2f());
            Vector2f v2 = VectorUtils.rotate(new Vector2f(x2,y2),facing,new Vector2f());

            x1 = v1.x;
            y1 = v1.y;
            x2 = v2.x;
            y2 = v2.y;
//            x1 = cos * radius * m1;
//            y1 = sin * radius * m1;
//            x2 = cos * (radius + thickness);
//            y2 = sin * (radius + thickness);

            //其实肯定的还可以整点更劲的

            //正确的在四边形上表示纹理的方式：
            //图的各个顶点（整个的肯定是矩形啦）
            //与四边形的定点对应
            //因为是循环绘制所以起点对起点的意思（）
            //s与t也是对应x与y轴的
            GL11.glTexCoord2f(1f, k1);
            GL11.glVertex2f(x1, y1);
            GL11.glTexCoord2f(0f, k1);
            GL11.glVertex2f(x2, y2);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }


}
