package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;

public class JavelinController {

    private final Javelin javelin;
    private final TileMapCollider collider;

    /** How deep the tip visually embeds into the wall face when sticking. */
    private static final float TIP_EMBED = 14f;

    public JavelinController(Javelin javelin, TileMapCollider collider) {
        this.javelin = javelin;
        this.collider = collider;
    }

    public void update(float delta) {
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

        float dir = javelin.isFacingRight() ? 1f : -1f;
        float tipX = javelin.getX() + dir * Javelin.DMG_TIP_REACH;
        CollisionRect wall = collider.findOverlappingRect(
            tipX - 4f, javelin.getBottom(), tipX + 4f, javelin.getTop());
        if (wall != null) {
            float face = dir > 0 ? wall.getLeft() : wall.getRight();
            javelin.setX(face - dir * (Javelin.DMG_TIP_REACH - TIP_EMBED));
            javelin.setState(Javelin.State.STICK);
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
