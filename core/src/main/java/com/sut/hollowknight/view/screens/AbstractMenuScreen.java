package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.enums.MenuTheme;
import com.sut.hollowknight.view.assets.Assets;

public abstract class AbstractMenuScreen extends AbstractScreen {
    protected Texture menuBackground;
    protected Image menuBackgroundImage;

    public AbstractMenuScreen(Game game) {
        super(game);

        MenuTheme menuTheme = GameSettings.getInstance().getCurrentMenuTheme();
        menuBackground = Assets.manager.get(menuTheme.getPathToFile(), Texture.class);
        menuBackgroundImage = new Image(menuBackground);
        menuBackgroundImage.setFillParent(true);
        uiStage.addActor(menuBackgroundImage);
    }

    @Override
    public void show() {
        super.show();

        refreshBackgroundImage();
    }

    protected void reloadBackground() {
        MenuTheme theme = GameSettings.getInstance().getCurrentMenuTheme();
        // Only reload if theme actually changed
        if (menuBackground != null
            && menuBackground.toString().equals(theme.getPathToFile())) {
            return;
        }
        if (menuBackground != null) {
            menuBackground.dispose();
        }
        menuBackground = new Texture(Gdx.files.internal(theme.getPathToFile()));
    }

    protected void refreshBackgroundImage() {
        reloadBackground();

        menuBackgroundImage.setDrawable(new TextureRegionDrawable(menuBackground));
    }
}
