package com.sut.hollowknight.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.view.assets.HudAssets;

public class HudRenderer {

    private final HudAssets assets;

    // ---- Layout ----
    private static final float UI_HEIGHT = 1080f;
    private static final float HUD_X   = 40f;   // frame left edge
    private static final float HUD_TOP = 30f;   // margin from the screen top

    private static final float FRAME_W = 257f, FRAME_H = 164f;
    private static final float ORB_W   = 130f, ORB_H   = 125f;
    // Orb center inside the frame (measured from the frame art's opaque pixels).
    private static final float ORB_CENTER_IN_FRAME_X = 129f;
    private static final float ORB_CENTER_IN_FRAME_Y = 78f;  // from frame bottom

    // Health nodes
    private static final float NODE_W = 126f, NODE_H = 167f;
    private static final float NODE_GLYPH_CENTER_X = 62f;
    private static final float NODE_GLYPH_CENTER_Y = 95f;
    private static final float NODE_SPACING = 58f;           // glyphs sit side by side
    private static final float NODES_START_X = HUD_X + 215f; // first glyph center
    private static final float NODES_CENTER_Y_OFFSET = 28f;  // glyph center above orb center

    // ---- Soul behaviour ----
    private static final int SOUL_GLOW_THRESHOLD = 33;
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
    private float glowTime = 0f;

    private final TextureRegion liquidClip = new TextureRegion();

    public HudRenderer(HudAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, Knight knight, float delta) {
        ensureNodes(knight);
        updateNodes(knight, delta);
        updateSoul(knight, delta);

        float frameX = HUD_X;
        float frameY = UI_HEIGHT - HUD_TOP - FRAME_H;
        float orbX = frameX + ORB_CENTER_IN_FRAME_X - ORB_W / 2f;
        float orbY = frameY + ORB_CENTER_IN_FRAME_Y - ORB_H / 2f;

        // 1. Backboard frame
        batch.draw(assets.getHudFrame(), frameX, frameY, FRAME_W, FRAME_H);

        // 2. Glow halo when a heal / spell is affordable
        if (knight.getSoulAmount() >= SOUL_GLOW_THRESHOLD) {
            glowTime += delta;
            float pulse = 0.55f + 0.35f * MathUtils.sin(glowTime * 4f);
            batch.setColor(1f, 1f, 1f, pulse);
            batch.draw(assets.getSoulGlow(), orbX - 6f, orbY - 5f, ORB_W + 12f, ORB_H + 12f);
            batch.setColor(Color.WHITE);
        } else {
            glowTime = 0f;
        }

        // 3. Soul orb: dark empty shell, then the liquid clipped to the level.
        TextureRegion orbFrame = currentOrbFrame(knight);
        batch.setColor(0.22f, 0.25f, 0.34f, 0.9f);
        batch.draw(orbFrame, orbX, orbY, ORB_W, ORB_H);
        batch.setColor(Color.WHITE);

        float fill = MathUtils.clamp(displayedSoul / SOUL_MAX, 0f, 1f);
        int srcH = (int) (orbFrame.getRegionHeight() * fill);
        if (srcH > 0) {
            // Bottom `fill` fraction of the orb sprite = the liquid level.
            liquidClip.setRegion(
                orbFrame,
                orbFrame.getRegionX(),
                orbFrame.getRegionY() + orbFrame.getRegionHeight() - srcH,
                orbFrame.getRegionWidth(),
                srcH);
            batch.draw(liquidClip, orbX, orbY, ORB_W, ORB_H * fill);
        }

        // 4. Health nodes (masks)
        float glyphCenterY = frameY + ORB_CENTER_IN_FRAME_Y + NODES_CENTER_Y_OFFSET;
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

    private TextureRegion currentOrbFrame(Knight knight) {
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
