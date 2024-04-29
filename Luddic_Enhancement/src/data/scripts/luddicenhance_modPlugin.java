package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import exerelin.campaign.SectorManager;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.ai.LE_MightAI;
import data.scripts.util.LE_Util;
import data.scripts.world.luddicenhance_gen;

public class luddicenhance_modPlugin extends BaseModPlugin {

    public static boolean haveNexerelin = false;
    public static boolean isExerelin = false;
    public static final String MIGHT_ID = "lp_dram_missile";

    @Override
    public void onNewGame() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            initluddenhance();
        }
    }

    @Override
    public void onApplicationLoad() throws Exception {
        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
    }

    private static void initluddenhance() {
        new luddicenhance_gen().generate(Global.getSector());
    }


    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        switch (LE_Util.getNonDHullId(ship.getHullSpec())) {
            case MIGHT_ID:
                return new PluginPick<ShipAIPlugin>(new LE_MightAI(ship), CampaignPlugin.PickPriority.HIGHEST);
            default:
                break;
        }

        return null;
    }

}

