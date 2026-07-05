package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.collision.AABB;

public interface EnemyController {
    void update(float delta);
    void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale);
    boolean overlapsKnight();
    void respawn();
    boolean isAlive();
    AABB getBodyBox();
    int getLastNailHitId();
    void setLastNailHitId(int attackId);
}
