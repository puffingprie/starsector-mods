package data.utils.visual;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;

public class FM_ParticleManager {
    public static FM_StarParticle getStarParticleManager(CombatEngineAPI engine) {
        FM_StarParticle visual = (FM_StarParticle) Global.getCombatEngine().getCustomData().get(FM_StarParticle.FM_StarParticleString);
        if (visual == null || !Global.getCombatEngine().getCustomData().containsKey(FM_StarParticle.FM_StarParticleString)) {
            visual = new FM_StarParticle();
            engine.getCustomData().put(FM_StarParticle.FM_StarParticleString, visual);
            engine.addLayeredRenderingPlugin(visual);
        }
        return visual;
    }

    public static FM_DiamondParticle3DTest getDiamondParticleManager(CombatEngineAPI engine) {
        FM_DiamondParticle3DTest visual = (FM_DiamondParticle3DTest) Global.getCombatEngine().getCustomData().get(FM_DiamondParticle3DTest.FM_DiamondParticleString);
        if (visual == null || !Global.getCombatEngine().getCustomData().containsKey(FM_DiamondParticle3DTest.FM_DiamondParticleString)) {
            visual = new FM_DiamondParticle3DTest();
            engine.getCustomData().put(FM_DiamondParticle3DTest.FM_DiamondParticleString, visual);
            engine.addLayeredRenderingPlugin(visual);
        }
        return visual;
    }

    public static FM_TriangleParticleNew getTriangleParticleManager(CombatEngineAPI engine) {
        FM_TriangleParticleNew visual = (FM_TriangleParticleNew) Global.getCombatEngine().getCustomData().get(FM_TriangleParticleNew.FM_TriangleParticleString);
        if (visual == null || !Global.getCombatEngine().getCustomData().containsKey(FM_TriangleParticleNew.FM_TriangleParticleString)) {
            visual = new FM_TriangleParticleNew();
            engine.getCustomData().put(FM_TriangleParticleNew.FM_TriangleParticleString, visual);
            engine.addLayeredRenderingPlugin(visual);
        }
        return visual;
    }

    @Deprecated
    public static FM_MaskAndGlow getMaskAndGlowTestManager(CombatEngineAPI engine) {
        FM_MaskAndGlow visual = (FM_MaskAndGlow) Global.getCombatEngine().getCustomData().get(FM_MaskAndGlow.FM_MaskAndGlowTestString);
        if (visual == null || !Global.getCombatEngine().getCustomData().containsKey(FM_MaskAndGlow.FM_MaskAndGlowTestString)) {
            visual = new FM_MaskAndGlow();
            engine.getCustomData().put(FM_MaskAndGlow.FM_MaskAndGlowTestString, visual);
            engine.addLayeredRenderingPlugin(visual);
        }
        return visual;
    }
}
