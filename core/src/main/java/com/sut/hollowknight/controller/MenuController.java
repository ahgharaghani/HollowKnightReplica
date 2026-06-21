package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.GuideScreen;
import com.sut.hollowknight.view.screens.SettingsScreen;

public class MenuController {
    private Game game;

    public MenuController(Game game) {
        this.game = game;
    }

    public void startNewGame() {
        game.setScreen(new GameScreen(game));
    }

    public void openSettings() {
        game.setScreen(new SettingsScreen(game));
    }

    public void openGuide() {
        game.setScreen(new GuideScreen(game));
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
