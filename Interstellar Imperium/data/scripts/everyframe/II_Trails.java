package data.scripts.everyframe;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.IIModPlugin;
import data.scripts.util.II_Util;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

public class II_Trails extends BaseEveryFrameCombatPlugin {

    private static final String ARCUS_PROJECTILE_ID = "ii_arcus_shot";
    private static final String SIEGE_MORTAR_PROJECTILE_ID = "ii_siegemortar_shot";
    private static final String SOLIS_PROJECTILE_ID = "ii_solis_shot";
    private static final String TELUM_PROJECTILE_ID = "ii_telum_shot";
    private static final String SLEDGE_GUN_PROJECTILE_ID = "ii_sledgegun_shot";
    private static final String SLEDGE_CANNON_PROJECTILE_ID = "ii_sledgecannon_shot";
    private static final String PULSAR_PROJECTILE_ID = "ii_pulsar_shot";
    private static final String HEAVY_PULSAR_PROJECTILE_ID = "ii_heavypulsar_shot";

    private static final String CROCEA_MORS_PROJECTILE_ID = "ii_croceamors_shot";
    private static final String PULSAR_BOMB_PROJECTILE_ID = "ii_pulsarbomb_shot";
    private static final String MAGNA_FULMEN_STANDARD_PROJECTILE_ID = "ii_magna_fulmen_standard";
    private static final String MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID = "ii_magna_fulmen_standard_armor";
    private static final String MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID = "ii_magna_fulmen_standard_targeting";
    private static final String MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID = "ii_magna_fulmen_standard_elite";
    private static final String MAGNA_FULMEN_ENHANCED_ARMOR_PROJECTILE_ID = "ii_magna_fulmen_enhanced_armor";
    private static final String MAGNA_FULMEN_ENHANCED_TARGETING_PROJECTILE_ID = "ii_magna_fulmen_enhanced_targeting";
    private static final String MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID = "ii_magna_fulmen_enhanced_elite1";
    private static final String MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID = "ii_magna_fulmen_enhanced_elite2";
    private static final String MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID = "ii_magna_fulmen_enhanced_elite3";
    private static final String MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID = "ii_magna_fulmen_enhanced_elite4";

    private static final Color ARCUS_TRAIL_COLOR = new Color(170, 220, 255);
    private static final Color SIEGE_MORTAR_TRAIL_COLOR = new Color(255, 175, 70);
    private static final Color SOLIS_TRAIL_COLOR_START = new Color(255, 150, 0);
    private static final Color SOLIS_TRAIL_COLOR_END = new Color(60, 20, 0);
    private static final Color SOLIS_TRAIL_COLOR2 = new Color(255, 120, 0);
    private static final Color TELUM_TRAIL_COLOR_START = new Color(175, 175, 240);
    private static final Color TELUM_TRAIL_COLOR_END = new Color(180, 180, 190);
    private static final Color SLEDGE_GUN_TRAIL_COLOR_START = new Color(0, 25, 255);
    private static final Color SLEDGE_GUN_TRAIL_COLOR_END = new Color(0, 150, 255);
    private static final Color SLEDGE_CANNON_TRAIL_COLOR_START = new Color(0, 25, 255);
    private static final Color SLEDGE_CANNON_TRAIL_COLOR_END = new Color(0, 150, 255);
    private static final Color PULSAR_TRAIL_COLOR_START = new Color(255, 150, 0);
    private static final Color PULSAR_TRAIL_COLOR_END = new Color(255, 50, 50);
    private static final Color PULSAR_TRAIL_COLOR2 = new Color(255, 50, 0);
    private static final Color HEAVY_PULSAR_TRAIL_COLOR_START = new Color(255, 100, 0);
    private static final Color HEAVY_PULSAR_TRAIL_COLOR_END = new Color(255, 50, 100);
    private static final Color HEAVY_PULSAR_TRAIL_COLOR2 = new Color(255, 50, 0);

    private static final Color CROCEA_MORS_TRAIL_COLOR_START = new Color(255, 150, 15);
    private static final Color CROCEA_MORS_TRAIL_COLOR_END = new Color(120, 40, 10);
    private static final Color CROCEA_MORS_TRAIL_COLOR2 = new Color(255, 160, 10);
    private static final Color PULSAR_BOMB_TRAIL_COLOR_START = new Color(255, 50, 0);
    private static final Color PULSAR_BOMB_TRAIL_COLOR_END = new Color(255, 50, 150);
    private static final Color PULSAR_BOMB_TRAIL_COLOR2 = new Color(255, 50, 0);
    private static final Color MAGNA_FULMEN_STANDARD_TRAIL_COLOR_START = new Color(255, 150, 50);
    private static final Color MAGNA_FULMEN_STANDARD_TRAIL_COLOR_END = new Color(255, 180, 75);
    private static final Color MAGNA_FULMEN_STANDARD_TRAIL_COLOR2 = new Color(255, 155, 100);
    private static final Color MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR_START = new Color(255, 205, 50);
    private static final Color MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR_END = new Color(255, 225, 75);
    private static final Color MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR2 = new Color(255, 190, 100);
    private static final Color MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR_START = new Color(50, 155, 255);
    private static final Color MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR_END = new Color(75, 150, 255);
    private static final Color MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR2 = new Color(100, 200, 255);
    private static final Color MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR_START = new Color(185, 50, 255);
    private static final Color MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR_END = new Color(210, 75, 255);
    private static final Color MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR2 = new Color(175, 100, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_ARMOR_TRAIL_COLOR = new Color(255, 190, 100);
    private static final Color MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR_START = new Color(50, 155, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR_END = new Color(75, 150, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR2 = new Color(100, 200, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR_START = new Color(185, 50, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR_END = new Color(210, 75, 255);
    private static final Color MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR2 = new Color(175, 100, 255);

    private static final float SIXTY_FPS = 1f / 60f;

    private static final String DATA_KEY = "II_Trails";

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<DamagingProjectileAPI, TrailData> trailMap = localData.trailMap;

        List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
        int size = projectiles.size();
        double trailCount = 0f;
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI projectile = projectiles.get(i);
            if (projectile.getProjectileSpecId() == null) {
                continue;
            }

            switch (projectile.getProjectileSpecId()) {
                case ARCUS_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.2f)) {
                        trailCount += 1f;
                    }
                    break;
                case SIEGE_MORTAR_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.3f)) {
                        trailCount += 1f;
                    }
                    break;
                case SOLIS_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.25f)) {
                        trailCount += 2f;
                    }
                    break;
                case TELUM_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.6f)) {
                        trailCount += 1f;
                    }
                    break;
                case SLEDGE_GUN_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 1f;
                    }
                    break;
                case SLEDGE_CANNON_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 1f;
                    }
                    break;
                case PULSAR_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.15f)) {
                        trailCount += 2f;
                    }
                    break;
                case HEAVY_PULSAR_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.175f)) {
                        trailCount += 2f;
                    }
                    break;
                case CROCEA_MORS_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case PULSAR_BOMB_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.25f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_STANDARD_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_ARMOR_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_TARGETING_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                case MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID:
                    if (II_Util.isOnscreen(projectile.getLocation(), projectile.getVelocity().length() * 0.4f)) {
                        trailCount += 2f;
                    }
                    break;
                default:
                    break;
            }
        }

        float trailFPSRatio = Math.min(3f, (float) Math.max(1f, (trailCount / 30f)));

        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);
            String spec = proj.getProjectileSpecId();
            TrailData data;
            if (spec == null) {
                continue;
            }

            boolean enableAngleFade = true;
            switch (spec) {
                case PULSAR_PROJECTILE_ID:
                case HEAVY_PULSAR_PROJECTILE_ID:
                case PULSAR_BOMB_PROJECTILE_ID:
                    enableAngleFade = false;
                    break;

                default:
                    break;
            }

            switch (spec) {
                case ARCUS_PROJECTILE_ID:
                case SIEGE_MORTAR_PROJECTILE_ID:
                case SOLIS_PROJECTILE_ID:
                case TELUM_PROJECTILE_ID:
                case SLEDGE_GUN_PROJECTILE_ID:
                case SLEDGE_CANNON_PROJECTILE_ID:
                case PULSAR_PROJECTILE_ID:
                case HEAVY_PULSAR_PROJECTILE_ID:
                case CROCEA_MORS_PROJECTILE_ID:
                case PULSAR_BOMB_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ARMOR_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_TARGETING_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID:
                    data = trailMap.get(proj);
                    if (data == null) {
                        data = new TrailData();
                        data.id = MagicTrailPlugin.getUniqueID();

                        switch (spec) {
                            case SOLIS_PROJECTILE_ID:
                            case PULSAR_PROJECTILE_ID:
                            case HEAVY_PULSAR_PROJECTILE_ID:
                            case CROCEA_MORS_PROJECTILE_ID:
                            case PULSAR_BOMB_PROJECTILE_ID:
                            case MAGNA_FULMEN_STANDARD_PROJECTILE_ID:
                            case MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID:
                            case MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID:
                            case MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID:
                            case MAGNA_FULMEN_ENHANCED_TARGETING_PROJECTILE_ID:
                            case MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID:
                            case MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID:
                            case MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID:
                            case MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID:
                                data.id2 = MagicTrailPlugin.getUniqueID();
                                break;

                            default:
                                break;
                        }
                    }

                    trailMap.put(proj, data);
                    break;

                default:
                    continue;
            }

            if (!data.enabled) {
                continue;
            }

            float fade = 1f;
            if (proj.getBaseDamageAmount() > 0f) {
                fade = proj.getDamageAmount() / proj.getBaseDamageAmount();
            }

            if (enableAngleFade) {
                float velFacing = VectorUtils.getFacing(proj.getVelocity());
                float angleError = Math.abs(MathUtils.getShortestRotation(proj.getFacing(), velFacing));

                float angleFade = 1f - Math.min(Math.max(angleError - 45f, 0f) / 45f, 1f);
                fade *= angleFade;

                if (angleFade <= 0f) {
                    if (!data.cut) {
                        MagicTrailPlugin.cutTrailsOnEntity(proj);
                        data.cut = true;
                    }
                } else {
                    data.cut = false;
                }
            }

            if (fade <= 0f) {
                continue;
            }

            fade = Math.max(0f, Math.min(1f, fade));

            Vector2f projVel = new Vector2f(proj.getVelocity());
            Vector2f projBodyVel = VectorUtils.rotate(new Vector2f(projVel), -proj.getFacing());
            Vector2f projLateralBodyVel = new Vector2f(0f, projBodyVel.getY());
            Vector2f sidewaysVel = VectorUtils.rotate(new Vector2f(projLateralBodyVel), proj.getFacing());

            Vector2f spawnPosition = new Vector2f(proj.getLocation());
            if (proj.getSpawnType() == ProjectileSpawnType.BALLISTIC) {
                spawnPosition.x += sidewaysVel.x * amount * -1.05f;
                spawnPosition.y += sidewaysVel.y * amount * -1.05f;
            }

            switch (spec) {
                case ARCUS_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_smoothtrail"), /* sprite */
                                proj.getLocation(), /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                7f, /* startSize */
                                3f, /* endSize */
                                ARCUS_TRAIL_COLOR, /* startColor */
                                ARCUS_TRAIL_COLOR, /* endColor */
                                fade * 0.4f, /* opacity */
                                0.0f, /* inDuration */
                                0.0f, /* mainDuration */
                                0.2f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case SIEGE_MORTAR_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_smoothtrail"), /* sprite */
                                proj.getLocation(), /* position */
                                -100f, /* startSpeed */
                                -100f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                Math.max(fade * 10f, 4f), /* startSize */
                                4f, /* endSize */
                                SIEGE_MORTAR_TRAIL_COLOR, /* startColor */
                                SIEGE_MORTAR_TRAIL_COLOR, /* endColor */
                                fade * 0.7f, /* opacity */
                                0.0f, /* inDuration */
                                0.0f, /* mainDuration */
                                0.3f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case SOLIS_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 20f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                8f, /* startSize */
                                25f, /* endSize */
                                SOLIS_TRAIL_COLOR_START, /* startColor */
                                SOLIS_TRAIL_COLOR_END, /* endColor */
                                fade * 0.4f, /* opacity */
                                0.05f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.15f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                1000f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                60f, /* startSize */
                                30f, /* endSize */
                                SOLIS_TRAIL_COLOR2, /* startColor */
                                SOLIS_TRAIL_COLOR2, /* endColor */
                                fade * 0.3f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.1f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case TELUM_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 15f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                7f, /* startSize */
                                1f, /* endSize */
                                TELUM_TRAIL_COLOR_START, /* startColor */
                                TELUM_TRAIL_COLOR_END, /* endColor */
                                fade * 0.5f, /* opacity */
                                0f, /* inDuration */
                                0.1f, /* mainDuration */
                                0.5f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE_MINUS_SRC_ALPHA, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case SLEDGE_GUN_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float offset = 5f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                7f, /* startSize */
                                3.5f, /* endSize */
                                SLEDGE_GUN_TRAIL_COLOR_START, /* startColor */
                                SLEDGE_GUN_TRAIL_COLOR_END, /* endColor */
                                fade * 0.4f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.4f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE_MINUS_SRC_ALPHA, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case SLEDGE_CANNON_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                11f, /* startSize */
                                5.5f, /* endSize */
                                SLEDGE_CANNON_TRAIL_COLOR_START, /* startColor */
                                SLEDGE_CANNON_TRAIL_COLOR_END, /* endColor */
                                fade * 0.6f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.5f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE_MINUS_SRC_ALPHA, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case PULSAR_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                10f, /* startSize */
                                0f, /* endSize */
                                PULSAR_TRAIL_COLOR_START, /* startColor */
                                PULSAR_TRAIL_COLOR_END, /* endColor */
                                fade * 0.5f, /* opacity */
                                0f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.1f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                900f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                20f, /* startSize */
                                20f, /* endSize */
                                PULSAR_TRAIL_COLOR2, /* startColor */
                                PULSAR_TRAIL_COLOR2, /* endColor */
                                fade * 0.2f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.15f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case HEAVY_PULSAR_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                15f, /* startSize */
                                0f, /* endSize */
                                HEAVY_PULSAR_TRAIL_COLOR_START, /* startColor */
                                HEAVY_PULSAR_TRAIL_COLOR_END, /* endColor */
                                fade * 0.7f, /* opacity */
                                0f, /* inDuration */
                                0.075f, /* mainDuration */
                                0.1f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                1000f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                30f, /* startSize */
                                30f, /* endSize */
                                HEAVY_PULSAR_TRAIL_COLOR2, /* startColor */
                                HEAVY_PULSAR_TRAIL_COLOR2, /* endColor */
                                fade * 0.3f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.175f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case CROCEA_MORS_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 40f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                12f, /* startSize */
                                40f, /* endSize */
                                CROCEA_MORS_TRAIL_COLOR_START, /* startColor */
                                CROCEA_MORS_TRAIL_COLOR_END, /* endColor */
                                fade * 0.4f, /* opacity */
                                0.0f, /* inDuration */
                                0.1f, /* mainDuration */
                                0.3f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                600f, /* textureLoopLength */
                                1000f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_smoothtrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                80f, /* startSize */
                                80f, /* endSize */
                                CROCEA_MORS_TRAIL_COLOR2, /* startColor */
                                CROCEA_MORS_TRAIL_COLOR2, /* endColor */
                                fade * 0.3f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.25f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                600f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case PULSAR_BOMB_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount / trailFPSRatio);
                    if (data.interval.intervalElapsed()) {
                        float offset = 10f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                25f, /* startSize */
                                0f, /* endSize */
                                PULSAR_BOMB_TRAIL_COLOR_START, /* startColor */
                                PULSAR_BOMB_TRAIL_COLOR_END, /* endColor */
                                fade, /* opacity */
                                0f, /* inDuration */
                                0.1f, /* mainDuration */
                                0.15f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                400f, /* textureLoopLength */
                                400f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 20f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(80f, 120f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-30f, 30f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-150f, 150f), /* endAngularVelocity */
                                50f, /* startSize */
                                50f, /* endSize */
                                PULSAR_BOMB_TRAIL_COLOR2, /* startColor */
                                PULSAR_BOMB_TRAIL_COLOR2, /* endColor */
                                fade * 0.4f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.25f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                -1f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case MAGNA_FULMEN_STANDARD_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID:
                case MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 35f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        float speed = MathUtils.getRandomNumberInRange(0f, 250f);
                        float angularVelocity = MathUtils.getRandomNumberInRange(-20f, 20f);

                        Color MAGNA_FULMEN_TRAIL_COLOR_START;
                        Color MAGNA_FULMEN_TRAIL_COLOR_END;
                        Color MAGNA_FULMEN_TRAIL_COLOR2;
                        switch (spec) {
                            default:
                            case MAGNA_FULMEN_STANDARD_PROJECTILE_ID:
                                MAGNA_FULMEN_TRAIL_COLOR_START = MAGNA_FULMEN_STANDARD_TRAIL_COLOR_START;
                                MAGNA_FULMEN_TRAIL_COLOR_END = MAGNA_FULMEN_STANDARD_TRAIL_COLOR_END;
                                MAGNA_FULMEN_TRAIL_COLOR2 = MAGNA_FULMEN_STANDARD_TRAIL_COLOR2;
                                break;
                            case MAGNA_FULMEN_STANDARD_ARMOR_PROJECTILE_ID:
                                MAGNA_FULMEN_TRAIL_COLOR_START = MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR_START;
                                MAGNA_FULMEN_TRAIL_COLOR_END = MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR_END;
                                MAGNA_FULMEN_TRAIL_COLOR2 = MAGNA_FULMEN_STANDARD_ARMOR_TRAIL_COLOR2;
                                break;
                            case MAGNA_FULMEN_STANDARD_TARGETING_PROJECTILE_ID:
                                MAGNA_FULMEN_TRAIL_COLOR_START = MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR_START;
                                MAGNA_FULMEN_TRAIL_COLOR_END = MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR_END;
                                MAGNA_FULMEN_TRAIL_COLOR2 = MAGNA_FULMEN_STANDARD_TARGETING_TRAIL_COLOR2;
                                break;
                            case MAGNA_FULMEN_STANDARD_ELITE_PROJECTILE_ID:
                                MAGNA_FULMEN_TRAIL_COLOR_START = MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR_START;
                                MAGNA_FULMEN_TRAIL_COLOR_END = MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR_END;
                                MAGNA_FULMEN_TRAIL_COLOR2 = MAGNA_FULMEN_STANDARD_ELITE_TRAIL_COLOR2;
                                break;
                        }

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_fuzzytrail"), /* sprite */
                                spawnPosition, /* position */
                                speed, /* startSpeed */
                                speed, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                angularVelocity, /* startAngularVelocity */
                                angularVelocity, /* endAngularVelocity */
                                40f, /* startSize */
                                20f, /* endSize */
                                MAGNA_FULMEN_TRAIL_COLOR_START, /* startColor */
                                MAGNA_FULMEN_TRAIL_COLOR_END, /* endColor */
                                fade * 0.2f, /* opacity */
                                0.0f, /* inDuration */
                                0.05f, /* mainDuration */
                                0.05f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                1024f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                25f, /* startSize */
                                25f, /* endSize */
                                MAGNA_FULMEN_TRAIL_COLOR2, /* startColor */
                                MAGNA_FULMEN_TRAIL_COLOR2, /* endColor */
                                fade * 0.4f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.4f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case MAGNA_FULMEN_ENHANCED_ARMOR_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 15f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                10f, /* startSize */
                                10f, /* endSize */
                                MAGNA_FULMEN_ENHANCED_ARMOR_TRAIL_COLOR, /* startColor */
                                MAGNA_FULMEN_ENHANCED_ARMOR_TRAIL_COLOR, /* endColor */
                                fade * 0.3f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.3f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case MAGNA_FULMEN_ENHANCED_TARGETING_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 25f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                MathUtils.getRandomNumberInRange(0f, 250f), /* startSpeed */
                                MathUtils.getRandomNumberInRange(0f, 250f), /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                MathUtils.getRandomNumberInRange(-20f, 20f), /* startAngularVelocity */
                                MathUtils.getRandomNumberInRange(-20f, 20f), /* endAngularVelocity */
                                90f, /* startSize */
                                45f, /* endSize */
                                MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR_START, /* startColor */
                                MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR_END, /* endColor */
                                fade * 0.5f, /* opacity */
                                0.0f, /* inDuration */
                                0.1f, /* mainDuration */
                                0.1f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                1024f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                40f, /* startSize */
                                40f, /* endSize */
                                MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR2, /* startColor */
                                MAGNA_FULMEN_ENHANCED_TARGETING_TRAIL_COLOR2, /* endColor */
                                fade * 0.2f, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.8f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                case MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID:
                case MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID:
                    if (data.interval == null) {
                        data.interval = new IntervalUtil(SIXTY_FPS, SIXTY_FPS);
                    }
                    data.interval.advance(amount);
                    if (data.interval.intervalElapsed()) {
                        float offset = 35f;
                        Vector2f offsetPoint = new Vector2f((float) Math.cos(Math.toRadians(proj.getFacing())) * offset, (float) Math.sin(Math.toRadians(proj.getFacing())) * offset);
                        spawnPosition.x += offsetPoint.x;
                        spawnPosition.y += offsetPoint.y;

                        float speed = MathUtils.getRandomNumberInRange(0f, 250f);
                        float angularVelocity = MathUtils.getRandomNumberInRange(-20f, 20f);

                        float startSize;
                        float endSize;
                        float outDuration;
                        float opacity;
                        float size2;
                        float opacity2;
                        switch (spec) {
                            default:
                            case MAGNA_FULMEN_ENHANCED_ELITE1_PROJECTILE_ID:
                                speed *= 1.2f;
                                angularVelocity *= 1.15f;

                                startSize = 70f;
                                endSize = 35f;
                                outDuration = 0.06f;
                                opacity = 0.3f;
                                size2 = 30f;
                                opacity2 = 0.5f;
                                break;
                            case MAGNA_FULMEN_ENHANCED_ELITE2_PROJECTILE_ID:
                                speed *= 1.4f;
                                angularVelocity *= 1.3f;

                                startSize = 80f;
                                endSize = 40f;
                                outDuration = 0.07f;
                                opacity = 0.4f;
                                size2 = 35f;
                                opacity2 = 0.6f;
                                break;
                            case MAGNA_FULMEN_ENHANCED_ELITE3_PROJECTILE_ID:
                                speed *= 1.6f;
                                angularVelocity *= 1.45f;

                                startSize = 90f;
                                endSize = 45f;
                                outDuration = 0.08f;
                                opacity = 0.5f;
                                size2 = 40f;
                                opacity2 = 0.7f;
                                break;
                            case MAGNA_FULMEN_ENHANCED_ELITE4_PROJECTILE_ID:
                                speed *= 1.8f;
                                angularVelocity *= 1.6f;

                                startSize = 100f;
                                endSize = 50f;
                                outDuration = 0.09f;
                                opacity = 0.6f;
                                size2 = 45f;
                                opacity2 = 0.8f;
                                break;
                        }

                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_zappytrail"), /* sprite */
                                spawnPosition, /* position */
                                speed, /* startSpeed */
                                speed, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                angularVelocity, /* startAngularVelocity */
                                angularVelocity, /* endAngularVelocity */
                                startSize, /* startSize */
                                endSize, /* endSize */
                                MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR_START, /* startColor */
                                MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR_END, /* endColor */
                                fade * opacity, /* opacity */
                                0.0f, /* inDuration */
                                0.05f, /* mainDuration */
                                outDuration, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                1024f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                        MagicTrailPlugin.addTrailMemberAdvanced(
                                proj, /* linkedEntity */
                                data.id2, /* ID */
                                Global.getSettings().getSprite("ii_trails", "ii_cleantrail"), /* sprite */
                                spawnPosition, /* position */
                                0f, /* startSpeed */
                                0f, /* endSpeed */
                                proj.getFacing() - 180f, /* angle */
                                0f, /* startAngularVelocity */
                                0f, /* endAngularVelocity */
                                size2, /* startSize */
                                size2, /* endSize */
                                MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR2, /* startColor */
                                MAGNA_FULMEN_ENHANCED_ELITE_TRAIL_COLOR2, /* endColor */
                                fade * opacity2, /* opacity */
                                0f, /* inDuration */
                                0f, /* mainDuration */
                                0.5f, /* outDuration */
                                GL11.GL_SRC_ALPHA, /* blendModeSRC */
                                GL11.GL_ONE, /* blendModeDEST */
                                512f, /* textureLoopLength */
                                0f, /* textureScrollSpeed */
                                -1, /* textureOffset */
                                sidewaysVel, /* offsetVelocity */
                                null, /* advancedOptions */
                                CombatEngineLayers.CONTRAILS_LAYER, /* layerToRenderOn */
                                1f /* frameOffsetMult */
                        );
                    }
                    break;

                default:
                    break;
            }
        }

        /* Clean up */
        Iterator<DamagingProjectileAPI> iter = trailMap.keySet().iterator();
        while (iter.hasNext()) {
            DamagingProjectileAPI proj = iter.next();
            if (!engine.isEntityInPlay(proj)) {
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    public static void createIfNeeded() {
        if (!IIModPlugin.hasMagicLib) {
            return;
        }

        if (Global.getCombatEngine() != null) {
            if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
                Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
                Global.getCombatEngine().addPlugin(new II_Trails());
            }
        }
    }

    private static final class LocalData {

        final Map<DamagingProjectileAPI, TrailData> trailMap = new LinkedHashMap<>(100);
    }

    private static final class TrailData {

        Float id = null;
        Float id2 = null;
        IntervalUtil interval = null;
        boolean cut = false;
        boolean enabled = true;
    }
}
