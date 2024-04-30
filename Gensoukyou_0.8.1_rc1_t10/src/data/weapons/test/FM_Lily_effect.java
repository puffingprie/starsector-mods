package data.weapons.test;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.utils.FM_ProjectEffect;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

//——————————————————未使用——————————————————
public class FM_Lily_effect implements EveryFrameWeaponEffectPlugin {

    private float TIMER = 0f;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        Vector2f weapon_loc = weapon.getLocation();

        Vector2f loc_1 = MathUtils.getPoint(weapon_loc, 10f, weapon.getCurrAngle() - 90);
        Vector2f loc_2 = MathUtils.getPoint(weapon_loc, 10f, weapon.getCurrAngle() + 90);
        Vector2f loc_3 = MathUtils.getPoint(weapon_loc, 5f, weapon.getCurrAngle() - 90);
        Vector2f loc_4 = MathUtils.getPoint(weapon_loc, 5f, weapon.getCurrAngle() + 90);

        if (!engine.getCustomData().containsKey("FM_Lily_effect")) {
            engine.getCustomData().put("FM_Lily_effect", new HashMap<>());
        }

        Map<DamagingProjectileAPI, Integer> project_choose = (Map) engine.getCustomData().get("FM_Lily_effect");


        if (weapon.getChargeLevel() >= 1) {
            TIMER = TIMER + amount;
            if (TIMER >= 0.02f) {
                engine.spawnProjectile(weapon.getShip(), weapon, "FM_ice_weapon_s", loc_1, weapon.getCurrAngle(), new Vector2f());
                engine.spawnProjectile(weapon.getShip(), weapon, "FM_ice_weapon_s", loc_2, weapon.getCurrAngle(), new Vector2f());
                engine.spawnProjectile(weapon.getShip(), weapon, "FM_ice_weapon_s", loc_3, weapon.getCurrAngle(), new Vector2f());
                engine.spawnProjectile(weapon.getShip(), weapon, "FM_ice_weapon_s", loc_4, weapon.getCurrAngle(), new Vector2f());
                TIMER = 0f;
            }

            Vector2f dir = MathUtils.getPoint(new Vector2f(), 1000f, weapon.getCurrAngle());
            //engine.addFloatingText(weapon.getLocation(),String.valueOf(dir),30f, Color.WHITE,weapon.getShip(),1f,1f);

            for (DamagingProjectileAPI project : FM_ProjectEffect.ProjectsThisFrame) {
                if (project.getWeapon() == weapon) {
                    float angle = VectorUtils.getAngle(project.getSpawnLocation(), weapon_loc);
                    float range = MathUtils.getDistance(project.getSpawnLocation(), weapon_loc);
                    //engine.addFloatingText(project.getLocation(),String.valueOf(angle),20f, Color.WHITE,project,1f,1f);
                    if (angle < 180 && !project_choose.containsKey(project)) {
                        if (range >= 8f) {
                            project_choose.put(project, 1);
                        } else {
                            project_choose.put(project, 3);
                        }
                    }
                    if (angle > 180 && !project_choose.containsKey(project)) {
                        if (range >= 8f) {
                            project_choose.put(project, 2);
                        } else {
                            project_choose.put(project, 4);
                        }

                    }
                    if (project_choose.get(project) == 1) {
                        sineEffect(project, project.getElapsed(), dir, 0.3f, 1.7f);
                    }
                    if (project_choose.get(project) == 2) {
                        sineEffect(project, project.getElapsed(), dir, -0.3f, 1.7f);
                    }
                    if (project_choose.get(project) == 3) {
                        sineEffect(project, project.getElapsed(), dir, 0.5f, 1f);
                    }
                    if (project_choose.get(project) == 4) {
                        sineEffect(project, project.getElapsed(), dir, -0.5f, 1f);
                    }

                }
            }
        }


    }

    //正弦轨迹
    //amount为弹头自身时间(Elapsed即可)，dir为初速(重点)，K为振幅相关(可为负数)，L为波长相关
    public void sineEffect(DamagingProjectileAPI project, float amount, Vector2f dir, float K, float L) {

        if (project.getWeapon() == null) return;

        float T = project.getWeapon().getRange() / dir.length();
        float a = (float) (2 * Math.PI / T);

        float y = (float) (K * FastTrig.cos(amount * a * L));


        Vector2f dir_y = new Vector2f();
        VectorUtils.rotate(dir, 90f, dir_y);

        Vector2f.add((Vector2f) dir_y.scale(y), dir, project.getVelocity());

        project.setFacing(VectorUtils.getFacing(project.getVelocity()));

    }


}
