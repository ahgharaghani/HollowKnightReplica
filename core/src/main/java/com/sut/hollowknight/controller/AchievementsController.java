package com.sut.hollowknight.controller;

import com.badlogic.gdx.Game;
import com.sut.hollowknight.model.Achievement;
import com.sut.hollowknight.model.AchievementsRegistry;
import com.sut.hollowknight.view.screens.MainMenuScreen;

import java.util.List;

public class AchievementsController {

    private final Game game;
    private final AchievementsRegistry registry;

    public AchievementsController(Game game) {
        this.game = game;
        this.registry = AchievementsRegistry.getInstance();
    }

    public List<Achievement> getAllAchievements() {
        return registry.all();
    }

    public int getUnlockedCount() {
        return registry.unlockedCount();
    }

    public int getTotalCount() {
        return registry.totalCount();
    }

    public List<Achievement> unlock(String id) {
        registry.unlock(id);
        return registry.all();
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }
}
