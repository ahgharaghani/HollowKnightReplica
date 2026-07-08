package com.sut.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public final class CameraRig {

    private static final float LERP_FACTOR = 4f;

    private final OrthographicCamera camera;
    private final float mapWidthPx;
    private final float mapHeightPx;

    private float shakeTimer;
    private float shakeDuration;
    private float shakeAmplitude;

    public CameraRig(OrthographicCamera camera, float mapWidthPx, float mapHeightPx) {
        this.camera = camera;
        this.mapWidthPx = mapWidthPx;
        this.mapHeightPx = mapHeightPx;
    }


    // shake requests with higher amplitude override weaker ones
    public void shake(float amplitude, float duration) {
        if (shakeTimer <= 0f || amplitude >= shakeAmplitude) {
            shakeAmplitude = amplitude;
            shakeDuration = duration;
            shakeTimer = duration;
        }
    }

    /** Jump straight to the target (map-clamped) - used on respawn so
     *  the camera does not glide across the map behind the fade. */
    public void snapTo(float targetX, float targetY) {
        float halfW = camera.viewportWidth  / 2f;
        float halfH = camera.viewportHeight / 2f;
        float camX = Math.max(halfW, Math.min(targetX, mapWidthPx  - halfW));
        float camY = Math.max(halfH, Math.min(targetY, mapHeightPx - halfH));
        camera.position.set(camX, camY, 0);
        camera.update();
    }

    public void follow(float targetX, float targetY, float delta) {
        float lerp = 1f - (float) Math.exp(-LERP_FACTOR * delta);

        float camX = camera.position.x + (targetX - camera.position.x) * lerp;
        float camY = camera.position.y + (targetY - camera.position.y) * lerp;

        float halfW = camera.viewportWidth  / 2f;
        float halfH = camera.viewportHeight / 2f;
        camX = Math.max(halfW, Math.min(camX, mapWidthPx  - halfW));
        camY = Math.max(halfH, Math.min(camY, mapHeightPx - halfH));

        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            if (shakeTimer < 0f) shakeTimer = 0f;
            float falloff = shakeDuration > 0f ? shakeTimer / shakeDuration : 0f;
            float strength = shakeAmplitude * falloff;
            camX += MathUtils.random(-strength, strength);
            camY += MathUtils.random(-strength, strength);
        }

        camera.position.set(camX, camY, 0);
        camera.update();
    }
}
