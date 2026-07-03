package com.sut.hollowknight.view.renderer.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.view.assets.WingedSentryAssets;

public class JavelinRenderer {

    private final WingedSentryAssets assets;

    private static final float DRAW_WIDTH  = 583f;
    private static final float DRAW_HEIGHT = 27f;

    public JavelinRenderer(WingedSentryAssets assets) {
        this.assets = assets;
    }

    public void draw(SpriteBatch batch, Javelin javelin) {
        if (javelin.isDone()) return;

        TextureRegion frame = getCurrentFrame(javelin);
        if (frame == null) return;

        float drawX = javelin.getX() - DRAW_WIDTH / 2f;
        float drawY = javelin.getY() + (Javelin.HEIGHT - DRAW_HEIGHT) / 2f;

        if (javelin.isFacingRight()) {
            batch.draw(frame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT);
        } else {
            batch.draw(frame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT);
        }
    }

    private TextureRegion getCurrentFrame(Javelin javelin) {
        float t = javelin.getStateTime();
        Animation<TextureRegion> anim;

        switch (javelin.getState()) {
            case FLYING:
                // In-flight: loop the neutral javelin animation
                anim = assets.getJavelinNeutralAnim();
                break;
            case IMPACT:
                anim = assets.getJavelinImpactAnim();
                break;
            case STICK:
            case NEUTRAL:
                // Stuck in a wall: loop the stick frames
                anim = assets.getJavelinStickAnim();
                break;
            case SNAP:
                anim = assets.getJavelinSnapAnim();
                break;
            default:
                return null;
        }
        return anim.getKeyFrame(t);
    }
}
