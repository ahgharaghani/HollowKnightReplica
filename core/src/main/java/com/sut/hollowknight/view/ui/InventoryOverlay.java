package com.sut.hollowknight.view.ui;

import com.sut.hollowknight.model.enums.UiText;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.sut.hollowknight.model.GameData;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.charms.Charm;
import com.sut.hollowknight.model.charms.CharmLoadout;
import com.sut.hollowknight.view.MenuUi;
import com.sut.hollowknight.view.assets.InventoryAssets;

/**
 * The in-game charm inventory (spec: "Charms & Inventory System"), styled
 * after the original Hollow Knight menu: a dark translucent backdrop framed
 * by animated "fleur" ornaments, the equipped row + notch capacity on the
 * left, the charm collection grid below, and a hover-driven details panel
 * (name, cost, artwork, official description) on the right.
 *
 * Opening plays every ornament animation ONCE (first -> last frame); closing
 * replays it in reverse (last -> first). Each ornament runs at its own
 * spec-mandated FPS; the backdrop and widgets fade in step with the sweep.
 */
public final class InventoryOverlay extends Group {

    private enum Phase { HIDDEN, OPENING, OPEN, CLOSING }

    // Spec-mandated ornament frame rates.
    private static final float FLEUR_BOTTOM_FPS = 45f;
    private static final float FLEUR_CORNER_FPS = 50f;
    private static final float FLEUR_TOP_FPS    = 60f;
    private static final float SIDE_ARROW_FPS   = 55f;

    /** "Dark background with a slight transparency" (spec/image). */
    private static final float BG_ALPHA = 0.86f;
    private static final int   GRID_COLUMNS = 4;
    private static final float NOTCH_FLASH_SECONDS = 0.7f;

    private final Knight knight;

    private final Texture pixel;
    private final BitmapFont titleFont;
    private final BitmapFont sectionFont;
    private final BitmapFont nameFont;
    private final BitmapFont textFont;
    private final BitmapFont hintFont;

    private final Image dimBackground;
    private final Group content = new Group();

    private Phase phase = Phase.HIDDEN;
    private float phaseTime;
    /** Longest ornament run; drives the shared open/close window. */
    private float ornamentDuration;
    private float notchFlashTimer;

    // ---- Widgets refreshed as the loadout changes ----
    private final Image[] equippedIcons  = new Image[CharmLoadout.MAX_NOTCHES];
    private final Charm[] equippedCharm  = new Charm[CharmLoadout.MAX_NOTCHES];
    private final Image[] notchPips      = new Image[CharmLoadout.MAX_NOTCHES];
    private final Image[] gridIcons      = new Image[Charm.values().length];
    private final Image[] costPips       = new Image[CharmLoadout.MAX_NOTCHES];
    private Label nameLabel;
    private Label costHeader;
    private Image detailIcon;
    private Label descriptionLabel;

    private final TextureRegionDrawable backboardDrawable;
    private final TextureRegionDrawable notchDrawable;
    private final TextureRegionDrawable costLitDrawable;
    private final TextureRegionDrawable[] charmDrawables =
        new TextureRegionDrawable[Charm.values().length];

    public InventoryOverlay(Knight knight, InventoryAssets assets,
                            float stageW, float stageH) {
        this.knight = knight;
        setSize(stageW, stageH);
        setVisible(false);

        pixel       = buildPixel();
        titleFont   = MenuUi.buildTrajanFont(46);
        sectionFont = MenuUi.buildTrajanFont(26);
        nameFont    = MenuUi.buildTrajanFont(32);
        textFont    = MenuUi.buildPerpetuaFont(24);
        hintFont    = MenuUi.buildPerpetuaFont(20);

        backboardDrawable = new TextureRegionDrawable(new TextureRegion(assets.getBackboard()));
        notchDrawable     = new TextureRegionDrawable(new TextureRegion(assets.getNotch()));
        costLitDrawable   = new TextureRegionDrawable(new TextureRegion(assets.getCostLit()));
        for (Charm charm : Charm.values()) {
            charmDrawables[charm.ordinal()] =
                new TextureRegionDrawable(new TextureRegion(assets.getCharmIcon(charm)));
        }

        dimBackground = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        dimBackground.setColor(0f, 0f, 0f, BG_ALPHA);
        dimBackground.setBounds(0f, 0f, stageW, stageH);
        addActor(dimBackground);

        buildContent(stageW, stageH);
        addActor(content);

        buildOrnaments(assets, stageW, stageH);
    }

    // ------------------------------------------------------------------
    //  Public control surface (driven by GameScreen)
    // ------------------------------------------------------------------

    public void open() {
        phase = Phase.OPENING;
        phaseTime = 0f;
        notchFlashTimer = 0f;
        setVisible(true);
        refresh();
        showDetails(defaultDetailsCharm());
    }

    /** Starts the reverse (last -> first frame) ornament playback. */
    public void beginClose() {
        if (phase == Phase.HIDDEN || phase == Phase.CLOSING) return;
        // A close during the opening sweep rewinds from the current frame.
        phaseTime = phase == Phase.OPENING
            ? Math.max(0f, ornamentDuration - phaseTime) : 0f;
        phase = Phase.CLOSING;
    }

    public boolean isShown()  { return phase != Phase.HIDDEN; }
    public boolean isHidden() { return phase == Phase.HIDDEN; }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (phase == Phase.HIDDEN) return;
        phaseTime += delta;

        if (phase == Phase.OPENING && phaseTime >= ornamentDuration) {
            phase = Phase.OPEN;
        } else if (phase == Phase.CLOSING && phaseTime >= ornamentDuration) {
            phase = Phase.HIDDEN;
            setVisible(false);
        }

        float progress;
        switch (phase) {
            case OPENING: progress = Math.min(1f, phaseTime / ornamentDuration); break;
            case CLOSING: progress = Math.max(0f, 1f - phaseTime / ornamentDuration); break;
            case OPEN:    progress = 1f; break;
            default:      progress = 0f; break;
        }
        dimBackground.getColor().a = BG_ALPHA * progress;
        content.getColor().a = progress;

        // Failed-equip feedback: pulse the notch row red, then settle back.
        if (notchFlashTimer > 0f) {
            notchFlashTimer = Math.max(0f, notchFlashTimer - delta);
            float heat = notchFlashTimer / NOTCH_FLASH_SECONDS;
            for (int i = 0; i < notchPips.length; i++) {
                notchPips[i].setColor(1f, 1f - 0.8f * heat, 1f - 0.8f * heat, 1f);
            }
        }
    }

    public void dispose() {
        pixel.dispose();
        titleFont.dispose();
        sectionFont.dispose();
        nameFont.dispose();
        textFont.dispose();
        hintFont.dispose();
    }

    // ------------------------------------------------------------------
    //  Layout
    // ------------------------------------------------------------------

    private void buildContent(float stageW, float stageH) {
        Label.LabelStyle titleStyle   = new Label.LabelStyle(titleFont, MenuUi.TEXT_LIGHT);
        Label.LabelStyle sectionStyle = new Label.LabelStyle(sectionFont, MenuUi.ACCENT);
        Label.LabelStyle nameStyle    = new Label.LabelStyle(nameFont, MenuUi.TEXT_LIGHT);
        Label.LabelStyle textStyle    = new Label.LabelStyle(textFont, MenuUi.TEXT_LIGHT);
        Label.LabelStyle hintStyle    = new Label.LabelStyle(hintFont, MenuUi.TEXT_DIM);

        Label title = new Label(UiText.CHARMS.get(), titleStyle);
        title.setBounds(0f, stageH - 128f, stageW, 60f);
        title.setAlignment(Align.center);
        content.addActor(title);

        float leftX = stageW * 0.08f;

        Label equippedHeader = new Label(UiText.EQUIPPED.get(), sectionStyle);
        equippedHeader.setPosition(leftX, stageH * 0.845f);
        content.addActor(equippedHeader);

        for (int i = 0; i < equippedIcons.length; i++) {
            final int slot = i;
            Image icon = new Image();
            icon.setBounds(leftX + i * 115f, stageH * 0.725f, 95f, 95f);
            icon.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Charm charm = equippedCharm[slot];
                    if (charm != null && isInteractive()) {
                        knight.getCharms().unequip(charm);
                        onLoadoutChanged();
                    }
                }

                @Override
                public void enter(InputEvent event, float x, float y,
                                  int pointer, Actor fromActor) {
                    Charm charm = equippedCharm[slot];
                    if (charm != null) showDetails(charm);
                }
            });
            equippedIcons[i] = icon;
            content.addActor(icon);
        }

        Label notchesHeader = new Label(UiText.NOTCHES.get(), sectionStyle);
        notchesHeader.setPosition(leftX, stageH * 0.655f);
        content.addActor(notchesHeader);

        for (int i = 0; i < notchPips.length; i++) {
            Image pip = new Image(notchDrawable);
            pip.setBounds(leftX + i * 48f, stageH * 0.60f, 40f, 40f);
            pip.setTouchable(Touchable.disabled);
            notchPips[i] = pip;
            content.addActor(pip);
        }

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        divider.setColor(1f, 1f, 1f, 0.75f);
        divider.setBounds(stageW * 0.06f, stageH * 0.575f, stageW * 0.56f, 2f);
        divider.setTouchable(Touchable.disabled);
        content.addActor(divider);

        // ---- Collection grid: backboard sockets + charm icons ----
        float gridX = stageW * 0.09f;
        float gridY = stageH * 0.42f;
        Charm[] all = Charm.values();
        for (int i = 0; i < all.length; i++) {
            final Charm charm = all[i];
            float x = gridX + (i % GRID_COLUMNS) * 130f;
            float y = gridY - (i / GRID_COLUMNS) * 145f;

            Image socket = new Image(backboardDrawable);
            socket.setBounds(x, y, 100f, 100f);
            content.addActor(socket);

            Image icon = new Image(charmDrawables[i]);
            icon.setBounds(x + 8f, y + 8f, 84f, 84f);
            gridIcons[i] = icon;
            content.addActor(icon);

            ClickListener listener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onSlotClicked(charm);
                }

                @Override
                public void enter(InputEvent event, float x, float y,
                                  int pointer, Actor fromActor) {
                    if (knight.getCharms().isAcquired(charm)) showDetails(charm);
                }
            };
            socket.addListener(listener);
            icon.addListener(listener);
        }

        // ---- Details panel (populated on hover) ----
        float panelX = stageW * 0.63f;
        float panelW = stageW * 0.32f;

        nameLabel = new Label("", nameStyle);
        nameLabel.setBounds(panelX, stageH * 0.83f, panelW, 44f);
        nameLabel.setAlignment(Align.center);
        content.addActor(nameLabel);

        costHeader = new Label(UiText.COST.get(), sectionStyle);
        costHeader.setBounds(panelX, stageH * 0.755f, panelW * 0.42f, 42f);
        costHeader.setAlignment(Align.right);
        costHeader.setVisible(false);
        content.addActor(costHeader);

        for (int i = 0; i < costPips.length; i++) {
            Image pip = new Image(costLitDrawable);
            pip.setBounds(panelX + panelW * 0.42f + 14f + i * 46f,
                stageH * 0.755f, 42f, 42f);
            pip.setTouchable(Touchable.disabled);
            pip.setVisible(false);
            costPips[i] = pip;
            content.addActor(pip);
        }

        detailIcon = new Image();
        detailIcon.setBounds(panelX + panelW / 2f - 70f, stageH * 0.585f, 140f, 140f);
        detailIcon.setTouchable(Touchable.disabled);
        content.addActor(detailIcon);

        descriptionLabel = new Label("", textStyle);
        descriptionLabel.setBounds(panelX + 24f, stageH * 0.22f, panelW - 48f,
            stageH * 0.33f);
        descriptionLabel.setAlignment(Align.topLeft);
        descriptionLabel.setWrap(true);
        content.addActor(descriptionLabel);

        Label hint = new Label(
            UiText.INVENTORY_HINT.get(), hintStyle);
        hint.setBounds(0f, 30f, stageW, 24f);
        hint.setAlignment(Align.center);
        content.addActor(hint);
    }

    private void buildOrnaments(InventoryAssets assets, float stageW, float stageH) {
        // Corner atlas art is the TOP-LEFT instance; mirror for the others.
        FleurPiece tl = new FleurPiece(assets.getFleurCorner(), FLEUR_CORNER_FPS, false, false);
        FleurPiece tr = new FleurPiece(assets.getFleurCorner(), FLEUR_CORNER_FPS, true,  false);
        FleurPiece bl = new FleurPiece(assets.getFleurCorner(), FLEUR_CORNER_FPS, false, true);
        FleurPiece br = new FleurPiece(assets.getFleurCorner(), FLEUR_CORNER_FPS, true,  true);
        tl.setPosition(18f, stageH - 18f - tl.getHeight());
        tr.setPosition(stageW - 18f - tr.getWidth(), stageH - 18f - tr.getHeight());
        bl.setPosition(18f, 18f);
        br.setPosition(stageW - 18f - br.getWidth(), 18f);

        FleurPiece top = new FleurPiece(assets.getFleurTop(), FLEUR_TOP_FPS, false, false);
        top.setPosition((stageW - top.getWidth()) / 2f, stageH - top.getHeight() - 8f);

        FleurPiece bottom = new FleurPiece(assets.getFleurBottom(), FLEUR_BOTTOM_FPS, false, false);
        bottom.setPosition((stageW - bottom.getWidth()) / 2f, 14f);

        // Side-arrow atlas art is the LEFT instance; mirror for the right.
        FleurPiece left  = new FleurPiece(assets.getSideArrow(), SIDE_ARROW_FPS, false, false);
        FleurPiece right = new FleurPiece(assets.getSideArrow(), SIDE_ARROW_FPS, true, false);
        left.setPosition(16f, (stageH - left.getHeight()) / 2f);
        right.setPosition(stageW - 16f - right.getWidth(), (stageH - right.getHeight()) / 2f);

        FleurPiece[] pieces = { tl, tr, bl, br, top, bottom, left, right };
        ornamentDuration = 0f;
        for (int i = 0; i < pieces.length; i++) {
            addActor(pieces[i]);
            ornamentDuration = Math.max(ornamentDuration, pieces[i].getRunDuration());
        }
    }

    // ------------------------------------------------------------------
    //  Behavior
    // ------------------------------------------------------------------

    private boolean isInteractive() {
        return phase == Phase.OPEN || phase == Phase.OPENING;
    }

    private void onSlotClicked(Charm charm) {
        if (!isInteractive()) return;
        CharmLoadout loadout = knight.getCharms();
        if (!loadout.isAcquired(charm)) return;
        if (loadout.isEquipped(charm)) {
            loadout.unequip(charm);
        } else if (!loadout.equip(charm)) {
            notchFlashTimer = NOTCH_FLASH_SECONDS; // capacity full - refuse
            return;
        }
        onLoadoutChanged();
    }

    /** Repaints the widgets and writes the loadout through to the save data. */
    private void onLoadoutChanged() {
        refresh();
        GameData data = GameSession.getActive();
        if (data != null) {
            data.equippedCharms.clear();
            data.equippedCharms.addAll(knight.getCharms().toNames());
        }
    }

    private void refresh() {
        CharmLoadout loadout = knight.getCharms();
        Charm[] all = Charm.values();

        // Grid: equipped (or unacquired) charms leave the bare backboard.
        for (int i = 0; i < all.length; i++) {
            gridIcons[i].setVisible(
                loadout.isAcquired(all[i]) && !loadout.isEquipped(all[i]));
        }

        // Equipped row, packed left to right.
        int slot = 0;
        for (int i = 0; i < equippedCharm.length; i++) equippedCharm[i] = null;
        for (int i = 0; i < all.length && slot < equippedIcons.length; i++) {
            if (!loadout.isEquipped(all[i])) continue;
            equippedCharm[slot] = all[i];
            equippedIcons[slot].setDrawable(charmDrawables[i]);
            equippedIcons[slot].setVisible(true);
            slot++;
        }
        for (; slot < equippedIcons.length; slot++) {
            equippedIcons[slot].setDrawable(null);
            equippedIcons[slot].setVisible(false);
        }

        // Notch row: lit pip = occupied notch, charm_cost.png = free notch.
        int used = loadout.usedNotches();
        for (int i = 0; i < notchPips.length; i++) {
            notchPips[i].setDrawable(i < used ? costLitDrawable : notchDrawable);
            notchPips[i].setColor(Color.WHITE);
        }
    }

    private void showDetails(Charm charm) {
        nameLabel.setText(charm.getDisplayName());
        descriptionLabel.setText(charm.getDescription());
        detailIcon.setDrawable(charmDrawables[charm.ordinal()]);
        detailIcon.setVisible(true);
        costHeader.setVisible(true);
        for (int i = 0; i < costPips.length; i++) {
            costPips[i].setVisible(i < charm.getNotchCost());
        }
    }

    private Charm defaultDetailsCharm() {
        CharmLoadout loadout = knight.getCharms();
        for (Charm charm : Charm.values()) {
            if (loadout.isAcquired(charm)) return charm;
        }
        return Charm.SOUL_CATCHER;
    }

    private static Texture buildPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    // ------------------------------------------------------------------
    //  Ornament actor
    // ------------------------------------------------------------------

    /**
     * One ornament of the menu frame. Plays its frames ONCE forward while
     * the menu opens and ONCE in reverse while it closes, at its own FPS.
     * Flipped copies are made up front: zero allocation at draw time.
     */
    private final class FleurPiece extends Actor {
        private final TextureRegion[] frames;
        private final float fps;

        FleurPiece(TextureRegion[] source, float fps, boolean flipX, boolean flipY) {
            this.fps = fps;
            frames = new TextureRegion[source.length];
            float maxW = 0f;
            float maxH = 0f;
            for (int i = 0; i < source.length; i++) {
                TextureRegion copy = new TextureRegion(source[i]);
                copy.flip(flipX, flipY);
                frames[i] = copy;
                maxW = Math.max(maxW, copy.getRegionWidth());
                maxH = Math.max(maxH, copy.getRegionHeight());
            }
            setTouchable(Touchable.disabled);
            setSize(maxW, maxH); // frames stay centered while they unfurl
        }

        float getRunDuration() { return (frames.length - 1) / fps; }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            int last = frames.length - 1;
            int index;
            switch (phase) {
                case OPENING: index = Math.min(last, (int) (phaseTime * fps)); break;
                case CLOSING: index = Math.max(0, last - (int) (phaseTime * fps)); break;
                case OPEN:    index = last; break;
                default:      return;
            }
            TextureRegion frame = frames[index];
            float w = frame.getRegionWidth();
            float h = frame.getRegionHeight();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(frame,
                getX() + (getWidth() - w) / 2f,
                getY() + (getHeight() - h) / 2f,
                w, h);
        }
    }
}
