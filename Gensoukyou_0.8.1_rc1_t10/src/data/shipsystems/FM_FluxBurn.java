package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import data.utils.visual.FM_MisfortuneAbsorbVisual;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.List;
import java.util.Set;

public class FM_FluxBurn extends BaseShipSystemScript {

    public static final float RANGE = 700f;
//    public static final float SPEED_FLOOR = 100f;
    public static final float SPEED_FRACTION = 0.66f;
    public static final float HARDFLUX_DIS_FRAC = 3f;
    private FM_MisfortuneAbsorbVisual.FM_MAVParams visual = null;
    private CombatEntityAPI entityForVisual = null;
    private float particleTimer = 0f;
    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);
        if (Global.getCombatEngine() == null)return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!(stats.getEntity() instanceof ShipAPI))return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        float range = FM_Misc.getSystemRange(ship,RANGE);

        if (ship.getShield().isOff()){
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,ship.getMouseTarget(),0);
        }
        if (ship.getShield().isOn()){
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
        }

        stats.getFluxDissipation().modifyMult(id,HARDFLUX_DIS_FRAC * effectLevel);
        stats.getHardFluxDissipationFraction().modifyFlat(id,HARDFLUX_DIS_FRAC * effectLevel);

        particleTimer = particleTimer + engine.getElapsedInLastFrame();

        List<ShipAPI> enemies = AIUtils.getEnemiesOnMap(ship);
        String buffId = id + ship.getId();
        out:
        for (ShipAPI enemy : enemies){
            if (!enemy.isFighter())continue;
            if (MathUtils.isWithinRange(enemy,ship,range)){
                Set<String> keys = enemy.getMutableStats().getMaxSpeed().getMultMods().keySet();
                for (String key : keys){
                    if (key.startsWith(id) && !key.equals(buffId)){
                        continue out;
                    }
                }
                //以上用于防止buff多次叠加
                if (particleTimer >= 0.1f){
                    engine.addHitParticle(
                            enemy.getLocation(),
                            FM_Misc.ZERO,
                            50f,
                            1f,
                            0.3f,
                            FM_Colors.FM_ORANGE_FLARE_FRINGE
                    );
                }
                enemy.getMutableStats().getMaxSpeed().modifyMult(buffId,1 - SPEED_FRACTION * effectLevel);

            }else {
                enemy.getMutableStats().getMaxSpeed().unmodifyMult(buffId);
            }
        }

        if (particleTimer >= 0.1f){
            engine.addNebulaParticle(
                    ship.getLocation(),
                    FM_Misc.ZERO,
                    MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.5f, ship.getCollisionRadius() * 1.1f ),
                    1.5f,
                    0.4f,
                    0.5f,
                    MathUtils.getRandomNumberInRange(0.6f,0.9f),
                    FM_Colors.FM_TEXT_RED,
                    true
            );
            for (int i = 0 ; i <= 5 ; i = i + 1){
                engine.addNebulaParticle(
                        MathUtils.getRandomPointOnCircumference(ship.getLocation(),range),
                        FM_Misc.ZERO,
                        MathUtils.getRandomNumberInRange(ship.getCollisionRadius() * 0.5f, ship.getCollisionRadius() * 1.1f ),
                        1.5f,
                        0.4f,
                        0.2f,
                        MathUtils.getRandomNumberInRange(0.8f,1.5f),
                        FM_Colors.FM_TEXT_RED,
                        true
                );
            }
            particleTimer = particleTimer - 0.1f;
        }

        //作用范围视觉效果
        if (visual == null) {
            visual = new FM_MisfortuneAbsorbVisual.FM_MAVParams();
            visual.additive = true;
            visual.fadeIn = ship.getSystem().getSpecAPI().getIn();
            visual.fadeIdle = ship.getSystem().getSpecAPI().getActive();
            visual.fadeOut = ship.getSystem().getSpecAPI().getOut();
            visual.radius = range;
            visual.thickness = 30f;
            visual.color = FM_Colors.FM_TEXT_RED;
            //visual.texForRing = Global.getSettings().getSprite("misc", "slipstream_edge");
            visual.perSegment = 36f;
            visual.noiseSpikes = 3f;
            entityForVisual = Global.getCombatEngine().addLayeredRenderingPlugin(new FM_MisfortuneAbsorbVisual(visual,ship));
        }
        if (visual != null && entityForVisual != null) {
            visual.loc.set(ship.getLocation());
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);

        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        if (Global.getCombatEngine() == null) return;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        String buffId = id + ship.getId();

        stats.getFluxDissipation().unmodifyMult(id);
        stats.getHardFluxDissipationFraction().unmodifyFlat(id);

        for (ShipAPI ship_in_range : AIUtils.getEnemiesOnMap(ship)) {
            ship_in_range.getMutableStats().getMaxSpeed().unmodifyMult(buffId);
        }

        visual = null;
        entityForVisual = null;
        particleTimer = 0f;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(I18nUtil.getShipSystemString("FM_FluxBurnInfo0") + (int) (SPEED_FRACTION * 100f * effectLevel) + "%", false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_FluxBurnInfo1") + (int) (HARDFLUX_DIS_FRAC * 100f * effectLevel) + "%", false);
        }
        return null;
    }
}
