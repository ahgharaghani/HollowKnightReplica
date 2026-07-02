package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sut.hollowknight.view.assets.Assets;

public class LoadingScreen extends AbstractScreen {
    private ShapeRenderer shapeRenderer;

    public LoadingScreen(Game game) {
        super(game);
        shapeRenderer = new ShapeRenderer();
        Assets.loadGameAssets();
    }

    @Override
    public void updateLogic(float delta) {
        if (Assets.update()) {
            game.setScreen(new LogoScreen(game));
        }
    }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(100, 100, 1720 * Assets.getProgress(), 20);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        shapeRenderer.dispose();
    }
}
