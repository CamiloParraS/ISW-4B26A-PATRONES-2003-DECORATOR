package com.towerdefense.dto;

public class ProjectileDto {
    private final String id;
    private final String type;
    private final double px;
    private final double py;

    public ProjectileDto(String id, String type, double px, double py) {
        this.id = id;
        this.type = type;
        this.px = px;
        this.py = py;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder(96);
        sb.append('{');
        sb.append("\"id\":\"").append(escape(id)).append("\",");
        sb.append("\"type\":\"").append(escape(type)).append("\",");
        sb.append("\"px\":").append(px).append(',');
        sb.append("\"py\":").append(py);
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
