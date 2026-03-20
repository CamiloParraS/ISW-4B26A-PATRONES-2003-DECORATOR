package com.nightcity.tower.decorator;

import com.nightcity.model.Enemy;
import com.nightcity.tower.Tower;

/**
 * Militech SmartLink
 *
 * Inspired by the Militech Smart Link cyberware from Cyberpunk 2077.
 * Increases accuracy/targeting so every shot deals 1.5x damage.
 *
 * Cost: 40 Eddies
 */
public class SmartLinkDecorator extends TowerDecorator {

    public SmartLinkDecorator(Tower inner) {
        super(inner);
    }

    @Override
    public double getDamage() {
        return inner.getDamage() * 1.5;
    }

    @Override
    public String getDescription() {
        return "Militech SmartLink >> " + inner.getDescription();
    }

    /**
     * {@code inner.shoot()} already applies {@code inner.getDamage()}.
     * This layer only needs to deal the 50 % bonus on top.
     */
    @Override
    public String shoot(Enemy enemy) {
        double bonus = inner.getDamage() * 0.5;
        String log   = inner.shoot(enemy);
        enemy.takeDamage(bonus);
        return log + String.format(" +SmartLink +%.0f", bonus);
    }
}
