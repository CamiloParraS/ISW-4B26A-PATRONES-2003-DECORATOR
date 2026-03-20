package com.towerdefense.engine;

import com.towerdefense.dto.EnemyDto;
import com.towerdefense.dto.GameStateDto;
import com.towerdefense.dto.ProjectileDto;
import com.towerdefense.dto.TowerDto;
import com.towerdefense.enemies.Enemy;
import com.towerdefense.map.GameMap;
import com.towerdefense.projectiles.Projectile;
import com.towerdefense.projectiles.ProjectileType;
import com.towerdefense.towers.Tower;
import com.towerdefense.towers.UpgradeRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private final Set<String> camoGrantedIds = new HashSet<>();
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

    public synchronized boolean trySpendCoins(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (coins < amount) {
            return false;
        }
        coins -= amount;
        return true;
    }

    public synchronized void addCoins(int amount) {
        if (amount > 0) {
            coins += amount;
        }
    }

    public void spawnProjectile(Tower source, Enemy target) {
        Projectile p = new Projectile(UUID.randomUUID().toString(),
                ProjectileType.valueOf(source.getProjectileType().toUpperCase()),
                source.getCenterPx(), source.getCenterPy(), target.getPx(), target.getPy(),
                source.getDamage(), source.getPierce());
        projectiles.add(p);
    }

    public void addTower(Tower tower) {
        towers.add(tower);
    }

    public boolean removeTower(String id) {
        return towers.removeIf(t -> t.getId().equals(id));
    }

    public boolean replaceTower(String id, Tower upgradedTower) {
        for (int i = 0; i < towers.size(); i++) {
            if (towers.get(i).getId().equals(id)) {
                towers.set(i, upgradedTower);
                return true;
            }
        }
        return false;
    }

    public Tower getTowerAt(int gx, int gy) {
        return towers.stream().filter(t -> t.getGridX() == gx && t.getGridY() == gy).findFirst()
                .orElse(null);
    }

    public Tower getTowerById(String id) {
        return towers.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public void cleanup() {
        enemies.removeIf(Enemy::isDead);
        projectiles.removeIf(Projectile::isDead);
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

        List<TowerDto> towerDtos = towers.stream()
                .map(t -> new TowerDto(t.getId(), t.getType(), t.getGridX(), t.getGridY(),
                        t.getRange(), t.getFireRate(), t.getUpgradeLabels(),
                        UpgradeRegistry.available(t), t.getSellValue()))
                .collect(Collectors.toList());

        List<ProjectileDto> projectileDtos =
                projectiles
                        .stream().map(p -> new ProjectileDto(p.getId(),
                                p.getType().name().toLowerCase(), p.getPx(), p.getPy()))
                        .collect(Collectors.toList());

        return new GameStateDto(wave, lives, coins, phase.name(), enemyDtos, towerDtos,
                projectileDtos);
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

    public void clearCamoGrantedIds() {
        camoGrantedIds.clear();
    }

    public void grantCamoTargeting(String towerId) {
        if (towerId != null && !towerId.isBlank()) {
            camoGrantedIds.add(towerId);
        }
    }

    public boolean hasCamoTargeting(String towerId) {
        return camoGrantedIds.contains(towerId);
    }
}
