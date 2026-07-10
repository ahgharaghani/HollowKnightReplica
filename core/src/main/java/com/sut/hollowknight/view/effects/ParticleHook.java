package com.sut.hollowknight.view.effects;

/**
 * Seam for particle effects that arrive later (spec: "make it so we can
 * add particle emission at a later time").
 *
 * <p>Game code fires these notifications at the exact moments particles
 * should appear; the {@link #NO_OP} default does nothing, so the game
 * runs unchanged until a real emitter is plugged in through
 * {@code GameScreen.setParticleHook(...)}. Implementations must be
 * allocation-free per call - these fire from inside the game loop.</p>
 */
public interface ParticleHook {

    /** A nail hit landed on a breakable wall (small dust puff). */
    void onWallHit(float centerX, float centerY);

    /** A breakable wall just crumbled (big dust burst over its area). */
    void onWallBreak(float centerX, float centerY, float width, float height);

    /** Default do-nothing hook used until a real emitter exists. */
    ParticleHook NO_OP = new ParticleHook() {
        @Override public void onWallHit(float centerX, float centerY) { }
        @Override public void onWallBreak(float centerX, float centerY,
                                          float width, float height) { }
    };
}
