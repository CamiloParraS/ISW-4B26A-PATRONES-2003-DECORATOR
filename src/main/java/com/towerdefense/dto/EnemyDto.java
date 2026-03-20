package com.towerdefense.dto;

public class EnemyDto {
    private final String id;
    private final String type;
    private final double px;
    private final double py;
    private final int hp;
    private final int maxHp;
    private final double slowFactor;

    public EnemyDto(String id, String type, double px, double py, int hp, int maxHp,
            double slowFactor) {
        this.id = id;
        this.type = type;
        this.px = px;
        this.py = py;
        this.hp = hp;
        this.maxHp = maxHp;
        this.slowFactor = slowFactor;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder(160);
        sb.append('{');
        sb.append("\"id\":\"").append(escape(id)).append("\",");
        sb.append("\"type\":\"").append(escape(type)).append("\",");
        sb.append("\"px\":").append(px).append(',');
        sb.append("\"py\":").append(py).append(',');
        sb.append("\"hp\":").append(hp).append(',');
        sb.append("\"maxHp\":").append(maxHp).append(',');
        sb.append("\"slowFactor\":").append(slowFactor);
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
