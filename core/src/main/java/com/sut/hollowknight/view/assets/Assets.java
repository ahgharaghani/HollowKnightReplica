package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.sut.hollowknight.model.enums.MenuTheme;

public class Assets {
    public static AssetManager manager;

    public static void loadGameAssets() {
        manager.load("sut_logo.png", Texture.class);
        manager.load("ui/normal-cursor.png", Texture.class);
        for (MenuTheme theme : MenuTheme.values()) {
            manager.load(theme.getPathToFile(), Texture.class);
        }
    }

    public static boolean update() {
        return manager.update();
    }

    public static float getProgress() {
        return manager.getProgress();
    }
}
