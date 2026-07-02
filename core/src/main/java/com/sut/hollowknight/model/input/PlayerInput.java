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
    public boolean isJumpJustPressed()  { return Gdx.input.isKeyJustPressed(settings.getJumpKey()); }

    public boolean isMoving() {
        return isMoveLeftPressed() ^ isMoveRightPressed();
    }
}
