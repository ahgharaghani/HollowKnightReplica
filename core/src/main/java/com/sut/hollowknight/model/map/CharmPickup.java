package com.sut.hollowknight.model.map;

import com.sut.hollowknight.model.charms.Charm;

/**
 * A charm lying in the world, authored as a point object in a Tiled
 * "Charms" object layer (spec: Void Heart pickable in the secret room).
 * The object name identifies the charm ("voidheart" - matching is
 * whitespace/case/punctuation-insensitive against the enum and display
 * names, so future pickups need no code changes).
 */
public class CharmPickup {

    private final String name;     // raw Tiled object name (registry key)
    private final Charm charm;
    private final float x, y;      // world units, y-up (pickup centre)

    private boolean collected;
    private float promptProgress;  // 0..1 COLLECT prompt reveal

    public CharmPickup(String name, Charm charm, float x, float y) {
        this.name = name;
        this.charm = charm;
        this.x = x;
        this.y = y;
    }

    /** Map a Tiled object name to a charm; null when unknown. */
    public static Charm resolveCharm(String objectName) {
        if (objectName == null) return null;
        String key = normalize(objectName);
        for (Charm charm : Charm.values()) {
            if (key.equals(normalize(charm.name()))
                || key.equals(normalize(charm.getDisplayName()))) {
                return charm;
            }
        }
        return null;
    }

    private static String normalize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (c >= 'a' && c <= 'z') sb.append(c);
        }
        return sb.toString();
    }

    public String getName()  { return name; }
    public Charm getCharm()  { return charm; }
    public float getX()      { return x; }
    public float getY()      { return y; }

    public boolean isCollected()          { return collected; }
    public void setCollected(boolean c)   { collected = c; }
    public float getPromptProgress()      { return promptProgress; }
    public void setPromptProgress(float p) { promptProgress = p; }
}
