package data.scripts.campaign.specialforces;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import exerelin.campaign.intel.specialforces.namer.SpecialForcesNamer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;

public class II_SpecialForcesNamer implements SpecialForcesNamer {

    private static final Map<String, String> GOD_MARKETS = new HashMap<>();
    private static final List<String> GOD_NAME_PREFIX = new ArrayList<>();

    static {
        GOD_MARKETS.put("ii_byzantium", "Minerva");
        GOD_MARKETS.put("ii_nova_capitalis", "Vulcan");
        GOD_MARKETS.put("ii_aleria", "Justitia");

        GOD_NAME_PREFIX.add("Arm");
        GOD_NAME_PREFIX.add("Crown");
        GOD_NAME_PREFIX.add("Disciples");
        GOD_NAME_PREFIX.add("Hammer");
        GOD_NAME_PREFIX.add("Hand");
        GOD_NAME_PREFIX.add("Faithful");
        GOD_NAME_PREFIX.add("Fist");
        GOD_NAME_PREFIX.add("Fury");
        GOD_NAME_PREFIX.add("Mercy");
        GOD_NAME_PREFIX.add("Mouth");
        GOD_NAME_PREFIX.add("Power");
        GOD_NAME_PREFIX.add("Rage");
        GOD_NAME_PREFIX.add("Shield");
        GOD_NAME_PREFIX.add("Sword");
        GOD_NAME_PREFIX.add("Temperance");
        GOD_NAME_PREFIX.add("Will");
        GOD_NAME_PREFIX.add("Wrath");
    }

    @Override
    public String getFleetName(CampaignFleetAPI fleet, MarketAPI origin, PersonAPI commander) {
        MarketAPI market = origin;

        String godName = GOD_MARKETS.get(market.getId());
        if (godName != null) {
            String prefix = GOD_NAME_PREFIX.get(MathUtils.getRandomNumberInRange(0, GOD_NAME_PREFIX.size() - 1));
            return prefix + " of " + godName;
        } else {
            int num = MathUtils.getRandomNumberInRange(1, 33);
            return Global.getSettings().getRoman(num) + " " + market.getName();
        }
    }
}
