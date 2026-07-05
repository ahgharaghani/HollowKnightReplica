package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sut.hollowknight.model.enemy.Tiktik;
import com.sut.hollowknight.view.assets.TiktikAssets;

public class TiktikRenderer {

    private final TiktikAssets assets;

    private final ShaderProgram flashShader;
    private static final float FLASH_MAX_INTENSITY = 0.85f;

    private static final float DRAW_WIDTH  = 115f;
    private static final float DRAW_HEIGHT = 105f;

    private static final float FOOT_OFFSET_Y   = 20f;
    private static final float SPRITE_CENTER_X = 56f;

    public TiktikRenderer(TiktikAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/hitflash.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("TiktikRenderer",
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

    public void draw(SpriteBatch batch, Tiktik tiktik) {
        // Corpses stay visible: Death Land clamps on its final frame.
        TextureRegion frame = getCurrentFrame(tiktik);
        if (frame == null) return;

        float drawY = tiktik.getY() - FOOT_OFFSET_Y;

        boolean flashing = flashShader != null && tiktik.isHitFlashing();
        if (flashing) {
            batch.setShader(flashShader);
            flashShader.setUniformf("u_flash",
                FLASH_MAX_INTENSITY * tiktik.getHitFlashStrength());
        }

        if (!tiktik.isFacingRight()) {
            float drawX = tiktik.getX() - (DRAW_WIDTH - SPRITE_CENTER_X);
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            float drawX = tiktik.getX() - SPRITE_CENTER_X;
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }

        if (flashing) {
            batch.setShader(null);
        }
    }

    public TextureRegion getCurrentFrame(Tiktik tiktik) {
        float t = tiktik.getStateTime();
        Animation<TextureRegion> anim;

        switch (tiktik.getState()) {
            case WALK:
                anim = assets.getWalkAnim();
                break;
            case DEATH_AIR:
                // Death Air Old anim played
                anim = assets.getDeathAirOldAnim();
                break;
            case DEATH_LAND:
                anim = assets.getDeathLandAnim();
                break;
            default:
                anim = assets.getWalkAnim();
                break;
        }
        return anim.getKeyFrame(t);
    }
}
