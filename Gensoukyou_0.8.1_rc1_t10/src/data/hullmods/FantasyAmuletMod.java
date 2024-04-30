package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
//import com.fs.starfarer.api.loading.DamagingExplosionSpec;
//import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.entities.DamagingExplosion;
import data.utils.FM_Colors;
import data.utils.FM_Misc;
import data.utils.I18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FantasyAmuletMod extends BaseHullMod {

    public static final float TIME = 0.4f;
    public static final float MAX_BUFF_OF_SPELLMOD = 15f;
    public static final float SpellSupply = 0.04f;
    private static final String buffId = "FantasyAmuletMod_Buff";
    private final Object key = new Object();

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        super.applyEffectsAfterShipCreation(ship, id);
        ship.addListener(new MikoHitListener(ship));
    }

    public void advanceInCombat(ShipAPI ship, float amount) {

        if (ship == null) {
            return;
        }
        if (!ship.isAlive()) return;
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        if (!engine.getCustomData().containsKey("FantasyAmuletMod")) {
            engine.getCustomData().put("FantasyAmuletMod", new HashMap<>());
        }
        Map<ShipAPI, FantasyAmuletMod.ModState> currState = (Map) engine.getCustomData().get("FantasyAmuletMod");
        if (!currState.containsKey(ship)) {
            currState.put(ship, new ModState());
        }
        //灵子容器部分
        if (!ship.getVariant().hasHullMod("FantasySpellMod")) return;
        FantasySpellMod.SpellModState spellModState = FM_Misc.getSpellModState(engine, ship);
        spellModState.spellPower = spellModState.spellPower + engine.getElapsedInLastFrame() * (0.01f + SpellSupply);
        float percent = spellModState.spellPower * MAX_BUFF_OF_SPELLMOD;
        if (percent >= 0) {
            ship.getMutableStats().getMaxSpeed().modifyPercent(buffId, percent);
            ship.getMutableStats().getMaxTurnRate().modifyPercent(buffId, percent);
            ship.getMutableStats().getAcceleration().modifyPercent(buffId, percent);
            ship.getMutableStats().getDeceleration().modifyPercent(buffId, percent);
            ship.getMutableStats().getTurnAcceleration().modifyPercent(buffId, percent);
        } else {
            ship.getMutableStats().getMaxSpeed().unmodifyPercent(buffId);
            ship.getMutableStats().getMaxTurnRate().unmodifyPercent(buffId);
            ship.getMutableStats().getAcceleration().unmodifyPercent(buffId);
            ship.getMutableStats().getDeceleration().unmodifyPercent(buffId);
            ship.getMutableStats().getTurnAcceleration().unmodifyPercent(buffId);
        }
        if (ship == engine.getPlayerShip()) {
            engine.maintainStatusForPlayerShip(key, ship.getSystem().getSpecAPI().getIconSpriteName(),
                    I18nUtil.getHullModString("FantasyAmuletMod_PlayerTitle"),
                    I18nUtil.getHullModString("FantasyAmuletMod_PlayerData") + (int) percent + "%", false);
        }
        //灵子容器视觉部分
        SpriteAPI effect = Global.getSettings().getSprite("fx", "FM_MikoEffect");
        Vector2f size = new Vector2f(effect.getWidth(), effect.getHeight());
        currState.get(ship).alphaForVisual = currState.get(ship).alphaForVisual + amount;

//        float alpha = MagicAnim.smoothReturnNormalizeRange(currState.get(ship).alphaForVisual,0f,1f) * spellModState.spellPower;
//        float jitterRange = alpha * 10;
        if (currState.get(ship).alphaForVisual >= 1f) {
            currState.get(ship).alphaForVisual = 0f;
            //engine.addFloatingText(ship.getLocation(),"" + alpha,20f,Color.WHITE,ship,0f,0f);
            for (int i = 0; i < 4; i = i + 1) {
                MagicRender.objectspace(
                        effect,
                        ship,
                        FM_Misc.ZERO,
                        FM_Misc.ZERO,
                        size,
                        ship.getRenderOffset(),
                        -180f,
                        0f,
                        true,
                        Misc.scaleAlpha(FM_Colors.FM_RED_EMP_FRINGE, Math.min(spellModState.spellPower, 1f)),
                        spellModState.spellPower * 3.5f,
                        0f,
                        1f,
                        1f,
                        0f,
                        0.3f,
                        0.3f,
                        0.4f,
                        true,
                        CombatEngineLayers.ABOVE_SHIPS_LAYER,
                        GL11.GL_SRC_ALPHA, GL11.GL_ONE
                );
            }
        }

//        if (alpha >= 1){alpha = 1f;}
//        if (alpha <= 0){alpha = 0f;}

        //MagicRender.singleframe(effect,ship.getLocation(),size,ship.getFacing() - 90f, Misc.scaleAlpha(Color.WHITE,alpha),true,CombatEngineLayers.ABOVE_SHIPS_LAYER);
        //御札诱导部分
        if (!currState.get(ship).isActive) {
            currState.get(ship).weapons = ship.getAllWeapons();
            currState.get(ship).isActive = true;
        }
        if (currState.get(ship).isActive) {
            currState.get(ship).timer = currState.get(ship).timer + amount;
            if (currState.get(ship).timer >= TIME) {
                currState.get(ship).timer = TIME;
            }

        }
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSpacer(10f);
        tooltip.addSectionHeading(I18nUtil.getHullModString("FM_DescriptionAndEvaluation"), Alignment.TMID, 4f);
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
        tooltip.addPara(I18nUtil.getHullModString("FantasyAmulet_DAE_0"), Misc.getTextColor(), 4f);
        tooltip.addSpacer(10f);
        tooltip.addPara(I18nUtil.getHullModString("FantasyAmulet_DAE_1"), Misc.getGrayColor(), 4f);
        }
        if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
            tooltip.addPara("Press and hold [%s] to view this information.", Float.valueOf(10.0f), Misc.getGrayColor(), Misc.getStoryBrightColor(), new String[]{"F1"}).setAlignment(Alignment.MID);
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int) (SpellSupply * 100f) + "%";
        if (index == 1) return "" + (int) (MAX_BUFF_OF_SPELLMOD) + "%";
        if (index == 2) return "" + 8;
        if (index == 3) return TIME + I18nUtil.getHullModString("FantasyAmuletMod_HL_1");
        if (index == 4) return 70 + I18nUtil.getHullModString("FantasyAmuletMod_HL_2");

        return null;
    }

    public static class MikoHitListener implements DamageDealtModifier {
        private final ShipAPI ship;
        public MikoHitListener(ShipAPI ship){
            this.ship = ship;
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (Global.getCombatEngine() == null)return null;
            CombatEngineAPI engine = Global.getCombatEngine();
            if (!engine.getCustomData().containsKey("FantasyAmuletMod")) {
                return null;
            }
            Map<ShipAPI,ModState> currState = (Map) engine.getCustomData().get("FantasyAmuletMod");
            if (!currState.containsKey(ship))return null;
            ModState shipState = currState.get(ship);

            if (param == null)return null;
            if (!(param instanceof DamagingProjectileAPI))return null;
            if (!(target instanceof ShipAPI))return null;
            if (param instanceof MissileAPI)return null;
            if (((DamagingProjectileAPI) param).isFromMissile())return null;
            if (param instanceof DamagingExplosion)return null;
            if (shipState.timer >= TIME){
                for (WeaponAPI weapon : shipState.weapons) {
                    if (weapon.getId().equals("FM_Amulet_B")) {
                        engine.spawnProjectile(ship, weapon, "FM_Amulet_B", weapon.getLocation(), MathUtils.getRandomNumberInRange(weapon.getCurrAngle() - 7.5f,weapon.getCurrAngle() + 7.5f), new Vector2f());
                        Global.getSoundPlayer().playSound("harpoon_fire", 10f, 0.5f, weapon.getLocation(), ship.getVelocity());
                        MagicRender.battlespace(Global.getSettings().getSprite("fx", "FM_modeffect_4"),
                                weapon.getLocation(),
                                MathUtils.getRandomPointInCircle(weapon.getShip().getVelocity(), 20f), new Vector2f(30f, 30f),
                                new Vector2f(10f, 10f), MathUtils.getRandomNumberInRange(0, 360),
                                10f, new Color(236, 56, 56, 221), true, 0.1f, 0.6f, 0.3f);
                    }
                }
                shipState.timer = 0f;
            }
            return ship.getId() + "_MikoHitListener";
        }
    }
    private final static class ModState {
        boolean isActive;
        List<WeaponAPI> weapons;
        float timer;
        float alphaForVisual;
        private ModState() {
            isActive = false;
            timer = 0f;
            alphaForVisual = 0f;

        }
    }

}
