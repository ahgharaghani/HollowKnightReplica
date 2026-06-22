package com.sut.hollowknight.model;

import com.badlogic.gdx.Input;
import com.sut.hollowknight.model.enums.MenuTheme;

public final class GameSettings {

    public enum Language { ENGLISH, PERSIAN }

    private static GameSettings instance;

    private MenuTheme currentMenuTheme = MenuTheme.THEME_01;
    private float musicVolume    = 0.7f;
    private boolean musicMuted   = false;
    private boolean sfxMuted     = false;
    private float sfxVolume      = 0.8f;
    private float sfxReducedGain = 0.4f;
    private float brightness     = 0.5f;   // 0..1 (0 dark, 1 bright); 0.5 == neutral
    private Language language    = Language.ENGLISH;

    // Default keyboard bindings
    private int moveLeftKey;
    private int moveRightKey;
    private int jumpKey;
    private int attackKey;
    private int dashKey;

    private GameSettings() {
        setDefaultKeyBindings();
    }

    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    public MenuTheme getCurrentMenuTheme()                      { return currentMenuTheme; }
    public void setCurrentMenuTheme(MenuTheme currentMenuTheme) { this.currentMenuTheme = currentMenuTheme; }

    public float  getMusicVolume()           { return musicVolume; }
    public void   setMusicVolume(float v)    { this.musicVolume = clamp01(v); }

    public boolean isMusicMuted()            { return musicMuted; }
    public void    setMusicMuted(boolean m)  { this.musicMuted = m; }

    public boolean isSfxMuted()              { return sfxMuted; }
    public void    setSfxMuted(boolean m)    { this.sfxMuted = m; }

    public float  getSfxVolume()             { return sfxVolume; }
    public void   setSfxVolume(float v)      { this.sfxVolume = clamp01(v); }

    public float  getSfxReducedGain()        { return sfxReducedGain; }
    public void   setSfxReducedGain(float v) { this.sfxReducedGain = clamp01(v); }

    public float  getBrightness()            { return brightness; }
    public void   setBrightness(float b)     { this.brightness = clamp01(b); }

    public Language getLanguage()            { return language; }
    public void     setLanguage(Language l)  { this.language = l; }

    public int getMoveLeftKey()           { return moveLeftKey; }
    public int getMoveRightKey()          { return moveRightKey; }
    public int getJumpKey()               { return jumpKey; }
    public int getAttackKey()             { return attackKey; }
    public int getDashKey()               { return dashKey; }

    public void setMoveLeftKey(int k)     { this.moveLeftKey = k; }
    public void setMoveRightKey(int k)    { this.moveRightKey = k; }
    public void setJumpKey(int k)         { this.jumpKey = k; }
    public void setAttackKey(int k)       { this.attackKey = k; }
    public void setDashKey(int k)         { this.dashKey = k; }

    public void setDefaultKeyBindings() {
        moveLeftKey  = Input.Keys.LEFT;
        moveRightKey = Input.Keys.RIGHT;
        jumpKey      = Input.Keys.SPACE;
        attackKey    = Input.Keys.X;
        dashKey      = Input.Keys.C;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
