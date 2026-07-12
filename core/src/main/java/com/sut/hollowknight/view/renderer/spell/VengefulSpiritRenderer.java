package com.sut.hollowknight.view.renderer.spell;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.spell.VengefulSpirit;
import com.sut.hollowknight.view.assets.VengefulSpiritAssets;

/**
 * Renders a Vengeful Spirit fireball: the travelling Ball while flying, then
 * the Ball End dissipation plus the Ball Wall Impact burst on the struck wall.
 */
public class VengefulSpiritRenderer {

    private final VengefulSpiritAssets assets;

    // Ball art canvas is 317x143;
    private static final float BALL_DRAW_WIDTH  = 317f;
    private static final float BALL_DRAW_HEIGHT = 143;

    // Shadow ball canvas is 504x157: the void core sits at the LEADING edge
    // with a long wispy tail behind it, so it is drawn leading-edge-aligned
    // with the regular ball to keep the core on the 110x46 hurtbox.
    private static final float SHADOW_BALL_DRAW_WIDTH  = 504f;
    private static final float SHADOW_BALL_DRAW_HEIGHT = 157f;

    // Wall impact art canvas is 259x204.
    private static final float WALL_DRAW_WIDTH  = 259f;
    private static final float WALL_DRAW_HEIGHT = 204f;

    // Blast art canvas is 306x289 — the emergence burst that envelops the
    // knight (hurtbox is 110 tall), anchored where the ball spawned.
    private static final float BLAST_DRAW_WIDTH  = 306f;
    private static final float BLAST_DRAW_HEIGHT = 289f;

    public VengefulSpiritRenderer(VengefulSpiritAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, VengefulSpirit spirit) {
        if (spirit.isDone()) return;

        // Emergence Blast on the knight. Keyed on total age (not state time),
        // so it survives the FLYING -> IMPACT transition intact.
        drawBlast(batch, spirit);

        if (spirit.getState() == VengefulSpirit.State.FLYING) {
            if (spirit.isShadow()) {
                drawShadowBall(batch, spirit, assets.getShadowBallFrame(spirit.getStateTime()));
            } else {
                drawBall(batch, spirit, assets.getBallFrame(spirit.getStateTime()));
            }
        } else { // IMPACT
            // The shadow set has no dedicated dissipation art, so the fire
            // set's Ball End / wall burst is reused with a void-dark tint.
            boolean tint = spirit.isShadow();
            if (tint) batch.setColor(0.35f, 0.25f, 0.48f, 1f);
            Animation<TextureRegion> end = assets.getBallEndAnim();
            drawBall(batch, spirit, end.getKeyFrame(spirit.getStateTime(), false));
            drawWallImpact(batch, spirit);
            if (tint) batch.setColor(Color.WHITE);
        }
    }

    private void drawBall(SpriteBatch batch, VengefulSpirit spirit, TextureRegion frame) {
        if (frame == null) return;
        float drawX = spirit.getX() - BALL_DRAW_WIDTH / 2f;
        float drawY = spirit.getY() - BALL_DRAW_HEIGHT / 2f;

        // Native art faces right; flip horizontally when travelling left.
        if (spirit.isFacingRight()) {
            batch.draw(frame, drawX, drawY, BALL_DRAW_WIDTH, BALL_DRAW_HEIGHT);
        } else {
            batch.draw(frame, drawX + BALL_DRAW_WIDTH, drawY, -BALL_DRAW_WIDTH, BALL_DRAW_HEIGHT);
        }
    }

    /**
     * Shadow ball: drawn so its leading edge lines up with where the regular
     * ball's leading edge would be - the core stays glued to the hurtbox and
     * the extra canvas width becomes the tail trailing behind the shot.
     */
    private void drawShadowBall(SpriteBatch batch, VengefulSpirit spirit, TextureRegion frame) {
        if (frame == null) return;
        float leadEdge = BALL_DRAW_WIDTH / 2f; // core front, relative to center
        float drawY = spirit.getY() - SHADOW_BALL_DRAW_HEIGHT / 2f;
        if (spirit.isFacingRight()) {
            float drawX = spirit.getX() + leadEdge - SHADOW_BALL_DRAW_WIDTH;
            batch.draw(frame, drawX, drawY, SHADOW_BALL_DRAW_WIDTH, SHADOW_BALL_DRAW_HEIGHT);
        } else {
            // Mirrored: leading edge on the left, tail extending right.
            float drawX = spirit.getX() - leadEdge;
            batch.draw(frame, drawX + SHADOW_BALL_DRAW_WIDTH, drawY,
                -SHADOW_BALL_DRAW_WIDTH, SHADOW_BALL_DRAW_HEIGHT);
        }
    }

    private void drawWallImpact(SpriteBatch batch, VengefulSpirit spirit) {
        TextureRegion frame = assets.getWallImpactAnim().getKeyFrame(spirit.getStateTime(), false);
        if (frame == null) return;

        // Burst sits at the ball's leading edge (the wall face it struck).
        float dir  = spirit.isFacingRight() ? 1f : -1f;
        float tipX = spirit.getX() + dir * (VengefulSpirit.DMG_WIDTH / 2f);
        float drawX = tipX - WALL_DRAW_WIDTH / 2f;
        float drawY = spirit.getY() - WALL_DRAW_HEIGHT / 2f;

        if (spirit.isFacingRight()) {
            batch.draw(frame, drawX, drawY, WALL_DRAW_WIDTH, WALL_DRAW_HEIGHT);
        } else {
            batch.draw(frame, drawX + WALL_DRAW_WIDTH, drawY, -WALL_DRAW_WIDTH, WALL_DRAW_HEIGHT);
        }
    }

    /** The burst showing the ball emerging from the knight (8 frames @ 24 FPS). */
    private void drawBlast(SpriteBatch batch, VengefulSpirit spirit) {
        // Shadow Blast shares the regular Blast 306x289 canvas: same draw.
        Animation<TextureRegion> blast = spirit.isShadow()
            ? assets.getShadowBlastAnim() : assets.getBlastAnim();
        if (blast == null || blast.isAnimationFinished(spirit.getAge())) return;

        TextureRegion frame = blast.getKeyFrame(spirit.getAge(), false);
        if (frame == null) return;

        float drawX = spirit.getOriginX() - BLAST_DRAW_WIDTH / 2f;
        float drawY = spirit.getOriginY() - BLAST_DRAW_HEIGHT / 2f;
        if (spirit.isFacingRight()) {
            batch.draw(frame, drawX, drawY, BLAST_DRAW_WIDTH, BLAST_DRAW_HEIGHT);
        } else {
            // Native art faces right; flip horizontally in place.
            batch.draw(frame, drawX + BLAST_DRAW_WIDTH, drawY,
                -BLAST_DRAW_WIDTH, BLAST_DRAW_HEIGHT);
        }
    }

}
