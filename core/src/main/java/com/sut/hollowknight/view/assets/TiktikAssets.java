package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class TiktikAssets {

    private static final String BASE = "animation/enemies/tiktik/";

    private static final String WALK_PATH          = BASE + "Walk.atlas";
    private static final String DEATH_AIR_OLD_PATH = BASE + "Death Air Old.atlas";
    private static final String DEATH_LAND_PATH    = BASE + "Death Land.atlas";

    //  Frame rates
    private static final float WALK_FPS          = 10f;
    private static final float DEATH_AIR_OLD_FPS = 20f;
    private static final float DEATH_LAND_FPS    = 15f;

    // Animations
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> deathAirOldAnim;
    private final Animation<TextureRegion> deathLandAnim;

    public TiktikAssets(AssetManager manager) {
        walkAnim        = build(manager, WALK_PATH,          WALK_FPS,          Animation.PlayMode.LOOP);
        deathAirOldAnim = build(manager, DEATH_AIR_OLD_PATH, DEATH_AIR_OLD_FPS, Animation.PlayMode.NORMAL);
        deathLandAnim   = build(manager, DEATH_LAND_PATH,    DEATH_LAND_FPS,    Animation.PlayMode.NORMAL);
    }

    // Loading

    public static void loadAll(AssetManager manager) {
        manager.load(WALK_PATH,          TextureAtlas.class);
        manager.load(DEATH_AIR_OLD_PATH, TextureAtlas.class);
        manager.load(DEATH_LAND_PATH,    TextureAtlas.class);
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

    public Animation<TextureRegion> getWalkAnim()        { return walkAnim; }
    public Animation<TextureRegion> getDeathAirOldAnim() { return deathAirOldAnim; }
    public Animation<TextureRegion> getDeathLandAnim()   { return deathLandAnim; }
}
