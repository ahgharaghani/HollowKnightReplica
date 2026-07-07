package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.SaveSlot;
import com.sut.hollowknight.model.SaveSlotRegistry;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.MainMenuScreen;

import java.util.List;

/**
 * Controller for the Start Game screen. Manages save slot selection,
 * new game creation, and loading existing saves from the database.
 */
public class StartGameController {

    private final Game game;

    public StartGameController(Game game) {
        this.game = game;
    }

    /** Returns the current list of save slots for display. */
    public List<SaveSlot> getSaveSlots() {
        return SaveSlotRegistry.all();
    }

    /**
     * Start a new game in the first empty slot.
     * Creates a fresh GameData snapshot and persists it.
     */
    public void startNewGame() {
        // Find the first empty slot
        for (SaveSlot slot : SaveSlotRegistry.all()) {
            if (slot.isEmpty()) {
                GameData data = GameData.newGame(slot.getIndex());
                SaveSlotRegistry.saveGameData(data);
                GameSession.begin(data);
                Gdx.app.log("StartGameController",
                    "New game in slot " + slot.getIndex());
                game.setScreen(new GameScreen(game));
                return;
            }
        }
        // All slots full — overwrite slot 1 (or show error)
        Gdx.app.log("StartGameController", "All slots full, overwriting slot 1");
        GameData data = GameData.newGame(1);
        SaveSlotRegistry.saveGameData(data);
        GameSession.begin(data);
        game.setScreen(new GameScreen(game));
    }

    /**
     * Load an existing save from the database and enter gameplay.
     */
    public void loadSaveSlot(int slotIndex) {
        GameData data = SaveSlotRegistry.loadGameData(slotIndex);
        GameSession.begin(data);
        Gdx.app.log("StartGameController",
            "Loaded slot " + slotIndex + ": " + data.knightName +
            " | " + data.lastArea + " | HP=" + data.hpMasks);
        // TODO: pass GameData to GameScreen so it can initialise the world
        game.setScreen(new GameScreen(game));
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }
}
