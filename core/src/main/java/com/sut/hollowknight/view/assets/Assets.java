package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.sut.hollowknight.model.enums.MenuTheme;

public class Assets {
    public static AssetManager manager;

    public static void loadGameAssets() {
        manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));

        // UI & menus
        manager.load("sut_logo.png", Texture.class);
        manager.load("ui/normal-cursor.png", Texture.class);
        for (MenuTheme theme : MenuTheme.values()) {
            manager.load(theme.getPathToFile(), Texture.class);
        }
        manager.load("ui/menu/pointer/MainMenuPointer.atlas", TextureAtlas.class);

        // Game map
        manager.load("CityOfTears.tmx", TiledMap.class);

        // Knight animations
        manager.load("animation/knight/KnightIdle.atlas", TextureAtlas.class);
        manager.load("animation/knight/KnightWalk.atlas", TextureAtlas.class);
        manager.load("animation/knight/Airborne.atlas", TextureAtlas.class);
        manager.load("animation/knight/Land.atlas", TextureAtlas.class);

        manager.load("animation/effects/RainGlow1.atlas", TextureAtlas.class);
        manager.load("animation/effects/RainGlow2.atlas", TextureAtlas.class);

        WingedSentryAssets.loadAll(manager);

        manager.load("ui/keybindings/button_skin_0004_square_key.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0003_wide_square_key.png", Texture.class);

        manager.load("ui/keybindings/button_skin_0004_square_arrow.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_u.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_d.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_r.png", Texture.class);

        manager.load("ui/keybindings/button_mouse_left_click.png", Texture.class);
        manager.load("ui/keybindings/button_mouse_middle_click.png", Texture.class);
        manager.load("ui/keybindings/button_mouse_right_click.png", Texture.class);
    }

    public static boolean update() {
        return manager.update();
    }

    public static float getProgress() {
        return manager.getProgress();
    }
}
