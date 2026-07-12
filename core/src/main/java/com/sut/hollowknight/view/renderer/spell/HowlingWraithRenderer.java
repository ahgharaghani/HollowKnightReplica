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
    private static final float SCREAM_DRAW_WIDTH  = 300f;
    private static final float SCREAM_DRAW_HEIGHT = SCREAM_DRAW_WIDTH * 306f / 332f;

    // Base canvas is 350x134 with the shockwave art in the lower half, so
    // centering the canvas on the feet places the art just under them.
    private static final float BASE_DRAW_WIDTH  = 250f;
    private static final float BASE_DRAW_HEIGHT = BASE_DRAW_WIDTH * 134f / 350f;

    // Shadow plume canvas is 357x292; shadow base canvas is 408x174. Drawn at
    // the same widths as the fire set so the spell reads identically in play.
    private static final float SHADOW_SCREAM_DRAW_WIDTH  = 300f;
    private static final float SHADOW_SCREAM_DRAW_HEIGHT = SHADOW_SCREAM_DRAW_WIDTH * 292f / 357f;
    private static final float SHADOW_BASE_DRAW_WIDTH  = 250f;
    private static final float SHADOW_BASE_DRAW_HEIGHT = SHADOW_BASE_DRAW_WIDTH * 174f / 408f;

    public HowlingWraithRenderer(HowlingWraithAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, HowlingWraith wraith) {
        if (wraith.isDone()) return;

        float age = wraith.getAge();
        boolean shadow = wraith.isShadow();

        // Ground shockwave under the feet (both sets run at 15 FPS).
        Animation<TextureRegion> baseAnim = shadow
            ? assets.getShadowScreamBaseAnim() : assets.getScreamBaseAnim();
        float baseW = shadow ? SHADOW_BASE_DRAW_WIDTH  : BASE_DRAW_WIDTH;
        float baseH = shadow ? SHADOW_BASE_DRAW_HEIGHT : BASE_DRAW_HEIGHT;
        if (!baseAnim.isAnimationFinished(age)) {
            drawFlipped(batch, baseAnim.getKeyFrame(age, false), wraith,
                wraith.getAnchorY() - baseH / 2f, baseW, baseH);
        }

        // Rising plume (both sets run at 20 FPS), bottom anchored at the feet.
        Animation<TextureRegion> screamAnim = shadow
            ? assets.getShadowScreamAnim() : assets.getScreamAnim();
        float plumeW = shadow ? SHADOW_SCREAM_DRAW_WIDTH  : SCREAM_DRAW_WIDTH;
        float plumeH = shadow ? SHADOW_SCREAM_DRAW_HEIGHT : SCREAM_DRAW_HEIGHT;
        if (!screamAnim.isAnimationFinished(age)) {
            drawFlipped(batch, screamAnim.getKeyFrame(age, false), wraith,
                wraith.getAnchorY(), plumeW, plumeH);
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
