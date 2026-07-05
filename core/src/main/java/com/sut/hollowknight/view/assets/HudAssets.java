package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

public class HudAssets {

    private static final String BASE = "ui/hud/";

    private static final String FRAME_PATH          = BASE + "HUD Frame.atlas";
    private static final String HEALTH_IDLE_PATH    = BASE + "Health Node Idle.atlas";
    private static final String HEALTH_EMPTY_PATH   = BASE + "Health Node Empty.atlas";
    private static final String HEALTH_BREAK_PATH   = BASE + "Health Node Break.atlas";
    private static final String HEALTH_REFILL_PATH  = BASE + "Health Node Refill.atlas";
    private static final String SOUL_IDLE_PATH      = BASE + "Soulorb Idle.atlas";
    private static final String SOUL_FILL_PATH      = BASE + "Soulorb Fill.atlas";
    private static final String SOUL_DRAIN_PATH     = BASE + "Soulorb Drain.atlas";
    private static final String SOUL_GLOW_PATH      = BASE + "Soulorb Glow.atlas";

    //  Frame rates
    private static final float HEALTH_IDLE_FPS   = 12f;  // 45-frame breathing loop
    private static final float HEALTH_BREAK_FPS  = 20f;  // 6 frames -> 0.3 s pop
    private static final float HEALTH_REFILL_FPS = 15f;  // 6 frames -> 0.4 s
    private static final float SOUL_IDLE_FPS     = 8f;   // subtle liquid wobble
    private static final float SOUL_FILL_FPS     = 14f;  // slosh while gaining soul
    private static final float SOUL_DRAIN_FPS    = 14f;  // slosh while spending soul

    // Animations
    private final Animation<TextureRegion> healthIdleAnim;
    private final Animation<TextureRegion> healthBreakAnim;
    private final Animation<TextureRegion> healthRefillAnim;
    private final Animation<TextureRegion> soulIdleAnim;
    private final Animation<TextureRegion> soulFillAnim;
    private final Animation<TextureRegion> soulDrainAnim;

    // Single-frame regions
    private final TextureRegion hudFrame;
    private final TextureRegion healthEmpty;
    private final TextureRegion soulGlow;

    public HudAssets(AssetManager manager) {
        healthIdleAnim   = build(manager, HEALTH_IDLE_PATH,   HEALTH_IDLE_FPS,   Animation.PlayMode.LOOP);
        healthBreakAnim  = build(manager, HEALTH_BREAK_PATH,  HEALTH_BREAK_FPS,  Animation.PlayMode.NORMAL);
        healthRefillAnim = build(manager, HEALTH_REFILL_PATH, HEALTH_REFILL_FPS, Animation.PlayMode.NORMAL);
        soulIdleAnim     = build(manager, SOUL_IDLE_PATH,     SOUL_IDLE_FPS,     Animation.PlayMode.LOOP);
        soulFillAnim     = build(manager, SOUL_FILL_PATH,     SOUL_FILL_FPS,     Animation.PlayMode.LOOP);
        soulDrainAnim    = build(manager, SOUL_DRAIN_PATH,    SOUL_DRAIN_FPS,    Animation.PlayMode.LOOP);

        hudFrame    = firstRegion(manager, FRAME_PATH);
        healthEmpty = firstRegion(manager, HEALTH_EMPTY_PATH);
        soulGlow    = firstRegion(manager, SOUL_GLOW_PATH);
    }

    // Loading

    public static void loadAll(AssetManager manager) {
        manager.load(FRAME_PATH,         TextureAtlas.class);
        manager.load(HEALTH_IDLE_PATH,   TextureAtlas.class);
        manager.load(HEALTH_EMPTY_PATH,  TextureAtlas.class);
        manager.load(HEALTH_BREAK_PATH,  TextureAtlas.class);
        manager.load(HEALTH_REFILL_PATH, TextureAtlas.class);
        manager.load(SOUL_IDLE_PATH,     TextureAtlas.class);
        manager.load(SOUL_FILL_PATH,     TextureAtlas.class);
        manager.load(SOUL_DRAIN_PATH,    TextureAtlas.class);
        manager.load(SOUL_GLOW_PATH,     TextureAtlas.class);
    }

    private static Animation<TextureRegion> build(AssetManager manager,
                                                  String path,
                                                  float fps,
                                                  Animation.PlayMode playMode) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
        if (frames.size == 0) {
            throw new IllegalStateException("Atlas '" + path + "' has no regions");
        }
        frames.sort(Comparator.comparingInt(r -> r.index));
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return new Animation<>(1f / fps, frames, playMode);
    }

    private static TextureRegion firstRegion(AssetManager manager, String path) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        if (atlas.getRegions().size == 0) {
            throw new IllegalStateException("Atlas '" + path + "' has no regions");
        }
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return atlas.getRegions().first();
    }

    // Getters

    public Animation<TextureRegion> getHealthIdleAnim()   { return healthIdleAnim; }
    public Animation<TextureRegion> getHealthBreakAnim()  { return healthBreakAnim; }
    public Animation<TextureRegion> getHealthRefillAnim() { return healthRefillAnim; }
    public Animation<TextureRegion> getSoulIdleAnim()     { return soulIdleAnim; }
    public Animation<TextureRegion> getSoulFillAnim()     { return soulFillAnim; }
    public Animation<TextureRegion> getSoulDrainAnim()    { return soulDrainAnim; }

    public TextureRegion getHudFrame()    { return hudFrame; }
    public TextureRegion getHealthEmpty() { return healthEmpty; }
    public TextureRegion getSoulGlow()    { return soulGlow; }
}
