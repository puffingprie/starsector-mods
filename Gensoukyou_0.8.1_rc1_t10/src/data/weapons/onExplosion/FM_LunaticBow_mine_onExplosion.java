package data.weapons.onExplosion;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import org.magiclib.util.MagicLensFlare;

import java.awt.*;

public class FM_LunaticBow_mine_onExplosion implements ProximityExplosionEffect {

    public static final Color FRINGE = new Color(255, 80, 80, 255);
    public static final Color CORE = new Color(255, 234, 234, 255);

//    public static final Color PING = new Color(100, 60,255,255);


    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        MagicLensFlare.createSharpFlare(engine, originalProjectile.getSource(), originalProjectile.getLocation(), 10f, 230f, 0f, FRINGE, CORE);

//        for (int i = 0 ; i < 30 ; i = i + 1){
//            engine.addSmoothParticle(originalProjectile.getLocation(), MathUtils.getRandomPointInCircle(new Vector2f(),MathUtils.getRandomNumberInRange(100f,120f)),8f,100f,2f,P);
//        }
    }
}
