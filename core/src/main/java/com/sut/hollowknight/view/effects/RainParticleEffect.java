package com.sut.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Editor-authored rain drops for the City of Tears, driven by
 * {@code effects/rain.p} (9-frame animated window_rain sprites, additive,
 * slow 60-80 px/s fall with a full fade-out transparency curve).
 *
 * This is the NEAR layer of the city's rain sandwich:
 *   - RainEffect (shader)      = distant full-screen rain sheet
 *   - RainParticleEffect(this) = sparse foreground drops that sink + fade
 *   - GlassRainEffect (shader) = drips on the window panes
 *
 * CAMERA-PINNED, with one deliberate override of the .p file: the authored
 * velocity x life is only ~180-360 px of travel before the fade completes,
 * so the file's thin top-ribbon spawn box would confine all drops to the
 * top of the view. We keep every authored motion/fade/tint value and
 * instead stretch the spawn box to the CURRENT camera view each frame -
 * drops are born throughout the view, drift down, and dissolve. The
 * authored fade-IN (0 -> 0.94 over the first 10% of life) is what masks
 * mid-air spawn pop.
 *
 * Note {@code attached: false} in the .p: live drops keep their world
 * position when the camera moves, so rain feels anchored to the world
 * while spawn coverage follows the view - best of both pinning modes.
 *
 * Zero-GC: the spawn-box refit is four float setters per frame, and
 * ParticleEffect reuses its internal arrays. ParticleEffect's default
 * cleansUpBlendFunction restores the batch blend state after the additive
 * pass, so the effects drawn after us are unaffected.
 */
public class RainParticleEffect implements Disposable {

    /** ~3 s at 30 Hz: one shortest-life drop generation mid-flight on entry. */
    private static final int   PREWARM_STEPS = 90;
    private static final float PREWARM_DT    = 1f / 30f;

    /** Overscan so drops drifting near the edges never spawn half-clipped. */
    private static final float EDGE_PAD = 64f;

    private final ParticleEffect effect;

    /** Pre-warm lazily on the first update: the ctor runs before the camera
     *  controller has framed the knight, so warming there would strand a
     *  cloud of drops at the wrong world position (attached: false). */
    private boolean prewarmed = false;

    public RainParticleEffect() {
        effect = new ParticleEffect();
        // imagesDir overload = the effect OWNS its nine frame textures;
        // dispose() releases them with no AssetManager involvement.
        effect.load(Gdx.files.internal("environment/particles/Rain.p"),
                    Gdx.files.internal("environment/particles/"));
        effect.start();
    }

    /** Advance the simulation. Called from updateLogic - frozen on pause. */
    public void update(OrthographicCamera cam, float delta) {
        float viewW = cam.viewportWidth  * cam.zoom + EDGE_PAD * 2f;
        float viewH = cam.viewportHeight * cam.zoom + EDGE_PAD * 2f;

        // Refit the spawn box to the view every frame - cheap, and robust
        // against zoom changes. Both ends of each range must be set: the
        // .p carries fixed editor values (1920 x 0..20) otherwise.
        for (int i = 0; i < effect.getEmitters().size; i++) {
            ParticleEmitter e = effect.getEmitters().get(i);
            e.getSpawnWidth().setLow(viewW);
            e.getSpawnWidth().setHigh(viewW);
            e.getSpawnHeight().setLow(viewH);
            e.getSpawnHeight().setHigh(viewH);
        }
        // Spawn box is centred on the effect position -> camera centre.
        effect.setPosition(cam.position.x, cam.position.y);

        if (!prewarmed) {
            prewarmed = true;
            for (int i = 0; i < PREWARM_STEPS; i++) {
                effect.update(PREWARM_DT);
            }
        }
        effect.update(delta);
    }

    /** Draw only (no implicit update). Call inside the world-space batch. */
    public void render(SpriteBatch batch) {
        effect.draw(batch);
    }

    @Override
    public void dispose() {
        effect.dispose(); // releases the nine window_rain frame textures
    }
}
