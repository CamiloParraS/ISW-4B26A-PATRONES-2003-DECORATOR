package com.towerdefense.enemies;

import java.util.List;

public interface Enemy {
    String getId();

    BloonType getType();

    double getPx();

    double getPy();

    double getRadius();

    int getHp();

    int getMaxHp();

    double getSlowFactor();

    boolean isDead();

    void tick(double delta, List<int[]> waypoints);

    void applyDamage(int amount);

    void applySlow(double factor, double durationSeconds);

    int getProgressIndex();

    double getProgressOffset();
}
