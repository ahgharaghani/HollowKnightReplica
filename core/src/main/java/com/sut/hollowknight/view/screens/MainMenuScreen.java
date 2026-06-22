package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.sut.hollowknight.controller.MenuController;
import com.sut.hollowknight.view.MenuUi;

public class MainMenuScreen extends AbstractMenuScreen {
    private MenuController controller;
    private Skin skin;
    private BitmapFont trajanFont;

    public MainMenuScreen(Game game) {
        super(game);
        this.controller = new MenuController(game);
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        trajanFont = MenuUi.buildTrajanFont(40);
        MenuUi.registerHeadingStyle(skin, trajanFont);

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        uiStage.addActor(table);

        TextButton btnStart = new TextButton("Start Game", skin, "headingBtn");
        TextButton btnSettings = new TextButton("Settings", skin, "headingBtn");
        TextButton btnGuide = new TextButton("Guide", skin, "headingBtn");
        TextButton btnAchievements = new TextButton("Achievements", skin, "headingBtn");
        TextButton btnQuit = new TextButton("Quit Game", skin, "headingBtn");

        btnStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.openStartGame();
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

        btnAchievements.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.openAchievements();
            }
        });

        btnQuit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.quitGame();
            }
        });

        table.defaults().pad(12);
        table.add(btnStart).width(360).height(60).row();
        table.add(btnSettings).width(360).height(60).row();
        table.add(btnGuide).width(360).height(60).row();
        table.add(btnAchievements).width(360).height(60).row();
        table.add(btnQuit).width(360).height(60).row();
        table.bottom().padBottom(60);
    }

    @Override
    public void updateLogic(float delta) {

    }

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
    }
}
