package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.PhysicsBody;

public class WingedSentry implements PhysicsBody {
    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean facingRight = true;

    // Size (collision box)
    public static final float WIDTH  = 70f;
    public static final float HEIGHT = 55f;

    public enum State {
        IDLE, // idle animation
        TURN_TO_IDLE,
        CHARGE_ANTIC, // preparation animation
        CHARGE, // locked y
        CHARGE_RECOVER, // recovery after charge
        CHASE,
        THROW_ATTACK,
        DEATH_AIR,
        DEATH_LAND
    }

    private State state = State.IDLE;
    private float stateTime;

    // Health
    private int hp;
    public static final int MAX_HP = 3;

    // AI parameters
    /** Detection range (pixels) — how far the sentry can "see" the player. */
    public static final float DETECTION_RANGE = 350f;
    public static final float CHASE_SPEED = 120f;
    /** Speed during the horizontal charge attack. */
    public static final float CHARGE_SPEED = 600f;
    /** Duration of the charge anticipation (wind-up) animation in seconds. */
    public static final float CHARGE_ANTIC_DURATION = 0.45f;
    /** Duration of the charge recover animation in seconds. */
    public static final float CHARGE_RECOVER_DURATION = 0.5f;
    /** Maximum distance the charge travels before auto-recovering. */
    public static final float MAX_CHARGE_DISTANCE = 800f;
    /** Y position locked at first sight — used during the charge. */
    private float lockedChargeY;
    /** X position when the charge started — to measure charge distance. */
    private float chargeStartX;
    /** Whether the sentry has seen the player at least once (chase is permanent). */
    private boolean hasDetectedPlayer = false;
    /** Cooldown timer before the next charge/throw attack can begin. */
    private float attackCooldown;
    /** Time between allowed special attacks. */
    public static final float ATTACK_COOLDOWN_DURATION = 2.0f;

    // Spawn position (for respawn)
    private final float spawnX;
    private final float spawnY;

    // Alive state
    private boolean alive = true;
    private boolean deadHandled = false;

    public WingedSentry(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.x = spawnX;
        this.y = spawnY;
        this.hp = MAX_HP;
        this.attackCooldown = 1.0f;
    }

    // Collision box

    @Override
    public float getLeft()   { return x - WIDTH / 2f; }
    @Override
    public float getRight()  { return x + WIDTH / 2f; }
    @Override
    public float getBottom() { return y; }
    @Override
    public float getTop()    { return y + HEIGHT; }

    // ---- PhysicsBody ----

    @Override
    public float getHalfWidth() { return WIDTH / 2f; }

    @Override
    public void setCenterX(float cx) { this.x = cx; }

    @Override
    public float getVelocityX()      { return velocityX; }

    // Getters

    public float getX()              { return x; }
    public float getY()              { return y; }
    public float getVelocityY()      { return velocityY; }
    public boolean isFacingRight()   { return facingRight; }
    public State getState()          { return state; }
    public float getStateTime()      { return stateTime; }
    public int getHp()               { return hp; }
    public int getMaxHp()            { return MAX_HP; }
    public boolean isAlive()         { return alive; }
    public boolean isDeadHandled()   { return deadHandled; }
    public float getLockedChargeY()  { return lockedChargeY; }
    public float getChargeStartX()   { return chargeStartX; }
    public boolean hasDetectedPlayer() { return hasDetectedPlayer; }
    public float getAttackCooldown() { return attackCooldown; }
    public float getSpawnX()         { return spawnX; }
    public float getSpawnY()         { return spawnY; }

    // Mutations

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

    public void setState(State newState) {
        if (this.state != newState) this.stateTime = 0f;
        this.state = newState;
    }

    public void addStateTime(float delta) {
        this.stateTime += delta;
    }

    public void setLockedChargeY(float y) { this.lockedChargeY = y; }
    public void setChargeStartX(float x)  { this.chargeStartX = x; }
    public void setDetectedPlayer(boolean detected) { this.hasDetectedPlayer = detected; }

    public void tickAttackCooldown(float delta) {
        if (attackCooldown > 0) {
            attackCooldown -= delta;
            if (attackCooldown < 0) attackCooldown = 0;
        }
    }

    public void resetAttackCooldown() {
        this.attackCooldown = ATTACK_COOLDOWN_DURATION;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public void setDeadHandled(boolean handled) { this.deadHandled = handled; }

    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.velocityX = 0;
        this.velocityY = 0;
        this.hp = MAX_HP;
        this.alive = true;
        this.deadHandled = false;
        this.hasDetectedPlayer = false;
        this.state = State.IDLE;
        this.stateTime = 0;
        this.attackCooldown = 1.0f;
    }

    public float distanceSquaredTo(float px, float py) {
        float dx = (x) - px;
        float dy = (y + HEIGHT / 2f) - py;
        return dx * dx + dy * dy;
    }

    public float horizontalDistanceTo(float px) {
        return Math.abs(x - px);
    }
}
