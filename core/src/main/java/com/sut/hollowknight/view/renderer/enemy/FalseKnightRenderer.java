package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.enemy.FalseKnight;
import com.sut.hollowknight.model.enemy.FalseKnightShockwave;
import com.sut.hollowknight.view.assets.FalseKnightAssets;

/**
 * Draws the False Knight: armor body, the maggot-head overlay while the
 * armor is open (stun) and while dying (spec request: the head animations
 * drive the head on stun and death), and the Power Slam shockwave.
 *
 * All body frames share one 1095x636 canvas, so a single set of draw
 * constants (measured from the opaque pixels of the art) positions every
 * animation without per-state offset tables.
 */
public class FalseKnightRenderer {

    private final FalseKnightAssets assets;

    private final ShaderProgram flashShader;
    private static final float FLASH_MAX_INTENSITY = 0.85f;

    // Canvas 1095x636 drawn at 1.1x (2x the original 0.55x). Idle opaque
    // body: x 183..795 (center ~489), feet 35 px above the canvas bottom.
    // NATIVE ART FACES RIGHT (the mace slam frames extend right to x~994),
    // so the sprite is mirrored when the boss faces LEFT.
    private static final float DRAW_WIDTH      = 1204.5f; // 1095 * 1.1
    private static final float DRAW_HEIGHT     = 699.6f;  // 636  * 1.1
    private static final float SPRITE_CENTER_X = 537.9f;  // 489  * 1.1
    private static final float FOOT_OFFSET_Y   = 38.5f;   // 35   * 1.1

    // Head overlay (maggot): the stun body frames bake the head in at
    // canvas (688, 499), so the overlay is sized/anchored to sit EXACTLY
    // on that baked head (opaque face 117/151 of the 151x147 canvas ->
    // draw at 182x177 so the face spans the baked ~141 world px), with
    // its bottom resting on the ground like the art.
    private static final float HEAD_DRAW_W        = 182f;
    private static final float HEAD_DRAW_H        = 177f;
    private static final float HEAD_BOTTOM_OFFSET = 8f;  // above the feet

    // Death head: bigger art (376x298); spills onto the ground up front.
    private static final float DEATH_HEAD_W        = 248f;
    private static final float DEATH_HEAD_H        = 196f;
    private static final float DEATH_HEAD_OFFSET_X = 200f;

    // Shockwave: a bright core quad with two fading trail quads.
    private static final float WAVE_TRAIL_SPACING = 0.8f;

    private final Texture waveTexture;

    public FalseKnightRenderer(FalseKnightAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/hitflash.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("FalseKnightRenderer",
                "Hit-flash shader failed to compile:\n" + shader.getLog());
            shader.dispose();
            shader = null;
        }
        this.flashShader = shader;

        // 1x1 white pixel for the shockwave quads (tinted at draw time).
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        waveTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void dispose() {
        if (flashShader != null) flashShader.dispose();
        waveTexture.dispose();
    }

    public void draw(SpriteBatch batch, FalseKnight boss,
                     FalseKnightShockwave shockwave) {
        drawBody(batch, boss);
        drawHeadOverlay(batch, boss);
        drawShockwave(batch, shockwave);
    }

    private void drawBody(SpriteBatch batch, FalseKnight boss) {
        TextureRegion frame = getBodyFrame(boss);
        if (frame == null) return;

        float drawY = boss.getY() - FOOT_OFFSET_Y;

        boolean flashing = flashShader != null && boss.isHitFlashing();
        if (flashing) {
            batch.setShader(flashShader);
            flashShader.setUniformf("u_flash",
                FLASH_MAX_INTENSITY * boss.getHitFlashStrength());
        }

        if (boss.isFacingRight()) {
            batch.draw(frame, boss.getX() - SPRITE_CENTER_X, drawY,
                DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            // Mirror around the opaque body center, not the canvas center.
            batch.draw(frame, boss.getX() + SPRITE_CENTER_X, drawY,
                -DRAW_WIDTH, DRAW_HEIGHT);
        }

        if (flashing) batch.setShader(null);
    }

    /**
     * The maggot head overlay (spec request): Head Idle/Head Hit while the
     * armor lies open during the stun, Death Head 1 -> 2 while dying.
     */
    private void drawHeadOverlay(SpriteBatch batch, FalseKnight boss) {
        FalseKnight.State state = boss.getState();

        if (state == FalseKnight.State.STUN_OPENED) {
            Animation<TextureRegion> anim = boss.isStunHit()
                ? assets.getHeadHitAnim() : assets.getHeadIdleAnim();
            float t = boss.isStunHit() ? boss.getStunHitTime() : boss.getStateTime();
            TextureRegion frame = anim.getKeyFrame(t);
            CollisionRect head = boss.getHeadBox();
            float headCenterX = (head.getLeft() + head.getRight()) / 2f;
            // The maggot rests on the ground in front of the armor (the
            // stun frames bake it there); anchor the overlay's bottom at
            // the feet so it covers the baked head exactly.
            drawMirrored(batch, frame, headCenterX,
                boss.getY() + HEAD_BOTTOM_OFFSET,
                HEAD_DRAW_W, HEAD_DRAW_H, boss.isFacingRight());
            return;
        }

        if (state == FalseKnight.State.DEATH_LAND
            || state == FalseKnight.State.DEATH_SPAZ) {
            float t = boss.getDeathHeadTime();
            float head1Duration = assets.getDeathHead1Anim().getAnimationDuration();
            TextureRegion frame = t < head1Duration
                ? assets.getDeathHead1Anim().getKeyFrame(t)
                : assets.getDeathHead2Anim().getKeyFrame(t - head1Duration);
            float dir = boss.isFacingRight() ? 1f : -1f;
            drawMirrored(batch, frame,
                boss.getX() + dir * DEATH_HEAD_OFFSET_X, boss.getY(),
                DEATH_HEAD_W, DEATH_HEAD_H, boss.isFacingRight());
        }
    }

    /** Draw centered on x, bottom at y, mirrored when facing left. */
    private static void drawMirrored(SpriteBatch batch, TextureRegion frame,
                                     float centerX, float bottomY,
                                     float w, float h, boolean facingRight) {
        if (facingRight) {
            batch.draw(frame, centerX - w / 2f, bottomY, w, h);
        } else {
            batch.draw(frame, centerX + w / 2f, bottomY, -w, h);
        }
    }

    /** Accelerating ground wave: bright core + fading trail (all quads). */
    private void drawShockwave(SpriteBatch batch, FalseKnightShockwave wave) {
        if (wave == null || !wave.isActive()) return;

        float w = FalseKnightShockwave.WIDTH;
        float h = FalseKnightShockwave.HEIGHT;
        float x = wave.getX();
        float y = wave.getY();
        float back = -wave.getDir() * w * WAVE_TRAIL_SPACING;

        // Core
        batch.setColor(1f, 0.87f, 0.45f, 0.9f);
        batch.draw(waveTexture, x - w / 2f, y, w, h);
        // Trail: two quads, shrinking and fading behind the core.
        batch.setColor(1f, 0.6f, 0.2f, 0.45f);
        batch.draw(waveTexture, x - w * 0.4f + back, y, w * 0.8f, h * 0.72f);
        batch.setColor(1f, 0.45f, 0.12f, 0.22f);
        batch.draw(waveTexture, x - w * 0.3f + back * 2f, y, w * 0.6f, h * 0.5f);
        batch.setColor(Color.WHITE);
    }

    private TextureRegion getBodyFrame(FalseKnight boss) {
        float t = boss.getStateTime();
        Animation<TextureRegion> anim;

        switch (boss.getState()) {
            case IDLE:           anim = assets.getIdleAnim();          break;
            case TURN:           anim = assets.getTurnAnim();          break;
            case RAGE:           anim = assets.getRageAnim();          break;
            case RUN:            anim = assets.getRunAnim();           break;
            case ATTACK_ANTIC:   anim = assets.getAttackAnticAnim();   break;
            case ATTACK:         anim = assets.getAttackAnim();        break;
            case ATTACK_RECOVER: anim = assets.getAttackRecoverAnim(); break;
            case JUMP_ANTIC:     anim = assets.getJumpAnticAnim();     break;
            case JUMP:           anim = assets.getJumpAnim();          break;
            case LAND:           anim = assets.getLandAnim();          break;
            case POWER_JUMP:     anim = assets.getPowerJumpAnim();     break;
            case POWER_HIT:      anim = assets.getPowerHitAnim();      break;
            case STUN_FALL:      anim = assets.getStunRollAnim();      break;
            case STUN_GROUND:    anim = assets.getStunRollEndAnim();   break;
            case STUN_OPEN:      anim = assets.getStunOpenAnim();      break;
            case STUN_OPENED:
                // A nail hit on the exposed head makes the armor flinch too.
                if (boss.isStunHit()) {
                    anim = assets.getStunHitAnim();
                    t = boss.getStunHitTime();
                } else {
                    anim = assets.getStunOpenedAnim();
                }
                break;
            case STUN_RECOVER:   anim = assets.getStunRecoverAnim();   break;
            case DEATH_FALL:     anim = assets.getDeathFallAnim();     break;
            case DEATH_LAND:     anim = assets.getDeathLandAnim();     break;
            case DEATH_SPAZ:     anim = assets.getDeathSpazAnim();     break;
            default:             anim = assets.getIdleAnim();          break;
        }
        return anim.getKeyFrame(t);
    }
}
