package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.sut.hollowknight.controller.MenuController;

public class MainMenuScreen extends AbstractScreen {
    private MenuController controller;
    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    public MainMenuScreen(Game game) {
        super(game);
        this.controller = new MenuController(game);
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        initTrajanFont();
        this.skin.add("trajanFont", trajanFont, BitmapFont.class);

        TextButtonStyle headingTextButtonStyle = new TextButtonStyle(skin.get(TextButtonStyle.class));
        headingTextButtonStyle.font = skin.getFont("trajanFont");
        skin.add("headingTextBtn", headingTextButtonStyle);

        createUI();
    }

    private void initTrajanFont() {
        FreeTypeFontGenerator trajanGenerator = new FreeTypeFontGenerator(
            Gdx.files.internal("font/TrajanPro-Regular.ttf"));
        FreeTypeFontParameter  trajanParameter = new FreeTypeFontParameter();
        trajanParameter.size = 40;
        trajanParameter.minFilter = Texture.TextureFilter.Linear;
        trajanParameter.magFilter = Texture.TextureFilter.Linear;
        trajanParameter.color = Color.WHITE;
        trajanParameter.borderColor = Color.BLACK;
        trajanParameter.borderWidth = 2;
        trajanParameter.shadowOffsetX = 2; trajanParameter.shadowOffsetY = 2;

        this.trajanFont = trajanGenerator.generateFont(trajanParameter);

        trajanGenerator.dispose();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        uiStage.addActor(table);

        TextButton btnStart = new TextButton("Start Game", skin, "headingTextBtn");
        TextButton btnSettings = new TextButton("Settings", skin, "headingTextBtn");
        TextButton btnGuide = new TextButton("Guide", skin, "headingTextBtn");
        TextButton btnAchievements = new TextButton("Achievements", skin, "headingTextBtn");
        TextButton btnQuit = new TextButton("Quit Game", skin, "headingTextBtn");

        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.startNewGame();
            }
        });

        btnSettings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.openSettings();
            }
        });

        btnGuide.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.openGuide();
            }
        });

        btnQuit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.quitGame();
            }
        });

        table.defaults().pad(12);
        table.add(btnStart).width(300).height(60).row();
        table.add(btnSettings).width(300).height(60).row();
        table.add(btnGuide).width(300).height(60).row();
        table.add(btnAchievements).width(300).height(60).row();
        table.add(btnQuit).width(300).height(60).row();
        table.bottom().padBottom(50);
    }

    @Override
    public void updateLogic(float delta) {

    }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
    }
}
