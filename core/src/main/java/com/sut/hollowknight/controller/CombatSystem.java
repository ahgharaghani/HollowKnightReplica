package com.sut.hollowknight.controller;

import com.sut.hollowknight.controller.enemy.EnemyController;
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.enemy.Javelin;

import java.util.List;

public class CombatSystem {

    // Contact damage (enemy body touching the knight)
    private static final int   CONTACT_DAMAGE      = 1;
    private static final float CONTACT_KNOCKBACK_X = 420f;
    private static final float CONTACT_KNOCKBACK_Y = 260f;

    // Javelin damage
    private static final float JAVELIN_KNOCKBACK_X = 380f;
    private static final float JAVELIN_KNOCKBACK_Y = 260f;

    // Nail attack
    private static final int   NAIL_DAMAGE = 1;
    /** 1 = standard recoil strength (a Heavy Blow charm would pass ~1.75). */
    private static final float NAIL_KNOCKBACK_SCALE = 1f;

    private final Knight knight;
    private final List<EnemyController> enemies;

    public CombatSystem(Knight knight, List<EnemyController> enemies) {
        this.knight = knight;
        this.enemies = enemies;
    }

    public void resolve(float delta) {
        for (int i = 0; i < enemies.size(); i++) {
            EnemyController enemy = enemies.get(i);
            resolveNailHit(enemy);
            resolveContactDamage(enemy);
            if (enemy instanceof WingedSentryController) {
                resolveJavelinDamage((WingedSentryController) enemy);
            }
        }
    }

    /** Knight's active slash vs the enemy body. One hit per swing per enemy. */
    private void resolveNailHit(EnemyController enemy) {
        if (!knight.isAttacking()) return;
        if (!enemy.isAlive()) return;
        if (enemy.getLastNailHitId() == knight.getAttackId()) return;

        CollisionRect slash = knight.getActiveSlashBox();
        if (slash == null || !AABB.overlaps(enemy.getBodyBox(), slash)) return;

        enemy.setLastNailHitId(knight.getAttackId());

        // The attack direction drives the recoil
        float dirX = 0f;
        float dirY = 0f;
        switch (knight.getState()) {
            case UP_SLASH:   dirY = 1f;  break;
            case DOWN_SLASH: dirY = -1f; break;
            default:
                dirX = enemy.getBodyBox().getCenterX() >= knight.getX() ? 1f : -1f;
                break;
        }
        enemy.hitByNail(NAIL_DAMAGE, dirX, dirY, NAIL_KNOCKBACK_SCALE);

        knight.addSoul(Knight.SOUL_PER_NAIL_HIT);

        if (knight.getState() == Knight.State.DOWN_SLASH) {
            knight.pogoBounce();
        }
    }

    private void resolveContactDamage(EnemyController enemy) {
        if (knight.isInvincible()) return;
        if (!enemy.isAlive() || !enemy.overlapsKnight()) return;

        int knockbackDir = knight.getX() < enemy.getBodyBox().getCenterX() ? -1 : 1;
        knight.takeDamage(CONTACT_DAMAGE);
        knight.applyKnockback(knockbackDir * CONTACT_KNOCKBACK_X, CONTACT_KNOCKBACK_Y);
    }

    private void resolveJavelinDamage(WingedSentryController sc) {
        if (knight.isInvincible()) return;

        Javelin javelin = sc.getJavelin();
        if (javelin == null || javelin.getState() != Javelin.State.FLYING) return;

        CollisionRect damageBox = javelin.getDamageBox();
        CollisionRect hurtBox   = knight.getHurtBox();
        if (!AABB.overlaps(damageBox, hurtBox)) return;

        int knockbackDir = knight.getX() < javelin.getCenterX() ? -1 : 1;
        knight.takeDamage(Javelin.DAMAGE);
        knight.applyKnockback(knockbackDir * JAVELIN_KNOCKBACK_X, JAVELIN_KNOCKBACK_Y);
        javelin.setState(Javelin.State.SNAP);
    }
}
