package com.sut.hollowknight.model.map;

import com.sut.hollowknight.model.collision.AABB;

/**
 * A nail-breakable wall authored in Tiled ("BreakableWallColliders" object
 * layer): a rectangle in world units (y-up) plus hit points and a debris
 * flag (spec: Breakable Walls & Secret Areas).
 *
 * <p>Pure state - no rendering, no physics. Each nail hit rewinds the
 * shake timer; the view reads {@link #getShakeStrength()} to wobble the
 * wall art. Once hp reaches zero the wall is broken for good.</p>
 */
public class BreakableWall implements AABB {

    /** How long one hit's shake lasts, in seconds. */
    public static final float SHAKE_DURATION = 0.3f;

    private final String name;
    private final float x, y, width, height;   // world units, y-up
    private final boolean debris;

    private int hp;
    private boolean broken;
    private float shakeTimer;
    private int lastNailHitId = -1;   // de-dupes multi-frame slash overlap

    public BreakableWall(String name, float x, float y,
                         float width, float height, int hp, boolean debris) {
        this.name = name == null ? "" : name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hp = Math.max(1, hp);
        this.debris = debris;
    }

    /** Apply one nail hit. @return true when this hit broke the wall. */
    public boolean hit() {
        if (broken) return false;
        hp--;
        shakeTimer = SHAKE_DURATION;
        if (hp <= 0) {
            broken = true;
            shakeTimer = 0f;
        }
        return broken;
    }

    /** Restore a wall already broken earlier this session - no shake, no
     *  particles, no camera kick (spec: state survives room re-entry). */
    public void markBrokenSilently() {
        hp = 0;
        broken = true;
        shakeTimer = 0f;
    }

    public void tick(float delta) {
        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            if (shakeTimer < 0f) shakeTimer = 0f;
        }
    }

    /** 1 right after a hit, decaying linearly to 0. */
    public float getShakeStrength() {
        return broken ? 0f : shakeTimer / SHAKE_DURATION;
    }

    public String getName()             { return name; }
    public boolean isBroken()           { return broken; }
    public boolean hasDebris()          { return debris; }
    public int getHp()                  { return hp; }
    public int getLastNailHitId()       { return lastNailHitId; }
    public void setLastNailHitId(int id) { lastNailHitId = id; }

    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }

    @Override public float getLeft()   { return x; }
    @Override public float getRight()  { return x + width; }
    @Override public float getBottom() { return y; }
    @Override public float getTop()    { return y + height; }
}
