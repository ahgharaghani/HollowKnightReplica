package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.PhysicsBody;

/**
 * Crystal Guardian ("Crystallized") — the most advanced enemy of the
 * assignment (fixed enemy #2 in the spec).
 *
 * Behaviour: stands rooted at its post, slowly sweeping its sightline left and
 * right. When the knight crosses into that sightline it locks on, aiming at
 * that spot; a thin harmless beam marks the fixed line while charging (~1.5s),
 * then it fires a thick damaging laser (~1s). Immediately after firing it charges to
 * the spot where the knight was first seen, then stops there and resumes its
 * idle sweep from that new position.
 */
public class CrystalGuardian implements PhysicsBody {

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;
    private boolean facingRight = false; // native art faces left

    // Size (collision box) — tuned to the creature's opaque torso.
    public static final float WIDTH  = 130f;
    public static final float HEIGHT = 135f;

    public enum State {
        IDLE,         // rooted, slowly sweeping its sightline
        TURN,         // brief turn animation when re-facing
        SHOOT_ANTIC,  // locked on the spot: harmless beam marks the line, charging
        SHOOT,        // firing starts; the damaging beam ignites, aim locked
        SHOOT_LOOP,   // beam held on the locked direction
        SHOOT_END,    // beam winds down
        ENRAGED,      // high-speed charge to where the knight was first seen
        DEATH_AIR,    // corpse launched by the killing blow
        DEATH_LAND    // corpse settles; last frame stays on screen
    }

    private State state = State.IDLE;
    private float stateTime;

    // Health — the toughest non-boss enemy.
    public static final int MAX_HP = 6;
    private int hp;

    // Vision — front-facing only (no eyes in the back); the idle sweep covers
    // both sides over time.
    public static final float DETECTION_RANGE    = 900f;
    /** Vertical half-window of the watch — it guards its own floor. */
    public static final float DETECTION_VERTICAL = 280f;

    /**
     * Aim clamp: the crystal arms can aim horizontally and diagonally but
     * cannot point straight up or down, so the beam always keeps a horizontal
     * component. Measured as elevation from horizontal — the beam may aim up to
     * 45 degrees above and 45 degrees below the straight eye line.
     */
    public static final float MAX_AIM_ANGLE_DEG = 45f;

    // Idle sweep — slowly turns left/right to patrol the immediate sightline.
    public static final float IDLE_TURN_MIN_INTERVAL = 1.8f;
    public static final float IDLE_TURN_MAX_INTERVAL = 3.4f;

    // Attack timings
    /** Charging phase: track the knight with a thin harmless beam for ~1.5s. */
    public static final float SHOOT_ANTIC_DURATION = 1.5f;
    /** Shoot animation: 7 frames at 15 fps. Damaging beam ignites on entry. */
    public static final float SHOOT_DURATION       = 7f / 15f;
    /** Beam held after the shoot animation — total firing lasts ~1s. */
    public static final float SHOOT_LOOP_DURATION  = 0.55f;
    /** Wind-down: 3 frames at 12 fps. */
    public static final float SHOOT_END_DURATION   = 0.25f;
    /** Turn animation: 3 frames at 10 fps. */
    public static final float TURN_DURATION        = 0.3f;

    // Enrage — charge to the spotted position, then stop.
    public static final float ENRAGE_SPEED = 475f;

    // Laser muzzle — the head-lamp, measured against the drawn sprite.
    /** Forward offset of the lamp from the body center, along facing. */
    public static final float MUZZLE_FORWARD = 36f;
    /** Lamp height above the feet. */
    public static final float MUZZLE_HEIGHT  = 105f;

    // Nail-hit recoil — a crystal-armored heavyweight, barely budges.
    public static final float RECOIL_SPEED    = 700f;
    public static final float RECOIL_DURATION = 0.12f;
    public static final float MASS = 1.0f;
    private float recoilVelX;
    private float recoilTimer;

    // Hit flash
    public static final float HIT_FLASH_DURATION = 0.15f;
    private float hitFlashTimer;

    // Spawn / lifecycle
    private final float spawnX;
    private final float spawnY;
    private final boolean spawnFacingRight;
    private boolean alive = true;
    private boolean deadHandled = false;

    /** Facing to apply once the TURN animation finishes. */
    private boolean turnTargetRight;

    /** Knight's X at the moment it was first spotted — the post-fire charge target. */
    private float spottedX;

    /** Reusable beam instance — firing never allocates in the game loop. */
    private final Laser laser = new Laser();

    public CrystalGuardian(float spawnX, float spawnY, boolean facingRight) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnFacingRight = facingRight;
        this.x = spawnX;
        this.y = spawnY;
        this.facingRight = facingRight;
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

    // Muzzle (world coordinates)

    public float getMuzzleX() {
        return x + (facingRight ? MUZZLE_FORWARD : -MUZZLE_FORWARD);
    }

    public float getMuzzleY() {
        return y + MUZZLE_HEIGHT;
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
    public boolean isSpawnFacingRight() { return spawnFacingRight; }
    public boolean getTurnTargetRight() { return turnTargetRight; }
    public float getSpottedX()       { return spottedX; }
    public Laser getLaser()          { return laser; }

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

    public void setSpottedX(float spottedX) { this.spottedX = spottedX; }

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
        this.facingRight = spawnFacingRight;
        this.state = State.IDLE;
        this.stateTime = 0;
        this.recoilTimer = 0;
        this.hitFlashTimer = 0;
        this.laser.deactivate();
    }
}
