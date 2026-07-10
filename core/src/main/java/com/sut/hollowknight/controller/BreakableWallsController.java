package com.sut.hollowknight.controller;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.map.BreakableWall;
import com.sut.hollowknight.model.map.DarknessZone;
import com.sut.hollowknight.view.effects.ParticleHook;

import java.util.List;

/**
 * Drives breakable walls (spec: Breakable Walls & Secret Areas).
 *
 * <ul>
 *   <li><b>Hit:</b> one hp per nail swing (same de-dupe as ZoteController),
 *       and the wall art shakes via its tile layer's render offset.</li>
 *   <li><b>Break:</b> the wall's cells open in the solid grid, its tiles
 *       are erased, linked darkness zones fade out, the camera kicks, and
 *       the particle hook fires (dust art plugs in later).</li>
 * </ul>
 *
 * No allocations in {@link #update(float)} - index loops only.
 */
public class BreakableWallsController {

    private static final float SHAKE_FREQUENCY_HZ = 30f;
    private static final float SHAKE_AMPLITUDE_PX = 3f;
    private static final float BREAK_CAMERA_SHAKE_AMPLITUDE = 6f;
    private static final float BREAK_CAMERA_SHAKE_DURATION  = 0.35f;

    private final List<BreakableWall> walls;
    private final List<DarknessZone> darknessZones;
    private final TileMapCollider collider;
    private final Knight knight;
    /** The "BreakableWalls" tile layer; null in rooms without walls. */
    private final TiledMapTileLayer wallTileLayer;
    private final GameController gameController;
    private ParticleHook particleHook;

    private float time;   // drives the shake oscillation

    public BreakableWallsController(List<BreakableWall> walls,
                                    List<DarknessZone> darknessZones,
                                    TileMapCollider collider,
                                    Knight knight,
                                    TiledMapTileLayer wallTileLayer,
                                    GameController gameController,
                                    ParticleHook particleHook) {
        this.walls = walls;
        this.darknessZones = darknessZones;
        this.collider = collider;
        this.knight = knight;
        this.wallTileLayer = wallTileLayer;
        this.gameController = gameController;
        this.particleHook = particleHook != null ? particleHook : ParticleHook.NO_OP;
    }

    /** Swap in a real dust emitter later without touching this class. */
    public void setParticleHook(ParticleHook hook) {
        particleHook = hook != null ? hook : ParticleHook.NO_OP;
    }

    public void update(float delta) {
        time += delta;

        for (int i = 0; i < darknessZones.size(); i++) {
            darknessZones.get(i).update(delta);
        }

        float strongestShake = 0f;
        for (int i = 0; i < walls.size(); i++) {
            BreakableWall wall = walls.get(i);
            wall.tick(delta);
            checkNailHit(wall);
            float s = wall.getShakeStrength();
            if (s > strongestShake) strongestShake = s;
        }

        // The wall tiles live on their own "BreakableWalls" layer, rendered
        // separately by GameScreen, so a render offset shakes them without
        // moving any other terrain. (The offset is shared by every wall on
        // the layer - fine while only one wall shakes at a time.)
        if (wallTileLayer != null) {
            float offset = strongestShake > 0f
                ? MathUtils.sin(time * SHAKE_FREQUENCY_HZ * MathUtils.PI2)
                    * SHAKE_AMPLITUDE_PX * strongestShake
                : 0f;
            wallTileLayer.setOffsetX(offset);
        }
    }

    /** Same de-dupe pattern as ZoteController: one hit per nail swing. */
    private void checkNailHit(BreakableWall wall) {
        if (wall.isBroken() || !knight.isAttacking()) return;
        if (wall.getLastNailHitId() == knight.getAttackId()) return;
        CollisionRect slash = knight.getActiveSlashBox();
        if (slash == null || !AABB.overlaps(slash, wall)) return;

        wall.setLastNailHitId(knight.getAttackId());
        boolean brokeNow = wall.hit();
        particleHook.onWallHit(wall.getCenterX(), wall.getCenterY());
        if (brokeNow) breakWall(wall);
    }

    private void breakWall(BreakableWall wall) {
        // 1. Open the passage: release the solid cells claimed at load.
        collider.setSolidRegion(wall.getX(), wall.getY(),
            wall.getWidth(), wall.getHeight(), false);

        // 2. Erase the wall art from its tile layer.
        if (wallTileLayer != null) {
            int tw = wallTileLayer.getTileWidth();
            int th = wallTileLayer.getTileHeight();
            int colStart = (int) Math.floor(wall.getLeft()   / tw);
            int colEnd   = (int) Math.ceil (wall.getRight()  / tw) - 1;
            int rowStart = (int) Math.floor(wall.getBottom() / th);
            int rowEnd   = (int) Math.ceil (wall.getTop()    / th) - 1;
            for (int row = rowStart; row <= rowEnd; row++) {
                if (row < 0 || row >= wallTileLayer.getHeight()) continue;
                for (int col = colStart; col <= colEnd; col++) {
                    if (col < 0 || col >= wallTileLayer.getWidth()) continue;
                    wallTileLayer.setCell(col, row, null);
                }
            }
            wallTileLayer.setOffsetX(0f);
        }

        // 3. Fade out every darkness zone this wall was hiding.
        for (int i = 0; i < darknessZones.size(); i++) {
            if (darknessZones.get(i).isRevealedBy(wall.getName())) {
                darknessZones.get(i).reveal();
            }
        }

        // 4. Impact feedback now; dust particles plug in here later.
        gameController.shakeCamera(BREAK_CAMERA_SHAKE_AMPLITUDE,
            BREAK_CAMERA_SHAKE_DURATION);
        if (wall.hasDebris()) {
            particleHook.onWallBreak(wall.getCenterX(), wall.getCenterY(),
                wall.getWidth(), wall.getHeight());
        }
    }

    public List<BreakableWall> getWalls() { return walls; }
}
