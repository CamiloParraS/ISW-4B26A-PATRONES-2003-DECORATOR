package com.towerdefense.towers;

import com.towerdefense.towers.decorators.CamoDetectorDecorator;
import com.towerdefense.towers.decorators.ExplosiveDecorator;
import com.towerdefense.towers.decorators.FreezingDecorator;
import com.towerdefense.towers.decorators.LaserDecorator;
import com.towerdefense.towers.decorators.PiercingDecorator;
import com.towerdefense.towers.decorators.RapidFireDecorator;
import com.towerdefense.towers.decorators.SniperDecorator;
import com.towerdefense.towers.decorators.TowerDecorator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpgradeRegistry {
    private record UpgradeSpec(Function<Tower, Tower> factory, int cost, String label) {
    }

    private static final Map<String, UpgradeSpec> SPECS =
            Map.of("RapidFire", new UpgradeSpec(RapidFireDecorator::new, 350, "Rapid Fire"),
                    "Sniper", new UpgradeSpec(SniperDecorator::new, 500, "Sniper"), "Explosive",
                    new UpgradeSpec(ExplosiveDecorator::new, 600, "Explosive"), "Freezing",
                    new UpgradeSpec(FreezingDecorator::new, 450, "Freezing"), "Piercing",
                    new UpgradeSpec(PiercingDecorator::new, 400, "Piercing"), "Laser",
                    new UpgradeSpec(LaserDecorator::new, 700, "Laser"), "CamoDetector",
                    new UpgradeSpec(CamoDetectorDecorator::new, 300, "Camo Detector"));

    private static final Map<String, String> LABEL_TO_KEY = buildLabelIndex();
    private static final Map<Class<?>, UpgradeSpec> CLASS_TO_SPEC = buildClassIndex();

    private UpgradeRegistry() {}

    public static Tower apply(String upgradeKey, Tower tower) {
        UpgradeSpec spec = SPECS.get(upgradeKey);
        if (spec == null) {
            throw new IllegalArgumentException("Unknown upgrade: " + upgradeKey);
        }

        if (tower instanceof TowerDecorator decorator) {
            Tower updatedWrapped = apply(upgradeKey, decorator.getWrapped());
            return rewrap(decorator, updatedWrapped);
        }

        return spec.factory().apply(tower);
    }

    public static int getCost(String upgradeKey) {
        UpgradeSpec spec = SPECS.get(upgradeKey);
        if (spec == null) {
            throw new IllegalArgumentException("Unknown upgrade: " + upgradeKey);
        }
        return spec.cost();
    }

    public static List<String> available(Tower tower) {
        Set<String> installed = installedKeys(tower);
        return SPECS.keySet().stream().filter(k -> !installed.contains(k)).sorted()
                .collect(Collectors.toList());
    }

    public static Set<String> installedKeys(Tower tower) {
        Set<String> installed = new HashSet<>();
        for (String label : tower.getUpgradeLabels()) {
            String key = LABEL_TO_KEY.get(label);
            if (key != null) {
                installed.add(key);
            }
        }
        return installed;
    }

    private static Map<String, String> buildLabelIndex() {
        Map<String, String> index = new HashMap<>();
        SPECS.forEach((key, spec) -> index.put(spec.label(), key));
        return Map.copyOf(index);
    }

    private static Map<Class<?>, UpgradeSpec> buildClassIndex() {
        return Map.of(RapidFireDecorator.class, SPECS.get("RapidFire"), SniperDecorator.class,
                SPECS.get("Sniper"), ExplosiveDecorator.class, SPECS.get("Explosive"),
                FreezingDecorator.class, SPECS.get("Freezing"), PiercingDecorator.class,
                SPECS.get("Piercing"), LaserDecorator.class, SPECS.get("Laser"),
                CamoDetectorDecorator.class, SPECS.get("CamoDetector"));
    }

    private static Tower rewrap(TowerDecorator decorator, Tower newWrapped) {
        UpgradeSpec spec = CLASS_TO_SPEC.get(decorator.getClass());
        if (spec == null) {
            throw new IllegalArgumentException("Unknown decorator type: " + decorator.getClass());
        }
        return spec.factory().apply(newWrapped);
    }
}
