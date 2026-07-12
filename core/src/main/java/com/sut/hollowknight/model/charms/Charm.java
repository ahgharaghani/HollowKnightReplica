package com.sut.hollowknight.model.charms;

import com.sut.hollowknight.model.enums.UiText;

/**
 * The eight collectible charms of the game (spec: "Charms & Inventory System").
 *
 * Each charm is a passive gameplay modifier. Per the spec, every equipped
 * charm occupies exactly ONE notch of the Knight's three-notch capacity.
 * Display names, icons and the official in-game descriptions (from the
 * original Hollow Knight) are carried here so the inventory UI has a single
 * source of truth and never rebuilds strings at runtime.
 */
public enum Charm {

    SOUL_CATCHER(
        UiText.CHARM_SOUL_CATCHER_NAME,
        "ui/charms/Soul Catcher - _0001_charm_more_soul.png",
        UiText.CHARM_SOUL_CATCHER_DESC),

    DASHMASTER(
        UiText.CHARM_DASHMASTER_NAME,
        "ui/charms/Dashmaster - _0011_charm_generic_03.png",
        UiText.CHARM_DASHMASTER_DESC),

    UNBREAKABLE_STRENGTH(
        UiText.CHARM_UNBREAKABLE_STRENGTH_NAME,
        "ui/charms/Unbreakable Strength_0002_charm_glass_attack_up_full.png",
        UiText.CHARM_UNBREAKABLE_STRENGTH_DESC),

    QUICK_SLASH(
        UiText.CHARM_QUICK_SLASH_NAME,
        "ui/charms/Quick Slash - _0003_charm_nail_slash_speed_up.png",
        UiText.CHARM_QUICK_SLASH_DESC),

    QUICK_FOCUS(
        UiText.CHARM_QUICK_FOCUS_NAME,
        "ui/charms/Quick Focus - _0005_charm_fast_focus.png",
        UiText.CHARM_QUICK_FOCUS_DESC),

    HEAVY_BLOW(
        UiText.CHARM_HEAVY_BLOW_NAME,
        "ui/charms/Heavy Blow - _0008_charm_nail_damage_up.png",
        UiText.CHARM_HEAVY_BLOW_DESC),

    SHARP_SHADOW(
        UiText.CHARM_SHARP_SHADOW_NAME,
        "ui/charms/Sharp Shadow - charm_shade_impact.png",
        UiText.CHARM_SHARP_SHADOW_DESC),

    VOID_HEART(
        UiText.CHARM_VOID_HEART_NAME,
        "ui/charms/Void Heart - charm_black.png",
        UiText.CHARM_VOID_HEART_DESC);

    /** Spec rule: every charm costs exactly one notch. */
    private static final int NOTCH_COST = 1;

    private final UiText displayName;
    private final String iconPath;
    private final UiText description;

    Charm(UiText displayName, String iconPath, UiText description) {
        this.displayName = displayName;
        this.iconPath    = iconPath;
        this.description = description;
    }

    /** Localised - resolved against the CURRENT language on every call. */
    public String getDisplayName() { return displayName.get(); }
    public String getIconPath()    { return iconPath; }
    public String getDescription() { return description.get(); }
    public int    getNotchCost()   { return NOTCH_COST; }
}
