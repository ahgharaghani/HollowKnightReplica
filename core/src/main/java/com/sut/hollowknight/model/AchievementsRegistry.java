package com.sut.hollowknight.model;

import com.sut.hollowknight.model.enums.UiText;

import com.sut.hollowknight.model.db.GameDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Global achievement state (spec: Achievements).
 *
 * <p>Game logic calls one of the on*() hooks (or {@link #unlock}) the
 * moment it decides a condition is met. Unlocks are idempotent, write
 * through to the active save session, and notify the registered
 * listener so the view can pop the Steam-style toast.</p>
 */
public final class AchievementsRegistry {

    public static final String COMPLETION  = "completion";
    public static final String SPEEDRUN    = "speedrun";
    public static final String TRUE_HUNTER = "true_hunter";
    public static final String FALSEHOOD   = "falsehood";
    public static final String CHARMED     = "charmed";

    /** Speedrun: finish the game in under 10 minutes. */
    public static final int SPEEDRUN_LIMIT_SECONDS = 10 * 60;

    /** Implemented by the view layer to show the unlock toast. */
    public interface UnlockListener {
        void onAchievementUnlocked(Achievement achievement);
    }

    private static AchievementsRegistry instance;
    private static UnlockListener listener;

    private final List<Achievement> achievements = new ArrayList<>();

    private AchievementsRegistry() {
        achievements.add(new Achievement(COMPLETION,
            UiText.ACH_COMPLETION_TITLE, UiText.ACH_COMPLETION_DESC,
            "ui/achievement/achievement__0000_100_complete.png"));
        achievements.add(new Achievement(SPEEDRUN,
            UiText.ACH_SPEEDRUN_TITLE, UiText.ACH_SPEEDRUN_DESC,
            "ui/achievement/achievement_ultra_fast_finish.png"));
        achievements.add(new Achievement(TRUE_HUNTER,
            UiText.ACH_TRUE_HUNTER_TITLE, UiText.ACH_TRUE_HUNTER_DESC,
            "ui/achievement/achievement_Hunter_Marks.png"));
        achievements.add(new Achievement(FALSEHOOD,
            UiText.ACH_FALSEHOOD_TITLE, UiText.ACH_FALSEHOOD_DESC,
            "ui/achievement/achievement__0031_false_knight_dream.png"));
        achievements.add(new Achievement(CHARMED,
            UiText.ACH_CHARMED_TITLE, UiText.ACH_CHARMED_DESC,
            "ui/achievement/achievement__0033_charm_01.png"));
    }

    public static synchronized AchievementsRegistry getInstance() {
        if (instance == null) instance = new AchievementsRegistry();
        return instance;
    }

    /** The view registers here; pass null to unregister. */
    public static void setUnlockListener(UnlockListener l) { listener = l; }

    /** Current listener - GameScreen.dispose() checks it before clearing,
     *  so a room transition's fresh listener survives the old screen. */
    public static UnlockListener getUnlockListener() { return listener; }

    // ---- Hooks for game logic (spec: expose unlock functions) ----

    /** Call when the game is finished; also grades the speedrun. */
    public void onGameFinished(int playTimeSeconds) {
        unlock(COMPLETION);
        if (playTimeSeconds < SPEEDRUN_LIMIT_SECONDS) unlock(SPEEDRUN);
    }

    /** Call when the False Knight boss dies. */
    public void onFalseKnightDefeated() { unlock(FALSEHOOD); }

    /** Call when every enemy in the level is dead. */
    public void onAllEnemiesKilled() { unlock(TRUE_HUNTER); }

    /** Call when the Knight acquires the Void Heart charm. */
    public void onVoidHeartAcquired() { unlock(CHARMED); }

    /**
     * Unlocks by id. Idempotent: returns true only on the first call,
     * which also persists to the session and notifies the toast.
     */
    public boolean unlock(String id) {
        Achievement a = find(id);
        if (a == null || a.isUnlocked()) return false;
        a.unlock();
        // Write-through: session list AND database, immediately. Persisting
        // here (not only on "Save and Quit") means an unlock can never be
        // lost to a crash or a plain window close (spec: save achievements).
        if (GameSession.isActive()) {
            if (!GameSession.getActive().unlockedAchievementIds.contains(id)) {
                GameSession.getActive().unlockedAchievementIds.add(id);
            }
            GameDatabase.saveAchievements(
                GameSession.getActive().slotIndex,
                GameSession.getActive().unlockedAchievementIds);
        }
        if (listener != null) listener.onAchievementUnlocked(a);
        return true;
    }

    /**
     * Relocks every achievement. The registry is a JVM-wide singleton, so
     * every screen that seeds it for one save slot (GameScreen) MUST call
     * this before syncFrom() - otherwise unlocks leaked from other slots
     * (e.g. the Achievements screen syncs the union of ALL slots) make
     * unlock() a silent no-op: no toast, no persist. That leak is exactly
     * why Charmed (Void Heart) and True Hunter appeared to never fire.
     * Also backs the debug "clear achievements" cheat.
     */
    public void resetAll() {
        for (int i = 0; i < achievements.size(); i++) {
            achievements.get(i).relock();
        }
    }

    /** Marks saved unlocks without toasts (loading persisted state). */
    public void syncFrom(List<String> ids) {
        if (ids == null) return;
        for (int i = 0; i < ids.size(); i++) {
            Achievement a = find(ids.get(i));
            if (a != null) a.unlock();
        }
    }

    public Achievement find(String id) {
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).getId().equals(id)) return achievements.get(i);
        }
        return null;
    }

    public List<Achievement> all() { return achievements; }

    public int unlockedCount() {
        int c = 0;
        for (int i = 0; i < achievements.size(); i++) {
            if (achievements.get(i).isUnlocked()) c++;
        }
        return c;
    }

    public int totalCount() { return achievements.size(); }
}
