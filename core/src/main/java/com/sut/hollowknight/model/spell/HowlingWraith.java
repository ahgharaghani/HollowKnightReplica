package com.sut.hollowknight.model.spell;

import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * Howling Wraiths: a stationary upward blast of magic anchored where the
 * knight screamed. Unlike a projectile it does not travel; it lingers for a
 * short time and deals three rapid, consecutive damage ticks to anything
 * inside its hitbox. Built to punish airborne and diving enemies.
 */
public class HowlingWraith {

    /** Lifetime of the blast (Scream plume: 13 frames @ 20 FPS). */
    public static final float DURATION = 13f / 20f;

    // Three rapid ticks spread across the active window (all well inside
    // DURATION: 0.05s, 0.23s, 0.41s of a 0.65s effect).
    public static final int   TICK_COUNT    = 3;
    public static final float FIRST_TICK_AT = 0.05f;
    public static final float TICK_INTERVAL = 0.18f;

    /** Damage per tick — a full scream is worth three nail hits. Tune here. */
    public static final int DAMAGE_PER_TICK = 1;

    // Hitbox: a wide column above the knight, tuned to the plume art
    // (drawn ~300x277 rising from the feet). The bottom is lifted off the
    // ground so it specifically covers enemies at and above head height.
    public static final float DMG_WIDTH         = 300f;
    public static final float DMG_HEIGHT        = 277f;
    public static final float DMG_BOTTOM_OFFSET = 30f;

    // Fixed anchor: per the spec the wraiths stay exactly where they erupted.
    private final float anchorX;      // knight center x at release
    private final float anchorY;      // knight feet y at release
    private final boolean facingRight;

    private float age;        // total time since release; drives everything
    private int   ticksFired; // how many damage ticks have been consumed

    /** Reusable rect so the per-frame combat loop never allocates. */
    private final CollisionRect damageBox = new CollisionRect(0, 0, DMG_WIDTH, DMG_HEIGHT);

    /** Cast with Void Heart equipped - the view swaps to the shadow art set. */
    private final boolean shadow;

    public HowlingWraith(float anchorX, float anchorY, boolean facingRight, boolean shadow) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.facingRight = facingRight;
        this.shadow = shadow;
    }

    public void addAge(float delta) {
        this.age += delta;
    }

    /**
     * Consumes one pending damage tick, if due. Call in a while-loop so a
     * single long frame cannot swallow ticks (each returns true at most
     * TICK_COUNT times over the blast's life).
     */
    public boolean pollTick() {
        if (ticksFired >= TICK_COUNT) return false;
        float due = FIRST_TICK_AT + ticksFired * TICK_INTERVAL;
        if (age < due) return false;
        ticksFired++;
        return true;
    }

    public boolean isDone() {
        return age >= DURATION;
    }

    /** The blast's damage area, refreshed in place (no allocation). */
    public CollisionRect getDamageBox() {
        return damageBox.set(anchorX - DMG_WIDTH / 2f, anchorY + DMG_BOTTOM_OFFSET,
            DMG_WIDTH, DMG_HEIGHT);
    }

    public float getAnchorX()      { return anchorX; }
    public float getAnchorY()      { return anchorY; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isShadow()      { return shadow; }
    public float getAge()          { return age; }
}
