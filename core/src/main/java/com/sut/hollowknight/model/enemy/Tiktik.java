package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.PhysicsBody;

public class Tiktik implements PhysicsBody {

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean facingRight = false;

    // Size (collision box). The walk sprite's opaque body is ~90x77 px in a
    // 115x105 canvas; the box is slightly tighter to feel fair.
    public static final float WIDTH  = 80f;
    public static final float HEIGHT = 66f;

    public enum State {
        WALK,
        DEATH_AIR,   // rendered with the Death Air Old animation, played once
        DEATH_LAND   // played once after landing, last frame is the corpse
    }

    private State state = State.WALK;
    private float stateTime;

    // Health
    public static final int MAX_HP = 2;
    private int hp;

    // Movement
    public static final float WALK_SPEED = 90f;

    //  Nail-hit recoil
    public static final float RECOIL_SPEED = 700f;
    public static final float RECOIL_DURATION = 0.15f;
    public static final float MASS = 1.0f;
    private float recoilVelX;
    private float recoilTimer;

    // Hit flash
    public static final float HIT_FLASH_DURATION = 0.15f;
    private float hitFlashTimer;

    // Spawn / lifecycle
    private final float spawnX;
    private final float spawnY;
    private boolean alive = true;
    private boolean deadHandled = false;

    public Tiktik(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.x = spawnX;
        this.y = spawnY;
        this.hp = MAX_HP;
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

    // PhysicsBody

    @Override
    public float getHalfWidth() { return WIDTH / 2f; }

    @Override
    public void setCenterX(float cx) { this.x = cx; }

    @Override
    public float getVelocityX() { return velocityX; }

    // ---- Getters ----

    public float getX()              { return x; }
    public float getY()              { return y; }
    public float getVelocityY()      { return velocityY; }
    public boolean isFacingRight()   { return facingRight; }
    public State getState()          { return state; }
    public float getStateTime()      { return stateTime; }
    public int getHp()               { return hp; }
    public boolean isAlive()         { return alive; }
    public boolean isDeadHandled()   { return deadHandled; }
    public float getSpawnX()         { return spawnX; }
    public float getSpawnY()         { return spawnY; }

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

    public void setState(State newState) {
        if (this.state != newState) this.stateTime = 0f;
        this.state = newState;
    }

    public void addStateTime(float delta) {
        this.stateTime += delta;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public void setDeadHandled(boolean handled) { this.deadHandled = handled; }

    // ---- Recoil ----

    public void applyRecoil(float dirX, float scale) {
        recoilVelX = dirX * RECOIL_SPEED * scale / MASS;
        recoilTimer = RECOIL_DURATION;
    }

    public void tickRecoil(float delta) {
        if (recoilTimer > 0f) {
            recoilTimer -= delta;
            if (recoilTimer < 0f) recoilTimer = 0f;
        }
    }

    public void cancelRecoil() { recoilTimer = 0f; }

    public boolean isRecoiling()  { return recoilTimer > 0f; }
    public float getRecoilVelX()  { return recoilVelX; }
    public float getRecoilTimer() { return recoilTimer; }

    // ---- Hit flash ----

    public void startHitFlash() { hitFlashTimer = HIT_FLASH_DURATION; }

    public void tickHitFlash(float delta) {
        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
            if (hitFlashTimer < 0f) hitFlashTimer = 0f;
        }
    }

    public boolean isHitFlashing() { return hitFlashTimer > 0f; }

    public float getHitFlashStrength() { return hitFlashTimer / HIT_FLASH_DURATION; }

    // ---- Lifecycle ----

    public void respawn() {
        this.x = spawnX;
        this.y = spawnY;
        this.velocityX = 0;
        this.velocityY = 0;
        this.hp = MAX_HP;
        this.alive = true;
        this.deadHandled = false;
        this.state = State.WALK;
        this.stateTime = 0;
        this.recoilTimer = 0;
        this.hitFlashTimer = 0;
    }
}
