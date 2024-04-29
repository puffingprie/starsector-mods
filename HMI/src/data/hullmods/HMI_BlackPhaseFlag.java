package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.fs.starfarer.api.combat.ShipAPI.HullSize.*;

public class HMI_BlackPhaseFlag extends BaseHullMod {

	private static final float TIME_FLOW_BOOST = 1.5f;
    private static final float ARMOR_REPAIR_MULTIPLIER = 600.0f;
	private static final float HULL_REPAIR = 400.0f;
    private static final float CR_DEGRADE = 2.0f;

    private final IntervalUtil interval = new IntervalUtil(0.033f, 0.033f);

    protected Object HOLEKEY = new Object();
    private final Random rand = new Random();

    private Color color1 = new Color(255,175,255,255);
    private Color color2 = new Color(102, 58, 102,255);

    public void advanceInCombat(ShipAPI ship, float amount) {
        ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
        String id = "HMI_Blackhole";
        CombatEngineAPI engine = Global.getCombatEngine();

        ship.setVentFringeColor(color2);
        ship.setVentCoreColor(color1);

        float effect_level = (ship.getFluxTracker().getFluxLevel());
        float Instability = (MathUtils.getRandomNumberInRange(0f, 100f)) * 0.01f;

        int ShieldRed = Math.round(color1.getRed() * (1 - Instability) + color2.getRed() * Instability);
        int ShieldGreen = Math.round(color1.getGreen() * (1 - Instability) + color2.getGreen() * Instability);
        int ShieldBlue = Math.round(color1.getBlue() * (1 - Instability) + color2.getBlue() * Instability);
        int ShieldAlpha = Math.round(MathUtils.getRandomNumberInRange(0, MathUtils.getRandomNumberInRange(10, 40)) * effect_level);

        ship.addAfterimage(new Color(ShieldRed, ShieldGreen, ShieldBlue, ShieldAlpha), 0f, 0f, 90f * MathUtils.getRandomNumberInRange(-0.8f, 0.8f) * effect_level, 90f * MathUtils.getRandomNumberInRange(-0.8f, 0.8f) * effect_level,
                0.5f * effect_level,
                0f, 0.5f, 2f * effect_level, true, false, false);


        if (ship.isPhased()){
            ship.getMutableStats().getTimeMult().modifyMult(id, TIME_FLOW_BOOST);
            ArmorGridAPI armorGrid = ship.getArmorGrid();
            int x = rand.nextInt(armorGrid.getGrid().length);
            int y = rand.nextInt(armorGrid.getGrid()[0].length);
            float newArmor = armorGrid.getArmorValue(x, y);
            float cellSize = armorGrid.getCellSize();

            if (Float.compare(newArmor, armorGrid.getMaxArmorInCell()) >= 0) {
                return;
            }

            newArmor += ARMOR_REPAIR_MULTIPLIER * amount;
            armorGrid.setArmorValue(x, y, Math.min(armorGrid.getMaxArmorInCell(), newArmor));

            interval.advance(engine.getElapsedInLastFrame());

            if (interval.intervalElapsed()) {
                ship.setHitpoints(Math.min(ship.getHitpoints() + interval.getIntervalDuration() * (1f - effect_level) * HULL_REPAIR, ship.getMaxHitpoints()));
                ship.getMutableStats().getCRLossPerSecondPercent().modifyMult(id, CR_DEGRADE);
                ship.getMutableStats().getPeakCRDuration().modifyMult(id, -CR_DEGRADE);
            }

        }

        if(ship.isPhased() && ship==playerShip){
            Global.getCombatEngine().maintainStatusForPlayerShip(
                    "HOLEKEY",
                    "graphics/icons/hullsys/temporal_shell.png",
                    "ERROR",
                    "???",
                    false);
        }

        if (!ship.isPhased()){
            ship.getMutableStats().getTimeMult().unmodify(id);
            ship.getMutableStats().getCRLossPerSecondPercent().unmodify(id);
            ship.getMutableStats().getPeakCRDuration().unmodify(id);
        }
    }


    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "regenerate armour and hull";
        if (index == 1) return "" + (int) ((CR_DEGRADE - 1) * 100) + "%";
        if (index == 2) return "" + (int) ((CR_DEGRADE - 1) * 100) + "%";
        return null;
    }

}