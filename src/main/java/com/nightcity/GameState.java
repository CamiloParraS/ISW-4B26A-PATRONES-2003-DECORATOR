package com.nightcity;

import com.nightcity.model.Enemy;
import com.nightcity.tower.*;
import com.nightcity.tower.decorator.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-instance mutable game state.
 * All mutation happens synchronously on the HTTP handler thread,
 * so no locking is required for this single-player educational project.
 */
public class GameState {

    // ── Constants ─────────────────────────────────────────────────────

    public static final int[] SLOTS = { 20, 40, 60, 80 };

    private static final String[] ENEMY_NAMES = {
        "Maelstrom Cyberpsycho",
        "Arasaka Elite Netrunner",
        "Militech Goliath Heavy"
    };

    // ── State fields ──────────────────────────────────────────────────

    public final List<PlacedTower> towers  = new ArrayList<>();
    public final List<Enemy>       enemies = new ArrayList<>();
    public final List<String>      log     = new ArrayList<>();

    public int     coreHp  = 100;
    public int     wave    = 0;
    public int     eddies  = 150;
    public int     ram     = 10;
    public boolean running = false;

    // ── Logging ───────────────────────────────────────────────────────

    public void addLog(String message) {
        System.out.println(message);
        log.add(message);
        if (log.size() > 60) {
            log.remove(0);
        }
    }

    // ── Slot helpers ──────────────────────────────────────────────────

    public boolean isValidSlot(int pos) {
        for (int s : SLOTS) {
            if (s == pos) return true;
        }
        return false;
    }

    public boolean isSlotFree(int pos) {
        for (PlacedTower t : towers) {
            if (t.pos == pos) return false;
        }
        return true;
    }

    // ── Commands ──────────────────────────────────────────────────────

    /**
     * Deploy a new BasicTurret at the given slot.
     * Costs 50 Eddies. Slot must be valid and unoccupied.
     */
    public void placeTower(int pos) {
        if (!isValidSlot(pos)) {
            addLog("[ERR] Slot " + pos + " is not a valid position."); return;
        }
        if (!isSlotFree(pos)) {
            addLog("[ERR] Slot " + pos + " is already occupied."); return;
        }
        if (eddies < 50) {
            addLog("[ERR] Need 50 Eddies to deploy. You have " + eddies + "."); return;
        }
        towers.add(new PlacedTower(new BasicTurret(pos), pos));
        eddies -= 50;
        addLog("[BUILD] Militech Smart-Gun Turret deployed at slot " + pos + "  (-50 Eddies)");
    }

    /**
     * Wrap the tower at {@code index} with the named decorator.
     * The old tower becomes the {@code inner} of the new decorator.
     */
    public void upgradeTower(int index, String decoratorKey) {
        if (index < 0 || index >= towers.size()) {
            addLog("[ERR] No tower at index " + index); return;
        }
        PlacedTower pt = towers.get(index);

        switch (decoratorKey) {
            case "quickhack" -> {
                if (ram < 3) { addLog("[ERR] Need 3 RAM (have " + ram + ")"); return; }
                pt.tower = new QuickhackDecorator(pt.tower);
                ram -= 3;
                addLog("[MOD] Contagion Quickhack Node installed on T" + index + "  (-3 RAM)");
            }
            case "smartlink" -> {
                if (eddies < 40) { addLog("[ERR] Need 40 Eddies (have " + eddies + ")"); return; }
                pt.tower = new SmartLinkDecorator(pt.tower);
                eddies -= 40;
                addLog("[MOD] Militech SmartLink installed on T" + index + "  (-40 Eddies)");
            }
            case "techshot" -> {
                if (eddies < 60) { addLog("[ERR] Need 60 Eddies (have " + eddies + ")"); return; }
                pt.tower = new TechShotDecorator(pt.tower);
                eddies -= 60;
                addLog("[MOD] Kang Tao Tech Railgun Mod installed on T" + index + "  (-60 Eddies)");
            }
            case "sandevistan" -> {
                if (ram < 5) { addLog("[ERR] Need 5 RAM (have " + ram + ")"); return; }
                pt.tower = new SandevistanDecorator(pt.tower);
                ram -= 5;
                addLog("[MOD] QianT Sandevistan Overclock installed on T" + index + "  (-5 RAM)");
            }
            default -> addLog("[ERR] Unknown module key: " + decoratorKey);
        }
    }

    /**
     * Spawn the next wave of enemies and start the simulation.
     * Enemies are staggered at negative positions so they enter the path one by one.
     * Grants +25 Eddies and +2 RAM as a wave-start bonus.
     */
    public void startWave() {
        if (running)      { addLog("[ERR] A wave is already active."); return; }
        if (coreHp <= 0)  { addLog("[ERR] Core destroyed. GAME OVER."); return; }

        wave++;
        enemies.clear();

        int    count = 3 + wave;
        double hp    = 30 + wave * 20.0;
        double speed = 4  + wave * 1.5;

        for (int i = 0; i < count; i++) {
            String name  = ENEMY_NAMES[i % ENEMY_NAMES.length];
            Enemy  enemy = new Enemy(name, hp, speed);
            enemy.position = -(i * 12.0);   // stagger entry from the left
            enemies.add(enemy);
        }

        eddies  += 25;
        ram     += 2;
        running  = true;
        addLog("=== WAVE " + wave + " INCOMING | " + count + " enemies"
               + " | HP=" + (int) hp + " speed=" + speed
               + " | +25 Eddies +2 RAM ===");
    }

    /**
     * Advance the simulation by one second.
     *
     * Order of operations each tick:
     *   1. Move all enemies forward by their speed.
     *   2. Remove enemies that have breached the core (deal -20 core HP).
     *   3. Each tower fires at the closest enemy within range (respecting cooldown).
     *   4. Remove killed enemies (grant +10 Eddies each).
     *   5. Check win/lose conditions.
     */
    public void tick() {
        if (!running) {
            addLog("[SIM] No active wave. Start a wave first."); return;
        }

        addLog("--- TICK | enemies=" + enemies.size()
               + " | coreHP=" + coreHp + " ---");

        List<Enemy> toRemove = new ArrayList<>();

        // 1 + 2: move & breach check
        for (Enemy enemy : enemies) {
            enemy.position += enemy.speed;
            if (enemy.position >= 100) {
                coreHp -= 20;
                toRemove.add(enemy);
                addLog("!!! " + enemy.name + " BREACHED THE CORE!  Core HP -> " + coreHp);
            }
        }
        enemies.removeAll(toRemove);
        toRemove.clear();

        // 3: towers fire
        for (PlacedTower pt : towers) {
            if (pt.ticksUntilFire > 0) {
                pt.ticksUntilFire--;
                continue;
            }

            Enemy  target  = null;
            double minDist = Double.MAX_VALUE;
            for (Enemy enemy : enemies) {
                double dist = Math.abs(enemy.position - pt.pos);
                if (dist <= pt.tower.getRange() && dist < minDist) {
                    minDist = dist;
                    target  = enemy;
                }
            }

            if (target != null) {
                String msg = pt.tower.shoot(target);
                addLog("  [T" + towers.indexOf(pt) + "@" + pt.pos + "] " + msg
                       + "  |  " + target.name + " HP=" + String.format("%.0f", target.hp));

                if (!target.alive) {
                    addLog("  >> FLATLINED: " + target.name + "  +10 Eddies");
                    toRemove.add(target);
                    eddies += 10;
                }

                // cooldown = ceil(1 / fireRate) ticks, minimum 1
                int cooldown = Math.max(1, (int) Math.round(1.0 / pt.tower.getFireRate()));
                pt.ticksUntilFire = cooldown - 1;
            }
        }
        enemies.removeAll(toRemove);

        // 4: end-of-tick conditions
        if (enemies.isEmpty()) {
            running = false;
            addLog("=== WAVE " + wave + " CLEARED"
                   + " | Eddies=" + eddies + " RAM=" + ram + " CoreHP=" + coreHp + " ===");
        }
        if (coreHp <= 0) {
            running = false;
            addLog("!!! CORE DESTROYED — GAME OVER !!!");
        }
    }
}
