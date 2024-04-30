package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.intel.GenericMissionManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import data.campaign.intel.bar.events.FM_ContactWithTokikoCreator;
import data.campaign.intel.bar.events.FM_ReimuBarEventCreator;
import data.campaign.intel.daily.FM_ReimuDailyTestIntelCreator;
import data.campaign.intel.events.FM_KutakaEventCreator;
import data.utils.FM_Person;
import data.weapons.missileAI.FM_Opposition_missile_ai;
import data.weapons.weaponAI.FM_MikoBuiltInFireAI;
import data.world.FMGen;
import exerelin.campaign.SectorManager;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

import static com.fs.starfarer.api.Global.getSettings;

public class Fantasyplugin extends BaseModPlugin {


    public static boolean hasGraphicsLib;


    @Override

    public void onApplicationLoad() throws Exception {
        hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");

        //check if lazylib exists
        boolean hasLazyLib = getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) {
            throw new RuntimeException("FM requires LazyLib!");
        }
        //check if MagicLib exists
        boolean hasMagicLib = getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) {
            throw new RuntimeException("FM requires MagicLib!");
        }
        //check if GraphicsLib exists

        boolean hasGraphicLib = getSettings().getModManager().isModEnabled("shaderLib");
        if (!hasGraphicLib) {
            throw new RuntimeException("FM requires GraphicLib!");
        }

        if (hasGraphicsLib) {
            ShaderLib.init();
            LightData.readLightDataCSV("data/lights/FM_light_data.csv");
            TextureData.readTextureDataCSV("data/lights/FM_texture_data.csv");
        }
    }

    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        if (missile.getProjectileSpecId().equals("FM_Opposition_proj")) {
            return new PluginPick<MissileAIPlugin>(new FM_Opposition_missile_ai(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
        return null;
    }
    @Override
    public PluginPick<AutofireAIPlugin> pickWeaponAutofireAI(WeaponAPI weapon) {
        if (weapon.getId().equals("FM_MikoBuiltIn")) {
            return new PluginPick<AutofireAIPlugin>(new FM_MikoBuiltInFireAI(weapon), CampaignPlugin.PickPriority.MOD_SPECIFIC);
        }
        return null;
    }
    //和酒馆事件的添加相关
    protected void addBarEvents() {
        BarEventManager bar = BarEventManager.getInstance();
        if (!bar.hasEventCreator(FM_ReimuBarEventCreator.class)) {
            bar.addEventCreator(new FM_ReimuBarEventCreator());
        }
        if (!bar.hasEventCreator(FM_ContactWithTokikoCreator.class)) {
            bar.addEventCreator(new FM_ContactWithTokikoCreator());
        }
    }

    protected void addScriptsIfNeeded() {

        GenericMissionManager manager = GenericMissionManager.getInstance();
// 		Replaced with bar/contact com.fs.starfarer.api.impl.campaign.missions.ProcurementMission
//		if (!manager.hasMissionCreator(ProcurementMissionCreator.class)) {
//			manager.addMissionCreator(new ProcurementMissionCreator());
//		}
//        if (!Global.getSector().hasScript(FM_ReimuDailyTestIntelCreator.class)) {
//            Global.getSector().addScript(new FM_ReimuDailyTestIntelCreator());
//        }
//        if (!Global.getSector().hasScript(FM_KutakaEventCreator.class)){
//            Global.getSector().addScript(new FM_KutakaEventCreator());
//        }

    }
    @Override
    public void beforeGameSave() {
        FM_Person.savePerson();
    }
    @Override
    public void onGameLoad(boolean newGame) {
        addBarEvents();
        if (!newGame)FM_Person.loadPerson();
        addScriptsIfNeeded();
    }
    @Override
    public void onNewGameAfterEconomyLoad() {
        //addScriptsIfNeeded();
    }
//    @Override
//    public void onNewGameAfterTimePass(){
//        EveryFrameScript script = new FM_ReimuDailyTestIntelCreator().createEvent();
//
//        if (script instanceof BaseIntelPlugin){
//            ((BaseIntelPlugin)script).setPostingLocation(null);
//            GenericMissionManager.getInstance().addActive(script);
//        }
//    }
//    @Override
//    public void configureXStream(XStream x){
//        super.configureXStream(x);
//        x.alias("FM_ReimuDailyTestIntelCreator", FM_ReimuDailyTestIntelCreator.class);
//    }

    public void onNewGame() {
        //Nex compatibility setting, if there is no nex or corvus mode(Nex), just generate the system
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        if (!haveNexerelin || SectorManager.getManager().isCorvusMode()) {
            new FMGen().generate(Global.getSector());
        }


    }


}
