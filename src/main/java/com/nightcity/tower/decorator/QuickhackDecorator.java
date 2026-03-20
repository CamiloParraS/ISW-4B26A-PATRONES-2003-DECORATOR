package com.nightcity.tower.decorator;

import com.nightcity.model.Enemy;
import com.nightcity.tower.Tower;

/**
 * Contagion Quickhack Node
 *
 * Inspired by the Contagion quickhack from Cyberpunk 2077.
 * Applies an additional +5 damage-over-time on every shot.
 *
 * Cost: 3 RAM
 */
public class QuickhackDecorator extends TowerDecorator {

    private static final double DOT = 5.0;

    public QuickhackDecorator(Tower inner) {
        super(inner);
    }

    /** Advertised damage includes the DoT bonus. */
    @Override
    public double getDamage() {
        return inner.getDamage() + DOT;
    }

    @Override
    public String getDescription() {
        return "Contagion Quickhack Node >> " + inner.getDescription();
    }

    /**
     * Delegates the base shot to {@code inner}, then applies DoT separately.
     * This keeps each layer responsible for exactly its own damage share.
     */
    @Override
    public String shoot(Enemy enemy) {
        String log = inner.shoot(enemy);   // inner applies its portion
        enemy.takeDamage(DOT);             // this layer adds its flat DoT
        return log + " +5 Contagion DoT";
    }
}
