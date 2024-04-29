package mmm;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import mmm.campaign.ForceAutoResolveInjector;
import mmm.missions.DefenseMission;
import mmm.missions.EscortMission;
import mmm.missions.OrbitalMissionBase;
import mmm.missions.VipMission;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Utils {
    public static final String MOD_ID = "MoreMilitaryMissions";
    public static final String LUNA_LIB_ID = "lunalib";
    public static final boolean DEBUG = MagicSettings.getBoolean(MOD_ID, "MmmDebug");
    private static final Logger log = Global.getLogger(Utils.class);
    static {
        if (DEBUG) {
            log.setLevel(Level.ALL);
        }
    }

    public static final boolean LUNA_LIB_ENABLED = Global.getSettings().getModManager().isModEnabled(LUNA_LIB_ID);

    // Returns the boolean setting called name in modSettings.json. If LunaLib is installed then return the same setting
    // from LunaLib instead, whose field is name prepended with mmm (so Name -> mmmName).
    public static boolean getBoolean(String name) {
        boolean value = MagicSettings.getBoolean(MOD_ID, name);
        if (LUNA_LIB_ENABLED) {
            Boolean luna_value = LunaSettings.getBoolean(MOD_ID, "mmm" + name);
            if (luna_value != null) return luna_value;
            log.error("LunaSettings.getBoolean failed for mmm" + name);
        }
        return value;
    }

    // Same as above but for integer.
    public static int getInteger(String name) {
        int value = MagicSettings.getInteger(MOD_ID, name);
        if (LUNA_LIB_ENABLED) {
            Integer luna_value = LunaSettings.getInt(MOD_ID, "mmm" + name);
            if (luna_value != null) return luna_value;
            log.error("LunaSettings.getInteger failed for mmm" + name);
        }
        return value;
    }

    // Same as above but for float.
    public static float getFloat(String name) {
        float value = MagicSettings.getFloat(MOD_ID, name);
        if (LUNA_LIB_ENABLED) {
            Float luna_value = LunaSettings.getFloat(MOD_ID, "mmm" + name);
            if (luna_value != null) return luna_value;
            log.error("LunaSettings.getFloat failed for mmm" + name);
        }
        return value;
    }

    // Load settings from LunaLib if the mod is enabled. Otherwise, load them from modSettings.json file. Only the
    // LunaLib settings are loaded here.
    public static void loadSettings() {
//        log.debug("loadSettings");
        DefenseMission.CREDIT_REWARD_PER_FP = getInteger("DmCreditRewardPerFp");
        DefenseMission.DIFFICULTY_GROWTH = getFloat("DmDifficultyGrowth");
        DefenseMission.ENEMY_IGNORED_BY_OTHER_FLEETS = getBoolean( "DmEnemyIgnoredByOtherFleets");
        DefenseMission.MIN_CREDIT_REWARD = getInteger("DmMinCreditReward");
        DefenseMission.USE_HIDDEN_FACTIONS = getBoolean("DmUseHiddenFactions");
        EscortMission.PIRATE_SPAWN_CHANCE = getFloat("EmPirateSpawnChance");
        EscortMission.CONVOY_STR_RATIO = getFloat("EmConvoyStrengthRatio");
        ForceAutoResolveInjector.MIN_STRENGTH_RATIO = getFloat("ForceAutoResolveMinStrengthRatio");
        MoreMilitaryMissionsModPlugin.FORCE_AUTO_RESOLVE_ENABLED = getBoolean("ForceAutoResolveEnabled");
        MoreMilitaryMissionsModPlugin.ESCORT_MISSION_ADD_ALL_INTEL = getBoolean("EmAddAllIntel");
        OrbitalMissionBase.STATION_EFFECTIVE_FP_RATIO = getFloat("DmStationEffectiveFpRatio");
        VipMission.MISSION_PROB = getFloat("VipMissionProb");
    }

    public static class MmmLunaSettingsListener implements LunaSettingsListener {
        @Override
        public void settingsChanged(String s) {
//            log.debug("settingsChanged");
            loadSettings();
            MoreMilitaryMissionsModPlugin.addRemoveScripts();
        }
    }

    // Reflection utilities; the important thing is that java.lang.reflect.Method/Field are never imported in order
    // to get around SecurityException.
    public static class ReflectionHandles {
        private static ReflectionHandles methodHandles = null;
        private static ReflectionHandles fieldHandles = null;

        // Must be class of java.lang.reflect.Method or java.lang.reflect.Field
        public Class<?> clazz;
        // Must be method handle to Method.setAccessibleHandle or Field.setAccessibleHandle
        public MethodHandle setAccessibleHandle;
        // Must be method handle to Method.invoke or Field.get
        public MethodHandle handle;

        private ReflectionHandles(Class<?> clazz, MethodHandle setAccessibleHandle, MethodHandle handle) {
            this.clazz = clazz;
            this.setAccessibleHandle = setAccessibleHandle;
            this.handle = handle;
        }

        public static ReflectionHandles get(boolean isMethod)
                throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
            if (methodHandles == null || fieldHandles == null) {
                // This is used instead of java.lang.reflect.Method/Field to avoid SecurityException.
                Class<?> methodClass = Class.forName("java.lang.reflect.Method", false,
                        Class.class.getClassLoader());
                Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false,
                        Class.class.getClassLoader());

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle methodSetAccessibleHandle = lookup.findVirtual(methodClass, "setAccessible",
                        MethodType.methodType(Void.TYPE, Boolean.TYPE));
                MethodHandle fieldSetAccessibleHandle = lookup.findVirtual(fieldClass, "setAccessible",
                        MethodType.methodType(Void.TYPE, Boolean.TYPE));
                MethodHandle invokeHandle = lookup.findVirtual(methodClass, "invoke",
                        MethodType.methodType(Object.class, Object.class, Object[].class));
                MethodHandle getHandle = lookup.findVirtual(fieldClass, "get",
                        MethodType.methodType(Object.class, Object.class));

                methodHandles = new ReflectionHandles(methodClass, methodSetAccessibleHandle, invokeHandle);
                fieldHandles = new ReflectionHandles(fieldClass, fieldSetAccessibleHandle, getHandle);
            }

            return isMethod ? methodHandles : fieldHandles;
        }
    }

    // Uses reflection to call the methodName method of object in clazz class, with the provided argument and
    // associated parameter type. Here clazz must be the class where methodName is declared. Returns null on error.
    public static Object reflectionInvoke(Class<?> clazz, Object object, String methodName, Class<?> parameterType,
                                          Object arg) {
        try {
            // Must be a java.lang.reflect.Method
            Object method = clazz.getDeclaredMethod(methodName, parameterType);
            ReflectionHandles handles = ReflectionHandles.get(true);
            handles.setAccessibleHandle.invoke(method, true);
            return handles.handle.invoke(method, object, arg);
        } catch (Throwable e) {
            log.error("invoke: ", e);
        }
        return null;
    }

    // Uses reflection to access the fieldName field of object in clazz class. Here clazz must be the class where
    // fieldName is declared. Returns null on error.
    public static Object reflectionGet(Class<?> clazz, Object object, String fieldName) {
        try {
            // Must be a java.lang.reflect.Field
            Object field = clazz.getDeclaredField(fieldName);
            ReflectionHandles handles = ReflectionHandles.get(false);
            handles.setAccessibleHandle.invoke(field, true);
            return handles.handle.invoke(field, object);
        } catch (Throwable e) {
            log.error("invoke: ", e);
        }
        return null;
    }
}
