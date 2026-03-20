package com.towerdefense.towers.decorators;

import com.towerdefense.towers.Tower;

public class RapidFireDecorator extends TowerDecorator {
    public RapidFireDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public double getFireRate() {
        return wrapped.getFireRate() * 3.0;
    }

    @Override
    protected String decoratorLabel() {
        return "Rapid Fire";
    }

    @Override
    protected int ownCost() {
        return 350;
    }
}
