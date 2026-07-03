package com.sut.hollowknight.model.collision;

public interface PhysicsBody extends AABB {
    float getHalfWidth();
    void setCenterX(float x);
    float getVelocityX();
}
