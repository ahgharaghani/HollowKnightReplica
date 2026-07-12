package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.GameSettings;

/**
 * Central sound-effect bank (spec: Audio SFX).
 *
 * <p>Architecture: one static, app-lifetime registry, loaded once on the
 * first GameScreen construction. Sounds are loaded directly with
 * Gdx.audio.newSound (the same pattern AchievementToastOverlay already
 * uses) instead of the AssetManager queue - .wav decoding is cheap, this
 * keeps the bank self-contained, and a missing file degrades to silence
 * instead of crashing the load screen.</p>
 *
 * <p>Every play() call reads volume/mute from GameSettings at call time,
 * so the Settings screen's SFX slider and mute checkbox apply instantly
 * without any listener plumbing. Variant groups (nail swings, soul
 * pickups...) play a random member to avoid machine-gun repetition -
 * exactly how the original game keeps its foley alive.</p>
 *
 * <p>Perf: all Sound objects are resolved at init; play()/loop() do no
 * allocation and no map lookups - fields are read directly.</p>
 */
public final class Sfx {

    private static final String TAG = "Sfx";
    private static final String DIR = "sfx/";

    private static boolean initialised;

    // ---- Hero ----
    public static Sound[] sword;          // nail slash variants (spec: Nail Slash Effect)
    public static Sound heroDamage;       // player hurt (spec: Damage SFX)
    public static Sound heroDeath;
    public static Sound heroDash;
    public static Sound heroScream;       // Howling Wraiths cast
    public static Sound footsteps;        // run loop
    public static Sound heartbeat;        // low-HP warning loop

    // ---- Soul & Focus ----
    public static Sound[] soulPickup;     // soul gain variants (spec: Soul Gain)
    public static Sound focusCharging;    // held-focus loop (spec: Focus)
    public static Sound focusReady;       // charge complete
    public static Sound focusHeal;        // mask restored

    // ---- Enemies & world ----
    public static Sound enemyDamage;      // generic enemy hurt (spec: Damage SFX)
    public static Sound[] wallHit;        // breakable wall clinks
    public static Sound wallBreak;        // stone collapse (spec bonus: Stone Breaking SFX)

    // ---- False Knight ----
    public static Sound[] fkVoice;        // attack grunts
    public static Sound fkJump;
    public static Sound fkLand;
    public static Sound fkLandFirst;      // very first arena landing
    public static Sound fkSwing;
    public static Sound fkStrikeGround;
    public static Sound fkRoll;           // charge run
    public static Sound fkArmour;         // nail clank on the armor
    public static Sound fkArmourFinal;    // armor bursts open (stun)
    public static Sound fkHeadDamage;     // maggot head hit during stun
    public static Sound bossFinalHit;
    public static Sound bossExplode;
    public static Sound bossDefeat;       // victory sting (end-of-fight)

    private Sfx() { }

    /** Idempotent; call once before gameplay (GameScreen constructor). */
    public static void init() {
        if (initialised) return;
        initialised = true;

        sword = group("sword_1.wav", "sword_2.wav", "sword_3.wav",
                      "sword_4.wav", "sword_5.wav");
        heroDamage  = load("hero_damage.wav");
        heroDeath   = load("hero_death_v2.wav");
        heroDash    = load("hero_dash.wav");
        heroScream  = load("hero_scream_spell.wav");
        footsteps   = load("hero_run_footsteps_stone.wav");
        heartbeat   = load("heartbeat_B_01.wav");

        soulPickup = group("soul_pickup_1.wav", "soul_pickup_2.wav",
                           "soul_pickup_3.wav", "soul_pickup_4.wav",
                           "soul_pickup_5.wav", "soul_pickup_6.wav",
                           "soul_pickup_7.wav");
        focusCharging = load("focus_health_charging.wav");
        focusReady    = load("focus_ready.wav");
        focusHeal     = load("focus_health_heal.wav");

        enemyDamage = load("enemy_damage.wav");
        wallHit     = group("breakable_wall_hit_1.wav", "breakable_wall_hit_2.wav");
        wallBreak   = load("breakable_wall_death.wav");

        fkVoice = group("False_Knight_Attack_New_01.wav",
                        "False_Knight_Attack_New_02.wav",
                        "False_Knight_Attack_New_03.wav",
                        "False_Knight_Attack_New_04.wav",
                        "False_Knight_Attack_New_05.wav");
        fkJump         = load("false_knight_jump.wav");
        fkLand         = load("false_knight_land.wav");
        fkLandFirst    = load("false_knight_land_1st_time.wav");
        fkSwing        = load("false_knight_swing.wav");
        fkStrikeGround = load("false_knight_strike_ground.wav");
        fkRoll         = load("false_knight_roll.wav");
        fkArmour       = load("false_knight_damage_armour.wav");
        fkArmourFinal  = load("false_knight_damage_armour_final.wav");
        fkHeadDamage   = load("false_knight_head_damage_2.wav");
        bossFinalHit   = load("boss_final_hit.wav");
        bossExplode    = load("boss_explode.wav");
        bossDefeat     = load("Boss Defeat.wav");

        Gdx.app.log(TAG, "Sound bank initialised");
    }

    // ---- Playback ----

    /** Fire-and-forget one-shot at the current SFX volume. Null-safe. */
    public static void play(Sound s) {
        play(s, 1f);
    }

    /**
     * One-shot with a per-sound gain multiplier on top of the user's SFX
     * volume. Use this to mix individual effects (e.g. quiet soul dings)
     * without touching the global slider.
     */
    public static void play(Sound s, float gain) {
        if (s == null) return;
        GameSettings settings = GameSettings.getInstance();
        if (settings.isSfxMuted()) return;
        s.play(settings.getSfxVolume() * gain);
    }

    /** One random member of a variant group (nail swings, soul motes...). */
    public static void playRandom(Sound[] variants) {
        playRandom(variants, 1f);
    }

    /** Random variant with a per-sound gain multiplier. */
    public static void playRandom(Sound[] variants, float gain) {
        if (variants == null || variants.length == 0) return;
        play(variants[MathUtils.random(variants.length - 1)], gain);
    }

    /**
     * Starts a looping sound and returns its handle (-1 when muted or
     * missing). Callers own the handle and MUST stop it via stopLoop().
     */
    public static long loop(Sound s) {
        return loop(s, 1f);
    }

    /** Loop with a per-sound gain multiplier (e.g. quieter footsteps). */
    public static long loop(Sound s, float gain) {
        if (s == null) return -1L;
        GameSettings settings = GameSettings.getInstance();
        if (settings.isSfxMuted()) return -1L;
        return s.loop(settings.getSfxVolume() * gain);
    }

    /**
     * Stops EVERY playing instance of a sound. Used as the orphan-killer
     * for loops when a screen goes away: handle-based stops can miss an
     * instance restarted later in the same frame, a global stop cannot.
     */
    public static void stopAll(Sound s) {
        if (s == null) return;
        s.stop();
    }

    /** Stops one looping instance. Safe to call with a -1 handle. */
    public static void stopLoop(Sound s, long handle) {
        if (s == null || handle == -1L) return;
        s.stop(handle);
    }

    // ---- Loading ----

    private static Sound load(String fileName) {
        FileHandle fh = Gdx.files.internal(DIR + fileName);
        if (!fh.exists()) {
            // Missing audio must never take the game down - log and stay silent.
            Gdx.app.error(TAG, "Missing sound file: " + fh.path());
            return null;
        }
        return Gdx.audio.newSound(fh);
    }

    private static Sound[] group(String... fileNames) {
        Sound[] out = new Sound[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            out[i] = load(fileNames[i]);
        }
        return out;
    }
}
