package com.sut.hollowknight.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.npc.Zote;
import com.sut.hollowknight.view.assets.ZoteAssets;

/**
 * Draws Zote's looping animations at 12 FPS (spec):
 * Idle when resting, Talk while prompted to speak, Attack during his
 * harmless tantrum. Frames come pre-sorted from {@link ZoteAssets};
 * nothing is allocated per frame.
 */
public class ZoteRenderer {

    public static final float FPS = 12f;
    public static final float ATTACK_FPS = 8f;

    private final TextureRegion[] idle;
    private final TextureRegion[] talk;
    private final TextureRegion[] attack;

    public ZoteRenderer(ZoteAssets assets) {
        idle   = assets.getIdleFrames();
        talk   = assets.getTalkFrames();
        attack = assets.getAttackFrames();
    }

    public void draw(SpriteBatch batch, Zote zote) {
        TextureRegion[] frames = select(zote.getState());
        float fps = zote.getState() == Zote.State.ANGRY ? ATTACK_FPS : FPS;
        TextureRegion frame =
                frames[(int) (zote.getAnimTimer() * fps) % frames.length];
        float w = Zote.WIDTH;
        float h = Zote.HEIGHT;
        // Source art faces left; mirror with a negative width to flip.
        if (zote.isFacingRight()) {
            batch.draw(frame, zote.getX() + w / 2f, zote.getY(), -w, h);
        } else {
            batch.draw(frame, zote.getX() - w / 2f, zote.getY(), w, h);
        }
    }

    private TextureRegion[] select(Zote.State state) {
        switch (state) {
            case TALK:  return talk;
            case ANGRY: return attack;
            default:    return idle;
        }
    }
}
