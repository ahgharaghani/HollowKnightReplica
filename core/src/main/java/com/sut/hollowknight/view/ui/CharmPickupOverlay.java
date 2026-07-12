package com.sut.hollowknight.view.ui;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.controller.CharmPickupController;
import com.sut.hollowknight.model.map.CharmPickup;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.InventoryAssets;

import java.util.List;

/**
 * Draws world charm pickups: the bobbing charm icon plus the COLLECT
 * prompt (spec: "show the same prompt used for the dialogue prompt and
 * write COLLECT as its text"). The prompt is the exact Zote treatment -
 * same prompt art, same oval-reveal shader, same Trajan face - with only
 * the caption changed.
 */
public class CharmPickupOverlay {

    // Mirrors ZoteDialogueOverlay so the two prompts feel identical.
    private static final float PROMPT_SCALE  = 1f;   // native size (1:1)
    private static final float PROMPT_LIFT   = 46f;  // above the icon
    private static final float OVAL_MAX_DIST = 1.45f;
    private static final float OVAL_FEATHER  = 0.28f;

    private static final float ICON_HEIGHT   = 52f;  // world px
    private static final float BOB_AMPLITUDE = 5f;
    private static final float BOB_SPEED     = 2.2f;

    private final CharmPickupController controller;
    private final Texture prompt;             // owned by the asset manager
    private final InventoryAssets icons;      // charm icon lookup
    private final ShaderProgram ovalShader;   // owned here
    private final BitmapFont collectFont;     // owned here
    private final GlyphLayout layout = new GlyphLayout(); // reusable

    private float time;   // render-only bob clock

    public CharmPickupOverlay(CharmPickupController controller,
                              Texture prompt, InventoryAssets icons) {
        this.controller = controller;
        this.prompt = prompt;
        this.icons = icons;

        ShaderProgram.pedantic = false;
        ovalShader = new ShaderProgram(
                Gdx.files.internal("shaders/default.vert"),
                Gdx.files.internal("shaders/ovalreveal.frag"));
        if (!ovalShader.isCompiled()) {
            Gdx.app.error("CharmPickupOverlay",
                    "Oval reveal shader failed to compile:\n"
                            + ovalShader.getLog());
        }
        collectFont = MenuUi.buildTrajanFont(30);
    }

    /** Call inside the world batch (same pass as Zote's prompt). */
    public void draw(SpriteBatch batch) {
        List<CharmPickup> pickups = controller.getPickups();
        if (pickups.isEmpty()) return;
        time += Gdx.graphics.getDeltaTime();

        for (int i = 0; i < pickups.size(); i++) {
            CharmPickup pickup = pickups.get(i);
            if (pickup.isCollected()) continue;

            // The charm itself, bobbing gently on the spot.
            Texture icon = icons.getCharmIcon(pickup.getCharm());
            float ih = ICON_HEIGHT;
            float iw = ih * icon.getWidth() / (float) icon.getHeight();
            float bob = MathUtils.sin(time * BOB_SPEED) * BOB_AMPLITUDE;
            batch.draw(icon, pickup.getX() - iw / 2f,
                pickup.getY() - ih / 2f + bob, iw, ih);

            float p = pickup.getPromptProgress();
            if (p <= 0f) continue;

            // Prompt art through the oval-reveal shader (Zote treatment).
            float w  = prompt.getWidth() * PROMPT_SCALE;
            float h  = prompt.getHeight() * PROMPT_SCALE;
            float cx = pickup.getX();
            float by = pickup.getY() + PROMPT_LIFT;

            batch.setShader(ovalShader);
            ovalShader.setUniformf("u_progress", p * OVAL_MAX_DIST);
            ovalShader.setUniformf("u_feather", OVAL_FEATHER);
            batch.draw(prompt, cx - w / 2f, by, w, h);
            batch.setShader(null);

            // COLLECT fades in with the reveal, centred on the prompt.
            float alpha = Math.min(1f, p * 1.2f);
            collectFont.setColor(0.94f, 0.94f, 0.96f, alpha);
            layout.setText(collectFont, UiText.COLLECT.get());
            collectFont.draw(batch, layout, cx - layout.width / 2f,
                    by + h * 0.64f + layout.height / 2f);
        }
    }

    public void dispose() {
        ovalShader.dispose();
        collectFont.dispose();
        // prompt + icons live in the asset manager - not ours to dispose.
    }
}
