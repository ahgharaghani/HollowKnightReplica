package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class AbstractScreen implements Screen {
    protected final Game game;

    protected OrthographicCamera worldCamera;
    protected OrthographicCamera uiCamera;
    protected Viewport worldViewport;
    protected Viewport uiViewport;

    protected Stage uiStage;

    public AbstractScreen(final Game game) {
        this.game = game;

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(1920, 1080, worldCamera);
        worldCamera.position.set(worldViewport.getWorldWidth() / 2, worldViewport.getWorldHeight() / 2, 0);

        uiCamera = new OrthographicCamera();
        uiViewport = new ExtendViewport(1920, 1080, uiCamera);
        uiCamera.position.set(uiViewport.getWorldWidth() / 2, uiViewport.getWorldHeight() / 2, 0);

        uiStage = new Stage(uiViewport);
    }

    @Override
    public void show() {
        initCursor();

        Gdx.input.setInputProcessor(uiStage);
    }

    private void initCursor() {
        Pixmap pixmap = new Pixmap(Gdx.files.internal("ui/normal-cursor.png"));

        float scale = 2.0f;
        int newWidth = (int) (pixmap.getWidth() * scale);
        int newHeight = (int) (pixmap.getHeight() * scale);

        Pixmap convertedPixMap = new Pixmap(
            newWidth, newHeight, Pixmap.Format.RGBA8888
        );
        convertedPixMap.drawPixmap(pixmap,
            0, 0, pixmap.getWidth(), pixmap.getHeight(),
            0, 0, newWidth, newHeight
        );
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(convertedPixMap, 0, 0));
        pixmap.dispose();
        convertedPixMap.dispose();
    }

    @Override
    public void render(float delta) {
        updateLogic(delta);
        renderGraphics();
    }

    public abstract void updateLogic(float delta);
    public abstract void renderGraphics();

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        uiViewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        uiStage.dispose();
    }
}
