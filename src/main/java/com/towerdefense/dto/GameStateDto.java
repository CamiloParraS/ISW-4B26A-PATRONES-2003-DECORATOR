package com.towerdefense.dto;

import java.util.List;

public class GameStateDto {
    private final int wave;
    private final int lives;
    private final int coins;
    private final String phase;
    private final List<EnemyDto> enemies;
    private final List<String> towers;
    private final List<String> projectiles;

    public GameStateDto(int wave, int lives, int coins, String phase, List<EnemyDto> enemies,
            List<String> towers, List<String> projectiles) {
        this.wave = wave;
        this.lives = lives;
        this.coins = coins;
        this.phase = phase;
        this.enemies = enemies;
        this.towers = towers;
        this.projectiles = projectiles;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append('{');
        sb.append("\"wave\":").append(wave).append(',');
        sb.append("\"lives\":").append(lives).append(',');
        sb.append("\"coins\":").append(coins).append(',');
        sb.append("\"phase\":\"").append(escape(phase)).append("\",");

        sb.append("\"enemies\":[");
        for (int i = 0; i < enemies.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(enemies.get(i).toJson());
        }
        sb.append("],");

        sb.append("\"towers\":[");
        for (int i = 0; i < towers.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(towers.get(i))).append('"');
        }
        sb.append("],");

        sb.append("\"projectiles\":[");
        for (int i = 0; i < projectiles.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(projectiles.get(i))).append('"');
        }
        sb.append(']');

        sb.append('}');
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
