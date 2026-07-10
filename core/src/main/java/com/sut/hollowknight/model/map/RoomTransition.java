package com.sut.hollowknight.model.map;

import com.sut.hollowknight.model.collision.AABB;

/**
 * A room exit authored in Tiled ("Transitions" object layer): when the
 * knight's hurt-box overlaps this rectangle, the screen fades to black,
 * {@code targetMap} is loaded, and the knight appears at the point object
 * named {@code targetSpawn} in that map - the original game's room
 * transition beat (spec: Room Transitions).
 */
public class RoomTransition implements AABB {

    private final float x, y, width, height;   // world units, y-up
    private final String targetMap;
    private final String targetSpawn;

    public RoomTransition(float x, float y, float width, float height,
                          String targetMap, String targetSpawn) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetMap = targetMap;
        this.targetSpawn = targetSpawn;
    }

    public String getTargetMap()   { return targetMap; }
    public String getTargetSpawn() { return targetSpawn; }

    @Override public float getLeft()   { return x; }
    @Override public float getRight()  { return x + width; }
    @Override public float getBottom() { return y; }
    @Override public float getTop()    { return y + height; }
}
