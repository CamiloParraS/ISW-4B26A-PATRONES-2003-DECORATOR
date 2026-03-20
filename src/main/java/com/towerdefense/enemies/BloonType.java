package com.towerdefense.enemies;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum BloonType {
    RED(1, 76, 3, null), BLUE(1, 94, 6, RED), GREEN(1, 112, 9, BLUE), YELLOW(1, 178, 12,
            GREEN), PINK(1, 225, 15, YELLOW), CAMO(1, 126, 21, GREEN, Resist.CAMO), LEAD(2, 68, 36,
                    PINK, Resist.LEAD), ZEBRA(1, 158, 39, null, Resist.FREEZE), RAINBOW(1, 182, 66,
                            ZEBRA), CERAMIC(9, 108, 180,
                                    RAINBOW), MOAB(170, 56, 720, null, Resist.LEAD);

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
