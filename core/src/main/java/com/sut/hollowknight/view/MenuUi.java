package com.sut.hollowknight.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Shared helpers for building Hollow-Knight-flavoured Scene2D widgets.
 *
 * <p>The original game uses a Trajan-style serif for headings and Perpetua
 * for body text on dark navy panels. We approximate that look by generating
 * the Trajan font at multiple sizes through libGDX's FreeType integration
 * and exposing {@link TextButton.TextButtonStyle}s that use them.</p>
 */
public final class MenuUi {

    public static final Color BG_DARK = new Color(0.07f, 0.08f, 0.13f, 1f);
    public static final Color BG_PANEL = new Color(0.05f, 0.06f, 0.10f, 0.85f);
    public static final Color TEXT_LIGHT = new Color(0.92f, 0.92f, 0.94f, 1f);
    public static final Color TEXT_DIM = new Color(0.55f, 0.55f, 0.60f, 1f);
    public static final Color ACCENT = new Color(0.78f, 0.78f, 0.84f, 1f);

    private MenuUi() { }

    /** Build a Trajan font at the requested size with the Hollow Knight outline/shadow. */
    public static BitmapFont buildTrajanFont(int size) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("font/TrajanPro-Regular.ttf"));

        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.color = Color.WHITE;
        param.borderColor = Color.BLACK;
        param.borderWidth = Math.max(1, size / 24f);
        param.shadowOffsetX = Math.max(1, size / 32);
        param.shadowOffsetY = Math.max(1, size / 32);

        BitmapFont font = generator.generateFont(param);
        generator.dispose();
        return font;
    }

    /** Build a Perpetua font at the requested size for body text. */
    public static BitmapFont buildPerpetuaFont(int size) {
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("font/Perpetua-Regular.otf"));

        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.color = Color.WHITE;
        param.borderColor = new Color(0, 0, 0, 0.6f);
        param.borderWidth = Math.max(0.5f, size / 36f);

        BitmapFont font = generator.generateFont(param);
        generator.dispose();
        return font;
    }

    /** Register the generated Trajan font on the given skin and return a heading TextButtonStyle. */
    public static TextButton.TextButtonStyle registerHeadingStyle(Skin skin, BitmapFont trajanFont) {
        skin.add("trajan", trajanFont, BitmapFont.class);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
            skin.get(TextButton.TextButtonStyle.class));
        style.font = trajanFont;

        // Clear backgrounds on the new style
        style.up = null;
        style.over = null;
        style.down = null;
        style.checked = null;

        skin.add("headingBtn", style);
        return style;
    }

    /** Register a Perpetua-based body TextButtonStyle on the given skin. */
    public static TextButton.TextButtonStyle registerBodyStyle(Skin skin, BitmapFont perpetuaFont) {
        skin.add("perpetua", perpetuaFont, BitmapFont.class);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(
            skin.get(TextButton.TextButtonStyle.class));
        style.font = perpetuaFont;

        style.up = null;
        style.over = null;
        style.down = null;
        style.checked = null;

        skin.add("bodyBtn", style);
        return style;
    }

    public static TextButton.TextButtonStyle removeTextBtnBackground(Skin skin) {
        TextButton.TextButtonStyle style = skin.get(TextButton.TextButtonStyle.class);
        style.up = null;
        style.over = null;
        style.down = null;
        style.checked = null;

        return style;
    }
}
