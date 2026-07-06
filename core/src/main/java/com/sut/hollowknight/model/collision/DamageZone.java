package com.sut.hollowknight.model.collision;

public class DamageZone {

    private final CollisionRect rect;
    private final boolean hasSafeSpot;
    private final float safeX;
    private final float safeY;

    public DamageZone(CollisionRect rect) {
        this.rect = rect;
        this.hasSafeSpot = false;
        this.safeX = 0f;
        this.safeY = 0f;
    }

    public DamageZone(CollisionRect rect, float safeX, float safeY) {
        this.rect = rect;
        this.hasSafeSpot = true;
        this.safeX = safeX;
        this.safeY = safeY;
    }

    public CollisionRect getRect()  { return rect; }
    public boolean hasSafeSpot()    { return hasSafeSpot; }
    public float getSafeX()         { return safeX; }
    public float getSafeY()         { return safeY; }
}
