/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import data.scripts.world.nosuchorg.NoSuchOrg_Gen;
import exerelin.campaign.SectorManager;
import org.apache.log4j.Logger;

public class NoSuchOrg_ModPlugin extends BaseModPlugin {

    public static boolean isExerelin = false;
    public static boolean templarsExists = false;
    public static Logger log = Global.getLogger(NoSuchOrg_ModPlugin.class);

    private static void initNSO() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getCorvusMode()){
            new NoSuchOrg_Gen().generate(Global.getSector());
            // Exerelin not found so continue and run normal generation code
        }
    }

    @Override
    public void onApplicationLoad() {
    }

    @Override
    public void onNewGame() {
        initNSO();
    }
    
    @Override
    public void onNewGameAfterEconomyLoad() {
        MarketAPI market = Global.getSector().getEconomy().getMarket("nso_tombstone");
        if (market != null && false) {//Short circuit off for now, was testing.
            log.info("Adding admin");
            PersonAPI admin = Global.getFactory().createPerson();
            admin.setFaction("no_such_org");
            admin.setGender(FullName.Gender.MALE);
            admin.setPostId(Ranks.AGENT);
            admin.setRankId(Ranks.AGENT);
            admin.getName().setFirst("Havelock");
            admin.getName().setLast("Meserole");
            admin.setPortraitSprite("graphics/kayse/portraits/havelock.png");
            admin.setContactWeight(100f);
            admin.setId("nso_havelock");
            admin.setImportance(PersonImportance.HIGH);
            //admin.
            admin.addTag("military");
            Global.getSector().getImportantPeople().addPerson(admin);
            /*BaseMissionHub.set(admin, new BaseMissionHub(admin));
            admin.getMemoryWithoutUpdate().set(BaseMissionHub.NUM_BONUS_MISSIONS, 1);*/
            log.info( admin.getPortraitSprite() );
            //                      "graphics/kayse/portraits";
            //min.setPortraitSprite("graphics/tahlan/portraits/yurika.png");

//            admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
//            admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
//            admin.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 3);

            market.setAdmin(admin);
            market.getCommDirectory().addPerson(admin, 0);
            market.addPerson(admin);
        }
    }
}
