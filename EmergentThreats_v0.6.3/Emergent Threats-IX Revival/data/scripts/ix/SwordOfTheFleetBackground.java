package data.scripts.ix;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import exerelin.campaign.backgrounds.BaseCharacterBackground;
import exerelin.utilities.NexFactionConfig;

public class SwordOfTheFleetBackground extends BaseCharacterBackground {
	
	private static String IX_FACTION_ID = "ix_battlegroup";
	private static String IX_MARZANNA_ID = "ix_marzanna";
	private static String IX_SKILL_ID = "ix_sword_of_the_fleet";
	private static String IX_SKILL_NAME = "Sword of the Fleet";

	@Override
	public boolean shouldShowInSelection(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
		return (factionConfig.factionId.equals("player"));
	}

	@Override
	public void onNewGameAfterTimePass(FactionSpecAPI factionSpec, NexFactionConfig factionConfig) {
		Global.getSector().getPlayerPerson().getStats().setSkillLevel(IX_SKILL_ID, 2f);
		Global.getSector().getFaction(IX_FACTION_ID).setRelationship("player", 0.60f);
		Global.getSector().getFaction(IX_MARZANNA_ID).setRelationship("player", 0.60f);
		CharacterDataAPI player = Global.getSector().getCharacterData();
		CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
		player.getHullMods().add("ix_ground_invasion_conversion");
		player.getHullMods().add("ix_laser_collimator");
		player.getHullMods().add("ix_reactive_combat_shields");
		player.getHullMods().add("ix_terminus_relay");
		if (Global.getSettings().getModManager().isModEnabled("EmergentThreats_Vice")) {
			player.getHullMods().add("vice_interdiction_array");
		}
		cargo.addCommodity("ix_panopticon_core", 1);
		cargo.addSpecial(new SpecialItemData("ix_biochip_sotf", ""), 1);
	}

	@Override
	public void addTooltipForSelection(TooltipMakerAPI tooltip, FactionSpecAPI factionSpec, NexFactionConfig factionConfig, Boolean expanded) {
		super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded);

		if (expanded) {
			String s = "Gain the %s skill, %s starting reputation with the IX Battlegroup, and a small number of hullmods and items to facilitate your mission";
            tooltip.addSpacer(10f);
            tooltip.addPara(s, 0f, Misc.getTextColor(), Misc.getHighlightColor(), IX_SKILL_NAME, "60");
		}
	}
}