package com.sut.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

public class RainEffect implements Disposable {

    private ShaderProgram rainShader;
    private Texture whitePixel;
    private float time = 0f;

    private static final String VERT = Gdx.files.internal("shaders/default.vert").readString();
    private static final String FRAG = Gdx.files.internal("shaders/cityoftears/rain.frag").readString();

    public RainEffect() {
        rainShader = new ShaderProgram(VERT, FRAG);
        if (!rainShader.isCompiled()) {
            Gdx.app.error("RainShader", "Shader compilation failed:\n" + rainShader.getLog());
        }

        // Generate a 1x1 white pixel texture to act as a canvas for the shader
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, 0);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        time += delta;
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        batch.enableBlending();

        batch.setShader(rainShader);
        rainShader.setUniformf("u_time", time);
        rainShader.setUniform2fv("u_resolution", new float[]{Gdx.graphics.getWidth(), Gdx.graphics.getHeight()}, 0, 2);

        batch.draw(whitePixel,
            camera.position.x - camera.viewportWidth / 2f,
            camera.position.y - camera.viewportHeight / 2f,
            camera.viewportWidth, camera.viewportHeight);

        batch.setShader(null);
    }

    @Override
    public void dispose() {
        rainShader.dispose();
        whitePixel.dispose();
    }
}
