package com.sut.hollowknight.view.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.sut.hollowknight.view.MenuUi;

/**
 * The in-game Pause menu (spec: opened with ESC during gameplay).
 *
 * <p>A single full-screen {@link Table} living on the GameScreen's UI stage.
 * The dim backdrop is one stretched 1x1 pixel — allocated once, zero cost per
 * frame. The overlay is toggled with {@code setVisible}; when hidden, Scene2D
 * skips it entirely for both drawing and hit detection, so the running game
 * pays nothing for it.</p>
 */
public class PauseOverlay extends Table {

    private final Skin skin;
    private final BitmapFont titleFont;
    private final BitmapFont trajanFont;
    private final BitmapFont perpetuaFont;
    private final Texture dimTexture;

    /**
     * @param cheatCodesText spec requires the pause menu to list cheat codes
     * @param onContinue     resume gameplay
     * @param onSettings     open the Settings screen over the frozen game
     * @param onSaveQuit     persist the run and exit to the main menu
     */
    public PauseOverlay(String cheatCodesText,
                        final Runnable onContinue,
                        final Runnable onSettings,
                        final Runnable onSaveQuit) {
        setFillParent(true);
        setVisible(false);

        // Dim the frozen world behind the menu ("like most games do").
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.62f);
        pixmap.fill();
        dimTexture = new Texture(pixmap);
        pixmap.dispose();
        setBackground(new TextureRegionDrawable(new TextureRegion(dimTexture)));

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        titleFont    = MenuUi.buildTrajanFont(64);
        trajanFont   = MenuUi.buildTrajanFont(40);
        perpetuaFont = MenuUi.buildPerpetuaFont(28);
        MenuUi.registerHeadingStyle(skin, trajanFont);
        MenuUi.registerBodyStyle(skin, perpetuaFont);

        Label title = new Label("PAUSED", new Label.LabelStyle(titleFont, Color.WHITE));
        add(title).padBottom(40).row();

        TextButton btnContinue = new AnimatedPointerButton("Continue", skin, "headingBtn");
        TextButton btnSettings = new AnimatedPointerButton("Settings", skin, "headingBtn");
        TextButton btnSaveQuit = new AnimatedPointerButton("Save and Quit", skin, "headingBtn");

        btnContinue.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                onContinue.run();
            }
        });
        btnSettings.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                onSettings.run();
            }
        });
        btnSaveQuit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                onSaveQuit.run();
            }
        });

        defaults().pad(8);
        add(btnContinue).width(420).height(60).row();
        add(btnSettings).width(420).height(60).row();
        add(btnSaveQuit).width(420).height(60).row();

        // Cheat code reference (spec: 10 pts).
        Label cheatsTitle = new Label("Cheat Codes",
            new Label.LabelStyle(perpetuaFont, MenuUi.ACCENT));
        Label cheats = new Label(cheatCodesText,
            new Label.LabelStyle(perpetuaFont, MenuUi.TEXT_DIM));
        cheats.setAlignment(Align.center);
        add(cheatsTitle).padTop(36).row();
        add(cheats).row();
    }

    /** Must be called from the owning screen's dispose(). */
    public void dispose() {
        skin.dispose();
        titleFont.dispose();
        trajanFont.dispose();
        perpetuaFont.dispose();
        dimTexture.dispose();
    }
}
