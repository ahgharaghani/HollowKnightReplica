package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.controller.StartGameController;
import com.sut.hollowknight.model.SaveSlot;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;

public class StartGameScreen extends AbstractMenuScreen {

    private final StartGameController controller;
    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;

    public StartGameScreen(Game game) {
        super(game);
        this.controller = new StartGameController(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont = MenuUi.buildTrajanFont(48);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(22);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(60);
        uiStage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        Label title = new Label("Start Game", titleStyle);
        root.add(title).padBottom(50).row();

        // --- Save slots ---
        Table slotTable = new Table();
        for (SaveSlot slot : controller.getSaveSlots()) {
            slotTable.add(buildSlotRow(slot)).width(720).height(96).pad(8).row();
        }
        root.add(slotTable).padBottom(40).row();

        // --- New Game + Back ---
        TextButton btnNew = new TextButton("New Game", skin, "headingBtn");
        TextButton btnBack = new TextButton("Back", skin, "headingBtn");

        btnNew.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.startNewGame();
            }
        });
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.backToMainMenu();
            }
        });

        HorizontalGroup footer = new HorizontalGroup();
        footer.space(40);
        footer.addActor(btnNew);
        footer.addActor(btnBack);
        root.add(footer).padTop(20);
    }

    private Table buildSlotRow(SaveSlot slot) {
        Table row = new Table();

        Label.LabelStyle slotTitleStyle =
            new Label.LabelStyle(trajanFont, slot.isEmpty() ? MenuUi.TEXT_DIM : MenuUi.TEXT_LIGHT);
        Label.LabelStyle slotSummaryStyle =
            new Label.LabelStyle(perpetuaFont, slot.isEmpty() ? MenuUi.TEXT_DIM : MenuUi.ACCENT);

        Label titleLabel = new Label(String.format("Slot %d", slot.getIndex()), slotTitleStyle);
        Label summaryLabel = new Label(slot.getSummary(), slotSummaryStyle);
        summaryLabel.setFontScale(0.9f);

        row.add(titleLabel).left().padLeft(28).expandX().row();
        row.add(summaryLabel).left().padLeft(28).padTop(6).expandX().row();
        row.left();
        row.setBackground(skin.newDrawable("white",
            slot.isEmpty() ? new Color(0.10f, 0.10f, 0.14f, 0.85f)
                           : new Color(0.13f, 0.14f, 0.20f, 0.85f)));

        row.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (slot.isEmpty()) {
                    controller.startNewGame();
                } else {
                    controller.loadSaveSlot(slot.getIndex());
                }
            }
        });

        return row;
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
