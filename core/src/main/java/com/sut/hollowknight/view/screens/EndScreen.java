package com.sut.hollowknight.view.screens;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sut.hollowknight.controller.EndScreenController;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.ui.AnimatedPointerButton;
import com.sut.hollowknight.view.ui.AchievementToastOverlay;

/**
 * End Screen (spec: End Screen). Shown once the False Knight - this build's
 * final boss - falls. Presents the finished run's statistics:
 *
 *   - number of deaths
 *   - number of enemies felled
 *   - total time played (h:mm:ss)
 *
 * plus Restart and Main Menu buttons (spec). Follows the MainMenuScreen
 * pattern exactly: scene2d Table over the themed menu background, Trajan
 * heading font, AnimatedPointerButton for actions. All layout happens once
 * in the constructor - the render path allocates nothing.
 */
public class EndScreen extends AbstractMenuScreen {

    private final EndScreenController controller;
    /** Continues the toast queue the dying GameScreen could not finish -
     *  the victory unlocks (Completion, Speedrun, Charmed) show here. */
    private final AchievementToastOverlay achievementToasts;
    private final Skin skin;
    private final BitmapFont trajanFont;
    private final BitmapFont statsFont;

    public EndScreen(Game game) {
        super(game);
        this.controller = new EndScreenController(game);
        this.achievementToasts = new AchievementToastOverlay();
        this.skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // Same face as the main menu, two sizes: title and stat lines.
        trajanFont = MenuUi.buildTrajanFont(40);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        statsFont = MenuUi.buildTrajanFont(28);

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        uiStage.addActor(table);

        Label.LabelStyle titleStyle = new Label.LabelStyle(trajanFont, MenuUi.TEXT_LIGHT);
        Label.LabelStyle statStyle  = new Label.LabelStyle(statsFont,  MenuUi.TEXT_LIGHT);

        Label title = new Label(UiText.END_TITLE.get(), titleStyle);

        // Stats (spec: deaths, kills, total play time).
        Label deaths = new Label(
            UiText.END_DEATHS.get() + "   " + controller.getDeathCount(), statStyle);
        Label kills = new Label(
            UiText.END_KILLS.get() + "   " + controller.getEnemyKillCount(), statStyle);
        Label time = new Label(
            UiText.END_TIME.get() + "   " + formatTime(controller.getPlayTimeSeconds()),
            statStyle);

        TextButton btnRestart = new AnimatedPointerButton(UiText.RESTART.get(), skin, "headingBtn");
        TextButton btnMenu    = new AnimatedPointerButton(UiText.MAIN_MENU.get(), skin, "headingBtn");

        btnRestart.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.restart();
            }
        });
        btnMenu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                controller.backToMainMenu();
            }
        });

        table.defaults().pad(12);
        table.add(title).padBottom(48).row();
        table.add(deaths).row();
        table.add(kills).row();
        table.add(time).padBottom(48).row();
        table.add(btnRestart).width(360).height(60).row();
        table.add(btnMenu).width(360).height(60).row();
    }

    /** h:mm:ss - matches the save-slot play-time format. No allocations after construction. */
    private static String formatTime(int totalSeconds) {
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }

    @Override public void updateLogic(float delta) { }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(MenuUi.BG_DARK.r, MenuUi.BG_DARK.g, MenuUi.BG_DARK.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();

        // Toasts carried over from the victory frame (static queue).
        SpriteBatch batch = (SpriteBatch) uiStage.getBatch();
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();
        achievementToasts.draw(batch,
            uiStage.getViewport().getWorldWidth(),
            uiStage.getViewport().getWorldHeight(),
            Gdx.graphics.getDeltaTime());
        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        skin.dispose();
        trajanFont.dispose();
        statsFont.dispose();
        achievementToasts.dispose();
    }
}
