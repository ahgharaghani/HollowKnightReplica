package com.sut.hollowknight.model.map;

/**
 * A world-space rectangle drawn as opaque black until revealed (Tiled
 * "DarknessZone" object layer). The map's {@code revealedBy} property
 * names the breakable wall whose destruction fades this darkness out over
 * {@code fadeDuration} seconds (spec: hidden path reveal).
 */
public class DarknessZone {

    private final float x, y, width, height;   // world units, y-up
    private final String revealedBy;
    private final float fadeDuration;

    private boolean revealed;
    private float alpha = 1f;

    public DarknessZone(float x, float y, float width, float height,
                        String revealedBy, float fadeDuration) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.revealedBy = revealedBy == null ? "" : revealedBy;
        this.fadeDuration = fadeDuration > 0f ? fadeDuration : 0.8f;
    }

    /** Begin fading the darkness away (idempotent). */
    public void reveal() { revealed = true; }

    public void update(float delta) {
        if (!revealed || alpha <= 0f) return;
        alpha -= delta / fadeDuration;
        if (alpha < 0f) alpha = 0f;
    }

    public boolean isRevealedBy(String wallName) {
        return revealedBy.equals(wallName);
    }

    public float getAlpha()  { return alpha; }
    public float getX()      { return x; }
    public float getY()      { return y; }
    public float getWidth()  { return width; }
    public float getHeight() { return height; }
}
