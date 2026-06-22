package com.sut.hollowknight.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the global state of all Hollow Knight achievements.
 *
 * <p>Modeled as a singleton so both the {@code AchievementsScreen} (for
 * display) and any future gameplay code (for unlocking) can reach the same
 * list without having to thread the object through every screen.</p>
 */
public final class AchievementsRegistry {

    private static AchievementsRegistry instance;

    private final List<Achievement> achievements = new ArrayList<>();

    private AchievementsRegistry() {
        // The five achievements required by the assignment.
        achievements.add(new Achievement(
            "completion",
            "Completion",
            "Finish the game and watch the credits roll.",
            "???",
            "Finish the game.",
            false));

        achievements.add(new Achievement(
            "speedrun",
            "Speedrun",
            "Finish the game in under 60 minutes.",
            "???",
            "Finish the game within a time limit.",
            false));

        achievements.add(new Achievement(
            "true_hunter",
            "True Hunter",
            "Defeat every type of enemy in the game.",
            "???",
            "Kill all enemy types.",
            false));

        achievements.add(new Achievement(
            "false_knight",
            "Defeat False Knight",
            "Defeat the False Knight boss.",
            "???",
            "Defeat the first major boss.",
            false));

        // Bonus / custom achievement.
        achievements.add(new Achievement(
            "explorer",
            "Path of the Knight",
            "Discover every area of Hallownest.",
            "???",
            "Explore the world.",
            false));
    }

    public static synchronized AchievementsRegistry getInstance() {
        if (instance == null) {
            instance = new AchievementsRegistry();
        }
        return instance;
    }

    public List<Achievement> all() {
        return achievements;
    }

    public int unlockedCount() {
        int c = 0;
        for (Achievement a : achievements) {
            if (a.isUnlocked()) c++;
        }
        return c;
    }

    public int totalCount() {
        return achievements.size();
    }

    /** Unlock the achievement with the given id. Returns true if it was newly unlocked. */
    public boolean unlock(String id) {
        for (Achievement a : achievements) {
            if (a.getId().equals(id)) {
                if (!a.isUnlocked()) {
                    a.unlock();
                    return true;
                }
                return false;
            }
        }
        return false;
    }
}
