package com.towerdefense.towers.decorators;

import com.towerdefense.enemies.Enemy;
import com.towerdefense.engine.GameState;
import com.towerdefense.towers.Tower;
import java.util.List;

public class CamoDetectorDecorator extends TowerDecorator {
    private static final double AURA_RANGE = 100.0;

    public CamoDetectorDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public boolean canTargetCamo() {
        return true;
    }

    @Override
    public void onTick(double delta, List<Enemy> enemies, GameState state) {
        state.grantCamoTargeting(getId());
        state.getTowers().forEach(t -> {
            double dx = t.getCenterPx() - getCenterPx();
            double dy = t.getCenterPy() - getCenterPy();
            if (Math.hypot(dx, dy) <= AURA_RANGE) {
                state.grantCamoTargeting(t.getId());
            }
        });
        wrapped.onTick(delta, enemies, state);
    }

    @Override
    protected String decoratorLabel() {
        return "Camo Detector";
    }

    @Override
    protected int ownCost() {
        return 300;
    }
}
