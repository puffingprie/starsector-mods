package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Scripted by Nitori_Tachyon
 */
public class FM_Person {
    //每一个新角色登场，就调用一次addCharacter(person,market,isStored)
    //用途:希腊奶，目前只是规范化
    //也许未来人物的更多功能也可以在这个类下面写
    private static List<FM_Person> CharacterList=new ArrayList<>();
    private static HashSet<String> JoinedCharacters=new HashSet<>();

    private PersonAPI person;
    private boolean isStored;
    private MarketAPI market;

    private FM_Person(PersonAPI person,MarketAPI market,boolean isStored)
    {//constructor
        this.person=person;
        this.market=market;
        this.isStored=isStored;
//        if (CharacterList == null){
//            CharacterList = new ArrayList<>();
//        }
    }

    public static void setMarket(String name,MarketAPI market)
    {
        if (!hasMetCharacter(name))return;
        getCharacterWithName(name).setMarket(market);
    }
    public static void setStored(String name,boolean isStored)
    {
        if (!hasMetCharacter(name))return;
        getCharacterWithName(name).setStored(isStored);
    }

    @NotNull
    public static PersonAPI getPerson(String name)
    {
        if (!hasMetCharacter(name))return new Person();
        return getCharacterWithName(name).getPerson();
    }
    public static MarketAPI getMarket(String name)
    {
        UpdateSituation();
        if (!hasMetCharacter(name))return null;
        return getCharacterWithName(name).getMarket();
    }
    public static boolean isStored(String name)
    {
        if (!hasMetCharacter(name))return false;
        return getCharacterWithName(name).isStored();
    }
    public static boolean isInFleet(String name)
    {
        if (!hasMetCharacter(name))return false;
        return getCharacterWithName(name).isInFleet();
    }

    public static void setMarket(PersonAPI person,MarketAPI market)
    {
        setMarket(person.getId(),market);
    }
    public static void setStored(PersonAPI person,boolean isStored)
    {
        setStored(person.getId(),isStored);
    }

    public static boolean isStored(PersonAPI person)
    {
        return isStored(person.getId());
    }
    public static boolean isInFleet(PersonAPI person)
    {
        return isInFleet(person.getId());
    }

    public static boolean addCharacter(PersonAPI person,MarketAPI market,boolean isStored)
    {
        if (!JoinedCharacters.add(person.getId()))return false;
        CharacterList.add(new FM_Person(person,market,isStored));
        return true;
    }
    public static boolean hasMetCharacter(String name)
    {//判断是否已见过该角色（指点进过角色对应酒馆事件，不论结局如何）
        return JoinedCharacters.contains(name);
    }
    public static boolean hasMetCharacter(PersonAPI person)
    {//判断是否已见过该角色（指点进过角色对应酒馆事件，不论结局如何）
        return hasMetCharacter(person.getId());
    }

    public static void UpdateSituation()
    {//更新列表中所有已储存的角色所处的市场
        List<MarketAPI> planets= Misc.getFactionMarkets(I18nUtil.getString("misc","FM_Id"));
        if (planets.isEmpty()||CharacterList.isEmpty())return;
        for (int i=0;i<CharacterList.size();i+=1)
        {
            FM_Person character=CharacterList.get(i);
            if (character.isStored())
            {
                if (!planets.contains(character.getMarket()))
                {
                    character.setMarket(planets.get(new Random().nextInt(planets.size())));
                }
            }
        }
    }

    public static void savePerson(){//保存人物信息
        UpdateSituation();
        Global.getSector().getFaction(I18nUtil.getString("misc","FM_Id")).getMemoryWithoutUpdate().set("$FM_Person",CharacterList);
    }
    @SuppressWarnings("unchecked")//因为在读取游戏前,必定保存过游戏，即运行savePerson(),所以从$FM_Person获取的List一定是有效的
    public static void loadPerson(){//读取人物信息
        CharacterList=(List<FM_Person>)Global.getSector().getFaction(I18nUtil.getString("misc","FM_Id")).getMemoryWithoutUpdate().get("$FM_Person");
    }

    private PersonAPI getPerson()
    {
        return this.person;
    }
    private MarketAPI getMarket()
    {
        return this.market;
    }
    private boolean isStored()
    {
        return this.isStored;
    }
    private  boolean isInFleet()
    {
        return this.getPerson().getFleet().equals(Global.getSector().getPlayerFleet());
    }
    private void setPerson(PersonAPI person)
    {
        this.person=person;
    }
    private void setMarket(MarketAPI market)
    {
        this.market=market;
        this.getPerson().setMarket(market);
    }
    private void setStored(boolean isStored)
    {
        this.isStored=isStored;
    }
    @NotNull
    private static FM_Person getCharacterWithName(String name)
    {//获取角色id对应的实例
        int index=getIndexWithName(name);
        try
        {
            if (index==-1)
            {
                throw new IndexOutOfBoundsException();
            }
        }
        catch (IndexOutOfBoundsException e)
        {//Should never happen
            return new FM_Person(null,null,false);
        }
        return CharacterList.get(index);
    }
    private static int getIndexWithName(String name)
    {//获取角色id对应在CharacterList中的下标
        //if (CharacterList == null) CharacterList = new ArrayList<>();
        if (CharacterList.isEmpty()) return -1;
        for (int i = 0; i < CharacterList.size(); i += 1)
        {
            if (CharacterList.get(i).getPerson().getId().equals(name))
            {
                return i;
            }
        }
        return -1;
    }
}
