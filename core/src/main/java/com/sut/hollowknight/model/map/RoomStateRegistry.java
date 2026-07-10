package com.sut.hollowknight.model.map;

import java.util.HashSet;
import java.util.Set;

/**
 * Session-scoped world state that must survive room transitions (spec:
 * "when returning from the secret room the darkness zone is rendered
 * again" - it must NOT be).
 *
 * <p>Each {@code GameScreen} is rebuilt from the TMX on every room switch,
 * so anything the player changed - broken walls, revealed darkness,
 * collected pickups - would silently reset. This static registry is the
 * single source of truth those screens consult while re-parsing the map.
 * Keys are map-qualified ("CityOfTears.tmx:BreakableWall_01") so names
 * only need to be unique within one map.</p>
 *
 * <p>Cleared on a fresh start from the menu. (Persisting it into the save
 * database can be layered on later without touching game code.)</p>
 */
public final class RoomStateRegistry {

    private static final Set<String> brokenWalls = new HashSet<>();
    private static final Set<String> collectedPickups = new HashSet<>();
    private static boolean voidHeartCollected;

    private RoomStateRegistry() { }   // static use only

    public static void markWallBroken(String key)   { brokenWalls.add(key); }
    public static boolean isWallBroken(String key)  { return brokenWalls.contains(key); }

    public static void markPickupCollected(String key)  { collectedPickups.add(key); }
    public static boolean isPickupCollected(String key) { return collectedPickups.contains(key); }

    public static void setVoidHeartCollected()     { voidHeartCollected = true; }
    public static boolean isVoidHeartCollected()   { return voidHeartCollected; }

    /** Fresh game start: forget everything from the previous session. */
    public static void clear() {
        brokenWalls.clear();
        collectedPickups.clear();
        voidHeartCollected = false;
    }
}
