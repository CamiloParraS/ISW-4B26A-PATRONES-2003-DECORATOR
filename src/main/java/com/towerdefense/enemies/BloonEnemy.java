package com.towerdefense.enemies;

import com.towerdefense.engine.GameState;
import com.towerdefense.map.GameMap;
import java.util.List;
import java.util.UUID;

public class BloonEnemy implements Enemy {
    private final String id = UUID.randomUUID().toString();
    private final BloonType type;
    private final int maxHp;
    private final double radius;

    private int hp;
    private double px;
    private double py;
    private double slowFactor = 1.0;
    private double slowTimerSeconds = 0.0;
    private boolean dead = false;

    private int progressIndex = 0;
    private double progressOffset = 0.0;

    public BloonEnemy(BloonType type, List<int[]> waypoints) {
        this.type = type;
        this.maxHp = type.getHp();
        this.hp = this.maxHp;
        this.radius = type == BloonType.MOAB ? 24.0 : (type == BloonType.CERAMIC ? 16.0 : 14.0);

        if (waypoints.isEmpty()) {
            this.px = 0;
            this.py = 0;
            this.dead = true;
            return;
        }

        this.px = centerPx(waypoints.get(0)[0]);
        this.py = centerPy(waypoints.get(0)[1]);
    }

    public BloonEnemy(BloonType type, double px, double py, int progressIndex,
            double progressOffset) {
        this.type = type;
        this.maxHp = type.getHp();
        this.hp = this.maxHp;
        this.radius = type == BloonType.MOAB ? 24.0 : (type == BloonType.CERAMIC ? 16.0 : 14.0);
        this.px = px;
        this.py = py;
        this.progressIndex = progressIndex;
        this.progressOffset = progressOffset;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public BloonType getType() {
        return type;
    }

    @Override
    public double getPx() {
        return px;
    }

    @Override
    public double getPy() {
        return py;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public double getSlowFactor() {
        return slowFactor;
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public void tick(double delta, List<int[]> waypoints) {
        if (dead || delta <= 0 || waypoints.size() < 2) {
            return;
        }

        if (slowTimerSeconds > 0) {
            slowTimerSeconds -= delta;
            if (slowTimerSeconds <= 0) {
                slowTimerSeconds = 0;
                slowFactor = 1.0;
            }
        }

        double remaining = type.getSpeedPxPerSecond() * slowFactor * delta;
        while (remaining > 0 && !dead) {
            if (progressIndex >= waypoints.size() - 1) {
                GameState.getInstance().loseLife();
                dead = true;
                return;
            }

            int[] targetWp = waypoints.get(progressIndex + 1);
            double targetX = centerPx(targetWp[0]);
            double targetY = centerPy(targetWp[1]);

            double dx = targetX - px;
            double dy = targetY - py;
            double dist = Math.hypot(dx, dy);

            if (dist < 0.000001) {
                progressIndex++;
                progressOffset = 0;
                continue;
            }

            if (remaining < dist) {
                double ratio = remaining / dist;
                px += dx * ratio;
                py += dy * ratio;
                progressOffset += remaining;
                remaining = 0;
            } else {
                px = targetX;
                py = targetY;
                remaining -= dist;
                progressIndex++;
                progressOffset = 0;
            }
        }

        if (!dead && progressIndex >= waypoints.size() - 1) {
            GameState.getInstance().loseLife();
            dead = true;
        }
    }

    @Override
    public void applyDamage(int amount) {
        if (dead || amount <= 0) {
            return;
        }
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
    }

    @Override
    public void applySlow(double factor, double durationSeconds) {
        if (dead || factor <= 0 || durationSeconds <= 0) {
            return;
        }
        slowFactor = Math.min(slowFactor, factor);
        slowTimerSeconds = Math.max(slowTimerSeconds, durationSeconds);
    }

    @Override
    public int getProgressIndex() {
        return progressIndex;
    }

    @Override
    public double getProgressOffset() {
        return progressOffset;
    }

    private static double centerPx(int col) {
        return col * (double) GameMap.TILE + GameMap.TILE / 2.0;
    }

    private static double centerPy(int row) {
        return row * (double) GameMap.TILE + GameMap.TILE / 2.0;
    }
}
