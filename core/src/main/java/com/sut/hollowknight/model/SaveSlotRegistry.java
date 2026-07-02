package com.sut.hollowknight.model;

import com.sut.hollowknight.model.db.GameDatabase;

import java.util.ArrayList;
import java.util.List;

public final class SaveSlotRegistry {

    private static final List<SaveSlot> SLOTS = new ArrayList<>();

    public static void reload() {
        SLOTS.clear();
        for (GameData data : GameDatabase.loadAll()) {
            SLOTS.add(new SaveSlot(
                data.slotIndex,
                data.empty,
                data.empty ? null : data.knightName,
                data.empty ? 0 : (data.playTimeSeconds / 60),
                data.empty ? 0 : data.completionPercent,
                data.empty ? null : data.lastArea
            ));
        }
    }

    public static List<SaveSlot> all() {
        if (SLOTS.isEmpty()) reload();
        return SLOTS;
    }

    public static SaveSlot get(int index) {
        for (SaveSlot s : all()) {
            if (s.getIndex() == index) return s;
        }
        return null;
    }

    public static GameData loadGameData(int slotIndex) {
        return GameDatabase.load(slotIndex);
    }

    public static void saveGameData(GameData data) {
        GameDatabase.save(data);
        reload();
    }

    private SaveSlotRegistry() { }
}
