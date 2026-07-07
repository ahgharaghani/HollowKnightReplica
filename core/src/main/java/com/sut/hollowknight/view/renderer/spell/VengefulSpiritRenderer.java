package com.sut.hollowknight.view.renderer.spell;

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

    // Ball art canvas is 317x143; keep that aspect at a readable in-world size.
    private static final float BALL_DRAW_WIDTH  = 170f;
    private static final float BALL_DRAW_HEIGHT = BALL_DRAW_WIDTH * 143f / 317f;

    // Wall impact art canvas is 259x204.
    private static final float WALL_DRAW_WIDTH  = 130f;
    private static final float WALL_DRAW_HEIGHT = WALL_DRAW_WIDTH * 204f / 259f;

    // Blast art canvas is 306x289 — the emergence burst that envelops the
    // knight (hurtbox is 110 tall), anchored where the ball spawned.
    private static final float BLAST_DRAW_WIDTH  = 190f;
    private static final float BLAST_DRAW_HEIGHT = BLAST_DRAW_WIDTH * 289f / 306f;

    public VengefulSpiritRenderer(VengefulSpiritAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, VengefulSpirit spirit) {
        if (spirit.isDone()) return;

        // Emergence Blast on the knight. Keyed on total age (not state time),
        // so it survives the FLYING -> IMPACT transition intact.
        drawBlast(batch, spirit);

        if (spirit.getState() == VengefulSpirit.State.FLYING) {
            drawBall(batch, spirit, assets.getBallFrame(spirit.getStateTime()));
        } else { // IMPACT
            Animation<TextureRegion> end = assets.getBallEndAnim();
            drawBall(batch, spirit, end.getKeyFrame(spirit.getStateTime(), false));
            drawWallImpact(batch, spirit);
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
        Animation<TextureRegion> blast = assets.getBlastAnim();
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
