package com.nightcity.tower.decorator;

import com.nightcity.model.Enemy;
import com.nightcity.tower.Tower;

/**
 * Kang Tao Tech Railgun Mod
 *
 * Inspired by the charged/tech weapons in Cyberpunk 2077 (e.g. Kang Tao G-58 Dian).
 * Extends the tower's effective range by 15 units and marks shots as piercing.
 *
 * Cost: 60 Eddies
 */
public class TechShotDecorator extends TowerDecorator {

    private static final double RANGE_BONUS = 15.0;

    public TechShotDecorator(Tower inner) {
        super(inner);
    }

    @Override
    public double getRange() {
        return inner.getRange() + RANGE_BONUS;
    }

    @Override
    public String getDescription() {
        return "Kang Tao Tech Railgun Mod >> " + inner.getDescription();
    }

    @Override
    public String shoot(Enemy enemy) {
        return inner.shoot(enemy) + " [PIERCING ROUND]";
    }
}
