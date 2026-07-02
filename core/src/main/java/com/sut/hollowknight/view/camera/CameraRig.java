package com.sut.hollowknight.view.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;

public final class CameraRig {

    private static final float LERP_FACTOR = 4f;

    private final OrthographicCamera camera;
    private final float mapWidthPx;
    private final float mapHeightPx;

    public CameraRig(OrthographicCamera camera, float mapWidthPx, float mapHeightPx) {
        this.camera = camera;
        this.mapWidthPx = mapWidthPx;
        this.mapHeightPx = mapHeightPx;
    }

    public void follow(float targetX, float targetY, float delta) {
        float lerp = LERP_FACTOR * delta;

        float camX = camera.position.x + (targetX - camera.position.x) * lerp;
        float camY = camera.position.y + (targetY - camera.position.y) * lerp;

        float halfW = camera.viewportWidth  / 2f;
        float halfH = camera.viewportHeight / 2f;
        camX = Math.max(halfW, Math.min(camX, mapWidthPx  - halfW));
        camY = Math.max(halfH, Math.min(camY, mapHeightPx - halfH));

        camera.position.set(camX, camY, 0);
        camera.update();
    }
}
