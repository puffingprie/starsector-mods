package data.scripts;

import com.fs.starfarer.api.Global;
import data.scripts.world.imperium.II_Corsica;
import data.scripts.world.imperium.II_Ex_Vis;
import data.scripts.world.imperium.II_Thracia;
import data.scripts.world.imperium.II_Yma;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class II_ModPluginAlt {

    static void initII() {
        if (IIModPlugin.isExerelin && !SectorManager.getManager().isCorvusMode()) {
            return;
        }

        new II_Thracia().generate(Global.getSector());
        new II_Corsica().generate(Global.getSector());
        new II_Ex_Vis().generate(Global.getSector());
        new II_Yma().generate(Global.getSector());
    }

    static void initShaderLib() {
        ShaderLib.init();
        LightData.readLightDataCSV("data/lights/ii_light_data.csv");
        TextureData.readTextureDataCSV("data/lights/ii_texture_data.csv");
    }

    static void adjustExerelin() {
        if (IIModPlugin.isExerelin) {
            SectorManager.NO_MILITARY_MARKET.add("ii_byzantium");
        }
    }
}
