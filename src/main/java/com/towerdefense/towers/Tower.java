package com.towerdefense.towers;

import com.towerdefense.enemies.Enemy;
import com.towerdefense.engine.GameState;
import java.util.List;

public interface Tower {
    String getId();

    String getType();

    int getGridX();

    int getGridY();

    double getCenterPx();

    double getCenterPy();

    double getRange();

    double getFireRate();

    int getDamage();

    int getPierce();

    boolean canTargetCamo();

    boolean canTargetLead();

    String getProjectileType();

    List<String> getUpgradeLabels();

    int getTotalCost();

    int getSellValue();

    default void onTick(double delta, List<Enemy> enemies, GameState state) {
        onTick(delta, enemies, state, this);
    }

    void onTick(double delta, List<Enemy> enemies, GameState state, Tower self);
}
