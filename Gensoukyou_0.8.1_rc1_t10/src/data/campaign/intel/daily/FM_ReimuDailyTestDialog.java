package data.campaign.intel.daily;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import java.util.Map;

public class FM_ReimuDailyTestDialog implements InteractionDialogPlugin {
    public InteractionDialogAPI dialog;
    public PersonAPI person;

    protected Map<String, MemoryAPI> memoryMap;

    protected enum OptionId {
        INIT,
        STAGE_1,
        STAGE_2,
        STAGE_3,
        DONE,
    }

    @Override
    public void init(InteractionDialogAPI dialog) {

        if (person == null) {
            person = Global.getFactory().createPerson();
            person.setPortraitSprite(Global.getSettings().getSpriteName("intel", "FM_Reimu"));
            person.setName(new FullName("Hakurei", "Reimu", FullName.Gender.FEMALE));
        }
        this.dialog = dialog;

        dialog.getVisualPanel().showPersonInfo(person, true);

        optionSelected(null, OptionId.INIT);

    }

    @Override
    public void optionSelected(String optionText, Object optionData) {

        if (!(optionData instanceof OptionId)) {
            return;
        }
        OptionId option = (OptionId) optionData;

        OptionPanelAPI options = dialog.getOptionPanel();
        TextPanelAPI text = dialog.getTextPanel();
        options.clearOptions();

        switch (option) {

            case INIT: {

                options.addOption("Next Test", OptionId.STAGE_1);
                break;
            }
            case STAGE_1: {


                options.addOption("Next Test", OptionId.STAGE_2);
                break;
            }
            case STAGE_2: {


                options.addOption("Next Test", OptionId.STAGE_3);
                break;
            }
            case STAGE_3: {


                options.addOption("Next Test", OptionId.DONE);
                break;
            }
            case DONE: {

                dialog.dismiss();
                break;
            }


        }

    }

    @Override
    public void advance(float amount) {
    }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {
    }

    public Object getContext() {
        return null;
    }

    public Map<String, MemoryAPI> getMemoryMap() {
        return memoryMap;
    }


}
