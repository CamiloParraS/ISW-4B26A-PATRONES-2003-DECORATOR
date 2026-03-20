package com.towerdefense.engine;

import com.towerdefense.server.SseHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameLoop {
    private ScheduledExecutorService scheduler;
    private long lastTick;

    public synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-loop");
            t.setDaemon(true);
            return t;
        });
        lastTick = System.nanoTime();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void tick() {
        double delta = computeDelta();
        GameState state = GameState.getInstance();
        state.drainCommands();
        if (!state.isRunning()) {
            SseHandler.broadcast(state.toDto().toJson());
            return;
        }

        state.getWaveManager().tick(delta, state);
        state.getEnemies().forEach(e -> e.tick(delta, state.getMap().getWaypoints()));
        state.cleanup();
        SseHandler.broadcast(state.toDto().toJson());
    }

    private double computeDelta() {
        long now = System.nanoTime();
        long elapsed = now - lastTick;
        lastTick = now;
        return elapsed / 1_000_000_000.0;
    }
}
