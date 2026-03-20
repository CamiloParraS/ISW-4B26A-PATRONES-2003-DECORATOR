package com.towerdefense.towers.decorators;

import com.towerdefense.towers.Tower;

public class PiercingDecorator extends TowerDecorator {
    public PiercingDecorator(Tower wrapped) {
        super(wrapped);
    }

    @Override
    public int getPierce() {
        return wrapped.getPierce() + 3;
    }

    @Override
    protected String decoratorLabel() {
        return "Piercing";
    }

    @Override
    protected int ownCost() {
        return 340;
    }
}
