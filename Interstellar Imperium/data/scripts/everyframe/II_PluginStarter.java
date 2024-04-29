package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.campaign.terrain.II_ChargedNebulaTerrainPlugin;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class II_PluginStarter extends BaseEveryFrameCombatPlugin {

    @Override
    public void init(CombatEngineAPI engine) {
        if (engine.isInCampaign() && (engine.getContext().getPlayerFleet() != null)) {
            CampaignFleetAPI playerFleet = engine.getContext().getPlayerFleet();
            for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
                if (terrain.getPlugin() instanceof II_ChargedNebulaTerrainPlugin) {
                    if (terrain.getPlugin().containsEntity(playerFleet)) {
                        engine.addPlugin(new ChargedNebulaPlugin());
                        break;
                    }
                }
            }
        }
    }

    public final static class ChargedNebulaPlugin extends BaseEveryFrameCombatPlugin {

        private static final Color STRIKE_CORE = new Color(255, 10, 10, 255);
        private static final Color STRIKE_FRINGE = new Color(255, 50, 75, 255);
        private static final Vector2f ZERO = new Vector2f();

        private final IntervalUtil interval = new IntervalUtil(0.05f, 0.1f);

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (Global.getCombatEngine() == null) {
                return;
            }
            if (Global.getCombatEngine().isPaused()) {
                return;
            }

            interval.advance(amount);
            if (interval.intervalElapsed()) {
                ShipAPI aShip = null;
                for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                    aShip = ship;
                    break;
                }

                float size = MathUtils.getRandomNumberInRange(400f, 1600f);
                Vector2f startLoc = null;
                for (int i = 0; i < 10; i++) {
                    startLoc = new Vector2f(
                            (float) (Math.random() - 0.5) * (Global.getCombatEngine().getMapWidth() - (2f * size)),
                            (float) (Math.random() - 0.5) * (Global.getCombatEngine().getMapHeight() - (2f * size)));
                    if (Global.getCombatEngine().getNebula().locationHasNebula(startLoc.getX(), startLoc.getY())) {
                        break;
                    } else {
                        startLoc = null;
                    }
                }
                if (startLoc == null) {
                    return;
                }

                Vector2f endLoc = null;
                for (int i = 0; i < 10; i++) {
                    endLoc = MathUtils.getRandomPointOnCircumference(startLoc, size);
                    if (Global.getCombatEngine().getNebula().locationHasNebula(endLoc.getX(), endLoc.getY())) {
                        break;
                    } else {
                        endLoc = null;
                    }
                }
                if (endLoc == null) {
                    return;
                }

                if (Global.getCombatEngine().getViewport().isNearViewport(startLoc, size)
                        || Global.getCombatEngine().getViewport().isNearViewport(endLoc, size)) {
                    Vector2f midLoc = MathUtils.getMidpoint(startLoc, endLoc);

                    CombatEntityAPI targetEntity = new SimpleEntity(endLoc);
                    Global.getCombatEngine().spawnEmpArc(aShip, startLoc, targetEntity, targetEntity, DamageType.ENERGY, 0f, 0f,
                            size + 100f, null, 2f * (float) Math.sqrt(size), STRIKE_FRINGE, STRIKE_CORE);
                    Global.getSoundPlayer().playSound("terrain_hyperspace_lightning", MathUtils.getRandomNumberInRange(0.95f, 1.05f), 1f, midLoc, ZERO);

                    StandardLight light = new StandardLight(startLoc, endLoc, ZERO, ZERO, null);
                    light.setIntensity(MathUtils.getRandomNumberInRange(0.4f, 0.6f));
                    light.setSize(25f * (float) Math.sqrt(size));
                    light.setColor(STRIKE_FRINGE);
                    light.fadeOut(MathUtils.getRandomNumberInRange(0.6f, 1.0f));
                    LightShader.addLight(light);

                    StandardLight specLight = new StandardLight(midLoc, ZERO, ZERO, null);
                    specLight.setIntensity(MathUtils.getRandomNumberInRange(0.3f, 0.6f));
                    specLight.setSpecularMult(3f);
                    specLight.setSize(50f * (float) Math.sqrt(size));
                    specLight.setColor(STRIKE_CORE);
                    specLight.fadeOut(MathUtils.getRandomNumberInRange(0.2f, 0.5f));
                    LightShader.addLight(specLight);
                }
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
        }
    };
}
