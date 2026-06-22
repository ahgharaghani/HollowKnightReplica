package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.view.screens.AchievementsScreen;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.GuideScreen;
import com.sut.hollowknight.view.screens.SettingsScreen;
import com.sut.hollowknight.view.screens.StartGameScreen;

public class MenuController {
    private Game game;

    public MenuController(Game game) {
        this.game = game;
    }

    /**
     * Open the Start Game menu where the player can either start a new
     * journey or load one of the four save slots.
     */
    public void openStartGame() {
        game.setScreen(new StartGameScreen(game));
    }

    /** Begin a brand new playthrough. */
    public void startNewGame() {
        game.setScreen(new GameScreen(game));
    }

    /** Continue an existing save slot. */
    public void loadSaveSlot(int slotIndex) {
        // In a real implementation this would deserialize the per-slot
        // save file before entering the GameScreen.
        Gdx.app.log("MenuController", "Loading save slot " + slotIndex);
        game.setScreen(new GameScreen(game));
    }

    public void openSettings() {
        game.setScreen(new SettingsScreen(game));
    }

    public void openGuide() {
        game.setScreen(new GuideScreen(game));
    }

    public void openAchievements() {
        game.setScreen(new AchievementsScreen(game));
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
