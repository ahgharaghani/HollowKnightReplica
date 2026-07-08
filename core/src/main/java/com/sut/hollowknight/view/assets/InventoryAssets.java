package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.sut.hollowknight.model.charms.Charm;

/**
 * Typed access to the inventory-menu art: the four animated "fleur" frame
 * ornaments plus every charm icon and notch/cost widget texture.
 *
 * Atlas frames are returned sorted by their explicit {@code index:} entry -
 * atlas file order is NOT guaranteed to match playback order.
 */
public final class InventoryAssets {

    public static final String FLEUR_CORNER_ATLAS = "ui/inventory/Inventory Fleur Corner.atlas";
    public static final String FLEUR_TOP_ATLAS    = "ui/inventory/Inventory Fleur Top.atlas";
    public static final String FLEUR_BOTTOM_ATLAS = "ui/inventory/Inventory Fleur Bottom.atlas";
    public static final String SIDE_ARROW_ATLAS   = "ui/inventory/Inventory Side Arrow.atlas";

    public static final String BACKBOARD_PNG = "ui/charms/charm_backboard.png";
    public static final String NOTCH_PNG     = "ui/charms/charm_cost.png";
    public static final String COST_LIT_PNG  = "ui/charms/charm_UI__0000_charm_cost_02_lit.png";
    public static final String COST_UNLIT_PNG = "ui/charms/charm_UI__0001_charm_cost_02_unlit.png";

    private final AssetManager manager;

    private final TextureRegion[] fleurCorner;
    private final TextureRegion[] fleurTop;
    private final TextureRegion[] fleurBottom;
    private final TextureRegion[] sideArrow;

    public InventoryAssets(AssetManager manager) {
        this.manager = manager;
        fleurCorner = frames(FLEUR_CORNER_ATLAS, "Inventory_Fleur_Corner Up");
        fleurTop    = frames(FLEUR_TOP_ATLAS,    "Inventory_Fleur_Top Up");
        fleurBottom = frames(FLEUR_BOTTOM_ATLAS, "Inventory_Fleur_Bottom Up");
        sideArrow   = frames(SIDE_ARROW_ATLAS,   "Inventory_Side_Arrow Up");
    }

    private TextureRegion[] frames(String atlasPath, String regionName) {
        TextureAtlas atlas = manager.get(atlasPath, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        TextureRegion[] ordered = new TextureRegion[regions.size];
        for (int i = 0; i < regions.size; i++) {
            TextureAtlas.AtlasRegion region = regions.get(i);
            ordered[region.index] = region; // indices are contiguous 0..n-1
        }
        return ordered;
    }

    public TextureRegion[] getFleurCorner() { return fleurCorner; }
    public TextureRegion[] getFleurTop()    { return fleurTop; }
    public TextureRegion[] getFleurBottom() { return fleurBottom; }
    public TextureRegion[] getSideArrow()   { return sideArrow; }

    public Texture getCharmIcon(Charm charm) {
        return manager.get(charm.getIconPath(), Texture.class);
    }

    public Texture getBackboard() { return manager.get(BACKBOARD_PNG, Texture.class); }
    public Texture getNotch()     { return manager.get(NOTCH_PNG, Texture.class); }
    public Texture getCostLit()   { return manager.get(COST_LIT_PNG, Texture.class); }
    public Texture getCostUnlit() { return manager.get(COST_UNLIT_PNG, Texture.class); }
}
