package com.sut.hollowknight.model.collision;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.ArrayList;
import java.util.List;

public class TileMapCollider {

    private final boolean[][] solid;
    private final int cols;
    private final int rows;
    private final int tileWidth;
    private final int tileHeight;
    private final float mapHeightPx;

    private final List<CollisionRect> collisionRects;

    public TileMapCollider(TiledMap map, String tileLayerName, String objectLayerName) {
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
        MapLayer objLayer = map.getLayers().get(objectLayerName);
        if (objLayer != null) {
            for (MapObject obj : objLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    RectangleMapObject rectObj = (RectangleMapObject) obj;

                    // LibGDX's TmxMapLoader ALREADY converts object Y to y-up.
                    // We just take the coordinates directly.
                    float worldX = rectObj.getRectangle().x;
                    float worldY = rectObj.getRectangle().y;
                    float w = rectObj.getRectangle().width;
                    float h = rectObj.getRectangle().height;

                    collisionRects.add(new CollisionRect(worldX, worldY, w, h));
                }
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
}
