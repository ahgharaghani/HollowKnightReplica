package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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

    // Laser beam visuals — art from Laser.atlas.
    /** Native beam tile: 117x117 with the pulsing core through the middle. */
    private static final float BEAM_SEGMENT_LENGTH = 117f;
    private static final float BEAM_DRAW_HEIGHT    = 117f;
    /** Muzzle burst canvas (228x123); the flash core sits at this pivot. */
    private static final float BURST_WIDTH   = 228f;
    private static final float BURST_HEIGHT  = 123f;
    private static final float BURST_PIVOT_X = 176f;
    private static final float BURST_PIVOT_Y = 62f;
    /** Lamp glow sprite (131x113), drawn centered on the muzzle. */
    private static final float GLOW_WIDTH  = 131f;
    private static final float GLOW_HEIGHT = 113f;
    /** Charging tracking beam: same art, squashed thin and faded. */
    private static final float TRACKING_HEIGHT = 12f;
    private static final int   TRACKING_FRAME  = 7; // thickest pulse frame
    private static final Color TRACKING_COLOR  = new Color(1f, 0.7f, 0.9f, 0.55f);

    /** Reusable region for the clipped tile at the beam's end — never allocated per frame. */
    private final TextureRegion beamEndRegion = new TextureRegion();

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

    }

    public void dispose() {
        if (flashShader != null) {
            flashShader.dispose();
        }
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
     * Draws the beam with the Laser.atlas art. The pulsing beam tiles repeat
     * along the beam line, rotated around the muzzle — the angle was cached
     * at fire time, so there is no per-frame trigonometry. A muzzle burst
     * plays once on ignition and the lamp glow marks the origin throughout.
     */
    private void drawLaser(SpriteBatch batch, Laser laser) {
        if (!laser.isActive()) return;

        // Charging tracking beam: a thin, faded thread of the same beam art.
        if (laser.isHarmless()) {
            Object[] frames = assets.getLaserBeamAnim().getKeyFrames();
            TextureRegion thin = (TextureRegion) frames[TRACKING_FRAME];
            batch.setColor(TRACKING_COLOR);
            drawBeamTiles(batch, laser, thin, TRACKING_HEIGHT);
            batch.setColor(Color.WHITE);
            drawMuzzleGlow(batch, laser, 0.6f);
            return;
        }

        // Fired beam: pulsing tiles at full thickness (LOOP over 15 frames).
        TextureRegion tile = assets.getLaserBeamAnim().getKeyFrame(laser.getLife());
        drawBeamTiles(batch, laser, tile, BEAM_DRAW_HEIGHT);

        // One-shot ignition burst, pivoting on the muzzle along the beam line.
        if (!assets.getLaserBurstAnim().isAnimationFinished(laser.getLife())) {
            TextureRegion burst = assets.getLaserBurstAnim().getKeyFrame(laser.getLife());
            batch.draw(burst,
                laser.getOriginX() - BURST_PIVOT_X,
                laser.getOriginY() - BURST_PIVOT_Y,
                BURST_PIVOT_X, BURST_PIVOT_Y,     // rotate around the flash core
                BURST_WIDTH, BURST_HEIGHT,
                1f, 1f, laser.getAngleDeg());
        }

        drawMuzzleGlow(batch, laser, 1f);
    }

    /** Repeats a beam tile along the beam line, rotated around the muzzle. */
    private void drawBeamTiles(SpriteBatch batch, Laser laser,
                               TextureRegion tile, float drawHeight) {
        float ox = laser.getOriginX();
        float oy = laser.getOriginY();
        float angle = laser.getAngleDeg();
        float length = laser.getLength();

        int fullTiles = (int) (length / BEAM_SEGMENT_LENGTH);
        for (int i = 0; i < fullTiles; i++) {
            float sx = ox + laser.getDirX() * (i * BEAM_SEGMENT_LENGTH);
            float sy = oy + laser.getDirY() * (i * BEAM_SEGMENT_LENGTH);
            batch.draw(tile,
                sx, sy - drawHeight / 2f,
                0f, drawHeight / 2f,              // pivot on the tile's left-center
                BEAM_SEGMENT_LENGTH, drawHeight,
                1f, 1f, angle);
        }

        // Clipped tile at the terrain hit — trimmed in texture space so the
        // art never overshoots the wall. Reuses one region: zero allocation.
        float rem = length - fullTiles * BEAM_SEGMENT_LENGTH;
        int remPx = (int) (tile.getRegionWidth() * (rem / BEAM_SEGMENT_LENGTH));
        if (remPx > 0) {
            beamEndRegion.setRegion(tile, 0, 0, remPx, tile.getRegionHeight());
            float sx = ox + laser.getDirX() * (fullTiles * BEAM_SEGMENT_LENGTH);
            float sy = oy + laser.getDirY() * (fullTiles * BEAM_SEGMENT_LENGTH);
            batch.draw(beamEndRegion,
                sx, sy - drawHeight / 2f,
                0f, drawHeight / 2f,
                rem, drawHeight,
                1f, 1f, angle);
        }
    }

    /** Radial glow centered on the lamp while any beam is active. */
    private void drawMuzzleGlow(SpriteBatch batch, Laser laser, float scale) {
        float w = GLOW_WIDTH * scale;
        float h = GLOW_HEIGHT * scale;
        batch.draw(assets.getLaserGlowRegion(),
            laser.getOriginX() - w / 2f,
            laser.getOriginY() - h / 2f,
            w, h);
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
