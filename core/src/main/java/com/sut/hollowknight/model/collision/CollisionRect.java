package com.sut.hollowknight.model.collision;

public class CollisionRect implements AABB {

    private float x;      // left edge
    private float y;      // bottom edge (y-up)
    private float width;
    private float height;

    public CollisionRect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public CollisionRect set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }

    @Override
    public float getLeft()   { return x; }

    @Override
    public float getRight()  { return x + width; }

    @Override
    public float getBottom() { return y; }

    @Override
    public float getTop()    { return y + height; }

    public boolean overlaps(CollisionRect other) {
        return AABB.overlaps(this, other);
    }

    public boolean overlaps(float left, float bottom, float right, float top) {
        return AABB.overlaps(this, left, bottom, right, top);
    }

    @Override
    public String toString() {
        return String.format("CollisionRect[x=%.1f, y=%.1f, w=%.1f, h=%.1f]", x, y, width, height);
    }
}
