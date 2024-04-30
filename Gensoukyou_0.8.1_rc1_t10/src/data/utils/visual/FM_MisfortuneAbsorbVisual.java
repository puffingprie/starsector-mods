package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Noise;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Arrays;
import java.util.EnumSet;

public class FM_MisfortuneAbsorbVisual extends BaseCombatLayeredRenderingPlugin {

    public static class FM_MAVParams implements Cloneable {
        public float fadeIn = 0.1f;
        public float fadeIdle = 0f;
        public float fadeOut = 0.5f;
        public float radius = 20f;
        public float thickness = 25f;
        public Color color = new Color(100, 100, 255);
        public boolean additive = false;
        public Vector2f loc = new Vector2f();
        public float perSegment = 48f;
        public float noiseSpikes = 1f;
        public SpriteAPI texForRing = Global.getSettings().getSprite("combat", "corona_hard");

        public FM_MAVParams() {
        }

        @Override
        protected FM_MAVParams clone() throws CloneNotSupportedException {
            try {
                return (FM_MAVParams) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    //我有特殊的淡入淡出技巧（）
    //很可惜只有淡入淡出.aya
    //复杂的东西等下一次...
    protected FaderUtil fader;
    protected SpriteAPI tex;

    protected FM_MAVParams params;
    protected int segments;
    protected ShipAPI ship;
    protected float visualFullDuration;

    //生动形象？
    //连续的噪声？
    protected float[] noiseFinal;
    protected float[] noise1;
    protected float[] noise2;
    protected float noiseElapsed = 0f;

    public FM_MisfortuneAbsorbVisual(FM_MAVParams params, ShipAPI ship) {
        this.params = params;
        this.ship = ship;
    }

    public float getRenderRadius() {
        return params.radius + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;
        visualFullDuration = visualFullDuration - amount;
        if (ship == null){
            return;
        }
        if (visualFullDuration >= params.fadeOut + params.fadeIdle) {
            fader.advance(amount);
        } else if (visualFullDuration <= params.fadeOut || !ship.isAlive()) {
            fader.advance(amount);
        }

        entity.getLocation().set(params.loc);
        //噪点相关
        //连续变化
        noiseElapsed += amount;
        if (noiseElapsed > 0.6f) {
            noiseElapsed = 0;
            noise1 = Arrays.copyOf(noise2, noise2.length);
            noise2 = Noise.genNoise(segments, params.noiseSpikes);
        }
        float f = noiseElapsed / 0.6f;
        for (int i = 0; i < noiseFinal.length; i++) {
            float n1 = noise1[i];
            float n2 = noise2[i];
            //连续变化！
            noiseFinal[i] = n1 + (n2 - n1) * f;
        }

    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        fader = new FaderUtil(0f, params.fadeIn, params.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        visualFullDuration = params.fadeIdle + params.fadeIn + params.fadeOut;

        tex = params.texForRing;
        //非常单纯的一段有多长.png
        float perSegment = params.perSegment;

        segments = (int) ((params.radius * 2f * 3.14f) / perSegment);
        if (segments < 16) segments = 16;

        noise1 = Noise.genNoise(segments, params.noiseSpikes);
        noise2 = Noise.genNoise(segments, params.noiseSpikes);
        noiseFinal = Arrays.copyOf(noise1, noise1.length);

    }

    public boolean isExpired() {
        return fader.isFadedOut();
    }


    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = params.loc.x;
        float y = params.loc.y;
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
//        if (fader.isFadingIn()) {
//            r *= 0.75f + Math.sqrt(f) * 0.25f;
//        } else {
//            r *= 0.1f + 0.9f * f;
//        }

        if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {

            renderAtmosphere(x, y, r, params.thickness, alphaMult, noiseFinal, segments, tex, params.color, params.additive);
        }
    }


    private void renderAtmosphere(float x, float y, float radius, float thickness, float alphaMult, float[] noise, int segments, SpriteAPI tex, Color color, boolean additive) {
        //角度与节点处理
        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360);
        float anglePerSegment = (endRad - startRad) / segments;

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        //Test
        GL11.glRotatef(0, 0, 0, 1);
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

        float texX = 0f;
        float incr = 1f / segments;

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        for (float i = 0; i < segments + 1; i++) {
            boolean last = i == segments;
            if (last) i = 0;
            float theta = anglePerSegment * i;

            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);

            //噪点和尖锐？
            float m1 = 0.95f + 0.05f * noise[(int) i];

            //同向平行.aya
            float x1 = cos * radius * m1;
            float y1 = sin * radius * m1;
            float x2 = cos * (radius + thickness);
            float y2 = sin * (radius + thickness);
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

            texX += incr;
            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
