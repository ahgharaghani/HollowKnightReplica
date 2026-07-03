package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;

public class JavelinController {

    private final Javelin javelin;
    private final TileMapCollider collider;
    private Knight knight;

    private boolean damageDealt = false;

    public JavelinController(Javelin javelin, TileMapCollider collider) {
        this.javelin = javelin;
        this.collider = collider;
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    public boolean isDamageDealt() { return damageDealt; }
    public void clearDamageDealt() { damageDealt = false; }

    public void update(float delta) {
        // Reset damage flag BEFORE the FSM step so a SNAP this frame
        // is correctly reported, and a SNAP last frame is cleared.
        damageDealt = false;

        // Advance the animation clock so the renderer's
        // animation.getKeyFrame(stateTime) actually progresses.
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
            // Shared wall push-out — same helper the Knight and the
            // sentry charge use. No more copy-pasted branch.
            CollisionResolver.pushOutHorizontally(javelin, wall);
            javelin.setState(Javelin.State.STICK);
            return;
        }

        if (knight != null && !knight.isInvincible() && AABB.overlaps(javelin, knight)) {
            damageDealt = true;
            javelin.setState(Javelin.State.SNAP);
            return;
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
        // 4 frames at 10 fps = 0.4s; the SNAP animation's own duration
        // matches this, so the renderer will clamp to the final frame.
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
