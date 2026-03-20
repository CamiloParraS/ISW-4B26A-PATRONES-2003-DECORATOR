package com.towerdefense.towers.decorators;

import com.towerdefense.towers.Tower;

public class LaserDecorator extends TowerDecorator {
    public LaserDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public String getProjectileType() {
        return "laser";
    }

    @Override
    public double getFireRate() {
        return wrapped.getFireRate() * 1.5;
    }

    @Override
    public boolean canTargetLead() {
        return true;
    }

    @Override
    protected String decoratorLabel() {
        return "Laser";
    }

    @Override
    protected int ownCost() {
        return 620;
    }
}
