package com.sut.hollowknight.model.map;

import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * The boss arena gate (spec: the arena seals when the fight starts).
 *
 * The Gate TILE layer holds the gate art in its raised (open) position; the
 * Gate OBJECT layer masks that art with one rectangle. This model owns the
 * rectangle, the drop distance to the arena floor, and the current offset.
 * The view moves the tile layer by the offset; the controller (GameScreen)
 * registers/unregisters the solid rect with the TileMapCollider.
 */
public class BossGate {

    public enum State { OPEN, CLOSING, SEALED, OPENING }

    /** The gate slams shut fast, but rises slowly when the fight ends. */
    public static final float CLOSE_SPEED = 620f;
    public static final float OPEN_SPEED  = 200f;

    /** The seal triggers once the knight is safely this far past the gate. */
    public static final float TRIGGER_MARGIN = 60f;

    private final CollisionRect gateRect;   // authored (raised) position
    /** Live collision rect while sealed — registered with the collider. */
    private final CollisionRect solidRect;
    private final float dropDistance;

    private State state = State.OPEN;
    private float offset; // 0 = fully raised .. dropDistance = fully sealed

    /**
     * @param gateRect the mask rectangle from the Gate object layer (world, y-up)
     * @param floorY   the arena floor top the gate must reach when sealed
     */
    public BossGate(CollisionRect gateRect, float floorY) {
        this.gateRect = gateRect;
        this.dropDistance = Math.max(0f, gateRect.getBottom() - floorY);
        this.solidRect = new CollisionRect(
            gateRect.getX(), gateRect.getY(), gateRect.getWidth(), gateRect.getHeight());
    }

    public State getState()          { return state; }
    public void setState(State s)    { this.state = s; }
    public float getOffset()         { return offset; }
    public float getDropDistance()   { return dropDistance; }
    public CollisionRect getGateRect() { return gateRect; }

    /** X the knight must cross (moving left, into the arena) to trigger the seal. */
    public float getTriggerX() { return gateRect.getLeft() - TRIGGER_MARGIN; }

    /** Advances the drop. @return true when it just reached fully sealed. */
    public boolean advanceClose(float delta) {
        offset += CLOSE_SPEED * delta;
        if (offset >= dropDistance) {
            offset = dropDistance;
            return true;
        }
        return false;
    }

    /** Raises the gate. @return true when it just reached fully open. */
    public boolean advanceOpen(float delta) {
        offset -= OPEN_SPEED * delta;
        if (offset <= 0f) {
            offset = 0f;
            return true;
        }
        return false;
    }

    /** The collision rect at the CURRENT dropped position. */
    public CollisionRect getSolidRect() {
        return solidRect.set(gateRect.getX(), gateRect.getY() - offset,
            gateRect.getWidth(), gateRect.getHeight());
    }
}
