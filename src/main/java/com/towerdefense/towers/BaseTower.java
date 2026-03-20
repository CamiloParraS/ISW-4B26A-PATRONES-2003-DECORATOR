package com.towerdefense.towers;

import com.towerdefense.enemies.BloonType;
import com.towerdefense.enemies.Enemy;
import com.towerdefense.engine.GameState;
import com.towerdefense.map.GameMap;
import java.util.Comparator;
import java.util.List;

public class BaseTower implements Tower {
    public static final int BUY_COST = 200;

    private final String id;
    private final int gridX;
    private final int gridY;
    private double cooldown = 0.0;

    public BaseTower(String id, int gridX, int gridY) {
        this.id = id;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "dart";
    }

    @Override
    public int getGridX() {
        return gridX;
    }

    @Override
    public int getGridY() {
        return gridY;
    }

    @Override
    public double getCenterPx() {
        return getGridX() * (double) GameMap.TILE + GameMap.TILE / 2.0;
    }

    @Override
    public double getCenterPy() {
        return getGridY() * (double) GameMap.TILE + GameMap.TILE / 2.0;
    }

    @Override
    public double getRange() {
        return 120.0;
    }

    @Override
    public double getFireRate() {
        return 1.0;
    }

    @Override
    public int getDamage() {
        return 1;
    }

    @Override
    public int getPierce() {
        return 1;
    }

    @Override
    public boolean canTargetCamo() {
        return false;
    }

    @Override
    public boolean canTargetLead() {
        return false;
    }

    @Override
    public String getProjectileType() {
        return "dart";
    }

    @Override
    public List<String> getUpgradeLabels() {
        return List.of();
    }

    @Override
    public int getTotalCost() {
        return BUY_COST;
    }

    @Override
    public int getSellValue() {
        return (int) (getTotalCost() * 0.7);
    }

    @Override
    public void onTick(double delta, List<Enemy> enemies, GameState state) {
        cooldown -= delta;
        if (cooldown > 0) {
            return;
        }

        Enemy target = enemies.stream().filter(e -> !e.isDead())
                .filter(e -> distanceTo(e) <= getRange()).filter(this::canHitEnemy)
                .max(Comparator.comparingInt(Enemy::getProgressIndex)
                        .thenComparingDouble(Enemy::getProgressOffset))
                .orElse(null);

        if (target == null) {
            return;
        }

        state.spawnProjectile(this, target);
        cooldown = 1.0 / getFireRate();
    }

    private boolean canHitEnemy(Enemy e) {
        if (!canTargetCamo() && e.getType().getResistances().contains(BloonType.Resist.CAMO)) {
            return false;
        }
        return canTargetLead() || !e.getType().getResistances().contains(BloonType.Resist.LEAD);
    }

    private double distanceTo(Enemy e) {
        return Math.hypot(e.getPx() - getCenterPx(), e.getPy() - getCenterPy());
    }
}
