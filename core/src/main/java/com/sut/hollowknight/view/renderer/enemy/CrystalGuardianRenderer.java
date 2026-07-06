package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sut.hollowknight.model.enemy.CrystalGuardian;
import com.sut.hollowknight.model.enemy.Laser;
import com.sut.hollowknight.view.assets.CrystalGuardianAssets;

public class CrystalGuardianRenderer {

    private final CrystalGuardianAssets assets;

    private final ShaderProgram flashShader;
    private static final float FLASH_MAX_INTENSITY = 0.85f;

    private static final float DRAW_WIDTH  = 242f;
    private static final float DRAW_HEIGHT = 161f;

    private static final float FOOT_OFFSET_Y   = 3f;
    private static final float SPRITE_CENTER_X = 109f;

    // Laser beam visuals: a soft outer glow around a bright core.
    private static final float GLOW_SCALE = 1.8f;
    private static final Color GLOW_COLOR = new Color(1f, 0.45f, 0.85f, 0.55f);
    private static final Color CORE_COLOR = new Color(1f, 0.9f, 0.97f, 0.95f);
    // Charging tracking beam: a faint thin thread, no glow.
    private static final Color TRACKING_COLOR = new Color(1f, 0.55f, 0.85f, 0.6f);

    /** 1x1 white pixel, rotated and stretched into the beam quad. */
    private final Texture beamTexture;
    private final TextureRegion beamRegion;

    public CrystalGuardianRenderer(CrystalGuardianAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/hitflash.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("CrystalGuardianRenderer",
                "Hit-flash shader failed to compile:\n" + shader.getLog());
            shader.dispose();
            shader = null;
        }
        this.flashShader = shader;

        // Build the beam texture once — never allocated in the render loop.
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        beamTexture = new Texture(pixmap);
        pixmap.dispose();
        beamRegion = new TextureRegion(beamTexture);
    }

    public void dispose() {
        if (flashShader != null) {
            flashShader.dispose();
        }
        beamTexture.dispose();
    }

    public void draw(SpriteBatch batch, CrystalGuardian guardian) {
        // Beam first, so the guardian's body covers the beam origin.
        drawLaser(batch, guardian.getLaser());

        // Corpses stay visible: Death Land clamps on its final frame.
        TextureRegion frame = getCurrentFrame(guardian);
        if (frame == null) return;

        float drawY = guardian.getY() - FOOT_OFFSET_Y;

        boolean flashing = flashShader != null && guardian.isHitFlashing();
        if (flashing) {
            batch.setShader(flashShader);
            flashShader.setUniformf("u_flash",
                FLASH_MAX_INTENSITY * guardian.getHitFlashStrength());
        }

        // Native art faces LEFT, so the sprite is mirrored when facing right.
        if (guardian.isFacingRight()) {
            float drawX = guardian.getX() - (DRAW_WIDTH - SPRITE_CENTER_X);
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            float drawX = guardian.getX() - SPRITE_CENTER_X;
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }

        if (flashing) {
            batch.setShader(null);
        }
    }

    /**
     * Draws the beam as two rotated quads (glow + core) pivoting on the
     * muzzle. The rotation angle was cached at fire time — no per-frame
     * trigonometry here.
     */
    private void drawLaser(SpriteBatch batch, Laser laser) {
        if (!laser.isActive()) return;

        float ox = laser.getOriginX();
        float oy = laser.getOriginY();
        float angle = laser.getAngleDeg();
        float length = laser.getLength();

        // The charging beam is a thin harmless thread; the fired beam is thick
        // with a soft outer glow.
        if (laser.isHarmless()) {
            batch.setColor(TRACKING_COLOR);
            batch.draw(beamRegion,
                ox, oy - Laser.TRACKING_THICKNESS / 2f,
                0f, Laser.TRACKING_THICKNESS / 2f,
                length, Laser.TRACKING_THICKNESS,
                1f, 1f, angle);
            batch.setColor(Color.WHITE);
            return;
        }

        float glowThickness = Laser.THICKNESS * GLOW_SCALE;
        batch.setColor(GLOW_COLOR);
        batch.draw(beamRegion,
            ox, oy - glowThickness / 2f,        // position (pre-rotation)
            0f, glowThickness / 2f,             // origin: pivot on the muzzle
            length, glowThickness,              // stretched into the beam
            1f, 1f, angle);

        batch.setColor(CORE_COLOR);
        batch.draw(beamRegion,
            ox, oy - Laser.THICKNESS / 2f,
            0f, Laser.THICKNESS / 2f,
            length, Laser.THICKNESS,
            1f, 1f, angle);

        batch.setColor(Color.WHITE);
    }

    public TextureRegion getCurrentFrame(CrystalGuardian guardian) {
        float t = guardian.getStateTime();
        Animation<TextureRegion> anim;

        switch (guardian.getState()) {
            case IDLE:        anim = assets.getIdleAnim();       break;
            case TURN:        anim = assets.getTurnAnim();       break;
            case SHOOT_ANTIC: anim = assets.getShootAnticAnim(); break;
            case SHOOT:       anim = assets.getShootAnim();      break;
            case SHOOT_LOOP:  anim = assets.getShootLoopAnim();  break;
            case SHOOT_END:   anim = assets.getShootEndAnim();   break;
            case ENRAGED:     anim = assets.getRunAnim();        break;
            case DEATH_AIR:   anim = assets.getDeathAirAnim();   break;
            case DEATH_LAND:  anim = assets.getDeathLandAnim();  break;
            default:          anim = assets.getIdleAnim();       break;
        }
        return anim.getKeyFrame(t);
    }
}
