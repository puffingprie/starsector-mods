package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
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
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class LE_Util {
    //Code Courtesy of the Titan code from Interstellar Imperium by Dark Revenant
    public static boolean OFFSCREEN = false;
    public static final float OFFSCREEN_GRACE_CONSTANT = 500f;
    public static final float OFFSCREEN_GRACE_FACTOR = 2f;

    /* LazyLib 2.4b revert */
    public static List<DamagingProjectileAPI> getProjectilesWithinRange(Vector2f location, float range) {
        List<DamagingProjectileAPI> projectiles = new ArrayList<>();

        for (DamagingProjectileAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if (tmp instanceof MissileAPI) {
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

    private static Color interpolateColor(Color old, Color dest, float progress) {
        final float clampedProgress = Math.max(0f, Math.min(1f, progress));
        final float antiProgress = 1f - clampedProgress;
        final float[] ccOld = old.getComponents(null), ccNew = dest.getComponents(null);
        return new Color(clamp255((int) ((ccOld[0] * antiProgress) + (ccNew[0] * clampedProgress))),
                clamp255((int) ((ccOld[1] * antiProgress) + (ccNew[1] * clampedProgress))),
                clamp255((int) ((ccOld[2] * antiProgress) + (ccNew[2] * clampedProgress))),
                clamp255((int) ((ccOld[3] * antiProgress) + (ccNew[3] * clampedProgress))));
    }

    public static float getActualDistance(Vector2f from, CombatEntityAPI target, boolean considerShield) {
        if (considerShield && (target instanceof ShipAPI)) {
            ShipAPI ship = (ShipAPI) target;
            ShieldAPI shield = ship.getShield();
            if (shield != null && shield.isOn() && shield.isWithinArc(from)) {
                return MathUtils.getDistance(from, shield.getLocation()) - shield.getRadius();
            }
        }
        return MathUtils.getDistance(from, target.getLocation()) - Misc.getTargetingRadius(from, target, false);
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
}
