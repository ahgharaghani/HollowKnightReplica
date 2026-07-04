// File: HollowKnight/core/src/main/java/com/sut/hollowknight/controller/GameController.java

package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.input.PlayerInput;
import com.sut.hollowknight.view.camera.CameraRig;

public class GameController {

    private final Knight knight;
    private final PlayerInput input;
    private final CollisionResolver collision;
    private final CameraRig cameraRig;

    private boolean paused = false;
    private boolean wasGrounded = false;
    private float landTimer = 0f;
    private static final float LAND_DURATION = (float) 3 / 12;

    public GameController(Game game, Knight knight, TileMapCollider collider,
                          OrthographicCamera camera, float mapWidthPx, float mapHeightPx) {
        this.knight    = knight;
        this.input     = new PlayerInput();
        this.collision = new CollisionResolver(collider);
        this.cameraRig = new CameraRig(camera, mapWidthPx, mapHeightPx);
    }

    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused()             { return paused; }

    public void update(float delta) {
        if (paused) return;

        handleInput();
        applyPhysics(delta);
        updateAnimationState(delta);
        cameraRig.follow(knight.getX(), knight.getY(), delta);
    }

    private void handleInput() {
        // During knockback the player has no control — let the throwback carry.
        if (knight.isInKnockback()) return;

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

        // Only allow jumping if not currently in the landing animation
        if (input.isJumpJustPressed() && knight.isGrounded()) {
            landTimer = 0;
            knight.setVelocityY(Knight.JUMP_IMPULSE);
            knight.setGrounded(false);
            knight.setState(Knight.State.JUMP);
            wasGrounded = false;
        }
    }

    private void applyPhysics(float delta) {
        knight.addStateTime(delta);

        float vx = knight.getVelocityX();
        if (vx != 0) {
            knight.setX(knight.getX() + vx * delta);
        }
        collision.resolveHorizontal(knight);

        knight.setVelocityY(knight.getVelocityY() - Knight.GRAVITY * delta);

        if (!knight.isInKnockback() && knight.getVelocityY() <= 0 && !knight.isGrounded()) {
            knight.setState(Knight.State.FALL);
        }

        float prevY = knight.getY();
        knight.setY(prevY + knight.getVelocityY() * delta);

        boolean grounded = collision.resolveVertical(knight, prevY);

        // Detect the exact moment the knight hits the ground
        if (!knight.isInKnockback() && !wasGrounded && grounded) {
            knight.setState(Knight.State.LAND);
            landTimer = LAND_DURATION;
        }

        knight.setGrounded(grounded);
        wasGrounded = grounded;

        knight.tickKnockback(delta);
        knight.tickInvincibility(delta);
    }

    private void updateAnimationState(float delta) {
        // HURT owns the animation until the knockback lockout ends.
        if (knight.isInKnockback()) return;

        if (landTimer > 0) {
            landTimer -= delta;
            // Allow early exit from land animation if the player starts moving
             if (landTimer <= 0) {
                knight.setState(Knight.State.IDLE);
            }
        } else {
            // Normal ground state updates
            if (knight.isGrounded()) {
                if (input.isMoving()) {
                    knight.setState(Knight.State.RUN);
                } else {
                    knight.setState(Knight.State.IDLE);
                }
            }
        }
    }

    public Knight getKnight()            { return knight; }
    public TileMapCollider getCollider() { return collision.getCollider(); }
}
