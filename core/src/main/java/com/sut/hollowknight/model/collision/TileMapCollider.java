package com.sut.hollowknight.model.collision;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMapCollider {

    private final boolean[][] solid;
    private final int cols;
    private final int rows;
    private final int tileWidth;
    private final int tileHeight;
    private final float mapHeightPx;

    private final List<CollisionRect> collisionRects;
    private final List<CollisionRect> damagingRects;
    private final List<DamageZone> damageZones;

    public TileMapCollider(TiledMap map, String tileLayerName, String objectLayerName) {
        this(map, tileLayerName, objectLayerName, null, null);
    }

    public TileMapCollider(TiledMap map, String tileLayerName,
                           String objectLayerName, String damagingLayerName) {
        this(map, tileLayerName, objectLayerName, damagingLayerName, null);
    }

    public TileMapCollider(TiledMap map, String tileLayerName, String objectLayerName,
                           String damagingLayerName, String safeSpotsLayerName) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(tileLayerName);
        this.cols = layer.getWidth();
        this.rows = layer.getHeight();
        this.tileWidth  = (int) layer.getTileWidth();
        this.tileHeight = (int) layer.getTileHeight();
        this.mapHeightPx = rows * tileHeight;

        solid = new boolean[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                solid[row][col] = layer.getCell(col, row) != null
                    && layer.getCell(col, row).getTile() != null;
            }
        }

        collisionRects = new ArrayList<>();
        loadRects(map, objectLayerName, collisionRects);

        // Damaging zones, each optionally referencing a named SafeSpot object.
        Map<String, float[]> safeSpots = loadSafeSpots(map, safeSpotsLayerName);
        damageZones = new ArrayList<>();
        damagingRects = new ArrayList<>();
        loadDamageZones(map, damagingLayerName, safeSpots, damageZones, damagingRects);
    }

    private static void loadRects(TiledMap map, String layerName, List<CollisionRect> out) {
        if (layerName == null) return;
        MapLayer objLayer = map.getLayers().get(layerName);
        if (objLayer == null) return;
        for (MapObject obj : objLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                RectangleMapObject rectObj = (RectangleMapObject) obj;
                float worldX = rectObj.getRectangle().x;
                float worldY = rectObj.getRectangle().y;
                float w = rectObj.getRectangle().width;
                float h = rectObj.getRectangle().height;
                out.add(new CollisionRect(worldX, worldY, w, h));
            }
        }
    }

    private static Map<String, float[]> loadSafeSpots(TiledMap map, String layerName) {
        Map<String, float[]> spots = new HashMap<>();
        if (layerName == null) return spots;
        MapLayer objLayer = map.getLayers().get(layerName);
        if (objLayer == null) return spots;
        for (MapObject obj : objLayer.getObjects()) {
            String name = obj.getName();
            if (name == null || name.isEmpty()) continue;
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                spots.put(name, new float[]{ x, y });
            }
        }
        return spots;
    }

    private static void loadDamageZones(TiledMap map, String layerName,
                                        Map<String, float[]> safeSpots,
                                        List<DamageZone> zonesOut,
                                        List<CollisionRect> rectsOut) {
        if (layerName == null) return;
        MapLayer objLayer = map.getLayers().get(layerName);
        if (objLayer == null) return;
        for (MapObject obj : objLayer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) continue;
            RectangleMapObject rectObj = (RectangleMapObject) obj;
            CollisionRect rect = new CollisionRect(
                rectObj.getRectangle().x, rectObj.getRectangle().y,
                rectObj.getRectangle().width, rectObj.getRectangle().height);
            rectsOut.add(rect);

            String safeSpotName = obj.getProperties().get("safeSpot", String.class);
            float[] spot = safeSpotName != null ? safeSpots.get(safeSpotName) : null;
            if (spot != null) {
                zonesOut.add(new DamageZone(rect, spot[0], spot[1]));
            } else {
                zonesOut.add(new DamageZone(rect));
            }
        }
    }

    public boolean isSolid(int tileX, int tileY) {
        if (tileX < 0 || tileX >= cols) return true;
        if (tileY < 0 || tileY >= rows) return false;
        return solid[tileY][tileX];
    }

    public int worldXToTile(float wx) {
        return (int) Math.floor(wx / tileWidth);
    }

    public int worldYToTile(float wy) {
        int t = (int) Math.floor(wy / tileHeight);
        if (t < 0)      t = 0;
        if (t >= rows)  t = rows - 1;
        return t;
    }

    public int getTileWidth()  { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
    public int getCols()       { return cols; }
    public int getRows()       { return rows; }
    public float getMapHeightPx() { return mapHeightPx; }

    public List<CollisionRect> getCollisionRects() {
        return collisionRects;
    }

    public List<CollisionRect> getDamagingRects() {
        return damagingRects;
    }

    public boolean overlapsAnyDamagingRect(AABB body) {
        for (CollisionRect damagingRect : damagingRects) {
            if (AABB.overlaps(body, damagingRect)) return true;
        }
        return false;
    }

    public DamageZone getOverlappingDamageZone(AABB body) {
        DamageZone firstHit = null;
        for (DamageZone zone : damageZones) {
            if (AABB.overlaps(body, zone.getRect())) {
                if (zone.hasSafeSpot()) return zone;
                if (firstHit == null) firstHit = zone;
            }
        }
        return firstHit;
    }

    public boolean overlapsAnyRect(float left, float bottom, float right, float top) {
        for (CollisionRect rect : collisionRects) {
            if (rect.overlaps(left, bottom, right, top)) return true;
        }
        return false;
    }

    public CollisionRect findOverlappingRect(float left, float bottom, float right, float top) {
        for (CollisionRect rect : collisionRects) {
            if (rect.overlaps(left, bottom, right, top)) return rect;
        }
        return null;
    }

    public boolean overlapsAnyRect(AABB body) {
        for (CollisionRect rect : collisionRects) {
            if (AABB.overlaps(body, rect)) return true;
        }
        return false;
    }

    public CollisionRect findOverlappingRect(AABB body) {
        for (CollisionRect rect : collisionRects) {
            if (AABB.overlaps(body, rect)) return rect;
        }
        return null;
    }

    public boolean hasLineOfSight(float x1, float y1, float x2, float y2) {
        // 1. Check against Object Layer Collision Rectangles
        for (CollisionRect rect : collisionRects) {
            // If the line segment intersects any of the 4 edges of the rectangle, it's blocked
            if (Intersector.intersectSegments(
                x1, y1, x2, y2, rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getBottom(), null))
                return false;
            if (Intersector.intersectSegments(
                x1, y1, x2, y2, rect.getLeft(), rect.getTop(), rect.getRight(), rect.getTop(), null))
                return false;
            if (Intersector.intersectSegments(
                x1, y1, x2, y2, rect.getLeft(), rect.getBottom(), rect.getLeft(), rect.getTop(), null))
                return false;
            if (Intersector.intersectSegments(
                x1, y1, x2, y2, rect.getRight(), rect.getBottom(), rect.getRight(), rect.getTop(), null))
                return false;
        }

        // 2. Check against the solid tile grid using a simple fixed-step raycast
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        // Step by a quarter-tile to ensure we don't skip over thin walls
        float stepSize = Math.min(tileWidth, tileHeight) / 4.0f;
        int steps = (int) Math.ceil(dist / stepSize);

        for (int i = 1; i < steps; i++) { // start at 1 to ignore the exact starting point
            float t = i / (float) steps;
            float checkX = x1 + dx * t;
            float checkY = y1 + dy * t;

            int tileX = worldXToTile(checkX);
            int tileY = worldYToTile(checkY);

            if (isSolid(tileX, tileY)) {
                return false; // Blocked by a solid tile
            }
        }

        return true; // No obstacles hit!
    }
}
