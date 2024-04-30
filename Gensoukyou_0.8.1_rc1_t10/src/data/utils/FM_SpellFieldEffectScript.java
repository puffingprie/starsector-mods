package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import data.hullmods.FantasyBasicMod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FM_SpellFieldEffectScript extends BaseEveryFrameCombatPlugin {


    private CombatEngineAPI engine;
    private int skipFrame = 0;
    private ShipAPI playerShip_last = null;
    private final Set<CombatFleetManagerAPI> managers = new HashSet<>();

    public static float BASE_MAXIMUM = 25f;
    public static String BONUS_ID = "FM_PsiEffectBonus";
    public static Object INFO = new Object();


    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) return;
        if (engine.isPaused()) return;

        if (playerShip_last == null) {
            playerShip_last = engine.getPlayerShip();
            skipFrame = 20;
        }

        if (skipFrame > 0) {
            skipFrame = skipFrame - 1;
            return;
        }

        int[] player_effect = getTotalAndMaximum(engine.getFleetManager(0));
        int[] enemy_effect = getTotalAndMaximum(engine.getFleetManager(1));

        if (player_effect == null || enemy_effect == null) {
            cleanUp(engine.getFleetManager(0));
            cleanUp(engine.getFleetManager(1));
            return;
        }

        float player_bonus_total = player_effect[0];
        float player_bonus_max = player_effect[1];

        float enemy_bonus_total = enemy_effect[0];
        float enemy_bonus_max = enemy_effect[1];

        CombatFleetManagerAPI player = engine.getFleetManager(0);
        CombatFleetManagerAPI enemy = engine.getFleetManager(1);

        float player_bonus = Math.min(player_bonus_max, player_bonus_total);
        float enemy_bonus = Math.min(enemy_bonus_max, enemy_bonus_total);

        if (player_bonus != 0f) {
            applyBonus(player, player_bonus);
            engine.maintainStatusForPlayerShip(INFO, Global.getSettings().getSpriteName("ui", "icon_tactical_electronic_warfare"),
                    I18nUtil.getString("misc", "FM_SpellFieldEffect_T"), I18nUtil.getString("misc", "FM_SpellFieldEffect_D") + (100 - player_bonus) + "%", false);
        }

        if (enemy_bonus != 0f) {
            applyBonus(enemy, enemy_bonus);
            //engine.maintainStatusForPlayerShip(INFO, Global.getSettings().getSpriteName("ui", "icon_tactical_electronic_warfare"),
            //       "灵能场协调","当前敌方舰队护盾维持与护盾所受伤害为" + (100 - enemy_bonus) + "%",false);
        }


    }

    private int[] getTotalAndMaximum(CombatFleetManagerAPI manager) {
        float maxB;
        float totalB = 0f;


        maxB = BASE_MAXIMUM;

        List<DeployedFleetMemberAPI> dp_ships = manager.getDeployedCopyDFM();
        for (DeployedFleetMemberAPI dp_ship : dp_ships) {
            if (dp_ship.isFighterWing()) continue;
            if (dp_ship.isStation()) continue;
            if (dp_ship.getShip().getVariant().hasHullMod(FantasyBasicMod.FANTASYBASICMOD)) {
                float curr_psi_B = 2f;
                totalB = totalB + curr_psi_B;

            }
        }

        return new int[]{(int) totalB, (int) maxB};
    }


    private void cleanUp(CombatFleetManagerAPI manager) {

        if (managers.contains(manager)) {
            managers.remove(manager);
            List<DeployedFleetMemberAPI> dp_ships = manager.getDeployedCopyDFM();
            for (DeployedFleetMemberAPI dp_ship : dp_ships) {
                if (dp_ship.isFighterWing()) continue;
                if (dp_ship.getShip() == null) continue;
                if (dp_ship.getShip().getVariant().hasHullMod("FantasyBasicMod")) {

                    dp_ship.getShip().getMutableStats().getShieldDamageTakenMult().unmodify(BONUS_ID);
                    dp_ship.getShip().getMutableStats().getShieldUpkeepMult().unmodify(BONUS_ID);

                }
            }
        }
    }

    private void applyBonus(CombatFleetManagerAPI manager, float bonus) {

        List<DeployedFleetMemberAPI> dp_ships = manager.getDeployedCopyDFM();
        for (DeployedFleetMemberAPI dp_ship : dp_ships) {

            if (dp_ship.getShip() == null) continue;
            dp_ship.getShip().getMutableStats().getShieldDamageTakenMult().modifyMult(BONUS_ID, 1 - bonus / 100f);
            dp_ship.getShip().getMutableStats().getShieldUpkeepMult().modifyMult(BONUS_ID, 1 - bonus / 100f);


        }
        managers.add(manager);
    }
}
