package com.sut.hollowknight.model.enemy;

import com.sut.hollowknight.model.collision.CollisionRect;

/**
 * The Power Mace Slam shockwave (spec: move 5, phase 2 only).
 *
 * A single ground wave that starts at the impact point and ACCELERATES as it
 * travels (spec), dealing double mace damage. One reusable instance lives on
 * the boss controller — spawn() re-arms it, so the game loop never allocates.
 */
public class FalseKnightShockwave {

    public static final float WIDTH  = 70f;
    public static final float HEIGHT = 46f;
    /** Double the mace contact damage (spec). */
    public static final int DAMAGE = 2;

    public static final float START_SPEED = 260f;
    public static final float ACCELERATION = 560f;
    public static final float MAX_LIFETIME = 3.5f;

    private boolean active;
    private float x;        // wave center
    private float y;        // ground line (wave bottom)
    private float dir;      // -1 or +1
    private float speed;
    private float life;

    /** Reusable damage box — no allocation in the game loop. */
    private final CollisionRect damageBox = new CollisionRect(0, 0, 0, 0);

    public void spawn(float x, float y, float dir) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.speed = START_SPEED;
        this.life = 0f;
        this.active = true;
    }

    /** Integrates the accelerating travel. Deactivates on timeout. */
    public void update(float delta) {
        if (!active) return;
        speed += ACCELERATION * delta;
        x += dir * speed * delta;
        life += delta;
        if (life >= MAX_LIFETIME) active = false;
    }

    public void deactivate() { active = false; }

    public boolean isActive() { return active; }
    public float getX()       { return x; }
    public float getY()       { return y; }
    public float getDir()     { return dir; }
    public float getSpeed()   { return speed; }

    public CollisionRect getDamageBox() {
        return damageBox.set(x - WIDTH / 2f, y, WIDTH, HEIGHT);
    }
}
