package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.db.GameDatabase;
import com.sut.hollowknight.view.screens.SettingsScreen;

import java.util.Arrays;
import java.util.List;

public class KeyBindingsController {

    public enum BindingAction {
        UP("Up"),
        DOWN("Down"),
        LEFT("Left"),
        RIGHT("Right"),
        JUMP("Jump"),
        ATTACK("Attack"),
        DASH("Dash"),
        FOCUS_CAST("Focus / Cast"),
        INVENTORY("Inventory"),
        QUICK_MAP("Quick Map"),
        SUPER_DASH("Super Dash"),
        DREAM_NAIL("Dream Nail"),
        QUICK_CAST("Quick Cast");

        public final String label;

        BindingAction(String label) {
            this.label = label;
        }
    }

    private final Game game;
    private final GameSettings settings;

    public KeyBindingsController(Game game) {
        this.game = game;
        this.settings = GameSettings.getInstance();
    }

    public int getKey(BindingAction action) {
        switch (action) {
            case UP:          return settings.getMoveUpKey();
            case DOWN:        return settings.getMoveDownKey();
            case LEFT:        return settings.getMoveLeftKey();
            case RIGHT:       return settings.getMoveRightKey();
            case JUMP:        return settings.getJumpKey();
            case ATTACK:      return settings.getAttackKey();
            case DASH:        return settings.getDashKey();
            case FOCUS_CAST:  return settings.getFocusCastKey();
            case INVENTORY:   return settings.getInventoryKey();
            case QUICK_MAP:   return settings.getQuickMapKey();
            case SUPER_DASH:  return settings.getSuperDashKey();
            case DREAM_NAIL:  return settings.getDreamNailKey();
            case QUICK_CAST:  return settings.getQuickCastKey();
            default:          return Input.Keys.UNKNOWN;
        }
    }

    public void setKey(BindingAction action, int keyCode) {
        switch (action) {
            case UP:          settings.setMoveUpKey(keyCode); break;
            case DOWN:        settings.setMoveDownKey(keyCode); break;
            case LEFT:        settings.setMoveLeftKey(keyCode); break;
            case RIGHT:       settings.setMoveRightKey(keyCode); break;
            case JUMP:        settings.setJumpKey(keyCode); break;
            case ATTACK:      settings.setAttackKey(keyCode); break;
            case DASH:        settings.setDashKey(keyCode); break;
            case FOCUS_CAST:  settings.setFocusCastKey(keyCode); break;
            case INVENTORY:   settings.setInventoryKey(keyCode); break;
            case QUICK_MAP:   settings.setQuickMapKey(keyCode); break;
            case SUPER_DASH:  settings.setSuperDashKey(keyCode); break;
            case DREAM_NAIL:  settings.setDreamNailKey(keyCode); break;
            case QUICK_CAST:  settings.setQuickCastKey(keyCode); break;
        }
        save();
    }

    public boolean isConflicting(int code, BindingAction except) {
        for (BindingAction a : BindingAction.values()) {
            if (a == except) continue;
            if (getKey(a) == code) return true;
        }
        return false;
    }

    public List<BindingAction> allActions() {
        return Arrays.asList(BindingAction.values());
    }

    public void resetDefaults() {
        settings.setDefaultKeyBindings();
        save();
    }

    public void backToSettings() {
        game.setScreen(new SettingsScreen(game));
    }

    private void save() {
        GameDatabase.saveSettings();
    }
}
