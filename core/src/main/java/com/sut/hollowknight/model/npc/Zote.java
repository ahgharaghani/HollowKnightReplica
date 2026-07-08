package com.sut.hollowknight.model.npc;

import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * Zote the Mighty - a harmless NPC (spec: NPC Interaction - Zote).
 *
 * <p>Pure model: position, facing, animation clock and the "tantrum"
 * sweep that plays when the Knight strikes him. The sweep follows a
 * sine curve, so he passes back and forth exactly {@link #ANGRY_CYCLES}
 * times and always settles precisely on his initial position.</p>
 */
public class Zote {

    public enum State { IDLE, TALK, ANGRY }

    /** Source frames are 349x186; rendered at native size (1:1). */
    public static final float SCALE  = 1f;
    public static final float WIDTH  = 349f * SCALE;
    public static final float HEIGHT = 186f * SCALE;

    // The hittable body is much narrower than the padded art frame.
    private static final float BODY_WIDTH  = WIDTH * 0.42f;
    private static final float BODY_HEIGHT = HEIGHT * 0.96f;

    // ---- Tantrum (spec: 4 back-and-forths, ends at initial position) ----
    public static final int   ANGRY_CYCLES    = 4;
    public static final float ANGRY_DURATION  = 3.6f;
    public static final float ANGRY_AMPLITUDE = 120f;

    private final float initialX;
    private final float y;
    private float x;
    private boolean facingRight;
    private State state = State.IDLE;

    private float animTimer;   // drives the 12 FPS loops in the renderer
    private float angryTimer;  // elapsed time of the current tantrum

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
        if (state == State.ANGRY) {
            angryTimer += delta;
            if (angryTimer >= ANGRY_DURATION) {
                x = initialX;              // settle exactly where he began
                state = State.IDLE;
                animTimer = 0f;
            } else {
                // x(t) = x0 + A*sin(2*pi*cycles*t/T): starts and ends at x0.
                float phase = (float) (2.0 * Math.PI * ANGRY_CYCLES
                        * (angryTimer / ANGRY_DURATION));
                x = initialX + ANGRY_AMPLITUDE * (float) Math.sin(phase);
                facingRight = Math.cos(phase) >= 0.0; // face travel direction
            }
        }
    }

    /** The Knight struck him: rage (harmlessly), then settle back down. */
    public void beginTantrum() {
        if (state == State.ANGRY) return; // spec: hits mid-tantrum do nothing
        state = State.ANGRY;
        angryTimer = 0f;
        animTimer = 0f; // restart the Attack loop on its first frame
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
