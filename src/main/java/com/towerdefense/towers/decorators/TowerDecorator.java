package com.towerdefense.towers.decorators;

import com.towerdefense.enemies.BloonType;
import com.towerdefense.enemies.Enemy;
import com.towerdefense.engine.GameState;
import com.towerdefense.towers.Tower;
import java.util.ArrayList;
import java.util.List;

public abstract class TowerDecorator implements Tower {
    protected final Tower wrapped;

    protected TowerDecorator(Tower wrapped) {
        this.wrapped = wrapped;
    }

    public final Tower getWrapped() {
        return wrapped;
    }

    @Override
    public String getId() {
        return wrapped.getId();
    }

    @Override
    public String getType() {
        return wrapped.getType();
    }

    @Override
    public int getGridX() {
        return wrapped.getGridX();
    }

    @Override
    public int getGridY() {
        return wrapped.getGridY();
    }

    @Override
    public double getCenterPx() {
        return wrapped.getCenterPx();
    }

    @Override
    public double getCenterPy() {
        return wrapped.getCenterPy();
    }

    @Override
    public double getRange() {
        return wrapped.getRange();
    }

    @Override
    public double getFireRate() {
        return wrapped.getFireRate();
    }

    @Override
    public int getDamage() {
        return wrapped.getDamage();
    }

    @Override
    public int getPierce() {
        return wrapped.getPierce();
    }

    @Override
    public boolean canTargetCamo() {
        return wrapped.canTargetCamo();
    }

    @Override
    public boolean canTargetLead() {
        return wrapped.canTargetLead();
    }

    @Override
    public String getProjectileType() {
        return wrapped.getProjectileType();
    }

    @Override
    public List<String> getUpgradeLabels() {
        List<String> list = new ArrayList<>(wrapped.getUpgradeLabels());
        list.add(decoratorLabel());
        return list;
    }

    @Override
    public int getTotalCost() {
        return wrapped.getTotalCost() + ownCost();
    }

    @Override
    public int getSellValue() {
        return (int) ((wrapped.getSellValue() / 0.7 + ownCost()) * 0.7);
    }

    @Override
    public void onTick(double delta, List<Enemy> enemies, GameState state, Tower self) {
        wrapped.onTick(delta, enemies, state, self);
    }

    protected boolean canHitEnemy(Enemy e, GameState state) {
        boolean hasCamoTargeting = canTargetCamo() || state.hasCamoTargeting(getId());
        if (!hasCamoTargeting && e.getType().getResistances().contains(BloonType.Resist.CAMO)) {
            return false;
        }
        return canTargetLead() || !e.getType().getResistances().contains(BloonType.Resist.LEAD);
    }

    protected abstract String decoratorLabel();

    protected abstract int ownCost();
}
