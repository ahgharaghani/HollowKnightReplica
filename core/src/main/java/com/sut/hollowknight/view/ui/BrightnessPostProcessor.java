package com.sut.hollowknight.view.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public final class BrightnessPostProcessor {

    private FrameBuffer fbo;
    private ShaderProgram brightnessShader;
    private SpriteBatch fboBatch;

    private int screenWidth;
    private int screenHeight;
    private boolean needsRebuild = true;

    public BrightnessPostProcessor() {
        initShader();
    }

    private void initShader() {
        String vertSrc = Gdx.files.internal("shaders/default.vert").readString();
        String fragSrc = Gdx.files.internal("shaders/brightness.frag").readString();

        brightnessShader = new ShaderProgram(vertSrc, fragSrc);

        if (!brightnessShader.isCompiled()) {
            Gdx.app.error("Brightness", "Shader error:\n" + brightnessShader.getLog());
        }

        fboBatch = new SpriteBatch();
    }

    public void resize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.needsRebuild = true;

        if (fbo != null) {
            fbo.dispose();
            fbo = null;
        }
    }

    private void ensureFbo() {
        if (!needsRebuild) return;
        if (screenWidth <= 0 || screenHeight <= 0) return;

        try {
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeight, false);
            needsRebuild = false;
        } catch (Exception e) {
            Gdx.app.error("Brightness", "Failed to create FBO", e);
            fbo = null;
        }
    }

    public void begin() {
        ensureFbo();
        if (fbo == null) return;
        fbo.begin();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void end(float brightness) {
        if (fbo == null) {
            Gdx.app.error("Brightness", "FBO is null in end()!");
            return;
        }

        fbo.end();

        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

        Gdx.gl.glDisable(GL20.GL_BLEND);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fboBatch.setColor(Color.WHITE);
        fboBatch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        Texture fboTexture = fbo.getColorBufferTexture();

        fboBatch.setShader(brightnessShader);
        fboBatch.begin();

        if (!brightnessShader.isCompiled()) {
            Gdx.app.error("Brightness", "Shader FAILED to compile:\n" + brightnessShader.getLog());
        }

        // Explicitly set sampler just in case
        brightnessShader.setUniformi("u_texture", 0);
        brightnessShader.setUniformf("u_brightness", brightness);

        fboBatch.draw(fboTexture, 0, 0, screenWidth, screenHeight, 0, 0,
            fboTexture.getWidth(), fboTexture.getHeight(),
            false, true);
        fboBatch.end();

        fboBatch.setShader(null);
    }

    public void dispose() {
        if (fbo != null) fbo.dispose();
        if (brightnessShader != null) brightnessShader.dispose();
        if (fboBatch != null) fboBatch.dispose();
    }
}
