package com.sut.hollowknight.model;

/**
 * Tracks which save slot the current play-session belongs to.
 *
 * <p>StartGameController begins a session when a game is started or loaded;
 * the Pause menu's "Save and Quit" flushes the live Knight state back into
 * this snapshot and persists it. Play time is accumulated here with a
 * float-second accumulator so we only touch the int field once per second
 * (no per-frame boxing or DB traffic).</p>
 */
public final class GameSession {

    private static GameData active;
    private static float secondsAccumulator = 0f;

    private GameSession() { }

    /** Start tracking a session for the given slot snapshot. */
    public static void begin(GameData data) {
        active = data;
        secondsAccumulator = 0f;
    }

    public static GameData getActive() { return active; }
    public static boolean isActive()   { return active != null; }

    /** Stop tracking (used when quitting back to the main menu). */
    public static void end() {
        active = null;
        secondsAccumulator = 0f;
    }

    /** Accumulate play time; whole seconds are folded into the snapshot. */
    public static void addPlayTime(float delta) {
        if (active == null) return;
        secondsAccumulator += delta;
        int whole = (int) secondsAccumulator;
        if (whole > 0) {
            active.playTimeSeconds += whole;
            secondsAccumulator -= whole;
        }
    }
}
