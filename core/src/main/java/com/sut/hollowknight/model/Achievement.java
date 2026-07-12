package com.sut.hollowknight.model;

import com.sut.hollowknight.model.enums.UiText;

/**
 * One achievement: identity, display text, icon and unlock state
 * (spec: Achievements).
 *
 * Title and description are UiText REFERENCES, resolved against the
 * current language on every call - so the Achievements screen and the
 * unlock toast are localised for free, even if the language changed
 * after this object was built.
 */
public class Achievement {

    private final String id;
    private final UiText title;
    private final UiText description;
    private final String iconPath;
    private boolean unlocked;

    public Achievement(String id, UiText title,
                       UiText description, String iconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String  getId()          { return id; }
    public String  getTitle()       { return title.get(); }
    public String  getDescription() { return description.get(); }
    public String  getIconPath()    { return iconPath; }
    public boolean isUnlocked()     { return unlocked; }

    public void unlock() { this.unlocked = true; }

    /** Relock - used when the singleton registry is re-seeded for a new
     *  session, and by the debug "clear achievements" cheat. */
    public void relock() { this.unlocked = false; }
}
