package com.sut.hollowknight.view.screens;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.sut.hollowknight.controller.AchievementsController;
import com.sut.hollowknight.model.Achievement;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.assets.ZoteAssets;

import java.util.ArrayList;
import java.util.List;

/**
 * Achievements list styled after the reference image (spec: Achievements):
 * page title with an ornamental divider, one row per achievement with a
 * framed square icon on the left (dimmed while locked), a gold title and
 * a grey description, a scroll indicator on the right, and a Back button.
 */
public class AchievementsScreen extends AbstractMenuScreen {

    private static final Color GOLD        = new Color(0.86f, 0.78f, 0.52f, 1f);
    private static final Color GOLD_LOCKED = new Color(0.52f, 0.49f, 0.40f, 1f);

    private final AchievementsController controller;
    private final Skin skin;
    private final BitmapFont trajanFont;    // page title
    private final BitmapFont rowTitleFont;  // achievement names
    private final BitmapFont perpetuaFont;  // descriptions

    /** Icon textures owned by this screen (menu-time only, disposed). */
    private final List<Texture> iconTextures = new ArrayList<>();

    public AchievementsScreen(Game game) {
        super(game);
        this.controller = new AchievementsController(game);

        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        this.trajanFont   = MenuUi.buildTrajanFont(46);
        this.rowTitleFont = MenuUi.buildTrajanFont(26);
        this.perpetuaFont = MenuUi.buildPerpetuaFont(22);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        createUI();
    }

    private void createUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(42);
        uiStage.addActor(root);

        Label title = new Label(UiText.ACHIEVEMENTS.get(),
            new Label.LabelStyle(trajanFont, MenuUi.TEXT_LIGHT));
        root.add(title).padBottom(6).row();

        // Ornamental divider under the title, like the reference image.
        TextureRegion fleur = null;
        if (Assets.manager != null
                && Assets.manager.isLoaded(ZoteAssets.FLEUR_TOP_ATLAS)) {
            TextureAtlas atlas =
                Assets.manager.get(ZoteAssets.FLEUR_TOP_ATLAS, TextureAtlas.class);
            fleur = atlas.findRegion("Fleur Top Up", 8); // fully unfurled frame
        }
        if (fleur != null) {
            Image ornament = new Image(new TextureRegionDrawable(fleur));
            ornament.setColor(1f, 1f, 1f, 0.85f);
            root.add(ornament).size(300, 44).padBottom(24).row();
        } else {
            // Fallback divider if the atlas is not loaded yet.
            Image line = new Image(skin.newDrawable("white", MenuUi.ACCENT));
            root.add(line).size(260, 2).padBottom(24).row();
        }

        Table rows = new Table();
        rows.top();
        for (Achievement a : controller.getAllAchievements()) {
            rows.add(buildRow(a)).growX().padBottom(28).row();
        }

        ScrollPane scrollPane = new ScrollPane(rows, skin);
        scrollPane.setFadeScrollBars(false);           // thin bar always visible
        scrollPane.setScrollingDisabled(true, false);  // vertical only
        root.add(scrollPane).width(880).height(540).padBottom(22).row();

        TextButton btnBack = new TextButton(UiText.BACK.get(), skin, "headingBtn");
        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.backToMainMenu();
            }
        });
        root.add(btnBack).width(260).height(58);
    }

    /** One achievement row: framed icon + gold title + grey description. */
    private Table buildRow(Achievement a) {
        boolean unlocked = a.isUnlocked();

        Texture tex = new Texture(Gdx.files.internal(a.getIconPath()));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        iconTextures.add(tex);
        Image icon = new Image(new TextureRegionDrawable(new TextureRegion(tex)));
        if (!unlocked) icon.setColor(0.42f, 0.42f, 0.48f, 0.85f); // dim locked

        // Square frame: bright while unlocked, faded while locked.
        Table frame = new Table();
        frame.setBackground(skin.newDrawable("white",
            unlocked ? new Color(0.72f, 0.72f, 0.78f, 0.95f)
                     : new Color(0.24f, 0.25f, 0.30f, 0.95f)));
        Table inner = new Table();
        inner.setBackground(skin.newDrawable("white",
            new Color(0.07f, 0.08f, 0.11f, 1f)));
        inner.add(icon).size(62, 62);
        frame.add(inner).pad(2);

        Label name = new Label(a.getTitle(), new Label.LabelStyle(
            rowTitleFont, unlocked ? GOLD : GOLD_LOCKED));
        Label desc = new Label(a.getDescription(), new Label.LabelStyle(
            perpetuaFont, unlocked ? MenuUi.TEXT_LIGHT : MenuUi.TEXT_DIM));

        Table text = new Table();
        text.left();
        text.add(name).left().row();
        text.add(desc).left().padTop(3);

        Table row = new Table();
        row.left();
        row.add(frame).size(72, 72).padRight(26);
        row.add(text).left().expandX();
        return row;
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
        rowTitleFont.dispose();
        perpetuaFont.dispose();
        for (int i = 0; i < iconTextures.size(); i++) {
            iconTextures.get(i).dispose();
        }
    }
}
