package com.sut.hollowknight.model.enemy;

/**
 * The Crystal Guardian's long-range laser beam.
 *
 * A single reusable instance lives inside {@link CrystalGuardian} — the beam
 * is (de)activated in place, so firing never allocates in the game loop.
 * The beam is a segment from the guardian's lamp toward the point where the
 * knight stood at fire time, cut short by the first terrain hit.
 */
public class Laser {

    public static final int   DAMAGE    = 1;
    /** Visual/damage thickness of the damaging beam core, in world px. */
    public static final float THICKNESS = 26f;
    /** Thickness of the harmless tracking beam shown while charging. */
    public static final float TRACKING_THICKNESS = 4f;
    /** "Long-range" per the spec — far beyond the detection range. */
    public static final float MAX_RANGE = 2600f;

    private boolean active;
    private float originX;
    private float originY;
    /** Normalized fire direction, locked at fire time. */
    private float dirX;
    private float dirY;
    /** Distance to the first terrain hit (or MAX_RANGE). */
    private float length;
    /** Cached at activation so the renderer never calls atan2 per frame. */
    private float angleDeg;
    /** Charging tracking beam: drawn thin and deals no damage. */
    private boolean harmless;

    /** Damaging beam — locked at fire time. */
    public void activate(float originX, float originY,
                         float dirX, float dirY, float length) {
        activate(originX, originY, dirX, dirY, length, false);
    }

    public void activate(float originX, float originY,
                         float dirX, float dirY, float length, boolean harmless) {
        this.originX = originX;
        this.originY = originY;
        this.dirX = dirX;
        this.dirY = dirY;
        this.length = length;
        this.angleDeg = (float) Math.toDegrees(Math.atan2(dirY, dirX));
        this.harmless = harmless;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
        this.harmless = false;
    }

    public boolean isActive()   { return active; }
    public boolean isHarmless() { return harmless; }
    public float getOriginX()  { return originX; }
    public float getOriginY()  { return originY; }
    public float getDirX()     { return dirX; }
    public float getDirY()     { return dirY; }
    public float getLength()   { return length; }
    public float getAngleDeg() { return angleDeg; }
    public float getEndX()     { return originX + dirX * length; }
    public float getEndY()     { return originY + dirY * length; }
}
