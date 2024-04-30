package data.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.CallEvent;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Person;
import data.utils.I18nUtil;

import java.util.List;
import java.util.Map;

public class FM_KutakaEventDialog implements CallEvent.CallableEvent, EveryFrameScript {

    protected PersonAPI Kutaka;
    protected MarketAPI market;

    public FM_KutakaEventDialog(){
        //设置好各种需要的参数和状态
        Kutaka = FM_Person.getPerson(I18nUtil.getString("person","FM_KutakaId"));
        Kutaka.getMemoryWithoutUpdate().set("$FM_KutakaFirstTimeMeet",true);
        market = Kutaka.getMarket();
        Kutaka.getMemoryWithoutUpdate().set("$FM_Kutaka_eventRef", this);
        PersonAPI pather = Global.getSector().getFaction(Factions.LUDDIC_PATH).createRandomPerson();
        pather.setId("FM_KutakaTest");
        //Global.getSector().getMemoryWithoutUpdate().set("$FM_KutakaTest", pather);
        Global.getSector().getImportantPeople().addPerson(pather);
//        PersonAPI Tokiko = FM_Person.getPerson(I18nUtil.getString("person","FM_TokikoId"));
//        Global.getSector().getImportantPeople().addPerson(Tokiko);


    }

    @Override
    public boolean callEvent(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action = params.get(0).getString(memoryMap);

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI cargo = playerFleet.getCargo();
        //MemoryAPI memory = planet.getMemoryWithoutUpdate();

        if (action.equals("prepare")) {

        }else if (action.equals("done")) {
            Kutaka.getMemoryWithoutUpdate().set("$FM_KutakaFirstTimeMeet",false);
        }
        return true;
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
