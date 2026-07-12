package com.sut.hollowknight.model.spell;

import com.badlogic.gdx.utils.IntSet;
import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * The Vengeful Spirit projectile: a magic fireball fired horizontally in the
 * knight's facing direction. It moves at constant speed, ignores gravity,
 * passes THROUGH enemies (dealing damage along the way) and is destroyed the
 * instant it meets an environmental wall.
 *
 * <p>Lifecycle mirrors {@link com.sut.hollowknight.model.enemy.Javelin}:
 * <ul>
 *   <li>{@link State#FLYING} — the ball travels and its damage box is live.</li>
 *   <li>{@link State#IMPACT} — a wall was hit; the ball plays its dissipation
 *       (Ball End) while the wall shows its own impact burst. No damage.</li>
 *   <li>{@link State#DONE}   — fully faded; the controller drops it.</li>
 * </ul>
 */
public class VengefulSpirit {

    // ---- Position & physics ----
    private float x;                 // center x
    private float y;                 // center y
    private final float velocityX;
    private boolean facingRight;

    /** Constant horizontal speed; gravity never applies. */
    public static final float SPEED = 900f;

    // ---- Damage box (the visible opaque fireball core) ----
    public static final float DMG_WIDTH  = 110f;
    public static final float DMG_HEIGHT = 46f;

    /** A slim probe used only to test the leading edge against walls. */
    public static final float PROBE_SIZE = 20f;

    // ---- Damage ----
    // Worth two nail hits — a cast costs three hits' worth of soul, so the
    // spell trades soul economy for range and safety. Tune here.
    public static final int DAMAGE = 2;

    /** Safety net: despawn after this far if no wall is ever hit. */
    public static final float MAX_TRAVEL = 4000f;

    public enum State { FLYING, IMPACT, DONE }

    private State state = State.FLYING;
    private float stateTime;
    private float travelled;

    /** Total time since the ball left the knight — drives the Blast overlay. */
    private float age;

    /** Where the ball emerged (the knight's cast point) — the Blast anchor. */
    private final float originX;
    private final float originY;

    private final CollisionRect damageBox = new CollisionRect(0, 0, DMG_WIDTH, DMG_HEIGHT);
    private final CollisionRect probeBox  = new CollisionRect(0, 0, PROBE_SIZE, PROBE_SIZE);

    /** Enemies already hit by this ball — it passes through, damaging each once. */
    private final IntSet hitEnemies = new IntSet();

    /** Cast with Void Heart equipped - the view swaps to the shadow art set. */
    private final boolean shadow;

    public VengefulSpirit(float x, float y, boolean facingRight, boolean shadow) {
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;
        this.shadow = shadow;
        this.velocityX = (facingRight ? 1f : -1f) * SPEED;
        this.originX = x;
        this.originY = y;
    }

    /** Advance the ball while it is flying. */
    public void move(float delta) {
        float dx = velocityX * delta;
        x += dx;
        travelled += Math.abs(dx);
    }

    // ---- Boxes (center-anchored) ----

    /** The live hurt region; only meaningful while {@link State#FLYING}. */
    public CollisionRect getDamageBox() {
        return damageBox.set(x - DMG_WIDTH / 2f, y - DMG_HEIGHT / 2f, DMG_WIDTH, DMG_HEIGHT);
    }

    /** Leading edge of the ball, used for wall detection. */
    public CollisionRect getProbeBox() {
        float tipX = x + (facingRight ? 1f : -1f) * (DMG_WIDTH / 2f);
        return probeBox.set(tipX - PROBE_SIZE / 2f, y - PROBE_SIZE / 2f, PROBE_SIZE, PROBE_SIZE);
    }

    // ---- Getters ----

    public float getX()            { return x; }
    public float getY()            { return y; }
    public float getOriginX()      { return originX; }
    public float getOriginY()      { return originY; }
    public float getAge()          { return age; }
    public boolean isFacingRight() { return facingRight; }
    public boolean isShadow()      { return shadow; }
    public State getState()        { return state; }
    public float getStateTime()    { return stateTime; }
    public boolean isFlying()      { return state == State.FLYING; }
    public boolean isDone()        { return state == State.DONE; }
    public boolean hasExceededRange() { return travelled >= MAX_TRAVEL; }

    // ---- Mutations ----

    public void setState(State newState) {
        if (this.state != newState) this.stateTime = 0f;
        this.state = newState;
    }

    public void addStateTime(float delta) {
        this.stateTime += delta;
        this.age += delta;
    }

    /** @return true if this ball has not yet damaged the enemy with {@code id}. */
    public boolean canHit(int id) {
        return !hitEnemies.contains(id);
    }

    public void markHit(int id) {
        hitEnemies.add(id);
    }
}
