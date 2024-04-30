package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WingRole;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.FM_ProjectEffect;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;
import org.magiclib.util.MagicAnim;

import java.util.HashMap;
import java.util.List;

public class FM_ReversedWing extends BaseShipSystemScript {

    public static final float VEL_EFFECT = 600f;
    private final HashMap<ShipAPI,Float> TrailIdMap = new HashMap<>();
    private boolean effectOn = false;
    private boolean startPointConfirmed = false;
    private Vector2f startPoint = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);

        if (Global.getCombatEngine() == null)return;
        if (!(stats.getEntity() instanceof ShipAPI))return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (!startPointConfirmed){
            startPoint = ship.getLocation();
            startPointConfirmed = true;
        }

        List<ShipAPI> fighters = FM_Misc.getFighters(ship,true);
        for (ShipAPI fighter : fighters){

            if (!TrailIdMap.containsKey(fighter)){
                TrailIdMap.put(fighter,MagicTrailPlugin.getUniqueID());
            }
            float trialId = TrailIdMap.get(fighter);

            if (effectLevel > 0 && effectLevel < 1){
                Vector2f mid = MathUtils.getMidpoint(startPoint,fighter.getLocation());
                //float distanceSq = MathUtils.getDistanceSquared(startPoint,fighter.getLocation());
                float dir = VectorUtils.getAngle(startPoint,fighter.getLocation());
                float k;
                if (Math.random() >= 0.5f){
                    k = 1;
                }else {
                    k = -1;
                }
                float concave = 90f * k;
                Vector2f medium = MathUtils.getPoint(mid,50f,dir + concave);
                float anim = MagicAnim.smooth(effectLevel);
                Vector2f effectPoint = FM_Misc.BezierCurvePoint(anim,startPoint,fighter.getLocation(),medium);
                MagicTrailPlugin.addTrailMemberAdvanced(
                        fighter,
                        trialId,
                        Global.getSettings().getSprite("fx", "base_trail_smooth"),
                        effectPoint,
                        0,
                        0,
                        -90f,
                        0f,
                        0f,
                        6f,
                        10f,
                        FM_Colors.FM_TEXT_BLUE,
                        FM_Colors.FM_ORANGE_FLARE_FRINGE,
                        0.8f,
                        0.1f,
                        0.4f,
                        0.3f,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        256f,
                        10,
                        0,
                        null,
                        null,
                        CombatEngineLayers.BELOW_SHIPS_LAYER,
                        1f
                );
            }

            if (effectLevel >= 1){
                if (!effectOn){
                    if (fighter.getWing().getRole() == WingRole.BOMBER){
                        for (int i = 0; i < 7; i = i + 1){
                            engine.spawnProjectile(
                                    fighter,
                                    engine.createFakeWeapon(fighter,"FM_Amulet_B"),
                                    "FM_Amulet_B",
                                    fighter.getLocation(),
                                    fighter.getFacing() + MathUtils.getRandomNumberInRange(-90,90),
                                    FM_Misc.ZERO
                            );
                        }
                        Vector2f velOrigin = fighter.getVelocity();
                        Vector2f velEx = MathUtils.getPoint(new Vector2f(), VEL_EFFECT, ship.getFacing());
                        Vector2f.add(velEx, velOrigin, fighter.getVelocity());
                    }else {
                        for (int i = 0; i < 3; i = i + 1){
                            engine.spawnProjectile(
                                    fighter,
                                    engine.createFakeWeapon(fighter,"FM_Amulet_B"),
                                    "flarelauncher3",
                                    fighter.getLocation(),
                                    MathUtils.getRandomNumberInRange(0,360),
                                    FM_Misc.ZERO
                            );
                        }
                        Vector2f velOrigin = fighter.getVelocity();
                        Vector2f velEx = MathUtils.getPoint(new Vector2f(), VEL_EFFECT, -ship.getFacing());
                        Vector2f.add(velEx, velOrigin, fighter.getVelocity());
                    }
                    engine.spawnExplosion(
                            fighter.getLocation(),
                            FM_Misc.ZERO,
                            FM_Colors.FM_BLUE_FLARE_CORE,
                            fighter.getCollisionRadius() + 40f,
                            MathUtils.getRandomNumberInRange(0.35f,0.45f)
                    );
                    engine.addNebulaParticle(fighter.getLocation(),FM_Misc.ZERO,fighter.getCollisionRadius() + 30f,
                            2f,0.3f,0.4f,0.6f,FM_Colors.FM_TEXT_BLUE);
                    engine.addHitParticle(fighter.getLocation(),FM_Misc.ZERO,fighter.getCollisionRadius() * 1.5f,1f,0.2f,FM_Colors.FM_GREEN_EMP_CORE);
                }
                fighter.blockCommandForOneFrame(ShipCommand.DECELERATE);
                fighter.blockCommandForOneFrame(ShipCommand.ACCELERATE);
                fighter.addAfterimage(
                        FM_ProjectEffect.EFFECT_7,
                        0,
                        0,
                        -fighter.getVelocity().x * 0.25f,
                        -fighter.getVelocity().y * 0.25f,
                        3f * effectLevel,
                        0f,
                        0.2f,
                        0.4f,
                        true,
                        true,
                        true
                );
            }
        }

        if (effectLevel >= 1f){
            effectOn = true;
        }

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);

        TrailIdMap.clear();
        effectOn = false;
        startPointConfirmed = false;
        startPoint = null;
    }


}
