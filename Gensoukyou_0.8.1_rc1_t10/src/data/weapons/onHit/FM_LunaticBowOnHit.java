package data.weapons.onHit;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.utils.FM_Colors;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicFakeBeamPlugin;

import java.util.List;

public class FM_LunaticBowOnHit implements OnHitEffectPlugin {


    public static final String MINELAYER = "FM_LunaticBow_minelayer";

    private BaseEveryFrameCombatPlugin plugin = null;


    @Override
    public void onHit(final DamagingProjectileAPI projectile, final CombatEntityAPI target, final Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, final CombatEngineAPI engine) {

        if (target == null) return;
        if (!(target instanceof ShipAPI)) return;
        if (projectile.getSource() == null) return;
        if (projectile.getWeapon() == null) return;

        engine.spawnExplosion(point, new Vector2f(), FM_Colors.FM_RED_EXPLOSION, 100f, 4f);

        //直接构造一个每帧来解决延时生成的问题
        if (plugin == null) {
            plugin = new BaseEveryFrameCombatPlugin() {

                public float TIMER = 0f;
                public int NUM_OF_MINE = 5;
                public boolean BEGIN = true;
                public Vector2f TARGET_LOC = new Vector2f();
                public float EFFECT_RADIUS = 0f;
                public Vector2f LINE = new Vector2f();
                public float BASE_FACING = 0f;

                public Vector2f LOC1;
                public Vector2f LOC2;
                public Vector2f LOC3;
                public Vector2f LOC4;


                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    super.advance(amount, events);
                    if (Global.getCombatEngine().isPaused()) return;
                    TIMER = TIMER + amount;
                    if (BEGIN) {

//                        engine.spawnProjectile(projectile.getSource(),projectile.getWeapon(),"minelayer1",point,projectile.getFacing(),new Vector2f());


                        TARGET_LOC = target.getLocation();
                        EFFECT_RADIUS = MathUtils.getDistance(point, TARGET_LOC);
                        EFFECT_RADIUS = Math.max(30f, EFFECT_RADIUS);

                        LINE = Vector2f.sub(point, TARGET_LOC, new Vector2f());
                        BASE_FACING = VectorUtils.getFacing(LINE);

//                        spawnMine(projectile.getSource(),point);
                        NUM_OF_MINE = NUM_OF_MINE - 1;
                        BEGIN = false;

                        LOC1 = MathUtils.getPoint(TARGET_LOC, MathUtils.getRandomNumberInRange(EFFECT_RADIUS - 15f, EFFECT_RADIUS + 15f) * 0.7f,
                                MathUtils.getRandomNumberInRange(BASE_FACING - 35f, BASE_FACING - 25f));
                        LOC2 = MathUtils.getPoint(TARGET_LOC, MathUtils.getRandomNumberInRange(EFFECT_RADIUS - 15f, EFFECT_RADIUS + 15f) * 0.7f,
                                MathUtils.getRandomNumberInRange(BASE_FACING + 25f, BASE_FACING + 35f));
                        LOC3 = MathUtils.getPoint(TARGET_LOC, MathUtils.getRandomNumberInRange(EFFECT_RADIUS - 15f, EFFECT_RADIUS + 15f) * 0.5f,
                                MathUtils.getRandomNumberInRange(BASE_FACING - 95f, BASE_FACING - 85f));
                        LOC4 = MathUtils.getPoint(TARGET_LOC, MathUtils.getRandomNumberInRange(EFFECT_RADIUS - 15f, EFFECT_RADIUS + 15f) * 0.5f,
                                MathUtils.getRandomNumberInRange(BASE_FACING + 85f, BASE_FACING + 95f));

                        MagicFakeBeamPlugin.addBeam(0.3f, 0.2f, 15f, point, VectorUtils.getAngle(point, LOC1), MathUtils.getDistance(point, LOC1), FM_Colors.FM_RED_EMP_CORE, FM_Colors.FM_RED_EMP_FRINGE);
                        MagicFakeBeamPlugin.addBeam(0.3f, 0.2f, 15f, point, VectorUtils.getAngle(point, LOC2), MathUtils.getDistance(point, LOC2), FM_Colors.FM_RED_EMP_CORE, FM_Colors.FM_RED_EMP_FRINGE);

//                        FM_StripVisual.FM_StripParam visual1 = new FM_StripVisual.FM_StripParam(point,LOC1,20f,PING,false,0.3f);
//                        FM_StripVisual.FM_StripParam visual2 = new FM_StripVisual.FM_StripParam(point,LOC2,20f,PING,false,0.3f);
//                        engine.addLayeredRenderingPlugin( new FM_StripVisual(visual1));
//                        engine.addLayeredRenderingPlugin( new FM_StripVisual(visual2));
                    }
                    if (!BEGIN && TIMER > 0.2f && NUM_OF_MINE == 4) {
                        spawnMine(projectile.getSource(), projectile.getWeapon(), LOC1);
                        spawnMine(projectile.getSource(), projectile.getWeapon(), LOC2);

                        MagicFakeBeamPlugin.addBeam(0.3f, 0.2f, 15f, LOC1, VectorUtils.getAngle(LOC1, LOC3), MathUtils.getDistance(LOC1, LOC3), FM_Colors.FM_RED_EMP_CORE, FM_Colors.FM_RED_EMP_FRINGE);
                        MagicFakeBeamPlugin.addBeam(0.3f, 0.2f, 15f, LOC2, VectorUtils.getAngle(LOC2, LOC4), MathUtils.getDistance(LOC2, LOC4), FM_Colors.FM_RED_EMP_CORE, FM_Colors.FM_RED_EMP_FRINGE);


//                        FM_StripVisual.FM_StripParam visual3 = new FM_StripVisual.FM_StripParam(LOC1,LOC3,20f,PING,false,0.3f);
//                        FM_StripVisual.FM_StripParam visual4 = new FM_StripVisual.FM_StripParam(LOC2,LOC4,20f,PING,false,0.3f);
//                        engine.addLayeredRenderingPlugin( new FM_StripVisual(visual3));
//                        engine.addLayeredRenderingPlugin( new FM_StripVisual(visual4));

//                        engine.spawnProjectile(projectile.getSource(),projectile.getWeapon(),"minelayer1",loc1,projectile.getFacing(),new Vector2f());
//                        engine.spawnProjectile(projectile.getSource(),projectile.getWeapon(),"minelayer1",loc2,projectile.getFacing(),new Vector2f());
                        NUM_OF_MINE = NUM_OF_MINE - 2;
                    } else if (!BEGIN && TIMER > 0.4f && NUM_OF_MINE == 2) {

                        spawnMine(projectile.getSource(), projectile.getWeapon(), LOC3);
                        spawnMine(projectile.getSource(), projectile.getWeapon(), LOC4);
//                        engine.spawnProjectile(projectile.getSource(),projectile.getWeapon(),"minelayer1",loc1,projectile.getFacing(),new Vector2f());
//                        engine.spawnProjectile(projectile.getSource(),projectile.getWeapon(),"minelayer1",loc2,projectile.getFacing(),new Vector2f());
                        NUM_OF_MINE = NUM_OF_MINE - 2;
                    }
                    if (NUM_OF_MINE <= 0) {
                        //debug
                        //engine.addFloatingText(point,"TEST",100f,Color.WHITE,target,0f,0f);

                        Global.getCombatEngine().removePlugin(plugin);
                    }
                }
            };
            Global.getCombatEngine().addPlugin(plugin);
            Global.getSoundPlayer().playSound("hit_heavy_energy", 1f, 0.45f, point, new Vector2f());
        }

    }

    public void spawnMine(ShipAPI source, WeaponAPI weapon, Vector2f mineLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();

        MissileAPI mine = (MissileAPI) engine.spawnProjectile(source, weapon,
                MINELAYER,
                mineLoc,
                (float) Math.random() * 360f, null);

        // "spawned" does not include this mine

        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(
                    source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
        }

        float fadeInTime = 0f;
        mine.getVelocity().scale(0);
        mine.fadeOutThenIn(fadeInTime);

        float liveTime = 0f;
        //liveTime = 0.01f;
        mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
        mine.addDamagedAlready(source);
        mine.setNoMineFFConcerns(true);

    }


}
