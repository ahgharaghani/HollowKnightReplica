package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.PhysicsBody;

/**
 * Husk Hornhead — the evolved ground enemy (fixed enemy #1 in the spec).
 *
 * Behaviour: walks a straight line for a fixed time, then stops and rests for
 * a while. It carries a vision RECTANGLE in front of it; when the knight
 * enters it, the hornhead winds up and lunges at high speed. The lunge is
 * blind — it ignores the knight and only ends on a wall hit or a cliff edge.
 */
public class HuskHornhead implements PhysicsBody {

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean facingRight = false; // native art faces left

    // Size (collision box). The walk sprite's opaque body is ~104x136 px at
    // draw scale; the box is slightly tighter to feel fair.
    public static final float WIDTH  = 90f;
    public static final float HEIGHT = 120f;

    public enum State {
        WALK,            // straight-line patrol, fixed duration
        REST,            // stands still between walk stretches
        TURN,            // brief turn animation when reversing direction
        ATTACK_ANTIC,    // saw the knight: wind-up before the lunge
        LUNGE,           // blind high-speed charge until wall/cliff
        ATTACK_COOLDOWN, // catches its breath after a lunge
        DEATH_AIR,       // corpse launched by the killing blow
        DEATH_LAND       // corpse settles; last frame stays on screen
    }

    private State state = State.WALK;
    private float stateTime;

    // Health — tougher than a plain crawler, weaker than the guardian.
    public static final int MAX_HP = 4;
    private int hp;

    // Movement
    public static final float WALK_SPEED  = 80f;
    public static final float LUNGE_SPEED = 430f;

    // Patrol rhythm: walk for a fixed time, then rest for a fixed time.
    public static final float WALK_DURATION = 2.6f;
    public static final float REST_DURATION = 1.6f;

    // Attack timings
    /** Wind-up: 5 anticipate frames at 12 fps. */
    public static final float ATTACK_ANTIC_DURATION    = 5f / 12f;
    public static final float ATTACK_COOLDOWN_DURATION = 0.9f;
    /** Turn animation: 2 frames at 10 fps. */
    public static final float TURN_DURATION = 0.2f;

    // Vision rectangle in FRONT of the hornhead (anchored at its front edge).
    public static final float VISION_LENGTH = 420f;
    public static final float VISION_HEIGHT = 150f;

    // Nail-hit recoil — heavier than a tiktik, so it recoils less.
    public static final float RECOIL_SPEED    = 550f;
    public static final float RECOIL_DURATION = 0.15f;
    public static final float MASS = 1.5f;
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

    /** Facing to apply once the TURN animation finishes. */
    private boolean turnTargetRight;

    /** Reusable vision rect — never allocated in the game loop. */
    private final CollisionRect visionRect = new CollisionRect(0, 0, 0, 0);

    public HuskHornhead(float spawnX, float spawnY) {
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

    /**
     * The vision rectangle in front of the hornhead. Anchored at the front
     * edge of the body, sitting on the ground (a knight jumping clean over
     * the box is not seen).
     */
    public CollisionRect getVisionRect() {
        float left = facingRight ? getRight() : getLeft() - VISION_LENGTH;
        return visionRect.set(left, y, VISION_LENGTH, VISION_HEIGHT);
    }

    // ---- Getters ----

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
    public float getSpawnX()         { return spawnX; }
    public float getSpawnY()         { return spawnY; }
    public boolean getTurnTargetRight() { return turnTargetRight; }

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

    public void setTurnTargetRight(boolean targetRight) {
        this.turnTargetRight = targetRight;
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
