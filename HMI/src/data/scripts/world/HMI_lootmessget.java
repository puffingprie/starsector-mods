package data.scripts.world;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class HMI_lootmessget extends BaseCampaignEventListener implements EveryFrameScript {

	public static final float MESS_PER_HULL_POINT = 0.002f;
	public static final float CORE1_PER_HULL_POINT = 0.00001f;
	public static final float CORE2_PER_HULL_POINT = 0.000005f;
	public static final float CORE3_PER_HULL_POINT = 0.000001f;

	public HMI_lootmessget() {
		super(true);
	}

	// Drops mess mass, courtesy of Histidine
	@Override
	public void reportEncounterLootGenerated(FleetEncounterContextPlugin plugin, CargoAPI loot) {
		CampaignFleetAPI loser = plugin.getLoser();
		if (loser == null) return;

		int totalHull = 0;
		int messHull = 0;
		List<FleetMemberAPI> deadShips = new ArrayList<>();
		List<FleetEncounterContextPlugin.FleetMemberData> casualties = plugin.getLoserData().getOwnCasualties();
		for (FleetMemberData memberData : casualties) {
			FleetEncounterContextPlugin.Status status = memberData.getStatus();
			if (status == FleetEncounterContextPlugin.Status.DESTROYED || status == FleetEncounterContextPlugin.Status.NORMAL) continue;
			FleetMemberAPI member = memberData.getMember();
			deadShips.add(member);
			int fp = member.getFleetPointCost();
			float hull = member.getHullSpec().getHitpoints() + member.getHullSpec().getArmorRating() * 4;
			if (member.isFighterWing())
				hull *= member.getNumFightersInWing();

			totalHull += hull;
			if (member.getHullId().startsWith("mess_"))
			{
				messHull += hull;
			}
		}

		float contrib = plugin.computePlayerContribFraction();
		messHull *= contrib;

		if (messHull != 0)
		{
			int nummess = (int)(messHull * MESS_PER_HULL_POINT * MathUtils.getRandomNumberInRange(0.75f, 1.25f));
			loot.addCommodity("mess_nano", nummess);
			loot.addCommodity(Commodities.METALS, -nummess);

			int numcore1 = (int)(messHull * CORE1_PER_HULL_POINT * MathUtils.getRandomNumberInRange(0.75f, 1.25f));
			loot.addCommodity("gamma_core", numcore1);
			loot.addCommodity(Commodities.METALS, -numcore1);

			int numcore2 = (int)(messHull * CORE2_PER_HULL_POINT * MathUtils.getRandomNumberInRange(0.75f, 1.25f));
			loot.addCommodity("beta_core", numcore2);
			loot.addCommodity(Commodities.METALS, -numcore2);

			int numcore3 = (int)(messHull * CORE3_PER_HULL_POINT * MathUtils.getRandomNumberInRange(0.75f, 1.25f));
			loot.addCommodity("alpha_core", numcore3);
			loot.addCommodity(Commodities.METALS, -numcore3);
		}
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	public boolean runWhilePaused() {
		return false;
	}

	@Override
	public void advance(float amount) {

	}

}