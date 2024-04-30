package data.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FighterWingAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.FantasySpellMod;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;


/**
 * Scripted by AnyIDElse, Nitori_Tachyon, homejerry99,just for convenient.
 */
public class FM_Misc {

    //尽管直接写入不可能，但仍然会因为赋值之后再写入而出现问题
    //更简单的来说，当你需要一个新的向量的时候，还是应当new一个
    //于是这里这个只能用做一个数字
    public static final Vector2f ZERO = new Vector2f();

    //方便的get舰载机的方法
    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        return getFighters(carrier, true);
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier, boolean includeReturn) {
        Set<ShipAPI> result = new HashSet<>();
        for (FighterWingAPI wing : carrier.getAllWings()) {
            result.addAll(wing.getWingMembers());

            if (includeReturn) {
                for (FighterWingAPI.ReturningFighter ret : wing.getReturning()) {
                    result.add(ret.fighter);
                }
            }
        }
        return new ArrayList<>(result);
    }

    //一种贝塞尔曲线，t在(0,1)区间
    public static Vector2f BezierCurvePoint(float t, Vector2f begin, Vector2f end, Vector2f medium) {

        Vector2f point;

        Vector2f p0 = (Vector2f) new Vector2f(0, 0).scale(1 - t * t);
        Vector2f p1 = new Vector2f((ReadableVector2f) Vector2f.sub(medium, begin, new Vector2f()).scale(2 * t * (1 - t)));
        Vector2f p2 = new Vector2f((ReadableVector2f) Vector2f.sub(end, begin, new Vector2f()).scale(t * t));


        point = new Vector2f(p0.x + p1.x + p2.x, p0.y + p1.y + p2.y);

        return Vector2f.add(point, begin, point);

    }

    //正弦轨迹
    //amount为弹头自身时间(Elapsed即可)，dir为初速(重点)，K为振幅相关(可为负数)，L为波长相关
    public static void sineEffect(DamagingProjectileAPI project, float amount, Vector2f dir, float K, float L) {

        if (project.getWeapon() == null) return;

        float T = project.getWeapon().getRange() / dir.length();
        float a = (float) (2 * Math.PI / T);

        float y = (float) (K * FastTrig.cos(amount * a * L));


        Vector2f dir_y = new Vector2f();
        VectorUtils.rotate(dir, 90f, dir_y);

        Vector2f.add((Vector2f) dir_y.scale(y), dir, project.getVelocity());

        project.setFacing(VectorUtils.getFacing(project.getVelocity()));

    }

    public static FantasySpellMod.SpellModState getSpellModState(@NotNull CombatEngineAPI engine, @NotNull ShipAPI ship) {

        if (!engine.getCustomData().containsKey(FantasySpellMod.SpellModId)) {
            engine.getCustomData().put(FantasySpellMod.SpellModId, new HashMap<>());
        }
        Map<ShipAPI, FantasySpellMod.SpellModState> currState = (Map) engine.getCustomData().get(FantasySpellMod.SpellModId);
        if (!currState.containsKey(ship)) {
            currState.put(ship, new FantasySpellMod.SpellModState());
        }

        return currState.get(ship);


    }

    public static float getSystemRange(@NotNull ShipAPI ship, float range) {
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(
                range
        );
    }

    //生涯实用方法与框架，来自Nitori_Tachyon
    //关系等级，只是因为懒得翻原版的预设
    public static final float HugeRelChange=0.18f,BigRelChange=0.12f,MidRelChange=0.08f,SlightRelChange=0.04f,
            baseRelNeeded=0.15f,advancedRelNeeded=0.5f;
    public static final float[] reltier={-0.8f,-0.5f,-0.2f,0f,0.2f,0.5f,0.8f};

    //增加评价等级，致敬某游戏
    public static void addRating(int delta, TextPanelAPI text)
    {
        int rating= Global.getSector().getMemoryWithoutUpdate().getInt("$FM_Rating")+delta;
        Global.getSector().getMemoryWithoutUpdate().set("$FM_Rating",rating);
        int _rating=Global.getSector().getMemoryWithoutUpdate().getInt("$FM_Rating_Total")+delta;
        Global.getSector().getMemoryWithoutUpdate().set("$FM_Rating_Total",_rating);
        LabelAPI labAR=text.addPara(I18nUtil.getString("misc","FM_addRating"), Misc.getHighlightColor(),Integer.toString(delta),Integer.toString(rating),Integer.toString(_rating));
        labAR.setHighlightColors(Misc.getHighlightColor());
        labAR.setHighlight(Integer.toString(delta),Integer.toString(rating),Integer.toString(_rating));
    }

    //用于替换原版关系改变，由于原版不是很支持单独与NPC关系的改变所以干脆全部写了以保持函数组的一致性
    //另外一提，如果是东方的话NPC之间对话内容也可以随着她们之间的关系不同而变化，所以引入了changeRelBetweenPerson，NPC之间关系储存在游戏内FM势力变量
    public static void changeRelPerson(PersonAPI target, float delta, TextPanelAPI text)
    {
        CoreReputationPlugin.CustomRepImpact impact=new CoreReputationPlugin.CustomRepImpact();
        impact.delta=delta;
        Global.getSector().adjustPlayerReputation(new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM,impact,null,text,true),target);
    }
    public static void changeRelBetweenPerson(PersonAPI target1,PersonAPI target2, float delta, TextPanelAPI text,boolean withText)
    {
        FactionAPI FM=Global.getSector().getFaction(I18nUtil.getString("misc","FM_Id"));
        String changeDir=delta>0?I18nUtil.getString("misc","FM_RelRise"):I18nUtil.getString("misc","FM_RelDec");
        float rel=FM.getMemoryWithoutUpdate().getFloat("$FM_Rel_"+target1.getId()+"_to_"+target2.getId());
        FM.getMemoryWithoutUpdate().set("$FM_Rel_"+target1.getId()+"_to_"+target2.getId(),rel+delta);
        FM.getMemoryWithoutUpdate().set("$FM_Rel_"+target2.getId()+"_to_"+target1.getId(),rel+delta);
        if (!withText)return;
        LabelAPI Labeltmp=text.addPara(target1.getNameString()+" 与 "+target2.getNameString()+" "+changeDir+" "+(int)(delta*100f)+". 目前的关系是 "+(int)((rel+delta)*100f)+"/100 ("+ RepLevel.getLevelFor(rel+delta).getDisplayName()+")");
        Labeltmp.setHighlight(target1.getNameString()+" 与 "+target2.getNameString(),changeDir+" "+(int)(delta*100f),". 目前的关系是 ",(int)((rel+delta)*100f)+"/100 ("+RepLevel.getLevelFor(rel+delta).getDisplayName()+")");
        Labeltmp.setHighlightColors(Misc.getGrayColor(),delta>0?Misc.getPositiveHighlightColor():Misc.getNegativeHighlightColor(),Misc.getGrayColor(),Misc.getRelColor(rel+delta));
    }
    public static void changeRelFaction(FactionAPI target, float delta, TextPanelAPI text)
    {
        Misc.adjustRep(delta,null,target.getId(),0f,null,null,text);
    }
    public static void changeRelBetweenFaction(FactionAPI target1,FactionAPI target2,float delta, TextPanelAPI text)
    {
        String changeDir=delta>0?I18nUtil.getString("misc","FM_RelRise"):I18nUtil.getString("misc","FM_RelDec");
        target1.adjustRelationship(target2.getId(),delta,null);
        LabelAPI Labeltmp=text.addPara(target1.getDisplayName()+" 与 "+target2.getDisplayName()+" "+changeDir+" "+(int)(delta*100f)+". 目前的关系是 "+(int)(target1.getRelationship(target2.getId())*100f)+"/100 ("+target1.getRelationshipLevel(target2.getId()).getDisplayName()+")");
        Labeltmp.setHighlight(target1.getDisplayName()," 与 ",target2.getDisplayName(),changeDir+" "+(int)(delta*100f),". 目前的关系是 ",(int)(target1.getRelationship(target2.getId())*100f)+"/100 ("+target1.getRelationshipLevel(target2.getId()).getDisplayName()+")");
        Labeltmp.setHighlightColors(target1.getColor(),Misc.getGrayColor(),target2.getColor(),delta>0?Misc.getPositiveHighlightColor():Misc.getNegativeHighlightColor(),Misc.getGrayColor(),Misc.getRelColor(target1.getRelationship(target2.getId())));
    }

    //在target身上添加provider的羁绊
    public static void addBond(PersonAPI target,PersonAPI provider, TextPanelAPI text,String skill)
    {
        target.getStats().setSkillLevel(skill,3);
        LabelAPI AJL2=text.addPara(target.getNameString()+" 获得了与 "+provider.getNameString()+" 的羁绊!");
        AJL2.setHighlightColors(Misc.getHighlightColor());
        AJL2.setHighlight(target.getNameString(),provider.getNameString());
    }
}
