package com.sut.hollowknight.model;

import com.sut.hollowknight.model.enums.UiText;

/**
 * Represents a Hollow Knight save slot.
 *
 * <p>The original game shows four save slots in the Start Game menu, each one
 * tracking how far the Knight has progressed (play-time, completion percent
 * and the most recently visited area). Empty slots invite the player to start
 * a new journey; occupied slots let them continue an existing one.</p>
 */
public class SaveSlot {

    private final int index;
    private final boolean empty;
    private final String knightName;
    private final int playTimeMinutes;
    private final int completionPercent;
    private final String lastArea;

    public SaveSlot(int index,
                    boolean empty,
                    String knightName,
                    int playTimeMinutes,
                    int completionPercent,
                    String lastArea) {
        this.index = index;
        this.empty = empty;
        this.knightName = knightName;
        this.playTimeMinutes = playTimeMinutes;
        this.completionPercent = completionPercent;
        this.lastArea = lastArea;
    }

    public int getIndex()                  { return index; }
    public boolean isEmpty()               { return empty; }
    public String getKnightName()          { return knightName; }
    public int getPlayTimeMinutes()        { return playTimeMinutes; }
    public int getCompletionPercent()      { return completionPercent; }
    public String getLastArea()            { return lastArea; }

    /** Returns a short summary line shown under the slot title. */
    public String getSummary() {
        if (empty) {
            return UiText.EMPTY_SLOT.get();
        }
        int hours = playTimeMinutes / 60;
        int mins  = playTimeMinutes % 60;
        return String.format("%s   |   %d:%02d   |   %d%%   |   %s",
            knightName, hours, mins, completionPercent, lastArea);
    }
}
