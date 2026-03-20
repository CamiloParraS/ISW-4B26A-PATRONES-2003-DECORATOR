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
            queue.add(new SpawnEvent(BloonType.RED, 18, 0.4));
        } else if (waveNumber == 2) {
            queue.add(new SpawnEvent(BloonType.BLUE, 18, 0.34));
            queue.add(new SpawnEvent(BloonType.RED, 14, 0.24));
        } else if (waveNumber == 3) {
            queue.add(new SpawnEvent(BloonType.GREEN, 14, 0.33));
            queue.add(new SpawnEvent(BloonType.BLUE, 10, 0.24));
        } else if (waveNumber == 4) {
            queue.add(new SpawnEvent(BloonType.YELLOW, 12, 0.3));
            queue.add(new SpawnEvent(BloonType.GREEN, 10, 0.24));
        } else if (waveNumber == 5) {
            queue.add(new SpawnEvent(BloonType.CAMO, 6, 0.58));
            queue.add(new SpawnEvent(BloonType.YELLOW, 18, 0.24));
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

        int baseCount = 10 + waveNumber * 3;
        int tierIndex = Math.min(tiers.size() - 1, 1 + waveNumber / 3);
        int lowerTierIndex = Math.max(0, tierIndex - 1);

        double fastInterval = Math.max(0.1, 0.34 - waveNumber * 0.01);
        double slowInterval = Math.max(0.16, fastInterval + 0.06);

        queue.add(new SpawnEvent(tiers.get(lowerTierIndex), baseCount, fastInterval));
        queue.add(new SpawnEvent(tiers.get(tierIndex), Math.max(6, baseCount / 2), slowInterval));

        if (waveNumber % 5 == 0) {
            queue.add(new SpawnEvent(BloonType.MOAB, Math.max(1, waveNumber / 12), 1.0));
        }
    }
}
