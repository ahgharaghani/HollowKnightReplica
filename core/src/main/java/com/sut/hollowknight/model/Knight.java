package com.sut.hollowknight.model;

/**
 * Pure data model for the Knight character.
 * Contains only state — position, velocity, health, soul, and animation state.
 * No update logic, no collision resolution, no rendering concerns.
 * The controller mutates this; the view reads from it.
 */
public class Knight {

    // ---- Position & physics ----
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean grounded;
    private boolean facingRight = true;

    // ---- Movement constants ----
    public static final float GRAVITY      = 980f;   // px/s^2
    public static final float MOVE_SPEED   = 250f;
    public static final float JUMP_IMPULSE = 420f;
    public static final float FRICTION     = 600f;
    public static final float KNIGHT_WIDTH  = 20f;   // collision box (not the sprite)
    public static final float KNIGHT_HEIGHT = 50f;

    // ---- Health & soul ----
    private int hpMasks;
    private int maxMasks;
    private int soulAmount;

    // ---- Animation state ----
    public enum State { IDLE, RUN, JUMP, FALL, LAND }
    private State state = State.IDLE;
    private float stateTime;

    // ---- Combat ----
    private boolean invincible;
    private float invincibleTimer;

    public Knight(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.hpMasks = 5;
        this.maxMasks = 5;
        this.soulAmount = 0;
        this.grounded = false;
    }

    // ---- Collision box (feet-to-head, center-based) ----

    public float getLeft()   { return x - KNIGHT_WIDTH / 2f; }
    public float getRight()  { return x + KNIGHT_WIDTH / 2f; }
    public float getBottom() { return y; }
    public float getTop()    { return y + KNIGHT_HEIGHT; }

    // ---- Getters ----

    public float getX()              { return x; }
    public float getY()              { return y; }
    public float getVelocityX()      { return velocityX; }
    public float getVelocityY()      { return velocityY; }
    public boolean isFacingRight()   { return facingRight; }
    public State getState()          { return state; }
    public float getStateTime()      { return stateTime; }
    public int getHpMasks()          { return hpMasks; }
    public int getMaxMasks()         { return maxMasks; }
    public int getSoulAmount()       { return soulAmount; }
    public boolean isInvincible()    { return invincible; }
    public boolean isGrounded()      { return grounded; }

    // ---- Mutations ----

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public void setVelocityX(float vx) { this.velocityX = vx; }
    public void setVelocityY(float vy) { this.velocityY = vy; }

    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public void setState(State state) {
        if (this.state != state) this.stateTime = 0f;
        this.state = state;
    }

    public void addStateTime(float delta) {
        this.stateTime += delta;
    }

    public void takeDamage(int amount) {
        if (invincible) return;
        hpMasks -= amount;
        if (hpMasks < 0) hpMasks = 0;
        invincible = true;
        invincibleTimer = 1.0f;
    }

    public void heal(int masks) {
        hpMasks = Math.min(hpMasks + masks, maxMasks);
    }

    public void addSoul(int amount) {
        soulAmount = Math.min(soulAmount + amount, 99);
    }

    public void consumeSoul(int amount) {
        soulAmount = Math.max(soulAmount - amount, 0);
    }

    public boolean isDead() {
        return hpMasks <= 0;
    }

    /** Returns true if invincibility just expired. */
    public boolean tickInvincibility(float delta) {
        if (!invincible) return false;
        invincibleTimer -= delta;
        if (invincibleTimer <= 0) {
            invincible = false;
            return true;
        }
        return false;
    }
}
