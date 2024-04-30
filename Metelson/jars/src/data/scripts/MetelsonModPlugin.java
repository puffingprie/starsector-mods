package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.BaseModPlugin;
import exerelin.campaign.SectorManager;
import data.scripts.world.MeteGen;

public class MetelsonModPlugin extends BaseModPlugin {

    @Override
    public void onNewGame() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()) {
            new MeteGen().generate(Global.getSector());
        }
    }
}