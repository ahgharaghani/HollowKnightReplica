package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.sut.hollowknight.view.assets.Assets;

public class LogoScreen extends AbstractScreen {
    private static final float TOTAL_TIME   = 3f;
    private static final float FADE_IN_TIME = 1f;
    private static final float FADE_OUT_TIME = 1f;

    private final SpriteBatch batch;
    private final Texture logoTexture;
    private float elapsedTime;

    public LogoScreen(Game game) {
        super(game);
        batch = new SpriteBatch();
        logoTexture = Assets.manager.get("sut_logo.png", Texture.class);
        elapsedTime = 0f;
    }

    @Override
    public void updateLogic(float delta) {
        elapsedTime += delta;
        if (elapsedTime >= TOTAL_TIME) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void renderGraphics() {
        float alpha = 1f;
        if (elapsedTime < FADE_IN_TIME) {
            alpha = Interpolation.fade.apply(0f, 1f, elapsedTime / FADE_IN_TIME);
        } else if (elapsedTime > TOTAL_TIME - FADE_OUT_TIME) {
            float progress = (elapsedTime - (TOTAL_TIME - FADE_OUT_TIME)) / FADE_OUT_TIME;
            alpha = Interpolation.fade.apply(1f, 0f, progress);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float maxW = uiViewport.getWorldWidth() * 0.6f;
        float maxH = uiViewport.getWorldHeight() * 0.6f;
        float scale = Math.min(
            Math.min(maxW / logoTexture.getWidth(), maxH / logoTexture.getHeight()), 1f);
        float logoW = logoTexture.getWidth() * scale;
        float logoH = logoTexture.getHeight() * scale;
        float x = (uiViewport.getWorldWidth() - logoW) / 2f;
        float y = (uiViewport.getWorldHeight() - logoH) / 2f;

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(logoTexture, x, y, logoW, logoH);
        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
    }
}
