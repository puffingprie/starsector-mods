package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import exerelin.campaign.SectorManager;

import data.scripts.world.pnGen;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class pnModPlugin extends BaseModPlugin {

 
    @Override
    public void onNewGame()
    {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode())
            new pnGen().generate(Global.getSector());
    }
    
    @Override
    public void onApplicationLoad() throws ClassNotFoundException {  
        
        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib){
            throw new RuntimeException("P9 requires LazyLib!"
                    + "\n"
                    + "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + "\n");
        }
        
        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib){
            throw new RuntimeException("P9 requires MagicLib!"
                    + "\n"
                    + "\nUpdate it at http://fractalsoftworks.com/forum/index.php?topic=13718"
                    + "\n");
        }
        
        boolean hasShaderLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (hasShaderLib){            
            ShaderLib.init();  
            LightData.readLightDataCSV("data/lights/pn_light.csv"); 
            TextureData.readTextureDataCSV("data/lights/pn_texture.csv"); 
        }        
    }                                                                                                                                                                                                                                                                                                                                            
    
    private static void initpn() {
        new pnGen().generate(Global.getSector());
    }
}