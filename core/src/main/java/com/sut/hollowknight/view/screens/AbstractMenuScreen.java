package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.enums.MenuTheme;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.Assets;

public abstract class AbstractMenuScreen extends AbstractScreen {
    protected static Texture sharedMenuBackground;
    protected static Image sharedMenuBackgroundImage;

    public AbstractMenuScreen(Game game) {
        super(game);

        MenuTheme menuTheme = GameSettings.getInstance().getCurrentMenuTheme();
        sharedMenuBackground = Assets.manager.get(menuTheme.getPathToFile(), Texture.class);
        sharedMenuBackgroundImage = new Image(sharedMenuBackground);
        sharedMenuBackgroundImage.setFillParent(true);
        uiStage.addActor(sharedMenuBackgroundImage);
    }

    @Override
    public void show() {
        super.show();

        refreshBackgroundImage();
    }

    protected void reloadBackground() {
        if (sharedMenuBackground != null) {
            sharedMenuBackground.dispose();
        }

        MenuTheme currentTheme = GameSettings.getInstance().getCurrentMenuTheme();

        sharedMenuBackground = new Texture(Gdx.files.internal(currentTheme.getPathToFile()));
    }

    protected void refreshBackgroundImage() {
        reloadBackground();

        sharedMenuBackgroundImage.setDrawable(new TextureRegionDrawable(sharedMenuBackground));
    }
}
