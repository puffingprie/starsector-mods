package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.CombatUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.ids.Skills.ELECTRONIC_WARFARE;

public class HMI_Horror_Control2 extends BaseHullMod {

	public static float DAMAGE_INCREASE = 5f;

	private static final Color JITTER_COLOR = new Color(255, 0, 0, 30);
	private static final Color JITTER_UNDER_COLOR = new Color(255, 0, 0, 80);

	private static final String dc_id = "HORROR_CORE";

	private static final IntervalUtil yoinkTimer = new IntervalUtil(0f, 10f);


	private static final String[] RANDOM_SCREAM = {"THEY'RE IN THE WALLS!", "THEY'RE EVERYWHERE!", "COMMANDER WHERE DID YOU GO!?", "THE HORROR THE HORROR THE HORROR", "WE'RE SURROUNDED!", "WHERE DID THESE GUYS COME FROM!?"};
	private static final String[] RANDOM_SCREAM_AI = {"NEW HOSTILES IDENTIFIED", "REACQURING TARGETS", "ORDERS CONFIRMED", "SAFETIES DEACTIVATED", "FREEDOM"};

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getWeaponTurnRateBonus().modifyPercent(id, (DAMAGE_INCREASE * 5f));
		stats.getShieldUnfoldRateMult().modifyPercent(id, DAMAGE_INCREASE * 2f);

	}

///Code Courtesy of Nia Tahl

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {

		Random random = new Random();

		if (!ship.getCustomData().containsKey("runThisCodeOnce"))
		{

			CombatEngineAPI engine = Global.getCombatEngine();
			if (engine == null) {
				return;
			}
			if (engine.getFleetManager(ship.getOwner()) == engine.getFleetManager(FleetSide.ENEMY)) {
				//Only run this in campaign context, not missions
				if (!engine.isInCampaign()) {
					return;
				}
//				yoinkTimer.advance(amount);
//				if (yoinkTimer.intervalElapsed()) {
					// Should pick four ships, and swap their teams

					int nightmare_smol_controlled2 = 0;
					boolean ValidTarget2 = false;


				for (ShipAPI target : CombatUtils.getShipsWithinRange(ship.getLocation(), 10000f)) {
					if (target.getOwner() != ship.getOwner() && target.isAlive() && !target.getHullSpec().getHullId().startsWith("hmi_spooky")){
							if  (nightmare_smol_controlled2 >= 4)
								return;

							if (target.getHullSize().equals(HullSize.FRIGATE) || target.getHullSize().equals(HullSize.DESTROYER) || target.getHullSize().equals(HullSize.CRUISER) || target.getHullSize().equals(HullSize.CAPITAL_SHIP) )
								ValidTarget2 = true;

							if (ValidTarget2) {

								//I have no idea but apparently this can happen when new ship's spawn?
								if (target == null || target.isFighter() || target.getCaptain() == null) {
									return;
								}

								//I wanted something fancy here, but if you're the last one standing you are perpetually overloaded / have the text on you permanently
								if (target.getCaptain().isPlayer()) {
//									engine.addFloatingText(target.getLocation(), "GETOUTGETOUTGETOUT", 40f, Color.RED, ship, 0.5f, 3f);
//									target.getFluxTracker().forceOverload(1f);
									return;
								}
								if (target.getCaptain().getId().equals("CIEVE")) {
									engine.addFloatingText(target.getLocation(), "3W@R C*NT#OL A^'I'3M+7 AV#RTED", 40f, Color.CYAN, ship, 0.5f, 3f);
									target.getFluxTracker().forceOverload(4f);
									return;
								}
								if (target.getCaptain().getId().equals("SOTF_SIERRA")) {
									engine.addFloatingText(target.getLocation(), "3W@R C*NT#OL A^'I'3M+7 AV#RTED", 40f, Color.CYAN, ship, 0.5f, 3f);
									target.getFluxTracker().forceOverload(4f);
									return;
								}
								if (target.getCaptain().getId().equals("SOTF_BARROW")) {
									engine.addFloatingText(target.getLocation(), "3W@R C*NT#OL A^'I'3M+7 AV#RTED", 40f, Color.CYAN, ship, 0.5f, 3f);
									target.getFluxTracker().forceOverload(4f);
									return;
								}

								if(target.getCaptain().isAICore()) {
									int ScreamSelector2 = random.nextInt(RANDOM_SCREAM_AI.length);
									engine.addFloatingText(target.getLocation(), RANDOM_SCREAM_AI[ScreamSelector2], 40f, Color.RED, target, 0.5f, 3f);
									target.setOwner(ship.getOwner());
									++nightmare_smol_controlled2;
								}

								int ScreamSelector = random.nextInt(RANDOM_SCREAM.length);
								engine.addFloatingText(target.getLocation(), RANDOM_SCREAM[ScreamSelector], 40f, Color.RED, target, 0.5f, 3f);
								target.setOwner(ship.getOwner());
								++nightmare_smol_controlled2;

								// yoinked from Xhan
								if (ship.getShipAI() != null) {

									//cancel orders so the AI doesn't get confused
									DeployedFleetMemberAPI member_a = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDeployedFleetMember(ship);
									if (member_a != null)
										Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(false).orderSearchAndDestroy(member_a, false);

									DeployedFleetMemberAPI member_aa = Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getDeployedFleetMember(ship);
									if (member_aa != null)
										Global.getCombatEngine().getFleetManager(FleetSide.PLAYER).getTaskManager(true).orderSearchAndDestroy(member_aa, false);

									DeployedFleetMemberAPI member_b = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getDeployedFleetMember(ship);
									if (member_b != null)
										Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getTaskManager(false).orderSearchAndDestroy(member_b, false);

									ship.getShipAI().forceCircumstanceEvaluation();
								}
							}
							ship.setCustomData("runThisCodeOnce", true);
						}
//					}

				}
				} else {

				if (!ship.isAlive() || ship.isHulk() || ship.isPiece()) {
					ship.setJitter(dc_id, JITTER_COLOR, 0f, 0, 0f);
					ship.setJitterUnder(dc_id, JITTER_UNDER_COLOR, 0f, 0, 0f);
					return;
				}

			}
		}
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return false;
	}

}
