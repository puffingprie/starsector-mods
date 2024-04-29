//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.world.systems.anvilgen;
import exerelin.campaign.SectorManager;

public class AnvilModPlugin extends BaseModPlugin {
    public static boolean isExerelin = false;

    public AnvilModPlugin() {
    }

    @Override
    public void onNewGame() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()){
            new anvilgen().generate(Global.getSector());
        }
    }


    public void onApplicationLoad() {
    }
}
