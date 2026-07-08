package com.sut.hollowknight.view.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Align;
import com.sut.hollowknight.controller.ZoteController;
import com.sut.hollowknight.model.npc.Zote;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.ZoteAssets;

/**
 * View half of the Zote conversation (spec: NPC Interaction - Zote).
 *
 * <p>Draws the world-space LISTEN prompt - an oval reveal done in a
 * tiny fragment shader so the art uncovers evenly from the centre with
 * a soft transparent border - and the screen-space dialogue box: a
 * soft-edged translucent black panel that grows from small, fleur
 * sweeps at its top and bottom, fast word-by-word text, and the
 * Arrow Up / Arrow Down pager centred on the bottom fleur.</p>
 */
public class ZoteDialogueOverlay {

    // ---- Prompt (world space) ----
    private static final float PROMPT_SCALE  = 1f; // native size (1:1)
    private static final float PROMPT_LIFT   = -58f;
    private static final float OVAL_MAX_DIST = 1.45f; // reaches the corners
    private static final float OVAL_FEATHER  = 0.28f; // soft oval border

    // ---- Dialogue box (screen space) ----
    private static final float BOX_HEIGHT_PCT = 0.30f;
    private static final float BOX_CENTER_Y   = 0.60f;
    private static final float BOX_MIN_SCALE  = 0.15f; // "starts small"
    private static final float BOX_ALPHA      = 0.74f;
    private static final float TEXT_PAD       = 80f;
    private static final float ARROW_FPS      = 12f;

    private final ZoteController controller;
    private final Zote zote;

    private final Texture prompt;
    private final TextureRegion[] arrowUp;
    private final TextureRegion[] arrowDown;
    private final TextureRegion[] fleurTop;
    private final TextureRegion[] fleurBottom;

    private final ShaderProgram ovalShader;
    private final BitmapFont listenFont;
    private final BitmapFont textFont;
    private final GlyphLayout layout = new GlyphLayout(); // reusable
    private Texture boxTexture; // generated soft-edged panel

    public ZoteDialogueOverlay(ZoteController controller, Zote zote,
                               ZoteAssets assets) {
        this.controller = controller;
        this.zote       = zote;
        prompt      = assets.getPrompt();
        arrowUp     = assets.getArrowUpFrames();
        arrowDown   = assets.getArrowDownFrames();
        fleurTop    = assets.getFleurTopFrames();
        fleurBottom = assets.getFleurBottomFrames();

        ShaderProgram.pedantic = false;
        ovalShader = new ShaderProgram(
                Gdx.files.internal("shaders/default.vert"),
                Gdx.files.internal("shaders/ovalreveal.frag"));
        if (!ovalShader.isCompiled()) {
            Gdx.app.error("ZoteDialogueOverlay",
                    "Oval reveal shader failed to compile:\n"
                            + ovalShader.getLog());
        }

        listenFont = MenuUi.buildTrajanFont(30);
        textFont   = MenuUi.buildPerpetuaFont(30);
        buildBoxTexture();
    }

    /**
     * Soft-edged panel, generated once. Rendered stretched; the wide
     * feather band keeps the borders "shaded" at any drawn size.
     */
    private void buildBoxTexture() {
        final int w = 512, h = 248, feather = 36;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int edge = Math.min(Math.min(px, w - 1 - px),
                                    Math.min(py, h - 1 - py));
                float a = Math.min(1f, edge / (float) feather);
                a = a * a * (3f - 2f * a); // smoothstep falloff
                pm.drawPixel(px, py, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        boxTexture = new Texture(pm);
        boxTexture.setFilter(Texture.TextureFilter.Linear,
                             Texture.TextureFilter.Linear);
        pm.dispose();
    }

    // ------------------------------------------------------------------
    // World space: the LISTEN prompt above Zote's head
    // ------------------------------------------------------------------

    /** Call inside the world batch, after Zote himself is drawn. */
    public void drawPrompt(SpriteBatch batch) {
        float p = controller.getPromptProgress();
        if (p <= 0f) return;

        float w  = prompt.getWidth() * PROMPT_SCALE;
        float h  = prompt.getHeight() * PROMPT_SCALE;
        float cx = zote.getX();
        float by = zote.getY() + Zote.HEIGHT + PROMPT_LIFT;

        // setShader flushes the batch and binds our shader immediately.
        batch.setShader(ovalShader);
        ovalShader.setUniformf("u_progress", p * OVAL_MAX_DIST);
        ovalShader.setUniformf("u_feather", OVAL_FEATHER);
        batch.draw(prompt, cx - w / 2f, by, w, h);
        batch.setShader(null); // back to the default pipeline

        // LISTEN fades in with the reveal, centred on the prompt.
        float alpha = Math.min(1f, p * 1.2f);
        listenFont.setColor(0.94f, 0.94f, 0.96f, alpha);
        layout.setText(listenFont, "LISTEN");
        listenFont.draw(batch, layout, cx - layout.width / 2f,
                by + h * 0.64f + layout.height / 2f);
    }

    // ------------------------------------------------------------------
    // Screen space: the dialogue box
    // ------------------------------------------------------------------

    /** Call inside the UI batch (HUD projection). */
    public void drawDialogue(SpriteBatch batch, float uiW, float uiH) {
        float s = controller.getBoxProgress();
        if (s <= 0f) return;
        float k = s * s * (3f - 2f * s); // smoothstep pop

        // The panel spans the native top fleur, so the ornament fits
        // the box exactly as in the original game.
        float fullW = fleurTop[fleurTop.length - 1].getRegionWidth() + 36f;
        float fullH = uiH * BOX_HEIGHT_PCT;
        float scale = BOX_MIN_SCALE + (1f - BOX_MIN_SCALE) * k;
        float w  = fullW * scale;
        float h  = fullH * scale;
        float cx = uiW / 2f;
        float cy = uiH * BOX_CENTER_Y;

        // Translucent black panel with shaded borders.
        batch.setColor(0f, 0f, 0f, BOX_ALPHA * k);
        batch.draw(boxTexture, cx - w / 2f, cy - h / 2f, w, h);
        batch.setColor(Color.WHITE);

        // Fleur sweeps track the pop-up frame for frame, so they play
        // forward while opening and in reverse while closing.
        drawFleur(batch, fleurTop,    s, k, cx, cy + h / 2f);
        drawFleur(batch, fleurBottom, s, k, cx, cy - h / 2f);

        ZoteController.Phase phase = controller.getPhase();
        boolean showText = phase == ZoteController.Phase.TYPING
                || phase == ZoteController.Phase.WAIT_INPUT
                || phase == ZoteController.Phase.ARROW_DOWN;
        if (showText) {
            textFont.setColor(0.92f, 0.92f, 0.94f, k);
            textFont.draw(batch, controller.getVisibleText(),
                    cx - fullW / 2f + TEXT_PAD, cy + fullH / 2f - TEXT_PAD,
                    fullW - 2f * TEXT_PAD, Align.center, true);
        }

        // Pager arrow, centred on the middle of the bottom fleur.
        float arrowCy = cy - h / 2f;
        if (phase == ZoteController.Phase.WAIT_INPUT) {
            // Plays once and holds its final frame - no looping.
            int idx = Math.min(arrowUp.length - 1,
                    (int) (controller.getPhaseTimer() * ARROW_FPS));
            drawArrow(batch, arrowUp[idx], cx, arrowCy);
        } else if (phase == ZoteController.Phase.ARROW_DOWN) {
            int idx = Math.min(arrowDown.length - 1,
                    (int) (controller.getPhaseTimer() * ARROW_FPS));
            drawArrow(batch, arrowDown[idx], cx, arrowCy);
        }
    }

    private void drawFleur(SpriteBatch batch, TextureRegion[] frames,
                           float progress, float alpha,
                           float cx, float cy) {
        int idx = Math.min(frames.length - 1,
                (int) (progress * frames.length));
        TextureRegion f = frames[idx];
        // Native size (1:1) - the art is rendered exactly as authored.
        float fw = f.getRegionWidth();
        float fh = f.getRegionHeight();
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(f, cx - fw / 2f, cy - fh / 2f, fw, fh);
        batch.setColor(Color.WHITE);
    }

    private void drawArrow(SpriteBatch batch, TextureRegion f,
                           float cx, float cy) {
        // Native size (1:1) - the art is rendered exactly as authored.
        float aw = f.getRegionWidth();
        float ah = f.getRegionHeight();
        batch.draw(f, cx - aw / 2f, cy - ah / 2f, aw, ah);
    }

    public void dispose() {
        ovalShader.dispose();
        listenFont.dispose();
        textFont.dispose();
        boxTexture.dispose();
    }
}
