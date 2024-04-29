package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;

public class II_AttractionMatrix extends BaseHullMod {

    private static final String DATA_KEY = "II_AttractionMatrix";
    private static final float ATTRACT_RANGE = 600f;
    private static final float ATTRACT_CHANCE = 0.2f;

    private static final Color ATTRACT_COLOR = new Color(100, 200, 255, 255);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if ((Global.getCombatEngine() == null) || (ship == null)) {
            return;
        }

        LocalData localData = (LocalData) Global.getCombatEngine().getCustomData().get(DATA_KEY);
        if (localData == null) {
            localData = new LocalData();
            Global.getCombatEngine().getCustomData().put(DATA_KEY, localData);
        }

        ShipData shipData = localData.shipData.get(ship);
        if (shipData == null) {
            shipData = new ShipData();
            localData.shipData.put(ship, shipData);
        }

        shipData.checkInterval.advance(amount);
        if (shipData.checkInterval.intervalElapsed()) {
            float rangeSquared = ATTRACT_RANGE * ATTRACT_RANGE;

            float shipSize = 20f;
            if (ship.getSpriteAPI() != null) {
                shipSize = Math.max(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()) * 2f;
            }
            for (MissileAPI missile : Global.getCombatEngine().getMissiles()) {
                if ((missile.getOwner() == ship.getOwner()) || missile.didDamage() || missile.isFading() || missile.isFlare() || missile.isMine() || !missile.isGuided()) {
                    continue;
                }

                float distanceSquared = MathUtils.getDistanceSquared(ship.getLocation(), missile.getLocation());
                if (distanceSquared > rangeSquared) {
                    continue;
                }

                if (shipData.missileDB.contains(missile)) {
                    continue;
                }
                shipData.missileDB.add(missile);

                float attractChance = ATTRACT_CHANCE;
                if (missile.getSource() != null) {
                    attractChance *= 1f - missile.getSource().getMutableStats().getEccmChance().getModifiedValue();
                }
                if (Math.random() >= attractChance) {
                    continue;
                }

                if (!(missile.getMissileAI() instanceof GuidedMissileAI)) {
                    continue;
                }
                GuidedMissileAI ai = (GuidedMissileAI) missile.getMissileAI();
                ai.setTarget(ship);

                float missileSize = 10f;
                if (missile.getSpriteAPI() != null) {
                    missileSize = Math.max(missile.getSpriteAPI().getWidth(), missile.getSpriteAPI().getHeight()) * 3f;
                }

                float missilePower = missile.getDamageAmount();
                if (missile.getDamageType() == DamageType.FRAGMENTATION) {
                    missilePower *= 0.33f;
                }
                missilePower += missile.getEmpAmount() * 0.25f;

                Global.getCombatEngine().addSmoothParticle(missile.getLocation(), missile.getVelocity(), missileSize, 1f, 0.25f, ATTRACT_COLOR);
                Global.getCombatEngine().addSmoothParticle(ship.getLocation(), ship.getVelocity(), shipSize, II_Util.lerp(0.1f, 1f, Math.min(2000f, missilePower) / 2000f), 0.25f, ATTRACT_COLOR);
            }
        }

        shipData.cleanupInterval.advance(amount);
        if (shipData.cleanupInterval.intervalElapsed()) {
            Iterator<MissileAPI> iter = shipData.missileDB.iterator();
            while (iter.hasNext()) {
                MissileAPI missile = iter.next();
                if (missile.didDamage() || missile.isFading() || !Global.getCombatEngine().isEntityInPlay(missile)) {
                    iter.remove();
                }
            }
        }
    }

    private static final class ShipData {

        final Set<MissileAPI> missileDB = new LinkedHashSet<>(100);
        final IntervalUtil checkInterval = new IntervalUtil(0.08f, 0.12f);
        final IntervalUtil cleanupInterval = new IntervalUtil(0.8f, 1.2f);
    }

    private static final class LocalData {

        final Map<ShipAPI, ShipData> shipData = new LinkedHashMap<>(50);
    }
}
