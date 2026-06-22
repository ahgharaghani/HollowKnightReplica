package com.sut.hollowknight.model;

/**
 * Represents a single Hollow Knight achievement.
 *
 * <p>Achievements start locked and become unlocked when the relevant in-game
 * event fires. The {@code codeName} is what the player sees while the
 * achievement is still locked (similar to the original game's "?" / hash
 * placeholder entries).</p>
 */
public class Achievement {

    private final String id;
    private final String displayName;
    private final String description;
    private final String codeName;        // shown when locked (e.g. "???")
    private final String hiddenDescription; // shown when locked
    private boolean unlocked;

    public Achievement(String id,
                       String displayName,
                       String description,
                       String codeName,
                       String hiddenDescription,
                       boolean unlocked) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.codeName = codeName;
        this.hiddenDescription = hiddenDescription;
        this.unlocked = unlocked;
    }

    public String getId()                 { return id; }
    public String getDisplayName()        { return displayName; }
    public String getDescription()        { return description; }
    public String getCodeName()           { return codeName; }
    public String getHiddenDescription()  { return hiddenDescription; }
    public boolean isUnlocked()           { return unlocked; }

    public void unlock() {
        this.unlocked = true;
    }

    /** Returns the text the UI should render for the achievement's title. */
    public String getDisplayTitle() {
        return unlocked ? displayName : codeName;
    }

    /** Returns the text the UI should render for the achievement's body. */
    public String getDisplayDescription() {
        return unlocked ? description : hiddenDescription;
    }
}
