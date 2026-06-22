package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.enums.MenuTheme;
import com.sut.hollowknight.view.MenuUi;
import org.w3c.dom.Text;

public class SettingsScreen extends AbstractMenuScreen {

    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    private final GameSettings settings = GameSettings.getInstance();

    private Label brightnessValueLabel;
    private Label musicValueLabel;
    private Label languageValueLabel;
    private Label themeValueLabel;

    public SettingsScreen(Game game) {
        super(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont = MenuUi.buildTrajanFont(44);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(22);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(50).padLeft(80).padRight(80);
        uiStage.addActor(root);

        // ----- Title -----
        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        Label title = new Label("Settings", titleStyle);
        root.add(title).colspan(2).padBottom(30).row();

        // ----- Body table: two columns (label | control) -----
        Table body = new Table();
        body.top();
        body.defaults().pad(10);

        Label.LabelStyle rowLabelStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);
        rowLabelStyle.fontColor = MenuUi.TEXT_LIGHT;

        // --- Music volume ---
        Label musicLabel = new Label("Music Volume", rowLabelStyle);
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(settings.getMusicVolume());
        musicValueLabel = new Label(volumeText(settings.getMusicVolume()), rowLabelStyle);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float v = musicSlider.getValue();
                settings.setMusicVolume(v);
                musicValueLabel.setText(volumeText(v));
            }
        });
        body.add(musicLabel).left().width(280);
        body.add(musicSlider).width(360);
        body.add(musicValueLabel).width(80).row();

        // --- Music mute checkbox ---
        Label musicMuteLabel = new Label("Mute Music", rowLabelStyle);
        CheckBox musicMuteBox = new CheckBox("", skin);
        musicMuteBox.setChecked(settings.isMusicMuted());
        musicMuteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setMusicMuted(musicMuteBox.isChecked());
            }
        });
        body.add(musicMuteLabel).left().width(280);
        body.add(musicMuteBox).colspan(2).left().row();

        // --- SFX mute checkbox ---
        Label sfxMuteLabel = new Label("Mute SFX (default reduction)", rowLabelStyle);
        CheckBox sfxMuteBox = new CheckBox("", skin);
        sfxMuteBox.setChecked(settings.isSfxMuted());
        sfxMuteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                settings.setSfxMuted(sfxMuteBox.isChecked());
            }
        });
        body.add(sfxMuteLabel).left().width(280);
        body.add(sfxMuteBox).colspan(2).left().row();

        // --- Brightness slider ---
        Label brightnessLabel = new Label("Brightness", rowLabelStyle);
        Slider brightnessSlider = new Slider(0f, 1f, 0.01f, false, skin);
        brightnessSlider.setValue(settings.getBrightness());
        brightnessValueLabel = new Label(
            String.format("%d%%", (int)(settings.getBrightness() * 100)),
            rowLabelStyle);
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float v = brightnessSlider.getValue();
                settings.setBrightness(v);
                brightnessValueLabel.setText(String.format("%d%%", (int)(v * 100)));
            }
        });
        body.add(brightnessLabel).left().width(280);
        body.add(brightnessSlider).width(360);
        body.add(brightnessValueLabel).width(80).row();

        // --- Keyboard controls reset ---
        Label controlsLabel = new Label("Keyboard Controls", rowLabelStyle);
        TextButton btnReset = new TextButton("Reset to Default", skin, "bodyBtn");
        btnReset.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.setDefaultKeyBindings();
            }
        });
        body.add(controlsLabel).left().width(280);
        body.add(btnReset).colspan(2).left().row();

        // --- Language toggle ---
        Label langLabel = new Label("Language", rowLabelStyle);
        languageValueLabel = new Label(settings.getLanguage().name(), rowLabelStyle);
        TextButton btnLang = new TextButton("Change Language", skin, "bodyBtn");
        btnLang.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.Language next =
                    settings.getLanguage() == GameSettings.Language.ENGLISH
                        ? GameSettings.Language.PERSIAN
                        : GameSettings.Language.ENGLISH;
                settings.setLanguage(next);
                languageValueLabel.setText(next.name());
            }
        });
        body.add(langLabel).left().width(280);
        body.add(btnLang).width(220);
        body.add(languageValueLabel).width(120).row();

        Label themeLabel = new Label("Theme", rowLabelStyle);
        themeValueLabel = new Label(settings.getCurrentMenuTheme().name(), rowLabelStyle);
        TextButton btnTheme = new TextButton("Change Theme", skin, "bodyBtn");
        btnTheme.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MenuTheme next = MenuTheme.values()[
                    (settings.getCurrentMenuTheme().ordinal() + 1) % MenuTheme.values().length
                    ];
                settings.setCurrentMenuTheme(next);
                themeValueLabel.setText(next.name());

                refreshBackgroundImage();
            }
        });

        body.add(themeLabel).left().width(280);
        body.add(btnTheme).width(220);
        body.add(themeValueLabel).width(120).row();

        root.add(body).growX().row();

        // ----- Footer: Back button -----
        TextButton btnBack = new TextButton("Back to Main Menu", skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        Table footer = new Table();
        footer.setFillParent(true);
        footer.bottom().padBottom(50);
        uiStage.addActor(footer);
        footer.add(btnBack).width(420).height(60);
    }

    private static String volumeText(float v) {
        if (v <= 0.001f) return "0%";
        return String.format("%d%%", (int)(v * 100));
    }

    @Override
    public void updateLogic(float delta) { }

    @Override
    public void renderGraphics() {
        // Background brightness subtly tracks the settings value, so the
        // player can see the brightness setting taking effect.
        float b = settings.getBrightness();
        float bg = 0.05f + 0.06f * b;
        Gdx.gl.glClearColor(bg, bg + 0.01f, bg + 0.04f, 1);
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
    }
}
