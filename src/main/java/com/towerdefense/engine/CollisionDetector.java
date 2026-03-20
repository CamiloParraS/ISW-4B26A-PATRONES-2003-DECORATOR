package com.towerdefense.engine;

import com.towerdefense.enemies.BloonEnemy;
import com.towerdefense.enemies.BloonType;
import com.towerdefense.enemies.Enemy;
import com.towerdefense.projectiles.Projectile;
import com.towerdefense.projectiles.ProjectileType;

public final class CollisionDetector {
    private CollisionDetector() {}

    public static void resolve(GameState state) {
        for (Projectile p : state.getProjectiles()) {
            if (p.isDead()) {
                continue;
            }

            state.getEnemies().stream().filter(e -> !e.isDead()).filter(e -> canHit(p, e))
                    .filter(e -> dist(p, e) <= p.getHitRadius() + e.getRadius())
                    .limit(p.getPierce()).forEach(e -> applyHit(p, e, state));
        }
    }

    private static boolean canHit(Projectile p, Enemy e) {
        if (p.getType() == ProjectileType.DART
                && e.getType().getResistances().contains(BloonType.Resist.LEAD)) {
            return false;
        }
        return p.getType() != ProjectileType.FREEZE
                || !e.getType().getResistances().contains(BloonType.Resist.FREEZE);
    }

    private static void applyHit(Projectile p, Enemy e, GameState state) {
        e.applyDamage(p.getDamage());
        p.recordHit();

        if (!e.isDead()) {
            return;
        }

        state.addCoins(e.getType().getReward());

        if (e.getType() == BloonType.MOAB) {
            for (int i = 0; i < 4; i++) {
                state.getEnemies().add(new BloonEnemy(BloonType.CERAMIC, e.getPx(), e.getPy(),
                        e.getProgressIndex(), e.getProgressOffset()));
            }
            return;
        }

        BloonType inner = e.getType().getInnerType();
        if (inner != null) {
            state.getEnemies().add(new BloonEnemy(inner, e.getPx(), e.getPy(), e.getProgressIndex(),
                    e.getProgressOffset()));
        }
    }

    private static double dist(Projectile p, Enemy e) {
        return Math.hypot(p.getPx() - e.getPx(), p.getPy() - e.getPy());
    }
}
