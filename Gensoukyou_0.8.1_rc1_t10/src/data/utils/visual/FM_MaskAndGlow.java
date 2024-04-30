package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

//目标是实现方便的发光纹路，输入遮罩纹理与效果形状之后便可自行流动
//主要输入: 位置 mask图像 base图像
public class FM_MaskAndGlow extends BaseCombatLayeredRenderingPlugin {
    public static class FM_HullGlowParam implements Cloneable {
        public float fadeIn;
        public float fadeOut;
        public float fadeIdle;
        public float facing;
        public float maskWidth;
        public float maskLength;
        public Color maskColor = new Color(100, 100, 255,255);
        public Color baseColor = new Color(255,255,255,255);
        public SpriteAPI mask;
        public SpriteAPI base;
        public Vector2f loc;
        public float texPieces;
        public CombatEntityAPI attachEntity;
        private float alpha = 0;
        private float cycleTimer = 0f;
        private float flowLevel = 0;
        private Vector2f maskLoc;
        private Vector2f facingVector;

        public FM_HullGlowParam() {
        }
        @Override
        protected FM_HullGlowParam clone() throws CloneNotSupportedException {
            try {
                return (FM_HullGlowParam) super.clone();
            } catch (CloneNotSupportedException e) {
                return null; // should never happen
            }
        }
    }

    public static final String FM_MaskAndGlowTestString = "FM_MaskAndGlowTestString";

    protected static java.util.List<FM_HullGlowParam> params = new ArrayList<>();
    protected static List<FM_HullGlowParam> deadParams = new ArrayList<>();
    protected int segments = 12;

    public void setLocation(FM_HullGlowParam param,Vector2f loc){
        param.loc = loc;
    }
    public void addParamToRender(FM_HullGlowParam param){
        params.add(param);
    }

    public float getRenderRadius() {
        return Float.MAX_VALUE;
    }
    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,CombatEngineLayers.ABOVE_SHIPS_LAYER);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        for (FM_HullGlowParam param : params) {
            param.cycleTimer = param.cycleTimer + amount;
            if (param.cycleTimer <= param.fadeIn){
                param.alpha = Math.min(param.cycleTimer/param.fadeIn,1f);
            }else if (param.cycleTimer <= param.fadeIn + param.fadeIdle){
                param.alpha = 1f;
            }else if (param.cycleTimer <= param.fadeIn + param.fadeIdle + param.fadeOut){
                param.alpha = Math.max(0f,1f - (param.cycleTimer - param.fadeIn - param.fadeIdle)/param.fadeOut);
            }else {
                deadParams.add(param);
            }
            //debug
//            Global.getCombatEngine().addFloatingText(
//                    param.loc,
//                    param.alpha + "",
//                    20f,
//                    Color.WHITE,
//                    entity,
//                    0f,
//                    0f
//            );
            //base移动
            param.loc = param.attachEntity.getLocation();
            //mask纹理流动
            param.flowLevel = param.flowLevel + amount;
//            if (param.flowLevel >= 1f){
//                param.flowLevel = param.flowLevel - 1f;
//            }
            //mask自身运动
            Vector2f maskLocNext = Vector2f.add(param.loc, param.facingVector ,new Vector2f());
            float moveLevel = param.cycleTimer/(param.fadeIn + param.fadeIdle + param.fadeOut);
            Vector2f moving = (Vector2f) new Vector2f(-param.facingVector.x,-param.facingVector.y).scale(2f * moveLevel);
            Vector2f.add(maskLocNext,moving,param.maskLoc);

        }
        params.removeAll(deadParams);
        deadParams.clear();
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
        entity.getLocation().set(FM_Misc.ZERO);

        segments = 4;

    }

    public boolean isExpired() {
        return false;
    }

    /**
     * @param loc
     * @param fadeIn
     * @param fadeIdle
     * @param fadeOut
     * @param baseColor
     * @param maskColor
     * @param maskWidth
     * @param maskLength
     * @param facing
     * @param texPieces
     * @param mask
     * @param base
     */
    public FM_HullGlowParam addMaskAndFlow(Vector2f loc,CombatEntityAPI attachEntity, float fadeIn, float fadeIdle, float fadeOut, Color baseColor,
                               Color maskColor, float maskWidth, float maskLength, float facing, float texPieces,
                               SpriteAPI mask, SpriteAPI base) {
        FM_HullGlowParam param = new FM_HullGlowParam();
        param.loc = new Vector2f(loc);
        param.attachEntity = attachEntity;
        param.fadeIn = fadeIn;
        param.fadeIdle = fadeIdle;
        param.fadeOut = fadeOut;
        param.baseColor = baseColor;
        param.maskColor = maskColor;
        param.maskWidth = maskWidth;
        param.maskLength = maskLength;
        param.facing = facing;
        param.texPieces = texPieces;
        param.mask = mask;
        param.base = base;

        param.alpha = 0;
        param.cycleTimer = 0;
        param.flowLevel = 0;
        param.facingVector = MathUtils.getPoint(FM_Misc.ZERO,param.base.getHeight() * 0.5f,facing);
        param.maskLoc = Vector2f.add(param.loc, param.facingVector ,new Vector2f());

        //在最后加入前把facingVector标准化
        //param.facingVector = VectorUtils.resize(param.facingVector,1f);
        return param;
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        //GL14.glBlendEquation(GL14.GL_FUNC_ADD);


        //下次记得搞清楚混合方式和层数特性…………
        //并记住是先画底层的再叠上层的（也就是先画的会作为后续画的DST
        //在ABOVE_SHIPS_AND_MISSILES_LAYER这一层上，开始默认的背景alpha是1
        for (FM_HullGlowParam param : params) {
            if (layer == CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER){
                param.base.setBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
                param.base.bindTexture();
                param.base.setAngle(param.facing - 90f);
                param.base.setColor(param.baseColor);
                param.base.renderAtCenter(param.loc.x,param.loc.y);
                renderMask(param.maskLoc.x,param.maskLoc.y,param.facing,param.maskWidth,param.maskLength,
                        param.alpha * viewport.getAlphaMult(),param.flowLevel,param.texPieces, segments, param.mask,param.maskColor);
            }
            if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER){

            }
        }

//		GL11.glPopMatrix();
    }


    //另一个矩形，但是有纹路流动等等，遮罩层
    private void renderMask(float x, float y,
                            float direction, float width, float length,
                            float alphaMult, float fluidLevel, float texPieces ,
                            int segments, SpriteAPI tex, Color color) {
        //角度与节点处理
//        float startRad = (float) Math.toRadians(0);
//        float endRad = (float) Math.toRadians(360);
//        float spanRad = Misc.normalizeAngle(endRad - startRad);
//        float anglePerSegment = spanRad / segments;

        GL11.glPushMatrix();
        //平移函数，矩阵相关操作
        GL11.glTranslatef(x, y, 0);
        //旋转函数，矩阵相关操作
        GL11.glRotatef(direction, 0, 0, 1);
        //纹理等
//        GL11.glDepthMask(false);
//        GL11.glDisable(GL11.GL_DEPTH_TEST);
//        GL11.glEnable(GL11.GL_RGBA_MODE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        tex.bindTexture();

        //混合颜色的方式.aya
        GL11.glEnable(GL11.GL_BLEND);
        //GL11.glBlendFunc(GL11.GL_DST_ALPHA,GL11.GL_ONE_MINUS_DST_ALPHA);
        //使用上一层的alpha
        //GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_ALPHA,GL11.GL_DST_ALPHA);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_DST_ALPHA);

        GL11.glColor4ub((byte)color.getRed(),
                (byte)color.getGreen(),
                (byte)color.getBlue(),
                (byte)((float) color.getAlpha() * alphaMult));

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
            //某种意义上的核心
            float pieceFraction = i/segments;
            //同向平行.aya
            float x1 = (-0.5f + pieceFraction) * length;
            float y1 = -0.5f * width;

            float x2 = (-0.5f + pieceFraction) * length;
            float y2 = 0.5f * width;

            //其实肯定的还可以整点更劲的

            //正确的在四边形上表示纹理的方式：
            //图的各个顶点（整个的肯定是矩形啦）
            //与四边形的定点对应
            //因为是循环绘制所以起点对起点的意思（）
            //s与t也是对应x与y轴的

            //仍然不理解为什么只有减法才能让方向变得正常
//            float texCoordX = MagicAnim.cycle( pieceFraction - fluidLevel ,0f,1f);

            //颜色与渐变相关参数
            float alpha;
            if (pieceFraction <= 0.4f){
                alpha = MagicAnim.smooth(pieceFraction/0.4f) ;
            }else if (pieceFraction <= 0.8f){
                alpha = 1f;
            }else {
                alpha = MagicAnim.smooth((1f - pieceFraction)/0.2f);
            }

            GL11.glTexCoord2f(0, pieceFraction);
            GL11.glColor4ub((byte)color.getRed(),
                    (byte)color.getGreen(),
                    (byte)color.getBlue(),
                    (byte)((float) color.getAlpha() * alphaMult * alpha));
            GL11.glVertex2f(x1, y1);

            GL11.glTexCoord2f(1, pieceFraction);
            GL11.glColor4ub((byte)color.getRed(),
                    (byte)color.getGreen(),
                    (byte)color.getBlue(),
                    (byte)((float) color.getAlpha() * alphaMult * alpha));
            GL11.glVertex2f(x2, y2);

            if (last) break;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }

//    //直接渲染矩形的图像
//    private void renderBase(float x, float y, float facing, int segments, float alphaMult,
//                            SpriteAPI tex, Color color) {
//        GL11.glPushMatrix();
//        //平移函数，矩阵相关操作
//        GL11.glTranslatef(x, y, 0);
//        //旋转函数，矩阵相关操作
//        GL11.glRotatef(facing, 0, 0, 1);
//        //纹理等
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        tex.bindTexture();
//
//
//        //混合颜色的方式.aya
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_DST_ALPHA);
//        GL11.glColor4ub((byte) color.getRed(),
//                (byte) color.getGreen(),
//                (byte) color.getBlue(),
//                (byte) ((float) color.getAlpha() * alphaMult));
//
//        //一种特殊好用的四边形的绘制方法.png
//        //重点系列
//        //非常惊喜的独特顺序（）
//        //例如如果用此法绘制一个矩形，顺序是 先一条边 再对角线连接至起点同侧 再另一条边
//        //而不是一笔画下来的正常顺序
//        //STRIP的原因（？
//        //有他自己的好处
//        //比如给两个同样方向的平行向量（）
//
//        GL11.glBegin(GL11.GL_QUAD_STRIP);
//        float width = tex.getHeight();
//        float length = tex.getWidth();
//
//        for (float i = 0; i < segments + 1; i++) {
//            boolean last = i == segments;
//            if (last) i = 0;
//            //某种意义上的核心
//            float pieceFraction = i/segments;
//            //同向平行.aya
//            float x1 = (-0.5f + pieceFraction) * length;
//            float y1 = -0.5f * width;
//
//            float x2 = (-0.5f + pieceFraction) * length;
//            float y2 = 0.5f * width;
//
//            //其实肯定的还可以整点更劲的
//
//            //正确的在四边形上表示纹理的方式：
//            //图的各个顶点（整个的肯定是矩形啦）
//            //与四边形的定点对应
//            //因为是循环绘制所以起点对起点的意思（）
//            //s与t也是对应x与y轴的
//
//            //颜色与渐变相关参数
//            //下面的是两端渐变的内容, 这里暂时不用
//
//            GL11.glTexCoord2f(0,pieceFraction);
////            GL11.glColor4ub((byte)color.getRed(),
////                    (byte)color.getGreen(),
////                    (byte)color.getBlue(),
////                    (byte)((float) color.getAlpha() * alpha));
//            GL11.glVertex2f(x1, y1);
//
//            GL11.glTexCoord2f(1,pieceFraction);
////            GL11.glColor4ub((byte)color.getRed(),
////                    (byte)color.getGreen(),
////                    (byte)color.getBlue(),
////                    (byte)((float) color.getAlpha() * alpha));
//            GL11.glVertex2f(x2, y2);
//
//            if (last) break;
//        }
//        GL11.glEnd();
//        GL11.glPopMatrix();
//    }
}
