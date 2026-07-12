package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.SaveSlotRegistry;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.MainMenuScreen;

import java.util.ArrayList;

/**
 * Controller for the End Screen (spec: shown after the final boss falls).
 *
 * <p>MVC: the EndScreen view only reads run stats through this controller
 * and forwards button clicks here; all session/persistence logic lives on
 * this side. The screen is reached while the victorious session is still
 * active, so the stats snapshot is simply the active GameData.</p>
 */
public class EndScreenController {

    private final Game game;

    /**
     * Stats snapshot captured at construction time. The session may be
     * ended or replaced by the buttons below, so the view must not go
     * back to GameSession later - it reads this frozen copy instead.
     */
    private final int deathCount;
    private final int enemyKillCount;
    private final int playTimeSeconds;
    private final int slotIndex;
    private final String knightName;

    public EndScreenController(Game game) {
        this.game = game;
        GameData run = GameSession.getActive();
        if (run != null) {
            deathCount      = run.deathCount;
            enemyKillCount  = run.enemyKillCount;
            playTimeSeconds = run.playTimeSeconds;
            slotIndex       = run.slotIndex;
            knightName      = run.knightName;
        } else {
            // Defensive: reached without a session (should not happen).
            deathCount = 0; enemyKillCount = 0; playTimeSeconds = 0;
            slotIndex = 1;  knightName = "Knight";
        }
    }

    // ---- Stats for the view ----

    public int getDeathCount()      { return deathCount; }
    public int getEnemyKillCount()  { return enemyKillCount; }
    public int getPlayTimeSeconds() { return playTimeSeconds; }

    /**
     * Restart (spec: end-screen restart button): the finished run's slot is
     * reset to a brand-new game. Achievements are earned per player, not
     * per attempt, so the unlock list survives the wipe; everything else
     * (boss kill, counters, charms, cleared rooms) starts over.
     */
    public void restart() {
        GameData fresh = GameData.newGame(slotIndex);
        fresh.knightName = knightName;
        fresh.unlockedAchievementIds =
            new ArrayList<>(GameSession.getActive() != null
                ? GameSession.getActive().unlockedAchievementIds
                : new ArrayList<>());
        SaveSlotRegistry.saveGameData(fresh);
        GameSession.begin(fresh);
        Gdx.app.log("EndScreenController", "Restarted run in slot " + slotIndex);
        game.setScreen(new GameScreen(game));
    }

    /** Main Menu (spec): the run is already persisted; just end the session. */
    public void backToMainMenu() {
        GameSession.end();
        game.setScreen(new MainMenuScreen(game));
    }
}
