package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;

public class FM_StripVisual extends BaseCombatLayeredRenderingPlugin {

    public static class FM_StripParam implements Cloneable {
        public float lifeTime = 1f;
        public Vector2f start = new Vector2f();
        public Vector2f dest = new Vector2f();
        public float thickness = 5f;
        //闪烁深度相关，取值0到1
        //与alpha联系，会在区间内调整alpha
        public float flickerLowest = 0f;
        public float flickerHighest = 1f;
        //闪烁频率相关
        public float flickerFrequency = 0.1f;
        public float pieceLength = 0.1f;
        public Color color = new Color(100, 100, 255);
        public boolean additive = false;

        public FM_StripParam() {
        }

        public FM_StripParam(Vector2f start, Vector2f dest, float thickness, Color color, boolean additive, float lifeTime) {
            super();
            this.start = start;
            this.dest = dest;
            this.thickness = thickness;
            this.color = color;
            this.additive = additive;
            this.lifeTime = lifeTime;
        }

        @Override
        protected FM_StripParam clone() throws CloneNotSupportedException {
            try {
                return (FM_StripParam) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }


    protected SpriteAPI tex;

    protected FM_StripParam params;

    protected int segments;

    protected IntervalUtil flicker;
    protected FaderUtil fader;


    public FM_StripVisual(FM_StripParam params) {
        this.params = params;
    }

    public float getRenderRadius() {
        return MathUtils.getDistance(params.start, params.dest) + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        flicker.advance(amount);
        fader.advance(amount);


    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        flicker = new IntervalUtil(0, params.flickerFrequency);
        fader = new FaderUtil(1f, params.lifeTime);
        fader.fadeOut();

        tex = Global.getSettings().getSprite("combat", "corona_hard");

    }

    public boolean isExpired() {
        return fader.isFadedOut();
    }


    public void render(CombatEngineLayers layer, ViewportAPI viewport) {


        Vector2f dir = Vector2f.sub(params.dest, params.start, new Vector2f());
        VectorUtils.resize(dir, params.thickness * 0.5f, dir);

        Vector2f up = VectorUtils.rotate(dir, 90f, new Vector2f());
        Vector2f down = VectorUtils.rotate(dir, -90f, new Vector2f());

        Vector2f p1 = Vector2f.add(params.start, up, new Vector2f());
        Vector2f p2 = Vector2f.add(params.dest, up, new Vector2f());

        Vector2f p3 = Vector2f.add(params.start, down, new Vector2f());
        Vector2f p4 = Vector2f.add(params.dest, down, new Vector2f());

        float x1 = p1.x;
        float y1 = p1.y;
        float x2 = p2.x;
        float y2 = p2.y;
        float x3 = p3.x;
        float y3 = p3.y;
        float x4 = p4.x;
        float y4 = p4.y;

        float alphaMult = params.flickerLowest + (params.flickerHighest - params.flickerLowest) * flicker.getElapsed() / params.flickerFrequency;


        if (layer == CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER) {

            renderStrip(x1, y1, x2, y2, x3, y3, x4, y4, alphaMult, params.pieceLength, tex, params.color, params.additive);
        }
    }


    private void renderStrip(float x1, float y1, float x2, float y2,
                             float x3, float y3, float x4, float y4, float alphaMult, float pieceLength, SpriteAPI tex, Color color, boolean additive) {


        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
//        GL11.glTranslatef(x1, y1, 0);
//        GL11.glTranslatef(x2, y2, 0);
//        GL11.glTranslatef(x3, y3, 0);
//        GL11.glTranslatef(x4, y4, 0);
        //旋转函数，矩阵相关操作
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

        for (float i = 0; i < 1; i = i + pieceLength) {

            //同向平行.aya
            //首尾相连
            float xp13 = x1 + (x3 - x1) * i;
            float yp13 = y1 + (y3 - y1) * i;
            float xp24 = x2 + (x4 - x2) * i;
            float yp24 = y2 + (y4 - y2) * i;


            //正确的在四边形上表示纹理的方式：
            //图的各个顶点（整个的肯定是矩形啦）
            //与四边形的定点对应
            //因为是循环绘制所以起点对起点的意思（）
            //s与t也是对应x与y轴的
            GL11.glTexCoord2f(0.05f, 0.05f);
            GL11.glVertex2f(xp13, yp13);
            GL11.glTexCoord2f(0.95f, 0.05f);
            GL11.glVertex2f(xp24, yp24);

            texX += incr;

        }

        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
