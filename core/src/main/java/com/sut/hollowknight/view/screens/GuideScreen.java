package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.controller.GuideController;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;

public class GuideScreen extends AbstractMenuScreen {

    private final GuideController controller;
    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    public GuideScreen(Game game) {
        super(game);
        this.controller = new GuideController(game);

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
        root.top().padTop(40).padLeft(60).padRight(60);
        uiStage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        Label title = new Label("Guide", titleStyle);
        root.add(title).colspan(2).padBottom(30).row();

        Label.LabelStyle sectionStyle = new Label.LabelStyle(trajanFont, MenuUi.ACCENT);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);

        // Key Bindings
        Label keyTitle = new Label("Key Bindings", sectionStyle);
        keyTitle.setFontScale(0.7f);
        root.add(keyTitle).left().padBottom(8).row();
        Label keyBody = new Label(controller.getKeyBindingsText(), bodyStyle);
        keyBody.setWrap(true);
        root.add(keyBody).left().width(900).padBottom(24).row();

        // Abilities
        Label abilityTitle = new Label("Abilities", sectionStyle);
        abilityTitle.setFontScale(0.7f);
        root.add(abilityTitle).left().padBottom(8).row();
        Label abilityBody = new Label(controller.getAbilitiesText(), bodyStyle);
        abilityBody.setWrap(true);
        root.add(abilityBody).left().width(900).padBottom(24).row();

        // Cheat Codes
        Label cheatTitle = new Label("Cheat Codes", sectionStyle);
        cheatTitle.setFontScale(0.7f);
        root.add(cheatTitle).left().padBottom(8).row();
        Label cheatBody = new Label(controller.getCheatCodesText(), bodyStyle);
        cheatBody.setWrap(true);
        root.add(cheatBody).left().width(900).padBottom(24).row();

        // Back
        TextButton btnBack = new AnimatedPointerButton("Back to Main Menu", skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.backToMainMenu();
            }
        });
        Table footer = new Table();
        footer.setFillParent(true);
        footer.bottom().padBottom(50);
        uiStage.addActor(footer);
        footer.add(btnBack).width(420).height(60);
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
    }
}
