package data.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class FM_ReimuBarEventCreator extends BaseBarEventCreator {

    public PortsideBarEvent createBarEvent() {
        return new FM_ReimuBarEvent();
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
        return 10f;
    }

}
