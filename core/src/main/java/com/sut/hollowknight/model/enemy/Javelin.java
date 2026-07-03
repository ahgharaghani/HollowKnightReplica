package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.PhysicsBody;

public class Javelin implements PhysicsBody {

    // Position
    private float x;
    private float y;

    // Physics
    private float velocityX;
    private float velocityY;
    public static final float SPEED = 450f;

    // Size
    public static final float WIDTH  = 60f;
    public static final float HEIGHT = 12f;

    // State
    public enum State {
        FLYING,
        IMPACT,
        STICK,
        SNAP,
        NEUTRAL,
        DONE
    }

    private State state = State.FLYING;
    private float stateTime;
    private boolean facingRight;

    // Lifetime
    public static final float STICK_DURATION = 3.0f;

    // Damage
    public static final int DAMAGE = 1;

    public Javelin(float x, float y, float dirX) {
        this.x = x;
        this.y = y;
        this.velocityX = SPEED * dirX;
        this.velocityY = 0;
        this.facingRight = dirX > 0;
    }

    // Collision box (bottom-y convention, matches Knight & WingedSentry)

    @Override
    public float getLeft()   { return x - WIDTH / 2f; }
    @Override
    public float getRight()  { return x + WIDTH / 2f; }
    @Override
    public float getBottom() { return y; }
    @Override
    public float getTop()    { return y + HEIGHT; }

    // ---- PhysicsBody ----

    @Override
    public float getHalfWidth() { return WIDTH / 2f; }

    @Override
    public void setCenterX(float cx) { this.x = cx; }

    @Override
    public float getVelocityX()    { return velocityX; }

    // Getters

    public float getX()            { return x; }
    public float getY()            { return y; }
    public float getVelocityY()    { return velocityY; }
    public boolean isFacingRight() { return facingRight; }
    public State getState()        { return state; }
    public float getStateTime()    { return stateTime; }
    public boolean isDone()        { return state == State.DONE; }

    // Mutations

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public void setState(State newState) {
        if (this.state != newState) {
            this.stateTime = 0f;
        }
        this.state = newState;
    }

    public void addStateTime(float delta) {
        this.stateTime += delta;
    }
}
