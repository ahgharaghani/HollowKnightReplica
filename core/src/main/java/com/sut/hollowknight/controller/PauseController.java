package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.SaveSlotRegistry;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.MainMenuScreen;
import com.sut.hollowknight.view.screens.SettingsScreen;

/**
 * Controller for the Pause overlay. Handles resume, save, and
 * navigation to settings or main menu.
 */
public class PauseController {

    private final Game game;

    public PauseController(Game game) {
        this.game = game;
    }

    /** Resume gameplay — hide the pause overlay. */
    public void resume(GameScreen gameScreen) {
        gameScreen.setPaused(false);
    }

    public void openSettings() {
        game.setScreen(new SettingsScreen(game));
    }

    /**
     * Save the current game state and return to the main menu.
     *
     * @param gameData the current game snapshot to persist
     */
    public void saveAndQuit(GameData gameData) {
        SaveSlotRegistry.saveGameData(gameData);
        game.setScreen(new MainMenuScreen(game));
    }

    public String getCheatCodesText() {
        return "Ctrl+B  Boss Teleport  |  Ctrl+F  Noclip  |  Ctrl+H  Heal\n" +
               "Ctrl+S  Refill Soul   |  Ctrl+G  God Mode |  Ctrl+K  Insta-Kill";
    }
}
