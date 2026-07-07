package com.sut.hollowknight.view.animator;

public final class KnightAnimConfig {

    /** Idle breathing loop, in FPS. */
    public static float IDLE_FPS         = 10f;
    /** Walk / run loop, in FPS. */
    public static float WALK_FPS         = 12f;
    /** Takeoff frame sequence, in FPS. */
    public static float JUMP_FPS         = 12f;
    /** Falling loop, in FPS. */
    public static float FALL_FPS         = 12f;
    /** Landing squash, in FPS. */
    public static float LAND_FPS         = 12f;

    /** Wall slide loop (player sliding down a wall). */
    public static float WALL_SLIDE_FPS        = 12f;
    /** Wall-slide slash — the nail swing while clinging to a wall. */
    public static float WALL_SLASH_FPS        = 20f;
    /** Wall jump — the push-off frames played when jumping off a wall. */
    public static float WALL_JUMP_FPS         = 24f;

    /** Standard nail slash (first swing of a combo). */
    public static float SLASH_FPS             = 20f;
    /** Slash visual effect overlay (the white arc). */
    public static float SLASH_EFFECT_FPS      = 20f;
    /** Alternate slash — every second swing of a consecutive combo. */
    public static float SLASH_ALT_FPS         = 20f;
    /** Alternate slash effect overlay. */
    public static float SLASH_ALT_EFFECT_FPS  = 20f;

    /** Ground dash. */
    public static float DASH_FPS              = 25f;
    /** Dash after-image / dust effect. */
    public static float DASH_EFFECT_FPS       = 20f;
    /** Downward dash (mid-air only). */
    public static float DASH_DOWN_FPS         = 20f;
    /** Landing animation after a downward dash hits the ground. */
    public static float DASH_DOWN_LAND_FPS    = 20f;

    /** Death sequence — plays once, then the knight stays on the last frame. */
    public static float DEATH_FPS             = 20f;
    /** Double-jump flip in mid-air. */
    public static float DOUBLE_JUMP_FPS       = 20f;
    /** Downward nail slash (pogo setup). */
    public static float DOWN_SLASH_FPS        = 20f;
    /** Downward slash effect overlay. */
    public static float DOWN_SLASH_EFFECT_FPS = 40f;

    /** Idle pose played when the knight is on its last mask of health. */
    public static float IDLE_LOW_HEALTH_FPS   = 20f;
    /** Knockback / hurt recoil pose. */
    public static float RECOIL_FPS            = 20f;
    /** Upward nail slash. */
    public static float UP_SLASH_FPS          = 20f;
    /** Upward slash effect overlay. */
    public static float UP_SLASH_EFFECT_FPS   = 40f;
    /** Focus channel animation. */
    public static float FOCUS_FPS             = 12f;
    /** Focus completion animation. */
    public static float FOCUS_END_FPS         = 12f;

    /** Spell cast wind-up (Fireball Antic). */
    public static float CAST_ANTIC_FPS        = 24f;
    /** Spell cast release & follow-through (Fireball Cast). */
    public static float CAST_FPS              = 24f;

    /** Scream wind-up (Scream Start). */
    public static float SCREAM_START_FPS      = 24f;
    /** Scream hold loop while the wraiths blast. */
    public static float SCREAM_FPS            = 24f;
    /** Scream recovery pose (Scream End). */
    public static float SCREAM_END_FPS        = 24f;

    public static float frameDuration(float fps) {
        if (fps <= 0f) {
            throw new IllegalArgumentException("FPS must be positive: " + fps);
        }
        return 1f / fps;
    }
}
