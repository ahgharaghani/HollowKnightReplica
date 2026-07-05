package com.sut.hollowknight.view.animator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.view.assets.Assets;

import java.util.Comparator;

public class KnightAnimator {

    // ---- Body animations ----
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> idleLowHealthAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> fallAnim;
    private Animation<TextureRegion> landAnim;
    private Animation<TextureRegion> recoilAnim;
    private Animation<TextureRegion> deathAnim;
    private Animation<TextureRegion> doubleJumpAnim;
    private Animation<TextureRegion> focusAnim;
    private Animation<TextureRegion> focusEndAnim;

    // ---- Slash family ----
    private Animation<TextureRegion> slashAnim;
    private Animation<TextureRegion> slashAltAnim;
    private Animation<TextureRegion> downSlashAnim;
    private Animation<TextureRegion> upSlashAnim;
    private Animation<TextureRegion> wallSlashAnim;

    // ---- Dash family ----
    private Animation<TextureRegion> dashAnim;
    private Animation<TextureRegion> dashDownAnim;
    private Animation<TextureRegion> dashDownLandAnim;

    // ---- Wall family ----
    private Animation<TextureRegion> wallSlideAnim;
    private Animation<TextureRegion> wallJumpAnim;

    // ---- Effect overlays ----
    private Animation<TextureRegion> slashEffectAnim;
    private Animation<TextureRegion> slashAltEffectAnim;
    private Animation<TextureRegion> downSlashEffectAnim;
    private Animation<TextureRegion> upSlashEffectAnim;
    private Animation<TextureRegion> dashEffectAnim;

    public KnightAnimator() {
        loadAnimations();
    }

    private void loadAnimations() {
        // Idle

        TextureAtlas idleAtlas = Assets.manager.get(
            "animation/knight/KnightIdle.atlas", TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> idleFrames = idleAtlas.getRegions();
        if (idleFrames.size == 0) {
            throw new IllegalStateException(
                "KnightIdle.atlas loaded but contains 0 regions. Check atlas format.");
        }
        for (Texture t : idleAtlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        idleAnim = new Animation<>(
            KnightAnimConfig.frameDuration(KnightAnimConfig.IDLE_FPS),
            idleFrames, Animation.PlayMode.LOOP);


        // Idle Low Health

        if (Assets.manager.isLoaded("animation/knight/Idle Low Health.atlas")) {
            idleLowHealthAnim = loadAtlas(
                "animation/knight/Idle Low Health.atlas",
                "Idle Hurt",
                KnightAnimConfig.IDLE_LOW_HEALTH_FPS,
                Animation.PlayMode.LOOP);
        }


        // Walk (legacy KnightWalk)

        if (Assets.manager.isLoaded("animation/knight/KnightWalk.atlas")) {
            TextureAtlas walkAtlas = Assets.manager.get(
                "animation/knight/KnightWalk.atlas", TextureAtlas.class);
            Array<TextureAtlas.AtlasRegion> walkFrames = walkAtlas.findRegions("KnightWalk");
            if (walkFrames.size == 0) walkFrames = walkAtlas.getRegions();
            walkAnim = new Animation<>(
                KnightAnimConfig.frameDuration(KnightAnimConfig.WALK_FPS),
                walkFrames, Animation.PlayMode.LOOP);
        }

        // Airborne (Jump & Fall)

        if (Assets.manager.isLoaded("animation/knight/Airborne.atlas")) {
            TextureAtlas airborneAtlas = Assets.manager.get(
                "animation/knight/Airborne.atlas", TextureAtlas.class);
            for (Texture t : airborneAtlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            Array<TextureAtlas.AtlasRegion> jumpFrames = new Array<>();
            Array<TextureAtlas.AtlasRegion> fallFrames = new Array<>();

            for (TextureAtlas.AtlasRegion region : airborneAtlas.getRegions()) {
                if (region.name.startsWith("Airborne_jump")) {
                    jumpFrames.add(region);
                } else if (region.name.startsWith("Airborne_fall")) {
                    fallFrames.add(region);
                }
            }

            Comparator<TextureAtlas.AtlasRegion> indexComparator =
                Comparator.comparingInt(r -> r.index);
            jumpFrames.sort(indexComparator);
            fallFrames.sort(indexComparator);

            if (jumpFrames.size > 0) {
                jumpAnim = new Animation<>(
                    KnightAnimConfig.frameDuration(KnightAnimConfig.JUMP_FPS),
                    jumpFrames, Animation.PlayMode.NORMAL);
            }
            if (fallFrames.size > 0) {
                fallAnim = new Animation<>(
                    KnightAnimConfig.frameDuration(KnightAnimConfig.FALL_FPS),
                    fallFrames, Animation.PlayMode.LOOP);
            }
        }


        // Land (legacy)

        if (Assets.manager.isLoaded("animation/knight/Land.atlas")) {
            TextureAtlas landAtlas = Assets.manager.get(
                "animation/knight/Land.atlas", TextureAtlas.class);
            for (Texture t : landAtlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
            Array<TextureAtlas.AtlasRegion> landFrames = landAtlas.findRegions("Land");
            if (landFrames.size == 0) {
                landFrames = landAtlas.getRegions();
                landFrames.sort(Comparator.comparingInt(r -> r.index));
            }
            if (landFrames.size > 0) {
                landAnim = new Animation<>(
                    KnightAnimConfig.frameDuration(KnightAnimConfig.LAND_FPS),
                    landFrames, Animation.PlayMode.NORMAL);
            }
        }

        // slash family

        slashAnim        = loadAtlas("animation/knight/Slash.atlas",        "Slash",
            KnightAnimConfig.SLASH_FPS,        Animation.PlayMode.NORMAL);
        slashAltAnim     = loadAtlas("animation/knight/Slash Alt.atlas",    "SlashAlt",
            KnightAnimConfig.SLASH_ALT_FPS,    Animation.PlayMode.NORMAL);
        downSlashAnim    = loadAtlas("animation/knight/Down Slash.atlas",   "DownSlash",
            KnightAnimConfig.DOWN_SLASH_FPS,   Animation.PlayMode.NORMAL);
        upSlashAnim      = loadAtlas("animation/knight/Up Slash.atlas",     "UpSlash",
            KnightAnimConfig.UP_SLASH_FPS,     Animation.PlayMode.NORMAL);
        wallSlashAnim    = loadAtlas("animation/knight/Wall Slash.atlas",   "Wall Slash",
            KnightAnimConfig.WALL_SLASH_FPS,   Animation.PlayMode.NORMAL);

        slashEffectAnim    = loadAtlas("animation/knight/Slash Effect.atlas",     "SlashEffect",
            KnightAnimConfig.SLASH_EFFECT_FPS,     Animation.PlayMode.NORMAL);
        slashAltEffectAnim = loadAtlas("animation/knight/Slash Effect Alt.atlas", "SlashEffectAlt",
            KnightAnimConfig.SLASH_ALT_EFFECT_FPS, Animation.PlayMode.NORMAL);
        downSlashEffectAnim= loadAtlas("animation/knight/Down Slash Effect.atlas","DownSlashEffect",
            KnightAnimConfig.DOWN_SLASH_EFFECT_FPS,Animation.PlayMode.NORMAL);
        upSlashEffectAnim  = loadAtlas("animation/knight/Up Slash Effect.atlas",  "UpSlashEffect",
            KnightAnimConfig.UP_SLASH_EFFECT_FPS,  Animation.PlayMode.NORMAL);

        // dash family

        dashAnim         = loadAtlas("animation/knight/Dash.atlas",           "Dash",
            KnightAnimConfig.DASH_FPS,         Animation.PlayMode.NORMAL);
        dashDownAnim     = loadAtlas("animation/knight/Dash Down.atlas",      "Dash Down",
            KnightAnimConfig.DASH_DOWN_FPS,    Animation.PlayMode.NORMAL);
        dashDownLandAnim = loadAtlas("animation/knight/Dash Down Land.atlas", "Dash Down Land",
            KnightAnimConfig.DASH_DOWN_LAND_FPS, Animation.PlayMode.NORMAL);
        dashEffectAnim   = loadAtlas("animation/knight/Dash Effect.atlas",    "Dash Effect",
            KnightAnimConfig.DASH_EFFECT_FPS,  Animation.PlayMode.NORMAL);

        // wall family

        wallSlideAnim    = loadAtlas("animation/knight/Wall Slide.atlas", "Wall Slide",
            KnightAnimConfig.WALL_SLIDE_FPS, Animation.PlayMode.LOOP);
        wallJumpAnim     = loadAtlas("animation/knight/Wall Jump.atlas",  "Walljump",
            KnightAnimConfig.WALL_JUMP_FPS,  Animation.PlayMode.NORMAL);

        // one-shots
        recoilAnim       = loadAtlas("animation/knight/Recoil.atlas",       "Recoil",
            KnightAnimConfig.RECOIL_FPS,      Animation.PlayMode.NORMAL);
        deathAnim        = loadAtlas("animation/knight/Death.atlas",        "Death",
            KnightAnimConfig.DEATH_FPS,       Animation.PlayMode.NORMAL);
        doubleJumpAnim   = loadAtlas("animation/knight/Double Jump.atlas",  "Double Jump",
            KnightAnimConfig.DOUBLE_JUMP_FPS, Animation.PlayMode.NORMAL);
        focusAnim        = loadAtlas("animation/knight/Focus.atlas",        "Focus_focus",
            KnightAnimConfig.FOCUS_FPS,       Animation.PlayMode.LOOP);
        focusEndAnim     = loadAtlas("animation/knight/Focus End.atlas",    "Focus End_focus",
            KnightAnimConfig.FOCUS_END_FPS,   Animation.PlayMode.NORMAL);
    }

    private Animation<TextureRegion> loadAtlas(String path, String regionPrefix,
                                               float fps, Animation.PlayMode mode) {
        if (!Assets.manager.isLoaded(path)) return null;

        TextureAtlas atlas = Assets.manager.get(path, TextureAtlas.class);
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions(regionPrefix);
        if (frames.size == 0) {
            frames = atlas.getRegions();
            frames.sort(Comparator.comparingInt(r -> r.index));
        }
        if (frames.size == 0) return null;

        return new Animation<>(KnightAnimConfig.frameDuration(fps), frames, mode);
    }

    //  Body frame

    /** Focus: frames 0..1 charge once, then frames 2..last loop while holding. */
    private static final int FOCUS_CHARGE_FRAMES = 2;

    public TextureRegion getCurrentFrame(Knight knight) {
        Animation<TextureRegion> anim = animationFor(knight);
        if (anim == null) {
            // Fall back to idle if the requested anim couldn't be loaded.
            anim = idleAnim;
        }

        float t = knight.getStateTime();

        if (knight.getState() == Knight.State.FOCUS && anim == focusAnim) {
            return focusFrame(anim, t);
        }

        if (knight.isDead()) {
            // Death plays once and freezes on the last frame.
            return anim.getKeyFrame(t, false);
        }
        return anim.getKeyFrame(t, anim.getPlayMode() == Animation.PlayMode.LOOP);
    }

    private TextureRegion focusFrame(Animation<TextureRegion> anim, float t) {
        float fd = anim.getFrameDuration();
        int total = Math.round(anim.getAnimationDuration() / fd);
        if (total <= FOCUS_CHARGE_FRAMES) {
            return anim.getKeyFrame(t, true);
        }

        float chargeTime = FOCUS_CHARGE_FRAMES * fd;
        if (t < chargeTime) {
            return anim.getKeyFrame(t, false);
        }
        // Map hold time back into the tail window, then sample as a one-shot so
        // the frame index lands in [FOCUS_CHARGE_FRAMES, total-1].
        int loopLen = total - FOCUS_CHARGE_FRAMES;
        float holdTime = chargeTime + ((t - chargeTime) % (loopLen * fd));
        return anim.getKeyFrame(holdTime, false);
    }

    private Animation<TextureRegion> animationFor(Knight knight) {
        switch (knight.getState()) {
            case RUN:               return walkAnim != null ? walkAnim : idleAnim;
            case JUMP:              return jumpAnim != null ? jumpAnim : idleAnim;
            case FALL:              return fallAnim != null ? fallAnim : idleAnim;
            case LAND:              return landAnim != null ? landAnim : idleAnim;

            // Hurt / knockback family — the recoil atlas IS the hurt pose.
            case HURT:
            case RECOIL:            return recoilAnim != null ? recoilAnim : idleAnim;

            case IDLE_LOW_HEALTH:   return idleLowHealthAnim != null ? idleLowHealthAnim : idleAnim;

            case DEATH:             return deathAnim != null ? deathAnim : idleAnim;
            case DOUBLE_JUMP:       return doubleJumpAnim != null ? doubleJumpAnim : jumpAnim;
            case FOCUS:             return focusAnim != null ? focusAnim : idleAnim;
            case FOCUS_END:         return focusEndAnim != null ? focusEndAnim : idleAnim;

            // Slash family
            case SLASH:             return slashAnim != null ? slashAnim : idleAnim;
            case SLASH_ALT:         return slashAltAnim != null ? slashAltAnim : slashAnim;
            case DOWN_SLASH:        return downSlashAnim != null ? downSlashAnim : slashAnim;
            case UP_SLASH:          return upSlashAnim != null ? upSlashAnim : slashAnim;
            case WALL_SLASH:        return wallSlashAnim != null ? wallSlashAnim : slashAnim;

            // Dash family
            case DASH:              return dashAnim != null ? dashAnim : idleAnim;
            case DASH_DOWN:         return dashDownAnim != null ? dashDownAnim : idleAnim;
            case DASH_DOWN_LAND:    return dashDownLandAnim != null ? dashDownLandAnim : landAnim;

            // Wall family
            case WALL_SLIDE:        return wallSlideAnim != null ? wallSlideAnim : fallAnim;
            case WALL_JUMP:         return wallJumpAnim != null ? wallJumpAnim : jumpAnim;

            case IDLE:
            default: {
                // Swap to the low-health idle automatically when HP is critical.
                if (idleLowHealthAnim != null
                    && knight.getHpMasks() <= Knight.LOW_HEALTH_THRESHOLD
                    && knight.getHpMasks() > 0) {
                    return idleLowHealthAnim;
                }
                return idleAnim;
            }
        }
    }

    //  Effect overlays — drawn as a second pass on top of the body

    /**
     * Returns the active slash/dash effect frame for the knight, or null when
     * no effect should be drawn.
     */
    public TextureRegion getCurrentEffectFrame(Knight knight) {
        switch (knight.getState()) {
            case SLASH:        return frameOf(slashEffectAnim,    knight);
            case SLASH_ALT:    return frameOf(slashAltEffectAnim, knight);
            case DOWN_SLASH:   return frameOf(downSlashEffectAnim,knight);
            case UP_SLASH:     return frameOf(upSlashEffectAnim,  knight);
            case DASH:         return frameOf(dashEffectAnim,     knight);
            default:           return null;
        }
    }

    private TextureRegion frameOf(Animation<TextureRegion> anim, Knight knight) {
        if (anim == null) return null;
        return anim.getKeyFrame(knight.getStateTime(), false);
    }


    //  Public accessors (used by the renderer when it needs a specific frame
    //  for box computations, e.g. CombatSystem)

    public Animation<TextureRegion> getIdleAnim()            { return idleAnim; }
    public Animation<TextureRegion> getIdleLowHealthAnim()   { return idleLowHealthAnim; }
    public Animation<TextureRegion> getWalkAnim()            { return walkAnim; }
    public Animation<TextureRegion> getJumpAnim()            { return jumpAnim; }
    public Animation<TextureRegion> getFallAnim()            { return fallAnim; }
    public Animation<TextureRegion> getLandAnim()            { return landAnim; }
    public Animation<TextureRegion> getRecoilAnim()          { return recoilAnim; }
    public Animation<TextureRegion> getDeathAnim()           { return deathAnim; }
    public Animation<TextureRegion> getDoubleJumpAnim()      { return doubleJumpAnim; }
    public Animation<TextureRegion> getFocusAnim()           { return focusAnim; }
    public Animation<TextureRegion> getFocusEndAnim()        { return focusEndAnim; }
    public Animation<TextureRegion> getSlashAnim()           { return slashAnim; }
    public Animation<TextureRegion> getSlashAltAnim()        { return slashAltAnim; }
    public Animation<TextureRegion> getDownSlashAnim()       { return downSlashAnim; }
    public Animation<TextureRegion> getUpSlashAnim()         { return upSlashAnim; }
    public Animation<TextureRegion> getWallSlashAnim()       { return wallSlashAnim; }
    public Animation<TextureRegion> getDashAnim()            { return dashAnim; }
    public Animation<TextureRegion> getDashDownAnim()        { return dashDownAnim; }
    public Animation<TextureRegion> getDashDownLandAnim()    { return dashDownLandAnim; }
    public Animation<TextureRegion> getWallSlideAnim()       { return wallSlideAnim; }
    public Animation<TextureRegion> getWallJumpAnim()        { return wallJumpAnim; }
}
