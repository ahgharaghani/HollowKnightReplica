package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Animations for the Vengeful Spirit spell:
 * <ul>
 *   <li><b>Ball</b> — the travelling fireball. Frame 1 plays once as it leaves
 *       the knight, then frames 2–4 loop for the flight (20 FPS).</li>
 *   <li><b>Ball End</b> — the fireball dissipating on wall impact (24 FPS).</li>
 *   <li><b>Blast</b> — the burst on the knight as the ball emerges (24 FPS).</li>
 *   <li><b>Ball Wall Impact</b> — the burst on the wall the ball strikes (24 FPS).</li>
 * </ul>
 */
public class VengefulSpiritAssets {

    private static final String BASE = "animation/effects/vengeful_spirit/";

    private static final String BALL_PATH        = BASE + "Ball.atlas";
    private static final String BALL_END_PATH    = BASE + "Ball End.atlas";
    private static final String BLAST_PATH       = BASE + "Blast.atlas";
    private static final String WALL_IMPACT_PATH = BASE + "Ball Wall Impact.atlas";

    private static final float BALL_FPS        = 20f;
    private static final float BALL_END_FPS    = 24f;
    private static final float BLAST_FPS       = 24f;
    private static final float WALL_IMPACT_FPS = 24f;

    /** Frame index at which the ball flight loop begins (2nd frame). */
    private static final int BALL_LOOP_START = 1;

    private final Animation<TextureRegion> ballAnim;      // full 4-frame ball, NORMAL
    private final Animation<TextureRegion> ballEndAnim;
    private final Animation<TextureRegion> blastAnim;
    private final Animation<TextureRegion> wallImpactAnim;

    public VengefulSpiritAssets(AssetManager manager) {
        ballAnim       = build(manager, BALL_PATH,        BALL_FPS,        Animation.PlayMode.NORMAL);
        ballEndAnim    = build(manager, BALL_END_PATH,    BALL_END_FPS,    Animation.PlayMode.NORMAL);
        blastAnim      = build(manager, BLAST_PATH,       BLAST_FPS,       Animation.PlayMode.NORMAL);
        wallImpactAnim = build(manager, WALL_IMPACT_PATH, WALL_IMPACT_FPS, Animation.PlayMode.NORMAL);
    }

    // ---- Loading ----

    public static void loadAll(AssetManager manager) {
        manager.load(BALL_PATH,        TextureAtlas.class);
        manager.load(BALL_END_PATH,    TextureAtlas.class);
        manager.load(BLAST_PATH,       TextureAtlas.class);
        manager.load(WALL_IMPACT_PATH, TextureAtlas.class);
    }

    private static Animation<TextureRegion> build(AssetManager manager, String path,
                                                  float fps, Animation.PlayMode mode) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
        if (frames.size == 0) {
            throw new IllegalStateException("Atlas '" + path + "' has no regions");
        }
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return new Animation<>(1f / fps, frames, mode);
    }

    // ---- Frame selection ----

    /**
     * The ball's flight frame for a given elapsed time: the first frame plays
     * once, then frames 2–4 loop for the remainder of the flight.
     */
    public TextureRegion getBallFrame(float stateTime) {
        float fd = ballAnim.getFrameDuration();
        int total = ((TextureRegion[]) ballAnim.getKeyFrames()).length;
        if (total <= BALL_LOOP_START + 1) {
            return ballAnim.getKeyFrame(stateTime, true);
        }

        float leadTime = BALL_LOOP_START * fd;      // duration of the intro frame(s)
        if (stateTime < leadTime) {
            return ballAnim.getKeyFrame(stateTime, false);
        }
        // Wrap the remaining time into the looping tail [BALL_LOOP_START, total-1].
        int loopLen = total - BALL_LOOP_START;
        float loopTime = leadTime + ((stateTime - leadTime) % (loopLen * fd));
        return ballAnim.getKeyFrame(loopTime, false);
    }

    // ---- Getters ----

    public Animation<TextureRegion> getBallEndAnim()    { return ballEndAnim; }
    public Animation<TextureRegion> getBlastAnim()      { return blastAnim; }
    public Animation<TextureRegion> getWallImpactAnim() { return wallImpactAnim; }
}
