package data.campaign.intel.daily;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import data.campaign.intel.bar.events.FM_ReimuBarEvent;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FM_ReimuDailyTestIntelCreator implements EveryFrameScript {

    protected List<EveryFrameScript> active = new ArrayList<EveryFrameScript>();
    //最大事件数
    protected int Max = 1;
    protected Random randomBase = new Random();

    private float t = 0;

    public FM_ReimuDailyTestIntelCreator() {

    }

    public EveryFrameScript createEvent() {

        return new FM_ReimuDailyTestIntel();
    }

    public void advance(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);

//		if (this instanceof GenericMissionManager) {
//			CountingMap<String> counts = new CountingMap<String>();
//			for (EveryFrameScript script : active) {
//				counts.add(script.getClass().getSimpleName());
//			}
//			System.out.println("-------------------------------");
//			System.out.println("MAX: " + getCurrMax());
//			for (String key : counts.keySet()) {
//				System.out.println("" + counts.getCount(key) + " <- " + key);
//			}
//			System.out.println("-------------------------------");
//		}

        List<EveryFrameScript> remove = new ArrayList<EveryFrameScript>();
        for (EveryFrameScript event : active) {
            event.advance(amount);
            if (event.isDone()) {
                remove.add(event);
            }
        }
        active.removeAll(remove);
        //days *= 1000f;

//		if (this instanceof FactionHostilityManager) {
//			System.out.println("wefwefe");
//		}
//		if (this instanceof PirateBaseManager) {
//			System.out.println("wefwefwef");
//		}

        int count = getActiveCount();
        if (Global.getSector().getMemoryWithoutUpdate().contains(FM_ReimuBarEvent.ReimuKey)) {

            t = t + days;
            if (t >= 5) {

                if (count < Max && MathUtils.getRandomNumberInRange(0f, 1f) < 0.25f) {
                    EveryFrameScript event = createEvent();
                    addActive(event);
                    Global.getSector().getMemoryWithoutUpdate().expire(FM_ReimuBarEvent.ReimuKey, 0);
                }

                t = 0;
            }
        }
    }

    public int getActiveCount() {
        int count = 0;
        for (EveryFrameScript s : active) {
            if (s instanceof BaseIntelPlugin) {
                BaseIntelPlugin intel = (BaseIntelPlugin) s;
                if (intel.isEnding()) continue;
            }
            count++;
        }
        return count;
    }

    public void addActive(EveryFrameScript event) {
        if (event != null) {
            active.add(event);
        }
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public List<EveryFrameScript> getActive() {
        return active;
    }


}
