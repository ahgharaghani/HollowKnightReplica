package com.sut.hollowknight.model;

/**
 * One achievement: identity, display text, icon and unlock state
 * (spec: Achievements).
 */
public class Achievement {

    private final String id;
    private final String title;
    private final String description;
    private final String iconPath;
    private boolean unlocked;

    public Achievement(String id, String title,
                       String description, String iconPath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String  getId()          { return id; }
    public String  getTitle()       { return title; }
    public String  getDescription() { return description; }
    public String  getIconPath()    { return iconPath; }
    public boolean isUnlocked()     { return unlocked; }

    public void unlock() { this.unlocked = true; }
}
