package com.sut.hollowknight.view.screens;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.controller.GuideController;
import com.sut.hollowknight.controller.KeyBindingsController;
import com.sut.hollowknight.controller.KeyBindingsController.BindingAction;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;
import com.sut.hollowknight.view.ui.KeyBindingButton;

/**
 * Guide hub: a landing menu with three sections (Key Bindings, Abilities,
 * Cheat Codes). Each section replaces the landing view and offers a BACK
 * button that returns to it. The Key Bindings section renders the current
 * bindings with the same keycap art used by the Key Bindings screen.
 */
public class GuideScreen extends AbstractMenuScreen {

    private final GuideController controller;
    /** Read-only lookups of the live bindings (never rebinds from here). */
    private final KeyBindingsController bindings;

    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;
    private BitmapFont keycapFont;

    /** Landing view: the three section options. */
    private Table menuRoot;
    /** Detail view: rebuilt each time a section is opened. */
    private Table sectionRoot;

    public GuideScreen(Game game) {
        super(game);
        this.controller = new GuideController(game);
        this.bindings = new KeyBindingsController(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont = MenuUi.buildTrajanFont(44);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(40);
        this.keycapFont = MenuUi.buildPerpetuaFont(18);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    // ---------------------------------------------------------------- landing

    private void createUI() {
        menuRoot = new Table();
        menuRoot.setFillParent(true);
        menuRoot.top().padTop(60);
        uiStage.addActor(menuRoot);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, Color.WHITE);
        menuRoot.add(new Label(UiText.GUIDE.get(), titleStyle)).padBottom(60).row();

        TextButton btnKeys = new AnimatedPointerButton(UiText.KEY_BINDINGS.get(), skin, "headingBtn");
        btnKeys.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                openKeyBindingsSection();
            }
        });
        menuRoot.add(btnKeys).width(420).height(60).padBottom(24).row();

        TextButton btnAbilities = new AnimatedPointerButton(UiText.ABILITIES.get(), skin, "headingBtn");
        btnAbilities.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                openTextSection(UiText.ABILITIES.get(), controller.getAbilitiesText());
            }
        });
        menuRoot.add(btnAbilities).width(420).height(60).padBottom(24).row();

        TextButton btnCheats = new AnimatedPointerButton(UiText.CHEAT_CODES.get(), skin, "headingBtn");
        btnCheats.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                openTextSection(UiText.CHEAT_CODES.get(), controller.getCheatCodesText());
            }
        });
        menuRoot.add(btnCheats).width(420).height(60).padBottom(24).row();

        TextButton btnBack = new AnimatedPointerButton(UiText.BACK_TO_MAIN_MENU.get(), skin, "headingBtn");
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

        // Detail container; populated on demand by the open* methods.
        sectionRoot = new Table();
        sectionRoot.setFillParent(true);
        sectionRoot.setVisible(false);
        uiStage.addActor(sectionRoot);

        this.footer = footer;
    }

    /** The landing footer (hidden while a section is open). */
    private Table footer;

    // ---------------------------------------------------------------- sections

    /** Swap the landing view for a freshly-built section view. */
    private void beginSection(String title) {
        menuRoot.setVisible(false);
        footer.setVisible(false);
        sectionRoot.clearChildren();
        sectionRoot.setVisible(true);
        sectionRoot.top().padTop(50).padLeft(120).padRight(120);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, MenuUi.ACCENT);
        Label heading = new Label(title, titleStyle);
        heading.setFontScale(0.8f);
        sectionRoot.add(heading).padBottom(40).row();
    }

    /** Append the BACK row and finish the section. */
    private void endSection() {
        TextButton btnBack = new AnimatedPointerButton(UiText.BACK.get(), skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                sectionRoot.setVisible(false);
                menuRoot.setVisible(true);
                footer.setVisible(true);
            }
        });
        sectionRoot.add(btnBack).width(300).height(60).padTop(40).row();
    }

    /** Abilities / Cheat Codes: a wrapped body-text section. */
    private void openTextSection(String title, String body) {
        beginSection(title);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);
        Label bodyLabel = new Label(body, bodyStyle);
        bodyLabel.setWrap(true);
        sectionRoot.add(bodyLabel).width(1000).row();
        endSection();
    }

    /** Key Bindings: live bindings drawn with the real keycap art. */
    private void openKeyBindingsSection() {
        beginSection(UiText.KEY_BINDINGS.get());

        Table grid = new Table();
        grid.defaults().pad(4);
        Label.LabelStyle rowStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);

        BindingAction[] actions = BindingAction.values();
        boolean left = true;
        for (BindingAction action : actions) {
            addKeycapRow(grid, rowStyle, action.label.get(), bindings.getKey(action), left);
            if (!left) grid.row();
            left = !left;
        }
        // Odd action count: Pause fills the dangling right-hand cell.
        addKeycapRow(grid, rowStyle, UiText.ACTION_PAUSE.get(), Input.Keys.ESCAPE, left);
        if (left) grid.row();

        sectionRoot.add(grid).row();
        endSection();
    }

    /** One "label + keycap" pair; two pairs form a grid row. */
    private void addKeycapRow(Table grid, Label.LabelStyle style,
                              String actionLabel, int code, boolean leftColumn) {
        Label label = new Label(actionLabel, style);
        grid.add(label).right().padRight(16).minWidth(200);

        // Same widget + textures as the Key Bindings screen, but inert:
        // touch is disabled so it can never enter rebind-listening mode.
        KeyBindingButton cap = new KeyBindingButton(skin, keycapFont);
        cap.refresh(code);
        cap.setTouchable(Touchable.disabled);
        grid.add(cap).left().minWidth(150).padRight(leftColumn ? 100 : 0);
    }

    // ---------------------------------------------------------------- render

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
        keycapFont.dispose();
    }
}
