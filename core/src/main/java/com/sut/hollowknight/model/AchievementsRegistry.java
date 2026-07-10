package com.sut.hollowknight.model;

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
        achievements.add(new Achievement(COMPLETION, "Completion",
            "Finish the game.",
            "ui/achievement/achievement__0000_100_complete.png"));
        achievements.add(new Achievement(SPEEDRUN, "Speedrun",
            "Finish the game in under 10 minutes.",
            "ui/achievement/achievement_ultra_fast_finish.png"));
        achievements.add(new Achievement(TRUE_HUNTER, "True Hunter",
            "Kill all the enemies.",
            "ui/achievement/achievement_Hunter_Marks.png"));
        achievements.add(new Achievement(FALSEHOOD, "Falsehood",
            "Defeat the False Knight.",
            "ui/achievement/achievement__0031_false_knight_dream.png"));
        achievements.add(new Achievement(CHARMED, "Charmed",
            "Acquire Void Heart.",
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
        // Write-through: the existing save flow persists this list as-is.
        if (GameSession.isActive()
                && !GameSession.getActive().unlockedAchievementIds.contains(id)) {
            GameSession.getActive().unlockedAchievementIds.add(id);
        }
        if (listener != null) listener.onAchievementUnlocked(a);
        return true;
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
