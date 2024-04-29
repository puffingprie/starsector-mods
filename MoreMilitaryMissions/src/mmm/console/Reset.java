package mmm.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import mmm.missions.DefenseMission;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class Reset implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        Global.getSector().getMemoryWithoutUpdate().unset(DefenseMission.DIFFICULTY_KEY);
        Console.showMessage("Difficulty reset.");

        int count = 0;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            MemoryAPI memory = market.getMemoryWithoutUpdate();
            DefenseMission.MissionData data = (DefenseMission.MissionData) memory.get(DefenseMission.MISSION_DATA_KEY);
            if (data != null) {
                data.ClearAndDespawnFleets(Long.MIN_VALUE);
                memory.unset(DefenseMission.MISSION_DATA_KEY);
                ++count;
            }
        }
        Console.showMessage("De-spawned fleets from " + count + " markets.");

//        IntelManagerAPI manager = Global.getSector().getIntelManager();
//        int intel_count = 0;
//        for (IntelInfoPlugin plugin : new ArrayList<>(manager.getIntel(MyTradeFleetDepartureIntel.class))) {
//            if (plugin instanceof MyTradeFleetDepartureIntel) {
//                manager.removeIntel(plugin);
//                ++intel_count;
//            }
//        }
//        Console.showMessage("Removed " + intel_count + " trade fleet intel.");
//
//        Global.getSector().removeTransientScriptsOfClass(MyTradeFleetDepartureIntel.Injector.class);

        return CommandResult.SUCCESS;
    }
}
