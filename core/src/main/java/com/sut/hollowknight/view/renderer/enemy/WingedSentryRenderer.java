package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.view.assets.WingedSentryAssets;

public class WingedSentryRenderer {

    private final WingedSentryAssets assets;

    /** Native sprite frame size from atlas. */
    private static final float DRAW_WIDTH  = 509f;
    private static final float DRAW_HEIGHT = 398f;

    public WingedSentryRenderer(WingedSentryAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, WingedSentry sentry) {
        if (sentry.isDeadHandled()) return; // fully-handled corpse: skip

        TextureRegion frame = getCurrentFrame(sentry);
        if (frame == null) return;

        float drawX = sentry.getX() - DRAW_WIDTH / 2f;
        float drawY = sentry.getY() - (DRAW_HEIGHT - WingedSentry.HEIGHT) / 2f;

        if (sentry.isFacingRight()) {
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }
    }

    public TextureRegion getCurrentFrame(WingedSentry sentry) {
        float t = sentry.getStateTime();
        Animation<TextureRegion> anim;

        switch (sentry.getState()) {
            case IDLE:
            case CHASE:
                anim = assets.getIdleAnim();
                break;
            case TURN_TO_IDLE:
                anim = assets.getTurnToIdleAnim();
                break;
            case CHARGE_ANTIC:
                anim = assets.getChargeAnticAnim();
                break;
            case CHARGE:
                anim = assets.getChargeAnim();
                break;
            case CHARGE_RECOVER:
                anim = assets.getChargeRecoverAnim();
                break;
            case THROW_ATTACK:
                anim = assets.getThrowAttackAnim();
                break;
            case DEATH_AIR:
                anim = assets.getDeathAirAnim();
                break;
            case DEATH_LAND:
                anim = assets.getDeathLandAnim();
                break;
            default:
                anim = assets.getIdleAnim();
                break;
        }
        return anim.getKeyFrame(t);
    }
}
