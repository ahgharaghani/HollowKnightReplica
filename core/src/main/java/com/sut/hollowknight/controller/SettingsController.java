package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.db.GameDatabase;
import com.sut.hollowknight.model.enums.MenuTheme;
import com.sut.hollowknight.view.screens.KeyBindingsScreen;
import com.sut.hollowknight.view.screens.MainMenuScreen;

/**
 * Controller for the Settings screen. All model mutations go through
 * here — the view never touches GameSettings directly. Every change
 * is persisted to the SQLite database immediately.
 */
public class SettingsController {

    private final Game game;
    private final GameSettings settings;

    public SettingsController(Game game) {
        this.game = game;
        this.settings = GameSettings.getInstance();
    }

    // ---- Read accessors (view calls these to initialise widgets) ----

    public float getMusicVolume()     { return settings.getMusicVolume(); }
    public boolean isMusicMuted()     { return settings.isMusicMuted(); }
    public boolean isSfxMuted()       { return settings.isSfxMuted(); }
    public float getBrightness()      { return settings.getBrightness(); }
    public String getLanguageName()   { return settings.getLanguage().name(); }
    public String getThemeName()      { return settings.getCurrentMenuTheme().name(); }

    // ---- Mutations (view calls these on user interaction) ----

    public void setMusicVolume(float v) {
        settings.setMusicVolume(v);
        save();
    }

    public void setMusicMuted(boolean m) {
        settings.setMusicMuted(m);
        save();
    }

    public void setSfxMuted(boolean m) {
        settings.setSfxMuted(m);
        save();
    }

    public void setBrightness(float b) {
        settings.setBrightness(b);
        save();
    }

    public void resetKeyBindings() {
        settings.setDefaultKeyBindings();
        save();
    }

    /** Toggles language and returns the new language name. */
    public String toggleLanguage() {
        GameSettings.Language next = settings.toggleLanguage();
        save();
        return next.name();
    }

    /** Cycles to the next theme and returns the new theme name. */
    public String cycleTheme() {
        MenuTheme next = settings.cycleTheme();
        save();
        return next.name();
    }

    // ---- Navigation ----

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }

    /**
     * Open the dedicated Key Bindings sub-screen. Replaces the current
     * Settings screen — the Key Bindings screen has its own BACK button
     * that returns here.
     */
    public void openKeyBindings() {
        game.setScreen(new KeyBindingsScreen(game));
    }

    // ---- Persistence ----

    private void save() {
        GameDatabase.saveSettings();
    }
}
