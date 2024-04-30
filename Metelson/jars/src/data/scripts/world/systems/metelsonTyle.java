package data.scripts.world.systems;

import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;

public class metelsonTyle
{
  public metelsonTyle() {}
  
  public void generate(SectorAPI sector)
  {
    StarSystemAPI system = sector.getStarSystem("Tyle");
    SectorEntityToken MIoutpost2 = system.addCustomEntity("metelsonTyle", "Voutyro Outpost", "station_midline2", "metelson");
    MIoutpost2.setCircularOrbit(system.getEntityById("tyle"),90 + 60, 4800, 260);
    MIoutpost2.setCustomDescriptionId("MIoutpost2");
  }
}

