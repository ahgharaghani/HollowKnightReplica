package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.view.screens.AchievementsScreen;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.GuideScreen;
import com.sut.hollowknight.view.screens.SettingsScreen;
import com.sut.hollowknight.view.screens.StartGameScreen;

/**
 * Controller for the Main Menu. Handles navigation to sub-screens.
 */
public class MainMenuController {

    private final Game game;

    public MainMenuController(Game game) {
        this.game = game;
    }

    public void openStartGame() {
        game.setScreen(new StartGameScreen(game));
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
        com.badlogic.gdx.Gdx.app.exit();
    }
}
