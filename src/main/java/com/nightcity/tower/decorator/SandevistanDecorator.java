package com.nightcity.tower.decorator;

import com.nightcity.model.Enemy;
import com.nightcity.tower.Tower;

/**
 * QianT Sandevistan Overclock
 *
 * Inspired by the Sandevistan operating system from Cyberpunk 2077.
 * Overclocks the tower's neural targeting loop, doubling its fire rate.
 *
 * Cost: 5 RAM
 */
public class SandevistanDecorator extends TowerDecorator {

    public SandevistanDecorator(Tower inner) {
        super(inner);
    }

    @Override
    public double getFireRate() {
        return inner.getFireRate() * 2.0;
    }

    @Override
    public String getDescription() {
        return "QianT Sandevistan Overclock >> " + inner.getDescription();
    }

    @Override
    public String shoot(Enemy enemy) {
        return inner.shoot(enemy) + " [SANDEVISTAN OVERCLOCK]";
    }
}
