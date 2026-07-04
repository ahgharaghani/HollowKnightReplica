package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;

public class JavelinController {

    private final Javelin javelin;
    private final TileMapCollider collider;

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

        CollisionRect wall = collider.findOverlappingRect(javelin);
        if (wall != null) {
            CollisionResolver.pushOutHorizontally(javelin, wall);
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
