package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.view.assets.WingedSentryAssets;

public class WingedSentryRenderer {

    private final WingedSentryAssets assets;

    private final ShaderProgram flashShader;
    /** 1 = pure white silhouette. */
    private static final float FLASH_MAX_INTENSITY = 0.85f;

    /** Native sprite frame size from atlas. */
    private static final float DRAW_WIDTH  = 509f;
    private static final float DRAW_HEIGHT = 398f;

    private static final float SPRITE_CENTER_X             = 222f; // px from canvas left (unflipped)
    private static final float SPRITE_CENTER_Y_FROM_BOTTOM = 272f; // px from canvas bottom

    public WingedSentryRenderer(WingedSentryAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/hitflash.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("WingedSentryRenderer",
                "Hit-flash shader failed to compile:\n" + shader.getLog());
            shader.dispose();
            shader = null;
        }
        this.flashShader = shader;
    }

    public void dispose() {
        if (flashShader != null) {
            flashShader.dispose();
        }
    }

    public void draw(SpriteBatch batch, WingedSentry sentry) {
        TextureRegion frame = getCurrentFrame(sentry);
        if (frame == null) return;

        // Align the creature's visual center with the body box center.
        float drawY = sentry.getY() + WingedSentry.HEIGHT / 2f - SPRITE_CENTER_Y_FROM_BOTTOM;

        // White hit-flash: switch shaders just for this sprite. setShader
        // flushes the batch, so already-queued sprites keep the default look.
        boolean flashing = flashShader != null && sentry.isHitFlashing();
        if (flashing) {
            batch.setShader(flashShader);
            flashShader.setUniformf("u_flash",
                FLASH_MAX_INTENSITY * sentry.getHitFlashStrength());
        }

        if (sentry.isFacingRight()) {
            float drawX = sentry.getX() - (DRAW_WIDTH - SPRITE_CENTER_X);
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            float drawX = sentry.getX() - SPRITE_CENTER_X;
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }

        if (flashing) {
            batch.setShader(null);
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
