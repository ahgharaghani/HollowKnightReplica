package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.view.screens.MainMenuScreen;

/**
 * Controller for the Guide screen. Provides formatted text content
 * derived from the current game settings (e.g. key bindings).
 */
public class GuideController {

    private final Game game;
    private final GameSettings settings;

    public GuideController(Game game) {
        this.game = game;
        this.settings = GameSettings.getInstance();
    }

    /** Builds key bindings text from current settings. */
    public String getKeyBindingsText() {
        return String.format(
            "Move Left:  key code %d\n" +
            "Move Right: key code %d\n" +
            "Jump:       key code %d\n" +
            "Attack:     key code %d\n" +
            "Dash:       key code %d\n" +
            "Focus:      A\n" +
            "Inventory:  I\n" +
            "Pause:      Escape",
            settings.getMoveLeftKey(),
            settings.getMoveRightKey(),
            settings.getJumpKey(),
            settings.getAttackKey(),
            settings.getDashKey());
    }

    public String getAbilitiesText() {
        return "Nail Attack - Strike enemies with your nail to deal damage and gain Soul.\n\n" +
               "Dash - A quick horizontal dash with brief invulnerability.\n\n" +
               "Double Jump - Jump again while airborne.\n\n" +
               "Focus - Channel Soul to heal one mask. Must remain stationary.\n\n" +
               "Vengeful Spirit - Fire a horizontal magic projectile (costs 33 Soul).\n\n" +
               "Howling Wraiths - Upward magic burst dealing 3 rapid hits (costs 33 Soul).\n\n" +
               "Pogo - Downward strike while airborne to bounce off enemies/spikes.";
    }

    public String getCheatCodesText() {
        return "Left Ctrl + B - Teleport to Boss Arena\n" +
               "Left Ctrl + F - Noclip / Spectator Mode\n" +
               "Left Ctrl + H - Emergency Heal\n" +
               "Left Ctrl + S - Refill Soul Vessel\n" +
               "Left Ctrl + G - God Mode (toggle)\n" +
               "Left Ctrl + K - Insta-Kill All Enemies";
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }
}
