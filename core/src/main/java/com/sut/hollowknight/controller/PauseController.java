package com.sut.hollowknight.controller;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.SaveSlotRegistry;
import com.sut.hollowknight.view.screens.GameScreen;
import com.sut.hollowknight.view.screens.MainMenuScreen;
import com.sut.hollowknight.view.screens.SettingsScreen;

/**
 * Controller for the Pause overlay. Handles resume, save-and-quit, and
 * navigation to the Settings screen (rendered over the frozen game).
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

    /**
     * Open the Settings screen — the SAME screen used from the main menu —
     * but backed by a darkened snapshot of the paused game.
     *
     * @param gameScreen   the (still paused, still alive) game screen to return to
     * @param gameSnapshot captured framebuffer of the frozen game; ownership
     *                     passes to the Settings screen, which disposes it
     */
    public void openSettings(GameScreen gameScreen, Texture gameSnapshot) {
        game.setScreen(new SettingsScreen(game, gameSnapshot, gameScreen));
    }

    /**
     * Flush the live Knight state into the active save slot, persist it,
     * and exit to the main menu.
     */
    public void saveAndQuit(GameScreen gameScreen) {
        // The full flush lives in GameScreen.persistSession() - one place
        // that saves vitals, position, current map + spawn point, charms
        // and boss/room state. The old inline flush here silently dropped
        // the room, spawn and charms, which is why loads misplaced the hero.
        gameScreen.persistSession();
        GameSession.end();
        game.setScreen(new MainMenuScreen(game));
        // The click that got us here was dispatched by the GameScreen's own
        // stage — defer disposal until this frame's event pass has unwound.
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() { gameScreen.dispose(); }
        });
    }

    public String getCheatCodesText() {
        return UiText.CHEAT_CODES_BODY.get();
    }
}
