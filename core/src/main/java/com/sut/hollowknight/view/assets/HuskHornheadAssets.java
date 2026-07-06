package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class HuskHornheadAssets {

    private static final String BASE = "animation/enemies/husk_hornhead/";

    private static final String IDLE_PATH        = BASE + "Idle.atlas";
    private static final String WALK_PATH        = BASE + "Walk.atlas";
    private static final String TURN_PATH        = BASE + "Turn.atlas";
    private static final String ANTIC_PATH       = BASE + "Attack Anticipate.atlas";
    private static final String LUNGE_PATH       = BASE + "Attack Lunge.atlas";
    private static final String COOLDOWN_PATH    = BASE + "Attack Cooldown.atlas";
    private static final String DEATH_AIR_PATH   = BASE + "Death Air.atlas";
    private static final String DEATH_LAND_PATH  = BASE + "Death Land.atlas";

    //  Frame rates
    private static final float IDLE_FPS       = 10f;
    private static final float WALK_FPS       = 10f;
    private static final float TURN_FPS       = 10f;
    private static final float ANTIC_FPS      = 12f;
    private static final float LUNGE_FPS      = 12f;
    private static final float COOLDOWN_FPS   = 12f;
    private static final float DEATH_AIR_FPS  = 30f;
    private static final float DEATH_LAND_FPS = 12f;

    // Animations
    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> turnAnim;
    private final Animation<TextureRegion> anticAnim;
    private final Animation<TextureRegion> lungeAnim;
    private final Animation<TextureRegion> cooldownAnim;
    private final Animation<TextureRegion> deathAirAnim;
    private final Animation<TextureRegion> deathLandAnim;

    public HuskHornheadAssets(AssetManager manager) {
        idleAnim      = build(manager, IDLE_PATH,       IDLE_FPS,       Animation.PlayMode.LOOP);
        walkAnim      = build(manager, WALK_PATH,       WALK_FPS,       Animation.PlayMode.LOOP);
        turnAnim      = build(manager, TURN_PATH,       TURN_FPS,       Animation.PlayMode.NORMAL);
        anticAnim     = build(manager, ANTIC_PATH,      ANTIC_FPS,      Animation.PlayMode.NORMAL);
        lungeAnim     = build(manager, LUNGE_PATH,      LUNGE_FPS,      Animation.PlayMode.LOOP);
        cooldownAnim  = build(manager, COOLDOWN_PATH,   COOLDOWN_FPS,   Animation.PlayMode.NORMAL);
        deathAirAnim  = build(manager, DEATH_AIR_PATH,  DEATH_AIR_FPS,  Animation.PlayMode.NORMAL);
        deathLandAnim = build(manager, DEATH_LAND_PATH, DEATH_LAND_FPS, Animation.PlayMode.NORMAL);
    }

    // Loading

    public static void loadAll(AssetManager manager) {
        manager.load(IDLE_PATH,       TextureAtlas.class);
        manager.load(WALK_PATH,       TextureAtlas.class);
        manager.load(TURN_PATH,       TextureAtlas.class);
        manager.load(ANTIC_PATH,      TextureAtlas.class);
        manager.load(LUNGE_PATH,      TextureAtlas.class);
        manager.load(COOLDOWN_PATH,   TextureAtlas.class);
        manager.load(DEATH_AIR_PATH,  TextureAtlas.class);
        manager.load(DEATH_LAND_PATH, TextureAtlas.class);
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
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        float frameDuration = 1f / fps;
        return new Animation<>(frameDuration, frames, playMode);
    }

    // Getters

    public Animation<TextureRegion> getIdleAnim()      { return idleAnim; }
    public Animation<TextureRegion> getWalkAnim()      { return walkAnim; }
    public Animation<TextureRegion> getTurnAnim()      { return turnAnim; }
    public Animation<TextureRegion> getAnticAnim()     { return anticAnim; }
    public Animation<TextureRegion> getLungeAnim()     { return lungeAnim; }
    public Animation<TextureRegion> getCooldownAnim()  { return cooldownAnim; }
    public Animation<TextureRegion> getDeathAirAnim()  { return deathAirAnim; }
    public Animation<TextureRegion> getDeathLandAnim() { return deathLandAnim; }
}
