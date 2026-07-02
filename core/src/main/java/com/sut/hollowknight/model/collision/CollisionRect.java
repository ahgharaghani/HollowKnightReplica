package com.sut.hollowknight.model.collision;

/**
 * Immutable axis-aligned bounding box used for collision detection.
 * Stored in world coordinates (y-up). Converted from Tiled's y-down
 * coordinate system when loaded from the map.
 */
public class CollisionRect {

    private final float x;      // left edge
    private final float y;      // bottom edge (y-up)
    private final float width;
    private final float height;

    public CollisionRect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }

    public float getLeft()   { return x; }
    public float getRight()  { return x + width; }
    public float getBottom() { return y; }
    public float getTop()    { return y + height; }

    /**
     * Check if this rectangle overlaps another.
     */
    public boolean overlaps(CollisionRect other) {
        return this.getLeft()   < other.getRight()
            && this.getRight()  > other.getLeft()
            && this.getBottom() < other.getTop()
            && this.getTop()    > other.getBottom();
    }

    /**
     * Check if this rectangle overlaps an AABB defined by its edges.
     */
    public boolean overlaps(float left, float bottom, float right, float top) {
        return this.getLeft()   < right
            && this.getRight()  > left
            && this.getBottom() < top
            && this.getTop()    > bottom;
    }

    @Override
    public String toString() {
        return String.format("CollisionRect[x=%.1f, y=%.1f, w=%.1f, h=%.1f]", x, y, width, height);
    }
}
