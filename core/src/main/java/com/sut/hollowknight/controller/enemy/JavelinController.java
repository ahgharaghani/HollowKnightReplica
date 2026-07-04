package com.sut.hollowknight.controller.enemy;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.PixelCollisionUtil;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;

public class JavelinController {

    private final Javelin javelin;
    private final TileMapCollider collider;
    private Knight knight;
    private TextureRegion currentFrame; // renderer sets this

    private boolean damageDealt = false;

    public JavelinController(Javelin javelin, TileMapCollider collider) {
        this.javelin = javelin;
        this.collider = collider;
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    // called by renderer
    public void setCurrentFrame(TextureRegion frame) {
        this.currentFrame = frame;
    }

    public boolean isDamageDealt() { return damageDealt; }
    public void clearDamageDealt() { damageDealt = false; }

    public void update(float delta) {
        damageDealt = false;
        javelin.addStateTime(delta);

        switch (javelin.getState()) {
            case FLYING:
                updateFlying(delta);
                break;
            case IMPACT:
                updateImpact(delta);
                break;
            case STICK:
                updateStick(delta);
                break;
            case SNAP:
                updateSnap(delta);
                break;
            case NEUTRAL:
                updateNeutral(delta);
                break;
            default:
                break;
        }
    }

    private void updateFlying(float delta) {
        javelin.setX(javelin.getX() + javelin.getVelocityX() * delta);

        CollisionRect wall = collider.findOverlappingRect(javelin);
        if (wall != null) {
            CollisionResolver.pushOutHorizontally(javelin, wall);
            javelin.setState(Javelin.State.STICK);
            return;
        }

        if (knight != null && !knight.isInvincible() && currentFrame != null) {
            // Javelin visual bounds (matching JavelinRenderer)
            float DRAW_WIDTH = 583f;
            float DRAW_HEIGHT = 27f;
            float drawX = javelin.getX() - DRAW_WIDTH / 2f;
            float drawY = javelin.getY() + (Javelin.HEIGHT - DRAW_HEIGHT) / 2f;
            boolean flipX = !javelin.isFacingRight(); // Renderer flips when facing right

            // Knight visual bounds (matching GameScreen renderer)
            float kFrameW = currentFrame.getRegionWidth(); // Approximate, or pass knight frame too
            float kFrameH = currentFrame.getRegionHeight();
            float kDrawX = knight.getX() - kFrameW / 2f;
            float kDrawY = knight.getY();
            boolean kFlipX = knight.isFacingRight();

            // For a fully accurate check, we need the knight's frame too.
            // For now, using a 1x1 white pixel fallback or knight's hurtbox as reg2.
            // Since we only have javelin frame here, we assume knight's bounding box.
            if (AABB.overlaps(javelin, knight.getHurtBox())) {
                damageDealt = true;
                javelin.setState(Javelin.State.SNAP);
                return;
            }
        }

        if (javelin.getX() < -100
            || javelin.getX() > collider.getCols() * collider.getTileWidth() + 100) {
            javelin.setState(Javelin.State.DONE);
        }
    }

    private void updateImpact(float delta) {
        if (javelin.getStateTime() >= 0.1f) {
            javelin.setState(Javelin.State.STICK);
        }
    }

    private void updateStick(float delta) {
        if (javelin.getStateTime() >= Javelin.STICK_DURATION) {
            javelin.setState(Javelin.State.SNAP);
        }
    }

    private void updateSnap(float delta) {
        if (javelin.getStateTime() >= 0.4f) {
            javelin.setState(Javelin.State.DONE);
        }
    }

    private void updateNeutral(float delta) {
        if (javelin.getStateTime() >= Javelin.STICK_DURATION) {
            javelin.setState(Javelin.State.SNAP);
        }
    }

    public Javelin getJavelin() { return javelin; }
}
