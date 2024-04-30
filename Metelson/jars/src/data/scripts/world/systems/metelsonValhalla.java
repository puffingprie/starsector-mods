package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class metelsonValhalla
{
  public metelsonValhalla() {}
  
  public void generate(SectorAPI sector)
  {
    StarSystemAPI system = sector.getStarSystem("Valhalla");
    SectorEntityToken MIoutpost = system.addCustomEntity("metelsonValhalla", "MI Mining Outpost", "station_side04", "metelson");
    MIoutpost.setCircularOrbit(system.getEntityById("ragnar"), 285.0F, 1400.0F, 50.0F);
    MIoutpost.setCustomDescriptionId(null);
        

  }
}

