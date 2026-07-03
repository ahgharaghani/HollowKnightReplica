package com.sut.hollowknight.model.collision;

public interface AABB {
    float getLeft();
    float getRight();
    float getBottom();
    float getTop();

    default float getCenterX() {
        return (getLeft() + getRight()) * 0.5f;
    }

    default float getCenterY() {
        return (getBottom() + getTop()) * 0.5f;
    }

    static boolean overlaps(AABB a, AABB b) {
        return a.getLeft()   < b.getRight()
            && a.getRight()  > b.getLeft()
            && a.getBottom() < b.getTop()
            && a.getTop()    > b.getBottom();
    }

    static boolean overlaps(AABB a, float left, float bottom, float right, float top) {
        return a.getLeft()   < right
            && a.getRight()  > left
            && a.getBottom() < top
            && a.getTop()    > bottom;
    }
}
