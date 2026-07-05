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
    public static final float HURT_WIDTH   = 44f;
    public static final float HURT_HEIGHT  = 110f;
    public static final float HURT_Y_OFFSET = 4f;    // opaque pixels start slightly above the feet

    // ---- Nail slash hitboxes ----
    public static final float SLASH_REACH_X = 125f;  // forward reach of a side slash
    public static final float SLASH_BACK_X  = 20f;   // slight reach behind the knight
    public static final float SLASH_HEIGHT  = 90f;   // vertical size of a side slash
    public static final float SLASH_Y       = 25f;   // side arc bottom, above the feet
    public static final float SLASH_UP_W    = 120f;
    public static final float SLASH_UP_Y    = 80f;   // up arc starts at the upper body
    public static final float SLASH_UP_H    = 150f;
    public static final float SLASH_DOWN_W  = 120f;
    public static final float SLASH_DOWN_H  = 140f;

    // ---- Soul & pogo ----
    // vessel caps at 99
    public static final int SOUL_PER_NAIL_HIT = 11;
    public static final float POGO_IMPULSE = 560f;

    // ---- Health & soul ----
    private int hpMasks;
    private int maxMasks;
    private int soulAmount;

    public static final int LOW_HEALTH_THRESHOLD = 1;

    // ---- Animation state ----
    public enum State {
        IDLE,
        RUN,
        JUMP,
        FALL,
        LAND,
        HURT,
        RECOIL,
        IDLE_LOW_HEALTH,
        DEATH,

        // Slash family
        SLASH,
        SLASH_ALT,
        DOWN_SLASH,
        UP_SLASH,
        WALL_SLASH,

        // Dash family
        DASH,
        DASH_DOWN,
        DASH_DOWN_LAND,

        // Wall / jump family
        WALL_SLIDE,
        WALL_JUMP,
        DOUBLE_JUMP,
    }
    private State state = State.IDLE;
    private float stateTime;

    // ---- Combat ----
    public static final float INVINCIBLE_DURATION = 1.3f;   // i-frames after a hit
    public static final float KNOCKBACK_DURATION  = 0.28f;  // input locked, HURT state
    private boolean invincible;
    private float invincibleTimer;
    private float knockbackTimer;

    // ---- Attack combo state ----
    /** How long a single slash animation takes to play (seconds). */
    public static final float ATTACK_DURATION = 0.35f;
    /** Window after a slash during which a follow-up slash counts as the alt swing. */
    public static final float ATTACK_COMBO_WINDOW = 0.45f;
    /** Down/up slash duration (slightly longer arc). */
    public static final float DIRECTIONAL_ATTACK_DURATION = 0.40f;
    /** Wall slash duration. */
    public static final float WALL_SLASH_DURATION = 0.35f;

    private int  attackComboCount;       // 0 = none, 1 = slash just used, alt next
    private float attackComboTimer;      // counts down ATTACK_COMBO_WINDOW
    private float attackTimer;           // counts down the active slash animation
    private int attackId;

    // ---- Dash state ----
    public static final float DASH_SPEED        = 720f;
    public static final float DASH_DURATION     = 0.18f;
    public static final float DASH_COOLDOWN     = 0.80f;
    public static final float DASH_DOWN_SPEED   = 1100f;
    public static final float DASH_DOWN_DURATION = 0.35f;
    public static final float DASH_DOWN_LAND_DURATION = 0.30f;

    private boolean dashing;
    private boolean dashingDown;
    private float   dashTimer;           // counts down DASH_DURATION
    private float   dashCooldownTimer;   // counts down DASH_COOLDOWN
    private float   dashDownTimer;       // counts down DASH_DOWN_DURATION
    private float   dashDownLandTimer;   // counts down DASH_DOWN_LAND_DURATION
    private int     dashDirection;       // -1 or +1

    // ---- Double jump ----
    public static final float DOUBLE_JUMP_IMPULSE = 560f;
    private boolean doubleJumpAvailable;

    // ---- Wall slide / wall jump ----
    public static final float WALL_SLIDE_SPEED  = 120f;   // capped fall speed while sliding
    public static final float WALL_JUMP_VX      = 360f;
    public static final float WALL_JUMP_VY      = 560f;
    public static final float WALL_JUMP_LOCK    = 0.18f;  // input-lock after wall jump
    public static final float WALL_JUMP_DURATION = 0.25f; // animation play time

    /** -1 = wall on left, +1 = wall on right, 0 = not touching a wall. Set by GameController. */
    private int wallDirection;
    private float wallJumpTimer;          // counts down WALL_JUMP_DURATION (anim)
    private float wallJumpLockTimer;      // counts down WALL_JUMP_LOCK (input lock)

    // ---- Death ----
    public static final float DEATH_DURATION = 1.6f;
    private float deathTimer;
    private boolean dead;

    public Knight(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.hpMasks = 5;
        this.maxMasks = 5;
        this.soulAmount = 0;
        this.grounded = false;
        this.doubleJumpAvailable = true; // refreshes on landing
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

    private final CollisionRect hurtBox  = new CollisionRect(0, 0, HURT_WIDTH, HURT_HEIGHT);
    private final CollisionRect slashBox = new CollisionRect(0, 0, 0, 0);

    public CollisionRect getHurtBox() {
        return hurtBox.set(
            x - HURT_WIDTH / 2f,
            y + HURT_Y_OFFSET,
            HURT_WIDTH,
            HURT_HEIGHT);
    }


    public CollisionRect getActiveSlashBox() {
        if (attackTimer <= 0f) return null;
        switch (state) {
            case SLASH:
            case SLASH_ALT:
            case WALL_SLASH: {
                float left = facingRight ? x - SLASH_BACK_X : x - SLASH_REACH_X;
                return slashBox.set(left, y + SLASH_Y, SLASH_REACH_X + SLASH_BACK_X, SLASH_HEIGHT);
            }
            case UP_SLASH:
                return slashBox.set(x - SLASH_UP_W / 2f, y + SLASH_UP_Y, SLASH_UP_W, SLASH_UP_H);
            case DOWN_SLASH:
                return slashBox.set(x - SLASH_DOWN_W / 2f, y - SLASH_DOWN_H, SLASH_DOWN_W, SLASH_DOWN_H);
            default:
                return null;
        }
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

    public boolean isDead()                  { return dead; }
    public float   getDeathTimer()           { return deathTimer; }
    public boolean isDashing()               { return dashing; }
    public boolean isDashingDown()           { return dashingDown; }
    public float   getDashTimer()            { return dashTimer; }
    public float   getDashDownTimer()        { return dashDownTimer; }
    public float   getDashDownLandTimer()    { return dashDownLandTimer; }
    public int     getDashDirection()        { return dashDirection; }
    public boolean isDashOnCooldown()        { return dashCooldownTimer > 0f; }
    public boolean isAttacking()             { return attackTimer > 0f; }
    public float   getAttackTimer()          { return attackTimer; }
    public int     getAttackComboCount()     { return attackComboCount; }
    public int     getAttackId()             { return attackId; }
    public boolean isWallSliding()           { return state == State.WALL_SLIDE; }
    public int     getWallDirection()        { return wallDirection; }
    public float   getWallJumpTimer()        { return wallJumpTimer; }
    public boolean isWallJumpLocked()        { return wallJumpLockTimer > 0f; }
    public boolean canDoubleJump()           { return doubleJumpAvailable; }

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
        if (grounded) {
            doubleJumpAvailable = true;
        }
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
        if (dead) return; // a dead knight can't be hit again
        hpMasks -= amount;
        if (hpMasks < 0) hpMasks = 0;
        invincible = true;
        invincibleTimer = INVINCIBLE_DURATION;
        // Cancel any active action when hit.
        cancelActions();
        if (hpMasks <= 0 && !dead) {
            startDeath();
        }
    }

    public void applyKnockback(float vx, float vy) {
        if (dead) return; // no knockback on a corpse
        this.velocityX = vx;
        this.velocityY = vy;
        this.knockbackTimer = KNOCKBACK_DURATION;
        this.grounded = false;
        cancelActions();
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

    // ------------------------------------------------------------------
    //  Attack (slash) helpers
    // ------------------------------------------------------------------

    public State beginSlash() {
        boolean alt = attackComboCount > 0 && attackComboTimer > 0f;
        attackId++;
        attackTimer = (ATTACK_DURATION);
        attackComboCount = alt ? 0 : 1;
        attackComboTimer = ATTACK_COMBO_WINDOW;
        return alt ? State.SLASH_ALT : State.SLASH;
    }

    public void beginDirectionalSlash(State which) {
        attackId++;
        attackTimer = DIRECTIONAL_ATTACK_DURATION;
        attackComboCount = 0;
        attackComboTimer = 0f;
        setState(which);
    }

    public void beginWallSlash() {
        attackId++;
        attackTimer = WALL_SLASH_DURATION;
        attackComboCount = 0;
        attackComboTimer = 0f;
        setState(State.WALL_SLASH);
    }

    /** Tick attack timers; clears the active attack when the animation finishes. */
    public void tickAttack(float delta) {
        if (attackTimer > 0f) {
            attackTimer -= delta;
            if (attackTimer <= 0f) {
                attackTimer = 0f;
            }
        }
        if (attackComboTimer > 0f) {
            attackComboTimer -= delta;
            if (attackComboTimer <= 0f) {
                attackComboTimer = 0f;
                attackComboCount = 0; // combo expired
            }
        }
    }

    /** @return true if the active slash animation has finished playing. */
    public boolean attackAnimationFinished() {
        return attackTimer <= 0f;
    }

    //  Dash helpers

    public void beginDash(int direction) {
        if (dashCooldownTimer > 0f) return;
        dashing = true;
        dashingDown = false;
        dashDirection = direction;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        velocityY = 0f; // dash is horizontal, ignore gravity during the burst
        velocityX = direction * DASH_SPEED;
        facingRight = (direction > 0);
        setState(State.DASH);
    }

    public void beginDashDown() {
        if (dashCooldownTimer > 0f) return;
        dashing = false;
        dashingDown = true;
        dashDirection = 0;
        dashDownTimer = DASH_DOWN_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        velocityX = 0f;
        velocityY = -DASH_DOWN_SPEED;
        setState(State.DASH_DOWN);
    }

    public void beginDashDownLand() {
        dashingDown = false;
        dashDownLandTimer = DASH_DOWN_LAND_DURATION;
        velocityY = 0f;
        velocityX = 0f;
        setState(State.DASH_DOWN_LAND);
    }

    public void tickDash(float delta) {
        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= delta;
            if (dashCooldownTimer < 0f) dashCooldownTimer = 0f;
        }
        if (dashTimer > 0f) {
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                dashTimer = 0f;
                dashing = false;
                // Preserve a sliver of momentum so the transition feels good.
                velocityX *= 0.4f;
            }
        }
        if (dashDownTimer > 0f) {
            dashDownTimer -= delta;
            if (dashDownTimer <= 0f) {
                dashDownTimer = 0f;
                // The dash-down only ends when the knight hits the ground;
                // while airborne, we keep the high downward velocity.
            }
        }
        if (dashDownLandTimer > 0f) {
            dashDownLandTimer -= delta;
            if (dashDownLandTimer <= 0f) {
                dashDownLandTimer = 0f;
            }
        }
    }

    public void cancelDash() {
        dashing = false;
        dashingDown = false;
        dashTimer = 0f;
        dashDownTimer = 0f;
    }

    //  Wall helpers

    public void setWallDirection(int dir) {
        this.wallDirection = dir;
    }

    public void beginWallSlide() {
        setState(State.WALL_SLIDE);
    }

    public void beginWallJump(int wallDir) {
        velocityX = -wallDir * WALL_JUMP_VX;
        velocityY = WALL_JUMP_VY;
        facingRight = (wallDir < 0); // face away from the wall
        wallDirection = 0; // detach from wall
        wallJumpTimer = WALL_JUMP_DURATION;
        wallJumpLockTimer = WALL_JUMP_LOCK;
        grounded = false;
        doubleJumpAvailable = true; // wall jump refreshes double jump
        setState(State.WALL_JUMP);
    }

    public void tickWallJump(float delta) {
        if (wallJumpTimer > 0f) {
            wallJumpTimer -= delta;
            if (wallJumpTimer <= 0f) wallJumpTimer = 0f;
        }
        if (wallJumpLockTimer > 0f) {
            wallJumpLockTimer -= delta;
            if (wallJumpLockTimer <= 0f) wallJumpLockTimer = 0f;
        }
    }

    //  Double jump

    public void performDoubleJump() {
        velocityY = DOUBLE_JUMP_IMPULSE;
        doubleJumpAvailable = false;
        setState(State.DOUBLE_JUMP);
    }

    //  Pogo

    public void pogoBounce() {
        velocityY = POGO_IMPULSE;
        doubleJumpAvailable = true;
        dashCooldownTimer = 0f;
        dashing = false;
        dashingDown = false;
        dashTimer = 0f;
        dashDownTimer = 0f;
        attackTimer = 0f; // end the slash so the state machine takes over
        setState(State.JUMP);
    }

    //  Death

    private void startDeath() {
        dead = true;
        deathTimer = DEATH_DURATION;
        cancelActions();
        setState(State.DEATH);
    }

    public void tickDeath(float delta) {
        if (!dead) return;
        if (deathTimer > 0f) {
            deathTimer -= delta;
            if (deathTimer <= 0f) deathTimer = 0f;
        }
    }

    public boolean deathAnimationFinished() {
        return dead && deathTimer <= 0f;
    }

    //  Bookkeeping

    /** Cancel every active action state — used when the knight is interrupted. */
    private void cancelActions() {
        attackTimer = 0f;
        // Combo is intentionally preserved so a hit mid-combo doesn't waste the chain.
        cancelDash();
        wallJumpTimer = 0f;
        wallJumpLockTimer = 0f;
        dashDownLandTimer = 0f;
    }
}
