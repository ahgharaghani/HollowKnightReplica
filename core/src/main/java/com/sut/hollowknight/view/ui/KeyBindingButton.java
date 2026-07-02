package com.sut.hollowknight.view.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.view.assets.Assets;

import java.util.Objects;

public class KeyBindingButton extends Button {

    // ---- Keycap texture paths ----
    private static final String TEX_SQUARE_KEY =
        "ui/keybindings/button_skin_0004_square_key.png";
    private static final String TEX_WIDE_KEY =
        "ui/keybindings/button_skin_0003_wide_square_key.png";
    private static final String TEX_ARROW_L =
        "ui/keybindings/button_skin_0004_square_arrow.png";
    private static final String TEX_ARROW_U =
        "ui/keybindings/button_skin_0004_square_arrow_u.png";
    private static final String TEX_ARROW_D =
        "ui/keybindings/button_skin_0004_square_arrow_d.png";
    private static final String TEX_ARROW_R =
        "ui/keybindings/button_skin_0004_square_arrow_r.png";
    private static final String TEX_MOUSE_L =
        "ui/keybindings/button_mouse_left_click.png";
    private static final String TEX_MOUSE_M =
        "ui/keybindings/button_mouse_middle_click.png";
    private static final String TEX_MOUSE_R =
        "ui/keybindings/button_mouse_right_click.png";

    private static final float SQUARE_SIZE = 64f;

    private static final float WIDE_WIDTH  = 140f;
    private static final float WIDE_HEIGHT = 64f;

    private final Image backgroundImage;
    private final Label label;
    private final Stack stack;

    private boolean listening = false;
    private float pulseTime = 0f;

    public interface RebindListener {
        void onListeningStarted(KeyBindingButton button);
        void onRebindComplete(KeyBindingButton button, int newCode);
    }

    private RebindListener rebindListener;

    public KeyBindingButton(Skin skin, BitmapFont font) {
        super();

        // Use a transparent background — the keycap texture IS the
        // visual. We disable the default Button background by clearing
        // it on the style.
        ButtonStyle transparent = new ButtonStyle();
        setStyle(transparent);

        backgroundImage = new Image();
        backgroundImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        Label.LabelStyle ls = new Label.LabelStyle(font, Color.WHITE);
        label = new Label("", ls);
        label.setAlignment(Align.center);

        // Stack the image and label so the label is centered on top of
        // the keycap.
        stack = new Stack();
        stack.add(backgroundImage);
        // A nested Table lets us pad the label so it sits visually
        // centered inside the keycap (text baseline correction).
        Table labelPad = new Table();
        labelPad.add(label).padBottom(font.getCapHeight() * 0.10f);
        stack.add(labelPad);

        add(stack).size(SQUARE_SIZE, SQUARE_SIZE);
        pad(2);

        // Click → enter listening mode.
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y,
                                     int pointer, int button) {
                // Don't toggle listening on the click that re-arms us.
                if (!listening) {
                    startListening();
                    event.cancel();   // swallow the click so it doesn't
                    // immediately register as a rebind
                }
                return true;
            }
        });
    }

    public void setRebindListener(RebindListener l) {
        this.rebindListener = l;
    }

    public boolean isListening() {
        return listening;
    }

    public void startListening() {
        if (listening) return;
        listening = true;
        label.setText("?");
        if (getStage() != null) {
            getStage().addCaptureListener(captureListener);
        }
        if (rebindListener != null) {
            rebindListener.onListeningStarted(this);
        }
    }

    public void cancelListening() {
        if (!listening) return;
        listening = false;
        if (getStage() != null) {
            getStage().removeCaptureListener(captureListener);
        }
    }

    private final InputListener captureListener = new InputListener() {
        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if (!listening) return false;

            // ESC cancels the rebind.
            if (keycode == Input.Keys.ESCAPE) {
                cancelListening();
                if (rebindListener != null) {
                    rebindListener.onRebindComplete(KeyBindingButton.this, -1);
                }
                event.cancel();
                return true;
            }

            // Ignore pure modifier presses — we want them only as part
            // of "L Shift", "L Ctrl" bindings, but libGDX reports the
            // modifier itself as the keycode, which is exactly what we
            // want to bind.
            applyRebind(keycode);
            event.cancel();
            return true;
        }

        @Override
        public boolean touchDown(InputEvent event, float x, float y,
                                 int pointer, int button) {
            if (!listening) return false;
            // Convert mouse button index into our sentinel space.
            applyRebind(GameSettings.mouseCode(button));
            event.cancel();
            return true;
        }
    };

    private void applyRebind(int code) {
        listening = false;
        if (getStage() != null) {
            getStage().removeCaptureListener(captureListener);
        }
        if (rebindListener != null) {
            rebindListener.onRebindComplete(this, code);
        }
    }

    /**
     * Refresh the button's visual to reflect the given bound code.
     * Call this whenever the underlying binding changes.
     */
    public void refresh(int boundCode) {
        String texPath = pickTexture(boundCode);
        String text    = pickLabel(boundCode);
        boolean isWide = isWide(boundCode);

        // Swap the keycap texture.
        TextureRegion region = new TextureRegion(
            Assets.manager.get(texPath, com.badlogic.gdx.graphics.Texture.class));
        region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Drawable drawable = new TextureRegionDrawable(region);
        backgroundImage.setDrawable(drawable);

        // Resize the cell to match the texture's aspect.
        float w = isWide ? WIDE_WIDTH  : SQUARE_SIZE;
        float h = isWide ? WIDE_HEIGHT : SQUARE_SIZE;
        getCell(stack).size(w, h);
        invalidateHierarchy();

        label.setText(text);
    }

    private static String pickTexture(int code) {
        if (GameSettings.isMouse(code)) {
            int btn = GameSettings.toMouseButton(code);
            if (btn == Input.Buttons.LEFT)   return TEX_MOUSE_L;
            if (btn == Input.Buttons.RIGHT)  return TEX_MOUSE_R;
            if (btn == Input.Buttons.MIDDLE) return TEX_MOUSE_M;
            return TEX_MOUSE_L;  // fallback
        }
        switch (code) {
            case Input.Keys.UP:    return TEX_ARROW_U;
            case Input.Keys.DOWN:  return TEX_ARROW_D;
            case Input.Keys.LEFT:  return TEX_ARROW_L;
            case Input.Keys.RIGHT: return TEX_ARROW_R;
            // Wide keys
            case Input.Keys.SPACE:
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
            case Input.Keys.TAB:
            case Input.Keys.BACKSPACE:
            case Input.Keys.ENTER:
                return TEX_WIDE_KEY;
            default:
                return TEX_SQUARE_KEY;
        }
    }

    /** Choose the overlay text for the bound code. */
    private static String pickLabel(int code) {
        if (GameSettings.isMouse(code)) return "";
        switch (code) {
            case Input.Keys.UP:          return "";
            case Input.Keys.DOWN:        return "";
            case Input.Keys.LEFT:        return "";
            case Input.Keys.RIGHT:       return "";
            case Input.Keys.SPACE:       return "Space";
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT: return "L Shift";
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT: return "Ctrl";
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:   return "Alt";
            case Input.Keys.TAB:         return "Tab";
            case Input.Keys.BACKSPACE:   return "Bksp";
            case Input.Keys.ENTER:       return "Enter";
            case Input.Keys.ESCAPE:      return "Esc";
            default:
                return safeKeyName(code);
        }
    }

    private static String safeKeyName(int code) {
        String name = Input.Keys.toString(code);
        return (name == null || name.isEmpty() || "Unknown".equals(name))
            ? "?"
            : name;
    }

    private static boolean isWide(int code) {
        return !GameSettings.isMouse(code)
            && Objects.equals(pickTexture(code), TEX_WIDE_KEY);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (listening) {
            pulseTime += delta;
            label.getColor().a = 0.55f + 0.45f * (0.5f + 0.5f * (float) Math.sin(pulseTime * 6f));
        } else {
            label.getColor().a = 1f;
        }
    }
}
