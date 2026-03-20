package com.towerdefense.towers.decorators;

import com.towerdefense.towers.Tower;

public class ExplosiveDecorator extends TowerDecorator {
    public ExplosiveDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public String getProjectileType() {
        return "bomb";
    }

    @Override
    public int getDamage() {
        return wrapped.getDamage() + 1;
    }

    @Override
    protected String decoratorLabel() {
        return "Explosive";
    }

    @Override
    protected int ownCost() {
        return 600;
    }
}
