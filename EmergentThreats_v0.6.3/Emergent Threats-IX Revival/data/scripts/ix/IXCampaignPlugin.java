package data.scripts.ix;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority;

import data.scripts.ix.PanopticonCorePlugin;

public class IXCampaignPlugin extends BaseCampaignPlugin {

	private static String PANOPTICON_CORE_ID = "ix_panopticon_core";
	private static String COMMAND_CORE_ID = "ix_command_core";

	public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
		if (PANOPTICON_CORE_ID.equals(commodityId) || COMMAND_CORE_ID.equals(commodityId)) {
			return new PluginPick<AICoreOfficerPlugin>(new PanopticonCorePlugin(), PickPriority.MOD_SET);
		}
		else return null;
	}
}