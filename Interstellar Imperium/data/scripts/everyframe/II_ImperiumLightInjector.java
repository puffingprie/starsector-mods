package data.scripts.everyframe;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.IIModPlugin;
import data.scripts.hullmods.II_BasePackage;
import data.scripts.shipsystems.II_ImpulseBoosterStats;
import data.scripts.shipsystems.II_LuxFinisStats;
import data.scripts.shipsystems.II_OverdriveStats;
import data.scripts.util.II_Util;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

public class II_ImperiumLightInjector extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "II_LightInjector";

    private static final Vector2f ZERO = new Vector2f();

    private CombatEngineAPI engine;
    private boolean activated = false;
    private final IntervalUtil inactiveInterval = new IntervalUtil(1f, 2f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }
        if (!IIModPlugin.hasGraphicsLib) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (!activated) {
            inactiveInterval.advance(amount);
            if (!inactiveInterval.intervalElapsed()) {
                return;
            }
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, Object> lights = localData.lights;

        List<ShipAPI> ships = engine.getShips();
        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship.isHulk()) {
                continue;
            }

            float shipRadius = II_Util.effectiveRadius(ship);

            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                String id = system.getId();
                switch (id) {
                    case "ii_impulsebooster":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            float num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = (StandardLight) lights.get(ship);

                                light.setLocation(location);

                                if (!system.isOn()) {
                                    if (!light.isFadingOut()) {
                                        float intensity = (float) Math.sqrt(shipRadius) / 20f;
                                        float size = intensity * 300f;
                                        light.setIntensity(intensity);
                                        light.setSize(size);
                                        light.fadeOut(0.75f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 30f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);

                                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                    light.setColor(1f, 0.7f, 0.25f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                    light.setColor(0.65f, 0.75f, 1f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                    light.setColor(0.7f, 0.25f, 1f);
                                } else {
                                    light.setColor(1f, 0.25f, 0.25f);
                                }

                                light.setSpecularMult(3f);
                                light.fadeIn(II_ImpulseBoosterStats.IN_OVERRIDE.get(ship.getHullSize()));

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "ii_celeritydrive":
                        if (system.isActive()) {
                            Vector2f location = ship.getLocation();

                            if (lights.containsKey(ship)) {
                                StandardLight light = (StandardLight) lights.get(ship);

                                light.setLocation(location);

                                if (system.isActive() && !system.isOn()) {
                                    if (!light.isFadingOut()) {
                                        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                            light.fadeOut(2f);
                                        } else {
                                            light.fadeOut(3f);
                                        }
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);

                                light.setIntensity(0.5f);
                                light.setSize(shipRadius * 2f);

                                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                    light.setColor(1f, 1f, 0.05f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                    light.setColor(0.05f, 0.4f, 1f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                    light.setColor(0.55f, 0.05f, 1f);
                                } else {
                                    light.setColor(1f, 0.6f, 0.05f);
                                }

                                light.setSpecularMult(6f);
                                if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                    light.fadeIn(1.5f);
                                } else {
                                    light.fadeIn(3f);
                                }

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "ii_microforge":
                        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                            if (system.isActive()) {
                                Vector2f location = ship.getLocation();

                                if (lights.containsKey(ship)) {
                                    StandardLight light = (StandardLight) lights.get(ship);

                                    light.setLocation(location);

                                    if (system.isActive() && !system.isOn()) {
                                        if (!light.isFadingOut()) {
                                            light.fadeIn(0f);
                                            light.setIntensity(1f);
                                            light.fadeOut(1f);
                                        }
                                    }
                                } else {
                                    StandardLight light = new StandardLight(location, ZERO, ZERO, null);

                                    light.setIntensity(0.5f);
                                    light.setSize(shipRadius * 4f);
                                    light.setColor(0.88f, 0.1f, 1f);
                                    light.fadeIn(1f);

                                    lights.put(ship, light);
                                    LightShader.addLight(light);
                                }
                            }
                        }
                        activated = true;
                        break;
                    case "ii_turbofeeder":
                        if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                            if (system.isActive()) {
                                Vector2f location = null;
                                if (ship.getEngineController() == null) {
                                    break;
                                }
                                List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                                float num = 0;
                                int enginesSize = engines.size();
                                for (int j = 0; j < enginesSize; j++) {
                                    ShipEngineAPI eng = engines.get(j);
                                    if (eng.isActive() && !eng.isDisabled()) {
                                        num++;
                                        if (location == null) {
                                            location = new Vector2f(eng.getLocation());
                                        } else {
                                            Vector2f.add(location, eng.getLocation(), location);
                                        }
                                    }
                                }
                                if (location == null) {
                                    break;
                                }

                                location.scale(1f / num);

                                if (lights.containsKey(ship)) {
                                    StandardLight light = (StandardLight) lights.get(ship);

                                    light.setLocation(location);

                                    if (system.isActive() && !system.isOn()) {
                                        if (!light.isFadingOut()) {
                                            light.fadeOut(1f);
                                        }
                                    }
                                } else {
                                    StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                    float intensity = (float) Math.sqrt(shipRadius) / 25f;
                                    float size = intensity * 200f;

                                    light.setIntensity(intensity);
                                    light.setSize(size);
                                    light.setColor(0.78f, 0.4f, 1f);
                                    light.fadeIn(1f);

                                    lights.put(ship, light);
                                    LightShader.addLight(light);
                                }
                            }
                        }
                        activated = true;
                        break;
                    case "ii_overdrive":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            float num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = (StandardLight) lights.get(ship);

                                if (!light.isFadingIn() && !light.isFadingOut()) {
                                    float intensity = (float) Math.sqrt(shipRadius) / 25f;
                                    intensity *= (float) Math.sqrt(II_OverdriveStats.getOverlevel(ship));
                                    float size = intensity * 250f;

                                    light.setIntensity(intensity);
                                    light.setSize(size);
                                }
                                light.setLocation(location);

                                if (system.isActive() && !system.isOn()) {
                                    if (!light.isFadingOut()) {
                                        if (ship.getFluxTracker().isOverloaded()) {
                                            light.fadeOut(0f);
                                        } else {
                                            light.fadeOut(0.5f);
                                        }
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 25f;
                                float size = intensity * 250f;

                                light.setIntensity(intensity);
                                light.setSize(size);

                                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                    light.setColor(0.8f, 0.6f, 0.4f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                    light.setColor(0.2f, 0.85f, 1f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                    light.setColor(0.55f, 0.2f, 1f);
                                } else {
                                    light.setColor(1f, 0.5f, 0.2f);
                                }

                                light.setSpecularMult(3f);
                                light.fadeIn(0.25f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "ii_luxfinis":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            float num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = (StandardLight) lights.get(ship);

                                if (!light.isFadingIn() && !light.isFadingOut()) {
                                    float intensity = (float) Math.sqrt(shipRadius) / 30f;
                                    intensity *= (float) Math.sqrt(II_LuxFinisStats.getOverlevel(ship));
                                    float size = intensity * 250f;

                                    light.setIntensity(intensity);
                                    light.setSize(size);
                                }
                                light.setLocation(location);

                                if (system.isActive() && !system.isOn()) {
                                    if (!light.isFadingOut()) {
                                        if (ship.getFluxTracker().isOverloaded()) {
                                            light.fadeOut(0f);
                                        } else {
                                            light.fadeOut(1f);
                                        }
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 30f;
                                float size = intensity * 250f;

                                light.setIntensity(intensity);
                                light.setSize(size);

                                if (ship.getVariant().hasHullMod(II_BasePackage.ARMOR_PACKAGE)) {
                                    light.setColor(1f, 0.75f, 0.25f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.TARGETING_PACKAGE)) {
                                    light.setColor(0.05f, 0.55f, 1f);
                                } else if (ship.getVariant().hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
                                    light.setColor(0.7f, 0.05f, 1f);
                                } else {
                                    light.setColor(1f, 0.35f, 0.25f);
                                }

                                light.setSpecularMult(5f);
                                light.fadeIn(1f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    default:
                        break;
                }
            }
        }

        Iterator<Map.Entry<ShipAPI, Object>> iter = lights.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, Object> entry = iter.next();
            ShipAPI ship = entry.getKey();

            if ((ship.getSystem() != null && !ship.getSystem().isActive()) || !ship.isAlive()) {
                StandardLight light = (StandardLight) entry.getValue();

                light.unattach();
                light.fadeOut(0);
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        engine.getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class LocalData {

        final Map<ShipAPI, Object> lights = new LinkedHashMap<>(50);
    }
}
