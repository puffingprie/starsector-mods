package data.campaign.intel.events;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.util.Misc;
import data.utils.FM_Person;
import data.utils.I18nUtil;

import java.util.List;

public class FM_KutakaEventCreator implements EveryFrameScript {

    private float t = 0;
    public void addKutaka(MarketAPI market){
        PersonAPI person = Global.getSector().getFaction("fantasy_manufacturing").createRandomPerson();
        person.addTag("FM_KutakaTag");
        person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Kutaka"));
        person.setName(new FullName("Niwatari", "Kutaka", FullName.Gender.FEMALE));
        person.setId(I18nUtil.getString("person","FM_KutakaId"));
        person.setPostId("FM_executiveAssistant");
        person.setRankId(Ranks.CITIZEN);
        FM_Person.addCharacter(person,market,true);
        market.addPerson(person);
        market.getCommDirectory().addPerson(person);
        //person.setImportance(PersonImportance.VERY_HIGH);
        Misc.makeImportant(person,I18nUtil.getString("person","FM_KutakaId"));

        Global.getLogger(this.getClass()).info("KutakaEventCreator addKutaka");

        //设置首次见面的剧情对话相关
        Global.getSector().addScript(new FM_KutakaEventDialog());
    }

    public boolean shouldShowAtMarket(MarketAPI market){
        if (DebugFlags.COLONY_DEBUG)return true;
        if (!market.getPrimaryEntity().getId().equals("FM_planet_hakurei")) return false;
        if (FM_Person.hasMetCharacter(I18nUtil.getString("person","FM_TokikoId")))return false;
        if (FM_Person.hasMetCharacter(I18nUtil.getString("person","FM_KutakaId")))return false;
        if (Math.random() <= 1f)return true;
        return Global.getSector().getPlayerStats().getLevel() >= 0 || DebugFlags.COLONY_DEBUG;
    }

    @Override
    public boolean isDone() {
        return FM_Person.isStored(I18nUtil.getString("person","FM_KutakaId"));
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
//        Global.getLogger(this.getClass()).info("KutakaEventCreator advance");
        if (FM_Person.isStored(I18nUtil.getString("person","FM_KutakaId")))return;
        float days = Global.getSector().getClock().convertToDays(amount);
        t = t + days;
        if (t >= 10f){
            List<MarketAPI> markets = Misc.getFactionMarkets("fantasy_manufacturing");
            for (MarketAPI market : markets){
                if (!shouldShowAtMarket(market))continue;
                addKutaka(market);
            }
            t = 0;
        }
    }
}
