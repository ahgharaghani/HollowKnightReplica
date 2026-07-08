package com.sut.hollowknight.model.charms;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * The Knight's equipped-charm state plus the three-notch capacity rule
 * (spec: "Charm Notches"). Pure model class: no rendering, no libGDX types,
 * so it is trivially unit-testable and serialisable.
 */
public final class CharmLoadout {

    /** Spec: the player owns exactly three notches. */
    public static final int MAX_NOTCHES = 3;

    private final EnumSet<Charm> equipped = EnumSet.noneOf(Charm.class);

    /** Void Heart is only usable once acquired (after felling the boss). */
    private boolean voidHeartAcquired;

    // ------------------------------------------------------------------
    //  Queries
    // ------------------------------------------------------------------

    public boolean isEquipped(Charm charm) { return equipped.contains(charm); }

    /** All charms are owned from the start except the Void Heart. */
    public boolean isAcquired(Charm charm) {
        return charm != Charm.VOID_HEART || voidHeartAcquired;
    }

    public boolean isVoidHeartAcquired() { return voidHeartAcquired; }

    public int usedNotches() {
        int used = 0;
        for (Charm charm : equipped) used += charm.getNotchCost();
        return used;
    }

    public int freeNotches() { return MAX_NOTCHES - usedNotches(); }

    /** True when the charm may be equipped right now (owned + capacity). */
    public boolean canEquip(Charm charm) {
        return !isEquipped(charm)
            && isAcquired(charm)
            && usedNotches() + charm.getNotchCost() <= MAX_NOTCHES;
    }

    // ------------------------------------------------------------------
    //  Mutations
    // ------------------------------------------------------------------

    /** @return true when the charm was actually equipped. */
    public boolean equip(Charm charm) {
        if (!canEquip(charm)) return false;
        equipped.add(charm);
        return true;
    }

    /** @return true when the charm was actually removed. */
    public boolean unequip(Charm charm) { return equipped.remove(charm); }

    public void setVoidHeartAcquired(boolean acquired) {
        voidHeartAcquired = acquired;
        if (!acquired) equipped.remove(Charm.VOID_HEART);
    }

    // ------------------------------------------------------------------
    //  Persistence bridge (GameData stores plain enum names)
    // ------------------------------------------------------------------

    public List<String> toNames() {
        List<String> names = new ArrayList<>(equipped.size());
        for (Charm charm : equipped) names.add(charm.name());
        return names;
    }

    /** Restores a saved loadout, silently dropping unknown/over-cap names. */
    public void loadFromNames(List<String> names) {
        equipped.clear();
        if (names == null) return;
        for (String name : names) {
            try {
                Charm charm = Charm.valueOf(name);
                if (usedNotches() + charm.getNotchCost() <= MAX_NOTCHES) {
                    equipped.add(charm);
                }
            } catch (IllegalArgumentException ignored) {
                // Corrupt or legacy save entry - skip it.
            }
        }
    }
}
