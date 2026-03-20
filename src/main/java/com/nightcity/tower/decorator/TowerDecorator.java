package com.nightcity.tower.decorator;

import com.nightcity.model.Enemy;
import com.nightcity.tower.Tower;

/**
 * Base class for all tower decorators.
 *
 * By default every method delegates to {@code inner}, so concrete
 * decorators only need to override the method(s) they actually change.
 * This is the classic Decorator pattern: wrap, override, delegate.
 */
public abstract class TowerDecorator implements Tower {

    protected final Tower inner;

    protected TowerDecorator(Tower inner) {
        this.inner = inner;
    }

    @Override public double getDamage()      { return inner.getDamage(); }
    @Override public double getRange()       { return inner.getRange(); }
    @Override public double getFireRate()    { return inner.getFireRate(); }
    @Override public String getDescription() { return inner.getDescription(); }
    @Override public String shoot(Enemy e)   { return inner.shoot(e); }
}
