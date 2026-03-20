package com.towerdefense.enemies;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BloonType {
    RED(1, 80, 1, null), BLUE(1, 100, 2, RED), GREEN(1, 120, 3, BLUE), YELLOW(1, 200, 4,
            GREEN), PINK(1, 250, 5, YELLOW), CAMO(1, 120, 6, GREEN, Resist.CAMO), LEAD(2, 60, 10,
                    PINK, Resist.LEAD), ZEBRA(1, 150, 11, null, Resist.FREEZE), RAINBOW(1, 175, 20,
                            ZEBRA), CERAMIC(10, 100, 50,
                                    RAINBOW), MOAB(200, 50, 200, null, Resist.LEAD);

    public enum Resist {
        CAMO, LEAD, FREEZE
    }

    private final int hp;
    private final double speedPxPerSecond;
    private final int reward;
    private final BloonType innerType;
    private final Set<Resist> resistances;

    BloonType(int hp, double speedPxPerSecond, int reward, BloonType innerType,
            Resist... resistances) {
        this.hp = hp;
        this.speedPxPerSecond = speedPxPerSecond;
        this.reward = reward;
        this.innerType = innerType;
        this.resistances =
                Collections.unmodifiableSet(resistances.length == 0 ? EnumSet.noneOf(Resist.class)
                        : EnumSet.of(resistances[0], resistances));
    }

    public int getHp() {
        return hp;
    }

    public double getSpeedPxPerSecond() {
        return speedPxPerSecond;
    }

    public int getReward() {
        return reward;
    }

    public BloonType getInnerType() {
        return innerType;
    }

    public Set<Resist> getResistances() {
        return resistances;
    }
}
