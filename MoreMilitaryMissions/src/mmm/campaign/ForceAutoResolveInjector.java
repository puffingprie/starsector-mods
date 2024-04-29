package mmm.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DevMenuOptions;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.OptionId;
import com.fs.starfarer.api.util.Misc;
import mmm.Utils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static mmm.missions.OrbitalMissionBase.FACTION_STRENGTH_RATIO;

public class ForceAutoResolveInjector implements EveryFrameScript {
    private static final Logger log = Global.getLogger(ForceAutoResolveInjector.class);
    static {
        if (Utils.DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    // Settings
    public static float MIN_STRENGTH_RATIO;

    // Reimplementation of FleetInteractionDialogPluginImpl.isValidTransferCommandTarget
    public static boolean isValidTransferCommandTarget(FleetMemberAPI member) {
        if (member.isFighterWing() || member.isAlly()) return false;
        if (Misc.isAutomated(member)) return false;
        if (Misc.isUnremovable(member.getCaptain())) return false;
        return true;
    }

    public static float getEffectiveStrength(CampaignFleetAPI fleet) {
        if (fleet == null) return 0f;  // Sanity check

        float ratio = 1f;
        if (fleet.getFaction() != null) {
            Float strength_ratio = FACTION_STRENGTH_RATIO.get(fleet.getFaction().getId());
            if (strength_ratio != null && strength_ratio > 0f) {
                ratio = strength_ratio;
            }
        }

        // getEffectiveStrength doesn't work for combined fleets (BattleAPI.getPlayerCombined() for example),
        // so we sum up their member strengths instead.
        float member_strengths = 0f;
        for (FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            member_strengths += Misc.getMemberStrength(member);
        }
        return Math.max(fleet.getEffectiveStrength(), member_strengths) * ratio;
    }

    // Uses reflection to call FleetInteractionDialogPluginImpl.getString for a set of ids, returning a List with the
    // same size as input, containing possibly null values in case of error.
    public static List<String> getStrings(FleetInteractionDialogPluginImpl plugin, List<String> ids) {
        ArrayList<String> result = new ArrayList<>();
        for (String id : ids) {
            String str = (String) Utils.reflectionInvoke(FleetInteractionDialogPluginImpl.class, plugin,
                    "getString", String.class, id);
            if (str != null) {
                str = str.replaceAll("pursuit", "battle");
            }
            result.add(str);
        }
        return result;
    }

    @Override
    public boolean isDone() { return false; }

    @Override
    public boolean runWhilePaused() { return true; }

    private SectorEntityToken previousTarget = null;
    @Override
    public void advance(float amount) {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (dialog == null || !(dialog.getPlugin() instanceof FleetInteractionDialogPluginImpl)) return;

        FleetInteractionDialogPluginImpl plugin = (FleetInteractionDialogPluginImpl) dialog.getPlugin();
        if (!(plugin.getContext() instanceof FleetEncounterContext)) return;

        OptionPanelAPI options = dialog.getOptionPanel();
        SectorEntityToken target = dialog.getInteractionTarget();
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        BattleAPI battle = ((FleetEncounterContext) plugin.getContext()).getBattle();

        if (options == null || playerFleet == null || battle == null) return;  // Sanity check

        // If we already have an auto pursuit option; nothing to do.
        if (options.hasOption(OptionId.AUTORESOLVE_PURSUE)) return;

        // Ensure we're in the right screen; see FleetInteractionDialogPluginImpl.updatePreCombat for reference:
        // won't work if some mod adds an extra option.
        boolean has_sel_flagship = options.hasOption(OptionId.SELECT_FLAGSHIP);
        boolean has_continue = options.hasOption(OptionId.CONTINUE_INTO_BATTLE);
        boolean has_go_back = options.hasOption(OptionId.GO_TO_MAIN);

        int optionsCount = options.getSavedOptionList().size();
        int expectedCount = Global.getSettings().isDevMode() ? 4 : 2;
        if (has_go_back) ++expectedCount;

        if (optionsCount != expectedCount || !has_sel_flagship || !has_continue) return;

        // Ignore allies when considering whether your fleet is big enough for auto resolve. If we're not above auto
        // resolve threshold, then do nothing.
        float player_strength = getEffectiveStrength(playerFleet);
        float allies_strength = getEffectiveStrength(battle.getPlayerCombined());
        float enemy_strength = getEffectiveStrength(battle.getNonPlayerCombined());
        float threshold = enemy_strength * MIN_STRENGTH_RATIO;
        boolean force = player_strength > threshold;

        if (force || previousTarget != target) {
            log.debug(MessageFormat.format(
                    "ForceAutoResolve: force={0}, threshold={1}, player_strength={2}, allies_strength={3}, enemy_strength={4}",
                    force, threshold, player_strength, allies_strength, enemy_strength));
        }
        previousTarget = target;
        if (!force) return;

        boolean canTransfer = false;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.isFlagship()) continue;
            if (!isValidTransferCommandTarget(member)) continue;
            canTransfer = true;
            break;
        }

        List<String> tooltipTexts = getStrings(plugin,
                Arrays.asList("tooltipPursueAutoresolve", "tooltipSelectFlagship"));
        if (tooltipTexts.size() != 2) return;  // Sanity check

        options.clearOptions();
        options.addOption("Order your second-in-command to handle it (force auto resolve)",
                OptionId.AUTORESOLVE_PURSUE, tooltipTexts.get(0));
        options.addOption("Transfer command for this engagement", OptionId.SELECT_FLAGSHIP, tooltipTexts.get(1));
        if (!canTransfer) {
            options.setEnabled(OptionId.SELECT_FLAGSHIP, false);
        }
        options.addOption("Take command of the action", OptionId.CONTINUE_INTO_BATTLE, null);

        if (has_go_back) {
            options.addOption("Go back", OptionId.GO_TO_MAIN, null);
            options.setShortcut(OptionId.GO_TO_MAIN, Keyboard.KEY_ESCAPE, false, false, false, true);
        }

        if (Global.getSettings().isDevMode()) {
            DevMenuOptions.addOptions(dialog);
        }
    }
}
