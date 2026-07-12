package com.sut.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Ambient floating crystal shards (Crystal Peaks flavour), driven by the
 * editor-authored effect file {@code effects/crystal_shards.p}.
 *
 * WORLD-PINNED: the .p ships with the editor's 1920x1080 preview spawn box.
 * We override it at runtime to the ROOM's pixel bounds and centre the effect
 * on the map, so the shard field is a stationary property of the room and
 * the camera pans across it - particles never follow the player the way a
 * camera-pinned emitter would.
 *
 * Architecture notes:
 *  - One ParticleEffect per room, continuous, never completes - no
 *    ParticleEffectPool needed (pools are for finite burst effects).
 *  - update() and render() are deliberately split so the pause/inventory
 *    freeze applies: updateLogic() drives the simulation, the world render
 *    pass only draws. ParticleEffect#draw(Batch,float) would sneak
 *    simulation into the render path and break the freeze.
 *  - Pre-warmed in the constructor so the player never watches the emitter
 *    "fill up" on room entry.
 *  - Zero-GC after construction: ParticleEffect reuses its internal particle
 *    arrays; nothing is allocated per frame.
 */
public class CrystalShardsEffect implements Disposable {

    /** Fixed-step pre-warm: 4s at 30Hz comfortably covers the 5s max life. */
    private static final int   PREWARM_STEPS = 120;
    private static final float PREWARM_DT    = 1f / 30f;

    private final ParticleEffect effect;

    public CrystalShardsEffect(float mapWidthPx, float mapHeightPx) {
        effect = new ParticleEffect();
        // imagesDir overload = the effect OWNS its six crystal textures;
        // dispose() below releases them with no AssetManager involvement.
        effect.load(Gdx.files.internal("environment/particles/Crystal Shards.p"),
                    Gdx.files.internal("environment/particles"));

        // World-pin: stretch every emitter's spawn box to the room bounds.
        // Set BOTH ends of each range - the file carries fixed low/high
        // values (1920/1080) and a stale low would shrink the box.
        for (int i = 0; i < effect.getEmitters().size; i++) {
            ParticleEmitter e = effect.getEmitters().get(i);
            e.getSpawnShape().setShape(ParticleEmitter.SpawnShape.square);
            e.getSpawnWidth().setLow(mapWidthPx);
            e.getSpawnWidth().setHigh(mapWidthPx);
            e.getSpawnHeight().setLow(mapHeightPx);
            e.getSpawnHeight().setHigh(mapHeightPx);
        }

        // The spawn box is centred on the effect position -> map centre.
        effect.setPosition(mapWidthPx * 0.5f, mapHeightPx * 0.5f);
        effect.start();

        // Pre-warm so the field is fully populated on the first frame.
        for (int i = 0; i < PREWARM_STEPS; i++) {
            effect.update(PREWARM_DT);
        }
    }

    /** Advance the simulation. Called from updateLogic - frozen on pause. */
    public void update(float delta) {
        effect.update(delta);
    }

    /** Draw only (no implicit update). Call inside the world-space batch. */
    public void render(SpriteBatch batch) {
        effect.draw(batch);
    }

    @Override
    public void dispose() {
        effect.dispose(); // releases the six crystal textures loaded above
    }
}
