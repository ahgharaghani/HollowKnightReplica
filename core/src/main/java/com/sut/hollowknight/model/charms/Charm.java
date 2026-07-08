package com.sut.hollowknight.model.charms;

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
        "Soul Catcher",
        "ui/charms/Soul Catcher - _0001_charm_more_soul.png",
        "Used by shamans to draw more SOUL from the world.\n\n"
            + "While the bearer is striking enemies with their nail, they "
            + "will absorb a greater amount of SOUL."),

    DASHMASTER(
        "Dashmaster",
        "ui/charms/Dashmaster - _0011_charm_generic_03.png",
        "Bears the likeness of an eccentric bug known only as 'The "
            + "Dashmaster'.\n\nThe bearer will be able to dash more often "
            + "as well as dash downwards. Perfect for those who want to "
            + "move around as quickly as possible."),

    UNBREAKABLE_STRENGTH(
        "Unbreakable Strength",
        "ui/charms/Unbreakable Strength_0002_charm_glass_attack_up_full.png",
        "Strengthens the bearer, increasing the damage they deal to "
            + "enemies with their nail.\n\nThis charm is unbreakable."),

    QUICK_SLASH(
        "Quick Slash",
        "ui/charms/Quick Slash - _0003_charm_nail_slash_speed_up.png",
        "Found embedded in the greatest of the Kingsmoulds. Its blade is "
            + "still sharp with ancient technology.\n\nAllows the bearer to "
            + "slash much more rapidly with their nail."),

    QUICK_FOCUS(
        "Quick Focus",
        "ui/charms/Quick Focus - _0005_charm_fast_focus.png",
        "A charm containing a crystal lens.\n\nIncreases the speed of "
            + "focusing SOUL, allowing the bearer to heal damage faster."),

    HEAVY_BLOW(
        "Heavy Blow",
        "ui/charms/Heavy Blow - _0008_charm_nail_damage_up.png",
        "Formed from the shell of a fallen warrior.\n\nIncreases the "
            + "force of the bearer's nail, causing enemies to recoil "
            + "further when hit."),

    SHARP_SHADOW(
        "Sharp Shadow",
        "ui/charms/Sharp Shadow - charm_shade_impact.png",
        "Contains the essence of a shadow creature.\n\nWhen using Shadow "
            + "Dash, the bearer's body will sharpen and damage enemies."),

    VOID_HEART(
        "Void Heart",
        "ui/charms/Void Heart - charm_black.png",
        "An emptiness that was hidden within, now unconstrained.\n\n"
            + "Unifies the void under the bearer's will.");

    /** Spec rule: every charm costs exactly one notch. */
    private static final int NOTCH_COST = 1;

    private final String displayName;
    private final String iconPath;
    private final String description;

    Charm(String displayName, String iconPath, String description) {
        this.displayName = displayName;
        this.iconPath    = iconPath;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getIconPath()    { return iconPath; }
    public String getDescription() { return description; }
    public int    getNotchCost()   { return NOTCH_COST; }
}
