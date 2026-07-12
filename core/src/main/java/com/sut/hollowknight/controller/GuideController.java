package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.model.enums.UiText;
import com.sut.hollowknight.view.screens.MainMenuScreen;

/**
 * Controller for the Guide screen. All display text comes from the
 * UiText catalog, so the Guide follows the Settings language.
 */
public class GuideController {

    private final Game game;

    public GuideController(Game game) {
        this.game = game;
    }

    public String getAbilitiesText() {
        return UiText.ABILITIES_BODY.get();
    }

    public String getCheatCodesText() {
        return UiText.CHEAT_CODES_BODY.get();
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }
}
