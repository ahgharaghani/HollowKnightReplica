package com.sut.hollowknight.model.npc;

import com.sut.hollowknight.model.collision.CollisionRect;

public class Zote {

    public enum State { IDLE, TALK, ANGRY }

    /** Source frames are 349x186; rendered at native size (1:1). */
    public static final float SCALE  = 1f;
    public static final float WIDTH  = 349f * SCALE;
    public static final float HEIGHT = 186f * SCALE;

    private static final float BODY_WIDTH  = WIDTH * 0.24f;
    private static final float BODY_HEIGHT = HEIGHT * 0.74f;

    // ---- Tantrum (spec: 4 back-and-forths, ends at initial position) ----
    public static final int   ANGRY_BOUNCES   = 4;
    public static final float ANGRY_AMPLITUDE = 120f;
    /** Slow, deliberate pace (px/s). */
    public static final float ANGRY_SPEED     = 110f;

    private final float initialX;
    private final float y;
    private float x;
    private boolean facingRight;
    private State state = State.IDLE;

    private float animTimer;   // drives the animation loops in the renderer

    // Tantrum sweep bookkeeping
    private float sweepDir;      // -1 or +1: current travel direction
    private float sweepTarget;   // x he is currently marching toward
    private int   bouncesLeft;   // turn-arounds left before heading home
    private boolean headingHome; // final leg back to initialX

    /** Dedup id so a single Knight swing lands at most one hit. */
    private int lastNailHitId = Integer.MIN_VALUE;

    // Reusable mutable - never allocated in the game loop.
    private final CollisionRect bodyBox = new CollisionRect(0, 0, 0, 0);

    public Zote(float x, float y) {
        this.initialX = x;
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        animTimer += delta;
        if (state != State.ANGRY) return;

        float step = ANGRY_SPEED * delta;
        float remaining = sweepTarget - x;
        if (Math.abs(remaining) <= step) {
            x = sweepTarget; // arrived at this leg's end point
            if (headingHome) {
                settleDown();
            } else {
                bounce();
            }
        } else {
            x += (remaining > 0f ? step : -step);
            facingRight = remaining > 0f; // face the travel direction
        }
    }

    /** Turn around at a sweep edge; head home once the bounces are spent. */
    private void bounce() {
        bouncesLeft--;
        sweepDir = -sweepDir;
        if (bouncesLeft <= 0) {
            headingHome = true;
            sweepTarget = initialX;
            if (x == initialX) settleDown(); // already home - done
        } else {
            sweepTarget = initialX + sweepDir * ANGRY_AMPLITUDE;
        }
        facingRight = sweepTarget > x;
    }

    private void settleDown() {
        x = initialX; // always settle exactly where he began
        state = State.IDLE;
        animTimer = 0f;
    }

    /**
     * The Knight struck him: rage, harmlessly.
     *
     * @param moveRight true to charge rightward first - the controller
     *                  passes the direction opposite the Knight.
     */
    public void beginTantrum(boolean moveRight) {
        if (state == State.ANGRY) return; // spec: hits mid-tantrum do nothing
        state = State.ANGRY;
        animTimer = 0f; // restart the Attack loop on its first frame
        headingHome = false;
        bouncesLeft = ANGRY_BOUNCES;
        sweepDir = moveRight ? 1f : -1f;
        sweepTarget = initialX + sweepDir * ANGRY_AMPLITUDE;
        facingRight = moveRight;
    }

    /** He shoved the Knight: turn and charge the other way (spec). */
    public void reverseSweep() {
        if (state != State.ANGRY || headingHome) return;
        sweepDir = -sweepDir;
        sweepTarget = initialX + sweepDir * ANGRY_AMPLITUDE;
        facingRight = sweepTarget > x;
    }

    /** Body box for nail-hit checks (recomputed into the reusable rect). */
    public CollisionRect getBodyBox() {
        return bodyBox.set(x - BODY_WIDTH / 2f, y, BODY_WIDTH, BODY_HEIGHT);
    }

    public float   getX()             { return x; }
    public float   getY()             { return y; }
    public float   getInitialX()      { return initialX; }
    public State   getState()         { return state; }
    public boolean isAngry()          { return state == State.ANGRY; }
    public boolean isFacingRight()    { return facingRight; }
    public float   getAnimTimer()     { return animTimer; }
    public int     getLastNailHitId() { return lastNailHitId; }

    public void setFacingRight(boolean right) { this.facingRight = right; }
    public void setLastNailHitId(int id)      { this.lastNailHitId = id; }

    /** IDLE <-> TALK only; the tantrum owns the ANGRY state. */
    public void setTalking(boolean talking) {
        if (state == State.ANGRY) return;
        State next = talking ? State.TALK : State.IDLE;
        if (state != next) {
            state = next;
            animTimer = 0f;
        }
    }
}
