package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import java.util.ArrayList;
import java.util.Arrays;

public class metelsonCorvus
{
  public metelsonCorvus() {}
  
  public void generate(SectorAPI sector)
  {
    StarSystemAPI system = sector.getStarSystem("Corvus");
    SectorEntityToken MIdepo1 = system.addCustomEntity("metelsonCorvus", "Asharu Shipyard", "station_side05", "metelson");
    MIdepo1.setCircularOrbitPointingDown(system.getEntityById("asharu"), 115.0F, 600.0F, 100.0F);
    MIdepo1.setCustomDescriptionId("MIdepo1");
    
  }
}
