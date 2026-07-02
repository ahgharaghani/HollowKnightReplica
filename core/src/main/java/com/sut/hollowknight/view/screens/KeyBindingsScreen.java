package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.sut.hollowknight.controller.KeyBindingsController;
import com.sut.hollowknight.controller.KeyBindingsController.BindingAction;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;
import com.sut.hollowknight.view.ui.KeyBindingButton;

import java.util.EnumMap;
import java.util.Map;

public class KeyBindingsScreen extends AbstractMenuScreen {

    private final KeyBindingsController controller;

    private Skin skin;
    private BitmapFont trajanFont;
    private BitmapFont perpetuaFont;
    private BitmapFont keycapFont;

    private final Map<BindingAction, KeyBindingButton> buttons =
        new EnumMap<>(BindingAction.class);

    public KeyBindingsScreen(Game game) {
        super(game);
        this.controller = new KeyBindingsController(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont   = MenuUi.buildTrajanFont(44);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(33);
        this.keycapFont   = MenuUi.buildPerpetuaFont(22);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(60).padLeft(120).padRight(120).padBottom(60);
        uiStage.addActor(root);

        root.add(makeFlourishedHeader("KEYBOARD", trajanFont))
            .colspan(4)
            .padBottom(50)
            .row();

        //  Two-column binding grid
        Table grid = new Table();
        grid.defaults().pad(8);

        Label.LabelStyle rowLabelStyle = new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_LIGHT);

        // Left column: Up, Down, Jump, Attack, Dash, Focus / Cast
        // Right column: Left, Right, Quick Map, Super Dash, Dream Nail, Quick Cast
        BindingAction[] leftCol = {
            BindingAction.UP,
            BindingAction.DOWN,
            BindingAction.JUMP,
            BindingAction.ATTACK,
            BindingAction.DASH,
            BindingAction.FOCUS_CAST
        };
        BindingAction[] rightCol = {
            BindingAction.LEFT,
            BindingAction.RIGHT,
            BindingAction.QUICK_MAP,
            BindingAction.SUPER_DASH,
            BindingAction.DREAM_NAIL,
            BindingAction.QUICK_CAST
        };

        for (int i = 0; i < leftCol.length; i++) {
            addBindingRow(grid, leftCol[i],  rowLabelStyle, true);
            addBindingRow(grid, rightCol[i], rowLabelStyle, false);
            grid.row();
        }
        root.add(grid).colspan(4).growX().row();

        // Inventory row, centred
        Table inventoryRow = new Table();
        inventoryRow.padTop(20).padBottom(20);
        inventoryRow.add(makeActionLabel("Inventory", rowLabelStyle)).padRight(40);
        KeyBindingButton inventoryBtn = makeKeycapButtonFor(BindingAction.INVENTORY);
        buttons.put(BindingAction.INVENTORY, inventoryBtn);
        inventoryRow.add(inventoryBtn);
        root.add(inventoryRow).colspan(4).padBottom(40).row();

        // RESET DEFAULTS
        TextButton btnReset = new AnimatedPointerButton("RESET DEFAULTS", skin, "bodyBtn");
        btnReset.getLabel().setAlignment(Align.center);
        btnReset.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.resetDefaults();
                refreshAllButtons();
            }
        });
        root.add(btnReset).colspan(4).width(280).height(50).padBottom(40).row();

        // BACK
        Table backRow = new Table();
        backRow.add(makeFlourishRule()).padRight(24).size(120, 2);
        TextButton btnBack = new AnimatedPointerButton("BACK", skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.backToSettings();
            }
        });
        // Use a wide invisible hit-target so the whole "BACK" line is clickable.
        backRow.add(btnBack).width(220).height(60);
        backRow.add(makeFlourishRule()).padLeft(24).size(120, 2);
        root.add(backRow).colspan(4).padTop(10).row();
    }

    /** Add a single (action label, keycap) cell to the grid. */
    private void addBindingRow(Table grid,
                               BindingAction action,
                               Label.LabelStyle rowLabelStyle,
                               boolean leftSide) {
        grid.add(makeActionLabel(action.label, rowLabelStyle))
            .width(220).left().padRight(20);
        KeyBindingButton btn = makeKeycapButtonFor(action);
        buttons.put(action, btn);
        grid.add(btn).padRight(leftSide ? 80 : 0);
    }

    private Label makeActionLabel(String text, Label.LabelStyle style) {
        Label l = new Label(text, style);
        l.setAlignment(Align.left);
        return l;
    }

    private KeyBindingButton makeKeycapButtonFor(final BindingAction action) {
        final KeyBindingButton btn = new KeyBindingButton(skin, keycapFont);
        btn.refresh(controller.getKey(action));
        btn.setRebindListener(new KeyBindingButton.RebindListener() {
            @Override
            public void onListeningStarted(KeyBindingButton button) {
                // no-op — the button itself shows the "?"
            }

            @Override
            public void onRebindComplete(KeyBindingButton button, int newCode) {
                if (newCode < 0) {
                    // Cancelled — restore previous visual.
                    button.refresh(controller.getKey(action));
                    return;
                }
                if (controller.isConflicting(newCode, action)) {
                    // Silently revert; future iteration could surface a toast.
                    button.refresh(controller.getKey(action));
                    return;
                }
                controller.setKey(action, newCode);
                button.refresh(newCode);
            }
        });
        return btn;
    }

    private void refreshAllButtons() {
        for (Map.Entry<BindingAction, KeyBindingButton> e : buttons.entrySet()) {
            e.getValue().refresh(controller.getKey(e.getKey()));
        }
    }

    private Actor makeFlourishedHeader(String title,
                                       BitmapFont font) {
        HorizontalGroup group = new HorizontalGroup();
        group.space(20);
        group.align(Align.center);

        // Left flourish
        group.addActor(makeFlourishRule());
        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.WHITE);
        Label titleLabel = new Label(title, titleStyle);
        titleLabel.setAlignment(Align.center);
        group.addActor(titleLabel);
        // Right flourish (mirror)
        group.addActor(makeFlourishRule());

        return group;
    }

    private Actor makeFlourishRule() {
        Texture tex = Assets.manager.get("ui/keybindings/button_skin_0004_square_key.png", Texture.class);
        Image img = new Image(tex);
        img.setColor(new Color(0.78f, 0.78f, 0.84f, 0.85f));
        Table wrapper = new Table();
        wrapper.add(img).size(120, 2).fill();
        return wrapper;
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
        perpetuaFont.dispose();
        keycapFont.dispose();
    }
}
