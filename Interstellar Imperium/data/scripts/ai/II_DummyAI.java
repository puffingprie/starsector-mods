package data.scripts.ai;

import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipwideAIFlags;

public class II_DummyAI implements ShipAIPlugin {

    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAIConfig config = new ShipAIConfig();

    public II_DummyAI() {
    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void cancelCurrentManeuver() {
    }

    @Override
    public void forceCircumstanceEvaluation() {
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public void setDoNotFireDelay(float amount) {
    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipAIConfig getConfig() {
        return config;
    }
}
