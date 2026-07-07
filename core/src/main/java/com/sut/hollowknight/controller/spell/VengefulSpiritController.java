package com.sut.hollowknight.controller.spell;

import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.spell.VengefulSpirit;

/**
 * Drives a single {@link VengefulSpirit} fireball: constant-speed travel with
 * no gravity, wall detection that ends the ball, and the short dissipation
 * (Ball End) tail before it is dropped.
 */
public class VengefulSpiritController {

    private final VengefulSpirit spirit;
    private final TileMapCollider collider;

    // The IMPACT state must live long enough for BOTH the ball's dissipation
    // (Ball End, 3 frames @ 24 FPS) and the wall burst (Ball Wall Impact,
    // 7 frames @ 24 FPS) to finish. The wall burst is the longer of the two.
    private static final float IMPACT_DURATION = 7f / 24f;

    // The ball must also outlive the Blast on the knight (8 frames @ 24 FPS),
    // in case a wall is struck immediately after release.
    private static final float MIN_LIFETIME = 8f / 24f;

    public VengefulSpiritController(VengefulSpirit spirit, TileMapCollider collider) {
        this.spirit = spirit;
        this.collider = collider;
    }

    public void update(float delta) {
        spirit.addStateTime(delta);

        switch (spirit.getState()) {
            case FLYING: updateFlying(delta); break;
            case IMPACT: updateImpact(delta); break;
            default: break;
        }
    }

    private void updateFlying(float delta) {
        spirit.move(delta);

        // Hit an environmental wall → dissipate at the wall face.
        CollisionRect probe = spirit.getProbeBox();
        if (collider.overlapsAnyRect(probe)) {
            spirit.setState(VengefulSpirit.State.IMPACT);
            return;
        }

        // Safety net: never let a ball fly forever if it clears the map.
        if (spirit.hasExceededRange()) {
            spirit.setState(VengefulSpirit.State.DONE);
        }
    }

    private void updateImpact(float delta) {
        if (spirit.getStateTime() >= IMPACT_DURATION
            && spirit.getAge() >= MIN_LIFETIME) {
            spirit.setState(VengefulSpirit.State.DONE);
        }
    }

    public VengefulSpirit getSpirit() { return spirit; }
    public boolean isDone()           { return spirit.isDone(); }
}
