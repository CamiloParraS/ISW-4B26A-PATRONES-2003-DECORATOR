package com.towerdefense.dto;

import java.util.List;

public class TowerDto {
    private final String id;
    private final String type;
    private final int gx;
    private final int gy;
    private final double range;
    private final List<String> upgrades;
    private final int sellValue;

    public TowerDto(String id, String type, int gx, int gy, double range, List<String> upgrades,
            int sellValue) {
        this.id = id;
        this.type = type;
        this.gx = gx;
        this.gy = gy;
        this.range = range;
        this.upgrades = upgrades;
        this.sellValue = sellValue;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder(160);
        sb.append('{');
        sb.append("\"id\":\"").append(escape(id)).append("\",");
        sb.append("\"type\":\"").append(escape(type)).append("\",");
        sb.append("\"gx\":").append(gx).append(',');
        sb.append("\"gy\":").append(gy).append(',');
        sb.append("\"range\":").append(range).append(',');
        sb.append("\"upgrades\":[");
        for (int i = 0; i < upgrades.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(upgrades.get(i))).append('"');
        }
        sb.append("],");
        sb.append("\"sellValue\":").append(sellValue);
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
