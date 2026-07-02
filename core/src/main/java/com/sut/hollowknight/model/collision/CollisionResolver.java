package com.sut.hollowknight.model.collision;

import com.sut.hollowknight.model.Knight;

/**
 * Resolves collisions between the Knight and the tile map's
 * rectangle-based collision layer. Returns whether the knight
 * is standing on a surface after vertical resolution.
 */
public final class CollisionResolver {

    private final TileMapCollider collider;

    public CollisionResolver(TileMapCollider collider) {
        this.collider = collider;
    }

    /** Push knight out of collision rectangles on the X axis. */
    public void resolveHorizontal(Knight knight) {
        float vx = knight.getVelocityX();
        if (vx == 0) return;

        for (CollisionRect rect : collider.getCollisionRects()) {
            if (!rect.overlaps(knight.getLeft(), knight.getBottom(),
                knight.getRight(), knight.getTop())) continue;

            if (vx > 0) {
                knight.setX(rect.getLeft() - Knight.KNIGHT_WIDTH / 2f);
            } else {
                knight.setX(rect.getRight() + Knight.KNIGHT_WIDTH / 2f);
            }
            knight.setVelocityX(0);
        }
    }

    /**
     * Push knight out of collision rectangles on the Y axis.
     * @param prevY the knight's Y before this frame's vertical move.
     * @return true if the knight landed on top of a rectangle.
     */
    public boolean resolveVertical(Knight knight, float prevY) {
        float vy = knight.getVelocityY();
        boolean standing = false;

        float prevBottom = prevY;
        float prevTop    = prevY + Knight.KNIGHT_HEIGHT;

        for (CollisionRect rect : collider.getCollisionRects()) {
            if (!rect.overlaps(knight.getLeft(), knight.getBottom(),
                knight.getRight(), knight.getTop())) continue;

            if (vy <= 0 && prevBottom >= rect.getTop() - 1f) {
                knight.setY(rect.getTop());
                knight.setVelocityY(0);
                standing = true;
            } else if (vy > 0 && prevTop <= rect.getBottom() + 1f) {
                knight.setY(rect.getBottom() - Knight.KNIGHT_HEIGHT);
                knight.setVelocityY(0);
            }
        }
        return standing;
    }

    public TileMapCollider getCollider() { return collider; }
}
