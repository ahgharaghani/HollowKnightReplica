package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.sut.hollowknight.controller.SettingsController;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;

public class SettingsScreen extends AbstractMenuScreen {

    private final SettingsController controller;
    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    private Label brightnessValueLabel;
    private Label musicValueLabel;
    private Label languageValueLabel;
    private Label themeValueLabel;

    // ---- Pause-menu context (null when opened from the main menu) ----
    /** Frozen game frame shown behind the settings; owned + disposed here. */
    private Texture pauseBackdrop;
    /** The still-paused GameScreen to hand back when BACK is clicked. */
    private GameScreen returnScreen;
    private TextButton btnBack;

    public SettingsScreen(Game game) {
        super(game);
        this.controller = new SettingsController(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont = MenuUi.buildTrajanFont(44);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(33);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    /**
     * Pause-menu variant: the exact same Settings UI as the main menu, but
     * rendered over a frozen, darkened snapshot of the running game.
     *
     * @param gameSnapshot captured framebuffer of the paused game (ownership
     *                     passes to this screen; disposed with it)
     * @param returnScreen the still-paused GameScreen to return to on BACK
     */
    public SettingsScreen(Game game, Texture gameSnapshot, GameScreen returnScreen) {
        this(game);
        this.pauseBackdrop = gameSnapshot;
        this.returnScreen = returnScreen;

        // Framebuffer pixmaps are stored top-down; flip the region vertically.
        TextureRegion region = new TextureRegion(gameSnapshot);
        region.flip(false, true);
        menuBackgroundImage.setDrawable(new TextureRegionDrawable(region));
        // Darken the frozen game via a multiplicative tint — no extra
        // overlay quad, so it costs nothing per frame.
        menuBackgroundImage.setColor(0.42f, 0.42f, 0.48f, 1f);

        btnBack.setText("Back to Game");
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(50).padLeft(80).padRight(80);
        uiStage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        Label title = new Label("Settings", titleStyle);
        root.add(title).colspan(2).padBottom(30).row();

        Table body = new Table();
        body.top();
        body.defaults().pad(10);

        Label.LabelStyle rowLabelStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);
        rowLabelStyle.fontColor = MenuUi.TEXT_LIGHT;

        // --- Music volume ---
        Label musicLabel = new Label("Music Volume", rowLabelStyle);
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(controller.getMusicVolume());
        musicValueLabel = new Label(volumeText(controller.getMusicVolume()), rowLabelStyle);
        musicSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float v = musicSlider.getValue();
                controller.setMusicVolume(v);
                musicValueLabel.setText(volumeText(v));
            }
        });
        body.add(musicLabel).left().width(280);
        body.add(musicSlider).width(360);
        body.add(musicValueLabel).width(80).row();

        // --- Music mute ---
        Label musicMuteLabel = new Label("Mute Music", rowLabelStyle);
        CheckBox musicMuteBox = new CheckBox("", skin);
        musicMuteBox.setChecked(controller.isMusicMuted());
        musicMuteBox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                controller.setMusicMuted(musicMuteBox.isChecked());
            }
        });
        body.add(musicMuteLabel).left().width(280);
        body.add(musicMuteBox).colspan(2).left().row();

        // --- SFX mute ---
        Label sfxMuteLabel = new Label("Mute SFX (default reduction)", rowLabelStyle);
        CheckBox sfxMuteBox = new CheckBox("", skin);
        sfxMuteBox.setChecked(controller.isSfxMuted());
        sfxMuteBox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                controller.setSfxMuted(sfxMuteBox.isChecked());
            }
        });
        body.add(sfxMuteLabel).left().width(280);
        body.add(sfxMuteBox).colspan(2).left().row();

        // --- Brightness ---
        Label brightnessLabel = new Label("Brightness", rowLabelStyle);
        Slider brightnessSlider = new Slider(0.2f, 1.8f, 0.01f, false, skin);
        brightnessSlider.setValue(controller.getBrightness());
        brightnessValueLabel = new Label(
            String.format("%d%%", (int)(controller.getBrightness() * 100)), rowLabelStyle);
        brightnessSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float v = brightnessSlider.getValue();
                controller.setBrightness(v);
                brightnessValueLabel.setText(String.format("%d%%", (int)(v * 100)));
            }
        });
        body.add(brightnessLabel).left().width(280);
        body.add(brightnessSlider).width(360);
        body.add(brightnessValueLabel).width(80).row();

        // --- Keyboard controls ---
        Label controlsLabel = new Label("Keyboard Controls", rowLabelStyle);
        TextButton btnKeyBindings = new AnimatedPointerButton("Key Bindings", skin, "bodyBtn");
        btnKeyBindings.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (returnScreen != null) {
                    // Keep the pause context alive: BACK on the KeyBindings
                    // screen returns to THIS instance, not a fresh one.
                    game.setScreen(new KeyBindingsScreen(game, SettingsScreen.this));
                } else {
                    controller.openKeyBindings();
                }
            }
        });
        body.add(controlsLabel).left().width(280);
        body.add(btnKeyBindings).colspan(2).left().row();

        // --- Language ---
        Label langLabel = new Label("Language", rowLabelStyle);
        languageValueLabel = new Label(controller.getLanguageName(), rowLabelStyle);
        TextButton btnLang = new AnimatedPointerButton("Change Language", skin, "bodyBtn");
        btnLang.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                String newName = controller.toggleLanguage();
                languageValueLabel.setText(newName);
            }
        });
        body.add(langLabel).left().width(280);
        body.add(btnLang).width(220);
        body.add(languageValueLabel).width(120).row();

        // --- Theme ---
        Label themeLabel = new Label("Theme", rowLabelStyle);
        themeValueLabel = new Label(controller.getThemeName(), rowLabelStyle);
        TextButton btnTheme = new AnimatedPointerButton("Change Theme", skin, "bodyBtn");
        btnTheme.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                String newName = controller.cycleTheme();
                themeValueLabel.setText(newName);
                refreshBackgroundImage();
            }
        });
        body.add(themeLabel).left().width(280);
        body.add(btnTheme).width(220);
        body.add(themeValueLabel).width(120).row();

        root.add(body).growX().row();

        // --- Back ---
        btnBack = new AnimatedPointerButton("Back to Main Menu", skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (returnScreen != null) {
                    // Came from the pause menu — hand the still-paused game back.
                    game.setScreen(returnScreen);
                    // This click was dispatched by our own stage; defer disposal
                    // until the event pass has unwound.
                    Gdx.app.postRunnable(SettingsScreen.this::dispose);
                } else {
                    controller.backToMainMenu();
                }
            }
        });

        Table footer = new Table();
        footer.setFillParent(true);
        footer.bottom().padBottom(50);
        uiStage.addActor(footer);
        footer.add(btnBack).width(420).height(60);
    }

    /**
     * The frozen-game snapshot when this screen was opened from the pause
     * menu, or null. Package-private: shared with the KeyBindings sub-screen.
     * Ownership stays with this screen — callers must NOT dispose it.
     */
    Texture getPauseBackdrop() {
        return pauseBackdrop;
    }

    @Override
    protected void refreshBackgroundImage() {
        // Keep the frozen-game backdrop when opened from pause; show() and
        // theme changes would otherwise restore the menu theme art.
        if (pauseBackdrop != null) return;
        super.refreshBackgroundImage();
    }

    private static String volumeText(float v) {
        if (v <= 0.001f) return "0%";
        return String.format("%d%%", (int)(v * 100));
    }

    @Override public void updateLogic(float delta) { }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(MenuUi.BG_DARK.r, MenuUi.BG_DARK.g, MenuUi.BG_DARK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
        trajanFont.dispose();
        perpetuaFont.dispose();
        if (pauseBackdrop != null) {
            pauseBackdrop.dispose();
        }
    }
}
