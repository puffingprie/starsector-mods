package data.weapons.test;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//目前未使用，保留了一种贝塞尔曲线的轨迹

public class FM_Alice_everyframe implements EveryFrameWeaponEffectPlugin {

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        if (engine == null) return;
        if (ship == null || !ship.hasLaunchBays()) return;

        if (!engine.getCustomData().containsKey("FM_Alice_everyframe")) {
            engine.getCustomData().put("FM_Alice_everyframe", new HashMap<>());
        }
        if (engine.isPaused()) return;

        List<ShipAPI> all_fighters = fighters(ship);


        Map<WeaponAPI, FighterState> currFighterState = (Map) engine.getCustomData().get("FM_Alice_everyframe");

        if (!currFighterState.containsKey(weapon)) {
            currFighterState.put(weapon, new FighterState());
        }

        currFighterState.get(weapon).timer = currFighterState.get(weapon).timer + amount;


        if (currFighterState.get(weapon).timer >= 1f) {
            currFighterState.get(weapon).timer = 0f;
            currFighterState.get(weapon).trail_id.clear();
        }

        float weapon_facing = weapon.getCurrAngle();
        //debug
        //engine.addFloatingText(weapon.getLocation(),String.valueOf(weapon_facing),10f,Color.WHITE,ship,1f,0f);

        Vector2f weapon_loc = weapon.getLocation();

        for (ShipAPI fighter : all_fighters) {

            float angle_0 = VectorUtils.getAngle(weapon_loc, fighter.getLocation());

            float diff = Misc.getAngleDiff(weapon_facing, angle_0);

            if (!currFighterState.get(weapon).trail_id.containsKey(fighter)) {
                currFighterState.get(weapon).trail_id.put(fighter, MagicTrailPlugin.getUniqueID());
            }

            if (MathUtils.isWithinRange(fighter, weapon.getLocation(), 2000f)
                    && diff <= 90f) {

                Vector2f begin = new Vector2f(weapon.getLocation());
                Vector2f end = new Vector2f(fighter.getLocation());

                float angle_b_e = Misc.normalizeAngle(VectorUtils.getAngle(begin, end));
                float distance = MathUtils.getDistance(begin, end);

                Vector2f mid = MathUtils.getMidpoint(begin, end);

                Vector2f medium = MathUtils.getPoint(mid, 0.5f * distance, angle_b_e + weapon_facing);

                Vector2f point_on_the_track = BezierCurvePoint(
                        currFighterState.get(weapon).timer,
                        begin,
                        end,
                        medium
                );
/*
                for (Vector2f point_on_the_track : BezierCurvePoints(20,begin,end,medium)){
                    engine.addSmoothParticle(point_on_the_track,new Vector2f(),10f,100f,1f,Color.cyan);
                }

 */


                //debug
                //engine.addFloatingText(fighter.getLocation(),point_on_the_track.toString(),10f,Color.WHITE,fighter,0f,0f);

                //engine.addSmoothParticle(point_on_the_track,new Vector2f(),10f,100f,1f,Color.cyan);


                if (currFighterState.get(weapon).timer < 1f && currFighterState.get(weapon).trail_id.containsKey(fighter)) {

                    MagicTrailPlugin.addTrailMemberAdvanced(
                            fighter,
                            currFighterState.get(weapon).trail_id.get(fighter),
                            Global.getSettings().getSprite("fx", "FM_trail_2"),
                            point_on_the_track,
                            0f,
                            0f,
                            fighter.getFacing() + 180f,
                            0f,
                            0f,
                            30f,
                            60f,
                            new Color(217, 55, 55, 255),
                            new Color(64, 249, 255, 102),
                            1f,
                            0.1f,
                            0.6f,
                            0.2f,
                            true,
                            50f,
                            200f,
                            0f,
                            null,
                            null,
                            CombatEngineLayers.ABOVE_SHIPS_LAYER,
                            60f


                    );

                }


            }

        }

    }

    private java.util.List<ShipAPI> fighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();
        for (ShipAPI fighter : Global.getCombatEngine().getShips()) {
            if (!fighter.isFighter()) continue;
            if (fighter.getWing() == null) continue;
            if (fighter.getWing().getSourceShip() != carrier) continue;

            result.add(fighter);

        }
        return result;
    }
/*
    private List<Vector2f> PointsOnArc(int numOfPoints, Vector2f begin, Vector2f end, float r) {
        ArrayList<Vector2f> points = new ArrayList<>();

        Vector2f mid = MathUtils.getMidpoint(begin,end);
        float distance_b_e = MathUtils.getDistance(begin,end);
        float angle_b_e = Misc.normalizeAngle(VectorUtils.getAngle(begin,end));
        float distance_m_c = (float) Math.sqrt(r*r - 0.25f*distance_b_e*distance_b_e);

        Vector2f center = MathUtils.getPoint(mid,distance_m_c,angle_b_e - 90f);

        float angle_b = VectorUtils.getAngle(center,begin);
        float angle_e = VectorUtils.getAngle(center,end);

        float diff = angle_e - angle_b;

        for (int i = 0; i < numOfPoints; i = i + 1){

          Vector2f point = MathUtils.getPoint(center,r,angle_b + (diff/numOfPoints) * i );

          points.add(point);

        }




        return points;
    }

 */


/*
    private List<Vector2f> BezierCurvePoints (int numOfPoints, Vector2f begin, Vector2f end, Vector2f medium){

        ArrayList<Vector2f> points = new ArrayList<>();

        for (int i = 0 ; i < numOfPoints ; i = i + 1){

            Vector2f point;

            float t = (float)i / (float)numOfPoints;

            Vector2f p0 = new Vector2f((ReadableVector2f) begin.scale(1-t*t));
            Vector2f p1 = new Vector2f((ReadableVector2f) medium.scale(2*t*(1-t)));
            Vector2f p2 = new Vector2f((ReadableVector2f) end.scale(t*t));

            point = new Vector2f(p0.x + p1.x + p2.x,p0.y + p1.y + p2.y);

            points.add(point);



        }



        return points;

    }

 */

    private Vector2f BezierCurvePoint(float t, Vector2f begin, Vector2f end, Vector2f medium) {

        Vector2f point;

        Vector2f p0 = (Vector2f) new Vector2f(0, 0).scale(1 - t * t);
        Vector2f p1 = new Vector2f((ReadableVector2f) Vector2f.sub(medium, begin, new Vector2f()).scale(2 * t * (1 - t)));
        Vector2f p2 = new Vector2f((ReadableVector2f) Vector2f.sub(end, begin, new Vector2f()).scale(t * t));


        point = new Vector2f(p0.x + p1.x + p2.x, p0.y + p1.y + p2.y);

        return Vector2f.add(point, begin, point);

    }

    private final static class FighterState {
        float timer;

        HashMap<ShipAPI, Float> trail_id;


        private FighterState() {

            timer = 0f;

            trail_id = new HashMap<>();
        }
    }


}
