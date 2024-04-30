package data.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.util.List;

public class FM_FireflyFlare extends BaseShipSystemScript {

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        super.apply(stats, id, state, effectLevel);
        if (!(stats.getEntity() instanceof ShipAPI))return;
        ShipAPI ship = (ShipAPI) stats.getEntity();
        List<WeaponAPI> weaponList = ship.getAllWeapons();
        for (WeaponAPI weapon : weaponList){
            if (!weapon.isDecorative())continue;
            if (weapon.getId().equals("FM_FireflyFlare")){
                weapon.setForceFireOneFrame(true);
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        super.unapply(stats, id);
    }
}
