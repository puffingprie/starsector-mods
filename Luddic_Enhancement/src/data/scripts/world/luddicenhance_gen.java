package data.scripts.world;


import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import data.scripts.world.systems.luddenhance_alexandretta;


@SuppressWarnings("unchecked")
public class luddicenhance_gen implements SectorGeneratorPlugin {

    @Override
    public void generate(SectorAPI sector) {

        new luddenhance_alexandretta().generate(sector);

    }
}