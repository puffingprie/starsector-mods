package mmm;

import com.fs.starfarer.api.*;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.XStream;
import lunalib.lunaSettings.LunaSettings;
import mmm.campaign.ForceAutoResolveInjector;
import mmm.campaign.MyTradeFleetDepartureIntel;
import mmm.missions.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoreMilitaryMissionsModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(MoreMilitaryMissionsModPlugin.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    public static boolean FORCE_AUTO_RESOLVE_ENABLED;
    public static boolean ESCORT_MISSION_ADD_ALL_INTEL;

    // Injects an error message so that player must see it.
    public static class MessageInjector implements EveryFrameScript {
        String msg;
        boolean isDone = false;
        MessageInjector(String msg) { this.msg = msg; }
        @Override
        public boolean isDone() { return isDone; }
        @Override
        public boolean runWhilePaused() { return true; }
        @Override
        public void advance(float amount) {
            try {
                CampaignUIAPI ui = Global.getSector().getCampaignUI();
                ui.getMessageDisplay().addMessage(msg, Color.RED);
                isDone = ui.showConfirmDialog(msg, "Ok", null, null, null);
            } catch (Exception ignored) {}
        }
    }

    public static void addRemoveScripts() {
        Class<ForceAutoResolveInjector> far_clazz = ForceAutoResolveInjector.class;
        if (FORCE_AUTO_RESOLVE_ENABLED) {
            if (!Global.getSector().hasTransientScript(far_clazz)) {
                Global.getSector().addTransientScript(new ForceAutoResolveInjector());
            }
        } else {
            Global.getSector().removeTransientScriptsOfClass(far_clazz);
        }

        Class<MyTradeFleetDepartureIntel.Injector> tdi_clazz = MyTradeFleetDepartureIntel.Injector.class;
        if (ESCORT_MISSION_ADD_ALL_INTEL) {
            if (!Global.getSector().hasTransientScript(tdi_clazz)) {
                Global.getSector().addTransientScript(new MyTradeFleetDepartureIntel.Injector());
            }
        } else {
            Global.getSector().removeTransientScriptsOfClass(tdi_clazz);
        }
    }

    @Override
    public void onApplicationLoad() {
        Utils.loadSettings();
        if (Utils.LUNA_LIB_ENABLED) {
            LunaSettings.addSettingsListener(new Utils.MmmLunaSettingsListener());
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        // Notify the user to disable conflicting mods.
        final List<String> modIds = Arrays.asList("ForceAutoResolve", "PickYourShips", "ScavengerTrading");
        ArrayList<String> modsToDisable = new ArrayList<>();
        ModManagerAPI manager = Global.getSettings().getModManager();
        for (String modId : modIds) {
            if (manager.isModEnabled(modId)) modsToDisable.add(modId);
        }
        if (!modsToDisable.isEmpty()) {
            String msg = MessageFormat.format(
                    "Please resolve mod conflicts by disabling the following mods as the functionality has " +
                    "been built into MoreMilitaryMissions: {0}", Misc.getAndJoined(modsToDisable));
            log.error(msg);
            Global.getSector().addTransientScript(new MessageInjector(msg));
        }

        // Add scripts
        Global.getSector().addTransientListener(new MissionInjector());
        Global.getSector().addTransientScript(new VipMission.TriggerInjector());
        addRemoveScripts();

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            // Fix for saves created before 0.1.1; ensure enemy fleets are stored using the neutral faction
            // TODO: remove when making backward incompatible changes
            MemoryAPI memory = market.getMemoryWithoutUpdate();
            DefenseMission.MissionData data = (DefenseMission.MissionData) memory.get(DefenseMission.MISSION_DATA_KEY);
            if (data != null) {
                for (CampaignFleetAPI fleet : data.fleets) {
                    if (!fleet.getFaction().getId().equals(Factions.NEUTRAL)) {
                        Vector2f loc = fleet.getLocation();
                        if (loc.getX() < -25999f && loc.getY() < -25999f) {
                            fleet.setFaction(Factions.NEUTRAL, true);
//                            log.debug("Fixing faction for fleet targeting market " + market.getName());
                        }
                    }
                }
            }
            // End fix
        }
    }

    @Override
    public void configureXStream(XStream x) {
        x.alias("mmm.missions.MyTradeFleetDepartureIntel", MyTradeFleetDepartureIntel.class);
    }
}
