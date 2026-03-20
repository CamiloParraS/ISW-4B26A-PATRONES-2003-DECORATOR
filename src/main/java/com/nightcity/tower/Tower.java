package com.nightcity.tower;

import com.nightcity.model.Enemy;

/**
 * Root interface for all towers (base and decorated).
 * Every decorator wraps another Tower, so the entire upgrade stack
 * is transparent to the simulation logic.
 */
public interface Tower {

    /** Total effective damage per shot (after all decorators). */
    double getDamage();

    /** Effective range (units on the 0-100 path). */
    double getRange();

    /** Effective shots per simulated second. */
    double getFireRate();

    /** Human-readable description of the full decorator stack. */
    String getDescription();

    /**
     * Apply this tower's shot to the given enemy.
     * Implementations must call {@code enemy.takeDamage()} for their share
     * and return a short log message.
     *
     * @param enemy the target
     * @return a short log line, e.g. "Hit Maelstrom Cyberpsycho for 12 dmg"
     */
    String shoot(Enemy enemy);
}
