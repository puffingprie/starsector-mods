package data.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class FM_ContactWithTokikoCreator extends BaseBarEventCreator {

    public PortsideBarEvent createBarEvent() {
        return new FM_ContactWithTokiko();
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 10000000000f; // one-time-only
    }

    @Override
    public boolean isPriority() {
        return true;
    }

    @Override
    public float getBarEventFrequencyWeight() {
        return 10000000000f; // will reset when intel ends... or not, if keeping this one-time-only
    }
}
