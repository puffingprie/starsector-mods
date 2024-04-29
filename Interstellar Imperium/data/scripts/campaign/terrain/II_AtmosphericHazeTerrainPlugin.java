package data.scripts.campaign.terrain;

import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import java.awt.Color;
import org.lwjgl.opengl.GL11;

public class II_AtmosphericHazeTerrainPlugin extends NebulaTerrainPlugin {

    @Override
    public String getTerrainName() {
        return "Atmospheric Haze";
    }

    @Override
    public void preMapRender(float alphaMult) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float mult = alphaMult * 0.5f;
        Color color = getRenderColor();
        GL11.glColor4ub((byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) (color.getAlpha() * mult));
    }

    @Override
    public void preRender(CampaignEngineLayers layer, float alphaMult) {
        GL11.glEnable(GL11.GL_BLEND);
        if (entity.isInHyperspace()) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        float mult = alphaMult * 0.35f;

        Color color = getRenderColor();
        GL11.glColor4ub((byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) (color.getAlpha() * mult));
    }
}
