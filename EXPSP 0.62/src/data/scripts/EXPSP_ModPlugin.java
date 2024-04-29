package data.scripts;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.world.EXPSP_WorldGen;


public class EXPSP_ModPlugin  extends BaseModPlugin
{
    @Override

    public void onNewGame() {
        new EXPSP_WorldGen().generate(Global.getSector());
    }
}
