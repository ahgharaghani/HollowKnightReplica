package com.sut.hollowknight.view.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sut.hollowknight.model.Achievement;
import com.sut.hollowknight.model.AchievementsRegistry;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.Assets;

import java.util.ArrayDeque;

/**
 * Steam-style achievement toast (spec: Achievements). Slides up from the
 * bottom-right corner, holds, then slides back down. Dark slate panel,
 * Steam-grey secondary text, icon on the left. Toasts queue so several
 * unlocks in a row each get their moment.
 */
public class AchievementToastOverlay
        implements AchievementsRegistry.UnlockListener {

    // Steam notification palette.
    private static final Color BG          = new Color(0.086f, 0.125f, 0.176f, 0.98f);
    private static final Color BORDER      = new Color(0.24f, 0.32f, 0.40f, 1f);
    private static final Color TITLE_COLOR = new Color(0.90f, 0.91f, 0.92f, 1f);
    private static final Color DESC_COLOR  = new Color(0.561f, 0.596f, 0.627f, 1f);

    private static final float WIDTH  = 430f;
    private static final float HEIGHT = 96f;
    private static final float MARGIN = 22f;   // inset from the corner
    private static final float ICON   = 64f;

    private static final float SLIDE_IN_DURATION  = 0.35f;
    private static final float HOLD_DURATION      = 4f;
    private static final float SLIDE_OUT_DURATION = 0.45f;

    private static final String SFX_PATH = "sfx/deck_ui_achievement_toast.wav";

    private enum Phase { SLIDE_IN, HOLD, SLIDE_OUT }

    // STATIC on purpose: the victory frame unlocks several achievements
    // at once (Falsehood, Completion, Speedrun, Charmed) but the
    // GameScreen only lives another VICTORY_DELAY + fade ~4.7s - less
    // than ONE toast's 4.8s lifecycle. With per-instance state the queue
    // died with the screen and Charmed (Void Heart) was never shown.
    // Shared state lets the next screen's overlay (EndScreen) resume the
    // queue exactly where the old screen left off; render resources
    // (panel, fonts) stay per-instance and are disposed as before.
    private static final ArrayDeque<Achievement> queue = new ArrayDeque<>();
    private static Achievement current;
    private static Phase phase = Phase.SLIDE_IN;
    private static float timer;

    private final Texture panel;
    private final BitmapFont titleFont;
    private final BitmapFont descFont;
    private final GlyphLayout layout = new GlyphLayout(); // reused, no GC

    /** Loaded lazily; shared - the toast chime outlives any one screen. */
    private static Sound sound;

    public AchievementToastOverlay() {
        panel = buildPanel();
        titleFont = MenuUi.buildPerpetuaFont(26);
        descFont  = MenuUi.buildPerpetuaFont(21);
    }

    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        queue.add(achievement);
    }

    /** Draw mid-batch in the HUD pass; also advances the animation. */
    public void draw(SpriteBatch batch, float uiW, float uiH, float delta) {
        update(delta);
        if (current == null) return;

        float p = slideProgress();
        float x = uiW - WIDTH - MARGIN;
        float y = -HEIGHT + p * (HEIGHT + MARGIN); // rises from below screen

        batch.draw(panel, x, y, WIDTH, HEIGHT);

        if (Assets.manager != null
                && Assets.manager.isLoaded(current.getIconPath(), Texture.class)) {
            Texture icon = Assets.manager.get(current.getIconPath(), Texture.class);
            batch.draw(icon, x + 16f, y + (HEIGHT - ICON) / 2f, ICON, ICON);
        }

        float tx = x + 16f + ICON + 14f;
        titleFont.setColor(TITLE_COLOR);
        layout.setText(titleFont, current.getTitle());
        titleFont.draw(batch, layout, tx, y + HEIGHT - 20f);
        descFont.setColor(DESC_COLOR);
        layout.setText(descFont, current.getDescription());
        descFont.draw(batch, layout, tx, y + HEIGHT - 54f);
    }

    private void update(float delta) {
        if (current == null) {
            if (!queue.isEmpty()) {
                current = queue.poll();
                phase = Phase.SLIDE_IN;
                timer = 0f;
                playSound();
            }
            return;
        }
        timer += delta;
        switch (phase) {
            case SLIDE_IN:
                if (timer >= SLIDE_IN_DURATION) { phase = Phase.HOLD; timer = 0f; }
                break;
            case HOLD:
                if (timer >= HOLD_DURATION) { phase = Phase.SLIDE_OUT; timer = 0f; }
                break;
            case SLIDE_OUT:
                if (timer >= SLIDE_OUT_DURATION) current = null;
                break;
            default:
                break;
        }
    }

    private float slideProgress() {
        switch (phase) {
            case SLIDE_IN: {
                float t = Math.min(1f, timer / SLIDE_IN_DURATION);
                return t * t * (3f - 2f * t); // smoothstep ease
            }
            case SLIDE_OUT: {
                float t = Math.min(1f, timer / SLIDE_OUT_DURATION);
                return 1f - t * t * (3f - 2f * t);
            }
            default:
                return 1f;
        }
    }

    /**
     * The wav ships later (user note) - checked on each toast until it
     * appears, so dropping the file in just works with no code change.
     */
    private void playSound() {
        if (sound == null) {
            FileHandle fh = Gdx.files.internal(SFX_PATH);
            if (fh.exists()) {
                try {
                    sound = Gdx.audio.newSound(fh);
                } catch (Exception e) {
                    Gdx.app.error("AchievementToast", "Bad toast sfx", e);
                }
            }
        }
        GameSettings settings = GameSettings.getInstance();
        if (sound != null && !settings.isSfxMuted()) {
            sound.play(settings.getSfxVolume());
        }
    }

    private static Texture buildPanel() {
        int w = (int) WIDTH, h = (int) HEIGHT, r = 10;
        Pixmap pix = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pix.setBlending(Pixmap.Blending.None);
        pix.setColor(0f, 0f, 0f, 0f);
        pix.fill();
        fillRound(pix, 0, 0, w, h, r, BORDER);
        fillRound(pix, 2, 2, w - 4, h - 4, r - 1, BG);
        Texture tex = new Texture(pix);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pix.dispose();
        return tex;
    }

    private static void fillRound(Pixmap p, int x, int y, int w, int h,
                                  int r, Color c) {
        p.setColor(c);
        p.fillRectangle(x + r, y, w - 2 * r, h);
        p.fillRectangle(x, y + r, w, h - 2 * r);
        p.fillCircle(x + r, y + r, r);
        p.fillCircle(x + w - r - 1, y + r, r);
        p.fillCircle(x + r, y + h - r - 1, r);
        p.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    public void dispose() {
        panel.dispose();
        titleFont.dispose();
        descFont.dispose();
        if (sound != null) sound.dispose();
    }
}
