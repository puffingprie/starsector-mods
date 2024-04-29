package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableFighter;
import com.fs.starfarer.api.plugins.AutofitPlugin.AvailableWeapon;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Tons of code duplication, should be a stripped-down copy of DefaultFleetInflater except for the necessary package-
// restricted transient variables and the use of the IG autofit plugin
public class II_IGFleetInflater extends DefaultFleetInflater {

    public II_IGFleetInflater(DefaultFleetInflaterParams p) {
        super(p);
    }

    private transient FleetMemberAPI currMember = null;
    private transient ShipVariantAPI currVariant = null;
    private transient List<AvailableFighter> fighters;
    private transient List<AvailableWeapon> weapons;
    private transient List<String> hullmods;
    private transient FactionAPI faction;

    @Override
    public void inflate(CampaignFleetAPI fleet) {
        Random random = new Random();
        if (p.seed != null) {
            random = new Random(p.seed);
        }

        Random dmodRandom = new Random();
        if (p.seed != null) {
            dmodRandom = Misc.getRandom(p.seed, 5);
        }

        II_IGAutofitPlugin auto = new II_IGAutofitPlugin(fleet.getCommander());
        auto.setRandom(random);

        boolean upgrade = random.nextFloat() < Math.min(0.1f + p.quality * 0.5f, 0.5f);
        auto.setChecked(II_IGAutofitPlugin.UPGRADE, upgrade);

        this.faction = fleet.getFaction();
        if (p.factionId != null) {
            this.faction = Global.getSector().getFaction(p.factionId);
        }

        hullmods = new ArrayList<>(faction.getKnownHullMods());

        SortedWeapons nonPriorityWeapons = new SortedWeapons();
        SortedWeapons priorityWeapons = new SortedWeapons();

        Set<String> weaponCategories = new LinkedHashSet<>();
        for (String weaponId : faction.getKnownWeapons()) {
            if (!faction.isWeaponKnownAt(weaponId, p.timestamp)) {
                continue;
            }

            WeaponSpecAPI spec = Global.getSettings().getWeaponSpec(weaponId);

            if (spec == null) {
                throw new RuntimeException("Weapon with spec id [" + weaponId + "] not found");
            }

            int tier = spec.getTier();
            String cat = spec.getAutofitCategory();

            if (isPriority(spec)) {
                List<AvailableWeapon> list = priorityWeapons.getWeapons(tier).getWeapons(cat).getWeapons(spec.getSize());
                list.add(new AvailableWeaponImpl(spec, 1000));
            } else {
                List<AvailableWeapon> list = nonPriorityWeapons.getWeapons(tier).getWeapons(cat).getWeapons(spec.getSize());
                list.add(new AvailableWeaponImpl(spec, 1000));
            }
            weaponCategories.add(cat);
        }

        ListMap<AvailableFighter> nonPriorityFighters = new ListMap<>();
        ListMap<AvailableFighter> priorityFighters = new ListMap<>();
        Set<String> fighterCategories = new LinkedHashSet<>();
        for (String wingId : faction.getKnownFighters()) {
            if (!faction.isFighterKnownAt(wingId, p.timestamp)) {
                continue;
            }

            FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wingId);
            if (spec == null) {
                throw new RuntimeException("Fighter wing with spec id [" + wingId + "] not found");
            }

            String cat = spec.getAutofitCategory();
            if (isPriority(spec)) {
                priorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
            } else {
                nonPriorityFighters.add(cat, new AvailableFighterImpl(spec, 1000));
            }
            fighterCategories.add(cat);
        }

        float averageDmods = getAverageDmodsForQuality(p.quality);

        boolean forceAutofit = fleet.getMemoryWithoutUpdate().getBoolean(MemFlags.MEMORY_KEY_FORCE_AUTOFIT_ON_NO_AUTOFIT_SHIPS);
        int memberIndex = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {

            if (!forceAutofit && member.getHullSpec().hasTag(Tags.TAG_NO_AUTOFIT)) {
                continue;
            }
            if (!forceAutofit && member.getVariant() != null && member.getVariant().hasTag(Tags.TAG_NO_AUTOFIT)) {
                continue;
            }

            if (!faction.isPlayerFaction()) {
                if (!forceAutofit && member.getHullSpec().hasTag(Tags.TAG_NO_AUTOFIT_UNLESS_PLAYER)) {
                    continue;
                }
                if (!forceAutofit && member.getVariant() != null && member.getVariant().hasTag(Tags.TAG_NO_AUTOFIT_UNLESS_PLAYER)) {
                    continue;
                }
            }

            // need this so that when reinflating a fleet that lost members, the members reinflate consistently
            if (p.seed != null) {
                int extra = member.getShipName().hashCode();
                random = new Random(p.seed * extra);
                auto.setRandom(random);
                dmodRandom = Misc.getRandom(p.seed * extra, 5);
            }

            List<WeaponSize> sizes = new ArrayList<>();
            sizes.add(WeaponSize.SMALL);
            sizes.add(WeaponSize.MEDIUM);
            sizes.add(WeaponSize.LARGE);

            weapons = new ArrayList<>();
            for (String cat : weaponCategories) {
                for (WeaponSize size : sizes) {
                    boolean foundSome = false;
                    for (int tier = 0; tier < 4; tier++) {
                        float tP = getTierProbability(tier, this.p.quality);
                        if (this.p.allWeapons != null && this.p.allWeapons) {
                            tP = 1f;
                        }

                        List<AvailableWeapon> priority = priorityWeapons.getWeapons(tier).getWeapons(cat).getWeapons(size);
                        List<AvailableWeapon> nonPriority = nonPriorityWeapons.getWeapons(tier).getWeapons(cat).getWeapons(size);

                        if (!foundSome) {
                            tP = 1f;
                        }

                        boolean tierAvailable = random.nextFloat() < tP;
                        if (!tierAvailable && foundSome) {
                            continue;
                        }

                        int num = 2;
                        switch (size) {
                            case LARGE:
                                num = 2;
                                break;
                            case MEDIUM:
                                num = 2;
                                break;
                            case SMALL:
                                num = 2;
                                break;
                        }

                        if (this.p.allWeapons != null && this.p.allWeapons) {
                            num = 500;
                        }

                        Set<Integer> picks = makePicks(num, priority.size(), random);
                        for (Integer index : picks) {
                            AvailableWeapon w = priority.get(index);
                            weapons.add(w);
                            foundSome = true;
                        }

                        num -= picks.size();
                        if (num > 0) {
                            picks = makePicks(num, nonPriority.size(), random);
                            for (Integer index : picks) {
                                AvailableWeapon w = nonPriority.get(index);
                                weapons.add(w);
                                foundSome = true;
                            }
                        }
                    }
                }
            }

            fighters = new ArrayList<>();
            for (String cat : fighterCategories) {
                List<AvailableFighter> priority = priorityFighters.get(cat);

                boolean madePriorityPicks = false;
                if (priority != null) {
                    int num = random.nextInt(2) + 1;
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        num = 100;
                    }

                    Set<Integer> picks = makePicks(num, priority.size(), random);
                    for (Integer index : picks) {
                        AvailableFighter f = priority.get(index);
                        fighters.add(f);
                        madePriorityPicks = true;
                    }
                }

                if (!madePriorityPicks) {
                    int num = random.nextInt(2) + 1;
                    if (this.p.allWeapons != null && this.p.allWeapons) {
                        num = 100;
                    }

                    List<AvailableFighter> nonPriority = nonPriorityFighters.get(cat);
                    Set<Integer> picks = makePicks(num, nonPriority.size(), random);
                    for (Integer index : picks) {
                        AvailableFighter f = nonPriority.get(index);
                        fighters.add(f);
                    }
                }
            }

            ShipVariantAPI target = member.getVariant();
            if (target.getOriginalVariant() != null) {
                // needed if inflating the same fleet repeatedly to pick up weapon availability changes etc
                target = Global.getSettings().getVariant(target.getOriginalVariant());
            }

            if (faction.isPlayerFaction()) {
                if (random.nextFloat() < GOAL_VARIANT_PROBABILITY) {
                    List<ShipVariantAPI> targets = Global.getSector().getAutofitVariants().getTargetVariants(member.getHullId());
                    WeightedRandomPicker<ShipVariantAPI> alts = new WeightedRandomPicker<>(random);
                    for (ShipVariantAPI curr : targets) {
                        if (curr.getHullSpec().getHullId().equals(target.getHullSpec().getHullId())) {
                            alts.add(curr);
                        }
                    }
                    if (!alts.isEmpty()) {
                        target = alts.pick();
                    }
                }
            }

            currVariant = Global.getSettings().createEmptyVariant(fleet.getId() + "_" + memberIndex, target.getHullSpec());
            currMember = member;

            if (target.isStockVariant()) {
                currVariant.setOriginalVariant(target.getHullVariantId());
            }

            float rProb = faction.getDoctrine().getAutofitRandomizeProbability();
            if (p.rProb != null) {
                rProb = p.rProb;
            }
            boolean randomize = random.nextFloat() < rProb;
            if (member.isStation()) {
                randomize = false;
            }
            auto.setChecked(II_IGAutofitPlugin.RANDOMIZE, randomize);

            memberIndex++;

            int maxSmods = 0;
            if (p.averageSMods != null && !member.isCivilian()) {
                maxSmods = getMaxSMods(currVariant, p.averageSMods, dmodRandom) - currVariant.getSMods().size();
            }
            auto.doFit(currVariant, target, maxSmods, this);
            currVariant.setSource(VariantSource.REFIT);
            member.setVariant(currVariant, false, false);

            if (!currMember.isStation()) {
                int addDmods = getNumDModsToAdd(currVariant, averageDmods, dmodRandom);
                if (addDmods > 0) {
                    DModManager.setDHull(currVariant);
                    DModManager.addDMods(member, true, addDmods, dmodRandom);
                }
            }
        }

        fleet.getFleetData().setSyncNeeded();
        fleet.getFleetData().syncIfNeeded();
    }

    @Override
    public void clearFighterSlot(int index, ShipVariantAPI variant) {
        variant.setWingId(index, null);
        for (AvailableFighter curr : fighters) {
            if (curr.getId().equals(curr.getId())) {
                curr.setQuantity(curr.getQuantity() + 1);
                break;
            }
        }
    }

    @Override
    public void clearWeaponSlot(WeaponSlotAPI slot, ShipVariantAPI variant) {
        variant.clearSlot(slot.getId());
        for (AvailableWeapon curr : weapons) {
            if (curr.getId().equals(curr.getId())) {
                curr.setQuantity(curr.getQuantity() + 1);
                break;
            }
        }
    }

    @Override
    public List<AvailableFighter> getAvailableFighters() {
        return fighters;
    }

    @Override
    public List<AvailableWeapon> getAvailableWeapons() {
        return weapons;
    }

    @Override
    public List<String> getAvailableHullmods() {
        return hullmods;
    }

    @Override
    public boolean isPriority(WeaponSpecAPI weapon) {
        return faction.isWeaponPriority(weapon.getWeaponId());
    }

    @Override
    public boolean isPriority(FighterWingSpecAPI wing) {
        return faction.isFighterPriority(wing.getId());
    }

    @Override
    public FleetMemberAPI getMember() {
        return currMember;
    }

    @Override
    public FactionAPI getFaction() {
        return faction;
    }
}
