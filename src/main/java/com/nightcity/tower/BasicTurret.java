package com.nightcity.tower;

import com.nightcity.model.Enemy;

/**
 * The simplest deployable tower.
 * Low damage, medium range, slow fire rate.
 * Serves as the innermost object in any decorator chain.
 */
public class BasicTurret implements Tower {

    private final int slotPosition;

    public BasicTurret(int slotPosition) {
        this.slotPosition = slotPosition;
    }

    @Override public double getDamage()   { return 12; }
    @Override public double getRange()    { return 20; }
    @Override public double getFireRate() { return 0.5; }  // 1 shot every 2 ticks

    @Override
    public String getDescription() {
        return "Militech Smart-Gun Turret [slot=" + slotPosition + "]";
    }

    @Override
    public String shoot(Enemy enemy) {
        enemy.takeDamage(getDamage());
        return "Hit " + enemy.name + " for " + (int) getDamage() + " dmg";
    }
}
