package com.nightcity.tower;

/**
 * Wraps a Tower (possibly a full decorator stack) with the physical
 * grid slot it occupies and a per-tick fire cooldown counter.
 *
 * The {@code tower} field is mutable so that decorators can be installed
 * at runtime: {@code pt.tower = new SmartLinkDecorator(pt.tower);}
 */
public class PlacedTower {

    /** The live tower instance (may be wrapped in decorators). */
    public Tower tower;

    /** Fixed slot position on the 0-100 path (20, 40, 60, or 80). */
    public final int pos;

    /**
     * Ticks remaining before this tower may fire again.
     * Decremented each tick; a shot resets it to {@code ceil(1 / fireRate) - 1}.
     */
    public int ticksUntilFire = 0;

    public PlacedTower(Tower tower, int pos) {
        this.tower = tower;
        this.pos   = pos;
    }
}
