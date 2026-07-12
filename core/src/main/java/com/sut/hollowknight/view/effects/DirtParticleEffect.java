package com.sut.hollowknight.view.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Dirt specks knocked loose from breakable walls (plugs into the
 * {@link ParticleHook} seam the walls controller already fires).
 *
 * <p>Architecture: a fixed-size ring pool of particles stored in parallel
 * primitive arrays - no objects, no boxing, zero allocation after the
 * constructor, so spawning bursts mid-combat can never trigger GC hitches.
 * When the pool wraps, the oldest speck is recycled; visually invisible
 * because old specks are the most faded ones anyway.</p>
 *
 * <p>Sim: each speck gets a random outward velocity, gravity pulls it into
 * a short ballistic arc, mild air drag settles it, and it fades out over
 * its {@link #LIFETIME 2-second} life (alpha ramps down over the back
 * half). Rendering tints a shared 1x1 white pixel dirt-brown, one of two
 * shades per speck so the debris reads as grit rather than confetti.</p>
 */
public class DirtParticleEffect implements ParticleHook, Disposable {

    /** Specs disappear after 2 seconds (request). */
    private static final float LIFETIME = 2f;

    /** Pool size: onWallBreak's big burst x a few rapid swings, with room. */
    private static final int MAX_PARTICLES = 256;

    private static final int HIT_COUNT   = 12;  // specks per nail hit
    private static final int BREAK_COUNT = 48;  // specks when the wall crumbles

    private static final float GRAVITY   = 900f; // px/s^2, matches world feel
    private static final float DRAG      = 2.2f; // per-second velocity decay
    private static final float HIT_SPEED_MIN   = 60f;
    private static final float HIT_SPEED_MAX   = 220f;
    private static final float BREAK_SPEED_MIN = 40f;
    private static final float BREAK_SPEED_MAX = 260f;
    private static final float SIZE_MIN = 2f;   // px - "little" specks
    private static final float SIZE_MAX = 5f;

    // Two dirt shades; per-speck choice breaks up the color monotony.
    private static final Color DIRT_DARK  = new Color(0.35f, 0.27f, 0.20f, 1f);
    private static final Color DIRT_LIGHT = new Color(0.52f, 0.42f, 0.30f, 1f);

    // ---- Particle pool (structure-of-arrays: cache-friendly, no objects) ----
    private final float[] px   = new float[MAX_PARTICLES];
    private final float[] py   = new float[MAX_PARTICLES];
    private final float[] vx   = new float[MAX_PARTICLES];
    private final float[] vy   = new float[MAX_PARTICLES];
    private final float[] size = new float[MAX_PARTICLES];
    private final float[] life = new float[MAX_PARTICLES]; // seconds left; <=0 = dead
    private final boolean[] dark = new boolean[MAX_PARTICLES];
    private int cursor; // ring write position - oldest speck is recycled first

    private final Texture pixel;

    public DirtParticleEffect() {
        // Shared 1x1 white canvas, tinted per speck at draw time.
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
    }

    // ================= ParticleHook =================

    @Override
    public void onWallHit(float centerX, float centerY) {
        // Small puff: specks spray outward, biased upward like chipped
        // plaster (full-circle angle, but upward speeds survive drag longer).
        burst(centerX, centerY, HIT_COUNT, HIT_SPEED_MIN, HIT_SPEED_MAX, 0f, 0f);
    }

    @Override
    public void onWallBreak(float centerX, float centerY,
                            float width, float height) {
        // Big burst seeded across the whole wall face so the debris looks
        // like the wall itself coming apart, not a point explosion.
        burst(centerX, centerY, BREAK_COUNT, BREAK_SPEED_MIN, BREAK_SPEED_MAX,
            width * 0.5f, height * 0.5f);
    }

    private void burst(float cx, float cy, int count,
                       float speedMin, float speedMax,
                       float spreadX, float spreadY) {
        for (int n = 0; n < count; n++) {
            int i = cursor;
            cursor = (cursor + 1) % MAX_PARTICLES; // recycle the oldest

            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(speedMin, speedMax);
            px[i] = cx + MathUtils.random(-spreadX, spreadX);
            py[i] = cy + MathUtils.random(-spreadY, spreadY);
            vx[i] = MathUtils.cos(angle) * speed;
            // Upward bias: falling debris reads better with a little pop.
            vy[i] = MathUtils.sin(angle) * speed + speed * 0.35f;
            size[i] = MathUtils.random(SIZE_MIN, SIZE_MAX);
            life[i] = LIFETIME;
            dark[i] = MathUtils.randomBoolean();
        }
    }

    // ================= Frame hooks =================

    /** Integrate the sim. Skipped while paused (world time is frozen). */
    public void update(float delta) {
        float dragFactor = 1f - Math.min(DRAG * delta, 0.9f);
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (life[i] <= 0f) continue;
            life[i] -= delta;
            vy[i] -= GRAVITY * delta;
            vx[i] *= dragFactor;
            px[i] += vx[i] * delta;
            py[i] += vy[i] * delta;
        }
    }

    /** Draw inside an open world-space batch (same pass as the rain). */
    public void render(SpriteBatch batch) {
        Color prev = batch.getColor();
        float prevPacked = prev.toFloatBits(); // restore exactly afterwards
        for (int i = 0; i < MAX_PARTICLES; i++) {
            if (life[i] <= 0f) continue;
            // Fade over the back half of the 2s life; fully gone at 0.
            float a = Math.min(1f, (life[i] / LIFETIME) * 2f);
            Color c = dark[i] ? DIRT_DARK : DIRT_LIGHT;
            batch.setColor(c.r, c.g, c.b, a);
            float s = size[i];
            batch.draw(pixel, px[i] - s / 2f, py[i] - s / 2f, s, s);
        }
        batch.setPackedColor(prevPacked);
    }

    @Override
    public void dispose() {
        pixel.dispose();
    }
}
