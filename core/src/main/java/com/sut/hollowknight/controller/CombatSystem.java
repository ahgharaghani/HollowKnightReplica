package com.sut.hollowknight.controller;

import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;

public class CombatSystem {

    // Contact damage (sentry body touching the knight)
    private static final int   CONTACT_DAMAGE        = 1;
    private static final float CONTACT_KNOCKBACK_X   = 420f;
    private static final float CONTACT_KNOCKBACK_Y   = 260f;

    // Javelin damage
    private static final float JAVELIN_KNOCKBACK_X   = 380f;
    private static final float JAVELIN_KNOCKBACK_Y   = 260f;

    // Nail attack
    private static final int   NAIL_DAMAGE              = 1;
    private static final float NAIL_KNOCKBACK_DISTANCE  = 15f;
    private static final float NAIL_DEATH_KNOCKBACK_SCALE = 0.3f;

    private final Knight knight;
    private final WingedSentryController sentryController;

    public CombatSystem(Knight knight, WingedSentryController sentryController) {
        this.knight = knight;
        this.sentryController = sentryController;
    }

    public void resolve(float delta) {
        resolveContactDamage();
        resolveJavelinDamage();
        // resolvePlayerAttack(); // dormant — nail-attack hook
    }

    private void resolveContactDamage() {
        if (!sentryController.overlapsKnight()) return;
        // Only knock back on an actual landed hit. During i-frames takeDamage()
        // is a no-op, but the velocity writes below would otherwise re-fire every
        // overlapping frame — pinning vy=+150 and floating the knight (and the
        // chasing sentry) skyward. Gate the whole reaction on invincibility.
        if (knight.isInvincible()) return;

        WingedSentry sentry = sentryController.getSentry();
        int knockbackDir = knight.getX() < sentry.getX() ? -1 : 1;

        knight.takeDamage(CONTACT_DAMAGE);
        knight.applyKnockback(knockbackDir * CONTACT_KNOCKBACK_X, CONTACT_KNOCKBACK_Y);
    }

    private void resolveJavelinDamage() {
        if (!sentryController.isJavelinDamageDealt()) return;

        Javelin javelin = sentryController.getJavelin();
        // Center-x is the natural pivot for "which side was the knight on";
        // works regardless of the javelin's internal y convention.
        int knockbackDir = knight.getX() < javelin.getCenterX() ? -1 : 1;

        knight.takeDamage(Javelin.DAMAGE);
        knight.applyKnockback(knockbackDir * JAVELIN_KNOCKBACK_X, JAVELIN_KNOCKBACK_Y);

        sentryController.clearJavelinDamageDealt();
    }

    /**
     * Dormant seam for the player's nail attack.
     *
     * <p>Once nail attack input and animation assets are dropped in,
     * this method (and the {@code hitByNail} call it will make on the
     * {@link WingedSentryController}) will be wired up. For now it
     * exists as a clearly-marked hook so the constants are in place
     * and the call site is obvious.</p>
     */
    public void resolvePlayerAttack() {
        // TODO: nail-attack — call sentryController.hitByNail(
        //   NAIL_DAMAGE, knockbackDir, NAIL_KNOCKBACK_DISTANCE)
        // once input + animation are in place. The death-knockback
        // scale (NAIL_DEATH_KNOCKBACK_SCALE) is currently consumed
        // inside hitByNail; if we want full single-owner of these
        // numbers we can move that logic here as a follow-up.
    }

    // Constants exposed for the (dormant) nail-attack hook

    /** Nail-attack damage per hit. */
    public int getNailDamage()             { return NAIL_DAMAGE; }
    /** Nail-attack horizontal knockback distance (px). */
    public float getNailKnockbackDistance() { return NAIL_KNOCKBACK_DISTANCE; }
    /** Scale applied to knockback when the hit kills the sentry. */
    public float getNailDeathKnockbackScale() { return NAIL_DEATH_KNOCKBACK_SCALE; }
}
