package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.sut.hollowknight.model.charms.Charm;
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
        manager.load("animation/knight/Run.atlas", TextureAtlas.class);
        manager.load("animation/knight/Airborne.atlas", TextureAtlas.class);
        manager.load("animation/knight/Land.atlas", TextureAtlas.class);

        // Knight animations — new (slash family, dash family, wall family, etc.)
        manager.load("animation/knight/Wall Slide.atlas",       TextureAtlas.class);
        manager.load("animation/knight/Wall Slash.atlas",       TextureAtlas.class);
        manager.load("animation/knight/Wall Jump.atlas",        TextureAtlas.class);
        manager.load("animation/knight/Slash.atlas",            TextureAtlas.class);
        manager.load("animation/knight/Slash Effect.atlas",     TextureAtlas.class);
        manager.load("animation/knight/Slash Alt.atlas",        TextureAtlas.class);
        manager.load("animation/knight/Slash Effect Alt.atlas", TextureAtlas.class);
        manager.load("animation/knight/Dash.atlas",             TextureAtlas.class);
        manager.load("animation/knight/Dash Effect.atlas",      TextureAtlas.class);
        manager.load("animation/knight/Dash Down.atlas",        TextureAtlas.class);
        manager.load("animation/knight/Dash Down Land.atlas",   TextureAtlas.class);
        manager.load("animation/knight/Death.atlas",            TextureAtlas.class);
        manager.load("animation/knight/Double Jump.atlas",      TextureAtlas.class);
        manager.load("animation/knight/Down Slash.atlas",       TextureAtlas.class);
        manager.load("animation/knight/Down Slash Effect.atlas",TextureAtlas.class);
        manager.load("animation/knight/Idle Low Health.atlas",  TextureAtlas.class);
        manager.load("animation/knight/Recoil.atlas",           TextureAtlas.class);
        manager.load("animation/knight/Up Slash.atlas",         TextureAtlas.class);
        manager.load("animation/knight/Up Slash Effect.atlas",  TextureAtlas.class);
        manager.load("animation/knight/Focus.atlas",           TextureAtlas.class);
        manager.load("animation/knight/Focus End.atlas",       TextureAtlas.class);
        manager.load("animation/knight/Fireball Antic.atlas",  TextureAtlas.class);
        manager.load("animation/knight/Fireball Cast.atlas",   TextureAtlas.class);
        manager.load("animation/knight/Scream Start.atlas",    TextureAtlas.class);
        manager.load("animation/knight/Scream.atlas",           TextureAtlas.class);
        manager.load("animation/knight/Scream End.atlas",       TextureAtlas.class);

        manager.load("animation/effects/RainGlow1.atlas", TextureAtlas.class);
        manager.load("animation/effects/RainGlow2.atlas", TextureAtlas.class);

        WingedSentryAssets.loadAll(manager);
        TiktikAssets.loadAll(manager);
        HuskHornheadAssets.loadAll(manager);
        CrystalGuardianAssets.loadAll(manager);
        VengefulSpiritAssets.loadAll(manager);
        HowlingWraithAssets.loadAll(manager);
        HudAssets.loadAll(manager);

        manager.load("ui/keybindings/button_skin_0004_square_key.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0003_wide_square_key.png", Texture.class);

        manager.load("ui/keybindings/button_skin_0004_square_arrow.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_u.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_d.png", Texture.class);
        manager.load("ui/keybindings/button_skin_0004_square_arrow_r.png", Texture.class);

        manager.load("ui/keybindings/button_mouse_left_click.png", Texture.class);
        manager.load("ui/keybindings/button_mouse_middle_click.png", Texture.class);
        manager.load("ui/keybindings/button_mouse_right_click.png", Texture.class);

        // ---- Inventory menu & charms (spec: Charms & Inventory System) ----
        manager.load(InventoryAssets.FLEUR_CORNER_ATLAS, TextureAtlas.class);
        manager.load(InventoryAssets.FLEUR_TOP_ATLAS, TextureAtlas.class);
        manager.load(InventoryAssets.FLEUR_BOTTOM_ATLAS, TextureAtlas.class);
        manager.load(InventoryAssets.SIDE_ARROW_ATLAS, TextureAtlas.class);
        manager.load(InventoryAssets.BACKBOARD_PNG, Texture.class);
        manager.load(InventoryAssets.NOTCH_PNG, Texture.class);
        manager.load(InventoryAssets.COST_LIT_PNG, Texture.class);
        manager.load(InventoryAssets.COST_UNLIT_PNG, Texture.class);
        for (Charm charm : Charm.values()) {
            manager.load(charm.getIconPath(), Texture.class);
        }
    }

    public static boolean update() {
        return manager.update();
    }

    public static float getProgress() {
        return manager.getProgress();
    }
}
