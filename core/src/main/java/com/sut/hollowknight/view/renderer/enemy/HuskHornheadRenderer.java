package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sut.hollowknight.model.enemy.HuskHornhead;
import com.sut.hollowknight.view.assets.HuskHornheadAssets;

public class HuskHornheadRenderer {

    private final HuskHornheadAssets assets;

    private final ShaderProgram flashShader;
    private static final float FLASH_MAX_INTENSITY = 0.85f;

    // Canvas 239x219 drawn at 0.75x. The opaque body inside the canvas is
    // ~139x181 px (native), so the on-screen creature stands ~104x136.
    private static final float DRAW_WIDTH  = 179f;
    private static final float DRAW_HEIGHT = 164f;

    /** Opaque bottom edge sits 11 native px above the canvas edge (x0.75). */
    private static final float FOOT_OFFSET_Y   = 8f;
    /** Opaque body center at ~108 native px from the canvas left (x0.75). */
    private static final float SPRITE_CENTER_X = 81f;

    public HuskHornheadRenderer(HuskHornheadAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/hitflash.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("HuskHornheadRenderer",
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

    public void draw(SpriteBatch batch, HuskHornhead hornhead) {
        // Corpses stay visible: Death Land clamps on its final frame.
        TextureRegion frame = getCurrentFrame(hornhead);
        if (frame == null) return;

        float drawY = hornhead.getY() - FOOT_OFFSET_Y;

        boolean flashing = flashShader != null && hornhead.isHitFlashing();
        if (flashing) {
            batch.setShader(flashShader);
            flashShader.setUniformf("u_flash",
                FLASH_MAX_INTENSITY * hornhead.getHitFlashStrength());
        }

        // Native art faces LEFT, so the sprite is mirrored when facing right.
        if (hornhead.isFacingRight()) {
            float drawX = hornhead.getX() - (DRAW_WIDTH - SPRITE_CENTER_X);
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            float drawX = hornhead.getX() - SPRITE_CENTER_X;
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }

        if (flashing) {
            batch.setShader(null);
        }
    }

    public TextureRegion getCurrentFrame(HuskHornhead hornhead) {
        float t = hornhead.getStateTime();
        Animation<TextureRegion> anim;

        switch (hornhead.getState()) {
            case WALK:            anim = assets.getWalkAnim();      break;
            case REST:            anim = assets.getIdleAnim();      break;
            case TURN:            anim = assets.getTurnAnim();      break;
            case ATTACK_ANTIC:    anim = assets.getAnticAnim();     break;
            case LUNGE:           anim = assets.getLungeAnim();     break;
            case ATTACK_COOLDOWN: anim = assets.getCooldownAnim();  break;
            case DEATH_AIR:       anim = assets.getDeathAirAnim();  break;
            case DEATH_LAND:      anim = assets.getDeathLandAnim(); break;
            default:              anim = assets.getWalkAnim();      break;
        }
        return anim.getKeyFrame(t);
    }
}
