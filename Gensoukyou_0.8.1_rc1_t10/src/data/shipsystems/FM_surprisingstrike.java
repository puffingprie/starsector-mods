package data.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.List;
import java.util.*;

public class FM_surprisingstrike extends BaseShipSystemScript {


    public static final Object KEY_JITTER = new Object();

    public static final float DAMAGE_CHANGE_PERCENT = 0.5f;
    public static final float SPEED_BUFF = 75f;

    public static final Color JITTER_UNDER_COLOR = new Color(0, 183, 255, 87);
    public static final Color JITTER_COLOR = new Color(0, 204, 255, 156);

    private final Map<ShipAPI, Float> TRAIL_IDS = new HashMap<>();

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (Global.getCombatEngine() == null) return;


        if (effectLevel > 0) {
            float maxRangeBonus = 5f;
            float jitterRangeBonus = effectLevel * maxRangeBonus;
            //test
//            for (FighterLaunchBayAPI bay :  ship.getLaunchBaysCopy()){
//                Global.getCombatEngine().addFloatingText(bay.getWeaponSlot().computePosition(ship),"" + bay.getFastReplacements(),20f,Color.WHITE,ship,0f,0f);
//            }
            //
            for (ShipAPI fighter : getFighters(ship)) {
                //test
//                if (fighter.getWing().isReturning(fighter)){
//                    fighter.getWing().getReturnData(fighter).bay = ship.getLaunchBaysCopy().get(2);
//                }
                //
                if (!TRAIL_IDS.containsKey(fighter)) {
                    float k = MagicTrailPlugin.getUniqueID();
                    TRAIL_IDS.put(fighter, k);
                }

                if (fighter.isHulk()) continue;
                MutableShipStatsAPI fStats = fighter.getMutableStats();
//				fStats.getBallisticWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getEnergyWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);
//				fStats.getMissileWeaponDamageMult().modifyPercent(id, DAMAGE_INCREASE_PERCENT * effectLevel);

                fStats.getBallisticWeaponDamageMult().modifyMult(id, 1f - DAMAGE_CHANGE_PERCENT * effectLevel);
                fStats.getEnergyWeaponDamageMult().modifyMult(id, 1f - DAMAGE_CHANGE_PERCENT * effectLevel);
                fStats.getMissileWeaponDamageMult().modifyMult(id, 1f + 1.5f * DAMAGE_CHANGE_PERCENT * effectLevel);
                fStats.getMaxSpeed().modifyFlat(id, SPEED_BUFF * effectLevel);
                fStats.getAcceleration().modifyFlat(id, SPEED_BUFF * effectLevel);
/*
                if (fighter.getVelocity().length() > 300f){
                    MagicTrailPlugin.AddTrailMemberAdvanced(
                            fighter,
                            TRAIL_IDS.get(fighter),
                            Global.getSettings().getSprite("fx","FM_trail_2"),
                            fighter.getLocation(),
                            0,
                            0,
                            -VectorUtils.getFacing(fighter.getVelocity()),
                            0f,
                            0f,
                            60f,
                            180f,
                            new Color(65, 195, 255, 186),
                            new Color(161, 235, 253, 128),
                            1f,
                            0.2f,
                            0.3f,
                            1f,
                            5,
                            12,
                            256f,
                            10,
                            10f,
                            new Vector2f(),
                            null,
                            CombatEngineLayers.BELOW_SHIPS_LAYER,
                            60f
                    );
                }
 */

                if (effectLevel > 0) {
                    //fighter.setWeaponGlow(effectLevel, new Color(255,50,0,125), EnumSet.allOf(WeaponType.class));
                    fighter.setWeaponGlow(effectLevel, Misc.setAlpha(JITTER_UNDER_COLOR, 255), EnumSet.allOf(WeaponType.class));

                    fighter.setJitterUnder(KEY_JITTER, JITTER_COLOR, effectLevel, 5, 0f, jitterRangeBonus);
                    fighter.setJitter(KEY_JITTER, JITTER_UNDER_COLOR, effectLevel, 2, 0f, 0 + jitterRangeBonus);
                    Global.getSoundPlayer().playLoop("system_targeting_feed_loop", ship, 1f, 1f, fighter.getLocation(), fighter.getVelocity());
                }
            }
        }


    }

    private List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

//		this didn't catch fighters returning for refit
//		for (FighterLaunchBayAPI bay : carrier.getLaunchBaysCopy()) {
//			if (bay.getWing() == null) continue;
//			result.addAll(bay.getWing().getWingMembers());
//		}

        for (ShipAPI ship : FM_Misc.getFighters(carrier)) {
            if (!ship.isFighter()) continue;
            if (ship.getWing() == null) continue;
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }


    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        for (ShipAPI fighter : getFighters(ship)) {
            if (fighter.isHulk()) continue;
            MutableShipStatsAPI fStats = fighter.getMutableStats();
            fStats.getBallisticWeaponDamageMult().unmodify(id);
            fStats.getEnergyWeaponDamageMult().unmodify(id);
            fStats.getMissileWeaponDamageMult().unmodify(id);
            fStats.getMaxSpeed().unmodify(id);
            fStats.getMaxTurnRate().unmodify(id);
            fStats.getTurnAcceleration().unmodify(id);
            fStats.getAcceleration().unmodify(id);
            fStats.getDeceleration().unmodify(id);
        }
    }


    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            //return new StatusData("+" + (int)percent + "% fighter damage", false);
            return new StatusData(I18nUtil.getShipSystemString("FM_SurprisingStrikeInfo1") + (int) (DAMAGE_CHANGE_PERCENT * 150f) + "%", false);
        } else if (index == 1) {
            return new StatusData(I18nUtil.getShipSystemString("FM_SurprisingStrikeInfo2") + (int) (SPEED_BUFF) + "%", false);
        } else if (index == 2) {
            return new StatusData(I18nUtil.getShipSystemString("FM_SurprisingStrikeInfo3") + (int) (DAMAGE_CHANGE_PERCENT * 100f) + "%", true);
        }
        return null;
    }
}