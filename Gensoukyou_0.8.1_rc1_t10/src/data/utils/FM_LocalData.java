package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.shipsystems.FM_endlessgallery;

import java.util.HashMap;
import java.util.Map;

public class FM_LocalData extends BaseEveryFrameCombatPlugin {

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put("FM_LocalData", new FM_Data());
    }

    public static FM_Data getCurrData() {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return null;
        }

        FM_Data data;

        if (engine.getCustomData().containsKey("FM_LocalData")) {
            data = (FM_Data) engine.getCustomData().get("FM_LocalData");
        } else {
            data = new FM_Data();
            engine.getCustomData().put("FM_LocalData", data);
        }
        return data;
    }


    public static final class FM_Data {

        //FantasyBorderMod
        public final Map<ShipAPI, Boolean> withFM_BorderMod = new HashMap<>();
        //public final Map<ShipAPI, Integer> numberOfBombs = new HashMap<>();

        //通用弹头参数相关
        public final Map<DamagingProjectileAPI, FM_ProjectEffect.UniversalProjectEffect> universalEffect = new HashMap<>();

        //FM_endlessgallery
        public final Map<ShipAPI, FM_endlessgallery.Wave> elgeffect = new HashMap<>();


    }


}
