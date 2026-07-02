package com.sut.hollowknight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.sut.hollowknight.model.db.GameDatabase;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.screens.LoadingScreen;

public class HollowKnight extends Game {

    @Override
    public void create() {
        Assets.manager = new AssetManager();
        GameDatabase.initialise();
        GameDatabase.loadSettings();
        this.setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        Assets.manager.dispose();
        super.dispose();
    }
}
