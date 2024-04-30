package data.shipsystems.ai;

import com.fs.starfarer.api.combat.*;
import data.shipsystems.FM_swordout;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class FM_swordout_ai implements ShipSystemAIScript {

    private ShipAPI ship;

    private ShipwideAIFlags flags;

    private final float active = 0f;

    private List<WeaponAPI> weapons;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.weapons = ship.getAllWeapons();
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {

        if (ship == null) return;

        if (target == null) return;
        if (ship.getSystem().isCoolingDown()) return;
        if (target.isFighter() || target.isDrone()) return;
        if (flags.hasFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN_COOLDOWN) ||
                flags.hasFlag(ShipwideAIFlags.AIFlags.BACK_OFF) ||
                flags.hasFlag(ShipwideAIFlags.AIFlags.BACKING_OFF) ||
                flags.hasFlag(ShipwideAIFlags.AIFlags.DO_NOT_PURSUE)
        ) return;
        if (ship.getFluxTracker().getFluxLevel() >= 0.75f) return;


        float distance = MathUtils.getDistance(ship, target);

        if (distance < (averageAntiShipWeaponsRange(weapons) + 50f)) {
            if (!ship.getSystem().isActive()) {
                ship.useSystem();
            }
        }

        if (ship.getSystem().isActive()) {
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.PURSUING);
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.MAINTAINING_STRIKE_RANGE);
        }
    }

    public float averageAntiShipWeaponsRange(List<WeaponAPI> weapons) {
        float sum = 0f;

        int weight = 0;

        for (WeaponAPI weapon : weapons) {
            if (weapon.getSpec().hasTag("PD")) continue;

            if (weapon.getSize().equals(WeaponAPI.WeaponSize.SMALL)) {
                sum = sum + weapon.getRange();
                weight = weight + 1;
            }
            if (weapon.getSize().equals(WeaponAPI.WeaponSize.MEDIUM)) {
                sum = sum + 3 * weapon.getRange();
                weight = weight + 3;
            }
            if (weapon.getSize().equals(WeaponAPI.WeaponSize.LARGE)) {
                sum = sum + 6 * weapon.getRange();
                weight = weight + 6;
            }
        }

        return sum / weight;
    }

}
