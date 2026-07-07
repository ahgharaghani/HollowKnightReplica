package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

public class HowlingWraithAssets {

    private static final String BASE = "animation/effects/scream/";

    private static final String SCREAM_PATH      = BASE + "Scream.atlas";
    private static final String SCREAM_BASE_PATH = BASE + "Scream Base.atlas";

    // Per the art direction: the plume runs at 20 FPS, the base at 15 FPS.
    private static final float SCREAM_FPS      = 20f;
    private static final float SCREAM_BASE_FPS = 15f;

    private final Animation<TextureRegion> screamAnim;     // 13 frames, 332x306
    private final Animation<TextureRegion> screamBaseAnim; //  8 frames, 350x134

    public HowlingWraithAssets(AssetManager manager) {
        screamAnim     = build(manager, SCREAM_PATH,      SCREAM_FPS);
        screamBaseAnim = build(manager, SCREAM_BASE_PATH, SCREAM_BASE_FPS);
    }

    // ---- Loading ----

    public static void loadAll(AssetManager manager) {
        manager.load(SCREAM_PATH,      TextureAtlas.class);
        manager.load(SCREAM_BASE_PATH, TextureAtlas.class);
    }

    /**
     * The scream regions share one name and are index-tagged, and their file
     * order is scrambled — so frames MUST be sorted by index, not taken in
     * atlas order.
     */
    private static Animation<TextureRegion> build(AssetManager manager, String path, float fps) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> frames = new Array<>(atlas.getRegions());
        if (frames.size == 0) {
            throw new IllegalStateException("Atlas '" + path + "' has no regions");
        }
        frames.sort(Comparator.comparingInt(r -> r.index));
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return new Animation<>(1f / fps, frames, Animation.PlayMode.NORMAL);
    }

    // ---- Getters ----

    public Animation<TextureRegion> getScreamAnim()     { return screamAnim; }
    public Animation<TextureRegion> getScreamBaseAnim() { return screamBaseAnim; }
}
