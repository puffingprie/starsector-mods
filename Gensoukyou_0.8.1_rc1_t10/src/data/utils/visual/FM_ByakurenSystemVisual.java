package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

import java.awt.*;
import java.util.EnumSet;

public class FM_ByakurenSystemVisual extends BaseCombatLayeredRenderingPlugin {

    public static class FM_BSVParam implements Cloneable {
        public float fadeIn = 0.1f;
        public float fadeOut = 0.5f;
        public float full = 1f;
        //        public float radius = 20f;
        public float width = 6f;
        public float length = 36f;
        public float fluidSpeed = 20f;
        public int texPiece = 2;
        public SpriteAPI tex = Global.getSettings().getSprite("fx", "FM_StreamFx0");
        public Color color = new Color(100, 100, 255);
        public CombatEngineLayers layers = CombatEngineLayers.ABOVE_SHIPS_LAYER;

        public Vector2f loc = new Vector2f();

        public FM_BSVParam() {
        }

        @Override
        protected FM_BSVParam clone() throws CloneNotSupportedException {
            try {
                return (FM_BSVParam) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    protected FaderUtil fader;
    protected SpriteAPI tex0;


    protected FM_BSVParam params;

    protected int segments;
    protected ShipAPI ship;

    protected float fluidTimer;

    protected float fadeTimer;

    public FM_ByakurenSystemVisual(FM_BSVParam params, ShipAPI ship) {
        this.params = params;
        this.ship = ship;
    }

    public float getRenderRadius() {
        return params.length + 500f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(params.layers);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        fluidTimer = fluidTimer + params.fluidSpeed * amount;

        fadeTimer = fadeTimer + amount;

        if (fadeTimer <= params.fadeIn) {
            fader.advance(amount);
        } else if (fadeTimer >= params.fadeIn + params.full) {
            fader.advance(amount);
        }
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);

        fader = new FaderUtil(0f, params.fadeIn, params.fadeOut);
        fader.setBounceDown(true);
        fader.fadeIn();

        fluidTimer = 0f;

        fadeTimer = 0;

        tex0 = params.tex;

        entity.getLocation().set(params.loc);
        segments = 40;

    }

    public boolean isExpired() {
        return fader.isFadedOut();
    }


    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        float f = fader.getBrightness();

//		GL11.glPushMatrix();
//		GL11.glTranslatef(x, y, 0);
//		GL11.glScalef(6f, 6f, 1f);
//		x = y = 0;

        //GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        if (layer == params.layers) {
            renderAtmosphere(x, y, ship.getFacing() - 90f, params.width, params.length, fluidTimer, f, segments, params.texPiece, tex0, params.color, true);
        }
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);

//		GL11.glPopMatrix();
    }


    private void renderAtmosphere(float x, float y, float facing, float width, float length, float fluidLevel,
                                  float alphaMult, int segments, int texPieces, SpriteAPI tex, Color color, boolean additive) {
        //二次曲线参数处理
        float k = 4 * length / (width * width);

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(facing, 0, 0, 1);
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
            float pieceFraction = i / segments;
            boolean last = i == segments;

            float x1 = -pieceFraction * width * 0.5f;
            float y1 = -x1 * x1 * k;

            float x2 = pieceFraction * width * 0.5f;
            float y2 = -x2 * x2 * k;
            //同向平行.aya
            float texCoordX = pieceFraction * texPieces - fluidLevel;
            //颜色与渐变相关参数
            float alpha;
            if (pieceFraction <= 0.5f) {
                alpha = 1f;
            } else {
                alpha = MagicAnim.smooth((1f - pieceFraction) * 2f);
            }

            GL11.glTexCoord2f(texCoordX, 0f);
            GL11.glColor4ub((byte) color.getRed(),
                    (byte) color.getGreen(),
                    (byte) color.getBlue(),
                    (byte) ((float) color.getAlpha() * alphaMult * alpha));
            GL11.glVertex2f(x1, y1);

            GL11.glTexCoord2f(texCoordX, 1f);
            GL11.glColor4ub((byte) color.getRed(),
                    (byte) color.getGreen(),
                    (byte) color.getBlue(),
                    (byte) ((float) color.getAlpha() * alphaMult * alpha));
            GL11.glVertex2f(x2, y2);
            //下次不要乱写这些东西……
            if (last) break;
        }


        GL11.glEnd();
        GL11.glPopMatrix();
    }

}
