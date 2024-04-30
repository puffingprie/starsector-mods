package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.FantasyAliceMod;
import data.utils.FM_Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicAnim;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Origin is data.utils.visual.ChainVisual, which scripted by AnyIDElse.
 * I just did some adjust.
 * Message left by homejerry99
 */
public class FM_ChainVisual extends BaseCombatLayeredRenderingPlugin {

    private static final Color LINECOLOR = new Color(80, 156, 255);

    private final WeaponAPI anchor;
    //机体与对应连线状态
    private final Map<ShipAPI, Float> wings = new HashMap<>();

    private final java.util.List<ShipAPI> toRemove = new ArrayList<>();

    private boolean valid = true;
    private float effectLevel = 1f;

    private boolean effectlevel_loop = false;

    public FM_ChainVisual(WeaponAPI anchor) {
        this.anchor = anchor;
    }

    @Override
    public float getRenderRadius() {
        return 1000000f;
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (anchor.getShip() == null) return;

        ShipAPI ship = anchor.getShip();


        //死亡判断与特效淡出
        if (!anchor.getShip().isAlive()) {
            effectLevel -= amount * 3f;
            if (effectLevel <= 0f) {
                effectLevel = 0f;
                valid = false;
            }

            return;
        } else {

            if (!effectlevel_loop) {
                effectLevel = effectLevel - amount * 0.6f;
                if (effectLevel <= 0.2f) {
                    effectLevel = effectLevel - amount * 0.05f;
                }
                if (effectLevel <= 0f) {
                    effectLevel = 0f;
                    effectlevel_loop = true;
                }
            } else {
                effectLevel = effectLevel + amount * 1.2f;
                if (effectLevel >= 1f) {
                    effectLevel = 1f;
                    effectlevel_loop = false;
                }
            }

        }


        Vector2f location = anchor.getLocation();
        entity.getLocation().set(location);

        float weapon_facing = anchor.getCurrAngle();
        Vector2f begin = location;


        //舰载机连线相关
        List<ShipAPI> fighters = FM_Misc.getFighters(ship);

        for (ShipAPI fighter : fighters) {
            if (!fighter.isFighter()) continue;
            if (fighter.getWing() == null) continue;


            if (!wings.containsKey(fighter)) wings.put(fighter, 0f);

        }

        for (ShipAPI wing : wings.keySet()) {
            float wingLevel = wings.get(wing);
            if (wing.isAlive()) {

                Vector2f end = wing.getLocation();

                float angle_0 = VectorUtils.getAngle(begin, end);

                float diff = Misc.getAngleDiff(weapon_facing, angle_0);

                if (diff >= 90f) {
                    wingLevel -= amount * 1.5f;
                    wingLevel = Math.max(wingLevel, 0f);

                } else {

                    wingLevel += amount * 2f;
                    wingLevel = Math.min(wingLevel, 1f);

                }

                if (wingLevel == 0f) {
                    toRemove.add(wing);
                }


            } else {
                toRemove.add(wing);
            }

            wings.put(wing, wingLevel);
        }

        for (ShipAPI remove : toRemove) {
            wings.remove(remove);
        }
        toRemove.clear();
    }

    @Override
    public void init(CombatEntityAPI entity) {
        super.init(entity);
        advance(0f);
    }

    @Override
    public boolean isExpired() {
        return !valid;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {

        //Has been adjusted......


        float alphaMult = viewport.getAlphaMult() * MagicAnim.smooth(effectLevel) * 0.7f;

        if (layer == CombatEngineLayers.ABOVE_PARTICLES_LOWER) {


            Vector2f begin = entity.getLocation();

            float weapon_facing = anchor.getCurrAngle();

            for (ShipAPI wing : wings.keySet()) {


                if (!MathUtils.isWithinRange(wing, entity,
                        FantasyAliceMod.EFFECT_RANGE)
                ) continue;

                Vector2f end = wing.getLocation();

                float angle_b_e = Misc.normalizeAngle(VectorUtils.getAngle(begin, end));
                float distance = MathUtils.getDistance(begin, end);

                Vector2f midPoint = MathUtils.getMidpoint(begin, end);

                Vector2f medium = MathUtils.getPoint(midPoint, 0.2f * distance, angle_b_e + weapon_facing);

                midPoint.setX(medium.x);
                midPoint.setY(medium.y); // for test

                float wingLevel = wings.get(wing);

                renderLine(begin, midPoint, end, 0.1d, alphaMult * wingLevel);


            }
        }
    }

    public void renderLine(Vector2f anchor, Vector2f mid, Vector2f target, double t, float alphaMult) {

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0f, 0f);
        GL11.glRotatef(0f, 0f, 0f, 1f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4ub((byte) LINECOLOR.getRed(), (byte) LINECOLOR.getGreen(), (byte) LINECOLOR.getBlue(), (byte) (int) (LINECOLOR.getAlpha() * alphaMult));

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (double k = 0; k <= 1d; k += t) {
            double r = 1d - k;
            double x = Math.pow(r, 2) * anchor.getX() + 2d * k * r * mid.getX() + Math.pow(k, 2) * target.getX();
            double y = Math.pow(r, 2) * anchor.getY() + 2d * k * r * mid.getY() + Math.pow(k, 2) * target.getY();

            GL11.glVertex2f((float) x, (float) y);

            //logger的debug
            //Global.getLogger(this.getClass()).info("drawing " + k);
        }
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}
