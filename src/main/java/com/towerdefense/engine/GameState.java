package com.towerdefense.engine;

import com.towerdefense.dto.EnemyDto;
import com.towerdefense.dto.GameStateDto;
import com.towerdefense.enemies.Enemy;
import com.towerdefense.map.GameMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class GameState {
    public enum Phase {
        PREP, WAVE, GAME_OVER
    }

    private static final GameState INSTANCE = new GameState();

    private volatile boolean running = false;
    private int lives = 100;
    private int coins = 650;
    private int wave = 0;
    private volatile Phase phase = Phase.PREP;

    private final GameMap map = GameMap.createDefault();
    private final WaveManager waveManager = new WaveManager();

    private final List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private final List<Tower> towers = new CopyOnWriteArrayList<>();
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Runnable> commandQueue = new LinkedBlockingQueue<>();

    private GameState() {}

    public static GameState getInstance() {
        return INSTANCE;
    }

    public void enqueue(Runnable cmd) {
        if (cmd != null) {
            commandQueue.offer(cmd);
        }
    }

    public void drainCommands() {
        Runnable cmd;
        while ((cmd = commandQueue.poll()) != null) {
            try {
                cmd.run();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void loseLife() {
        if (phase == Phase.GAME_OVER) {
            return;
        }

        lives = Math.max(0, lives - 1);
        if (lives == 0) {
            phase = Phase.GAME_OVER;
            running = false;
        }
    }

    public synchronized int nextWave() {
        wave += 1;
        phase = Phase.WAVE;
        return wave;
    }

    public void cleanup() {
        enemies.removeIf(Enemy::isDead);
        if (phase == Phase.WAVE && waveManager.isComplete()) {
            phase = Phase.PREP;
            running = false;
        }
    }

    public GameStateDto toDto() {
        List<EnemyDto> enemyDtos =
                enemies.stream()
                        .map(e -> new EnemyDto(e.getId(), e.getType().name(), e.getPx(), e.getPy(),
                                e.getHp(), e.getMaxHp(), e.getSlowFactor()))
                        .collect(Collectors.toList());

        return new GameStateDto(wave, lives, coins, phase.name(), enemyDtos, List.of(), List.of());
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (phase != Phase.GAME_OVER) {
            phase = running ? Phase.WAVE : Phase.PREP;
        }
    }

    public int getLives() {
        return lives;
    }

    public int getCoins() {
        return coins;
    }

    public int getWave() {
        return wave;
    }

    public Phase getPhase() {
        return phase;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public GameMap getMap() {
        return map;
    }
}
