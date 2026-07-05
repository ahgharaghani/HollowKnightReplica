package com.sut.hollowknight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.view.assets.HudAssets;

public class HudRenderer {

    private final HudAssets assets;

    private final ShaderProgram soulMaskShader;

    // ---- Layout ----
    private static final float UI_HEIGHT = 1080f;
    private static final float HUD_X   = 40f;   // frame left edge
    private static final float HUD_TOP = 30f;   // margin from the screen top

    private static final float FRAME_W = 257f, FRAME_H = 164f;

    // Vessel circle
    private static final float ORB_CENTER_IN_FRAME_X = 74f;
    private static final float ORB_CENTER_IN_FRAME_Y = 70f;
    private static final float ORB_W = 130f, ORB_H = 125f;

    // Health nodes
    private static final float NODE_W = 126f, NODE_H = 167f;
    private static final float NODE_GLYPH_CENTER_X = 62f;
    private static final float NODE_GLYPH_CENTER_Y = 95f;
    private static final float NODE_SPACING = 58f;           // glyphs sit side by side
    private static final float NODES_START_X = HUD_X + 215f; // first glyph center
    private static final float NODES_CENTER_Y_OFFSET = 28f;  // glyph center above orb center

    // ---- Soul behaviour ----
    private static final int SOUL_MAX = 99;
    private static final float SOUL_FILL_RATE  = 90f;   // displayed soul/s while gaining
    private static final float SOUL_DRAIN_RATE = 200f;  // displayed soul/s while spending
    private static final float SOUL_EPSILON = 0.5f;

    // ---- Node state ----
    private static final int NODE_IDLE = 0, NODE_BREAK = 1, NODE_EMPTY = 2, NODE_REFILL = 3;

    private int[] nodeStates;
    private float[] nodeTimes;
    private int prevHp = -1;

    private float displayedSoul = 0f;
    private float orbTime = 0f;

    public HudRenderer(HudAssets assets) {
        this.assets = assets;

        ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/soulmask.frag"));
        if (!shader.isCompiled()) {
            Gdx.app.error("HudRenderer",
                "Soul mask shader failed to compile:\n" + shader.getLog());
            shader.dispose();
            shader = null;
        }
        this.soulMaskShader = shader;
    }

    public void dispose() {
        if (soulMaskShader != null) {
            soulMaskShader.dispose();
        }
    }

    public void draw(SpriteBatch batch, Knight knight, float delta) {
        ensureNodes(knight);
        updateNodes(knight, delta);
        updateSoul(knight, delta);

        float frameX = HUD_X;
        float frameY = UI_HEIGHT - HUD_TOP - FRAME_H;
        float orbCenterX = frameX + ORB_CENTER_IN_FRAME_X;
        float orbCenterY = frameY + ORB_CENTER_IN_FRAME_Y;
        float orbX = orbCenterX - ORB_W / 2f;
        float orbY = orbCenterY - ORB_H / 2f;

        // 1. The frame IS the empty vessel (dark circle + crescent).
        batch.draw(assets.getHudFrame(), frameX, frameY, FRAME_W, FRAME_H);

        // 2. The full soul orb, revealed from the bottom up by the liquid tile.
        float fill = MathUtils.clamp(displayedSoul / SOUL_MAX, 0f, 1f);
        if (fill > 0.005f && soulMaskShader != null) {
            TextureRegion orb  = assets.getSoulGlow();
            TextureRegion mask = currentLiquidFrame(knight);

            batch.setShader(soulMaskShader);

            // Bind the mask tile to texture unit 1; leave unit 0 active so the
            // SpriteBatch draw below binds the orb to unit 0 as usual.
            mask.getTexture().bind(1);
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

            soulMaskShader.setUniformi("u_mask", 1);
            soulMaskShader.setUniformf("u_orbRect",
                orb.getU(), orb.getV(), orb.getU2(), orb.getV2());
            soulMaskShader.setUniformf("u_maskRect",
                mask.getU(), mask.getV(), mask.getU2(), mask.getV2());
            soulMaskShader.setUniformf("u_fill", fill);

            batch.draw(orb, orbX, orbY, ORB_W, ORB_H);
            batch.setShader(null);
        }

        // 3. Health nodes (masks).
        float glyphCenterY = orbCenterY + NODES_CENTER_Y_OFFSET;
        float nodeY = glyphCenterY - NODE_GLYPH_CENTER_Y;
        for (int i = 0; i < nodeStates.length; i++) {
            float nodeX = NODES_START_X + i * NODE_SPACING - NODE_GLYPH_CENTER_X;
            batch.draw(nodeFrame(i), nodeX, nodeY, NODE_W, NODE_H);
        }
    }

    // ---- internals ----

    private void ensureNodes(Knight knight) {
        int max = knight.getMaxMasks();
        if (nodeStates == null || nodeStates.length != max) {
            nodeStates = new int[max];
            nodeTimes = new float[max];
            prevHp = knight.getHpMasks();
            for (int i = 0; i < max; i++) {
                nodeStates[i] = i < prevHp ? NODE_IDLE : NODE_EMPTY;
                nodeTimes[i] = 0f;
            }
        }
    }

    private void updateNodes(Knight knight, float delta) {
        int hp = knight.getHpMasks();
        if (hp < prevHp) {
            for (int i = hp; i < prevHp && i < nodeStates.length; i++) {
                nodeStates[i] = NODE_BREAK;   // lost masks shatter
                nodeTimes[i] = 0f;
            }
        } else if (hp > prevHp) {
            for (int i = Math.max(prevHp, 0); i < hp && i < nodeStates.length; i++) {
                nodeStates[i] = NODE_REFILL;  // healed masks refill
                nodeTimes[i] = 0f;
            }
        }
        prevHp = hp;

        for (int i = 0; i < nodeStates.length; i++) {
            nodeTimes[i] += delta;
            if (nodeStates[i] == NODE_BREAK
                && assets.getHealthBreakAnim().isAnimationFinished(nodeTimes[i])) {
                nodeStates[i] = NODE_EMPTY;
                nodeTimes[i] = 0f;
            } else if (nodeStates[i] == NODE_REFILL
                && assets.getHealthRefillAnim().isAnimationFinished(nodeTimes[i])) {
                nodeStates[i] = NODE_IDLE;
                nodeTimes[i] = 0f;
            }
        }
    }

    private TextureRegion nodeFrame(int i) {
        switch (nodeStates[i]) {
            case NODE_BREAK:  return assets.getHealthBreakAnim().getKeyFrame(nodeTimes[i], false);
            case NODE_EMPTY:  return assets.getHealthEmpty();
            case NODE_REFILL: return assets.getHealthRefillAnim().getKeyFrame(nodeTimes[i], false);
            default:          return assets.getHealthIdleAnim().getKeyFrame(nodeTimes[i], true);
        }
    }

    private void updateSoul(Knight knight, float delta) {
        orbTime += delta;
        float target = knight.getSoulAmount();
        if (displayedSoul < target) {
            displayedSoul = Math.min(target, displayedSoul + SOUL_FILL_RATE * delta);
        } else if (displayedSoul > target) {
            displayedSoul = Math.max(target, displayedSoul - SOUL_DRAIN_RATE * delta);
        }
    }

    private TextureRegion currentLiquidFrame(Knight knight) {
        float target = knight.getSoulAmount();
        if (displayedSoul < target - SOUL_EPSILON) {
            return assets.getSoulFillAnim().getKeyFrame(orbTime, true);
        }
        if (displayedSoul > target + SOUL_EPSILON) {
            return assets.getSoulDrainAnim().getKeyFrame(orbTime, true);
        }
        return assets.getSoulIdleAnim().getKeyFrame(orbTime, true);
    }
}
