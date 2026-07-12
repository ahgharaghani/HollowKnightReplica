package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * False Knight animation bank (spec: Boss Fight).
 *
 * Only the animations the fight actually needs are loaded (the asset pack
 * also ships Body/Mace/Head Spaz/Run Antic sheets that this boss design
 * does not use). Regions are pulled with findRegions(), which returns them
 * sorted by index - several of these atlases store frames out of order.
 *
 * The three "Jump Attack Hit" sheets are stitched into ONE impact
 * animation for the Power Mace Slam landing.
 */
public class FalseKnightAssets {

    private static final String BASE = "animation/enemies/false_knight/";

    // ---- Body (armor) ----
    private static final String IDLE_PATH           = BASE + "Idle.atlas";
    private static final String TURN_PATH           = BASE + "Turn.atlas";
    private static final String RAGE_PATH           = BASE + "Rage.atlas";
    private static final String RUN_PATH            = BASE + "Run.atlas";
    private static final String ATTACK_ANTIC_PATH   = BASE + "Attack Antic.atlas";
    private static final String ATTACK_PATH         = BASE + "Attack.atlas";
    private static final String ATTACK_RECOVER_PATH = BASE + "Attack Recover.atlas";
    private static final String JUMP_ANTIC_PATH     = BASE + "Jump Antic.atlas";
    private static final String JUMP_PATH           = BASE + "Jump.atlas";
    private static final String LAND_PATH           = BASE + "Land.atlas";
    private static final String JUMP_ATTACK_UP_PATH = BASE + "Jump Attack Up.atlas";
    private static final String JUMP_HIT_1_PATH     = BASE + "Jump Attack Hit 1.atlas";
    private static final String JUMP_HIT_2_PATH     = BASE + "Jump Attack Hit 2.atlas";
    private static final String JUMP_HIT_3_PATH     = BASE + "Jump Attack Hit 3.atlas";

    // ---- Stun (armor) ----
    private static final String STUN_ROLL_PATH      = BASE + "Stun Roll.atlas";
    private static final String STUN_ROLL_END_PATH  = BASE + "Stun Roll End.atlas";
    private static final String STUN_OPEN_PATH      = BASE + "Stun Open.atlas";
    private static final String STUN_OPENED_PATH    = BASE + "Stun Opened.atlas";
    private static final String STUN_HIT_PATH       = BASE + "Stun Hit.atlas";
    private static final String STUN_RECOVER_PATH   = BASE + "Stun Recover.atlas";

    // ---- Head (the maggot) - stun & death overlays (spec request) ----
    private static final String HEAD_IDLE_PATH      = BASE + "Head Idle.atlas";
    private static final String HEAD_HIT_PATH       = BASE + "Head Hit.atlas";
    private static final String DEATH_HEAD_1_PATH   = BASE + "Death Head 1.atlas";
    private static final String DEATH_HEAD_2_PATH   = BASE + "Death Head 2.atlas";

    // ---- Death (armor) ----
    private static final String DEATH_FALL_PATH     = BASE + "Death Fall.atlas";
    private static final String DEATH_LAND_PATH     = BASE + "Death Land.atlas";
    private static final String DEATH_SPAZ_PATH     = BASE + "Death Spaz.atlas";

    private final Animation<TextureRegion> idleAnim;
    private final Animation<TextureRegion> turnAnim;
    private final Animation<TextureRegion> rageAnim;
    private final Animation<TextureRegion> runAnim;
    private final Animation<TextureRegion> attackAnticAnim;
    private final Animation<TextureRegion> attackAnim;
    private final Animation<TextureRegion> attackRecoverAnim;
    private final Animation<TextureRegion> jumpAnticAnim;
    private final Animation<TextureRegion> jumpAnim;
    private final Animation<TextureRegion> landAnim;
    private final Animation<TextureRegion> powerJumpAnim;
    private final Animation<TextureRegion> powerHitAnim;
    private final Animation<TextureRegion> stunRollAnim;
    private final Animation<TextureRegion> stunRollEndAnim;
    private final Animation<TextureRegion> stunOpenAnim;
    private final Animation<TextureRegion> stunOpenedAnim;
    private final Animation<TextureRegion> stunHitAnim;
    private final Animation<TextureRegion> stunRecoverAnim;
    private final Animation<TextureRegion> headIdleAnim;
    private final Animation<TextureRegion> headHitAnim;
    private final Animation<TextureRegion> deathFallAnim;
    private final Animation<TextureRegion> deathLandAnim;
    private final Animation<TextureRegion> deathSpazAnim;
    private final Animation<TextureRegion> deathHead1Anim;
    private final Animation<TextureRegion> deathHead2Anim;

    public FalseKnightAssets(AssetManager manager) {
        idleAnim          = build(manager, IDLE_PATH,           "Idle",            8f, Animation.PlayMode.LOOP);
        turnAnim          = build(manager, TURN_PATH,           "Turn",           10f, Animation.PlayMode.NORMAL);
        rageAnim          = build(manager, RAGE_PATH,           "Rage",           10f, Animation.PlayMode.NORMAL);
        runAnim           = build(manager, RUN_PATH,            "Run",            12f, Animation.PlayMode.LOOP);
        attackAnticAnim   = build(manager, ATTACK_ANTIC_PATH,   "Attack Antic",   12f, Animation.PlayMode.NORMAL);
        attackAnim        = build(manager, ATTACK_PATH,         "Attack",         15f, Animation.PlayMode.NORMAL);
        attackRecoverAnim = build(manager, ATTACK_RECOVER_PATH, "Attack Recover", 12f, Animation.PlayMode.NORMAL);
        jumpAnticAnim     = build(manager, JUMP_ANTIC_PATH,     "Jump Antic",     12f, Animation.PlayMode.NORMAL);
        jumpAnim          = build(manager, JUMP_PATH,           "Jump",           10f, Animation.PlayMode.NORMAL);
        landAnim          = build(manager, LAND_PATH,           "Land",           15f, Animation.PlayMode.NORMAL);
        powerJumpAnim     = build(manager, JUMP_ATTACK_UP_PATH, "Jump Attack Up", 12f, Animation.PlayMode.NORMAL);
        powerHitAnim      = buildPowerHit(manager);
        stunRollAnim      = build(manager, STUN_ROLL_PATH,      "Stun Roll",      12f, Animation.PlayMode.LOOP);
        stunRollEndAnim   = build(manager, STUN_ROLL_END_PATH,  "Stun Roll End",  12f, Animation.PlayMode.NORMAL);
        stunOpenAnim      = build(manager, STUN_OPEN_PATH,      "Stun Open",      10f, Animation.PlayMode.NORMAL);
        stunOpenedAnim    = build(manager, STUN_OPENED_PATH,    "Stun Opened",     8f, Animation.PlayMode.LOOP);
        stunHitAnim       = build(manager, STUN_HIT_PATH,       "Stun Hit",       15f, Animation.PlayMode.NORMAL);
        stunRecoverAnim   = build(manager, STUN_RECOVER_PATH,   "Stun Recover",   10f, Animation.PlayMode.NORMAL);
        headIdleAnim      = build(manager, HEAD_IDLE_PATH,      "Head Idle",       8f, Animation.PlayMode.LOOP);
        headHitAnim       = build(manager, HEAD_HIT_PATH,       "Head Hit",       15f, Animation.PlayMode.NORMAL);
        deathFallAnim     = build(manager, DEATH_FALL_PATH,     "Death Fall",     10f, Animation.PlayMode.LOOP);
        deathLandAnim     = build(manager, DEATH_LAND_PATH,     "Death Land",     12f, Animation.PlayMode.NORMAL);
        deathSpazAnim     = build(manager, DEATH_SPAZ_PATH,     "Death Spaz",     10f, Animation.PlayMode.NORMAL);
        deathHead1Anim    = build(manager, DEATH_HEAD_1_PATH,   "Death Head 1",   12f, Animation.PlayMode.NORMAL);
        deathHead2Anim    = build(manager, DEATH_HEAD_2_PATH,   "Death Head 2",    8f, Animation.PlayMode.NORMAL);
    }

    public static void loadAll(AssetManager manager) {
        manager.load(IDLE_PATH,           TextureAtlas.class);
        manager.load(TURN_PATH,           TextureAtlas.class);
        manager.load(RAGE_PATH,           TextureAtlas.class);
        manager.load(RUN_PATH,            TextureAtlas.class);
        manager.load(ATTACK_ANTIC_PATH,   TextureAtlas.class);
        manager.load(ATTACK_PATH,         TextureAtlas.class);
        manager.load(ATTACK_RECOVER_PATH, TextureAtlas.class);
        manager.load(JUMP_ANTIC_PATH,     TextureAtlas.class);
        manager.load(JUMP_PATH,           TextureAtlas.class);
        manager.load(LAND_PATH,           TextureAtlas.class);
        manager.load(JUMP_ATTACK_UP_PATH, TextureAtlas.class);
        manager.load(JUMP_HIT_1_PATH,     TextureAtlas.class);
        manager.load(JUMP_HIT_2_PATH,     TextureAtlas.class);
        manager.load(JUMP_HIT_3_PATH,     TextureAtlas.class);
        manager.load(STUN_ROLL_PATH,      TextureAtlas.class);
        manager.load(STUN_ROLL_END_PATH,  TextureAtlas.class);
        manager.load(STUN_OPEN_PATH,      TextureAtlas.class);
        manager.load(STUN_OPENED_PATH,    TextureAtlas.class);
        manager.load(STUN_HIT_PATH,       TextureAtlas.class);
        manager.load(STUN_RECOVER_PATH,   TextureAtlas.class);
        manager.load(HEAD_IDLE_PATH,      TextureAtlas.class);
        manager.load(HEAD_HIT_PATH,       TextureAtlas.class);
        manager.load(DEATH_FALL_PATH,     TextureAtlas.class);
        manager.load(DEATH_LAND_PATH,     TextureAtlas.class);
        manager.load(DEATH_SPAZ_PATH,     TextureAtlas.class);
        manager.load(DEATH_HEAD_1_PATH,   TextureAtlas.class);
        manager.load(DEATH_HEAD_2_PATH,   TextureAtlas.class);
    }

    private static Animation<TextureRegion> build(AssetManager manager,
                                                  String path, String regionName,
                                                  float fps,
                                                  Animation.PlayMode playMode) {
        TextureAtlas atlas = manager.get(path, TextureAtlas.class);
        // findRegions returns frames ordered by index - several of these
        // atlases store their pages/frames out of order on disk.
        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(regionName);
        if (frames.size == 0) {
            throw new IllegalStateException(
                "Atlas '" + path + "' has no regions named '" + regionName + "'");
        }
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return new Animation<>(1f / fps, frames, playMode);
    }

    /** Stitch Jump Attack Hit 1+2+3 into one 6-frame impact animation. */
    private static Animation<TextureRegion> buildPowerHit(AssetManager manager) {
        Array<TextureRegion> frames = new Array<>(6);
        String[][] parts = {
            { JUMP_HIT_1_PATH, "Jump Attack Hit 1" },
            { JUMP_HIT_2_PATH, "Jump Attack Hit 2" },
            { JUMP_HIT_3_PATH, "Jump Attack Hit 3" },
        };
        for (String[] part : parts) {
            TextureAtlas atlas = manager.get(part[0], TextureAtlas.class);
            for (Texture t : atlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
            Array<TextureAtlas.AtlasRegion> found = atlas.findRegions(part[1]);
            for (int i = 0; i < found.size; i++) frames.add(found.get(i));
        }
        if (frames.size == 0) {
            throw new IllegalStateException("Jump Attack Hit atlases are empty");
        }
        return new Animation<>(1f / 15f, frames, Animation.PlayMode.NORMAL);
    }

    public Animation<TextureRegion> getIdleAnim()          { return idleAnim; }
    public Animation<TextureRegion> getTurnAnim()          { return turnAnim; }
    public Animation<TextureRegion> getRageAnim()          { return rageAnim; }
    public Animation<TextureRegion> getRunAnim()           { return runAnim; }
    public Animation<TextureRegion> getAttackAnticAnim()   { return attackAnticAnim; }
    public Animation<TextureRegion> getAttackAnim()        { return attackAnim; }
    public Animation<TextureRegion> getAttackRecoverAnim() { return attackRecoverAnim; }
    public Animation<TextureRegion> getJumpAnticAnim()     { return jumpAnticAnim; }
    public Animation<TextureRegion> getJumpAnim()          { return jumpAnim; }
    public Animation<TextureRegion> getLandAnim()          { return landAnim; }
    public Animation<TextureRegion> getPowerJumpAnim()     { return powerJumpAnim; }
    public Animation<TextureRegion> getPowerHitAnim()      { return powerHitAnim; }
    public Animation<TextureRegion> getStunRollAnim()      { return stunRollAnim; }
    public Animation<TextureRegion> getStunRollEndAnim()   { return stunRollEndAnim; }
    public Animation<TextureRegion> getStunOpenAnim()      { return stunOpenAnim; }
    public Animation<TextureRegion> getStunOpenedAnim()    { return stunOpenedAnim; }
    public Animation<TextureRegion> getStunHitAnim()       { return stunHitAnim; }
    public Animation<TextureRegion> getStunRecoverAnim()   { return stunRecoverAnim; }
    public Animation<TextureRegion> getHeadIdleAnim()      { return headIdleAnim; }
    public Animation<TextureRegion> getHeadHitAnim()       { return headHitAnim; }
    public Animation<TextureRegion> getDeathFallAnim()     { return deathFallAnim; }
    public Animation<TextureRegion> getDeathLandAnim()     { return deathLandAnim; }
    public Animation<TextureRegion> getDeathSpazAnim()     { return deathSpazAnim; }
    public Animation<TextureRegion> getDeathHead1Anim()    { return deathHead1Anim; }
    public Animation<TextureRegion> getDeathHead2Anim()    { return deathHead2Anim; }
}
