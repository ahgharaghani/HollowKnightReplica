package com.sut.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.screens.LoadingScreen;

public class HollowKnight extends Game {
    @Override
    public void create () {
        // Initialize the global AssetManager
        Assets.manager = new AssetManager();

        // Start by showing the loading screen
        this.setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose () {
        Assets.manager.dispose();
        super.dispose();
    }
}
