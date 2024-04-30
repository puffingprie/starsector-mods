package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.visual.FM_DiamondParticle3DTest;
import data.utils.visual.FM_ParticleManager;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicFakeBeamPlugin;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.List;

public class FM_GhostDash extends BaseShipSystemScript {

    public static final float VEL_BUFF = 800f;
    public static final float DAMAGE = 400f;
    public static final Color AFTERIMAGE = new Color(214, 245, 255, 201);
    private boolean APPLY_FORCE = false;
    private float DAMAGE_APPLY_TIMER = 0f;
    private float VISUAL_TIMER = 0f;
    private float trailId = MagicTrailPlugin.getUniqueID();

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (Global.getCombatEngine() == null) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!ship.isAlive())return;

        FM_DiamondParticle3DTest manager = FM_ParticleManager.getDiamondParticleManager(engine);

        Vector2f trailPoint = ship.getLocation();

        if (!APPLY_FORCE) {
            Vector2f velOrigin = ship.getVelocity();
            Vector2f velEx = MathUtils.getPoint(new Vector2f(), VEL_BUFF, ship.getFacing());
            Vector2f.add(velEx, velOrigin, ship.getVelocity());
            APPLY_FORCE = true;
            trailId = MagicTrailPlugin.getUniqueID();
            trailPoint = MathUtils.getPoint(ship.getLocation(),ship.getCollisionRadius() * 3,ship.getFacing() + 180f);

            MagicRender.battlespace(
                    Global.getSettings().getSprite("fx","FM_modeffect_3"),
                    ship.getLocation(),
                    FM_Misc.ZERO,
                    new Vector2f(100,100),
                    new Vector2f(-100 * engine.getElapsedInLastFrame(),-100 * engine.getElapsedInLastFrame()),
                    MathUtils.getRandomNumberInRange(0,360),
                    0f,
                    FM_Colors.FM_BLUE_FLARE_CORE,
                    true,
                    0.1f,0.2f,0.3f
            );
            engine.addHitParticle(ship.getLocation(),FM_Misc.ZERO,120,1f,0.8f,FM_Colors.FM_PURPLE_RED_CORE);

            for (int i = 0; i < 16; i = i + 1){
                Vector2f loc = ship.getLocation();
                Vector2f vel = MathUtils.getRandomPointInCone(FM_Misc.ZERO,200f,ship.getFacing() + 120,ship.getFacing() + 240);
                float size = MathUtils.getRandomNumberInRange(8f,12f);
                manager.addDiamondParticle(
                        loc,
                        vel,
                        size,
                        0.05f,
                        MathUtils.getRandomNumberInRange(0.45f,0.7f),
                        FM_Colors.FM_PURPLE_RED_CORE,
                        7f,
                        MathUtils.getRandomNumberInRange(0,360f),
                        MathUtils.getRandomNumberInRange(180f,540f),
                        MathUtils.getRandomNumberInRange(90f,360f),
                        Math.random() < 0.5f
                );
                engine.addHitParticle(
                        loc,
                        vel,
                        size * 2f,
                        1f,MathUtils.getRandomNumberInRange(0.5f,0.75f),FM_Colors.FM_PURPLE_RED_CORE
                );
            }
        }

        stats.getDeceleration().modifyMult(id,0f);
        stats.getMaxSpeed().modifyFlat(id,VEL_BUFF);
        ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
        ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
        ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);

        DAMAGE_APPLY_TIMER = DAMAGE_APPLY_TIMER + engine.getElapsedInLastFrame();

        if (DAMAGE_APPLY_TIMER >= 0.1f){
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(ship,ship.getCollisionRadius());
            for (ShipAPI enemy : enemies){
                if (CollisionUtils.isPointWithinBounds(ship.getLocation(),enemy)){
                    engine.applyDamage(
                            enemy,
                            ship.getLocation(),
                            DAMAGE,
                            DamageType.ENERGY,
                            DAMAGE,
                            false,
                            false,
                            ship,
                            true
                    );
                    engine.addHitParticle(ship.getLocation(), FM_Misc.ZERO,150f,1f,0.4f,AFTERIMAGE);
                    float d = MathUtils.getRandomNumberInRange(ship.getCollisionRadius() - 10f,ship.getCollisionRadius() + 30f);
                    Vector2f begin = MathUtils.getRandomPointOnCircumference(ship.getLocation(),d);
                    MagicFakeBeamPlugin.addBeam(
                            0.1f,
                            0.3f,
                            15f,
                            begin,
                            VectorUtils.getAngle(begin,ship.getLocation()),
                            d * 2f,
                            FM_Colors.FM_PURPLE_RED_CORE,
                            FM_Colors.FM_TEXT_BLUE
                    );

                }
            }
            DAMAGE_APPLY_TIMER = DAMAGE_APPLY_TIMER - 0.1f;
        }

//        VISUAL_TIMER = VISUAL_TIMER + engine.getElapsedInLastFrame();
//        if (VISUAL_TIMER >= 0.03f){
//            for (int i = 0; i < 16; i = i + 1){
//                Vector2f loc = ship.getLocation();
//                Vector2f vel = MathUtils.getRandomPointInCone(FM_Misc.ZERO,200f,ship.getFacing() + 150,ship.getFacing() + 210);
//                float size = MathUtils.getRandomNumberInRange(8f,12f);
//                manager.addDiamondParticle(
//                        loc,
//                        vel,
//                        size,
//                        0.05f,
//                        0.5f,
//                        FM_Colors.FM_PURPLE_RED_CORE,
//                        7f,
//                        MathUtils.getRandomNumberInRange(0,360f),
//                        MathUtils.getRandomNumberInRange(180f,540f),
//                        MathUtils.getRandomNumberInRange(90f,360f),
//                        Math.random() < 0.5f
//                );
//                engine.addHitParticle(
//                        loc,
//                        vel,
//                        size * 2f,
//                        1f,0.4f,FM_Colors.FM_PURPLE_RED_CORE
//                );
//            }
//            VISUAL_TIMER = VISUAL_TIMER - 0.03f;
//        }

        ship.addAfterimage(
                AFTERIMAGE,
                0,
                0,
                -ship.getVelocity().x * 0.25f,
                -ship.getVelocity().y * 0.25f,
                10f * effectLevel,
                0f,
                0.1f,
                0.4f,
                true,
                true,
                true
        );



        MagicTrailPlugin.addTrailMemberAdvanced(
                ship,
                trailId,
                Global.getSettings().getSprite("fx", "base_trail_smooth"),
                trailPoint,
                0,
                0,
                ship.getFacing() + 180f,
                0f,
                0f,
                15f,
                10f,
                FM_Colors.FM_PURPLE_RED_CORE,
                FM_Colors.FM_TEXT_BLUE,
                1f,
                0.2f,
                0.3f,
                0.3f,
                GL11.GL_BLEND_SRC,
                GL11.GL_ONE_MINUS_CONSTANT_ALPHA,
                256f,
                10,
                10f,
                null,
                null,
                CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER,
                60f
        );

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        APPLY_FORCE = false;
        DAMAGE_APPLY_TIMER = 0f;
        trailId = MagicTrailPlugin.getUniqueID();
    }
}
