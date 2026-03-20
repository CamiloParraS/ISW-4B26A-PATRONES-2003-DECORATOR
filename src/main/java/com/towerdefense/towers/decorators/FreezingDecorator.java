package com.towerdefense.towers.decorators;

import com.towerdefense.towers.Tower;

public class FreezingDecorator extends TowerDecorator {
    public FreezingDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public String getProjectileType() {
        return "freeze";
    }

    @Override
    public int getPierce() {
        return wrapped.getPierce() + 2;
    }

    @Override
    protected String decoratorLabel() {
        return "Freezing";
    }

    @Override
    protected int ownCost() {
        return 380;
    }
}
