package com.towerdefense.towers.decorators;

import com.towerdefense.enemies.Enemy;
import com.towerdefense.engine.GameState;
import com.towerdefense.towers.Tower;
import java.util.Comparator;
import java.util.List;

public class SniperDecorator extends TowerDecorator {
    private double cooldown = 0.0;

    public SniperDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public double getRange() {
        return wrapped.getRange() * 2.5;
    }

    @Override
    public double getFireRate() {
        return wrapped.getFireRate() * 0.6;
    }

    @Override
    public void onTick(double delta, List<Enemy> enemies, GameState state) {
        cooldown -= delta;
        if (cooldown > 0) {
            return;
        }

        Enemy target = enemies.stream().filter(e -> !e.isDead()).filter(
                e -> Math.hypot(e.getPx() - getCenterPx(), e.getPy() - getCenterPy()) <= getRange())
                .filter(e -> canHitEnemy(e, state))
                .max(Comparator.comparingInt(Enemy::getProgressIndex)
                        .thenComparingDouble(Enemy::getProgressOffset))
                .orElse(null);

        if (target == null) {
            return;
        }

        state.spawnProjectile(this, target);
        cooldown = 1.0 / getFireRate();
    }

    @Override
    protected String decoratorLabel() {
        return "Sniper";
    }

    @Override
    protected int ownCost() {
        return 500;
    }
}
