package com.towerdefense.engine;

import com.towerdefense.enemies.BloonEnemy;
import com.towerdefense.enemies.BloonType;
import com.towerdefense.enemies.Enemy;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WaveManager {
    public record SpawnEvent(BloonType type, int count, double interval) {
    }

    private final Queue<SpawnEvent> queue = new LinkedList<>();
    private SpawnEvent activeEvent;
    private int activeRemaining;
    private double spawnTimer = 0;

    public synchronized void startWave(int waveNumber) {
        queue.clear();
        activeEvent = null;
        activeRemaining = 0;
        spawnTimer = 0;

        if (waveNumber <= 0) {
            return;
        }

        if (waveNumber == 1) {
            queue.add(new SpawnEvent(BloonType.RED, 20, 0.5));
        } else if (waveNumber == 2) {
            queue.add(new SpawnEvent(BloonType.BLUE, 20, 0.4));
            queue.add(new SpawnEvent(BloonType.RED, 10, 0.3));
        } else if (waveNumber == 3) {
            queue.add(new SpawnEvent(BloonType.GREEN, 15, 0.4));
        } else if (waveNumber == 4) {
            queue.add(new SpawnEvent(BloonType.YELLOW, 10, 0.35));
        } else if (waveNumber == 5) {
            queue.add(new SpawnEvent(BloonType.CAMO, 5, 0.8));
            queue.add(new SpawnEvent(BloonType.YELLOW, 20, 0.3));
        } else {
            buildScaledWave(waveNumber);
        }
    }

    public void tick(double delta, GameState state) {
        if (delta <= 0) {
            return;
        }

        synchronized (this) {
            spawnTimer -= delta;

            while (spawnTimer <= 0) {
                if (activeEvent == null || activeRemaining <= 0) {
                    if (queue.isEmpty()) {
                        return;
                    }
                    activeEvent = queue.poll();
                    activeRemaining = activeEvent.count();
                }

                Enemy enemy = new BloonEnemy(activeEvent.type(), state.getMap().getWaypoints());
                state.getEnemies().add(enemy);

                activeRemaining--;
                spawnTimer += activeEvent.interval();

                if (activeRemaining <= 0) {
                    activeEvent = null;
                }
            }
        }
    }

    public boolean isComplete() {
        GameState state = GameState.getInstance();
        return queue.isEmpty() && (activeEvent == null || activeRemaining <= 0)
                && state.getEnemies().stream().noneMatch(e -> !e.isDead());
    }

    private void buildScaledWave(int waveNumber) {
        List<BloonType> tiers = List.of(BloonType.RED, BloonType.BLUE, BloonType.GREEN,
                BloonType.YELLOW, BloonType.PINK, BloonType.ZEBRA, BloonType.RAINBOW,
                BloonType.CERAMIC, BloonType.MOAB);

        int baseCount = 12 + waveNumber * 3;
        int tierIndex = Math.min(tiers.size() - 1, 1 + waveNumber / 3);
        int lowerTierIndex = Math.max(0, tierIndex - 1);

        double fastInterval = Math.max(0.12, 0.38 - waveNumber * 0.01);
        double slowInterval = Math.max(0.2, fastInterval + 0.08);

        queue.add(new SpawnEvent(tiers.get(lowerTierIndex), baseCount, fastInterval));
        queue.add(new SpawnEvent(tiers.get(tierIndex), Math.max(6, baseCount / 2), slowInterval));

        if (waveNumber % 5 == 0) {
            queue.add(new SpawnEvent(BloonType.MOAB, Math.max(1, waveNumber / 10), 1.2));
        }
    }
}
