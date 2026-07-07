package com.sut.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.sut.hollowknight.controller.enemy.EnemyController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;

import java.util.List;

/**
 * Cheat codes (spec Part 3). Every combo requires LEFT CTRL to be held so the
 * cheats never conflict with normal gameplay keys:
 *
 *   Ctrl+B  teleport to the Boss Arena marker
 *   Ctrl+F  toggle Noclip / Spectator mode
 *   Ctrl+H  arm the one-shot Emergency Heal
 *   Ctrl+S  refill the Soul vessel
 *   Ctrl+G  toggle God Mode
 *   Ctrl+K  insta-kill every living enemy (bonus cheat)
 *
 * Polls raw keys via Gdx.input (deliberately NOT PlayerInput: cheats must not
 * be rebindable). No allocations per frame.
 */
public class CheatController {

    private static final String TAG = "Cheats";

    /** Damage large enough to fell any enemy in one hit. */
    private static final int INSTA_KILL_DAMAGE = 9999;

    private final Knight knight;
    private final List<EnemyController> enemies;
    private final float bossArenaX;
    private final float bossArenaY;

    public CheatController(Knight knight, List<EnemyController> enemies,
                           float bossArenaX, float bossArenaY) {
        this.knight = knight;
        this.enemies = enemies;
        this.bossArenaX = bossArenaX;
        this.bossArenaY = bossArenaY;
    }

    /** Call once per frame while gameplay is running (not paused). */
    public void update() {
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            teleportToBossArena();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            boolean on = knight.toggleNoclip();
            Gdx.app.log(TAG, "Noclip " + (on ? "ON" : "OFF"));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            knight.armEmergencyHeal();
            Gdx.app.log(TAG, "Emergency Heal armed (one-shot)");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            knight.addSoul(99); // addSoul clamps to the vessel cap
            Gdx.app.log(TAG, "Soul vessel refilled");
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            boolean on = knight.toggleGodMode();
            Gdx.app.log(TAG, "God Mode " + (on ? "ON" : "OFF"));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            instaKillAll();
        }
    }

    private void teleportToBossArena() {
        knight.setPosition(bossArenaX, bossArenaY);
        knight.setVelocityX(0f);
        knight.setVelocityY(0f);
        Gdx.app.log(TAG, "Teleported to boss arena (" + bossArenaX + ", " + bossArenaY + ")");
    }

    /** Bonus cheat: fell every living enemy with a single oversized nail hit. */
    private void instaKillAll() {
        int killed = 0;
        for (int i = 0; i < enemies.size(); i++) {
            EnemyController e = enemies.get(i);
            if (!e.isAlive()) continue;
            AABB body = e.getBodyBox();
            float centerX = (body.getLeft() + body.getRight()) * 0.5f;
            float dirX = centerX >= knight.getX() ? 1f : -1f;
            e.hitByNail(INSTA_KILL_DAMAGE, dirX, 0f, 0f);
            killed++;
        }
        Gdx.app.log(TAG, "Insta-kill: " + killed + " enemies felled");
    }
}
