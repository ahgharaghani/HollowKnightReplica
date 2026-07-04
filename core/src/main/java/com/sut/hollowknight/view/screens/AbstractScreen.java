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
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.view.ui.BrightnessPostProcessor;

public abstract class AbstractScreen implements Screen {
    protected final Game game;

    protected OrthographicCamera worldCamera;
    protected OrthographicCamera uiCamera;
    protected Viewport worldViewport;
    protected Viewport uiViewport;

    protected BrightnessPostProcessor brightnessProcessor;

    protected Stage uiStage;

    public AbstractScreen(final Game game) {
        this.game = game;

        worldCamera = new OrthographicCamera();
        worldViewport = new FitViewport(1920, 1080, worldCamera);
        worldCamera.position.set(worldViewport.getWorldWidth() / 2, worldViewport.getWorldHeight() / 2, 0);

        uiCamera = new OrthographicCamera();
        uiViewport = new ExtendViewport(1920, 1080, uiCamera);
        uiCamera.position.set(uiViewport.getWorldWidth() / 2, uiViewport.getWorldHeight() / 2, 0);

        brightnessProcessor = new BrightnessPostProcessor();

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

    /**
     * Max simulation step. When the window is unfocused/minimized LWJGL3 stops
     * rendering, so the next render(delta) reports the whole real-time gap in one
     * shot. Feeding that raw into physics teleports bodies past thin platforms
     * (tunneling) — the knight ends up below the map. Clamp so one stalled frame
     * costs at most one normal step of movement instead of seconds' worth.
     */
    private static final float MAX_DELTA = 1f / 30f;

    @Override
    public void render(float delta) {
        if (delta > MAX_DELTA) delta = MAX_DELTA;
        updateLogic(delta);

        float brightness = GameSettings.getInstance().getBrightness();

        brightnessProcessor.begin();
        renderGraphics();
        brightnessProcessor.end(brightness);
    }

    public abstract void updateLogic(float delta);
    public abstract void renderGraphics();

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height);
        uiViewport.update(width, height, true);
        brightnessProcessor.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (brightnessProcessor != null) {
            brightnessProcessor.dispose();
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
    }
}
