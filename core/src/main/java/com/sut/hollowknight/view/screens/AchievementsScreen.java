package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.controller.AchievementsController;
import com.sut.hollowknight.model.Achievement;
import com.sut.hollowknight.view.MenuUi;

import java.util.List;

public class AchievementsScreen extends AbstractMenuScreen {

    private final AchievementsController controller;
    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    private Table cardsTable;
    private Label counterLabel;

    public AchievementsScreen(Game game) {
        super(game);
        this.controller = new AchievementsController(game);

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
        root.top().padTop(50);
        uiStage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        Label title = new Label("Achievements", titleStyle);
        counterLabel = new Label(countText(), new Label.LabelStyle(perpetuaFont, MenuUi.ACCENT));

        root.add(title).padBottom(8).row();
        root.add(counterLabel).padBottom(20).row();

        cardsTable = new Table();
        cardsTable.top();
        rebuildCards();

        ScrollPane scrollPane = new ScrollPane(cardsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        root.add(scrollPane).width(1000).height(620).padBottom(20).row();

        TextButton btnUnlockDemo = new TextButton("Unlock Example (demo)", skin, "bodyBtn");
        btnUnlockDemo.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.unlock("false_knight");
                rebuildCards();
                counterLabel.setText(countText());
            }
        });

        TextButton btnBack = new TextButton("Back to Main Menu", skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.backToMainMenu();
            }
        });

        Table footer = new Table();
        footer.add(btnUnlockDemo).width(280).height(50).padRight(20);
        footer.add(btnBack).width(380).height(60);
        root.add(footer).padTop(10);
    }

    private String countText() {
        return String.format("%d / %d Unlocked",
            controller.getUnlockedCount(), controller.getTotalCount());
    }

    private void rebuildCards() {
        cardsTable.clear();
        for (Achievement a : controller.getAllAchievements()) {
            cardsTable.add(buildCard(a)).width(960).height(120).pad(8).row();
        }
    }

    private Table buildCard(Achievement a) {
        Table card = new Table();
        card.top().left();
        card.defaults().pad(6).left();

        Color titleColor = a.isUnlocked() ? MenuUi.TEXT_LIGHT : MenuUi.TEXT_DIM;
        Color descColor  = a.isUnlocked() ? MenuUi.ACCENT     : new Color(0.35f, 0.35f, 0.38f, 1f);

        Label.LabelStyle ts = new Label.LabelStyle(trajanFont, titleColor);
        Label.LabelStyle ds = new Label.LabelStyle(perpetuaFont, descColor);
        Label.LabelStyle ss = new Label.LabelStyle(perpetuaFont,
            a.isUnlocked() ? new Color(0.55f, 0.78f, 0.55f, 1f)
                           : new Color(0.55f, 0.55f, 0.60f, 1f));

        Label titleLabel = new Label(a.getDisplayTitle(), ts);
        titleLabel.setFontScale(0.65f);
        Label descLabel = new Label(a.getDisplayDescription(), ds);
        descLabel.setFontScale(0.95f);
        descLabel.setWrap(true);
        Label statusLabel = new Label(
            a.isUnlocked() ? "[ UNLOCKED ]" : "[ LOCKED ]", ss);
        statusLabel.setFontScale(0.9f);

        Table headerRow = new Table();
        headerRow.add(titleLabel).expandX().left();
        headerRow.add(statusLabel).right();
        card.add(headerRow).growX().padTop(10).padLeft(20).padRight(20).row();
        card.add(descLabel).growX().padLeft(20).padRight(20).padBottom(12).row();

        Color bg = a.isUnlocked()
            ? new Color(0.16f, 0.17f, 0.21f, 0.95f)
            : new Color(0.09f, 0.09f, 0.11f, 0.95f);
        card.setBackground(skin.newDrawable("white", bg));

        return card;
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
