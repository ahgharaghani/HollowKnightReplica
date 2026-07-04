package com.sut.hollowknight.controller;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.model.collision.PixelCollisionUtil;
import java.util.List;

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
    private final List<WingedSentryController> sentryControllers;

    public CombatSystem(Knight knight, List<WingedSentryController> sentryControllers) {
        this.knight = knight;
        this.sentryControllers = sentryControllers;
    }

    public void resolve(float delta, TextureRegion knightFrame) {
        for (WingedSentryController sc : sentryControllers) {
            resolveContactDamage(sc, knightFrame);
            resolveJavelinDamage(sc, knightFrame);
        }
    }

    private void resolveContactDamage(WingedSentryController sentryController, TextureRegion knightFrame) {
        if (knight.isInvincible()) return;

        WingedSentry sentry = sentryController.getSentry();
        // Skip if dead or already overlapping bounding boxes
        if (!sentry.isAlive() || !sentryController.overlapsKnight()) return;

        // Sentry visual bounds (matching WingedSentryRenderer)
        float sDrawW = 509f;
        float sDrawH = 398f;
        float sDrawX = sentry.getX() - sDrawW / 2f;
        float sDrawY = sentry.getY() - (sDrawH - WingedSentry.HEIGHT) / 2f;
        boolean sFlip = sentry.isFacingRight();

        // Knight visual bounds (matching GameScreen renderer)
        float kDrawW = knightFrame.getRegionWidth();
        float kDrawH = knightFrame.getRegionHeight();
        float kDrawX = knight.getX() - kDrawW / 2f;
        float kDrawY = knight.getY();
        boolean kFlip = knight.isFacingRight();

        // Use Pixel-Perfect Collision!
        boolean hit = PixelCollisionUtil.overlaps(
            knightFrame, kDrawX, kDrawY, kDrawW, kDrawH, kFlip,
            sentryController.getCurrentFrame(), sDrawX, sDrawY, sDrawW, sDrawH, sFlip
        );

        if (hit) {
            int knockbackDir = knight.getX() < sentry.getX() ? -1 : 1;
            knight.takeDamage(CONTACT_DAMAGE);
            knight.applyKnockback(knockbackDir * CONTACT_KNOCKBACK_X, CONTACT_KNOCKBACK_Y);
        }
    }

    private void resolveJavelinDamage(WingedSentryController sentryController, TextureRegion knightFrame) {
        if (!sentryController.isJavelinDamageDealt()) return;

        Javelin javelin = sentryController.getJavelin();
        int knockbackDir = knight.getX() < javelin.getCenterX() ? -1 : 1;

        knight.takeDamage(Javelin.DAMAGE);
        knight.applyKnockback(knockbackDir * JAVELIN_KNOCKBACK_X, JAVELIN_KNOCKBACK_Y);

        sentryController.clearJavelinDamageDealt();
    }
}
