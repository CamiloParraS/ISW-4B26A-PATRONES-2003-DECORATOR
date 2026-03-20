package com.nightcity.model;

/**
 * Represents an enemy unit moving along the path from position 0 → 100.
 */
public class Enemy {

    public String  name;
    public double  hp;
    public double  maxHp;
    public double  position;
    public double  speed;
    public boolean alive = true;

    public Enemy(String name, double hp, double speed) {
        this.name  = name;
        this.hp    = hp;
        this.maxHp = hp;
        this.speed = speed;
    }

    public void takeDamage(double amount) {
        hp -= amount;
        if (hp <= 0) {
            hp    = 0;
            alive = false;
        }
    }

    /** One-line status string used in both console and HTML output. */
    public String status() {
        return String.format("%-30s  HP:%5.0f/%-5.0f  pos:%5.1f",
                name, hp, maxHp, position);
    }
}
