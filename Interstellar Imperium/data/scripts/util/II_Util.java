package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class II_Util {

    public static boolean OFFSCREEN = false;
    public static final float OFFSCREEN_GRACE_CONSTANT = 500f;
    public static final float OFFSCREEN_GRACE_FACTOR = 2f;

    public static final Set<String> FACTION_WHITELIST = new HashSet<>();

    static {
        FACTION_WHITELIST.add(Factions.DIKTAT);
        FACTION_WHITELIST.add(Factions.HEGEMONY);
        FACTION_WHITELIST.add(Factions.INDEPENDENT);
        FACTION_WHITELIST.add(Factions.LIONS_GUARD);
        FACTION_WHITELIST.add(Factions.LUDDIC_CHURCH);
        FACTION_WHITELIST.add(Factions.LUDDIC_PATH);
        FACTION_WHITELIST.add(Factions.PERSEAN);
        FACTION_WHITELIST.add(Factions.PIRATES);
        FACTION_WHITELIST.add(Factions.TRITACHYON);
        FACTION_WHITELIST.add("blackrock_driveyards");
        FACTION_WHITELIST.add("dassault_mikoyan");
        FACTION_WHITELIST.add("diableavionics");
        FACTION_WHITELIST.add("ii_imperial_guard");
        FACTION_WHITELIST.add("interstellarimperium");
        FACTION_WHITELIST.add("pack");
        FACTION_WHITELIST.add("junk_pirates");
        FACTION_WHITELIST.add("syndicate_asp");
        FACTION_WHITELIST.add("ORA");
        FACTION_WHITELIST.add("SCY");
        FACTION_WHITELIST.add("shadow_industry");
        FACTION_WHITELIST.add("tiandong");
        FACTION_WHITELIST.add("Coalition");
        FACTION_WHITELIST.add("mayasura");
        FACTION_WHITELIST.add("kadur_remnant");
        FACTION_WHITELIST.add("metelson");
    }

    public static final String[] NUM_NAMES = {
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen"
    };

    private static final Color BORDER;
    private static final float COLOR_UPDATE_DURATION = 0.2f; // How fast the widget will switch between different colors

    // Used to smoothly interpolate between status colors
    private static Color lastColor;
    private static float lastUpdate;

    static {
        BORDER = Global.getSettings().getColor("textFriendColor");
    }

    public static void filterObscuredTargets(CombatEntityAPI primaryTarget, Vector2f originPoint, List nearbyTargets,
            boolean filterModules, boolean filterShielded, boolean filterBlocked) {
        Iterator<CombatEntityAPI> iter = nearbyTargets.iterator();
        while (iter.hasNext()) {
            CombatEntityAPI nearbyTarget = iter.next();
            if (nearbyTarget.getCollisionClass() == CollisionClass.NONE) {
                iter.remove();
                continue;
            }

            if (filterModules && (nearbyTarget instanceof ShipAPI)) {
                ShipAPI ship = (ShipAPI) nearbyTarget;
                if (ship.getParentStation() == primaryTarget) {
                    iter.remove();
                    continue;
                }
            }

            Vector2f nearestPoint = getNearestPointForDamage(originPoint, nearbyTarget);

            boolean remove = false;
            for (Object otherTarget : nearbyTargets) {
                if (!(otherTarget instanceof CombatEntityAPI)) {
                    continue;
                }
                CombatEntityAPI otherEntity = (CombatEntityAPI) otherTarget;

                if ((nearbyTarget.getOwner() != otherEntity.getOwner()) || (otherEntity == nearbyTarget)) {
                    continue;
                }

                if (filterShielded && (otherEntity.getShield() != null) && (otherEntity.getShield().isWithinArc(nearestPoint) && otherEntity.getShield().isOn()
                        && MathUtils.isWithinRange(nearestPoint, otherEntity.getShield().getLocation(), otherEntity.getShield().getRadius()))) {
                    remove = true;
                    break;
                }

                if (filterBlocked && CollisionUtils.getCollides(originPoint, nearestPoint, otherEntity.getLocation(), otherEntity.getCollisionRadius())) {
                    if (CollisionUtils.getCollisionPoint(nearestPoint, originPoint, otherEntity) != null) {
                        remove = true;
                        break;
                    }
                }
            }

            if (remove) {
                iter.remove();
            }
        }
    }

    public static Vector2f getNearestPointForDamage(Vector2f source, CombatEntityAPI entity) {
        if (entity instanceof DamagingProjectileAPI) {
            return entity.getLocation();
        }

        return CollisionUtils.getNearestPointOnBounds(source, entity);
    }

    /* LazyLib 2.4b revert */
    public static List<DamagingProjectileAPI> getProjectilesWithinRange(Vector2f location, float range) {
        List<DamagingProjectileAPI> projectiles = new ArrayList<>();

        for (DamagingProjectileAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if ((tmp instanceof MissileAPI) || (tmp == null)) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                projectiles.add(tmp);
            }
        }

        return projectiles;
    }

    /* LazyLib 2.4b revert */
    public static List<MissileAPI> getMissilesWithinRange(Vector2f location, float range) {
        List<MissileAPI> missiles = new ArrayList<>();

        for (MissileAPI tmp : Global.getCombatEngine().getMissiles()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                missiles.add(tmp);
            }
        }

        return missiles;
    }

    /* LazyLib 2.4b revert */
    public static List<ShipAPI> getShipsWithinRange(Vector2f location, float range) {
        List<ShipAPI> ships = new ArrayList<>();

        for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
            if (tmp.isShuttlePod()) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp, location, range)) {
                ships.add(tmp);
            }
        }

        return ships;
    }

    /* LazyLib 2.4b revert */
    public static List<CombatEntityAPI> getAsteroidsWithinRange(Vector2f location, float range) {
        List<CombatEntityAPI> asteroids = new ArrayList<>();

        for (CombatEntityAPI tmp : Global.getCombatEngine().getAsteroids()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                asteroids.add(tmp);
            }
        }

        return asteroids;
    }

    /* LazyLib 2.4b revert */
    public static List<BattleObjectiveAPI> getObjectivesWithinRange(Vector2f location,
            float range) {
        List<BattleObjectiveAPI> objectives = new ArrayList<>();

        for (BattleObjectiveAPI tmp : Global.getCombatEngine().getObjectives()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                objectives.add(tmp);
            }
        }

        return objectives;
    }

    /* LazyLib 2.4b revert */
    public static List<CombatEntityAPI> getEntitiesWithinRange(Vector2f location, float range) {
        List<CombatEntityAPI> entities = new ArrayList<>();

        for (CombatEntityAPI tmp : Global.getCombatEngine().getShips()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        // This also includes missiles
        for (CombatEntityAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        for (CombatEntityAPI tmp : Global.getCombatEngine().getAsteroids()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        return entities;
    }

    public static void applyForce(CombatEntityAPI target, Vector2f dir, float force) {
        if (target instanceof ShipAPI) {
            ShipAPI root = II_Multi.getRoot((ShipAPI) target);
            float forceRatio = root.getMass() / root.getMassWithModules();
            CombatUtils.applyForce(root, dir, force * forceRatio);
        } else {
            CombatUtils.applyForce(target, dir, force);
        }
    }

    public static String getMoreAggressivePersonality(FleetMemberAPI member, ShipAPI ship) {
        if (ship == null) {
            return Personalities.AGGRESSIVE;
        }

        boolean player = false;
        if ((member != null) && (member.getFleetData() != null) && (member.getFleetData().getFleet() != null)
                && member.getFleetData().getFleet().isPlayerFleet()) {
            player = true;
        }

        String personality = null;
        if (member != null) {
            if (member.getCaptain() != null) {
                /* Skip the player's ship or any player officer ships */
                if (player && (!member.getCaptain().isDefault() || member.getCaptain().isPlayer())) {
                    return null;
                }

                personality = member.getCaptain().getPersonalityAPI().getId();
            }
        } else {
            if (ship.getCaptain() != null) {
                personality = ship.getCaptain().getPersonalityAPI().getId();
            }
        }

        if ((ship.getShipAI() != null) && (ship.getShipAI().getConfig() != null)) {
            if (ship.getShipAI().getConfig().personalityOverride != null) {
                personality = ship.getShipAI().getConfig().personalityOverride;
            }
        }

        String newPersonality;
        if (personality == null) {
            newPersonality = Personalities.AGGRESSIVE;
        } else {
            switch (personality) {
                case Personalities.TIMID:
                    newPersonality = Personalities.CAUTIOUS;
                    break;
                case Personalities.CAUTIOUS:
                    newPersonality = Personalities.STEADY;
                    break;
                default:
                case Personalities.STEADY:
                    newPersonality = Personalities.AGGRESSIVE;
                    break;
                case Personalities.AGGRESSIVE:
                case Personalities.RECKLESS:
                    newPersonality = Personalities.RECKLESS;
                    break;
            }
        }

        return newPersonality;
    }

    public static String getLessAggressivePersonality(FleetMemberAPI member, ShipAPI ship) {
        if (ship == null) {
            return Personalities.CAUTIOUS;
        }

        boolean player = false;
        if ((member != null) && (member.getFleetData() != null) && (member.getFleetData().getFleet() != null)
                && member.getFleetData().getFleet().isPlayerFleet()) {
            player = true;
        }

        String personality = null;
        if (member != null) {
            if (member.getCaptain() != null) {
                /* Skip the player's ship or any player officer ships */
                if (player && (!member.getCaptain().isDefault() || member.getCaptain().isPlayer())) {
                    return null;
                }

                personality = member.getCaptain().getPersonalityAPI().getId();
            }
        } else {
            if (ship.getCaptain() != null) {
                personality = ship.getCaptain().getPersonalityAPI().getId();
            }
        }

        if ((ship.getShipAI() != null) && (ship.getShipAI().getConfig() != null)) {
            if (ship.getShipAI().getConfig().personalityOverride != null) {
                personality = ship.getShipAI().getConfig().personalityOverride;
            }
        }

        String newPersonality;
        if (personality == null) {
            newPersonality = Personalities.CAUTIOUS;
        } else {
            switch (personality) {
                case Personalities.TIMID:
                case Personalities.CAUTIOUS:
                    newPersonality = Personalities.TIMID;
                    break;
                default:
                case Personalities.STEADY:
                    newPersonality = Personalities.CAUTIOUS;
                    break;
                case Personalities.AGGRESSIVE:
                    newPersonality = Personalities.STEADY;
                    break;
                case Personalities.RECKLESS:
                    newPersonality = Personalities.AGGRESSIVE;
                    break;
            }
        }

        return newPersonality;
    }

    public static void drawSystemUI(ShipAPI ship, Color intendedColor, float fill) {
        if (!ship.isAlive()) {
            return;
        }
        if (Global.getCombatEngine().getCombatUI().isShowingCommandUI()) {
            return;
        }
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());
        final float boxWidth = 17f;
        final float boxHeight = 7f;

        // Used to properly interpolate between colors
        final CombatEngineAPI engine = Global.getCombatEngine();
        final float elapsed = engine.getTotalElapsedTime(true);
        float alpha = 1;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            alpha = 0.28f;
        }

        // Calculate what color to use
        Color actualColor;
        if (lastUpdate > elapsed) {
            lastUpdate = elapsed;
        }
        float progress = (elapsed - lastUpdate) / COLOR_UPDATE_DURATION;
        if (lastColor == null || lastColor == intendedColor || progress > 1f) {
            lastColor = intendedColor;
            actualColor = lastColor;
            lastUpdate = elapsed;
        } else {
            actualColor = interpolateColor(lastColor, intendedColor, progress);
        }

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        final Vector2f boxLoc = Vector2f.add(new Vector2f(497f, 80f),
                getUIElementOffset(ship, ship.getVariant()), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(498f, 79f),
                getUIElementOffset(ship, ship.getVariant()), null);

        // Render the drop shadow
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxWidth + 1f, shadowLoc.y - 1f);
        GL11.glVertex2f(shadowLoc.x + boxWidth + 1f, shadowLoc.y + boxHeight + 1f);
        GL11.glVertex2f(shadowLoc.x - 1f, shadowLoc.y + boxHeight + 1f);

        // Render the border transparency fix
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y + boxHeight + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxHeight + 1f);

        // Render the border
        GL11.glColor4f(BORDER.getRed() / 255f, BORDER.getGreen() / 255f, BORDER.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y - 1f);
        GL11.glVertex2f(boxLoc.x + boxWidth + 1f, boxLoc.y + boxHeight + 1f);
        GL11.glVertex2f(boxLoc.x - 1f, boxLoc.y + boxHeight + 1f);

        // Render the background
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
        GL11.glEnd();

        // Render the fill element
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glColor4f(actualColor.getRed() / 255f, actualColor.getGreen() / 255f, actualColor.getBlue() / 255f,
                alpha * (actualColor.getAlpha() / 255f)
                * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth * fill, boxLoc.y);
        GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth * fill, boxLoc.y + boxHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static float lerp(float x, float y, float alpha) {
        return (1f - alpha) * x + alpha * y;
    }

    public static float effectiveRadius(ShipAPI ship) {
        if (ship.getSpriteAPI() == null || ship.isPiece()) {
            return ship.getCollisionRadius();
        } else {
            float fudgeFactor = 1.5f;
            return ((ship.getSpriteAPI().getWidth() / 2f) + (ship.getSpriteAPI().getHeight() / 2f)) * 0.5f * fudgeFactor;
        }
    }

    private static Vector2f getUIElementOffset(ShipAPI ship, ShipVariantAPI variant) {
        int numEntries = 0;
        final List<WeaponGroupSpec> weaponGroups = variant.getWeaponGroups();
        final List<WeaponAPI> usableWeapons = ship.getUsableWeapons();
        for (WeaponGroupSpec group : weaponGroups) {
            final Set<String> uniqueWeapons = new HashSet<>(group.getSlots().size());
            for (String slot : group.getSlots()) {
                boolean isUsable = false;
                for (WeaponAPI weapon : usableWeapons) {
                    if (weapon.getSlot().getId().contentEquals(slot)) {
                        isUsable = true;
                        break;
                    }
                }

                if (!isUsable) {
                    continue;
                }

                String id = variant.getWeaponId(slot);
                if (id != null) {
                    uniqueWeapons.add(id);
                }
            }

            numEntries += uniqueWeapons.size();
        }

        if (variant.getFittedWings().isEmpty()) {
            if (numEntries < 2) {
                return new Vector2f(0f, 0f);
            }

            return new Vector2f(10f + ((numEntries - 2) * 13f), 18f + ((numEntries - 2) * 26f));
        } else {
            if (numEntries < 2) {
                return new Vector2f(29f, 58f);
            }

            return new Vector2f(39f + ((numEntries - 2) * 13f), 76f + ((numEntries - 2) * 26f));
        }
    }

    public static Color interpolateColor(Color old, Color dest, float progress) {
        final float clampedProgress = Math.max(0f, Math.min(1f, progress));
        final float antiProgress = 1f - clampedProgress;
        final float[] ccOld = old.getComponents(null), ccNew = dest.getComponents(null);
        return new Color(clamp255((int) ((ccOld[0] * antiProgress) + (ccNew[0] * clampedProgress))),
                clamp255((int) ((ccOld[1] * antiProgress) + (ccNew[1] * clampedProgress))),
                clamp255((int) ((ccOld[2] * antiProgress) + (ccNew[2] * clampedProgress))),
                clamp255((int) ((ccOld[3] * antiProgress) + (ccNew[3] * clampedProgress))));
    }

    public static Color interpolateColor255(Color old, Color dest, float progress) {
        final float clampedProgress = Math.max(0f, Math.min(1f, progress));
        final float antiProgress = 1f - clampedProgress;
        final float[] ccOld = old.getComponents(null), ccNew = dest.getComponents(null);
        return new Color(clamp255((int) ((ccOld[0] * 255f * antiProgress) + (ccNew[0] * 255f * clampedProgress))),
                clamp255((int) ((ccOld[1] * 255f * antiProgress) + (ccNew[1] * 255f * clampedProgress))),
                clamp255((int) ((ccOld[2] * 255f * antiProgress) + (ccNew[2] * 255f * clampedProgress))),
                clamp255((int) ((ccOld[3] * 255f * antiProgress) + (ccNew[3] * 255f * clampedProgress))));
    }

    public static float getActualDistance(Vector2f from, CombatEntityAPI target, boolean considerShield) {
        if (considerShield && (target instanceof ShipAPI)) {
            ShipAPI ship = (ShipAPI) target;
            ShieldAPI shield = ship.getShield();
            if (shield != null && shield.isOn() && shield.isWithinArc(from)) {
                return MathUtils.getDistance(from, shield.getLocation()) - shield.getRadius();
            }
        }
        if (target instanceof ShipAPI) {
            return MathUtils.getDistance(target.getLocation(), from) - Misc.getTargetingRadius(from, target, false);
        } else {
            return MathUtils.getDistance(target, from);
        }
    }

    public static Collection<String> getBuiltInHullMods(ShipAPI ship) {
        ShipVariantAPI tmp = ship.getVariant().clone();
        tmp.clearHullMods();
        return tmp.getHullMods();
    }

    public static String getNonDHullId(ShipHullSpecAPI spec) {
        if (spec == null) {
            return null;
        }
        if (spec.getDParentHullId() != null && !spec.getDParentHullId().isEmpty()) {
            return spec.getDParentHullId();
        } else {
            return spec.getHullId();
        }
    }

    public static boolean isOnscreen(Vector2f point, float radius) {
        return OFFSCREEN || ShaderLib.isOnScreen(point, radius * OFFSCREEN_GRACE_FACTOR + OFFSCREEN_GRACE_CONSTANT);
    }

    public static boolean isWithinEmpRange(Vector2f loc, float dist, ShipAPI ship) {
        float distSq = dist * dist;
        if (ship.getShield() != null && ship.getShield().isOn() && ship.getShield().isWithinArc(loc)) {
            if (MathUtils.getDistance(ship.getLocation(), loc) - ship.getShield().getRadius() <= dist) {
                return true;
            }
        }
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (!weapon.getSlot().isHidden() && weapon.getSlot().getWeaponType() != WeaponType.DECORATIVE
                    && weapon.getSlot().getWeaponType() != WeaponType.LAUNCH_BAY
                    && weapon.getSlot().getWeaponType() != WeaponType.SYSTEM) {
                if (MathUtils.getDistanceSquared(weapon.getLocation(), loc) <= distSq) {
                    return true;
                }
            }
        }
        for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
            if (!engine.isSystemActivated()) {
                if (MathUtils.getDistanceSquared(engine.getLocation(), loc) <= distSq) {
                    return true;
                }
            }
        }
        return false;
    }
}
