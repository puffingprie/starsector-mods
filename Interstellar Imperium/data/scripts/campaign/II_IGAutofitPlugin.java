package data.scripts.campaign;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;
import data.scripts.hullmods.II_BasePackage;
import java.util.ArrayList;
import java.util.List;

public class II_IGAutofitPlugin extends CoreAutofitPlugin {

    public II_IGAutofitPlugin(PersonAPI fleetCommander) {
        super(fleetCommander);
    }

    @Override
    public void doFit(ShipVariantAPI current, ShipVariantAPI target, int maxSMods, AutofitPluginDelegate delegate) {
        target = target.clone();
        target.setSource(VariantSource.REFIT);
        List<String> sMods = new ArrayList<>();
        for (String mod : target.getSMods()) {
            sMods.add(mod);
        }
        for (String mod : sMods) {
            target.removePermaMod(mod);
            if (mod.contentEquals(II_BasePackage.CORE_UPGRADES) || mod.contentEquals(II_BasePackage.ARMOR_PACKAGE)
                    || mod.contentEquals(II_BasePackage.TARGETING_PACKAGE) || mod.contentEquals(II_BasePackage.ELITE_PACKAGE)) {
                target.addMod(mod);
            }
        }
        target.addTag(Tags.VARIANT_ALLOW_EXCESS_OP_ETC);
        if (!target.hasHullMod(II_BasePackage.ARMOR_PACKAGE) && !target.hasHullMod(II_BasePackage.TARGETING_PACKAGE) && !target.hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            target.addMod(II_BasePackage.CORE_UPGRADES);
            target.setVariantDisplayName("Imperial Guard");
            if (maxSMods > 0) {
                maxSMods--;
            }
        } else if (target.hasHullMod(II_BasePackage.ELITE_PACKAGE)) {
            target.setVariantDisplayName("Imperial Elite");
            if (maxSMods > 0) {
                maxSMods--;
            }
        }

        super.doFit(current, target, maxSMods, delegate);
    }

    @Override
    public int addModIfPossible(HullModSpecAPI mod, AutofitPluginDelegate delegate, ShipVariantAPI current, int opLeft) {
        if (mod == null) {
            return 0;
        }

        if (current.hasHullMod(mod.getId())) {
            return 0;
        }
        if (delegate.isPlayerCampaignRefit() && !delegate.canAddRemoveHullmodInPlayerCampaignRefit(mod.getId())) {
            return 0;
        }

        int cost = mod.getCostFor(current.getHullSize());
        boolean sModIt = false;
        if (mod.getId().contentEquals(II_BasePackage.ELITE_PACKAGE) || mod.getId().contentEquals(II_BasePackage.CORE_UPGRADES)) {
            cost = 0;
            sModIt = true;
        }
        if (cost > opLeft) {
            return 0;
        }

        ShipAPI ship = delegate.getShip();
        ShipVariantAPI orig = null;
        if (ship != null) {
            orig = ship.getVariant();
            ship.setVariantForHullmodCheckOnly(current);
        }
        if (ship != null && mod.getEffect() != null && ship.getVariant() != null && !mod.getEffect().isApplicableToShip(ship)
                && !ship.getVariant().hasHullMod(mod.getId())) {
            if (orig != null) {
                ship.setVariantForHullmodCheckOnly(orig);
            }
            return 0;
        }

        if (orig != null && ship != null) {
            ship.setVariantForHullmodCheckOnly(orig);
        }

        current.addMod(mod.getId());
        if (sModIt) {
            current.addPermaMod(mod.getId(), true);
        }
        return cost;
    }
}
