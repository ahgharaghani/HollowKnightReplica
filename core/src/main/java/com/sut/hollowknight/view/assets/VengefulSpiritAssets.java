package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class VengefulSpiritAssets {

    private static final String BASE = "animation/effects/vengeful_spirit/";

    private static final String BALL_PATH        = BASE + "Ball.atlas";
    private static final String BALL_END_PATH    = BASE + "Ball End.atlas";
    private static final String BLAST_PATH       = BASE + "Blast.atlas";
    private static final String WALL_IMPACT_PATH = BASE + "Ball Wall Impact.atlas";

    // Void (shadow) variants - swapped in while Void Heart is equipped.
    private static final String SHADOW_BALL_PATH  = BASE + "Shadow Ball.atlas";
    private static final String SHADOW_BLAST_PATH = BASE + "Shadow Blast.atlas";

    private static final float BALL_FPS        = 20f;
    private static final float BALL_END_FPS    = 24f;
    private static final float BLAST_FPS       = 24f;
    private static final float WALL_IMPACT_FPS = 24f;

    // Shadow variants (art direction): both run at 24 FPS.
    private static final float SHADOW_BALL_FPS  = 24f;
    private static final float SHADOW_BLAST_FPS = 24f;

    /** Frame index at which the ball flight loop begins (2nd frame). */
    private static final int BALL_LOOP_START = 1;

    /** Shadow ball: 6 frames - the first 2 lead in, the last 4 loop. */
    private static final int SHADOW_BALL_LOOP_START = 2;

    private final Animation<TextureRegion> ballAnim;
    private final Animation<TextureRegion> ballEndAnim;
    private final Animation<TextureRegion> blastAnim;
    private final Animation<TextureRegion> wallImpactAnim;
    private final Animation<TextureRegion> shadowBallAnim;
    private final Animation<TextureRegion> shadowBlastAnim;

    /** Captured at build-time so we never have to call getKeyFrames(). */
    private final int ballFrameCount;
    private final int shadowBallFrameCount;

    public VengefulSpiritAssets(AssetManager manager) {
        TextureAtlas ballAtlas = manager.get(BALL_PATH, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> ballFrames = typedRegions(ballAtlas, BALL_PATH);
        setLinear(ballAtlas);
        ballAnim        = new Animation<>(1f / BALL_FPS, ballFrames, Animation.PlayMode.NORMAL);
        ballFrameCount  = ballFrames.size;

        ballEndAnim    = build(manager, BALL_END_PATH,    BALL_END_FPS,    Animation.PlayMode.NORMAL);
        blastAnim      = build(manager, BLAST_PATH,       BLAST_FPS,       Animation.PlayMode.NORMAL);
        wallImpactAnim = build(manager, WALL_IMPACT_PATH, WALL_IMPACT_FPS, Animation.PlayMode.NORMAL);

        // The shadow atlases ship with scrambled region order (index-tagged),
        // so their frames MUST be sorted by index before animating.
        shadowBallAnim       = buildSorted(manager, SHADOW_BALL_PATH,  SHADOW_BALL_FPS);
        shadowBlastAnim      = buildSorted(manager, SHADOW_BLAST_PATH, SHADOW_BLAST_FPS);
        shadowBallFrameCount = shadowBallAnim.getKeyFrames().length;
    }

    // ---- Loading ----

    public static void loadAll(AssetManager manager) {
        manager.load(BALL_PATH,        TextureAtlas.class);
        manager.load(BALL_END_PATH,    TextureAtlas.class);
        manager.load(BLAST_PATH,       TextureAtlas.class);
        manager.load(WALL_IMPACT_PATH, TextureAtlas.class);
        manager.load(SHADOW_BALL_PATH,  TextureAtlas.class);
        manager.load(SHADOW_BLAST_PATH, TextureAtlas.class);
    }

    private static Animation<TextureRegion> build(AssetManager manager, String path,
                                                  float fps, Animation.PlayMode mode) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> frames = typedRegions(atlas, path);
        setLinear(atlas);
        return new Animation<>(1f / fps, frames, mode);
    }

    /**
     * Copies an atlas's regions into an Array explicitly backed by
     * AtlasRegion[]. TextureAtlas.getRegions() is backed by Object[]
     * (libGDX builds it with the untyped Array constructor), and an
     * Animation created from it inherits that Object[] backing - then
     * getKeyFrames() throws ClassCastException when the erased T[] cast
     * is reified at the call site. findRegions() does not have this bug
     * because it constructs a typed Array, which is why the original
     * knight/enemy loaders never crashed.
     */
    private static Array<TextureAtlas.AtlasRegion> typedRegions(TextureAtlas atlas, String path) {
        Array<TextureAtlas.AtlasRegion> source = atlas.getRegions();
        if (source.size == 0) {
            throw new IllegalStateException("Atlas '" + path + "' has no regions");
        }
        Array<TextureAtlas.AtlasRegion> typed =
            new Array<>(true, source.size, TextureAtlas.AtlasRegion.class);
        typed.addAll(source);
        return typed;
    }

    /**
     * Like {@link #build}, but sorts frames by their atlas index first -
     * required for the shadow atlases, whose file order is scrambled.
     */
    private static Animation<TextureRegion> buildSorted(AssetManager manager, String path, float fps) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> frames = typedRegions(atlas, path);
        frames.sort(java.util.Comparator.comparingInt(r -> r.index));
        setLinear(atlas);
        return new Animation<>(1f / fps, frames, Animation.PlayMode.NORMAL);
    }

    private static void setLinear(TextureAtlas atlas) {
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    // ---- Frame selection ----

    /**
     * The ball's flight frame for a given elapsed time: the first frame plays
     * once, then frames 2–4 loop for the remainder of the flight.
     */
    public TextureRegion getBallFrame(float stateTime) {
        return ballFrame(ballAnim, ballFrameCount, BALL_LOOP_START, stateTime);
    }

    /** Shadow ball flight frame: 2 lead-in frames, then the last 4 loop. */
    public TextureRegion getShadowBallFrame(float stateTime) {
        return ballFrame(shadowBallAnim, shadowBallFrameCount,
            SHADOW_BALL_LOOP_START, stateTime);
    }

    /** Shared lead-in + looping-tail frame selection (no allocation). */
    private static TextureRegion ballFrame(Animation<TextureRegion> anim, int total,
                                           int loopStart, float stateTime) {
        float fd = anim.getFrameDuration();

        if (total <= loopStart + 1) {
            return anim.getKeyFrame(stateTime, true);
        }

        float leadTime = loopStart * fd;
        if (stateTime < leadTime) {
            return anim.getKeyFrame(stateTime, false);
        }
        // Wrap the remaining time into the looping tail [loopStart, total-1].
        int loopLen = total - loopStart;
        float loopTime = leadTime + ((stateTime - leadTime) % (loopLen * fd));
        return anim.getKeyFrame(loopTime, false);
    }

    // ---- Getters ----

    public Animation<TextureRegion> getBallEndAnim()    { return ballEndAnim; }
    public Animation<TextureRegion> getBlastAnim()      { return blastAnim; }
    public Animation<TextureRegion> getWallImpactAnim() { return wallImpactAnim; }
    public Animation<TextureRegion> getShadowBlastAnim() { return shadowBlastAnim; }
}
