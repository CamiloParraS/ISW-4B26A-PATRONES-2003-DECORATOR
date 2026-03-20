package com.towerdefense.projectiles;

public class Projectile {
    private final String id;
    private final ProjectileType type;
    private double px;
    private double py;
    private final double targetPx;
    private final double targetPy;
    private final double speed;
    private final int damage;
    private final int pierce;
    private int hitsRemaining;
    private final double hitRadius;
    private boolean dead;

    public Projectile(String id, ProjectileType type, double px, double py, double targetPx,
            double targetPy, int damage, int pierce) {
        this.id = id;
        this.type = type;
        this.px = px;
        this.py = py;
        this.targetPx = targetPx;
        this.targetPy = targetPy;
        this.damage = damage;

        this.speed = defaultSpeed(type);
        this.hitRadius = defaultHitRadius(type);

        int resolvedPierce = pierce > 0 ? pierce : defaultPierce(type);
        this.pierce = resolvedPierce;
        this.hitsRemaining = resolvedPierce;
    }

    public void tick(double delta) {
        if (dead || delta <= 0) {
            return;
        }
        if (hitsRemaining == 0) {
            dead = true;
            return;
        }

        double dx = targetPx - px;
        double dy = targetPy - py;
        double dist = Math.hypot(dx, dy);
        if (dist <= hitRadius) {
            dead = true;
            return;
        }

        double step = speed * delta;
        if (step >= dist) {
            px = targetPx;
            py = targetPy;
            if (Math.hypot(targetPx - px, targetPy - py) <= hitRadius) {
                dead = true;
            }
            return;
        }

        double ratio = step / dist;
        px += dx * ratio;
        py += dy * ratio;

        if (Math.hypot(targetPx - px, targetPy - py) <= hitRadius) {
            dead = true;
        }
    }

    public void recordHit() {
        if (dead || hitsRemaining <= 0) {
            dead = true;
            return;
        }
        hitsRemaining -= 1;
        if (hitsRemaining <= 0) {
            dead = true;
        }
    }

    public String getId() {
        return id;
    }

    public ProjectileType getType() {
        return type;
    }

    public double getPx() {
        return px;
    }

    public double getPy() {
        return py;
    }

    public double getTargetPx() {
        return targetPx;
    }

    public double getTargetPy() {
        return targetPy;
    }

    public double getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public int getPierce() {
        return pierce;
    }

    public int getHitsRemaining() {
        return hitsRemaining;
    }

    public double getHitRadius() {
        return hitRadius;
    }

    public boolean isDead() {
        return dead;
    }

    private static double defaultSpeed(ProjectileType type) {
        return switch (type) {
            case DART -> 400;
            case BOMB -> 300;
            case FREEZE -> 350;
            case LASER -> 1200;
        };
    }

    private static double defaultHitRadius(ProjectileType type) {
        return switch (type) {
            case DART -> 8;
            case BOMB -> 12;
            case FREEZE -> 10;
            case LASER -> 6;
        };
    }

    private static int defaultPierce(ProjectileType type) {
        return switch (type) {
            case DART -> 1;
            case BOMB -> 999;
            case FREEZE -> 999;
            case LASER -> 1;
        };
    }
}
