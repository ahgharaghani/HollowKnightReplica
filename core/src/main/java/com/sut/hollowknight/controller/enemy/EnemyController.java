package com.sut.hollowknight.controller.enemy;

public interface EnemyController {
    void update(float delta);
    void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale);
    boolean overlapsKnight();
    void respawn();
    int getLastNailHitId();
    void setLastNailHitId(int attackId);
}
