package com.sut.hollowknight.view.renderer.spell;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.spell.HowlingWraith;
import com.sut.hollowknight.view.assets.HowlingWraithAssets;

/**
 * Renders a Howling Wraiths blast: the rising scream plume above the knight
 * plus the ground shockwave (Scream Base) under the hero's feet.
 */
public class HowlingWraithRenderer {

    private final HowlingWraithAssets assets;

    // Plume canvas is 332x306; the art rises from the anchor upward, so it is
    // drawn bottom-anchored at the feet.
    private static final float SCREAM_DRAW_WIDTH  = 332f;
    private static final float SCREAM_DRAW_HEIGHT = 306f;

    // Base canvas is 350x134 with the shockwave art in the lower half, so
    // centering the canvas on the feet places the art just under them.
    private static final float BASE_DRAW_WIDTH  = 350f;
    private static final float BASE_DRAW_HEIGHT = 134f;

    public HowlingWraithRenderer(HowlingWraithAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, HowlingWraith wraith) {
        if (wraith.isDone()) return;

        float age = wraith.getAge();

        // Ground shockwave under the feet (8 frames @ 15 FPS).
        Animation<TextureRegion> baseAnim = assets.getScreamBaseAnim();
        if (!baseAnim.isAnimationFinished(age)) {
            drawFlipped(batch, baseAnim.getKeyFrame(age, false), wraith,
                wraith.getAnchorY() - BASE_DRAW_HEIGHT / 2f,
                BASE_DRAW_WIDTH, BASE_DRAW_HEIGHT);
        }

        // Rising plume (13 frames @ 20 FPS), bottom anchored at the feet.
        Animation<TextureRegion> screamAnim = assets.getScreamAnim();
        if (!screamAnim.isAnimationFinished(age)) {
            drawFlipped(batch, screamAnim.getKeyFrame(age, false), wraith,
                wraith.getAnchorY(),
                SCREAM_DRAW_WIDTH, SCREAM_DRAW_HEIGHT);
        }
    }

    /** Draws centered on the anchor x, flipped to match the knight's facing. */
    private static void drawFlipped(SpriteBatch batch, TextureRegion frame,
                                    HowlingWraith wraith, float bottomY,
                                    float w, float h) {
        if (frame == null) return;
        float drawX = wraith.getAnchorX() - w / 2f;
        if (wraith.isFacingRight()) {
            batch.draw(frame, drawX, bottomY, w, h);
        } else {
            batch.draw(frame, drawX + w, bottomY, -w, h);
        }
    }
}
