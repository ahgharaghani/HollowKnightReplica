package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class CrystalGuardianAssets {

    private static final String BASE = "animation/enemies/crystal_guardian/";

    private static final String IDLE_PATH        = BASE + "Idle.atlas";
    private static final String TURN_PATH        = BASE + "Turn.atlas";
    private static final String RUN_PATH         = BASE + "Run.atlas";
    private static final String SHOOT_ANTIC_PATH = BASE + "Shoot Antic.atlas";
    private static final String SHOOT_PATH       = BASE + "Shoot.atlas";
    private static final String SHOOT_LOOP_PATH  = BASE + "Shoot Loop.atlas";
    private static final String SHOOT_END_PATH   = BASE + "Shoot End.atlas";
    private static final String DEATH_AIR_PATH   = BASE + "Death Air.atlas";
    private static final String DEATH_LAND_PATH  = BASE + "Death Land.atlas";

    //  Frame rates
    private static final float IDLE_FPS        = 12f;
    private static final float TURN_FPS        = 12f;
    private static final float RUN_FPS         = 12f;
    private static final float SHOOT_ANTIC_FPS = 12f;
    private static final float SHOOT_FPS       = 12f;
    private static final float SHOOT_LOOP_FPS  = 12f;
    private static final float SHOOT_END_FPS   = 12f;
    private static final float DEATH_AIR_FPS   = 12f;
    private static final float DEATH_LAND_FPS  = 12f;

    // Animations
    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> turnAnim;
    private final Animation<TextureRegion> runAnim;
    private final Animation<TextureRegion> shootAnticAnim;
    private final Animation<TextureRegion> shootAnim;
    private final Animation<TextureRegion> shootLoopAnim;
    private final Animation<TextureRegion> shootEndAnim;
    private final Animation<TextureRegion> deathAirAnim;
    private final Animation<TextureRegion> deathLandAnim;

    public CrystalGuardianAssets(AssetManager manager) {
        idleAnim       = build(manager, IDLE_PATH,        IDLE_FPS,        Animation.PlayMode.LOOP);
        turnAnim       = build(manager, TURN_PATH,        TURN_FPS,        Animation.PlayMode.NORMAL);
        runAnim        = build(manager, RUN_PATH,         RUN_FPS,         Animation.PlayMode.LOOP);
        shootAnticAnim = build(manager, SHOOT_ANTIC_PATH, SHOOT_ANTIC_FPS, Animation.PlayMode.NORMAL);
        shootAnim      = build(manager, SHOOT_PATH,       SHOOT_FPS,       Animation.PlayMode.NORMAL);
        shootLoopAnim  = build(manager, SHOOT_LOOP_PATH,  SHOOT_LOOP_FPS,  Animation.PlayMode.LOOP);
        shootEndAnim   = build(manager, SHOOT_END_PATH,   SHOOT_END_FPS,   Animation.PlayMode.NORMAL);
        deathAirAnim   = build(manager, DEATH_AIR_PATH,   DEATH_AIR_FPS,   Animation.PlayMode.NORMAL);
        deathLandAnim  = build(manager, DEATH_LAND_PATH,  DEATH_LAND_FPS,  Animation.PlayMode.NORMAL);
    }

    // Loading

    public static void loadAll(AssetManager manager) {
        manager.load(IDLE_PATH,        TextureAtlas.class);
        manager.load(TURN_PATH,        TextureAtlas.class);
        manager.load(RUN_PATH,         TextureAtlas.class);
        manager.load(SHOOT_ANTIC_PATH, TextureAtlas.class);
        manager.load(SHOOT_PATH,       TextureAtlas.class);
        manager.load(SHOOT_LOOP_PATH,  TextureAtlas.class);
        manager.load(SHOOT_END_PATH,   TextureAtlas.class);
        manager.load(DEATH_AIR_PATH,   TextureAtlas.class);
        manager.load(DEATH_LAND_PATH,  TextureAtlas.class);
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

    public Animation<TextureRegion> getIdleAnim()       { return idleAnim; }
    public Animation<TextureRegion> getTurnAnim()       { return turnAnim; }
    public Animation<TextureRegion> getRunAnim()        { return runAnim; }
    public Animation<TextureRegion> getShootAnticAnim() { return shootAnticAnim; }
    public Animation<TextureRegion> getShootAnim()      { return shootAnim; }
    public Animation<TextureRegion> getShootLoopAnim()  { return shootLoopAnim; }
    public Animation<TextureRegion> getShootEndAnim()   { return shootEndAnim; }
    public Animation<TextureRegion> getDeathAirAnim()   { return deathAirAnim; }
    public Animation<TextureRegion> getDeathLandAnim()  { return deathLandAnim; }
}
