package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;
import java.util.ArrayList;
import java.util.List;

public class II_Multi {

    public static ShipAPI getRoot(ShipAPI ship) {
        if (isMultiShip(ship)) {
            ShipAPI root = ship;
            while (root.getParentStation() != null) {
                root = root.getParentStation();
            }
            return root;
        } else {
            return ship;
        }
    }

    public static boolean isMultiShip(ShipAPI ship) {
        return ship.getParentStation() != null || ship.isShipWithModules();
    }

    public static boolean isRoot(ShipAPI ship) {
        return getRoot(ship) == ship;
    }

    public static List<ShipAPI> getChildren(ShipAPI ship) {
        if (!ship.isShipWithModules()) {
            return new ArrayList<>(0);
        } else {
            List<ShipAPI> children = ship.getChildModulesCopy();
            List<ShipAPI> allChildren = new ArrayList<>(children.size());
            List<ShipAPI> toCheck = new ArrayList<>(children.size());

            while (!children.isEmpty()) {
                allChildren.addAll(children);
                toCheck.clear();
                toCheck.addAll(children);
                children.clear();
                for (ShipAPI child : toCheck) {
                    if (child.isShipWithModules()) {
                        children.addAll(child.getChildModulesCopy());
                    }
                }
            }

            return allChildren;
        }
    }
}
