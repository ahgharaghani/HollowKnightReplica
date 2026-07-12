package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * False Knight — the boss (spec: Boss Fight).
 *
 * Pure model (MVC): state, tunables and hitbox math only. All decision
 * making lives in FalseKnightController, all drawing in FalseKnightRenderer.
 *
 * Life cycle: full-armor fight -> one-time STUN at 50% HP (armor opens, the
 * maggot head becomes the vulnerable hitbox) -> PHASE 2 (faster movement,
 * faster attack animations, faster decisions, Power Mace Slam unlocked)
 * -> death (armor collapses, the head spills out and dies).
 */
public class FalseKnight implements AABB {

    // ---- Collision box (armor torso; the art canvas is far larger) ----
    // Measured from the Idle art: the armor mass spans canvas x 400-730
    // (center 565) with the solid helmet dome topping out near y 256 -
    // i.e. the body sits ~84 world px FORWARD of the sprite anchor at
    // 1.1x scale. A symmetric box around x left empty air behind the
    // boss and cut off the front of the armor.
    public static final float WIDTH  = 360f;
    public static final float HEIGHT = 380f;
    /** Armor mass center, forward of the sprite anchor (facing-relative). */
    public static final float BODY_OFFSET_X = 84f;

    // ---- Health: far more hits than the average enemy (3-6 HP) ----
    public static final int MAX_HP = 100;
    /** The one-time stun triggers at half health (spec). */
    public static final int STUN_HP_THRESHOLD = MAX_HP / 2;
    /** The exposed maggot takes normal damage - the stun's value is the
     *  free, uninterrupted 6s window, not a damage bonus. */
    public static final int STUN_DAMAGE_MULT = 1;

    public enum State {
        IDLE, TURN,
        RAGE, RUN,                            // move 2: charge run
        ATTACK_ANTIC, ATTACK, ATTACK_RECOVER, // move 1: mace slam
        JUMP_ANTIC, JUMP, LAND,               // moves 3/4: leaps
        POWER_JUMP, POWER_HIT,                // move 5: power slam (phase 2)
        STUN_FALL, STUN_GROUND, STUN_OPEN, STUN_OPENED, STUN_RECOVER,
        DEATH_FALL, DEATH_LAND, DEATH_SPAZ
    }

    /** The five moves of the AI decision system (spec). */
    public enum Move { MACE_SLAM, CHARGE, LEAP_ATTACK, LEAP_BACK, POWER_SLAM }

    // ---- Animation-locked state durations (frames / fps) ----
    public static final float TURN_DURATION           = 2f / 10f;
    public static final float RAGE_DURATION           = 5f / 10f;
    public static final float ATTACK_ANTIC_DURATION   = 6f / 12f;
    public static final float ATTACK_DURATION         = 3f / 15f;
    public static final float ATTACK_RECOVER_DURATION = 5f / 12f;
    public static final float JUMP_ANTIC_DURATION     = 3f / 12f;
    public static final float LAND_DURATION           = 5f / 15f;
    public static final float POWER_HIT_DURATION      = 6f / 15f;
    public static final float STUN_GROUND_DURATION    = 4f / 12f;
    public static final float STUN_OPEN_DURATION      = 4f / 10f;
    /** How long the armor stays open — the punish window. */
    public static final float STUN_OPENED_DURATION    = 6f;
    public static final float STUN_RECOVER_DURATION   = 6f / 10f;
    public static final float DEATH_LAND_DURATION     = 5f / 12f;

    // ---- Movement tunables ----
    /** Heavier than the knight: a squat, punchy jump arc. */
    public static final float GRAVITY          = 1600f;
    public static final float CHARGE_SPEED     = 430f;
    public static final float CHARGE_MAX_TIME  = 2.5f;   // safety timeout
    public static final float LEAP_VY          = 780f;
    public static final float LEAP_VX_MAX      = 540f;
    public static final float LEAP_BACK_VY     = 560f;
    public static final float LEAP_BACK_VX     = 330f;
    public static final float POWER_JUMP_VY    = 950f;
    public static final float POWER_JUMP_VX_MAX = 430f;

    // ---- Phase 2 (post-stun) multipliers (spec) ----
    public static final float PHASE2_MOVE_MULT     = 1.3f;
    public static final float PHASE2_ANIM_MULT     = 1.35f;
    public static final float PHASE2_DECISION_MULT = 1.5f;

    // ---- Mace strike hitboxes ----
    /** Ground slam in front of the armor. */
    public static final float STRIKE_REACH  = 480f;
    public static final float STRIKE_HEIGHT = 300f;
    /** Power slam impact box, centered on the landing point. */
    public static final float POWER_STRIKE_HALF_W = 300f;
    public static final float POWER_STRIKE_H      = 280f;

    // ---- Defensive leap trigger: heavy hits in a short window (spec) ----
    public static final float DAMAGE_WINDOW      = 1.4f;
    public static final int   DAMAGE_WINDOW_HITS = 3;

    // ---- Head (maggot) hitbox while the armor is open / dying ----
    // The Stun Opened art bakes the head in at canvas (688, 499): the
    // maggot slumps onto the GROUND in front of the armor, not on top of
    // it. World anchor at 1.1x: (688-489)*1.1 forward, (601-499)*1.1 up.
    public static final float HEAD_BOX_SIZE      = 130f;
    /** Forward of the armor center (measured from Stun Opened art). */
    public static final float HEAD_STUN_OFFSET_X = 219f;
    /** Head center height above the feet (measured from art). */
    public static final float HEAD_STUN_CENTER_Y = 112f;

    // ---- Hit flash ----
    public static final float HIT_FLASH_DURATION = 0.12f;
    /** Body flinch + head-hit animation window during the stun. */
    public static final float STUN_HIT_DURATION  = 3f / 15f;

    private float x;                 // feet center
    private float y;                 // feet
    private float velocityX;
    private float velocityY;
    private boolean facingRight = false;
    private boolean turnTargetRight;

    private State state = State.IDLE;
    private float stateTime;

    private int hp = MAX_HP;
    private boolean alive = true;
    private boolean deadHandled = false;
    private boolean phase2 = false;
    private boolean stunTriggered = false;

    private float hitFlashTimer;
    private float stunHitTimer;
    private float stunHitTime;       // drives the Stun Hit / Head Hit anims
    private float deathHeadTime;     // drives Death Head 1 -> 2

    // Rolling damage window for the defensive-leap trigger.
    private int   recentHits;
    private float damageWindowTimer;

    private final float spawnX;
    private final float spawnY;

    /** Reusable boxes — never allocated in the game loop. */
    private final CollisionRect strikeRect = new CollisionRect(0, 0, 0, 0);
    private final CollisionRect headRect   = new CollisionRect(0, 0, 0, 0);
    private boolean strikeActive;
    private boolean powerStrike; // strike box centered (power slam) vs frontal

    public FalseKnight(float spawnX, float spawnY) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.x = spawnX;
        this.y = spawnY;
    }

    // ---- AABB (armor torso) ----

    /** The armor mass sits forward of the anchor; mirror with facing. */
    private float bodyCenterX() {
        return x + (facingRight ? BODY_OFFSET_X : -BODY_OFFSET_X);
    }

    @Override public float getLeft()   { return bodyCenterX() - WIDTH / 2f; }
    @Override public float getRight()  { return bodyCenterX() + WIDTH / 2f; }
    @Override public float getBottom() { return y; }
    @Override public float getTop()    { return y + HEIGHT; }

    /** Maggot hitbox — the vulnerable target while the armor is open. */
    public CollisionRect getHeadBox() {
        float dir = facingRight ? 1f : -1f;
        float cx = x + dir * HEAD_STUN_OFFSET_X;
        float cy = y + HEAD_STUN_CENTER_Y;
        return headRect.set(cx - HEAD_BOX_SIZE / 2f, cy - HEAD_BOX_SIZE / 2f,
            HEAD_BOX_SIZE, HEAD_BOX_SIZE);
    }

    /** Active mace hitbox, or null when no strike is live. */
    public CollisionRect getStrikeBox() {
        if (!strikeActive) return null;
        if (powerStrike) {
            return strikeRect.set(x - POWER_STRIKE_HALF_W, y,
                POWER_STRIKE_HALF_W * 2f, POWER_STRIKE_H);
        }
        float left = facingRight ? getRight() - 20f : getLeft() + 20f - STRIKE_REACH;
        return strikeRect.set(left, y, STRIKE_REACH, STRIKE_HEIGHT);
    }

    public void setStrike(boolean active, boolean power) {
        this.strikeActive = active;
        this.powerStrike = power;
    }

    public boolean isStunnedState() {
        return state == State.STUN_FALL || state == State.STUN_GROUND
            || state == State.STUN_OPEN || state == State.STUN_OPENED
            || state == State.STUN_RECOVER;
    }

    // ---- Getters ----

    public float getX()               { return x; }
    public float getY()               { return y; }
    public float getVelocityX()       { return velocityX; }
    public float getVelocityY()       { return velocityY; }
    public boolean isFacingRight()    { return facingRight; }
    public boolean getTurnTargetRight() { return turnTargetRight; }
    public State getState()           { return state; }
    public float getStateTime()       { return stateTime; }
    public int getHp()                { return hp; }
    public boolean isAlive()          { return alive; }
    public boolean isDeadHandled()    { return deadHandled; }
    public boolean isPhase2()         { return phase2; }
    public boolean isStunTriggered()  { return stunTriggered; }
    public float getSpawnX()          { return spawnX; }
    public float getSpawnY()          { return spawnY; }
    public float getStunHitTime()     { return stunHitTime; }
    public boolean isStunHit()        { return stunHitTimer > 0f; }
    public float getDeathHeadTime()   { return deathHeadTime; }

    /** Combined move/anim speed helper for phase 2. */
    public float getMoveMult() { return phase2 ? PHASE2_MOVE_MULT : 1f; }
    public float getAnimMult() { return phase2 ? PHASE2_ANIM_MULT : 1f; }

    // ---- Mutations ----

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setVelocityX(float vx) { this.velocityX = vx; }
    public void setVelocityY(float vy) { this.velocityY = vy; }
    public void setFacingRight(boolean right) { this.facingRight = right; }
    public void setTurnTargetRight(boolean right) { this.turnTargetRight = right; }
    public void setPhase2(boolean phase2) { this.phase2 = phase2; }
    public void setStunTriggered(boolean t) { this.stunTriggered = t; }
    public void setDeadHandled(boolean handled) { this.deadHandled = handled; }

    public void setState(State newState) {
        if (this.state != newState) this.stateTime = 0f;
        this.state = newState;
    }

    public void addStateTime(float delta) { this.stateTime += delta; }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    /** True once HP has crossed the one-time stun threshold. */
    public boolean shouldStun() {
        return alive && !stunTriggered && hp <= STUN_HP_THRESHOLD;
    }

    // ---- Damage window (defensive-leap trigger) ----

    public void registerHit() {
        if (damageWindowTimer <= 0f) recentHits = 0; // window expired: restart
        recentHits++;
        damageWindowTimer = DAMAGE_WINDOW;
    }

    public boolean isUnderHeavyFire() { return recentHits >= DAMAGE_WINDOW_HITS; }

    public void clearDamageWindow() {
        recentHits = 0;
        damageWindowTimer = 0f;
    }

    public void tickDamageWindow(float delta) {
        if (damageWindowTimer > 0f) {
            damageWindowTimer -= delta;
            if (damageWindowTimer <= 0f) recentHits = 0;
        }
    }

    // ---- Hit flash / stun-hit flinch / death head clock ----

    public void startHitFlash() { hitFlashTimer = HIT_FLASH_DURATION; }

    public void startStunHit() {
        stunHitTimer = STUN_HIT_DURATION;
        stunHitTime = 0f;
    }

    public void tickTimers(float delta) {
        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
            if (hitFlashTimer < 0f) hitFlashTimer = 0f;
        }
        if (stunHitTimer > 0f) {
            stunHitTimer -= delta;
            stunHitTime += delta;
            if (stunHitTimer < 0f) stunHitTimer = 0f;
        }
        if (!alive && (state == State.DEATH_LAND || state == State.DEATH_SPAZ)) {
            deathHeadTime += delta;
        }
    }

    public boolean isHitFlashing() { return hitFlashTimer > 0f; }

    public float getHitFlashStrength() { return hitFlashTimer / HIT_FLASH_DURATION; }

    // ---- Lifecycle ----

    public void respawn() {
        x = spawnX;
        y = spawnY;
        velocityX = 0f;
        velocityY = 0f;
        facingRight = false;
        state = State.IDLE;
        stateTime = 0f;
        hp = MAX_HP;
        alive = true;
        deadHandled = false;
        phase2 = false;
        stunTriggered = false;
        hitFlashTimer = 0f;
        stunHitTimer = 0f;
        stunHitTime = 0f;
        deathHeadTime = 0f;
        recentHits = 0;
        damageWindowTimer = 0f;
        strikeActive = false;
        powerStrike = false;
    }
}
