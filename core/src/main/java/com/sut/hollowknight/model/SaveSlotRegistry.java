package com.sut.hollowknight.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample list of four save slots shown on the Start Game screen.
 *
 * <p>The original Hollow Knight has four save slots numbered 1-4. Some are
 * empty (the player has not started a journey there yet) and some hold a
 * mid-playthrough Knight. This class fabricates a believable starting state
 * so the menu has something meaningful to display; a real implementation
 * would persist these to disk via libGDX {@code Preferences}.</p>
 */
public final class SaveSlotRegistry {

    private static final List<SaveSlot> SLOTS = new ArrayList<>();

    static {
        SLOTS.add(new SaveSlot(1, false, "Knight",      184, 47, "City of Tears"));
        SLOTS.add(new SaveSlot(2, false, "Vessel",       62, 18, "Greenpath"));
        SLOTS.add(new SaveSlot(3, true,  null,           0, 0, null));
        SLOTS.add(new SaveSlot(4, true,  null,           0, 0, null));
    }

    public static List<SaveSlot> all() {
        return SLOTS;
    }

    public static SaveSlot get(int index) {
        for (SaveSlot s : SLOTS) {
            if (s.getIndex() == index) return s;
        }
        return null;
    }

    private SaveSlotRegistry() { }
}
