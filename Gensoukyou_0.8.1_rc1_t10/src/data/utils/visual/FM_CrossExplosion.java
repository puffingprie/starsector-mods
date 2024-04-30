package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class FM_CrossExplosion extends BaseCombatLayeredRenderingPlugin {

    public static class FM_CEParams implements Cloneable {
        public Vector2f loc = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float fadeIn = 0.1f;
        public float fadeIdle = 0f;
        public float fadeOut = 0.5f;
        public float radius = 20f;
        public float thickness = 5f;
        public float facing = 0f;
        public Color color = Color.RED;
        public Color coreColor = Color.BLACK;
        public boolean additive = false;
        public SpriteAPI texForRing = Global.getSettings().getSprite("combat", "corona_hard");

        public FM_CEParams() {
        }

        @Override
        protected FM_CEParams clone() throws CloneNotSupportedException {
            try {
                return (FM_CEParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    protected FaderUtil fader;
    protected SpriteAPI tex;

    protected FM_CEParams params;

    protected int segments;
    protected float visualFullDuration;

    //生动形象？
    //连续的噪声？
//    protected float [] noiseFinal;
//    protected float [] noise1;
//    protected float [] noise2;
//    protected float noiseElapsed = 0f;

    public FM_CrossExplosion(FM_CEParams params) {
        this.params = params;
    }

    public float getRenderRadius() {
        return params.radius + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES, CombatEngineLayers.ABOVE_PARTICLES_LOWER);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        visualFullDuration = visualFullDuration - amount;
        if (visualFullDuration >= params.fadeOut + params.fadeIdle) {
            fader.advance(amount);
        } else if (visualFullDuration <= params.fadeOut) {
            fader.advance(amount);
        }
        //噪点相关
        //连续变化
//        noiseElapsed += amount;
//        if (noiseElapsed > 0.6f) {
//            noiseElapsed = 0;
//            noise1 = Arrays.copyOf(noise2, noise2.length);
//            noise2 = Noise.genNoise(segments, 1f);
//        }
//        float f = noiseElapsed / 0.6f;
//        for (int i = 0; i < noiseFinal.length; i++) {
//            float n1 = noise1[i];
//            float n2 = noise2[i];
//            //连续变化！
//            noiseFinal[i] = n1 + (n2 - n1) * f;
//        }

    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
        entity.getLocation().set(params.loc);
        entity.getVelocity().set(params.vel);

        fader = new FaderUtil(0f, params.fadeIn, params.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        visualFullDuration = params.fadeIdle + params.fadeIn + params.fadeOut;

        tex = params.texForRing;

        segments = 8;

//        noise1 = Noise.genNoise(segments, 1f);
//        noise2 = Noise.genNoise(segments, 1f);
//        noiseFinal = Arrays.copyOf(noise1, noise1.length);

    }

    public boolean isExpired() {
        return fader.isFadedOut();
    }


    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;
        //淡入淡出相关
        float f = fader.getBrightness();
        float alphaMult = viewport.getAlphaMult();
        if (fader.isFadingIn()) {
            alphaMult = Math.min(1f, f * 3f);
        } else {
            alphaMult = f;
        }
        //扩散相关
        float r = params.radius;
        if (fader.isFadingIn()) {
            r *= 0.75f + Math.sqrt(f) * 0.25f;
        } else {
            r = (1f - f) * (1f - f) * params.radius + params.radius;
        }

        if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
            renderAtmosphere(x, y, r, params.thickness, params.facing, alphaMult, segments, tex, params.color, params.additive);
            renderCore(x, y, r, params.facing, alphaMult, segments, params.coreColor, true);
        } else if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {
            float circleAlpha = 1f;
            if (alphaMult < 0.5f) {
                circleAlpha = alphaMult * 2f;
            }
            float tCircleBorder = 1f;
            renderCore(x, y, r, params.facing, circleAlpha, segments, Color.black, false);
            renderAtmosphere(x, y, r, tCircleBorder, params.facing, circleAlpha, segments, params.texForRing, Color.black, false);
        }
    }


    private void renderAtmosphere(float x, float y, float radius, float thickness, float faceing, float alphaMult, int segments, SpriteAPI tex, Color color, boolean additive) {
        //角度与节点处理
        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float anglePerSegment = (endRad - startRad) / segments;

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(0, 0, 0, 1);
        //纹理等(启用)
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();

        //混合颜色的方式.aya(启用)
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

        for (float i = 1; i < segments + 2; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i + faceing;

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            float r = 1f;
            if (i % 2 != 0) {
                r = 0.1f;
            }

            //同向平行.aya
            float x1 = cos * radius * r;
            float y1 = sin * radius * r;
            float x2 = cos * ((radius + thickness) * r);
            float y2 = sin * ((radius + thickness) * r);
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

    private void renderCore(float x, float y, float radius, float facing, float alphaMult, int segments, Color color, boolean additive) {
        //角度与节点处理
        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float anglePerSegment = (endRad - startRad) / segments;

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(0, 0, 0, 1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        for (float i = 1; i < segments + 2; i++) {
            boolean last = i == segments + 1;
            if (last) i = 0;
            float theta = anglePerSegment * i + facing;

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            float r = 1f;
            if (i % 2 != 0) {
                r = 0.1f;
            }

            float x1 = cos * radius * r;
            float y1 = sin * radius * r;
//            GL11.glTexCoord2f(0.5f, 0.05f);
            GL11.glVertex2f(x1, y1);
//            GL11.glTexCoord2f(0.5f, 0.95f);

            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }


}
