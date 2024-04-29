package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import data.scripts.world.JYD_gen;
import exerelin.campaign.SectorManager;


public class JYDmodPlugin extends BaseModPlugin {

    @Override
    public void onNewGame() {
	boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
	if (!haveNexerelin || SectorManager.getManager().getCorvusMode()){
            new JYD_gen().generate(Global.getSector());
        }
    }
 
//     public void onNewGame() {
 //       new JYD_gen().generate(Global.getSector());
 //   }
 
}
	

