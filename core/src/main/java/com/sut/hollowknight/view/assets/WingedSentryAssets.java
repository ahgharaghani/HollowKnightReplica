package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class WingedSentryAssets {

    // Atlas paths
    private static final String BASE = "animation/enemies/winged_sentry/";

    private static final String IDLE_PATH            = BASE + "Idle/Idle.atlas";
    private static final String CHARGE_ANTIC_PATH    = BASE + "Charge Antic/Charge Antic.atlas";
    private static final String CHARGE_PATH          = BASE + "Charge/Charge.atlas";
    private static final String CHARGE_RECOVER_PATH  = BASE + "Charge Recover/Charge Recover.atlas";
    private static final String THROW_ATTACK_PATH    = BASE + "Throw Attack/Throw Attack.atlas";
    private static final String DEATH_AIR_PATH       = BASE + "Death Air/Death Air.atlas";
    private static final String DEATH_LAND_PATH      = BASE + "Death Land/Death Land.atlas";
    private static final String TURN_TO_IDLE_PATH    = BASE + "Turn To Idle/Turn To Idle.atlas";

    // ---- Javelin animations ----
    private static final String JAVELIN_BASE      = "animation/enemies/javelin/";
    private static final String JAVELIN_IMPACT_PATH  = JAVELIN_BASE + "Javelin/Javelin.atlas";
    private static final String JAVELIN_NEUTRAL_PATH = JAVELIN_BASE + "Javelin Neutral/Javelin Neutral.atlas";
    private static final String JAVELIN_SNAP_PATH    = JAVELIN_BASE + "Javelin Snap/Javelin Snap.atlas";
    private static final String JAVELIN_STICK_PATH   = JAVELIN_BASE + "Javelin Stick/Javelin Stick.atlas";

    //  Frame rates
    // Match the values previously hard-coded inside the renderers.
    private static final float IDLE_FPS            = 8f;
    private static final float TURN_TO_IDLE_FPS    = 10f;
    private static final float CHARGE_ANTIC_FPS    = 9f;
    private static final float CHARGE_FPS          = 10f;
    private static final float CHARGE_RECOVER_FPS  = 8f;
    private static final float THROW_ATTACK_FPS    = 10f;
    private static final float DEATH_AIR_FPS       = 8f;
    private static final float DEATH_LAND_FPS      = 8f;

    private static final float JAVELIN_NEUTRAL_FPS = 1f;
    private static final float JAVELIN_IMPACT_FPS  = 10f;
    private static final float JAVELIN_STICK_FPS   = 6f;
    private static final float JAVELIN_SNAP_FPS    = 10f;

    // Animations
    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> chargeAnticAnim;
    private final Animation<TextureRegion> chargeAnim;
    private final Animation<TextureRegion> chargeRecoverAnim;
    private final Animation<TextureRegion> throwAttackAnim;
    private final Animation<TextureRegion> deathAirAnim;
    private final Animation<TextureRegion> deathLandAnim;
    private final Animation<TextureRegion> turnToIdleAnim;

    private final Animation<TextureRegion> javelinImpactAnim;
    private final Animation<TextureRegion> javelinNeutralAnim;
    private final Animation<TextureRegion> javelinSnapAnim;
    private final Animation<TextureRegion> javelinStickAnim;

    public WingedSentryAssets(AssetManager manager) {
        idleAnim           = build(manager, IDLE_PATH,            IDLE_FPS,            Animation.PlayMode.LOOP);
        chargeAnticAnim    = build(manager, CHARGE_ANTIC_PATH,    CHARGE_ANTIC_FPS,    Animation.PlayMode.NORMAL);
        chargeAnim         = build(manager, CHARGE_PATH,          CHARGE_FPS,          Animation.PlayMode.LOOP);
        chargeRecoverAnim  = build(manager, CHARGE_RECOVER_PATH,  CHARGE_RECOVER_FPS,  Animation.PlayMode.NORMAL);
        throwAttackAnim    = build(manager, THROW_ATTACK_PATH,    THROW_ATTACK_FPS,    Animation.PlayMode.NORMAL);
        deathAirAnim       = build(manager, DEATH_AIR_PATH,       DEATH_AIR_FPS,       Animation.PlayMode.NORMAL);
        deathLandAnim      = build(manager, DEATH_LAND_PATH,      DEATH_LAND_FPS,      Animation.PlayMode.NORMAL);
        turnToIdleAnim     = build(manager, TURN_TO_IDLE_PATH,    TURN_TO_IDLE_FPS,    Animation.PlayMode.NORMAL);

        javelinImpactAnim  = build(manager, JAVELIN_IMPACT_PATH,  JAVELIN_IMPACT_FPS,  Animation.PlayMode.NORMAL);
        javelinNeutralAnim = build(manager, JAVELIN_NEUTRAL_PATH, JAVELIN_NEUTRAL_FPS, Animation.PlayMode.LOOP);
        javelinSnapAnim    = build(manager, JAVELIN_SNAP_PATH,    JAVELIN_SNAP_FPS,    Animation.PlayMode.NORMAL);
        javelinStickAnim   = build(manager, JAVELIN_STICK_PATH,   JAVELIN_STICK_FPS,   Animation.PlayMode.LOOP);
    }

    // Loading

    public static void loadAll(AssetManager manager) {
        queueAtlas(manager, IDLE_PATH);
        queueAtlas(manager, CHARGE_ANTIC_PATH);
        queueAtlas(manager, CHARGE_PATH);
        queueAtlas(manager, CHARGE_RECOVER_PATH);
        queueAtlas(manager, THROW_ATTACK_PATH);
        queueAtlas(manager, DEATH_AIR_PATH);
        queueAtlas(manager, DEATH_LAND_PATH);
        queueAtlas(manager, TURN_TO_IDLE_PATH);

        queueAtlas(manager, JAVELIN_IMPACT_PATH);
        queueAtlas(manager, JAVELIN_NEUTRAL_PATH);
        queueAtlas(manager, JAVELIN_SNAP_PATH);
        queueAtlas(manager, JAVELIN_STICK_PATH);
    }

    private static void queueAtlas(AssetManager manager, String path) {
        manager.load(path, TextureAtlas.class);
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

    public Animation<TextureRegion> getIdleAnim()           { return idleAnim; }
    public Animation<TextureRegion> getChargeAnticAnim()    { return chargeAnticAnim; }
    public Animation<TextureRegion> getChargeAnim()         { return chargeAnim; }
    public Animation<TextureRegion> getChargeRecoverAnim()  { return chargeRecoverAnim; }
    public Animation<TextureRegion> getThrowAttackAnim()    { return throwAttackAnim; }
    public Animation<TextureRegion> getDeathAirAnim()       { return deathAirAnim; }
    public Animation<TextureRegion> getDeathLandAnim()      { return deathLandAnim; }
    public Animation<TextureRegion> getTurnToIdleAnim()     { return turnToIdleAnim; }

    public Animation<TextureRegion> getJavelinImpactAnim()  { return javelinImpactAnim; }
    public Animation<TextureRegion> getJavelinNeutralAnim() { return javelinNeutralAnim; }
    public Animation<TextureRegion> getJavelinSnapAnim()    { return javelinSnapAnim; }
    public Animation<TextureRegion> getJavelinStickAnim()   { return javelinStickAnim; }
}
