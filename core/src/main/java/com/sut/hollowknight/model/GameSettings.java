package com.sut.hollowknight.model;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.enums.MenuTheme;

public final class GameSettings {

    public enum Language { ENGLISH, FRENCH }

    public static final int MOUSE_OFFSET = 1000;

    private static GameSettings instance;

    private MenuTheme currentMenuTheme = MenuTheme.THEME_01;
    private float musicVolume    = 0.7f;
    private boolean musicMuted   = false;
    private boolean sfxMuted     = false;
    private float sfxVolume      = 0.8f;
    private float sfxReducedGain = 0.4f;
    private float brightness     = 1.0f;
    private static final float MIN_BRIGHTNESS = 0.2f;
    private static final float MAX_BRIGHTNESS = 1.8f;
    private Language language    = Language.ENGLISH;

    private int moveUpKey;
    private int moveDownKey;
    private int moveLeftKey;
    private int moveRightKey;
    private int jumpKey;
    private int attackKey;
    private int dashKey;
    private int focusCastKey;
    private int inventoryKey;
    private int quickMapKey;
    private int superDashKey;
    private int dreamNailKey;
    private int quickCastKey;

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
    public void   setBrightness(float b)     {
        this.brightness = MathUtils.clamp(b, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

    public Language getLanguage()            { return language; }
    public void     setLanguage(Language l)  { this.language = l; }

    // ---- Key binding accessors ----

    public int getMoveUpKey()        { return moveUpKey; }
    public int getMoveDownKey()      { return moveDownKey; }
    public int getMoveLeftKey()      { return moveLeftKey; }
    public int getMoveRightKey()     { return moveRightKey; }
    public int getJumpKey()          { return jumpKey; }
    public int getAttackKey()        { return attackKey; }
    public int getDashKey()          { return dashKey; }
    public int getFocusCastKey()     { return focusCastKey; }
    public int getInventoryKey()     { return inventoryKey; }
    public int getQuickMapKey()      { return quickMapKey; }
    public int getSuperDashKey()     { return superDashKey; }
    public int getDreamNailKey()     { return dreamNailKey; }
    public int getQuickCastKey()     { return quickCastKey; }

    public void setMoveUpKey(int k)      { this.moveUpKey = k; }
    public void setMoveDownKey(int k)    { this.moveDownKey = k; }
    public void setMoveLeftKey(int k)    { this.moveLeftKey = k; }
    public void setMoveRightKey(int k)   { this.moveRightKey = k; }
    public void setJumpKey(int k)        { this.jumpKey = k; }
    public void setAttackKey(int k)      { this.attackKey = k; }
    public void setDashKey(int k)        { this.dashKey = k; }
    public void setFocusCastKey(int k)   { this.focusCastKey = k; }
    public void setInventoryKey(int k)   { this.inventoryKey = k; }
    public void setQuickMapKey(int k)    { this.quickMapKey = k; }
    public void setSuperDashKey(int k)   { this.superDashKey = k; }
    public void setDreamNailKey(int k)   { this.dreamNailKey = k; }
    public void setQuickCastKey(int k)   { this.quickCastKey = k; }

    public void setDefaultKeyBindings() {
        moveUpKey      = Input.Keys.W;
        moveDownKey    = Input.Keys.S;
        moveLeftKey    = Input.Keys.A;
        moveRightKey   = Input.Keys.D;
        jumpKey        = Input.Keys.SPACE;
        attackKey      = mouseCode(Input.Buttons.LEFT);
        dashKey        = Input.Keys.SHIFT_LEFT;
        focusCastKey   = Input.Keys.Q;
        inventoryKey   = Input.Keys.I;
        quickMapKey    = Input.Keys.M;
        superDashKey   = Input.Keys.R;
        dreamNailKey   = mouseCode(Input.Buttons.RIGHT);
        quickCastKey   = Input.Keys.F;
    }

    // ---- Mouse-binding helpers ----

    public static int mouseCode(int button) {
        return MOUSE_OFFSET + button;
    }

    public static boolean isMouse(int code) {
        return code >= MOUSE_OFFSET;
    }

    public static int toMouseButton(int code) {
        return code - MOUSE_OFFSET;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    public Language toggleLanguage() {
        language = (language == Language.ENGLISH) ? Language.FRENCH : Language.ENGLISH;
        return language;
    }

    public MenuTheme cycleTheme() {
        MenuTheme[] themes = MenuTheme.values();
        currentMenuTheme = themes[(currentMenuTheme.ordinal() + 1) % themes.length];
        return currentMenuTheme;
    }
}
