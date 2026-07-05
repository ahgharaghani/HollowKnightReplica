package com.sut.hollowknight.controller;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.input.PlayerInput;
import com.sut.hollowknight.view.camera.CameraRig;

public class GameController {

    private final Knight knight;
    private final PlayerInput input;
    private final CollisionResolver collision;
    private final CameraRig cameraRig;
    private final TileMapCollider collider;

    private boolean paused = false;
    private boolean wasGrounded = false;
    private float landTimer = 0f;
    private static final float LAND_DURATION = (float) 3 / 12;

    private boolean wasJumpHeld = false;

    // ---- Game-feel timers ----
    private static final float COYOTE_TIME = 0.10f;
    private static final float JUMP_BUFFER_TIME = 0.12f;
    private float coyoteTimer = 0f;
    private float jumpBufferTimer = 0f;

    // ---- Camera shake on damage ----
    private static final float DAMAGE_SHAKE_AMPLITUDE = 6f;
    private static final float DAMAGE_SHAKE_DURATION  = 0.25f;
    private boolean wasInvincible = false;

    public GameController(Knight knight, TileMapCollider collider,
                          OrthographicCamera camera, float mapWidthPx, float mapHeightPx) {
        this.knight    = knight;
        this.input     = new PlayerInput();
        this.collider  = collider;
        this.collision = new CollisionResolver(collider);
        this.cameraRig = new CameraRig(camera, mapWidthPx, mapHeightPx);
    }

    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused()             { return paused; }

    public void update(float delta) {
        if (paused) return;

        // Death freezes the world for the knight; only the death animation plays.
        if (knight.isDead()) {
            knight.addStateTime(delta);
            knight.tickDeath(delta);
            cameraRig.follow(knight.getX(), knight.getY(), delta);
            return;
        }

        handleInput();
        applyPhysics(delta);
        updateAnimationState(delta);

        // Shake on the exact frame the knight takes a hit (i-frame rising edge).
        boolean invincibleNow = knight.isInvincible();
        if (invincibleNow && !wasInvincible) {
            cameraRig.shake(DAMAGE_SHAKE_AMPLITUDE, DAMAGE_SHAKE_DURATION);
        }
        wasInvincible = invincibleNow;

        cameraRig.follow(knight.getX(), knight.getY(), delta);
    }

    private void handleInput() {
        boolean jumpHeld = input.isJumpPressed();

        if (knight.isDead()) return;

        // --- During knockback the player has no control ---
        if (knight.isInKnockback()) {
            wasJumpHeld = jumpHeld;
            return;
        }

        // --- Wall-jump input lock: player can't steer mid wall-jump ---
        if (knight.isWallJumpLocked()) {
            wasJumpHeld = jumpHeld;
            return;
        }

        // --- Dashing: ignore normal horizontal input; the dash owns velocity ---
        if (knight.isDashing() || knight.isDashingDown()) {
            // Allow jump out of a horizontal dash to cancel it.
            if (input.isJumpJustPressed() && knight.isDashing() && !knight.isGrounded()) {
                knight.cancelDash();
                performJump();
            }
            // Allow attack during dash (dash-slash feel).
            if (input.isAttackJustPressed()) {
                triggerAttack();
            }
            wasJumpHeld = jumpHeld;
            return;
        }

        boolean left  = input.isMoveLeftPressed();
        boolean right = input.isMoveRightPressed();

        if (left && !right) {
            knight.setVelocityX(-Knight.MOVE_SPEED);
            knight.setFacingRight(false);
        } else if (right && !left) {
            knight.setVelocityX(Knight.MOVE_SPEED);
            knight.setFacingRight(true);
        } else {
            knight.setVelocityX(0);
        }

        // --- Jump (buffered ground jump, coyote time, double-jump, wall-jump) ---
        if (input.isJumpJustPressed()) {
            jumpBufferTimer = JUMP_BUFFER_TIME;
        }
        if (jumpBufferTimer > 0f) {
            if (knight.isGrounded() || coyoteTimer > 0f) {
                // Buffered/coyote ground jump — forgiving on ledges and landings.
                performJump();
            } else if (input.isJumpJustPressed()) {
                // Air options fire on the actual press only, never from the buffer.
                int wd = knight.getWallDirection();
                if (knight.isWallSliding() || wd != 0) {
                    if (wd != 0) {
                        knight.beginWallJump(wd);
                        jumpBufferTimer = 0f;
                    } else if (knight.canDoubleJump()) {
                        knight.performDoubleJump();
                        jumpBufferTimer = 0f;
                    }
                } else if (knight.canDoubleJump()) {
                    knight.performDoubleJump();
                    jumpBufferTimer = 0f;
                }
            }
        }

        if (wasJumpHeld && !jumpHeld && knight.getVelocityY() > 0f) {
            knight.cutJumpVelocity();
        }

        // --- Attack (slash) ---
        if (input.isAttackJustPressed()) {
            triggerAttack();
        }

        // --- Dash ---
        if (input.isDashJustPressed() && !knight.isDashOnCooldown()) {
            if (input.isMoveDownPressed() && !knight.isGrounded()) {
                knight.beginDashDown();
            } else {
                int dir = 0;
                if (left && !right)       dir = -1;
                else if (right && !left)  dir = +1;
                else                       dir = (knight.isFacingRight() ? +1 : -1);
                knight.beginDash(dir);
            }
        }

        wasJumpHeld = jumpHeld;
    }

    private void performJump() {
        landTimer = 0;
        jumpBufferTimer = 0f;
        coyoteTimer = 0f;
        knight.setVelocityY(Knight.JUMP_IMPULSE);
        knight.setGrounded(false);
        knight.setState(Knight.State.JUMP);
        wasGrounded = false;
    }

    private void triggerAttack() {
        if (knight.isAttacking()) return; // can't re-trigger mid-swing

        if (knight.isWallSliding()) {
            knight.beginWallSlash();
            return;
        }
        if (input.isMoveUpPressed()) {
            knight.beginDirectionalSlash(Knight.State.UP_SLASH);
            return;
        }
        if (input.isMoveDownPressed() && !knight.isGrounded()) {
            knight.beginDirectionalSlash(Knight.State.DOWN_SLASH);
            return;
        }

        Knight.State s = knight.beginSlash();
        knight.setState(s);
    }

    private void applyPhysics(float delta) {
        knight.addStateTime(delta);

        // Tick every action timer once per frame.
        knight.tickAttack(delta);
        knight.tickDash(delta);
        knight.tickWallJump(delta);
        knight.tickKnockback(delta);
        knight.tickInvincibility(delta);

        int wallDir = detectWallDirection();
        knight.setWallDirection(wallDir);

        boolean pressingIntoWall =
            (wallDir < 0 && input.isMoveLeftPressed())  ||
                (wallDir > 0 && input.isMoveRightPressed());

        boolean canWallSlide = !knight.isGrounded()
            && wallDir != 0
            && pressingIntoWall
            && knight.getVelocityY() <= 0f
            && !knight.isDashing()
            && !knight.isDashingDown()
            && !knight.isAttacking()
            && !knight.isInKnockback()
            && knight.getState() != Knight.State.WALL_JUMP;

        if (canWallSlide) {
            if (knight.getVelocityY() < -Knight.WALL_SLIDE_SPEED) {
                knight.setVelocityY(-Knight.WALL_SLIDE_SPEED);
            }
            if (knight.getState() != Knight.State.WALL_SLIDE) {
                knight.beginWallSlide();
            }
        } else if (knight.getState() == Knight.State.WALL_SLIDE) {
            knight.setState(Knight.State.FALL);
        }

        if (knight.isDashing()) {
            knight.setVelocityX(knight.getDashDirection() * Knight.DASH_SPEED);
        } else if (knight.isDashingDown()) {
            knight.setVelocityX(0f);
            knight.setVelocityY(-Knight.DASH_DOWN_SPEED);
        } else if (knight.isWallJumpLocked()) {
            // Wall-jump lock
        } else if (!knight.isInKnockback()) {
            // Standard horizontal input was already applied in handleInput().
        }

        float vx = knight.getVelocityX();
        if (vx != 0) {
            knight.setX(knight.getX() + vx * delta);
        }
        collision.resolveHorizontal(knight);

        if (!knight.isDashing()) {
            knight.setVelocityY(knight.getVelocityY() - Knight.GRAVITY * delta);
        }

        if (!knight.isInKnockback()
            && !knight.isDashing()
            && !knight.isDashingDown()
            && !knight.isWallSliding()
            && !knight.isAttacking()
            && knight.getVelocityY() <= 0
            && !knight.isGrounded()
            && knight.getState() != Knight.State.WALL_JUMP) {
            knight.setState(Knight.State.FALL);
        }

        float prevY = knight.getY();
        knight.setY(prevY + knight.getVelocityY() * delta);

        boolean grounded = collision.resolveVertical(knight, prevY);

        if (knight.isDashingDown() && grounded) {
            knight.beginDashDownLand();
        } else if (knight.getState() == Knight.State.DASH_DOWN_LAND
            && knight.getDashDownLandTimer() <= 0f) {
            // Animation finished
            knight.setState(Knight.State.IDLE);
        }

        if (!knight.isInKnockback()
            && !knight.isDashingDown()
            && !wasGrounded && grounded) {
            knight.setState(Knight.State.LAND);
            landTimer = LAND_DURATION;
        }

        knight.setGrounded(grounded);
        wasGrounded = grounded;

        // Coyote window: short grace period to jump after leaving a platform
        if (grounded) {
            coyoteTimer = COYOTE_TIME;
        } else if (coyoteTimer > 0f) {
            coyoteTimer -= delta;
            if (coyoteTimer < 0f) coyoteTimer = 0f;
        }
        // Jump input buffer decays every frame.
        if (jumpBufferTimer > 0f) {
            jumpBufferTimer -= delta;
            if (jumpBufferTimer < 0f) jumpBufferTimer = 0f;
        }
    }

    private int detectWallDirection() {
        float probeOffset = 2f;
        float probeWidth  = 4f;
        float midY = knight.getY() + Knight.KNIGHT_HEIGHT * 0.5f;
        float probeH = Knight.KNIGHT_HEIGHT * 0.6f;

        // Left probe
        float leftL = knight.getLeft() - probeOffset - probeWidth;
        float leftR = knight.getLeft() - probeOffset;
        boolean wallLeft = collider.overlapsAnyRect(
            leftL, midY - probeH / 2f, leftR, midY + probeH / 2f);

        // Right probe
        float rightL = knight.getRight() + probeOffset;
        float rightR = knight.getRight() + probeOffset + probeWidth;
        boolean wallRight = collider.overlapsAnyRect(
            rightL, midY - probeH / 2f, rightR, midY + probeH / 2f);

        if (wallLeft)  return -1;
        if (wallRight) return +1;
        return 0;
    }

    private void updateAnimationState(float delta) {
        if (knight.isInKnockback()) {
            return;
        }

        if (knight.isDashing()) {
            // Dash animation owns the state until the dash timer ends.
            if (knight.getState() != Knight.State.DASH) {
                knight.setState(Knight.State.DASH);
            }
            return;
        }
        if (knight.isDashingDown()) {
            if (knight.getState() != Knight.State.DASH_DOWN) {
                knight.setState(Knight.State.DASH_DOWN);
            }
            return;
        }
        if (knight.getState() == Knight.State.DASH_DOWN_LAND) {
            // Wait for the land animation to finish; let applyPhysics clear it.
            return;
        }
        if (knight.isAttacking()) {
            // The slash animation owns the state until attackTimer runs out.
            return;
        }
        if (knight.getState() == Knight.State.WALL_JUMP) {
            if (knight.getWallJumpTimer() > 0f) return;
            // Once the wall-jump animation finishes, fall through to airborne.
        }
        if (knight.isWallSliding()) {
            // Wall slide state is set/cleared in applyPhysics; just bail here.
            return;
        }
        if (knight.getState() == Knight.State.DOUBLE_JUMP) {
            // Stay in DOUBLE_JUMP for one animation cycle, then fall through.
            // Reuse jump timer heuristic: when stateTime exceeds 0.4s, leave.
            if (knight.getStateTime() < 0.40f
                && knight.getVelocityY() > 0f) {
                return;
            }
        }

        if (landTimer > 0) {
            landTimer -= delta;
            if (landTimer <= 0) {
                knight.setState(Knight.State.IDLE);
            }
            return;
        }

        if (knight.isGrounded()) {
            if (input.isMoving()) {
                knight.setState(Knight.State.RUN);
            } else if (knight.getHpMasks() <= Knight.LOW_HEALTH_THRESHOLD
                && knight.getHpMasks() > 0) {
                knight.setState(Knight.State.IDLE_LOW_HEALTH);
            } else {
                knight.setState(Knight.State.IDLE);
            }
        } else {
            // Airborne and not in any action state → jump or fall.
            if (knight.getVelocityY() > 0f) {
                knight.setState(Knight.State.JUMP);
            } else {
                knight.setState(Knight.State.FALL);
            }
        }
    }

    public Knight getKnight()            { return knight; }
    public TileMapCollider getCollider() { return collision.getCollider(); }
    public CameraRig getCameraRig()      { return cameraRig; }
}
