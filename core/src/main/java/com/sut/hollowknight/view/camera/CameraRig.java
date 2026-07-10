package com.sut.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public final class CameraRig {

    private static final float LERP_FACTOR = 4f;

    private final OrthographicCamera camera;

    // World-space clamp bounds. Default to the map's 0-based pixel size;
    // rooms authored around negative coordinates widen these through
    // setWorldBounds().
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    private float shakeTimer;
    private float shakeDuration;
    private float shakeAmplitude;

    public CameraRig(OrthographicCamera camera, float mapWidthPx, float mapHeightPx) {
        this.camera = camera;
        this.minX = 0f;
        this.minY = 0f;
        this.maxX = mapWidthPx;
        this.maxY = mapHeightPx;
    }

    /** Widen the clamp region (spec: rooms with content beyond the grid). */
    public void setWorldBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // shake requests with higher amplitude override weaker ones
    public void shake(float amplitude, float duration) {
        if (shakeTimer <= 0f || amplitude >= shakeAmplitude) {
            shakeAmplitude = amplitude;
            shakeDuration = duration;
            shakeTimer = duration;
        }
    }

    /** Jump straight to the target (bounds-clamped) - used on respawn and
     *  room transitions so the camera does not glide behind the fade. */
    public void snapTo(float targetX, float targetY) {
        camera.position.set(clampX(targetX), clampY(targetY), 0);
        camera.update();
    }

    public void follow(float targetX, float targetY, float delta) {
        float lerp = 1f - (float) Math.exp(-LERP_FACTOR * delta);

        float camX = clampX(camera.position.x + (targetX - camera.position.x) * lerp);
        float camY = clampY(camera.position.y + (targetY - camera.position.y) * lerp);

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

    // Zoom-aware clamps (spec: secret room renders 1.5x bigger, i.e.
    // zoom < 1 shrinks the visible world slice). Rooms narrower than the
    // visible slice pin to their centre instead.
    private float clampX(float x) {
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        if (maxX - minX <= halfW * 2f) return (minX + maxX) / 2f;
        return Math.max(minX + halfW, Math.min(x, maxX - halfW));
    }

    private float clampY(float y) {
        float halfH = camera.viewportHeight * camera.zoom / 2f;
        if (maxY - minY <= halfH * 2f) return (minY + maxY) / 2f;
        return Math.max(minY + halfH, Math.min(y, maxY - halfH));
    }
}
