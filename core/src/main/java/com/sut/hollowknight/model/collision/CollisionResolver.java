package com.sut.hollowknight.model.collision;

import com.sut.hollowknight.model.Knight;

public class CollisionResolver {

    private final TileMapCollider collider;

    public CollisionResolver(TileMapCollider collider) {
        this.collider = collider;
    }

    public static void pushOutHorizontally(PhysicsBody body, CollisionRect wall) {
        if (body.getVelocityX() > 0) {
            body.setCenterX(wall.getLeft() - body.getHalfWidth());
        } else {
            body.setCenterX(wall.getRight() + body.getHalfWidth());
        }
    }
    
    public void resolveHorizontal(Knight knight) {
        float vx = knight.getVelocityX();
        if (vx == 0) return;

        for (CollisionRect rect : collider.getCollisionRects()) {
            if (!AABB.overlaps(knight, rect)) continue;

            pushOutHorizontally(knight, rect);
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
            if (!AABB.overlaps(knight, rect)) continue;

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
