package com.sut.hollowknight.controller;

import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;

import java.util.List;

public class CombatSystem {

    // Contact damage (sentry body touching the knight)
    private static final int   CONTACT_DAMAGE      = 1;
    private static final float CONTACT_KNOCKBACK_X = 420f;
    private static final float CONTACT_KNOCKBACK_Y = 260f;

    // Javelin damage
    private static final float JAVELIN_KNOCKBACK_X = 380f;
    private static final float JAVELIN_KNOCKBACK_Y = 260f;

    // Nail attack
    private static final int   NAIL_DAMAGE          = 1;
    private static final float NAIL_KNOCKBACK_FORCE = 150f;

    private final Knight knight;
    private final List<WingedSentryController> sentryControllers;

    public CombatSystem(Knight knight, List<WingedSentryController> sentryControllers) {
        this.knight = knight;
        this.sentryControllers = sentryControllers;
    }

    public void resolve(float delta) {
        // Index-based loop: no Iterator allocation per frame.
        for (int i = 0; i < sentryControllers.size(); i++) {
            WingedSentryController sc = sentryControllers.get(i);
            resolveNailHit(sc);
            resolveContactDamage(sc);
            resolveJavelinDamage(sc);
        }
    }

    private void resolveNailHit(WingedSentryController sc) {
        if (!knight.isAttacking()) return;

        WingedSentry sentry = sc.getSentry();
        if (!sentry.isAlive()) return;
        if (sc.getLastNailHitId() == knight.getAttackId()) return; // already hit by this swing

        CollisionRect slash = knight.getActiveSlashBox();
        if (slash == null || !AABB.overlaps(sentry, slash)) return;

        sc.setLastNailHitId(knight.getAttackId());

        int knockbackDir = sentry.getX() >= knight.getX() ? 1 : -1;
        sc.hitByNail(NAIL_DAMAGE, knockbackDir, NAIL_KNOCKBACK_FORCE);

        // Successful nail hits charge the Soul vessel (spec: +11, capped at 99).
        knight.addSoul(Knight.SOUL_PER_NAIL_HIT);

        // Pogo: a connecting down-slash relaunches the knight upward and
        // refreshes dash + double jump so platforming chains stay alive.
        if (knight.getState() == Knight.State.DOWN_SLASH) {
            knight.pogoBounce();
        }
    }

    private void resolveContactDamage(WingedSentryController sc) {
        if (knight.isInvincible()) return;

        WingedSentry sentry = sc.getSentry();
        if (!sentry.isAlive() || !sc.overlapsKnight()) return;

        int knockbackDir = knight.getX() < sentry.getX() ? -1 : 1;
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
        javelin.setState(Javelin.State.SNAP); // Trigger snap animation on hit
    }
}
