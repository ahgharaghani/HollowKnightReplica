package com.sut.hollowknight.model.input;

import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.model.GameSettings;

public final class PlayerInput {

    private final GameSettings settings;

    public PlayerInput() {
        this.settings = GameSettings.getInstance();
    }

    public boolean isMoveLeftPressed()  { return Gdx.input.isKeyPressed(settings.getMoveLeftKey()); }
    public boolean isMoveRightPressed() { return Gdx.input.isKeyPressed(settings.getMoveRightKey()); }
    public boolean isMoveUpPressed()    { return Gdx.input.isKeyPressed(settings.getMoveUpKey()); }
    public boolean isMoveDownPressed()  { return Gdx.input.isKeyPressed(settings.getMoveDownKey()); }

    public boolean isJumpJustPressed()  { return Gdx.input.isKeyJustPressed(settings.getJumpKey()); }
    public boolean isJumpPressed()      { return Gdx.input.isKeyPressed(settings.getJumpKey()); }

    public boolean isAttackJustPressed() {
        int key = settings.getAttackKey();
        if (GameSettings.isMouse(key)) {
            int btn = GameSettings.toMouseButton(key);
            return Gdx.input.justTouched() && Gdx.input.isButtonPressed(btn);
        }

        return Gdx.input.isKeyJustPressed(key);
    }

    public boolean isDashJustPressed() {
        return Gdx.input.isKeyJustPressed(settings.getDashKey());
    }

    public boolean isMoving() {
        return isMoveLeftPressed() ^ isMoveRightPressed();
    }
}
