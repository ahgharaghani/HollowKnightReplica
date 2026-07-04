package com.sut.hollowknight.model;

import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.PhysicsBody;

public class Knight implements PhysicsBody {

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
    public static final float JUMP_IMPULSE = 620f;
    public static final float FRICTION     = 600f;
    public static final float JUMP_CUT_MULTIPLIER = 0.45f;
    public static final float KNIGHT_WIDTH  = 20f;   // PHYSICS box vs terrain (not the sprite)
    public static final float KNIGHT_HEIGHT = 50f;

    // ---- Combat hurtbox ----
    // Separate from the physics box
    public static final float HURT_WIDTH   = 34f;
    public static final float HURT_HEIGHT  = 92f;
    public static final float HURT_Y_OFFSET = 0f;    // lift box off the feet if needed

    // ---- Health & soul ----
    private int hpMasks;
    private int maxMasks;
    private int soulAmount;

    // ---- Animation state ----
    public enum State { IDLE, RUN, JUMP, FALL, LAND, HURT }
    private State state = State.IDLE;
    private float stateTime;

    // ---- Combat ----
    public static final float INVINCIBLE_DURATION = 1.3f;   // i-frames after a hit
    public static final float KNOCKBACK_DURATION  = 0.28f;  // input locked, HURT state
    private boolean invincible;
    private float invincibleTimer;
    private float knockbackTimer;

    public Knight(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.hpMasks = 5;
        this.maxMasks = 5;
        this.soulAmount = 0;
        this.grounded = false;
    }

    // Collision box

    @Override
    public float getLeft()   { return x - KNIGHT_WIDTH / 2f; }
    @Override
    public float getRight()  { return x + KNIGHT_WIDTH / 2f; }
    @Override
    public float getBottom() { return y; }
    @Override
    public float getTop()    { return y + KNIGHT_HEIGHT; }

    /** Combat hurtbox (what javelins/enemies test against), NOT the physics box. */
    public CollisionRect getHurtBox() {
        return new CollisionRect(
            x - HURT_WIDTH / 2f,
            y + HURT_Y_OFFSET,
            HURT_WIDTH,
            HURT_HEIGHT);
    }

    //PhysicsBody

    @Override
    public float getHalfWidth() { return KNIGHT_WIDTH / 2f; }

    @Override
    public void setCenterX(float cx) { this.x = cx; }

    @Override
    public float getVelocityX()      { return velocityX; }

    // ---- Getters ----

    public float getX()              { return x; }
    public float getY()              { return y; }
    public float getVelocityY()      { return velocityY; }
    public boolean isFacingRight()   { return facingRight; }
    public State getState()          { return state; }
    public float getStateTime()      { return stateTime; }
    public int getHpMasks()          { return hpMasks; }
    public int getMaxMasks()         { return maxMasks; }
    public int getSoulAmount()       { return soulAmount; }
    public boolean isInvincible()    { return invincible; }
    public boolean isGrounded()      { return grounded; }
    public boolean isInKnockback()   { return knockbackTimer > 0f; }
    public float getInvincibleTimer() { return invincibleTimer; }

    // ---- Mutations ----

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public void setVelocityX(float vx) { this.velocityX = vx; }
    public void setVelocityY(float vy) { this.velocityY = vy; }

    public void cutJumpVelocity() {
        if (velocityY > 0f) {
            velocityY *= JUMP_CUT_MULTIPLIER;
        }
    }

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
        invincibleTimer = INVINCIBLE_DURATION;
    }

    public void applyKnockback(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
        this.knockbackTimer = KNOCKBACK_DURATION;
        this.grounded = false;
        setState(State.HURT);
    }

    /** Tick the knockback lockout. Call once per frame. */
    public void tickKnockback(float delta) {
        if (knockbackTimer > 0f) {
            knockbackTimer -= delta;
            if (knockbackTimer < 0f) knockbackTimer = 0f;
        }
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
