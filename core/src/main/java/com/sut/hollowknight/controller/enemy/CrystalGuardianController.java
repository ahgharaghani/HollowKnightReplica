package com.sut.hollowknight.controller.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.CrystalGuardian;
import com.sut.hollowknight.model.enemy.Laser;

/**
 * Drives the Crystal Guardian state machine:
 *
 *   IDLE (rooted, slowly sweeping its sightline; occasional [TURN])
 *     -> SHOOT_ANTIC (aim locked on the spot; a thin harmless beam marks the
 *                     fixed firing line for ~1.5s)
 *     -> SHOOT (thick damaging beam fires along that line) -> SHOOT_LOOP -> SHOOT_END
 *     -> ENRAGED (charges to where the knight was first seen, then stops)
 *     -> IDLE (resumes the sweep from the new position)
 *
 * The laser is a terrain-clipped segment; its damage is resolved by
 * {@link com.sut.hollowknight.controller.CombatSystem}. While charging the
 * beam is flagged harmless and deals no damage.
 */
public class CrystalGuardianController implements EnemyController {

    private final CrystalGuardian guardian;
    private final TileMapCollider collider;
    private Knight knight;

    private int lastNailHitId = -1;

    /** Set once per shot so the beam ignites exactly one time. */
    private boolean laserFired = false;

    /** Seconds the guardian holds its current idle facing before sweeping. */
    private float nextIdleTurnInterval = CrystalGuardian.IDLE_TURN_MIN_INTERVAL;

    /** Normalized, angle-clamped aim direction — recomputed per shot/track. */
    private float aimX;
    private float aimY;

    /** Cliff probe: a thin box just past the front foot, reaching below the feet. */
    private static final float CLIFF_PROBE_WIDTH = 6f;
    private static final float CLIFF_PROBE_DEPTH = 14f;

    /** Step used to march the beam through the terrain (tile size = 8 px). */
    private static final float BEAM_CAST_STEP = 8f;
    /** Skip the guardian's own body when casting the beam. */
    private static final float BEAM_CAST_START = 24f;

    /** Stop chasing when this close horizontally — avoids jittering. */
    private static final float ENRAGE_STOP_DISTANCE = 30f;

    // Death: pop up with the killing blow, fall, land, settle as a corpse.
    private static final float DEATH_GRAVITY = 900f;
    private static final float DEATH_LAUNCH_VX = 100f;
    private static final float DEATH_LAUNCH_VY = 200f;
    /** Death Land plays 3 frames at 10 fps — once, then the corpse settles. */
    private static final float DEATH_LAND_DURATION = 3f / 12f;
    private static final float RESPAWN_DISTANCE = 2000f;

    // Push-out resolution sides
    private static final int PUSH_LEFT = 0, PUSH_RIGHT = 1, PUSH_UP = 2, PUSH_DOWN = 3;

    public CrystalGuardianController(CrystalGuardian guardian, TileMapCollider collider) {
        this.guardian = guardian;
        this.collider = collider;
        snapToGround();
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    private void snapToGround() {
        float bestTop = -Float.MAX_VALUE;
        for (CollisionRect rect : collider.getCollisionRects()) {
            boolean overlapsX = rect.getRight() > guardian.getLeft()
                && rect.getLeft() < guardian.getRight();
            if (!overlapsX) continue;
            float top = rect.getTop();
            if (top <= guardian.getY() + 4f && top > bestTop) {
                bestTop = top;
            }
        }
        if (bestTop > -Float.MAX_VALUE) {
            guardian.setY(bestTop);
        }
    }

    @Override
    public void update(float delta) {
        // Hit flash fades even during death (the killing blow flashes too).
        guardian.tickHitFlash(delta);

        if (!guardian.isAlive()) {
            updateDeath(delta);
            return;
        }

        guardian.addStateTime(delta);

        switch (guardian.getState()) {
            case IDLE:        updateIdle();          break;
            case TURN:        updateTurn();          break;
            case SHOOT_ANTIC: updateShootAntic();    break;
            case SHOOT:       updateShoot();         break;
            case SHOOT_LOOP:  updateShootLoop();     break;
            case SHOOT_END:   updateShootEnd();      break;
            case ENRAGED:     updateEnraged(delta);  break;
            default: break;
        }

        applyRecoil(delta);
    }

    //  Watching

    private void updateIdle() {
        guardian.setVelocityX(0);

        // Target lock: the moment the knight enters the sightline, stop
        // sweeping, face them, and remember where they were first seen.
        if (seesKnight()) {
            // Face the knight, remember the spot, and LOCK the aim right here —
            // the beam holds this line through the charge, it does not track.
            guardian.setFacingRight(knight.getX() >= guardian.getX());
            guardian.setSpottedX(knight.getX());
            lockAim();
            laserFired = false;
            guardian.setState(CrystalGuardian.State.SHOOT_ANTIC);
            return;
        }

        // Passive sweep: slowly turn left/right to patrol the immediate sightline.
        if (guardian.getStateTime() >= nextIdleTurnInterval) {
            guardian.setTurnTargetRight(!guardian.isFacingRight());
            rollNextIdleTurnInterval();
            guardian.setState(CrystalGuardian.State.TURN);
        }
    }

    private void rollNextIdleTurnInterval() {
        nextIdleTurnInterval = MathUtils.random(
            CrystalGuardian.IDLE_TURN_MIN_INTERVAL,
            CrystalGuardian.IDLE_TURN_MAX_INTERVAL);
    }

    private void updateTurn() {
        guardian.setVelocityX(0);
        if (guardian.getStateTime() >= CrystalGuardian.TURN_DURATION) {
            guardian.setFacingRight(guardian.getTurnTargetRight());
            guardian.setState(CrystalGuardian.State.IDLE);
        }
    }

    /**
     * Front-facing watch only — the guardian has no eyes in its back.
     * Requires the knight in front, inside the watch window, with a clear
     * line of sight from the lamp.
     */
    private boolean seesKnight() {
        if (knight == null) return false;

        float knightCenterY = knight.getY() + Knight.KNIGHT_HEIGHT / 2f;
        float dx = knight.getX() - guardian.getX();

        boolean inFront = guardian.isFacingRight() ? dx > 0f : dx < 0f;
        if (!inFront) return false;
        if (Math.abs(dx) > CrystalGuardian.DETECTION_RANGE) return false;

        float dy = knightCenterY - guardian.getCenterY();
        if (Math.abs(dy) > CrystalGuardian.DETECTION_VERTICAL) return false;

        return collider.hasLineOfSight(
            guardian.getMuzzleX(), guardian.getMuzzleY(),
            knight.getX(), knightCenterY);
    }

    //  Shooting

    /**
     * Charging phase: posture and aim are already locked. A thin harmless beam
     * marks the fixed firing line for ~1.5s — the knight can read it and step
     * out before the real beam fires.
     */
    private void updateShootAntic() {
        guardian.setVelocityX(0);

        float ox = guardian.getMuzzleX();
        float oy = guardian.getMuzzleY();
        float length = castBeam(ox, oy, aimX, aimY);
        guardian.getLaser().activate(ox, oy, aimX, aimY, length, true);

        if (guardian.getStateTime() >= CrystalGuardian.SHOOT_ANTIC_DURATION) {
            guardian.getLaser().deactivate();
            guardian.setState(CrystalGuardian.State.SHOOT);
        }
    }

    private void updateShoot() {
        guardian.setVelocityX(0);

        // Firing phase: posture locks and the thick damaging beam ignites once,
        // aimed where the knight stands now. It then holds perfectly still —
        // the knight can dodge by moving after this moment.
        if (!laserFired) {
            fireLaser();
            laserFired = true;
        }

        if (guardian.getStateTime() >= CrystalGuardian.SHOOT_DURATION) {
            guardian.setState(CrystalGuardian.State.SHOOT_LOOP);
        }
    }

    private void updateShootLoop() {
        guardian.setVelocityX(0);
        if (guardian.getStateTime() >= CrystalGuardian.SHOOT_LOOP_DURATION) {
            guardian.getLaser().deactivate();
            guardian.setState(CrystalGuardian.State.SHOOT_END);
        }
    }

    private void updateShootEnd() {
        guardian.setVelocityX(0);
        if (guardian.getStateTime() >= CrystalGuardian.SHOOT_END_DURATION) {
            // "Immediately after firing the laser" — straight into the rage.
            guardian.setState(CrystalGuardian.State.ENRAGED);
        }
    }

    /**
     * Locks the aim direction on the knight's center at the instant the guardian
     * commits to the shot. Held unchanged through the charge and the shot, so
     * the knight can dodge by moving after this moment.
     */
    private void lockAim() {
        float ox = guardian.getMuzzleX();
        float oy = guardian.getMuzzleY();
        if (knight != null) {
            aimAt(ox, oy, knight.getX(), knight.getY() + Knight.KNIGHT_HEIGHT / 2f);
        } else {
            aimX = guardian.isFacingRight() ? 1f : -1f;
            aimY = 0f;
        }
    }

    /** Ignites the thick damaging beam along the already-locked aim line. */
    private void fireLaser() {
        float ox = guardian.getMuzzleX();
        float oy = guardian.getMuzzleY();
        float length = castBeam(ox, oy, aimX, aimY);
        guardian.getLaser().activate(ox, oy, aimX, aimY, length);
    }

    /**
     * Computes a normalized aim direction from (ox,oy) toward the target,
     * clamped so the beam never points straight up or down: the elevation is
     * capped and a horizontal component is always kept along the facing side.
     * One atan2 per call — outside any hot loop.
     */
    private void aimAt(float ox, float oy, float targetX, float targetY) {
        float dx = targetX - ox;
        float dy = targetY - oy;

        // Horizontal side: follow the target, or the facing if dead overhead.
        float h = dx != 0f ? Math.signum(dx)
                           : (guardian.isFacingRight() ? 1f : -1f);

        float elevation = (float) Math.atan2(dy, Math.abs(dx));
        float maxElevation = CrystalGuardian.MAX_AIM_ANGLE_DEG * MathUtils.degreesToRadians;
        elevation = MathUtils.clamp(elevation, -maxElevation, maxElevation);

        aimX = h * (float) Math.cos(elevation);
        aimY = (float) Math.sin(elevation);
    }

    /** Marches the beam through the level and returns the distance to the first terrain hit. */
    private float castBeam(float ox, float oy, float dirX, float dirY) {
        for (float t = BEAM_CAST_START; t <= Laser.MAX_RANGE; t += BEAM_CAST_STEP) {
            float px = ox + dirX * t;
            float py = oy + dirY * t;
            if (pointBlocked(px, py)) return t;
        }
        return Laser.MAX_RANGE;
    }

    private boolean pointBlocked(float px, float py) {
        if (collider.overlapsAnyRect(px - 1f, py - 1f, px + 1f, py + 1f)) return true;
        return collider.isSolid(collider.worldXToTile(px), collider.worldYToTile(py));
    }

    //  Rage — charge to where the knight was first seen, then stop.

    private void updateEnraged(float delta) {
        // Target is the spotted spot, fixed regardless of where the knight is now.
        float dx = guardian.getSpottedX() - guardian.getX();
        if (Math.abs(dx) <= ENRAGE_STOP_DISTANCE) {
            arriveAtSpot();
            return;
        }

        float dir = dx > 0f ? 1f : -1f;
        guardian.setFacingRight(dir > 0f);
        guardian.setVelocityX(dir * CrystalGuardian.ENRAGE_SPEED);
        float prevX = guardian.getX();
        guardian.setX(prevX + guardian.getVelocityX() * delta);

        // Walls stop the charge in place; cliffs are never crossed.
        CollisionRect wall = collider.findOverlappingRect(guardian);
        if (wall != null) {
            pushOut(wall);
            arriveAtSpot();
        } else if (!groundAhead(dir)) {
            guardian.setX(prevX);
            arriveAtSpot();
        }
    }

    /** Reached (or was stopped short of) the spot: settle and resume the sweep. */
    private void arriveAtSpot() {
        guardian.setVelocityX(0);
        rollNextIdleTurnInterval();
        guardian.setState(CrystalGuardian.State.IDLE);
    }

    private boolean groundAhead(float dir) {
        float frontX = dir > 0
            ? guardian.getRight() + CLIFF_PROBE_WIDTH / 2f
            : guardian.getLeft() - CLIFF_PROBE_WIDTH / 2f;
        return collider.overlapsAnyRect(
            frontX - CLIFF_PROBE_WIDTH / 2f, guardian.getY() - CLIFF_PROBE_DEPTH,
            frontX + CLIFF_PROBE_WIDTH / 2f, guardian.getY() - 1f);
    }

    //  Recoil / death (same conventions as the other ground enemies)

    private void applyRecoil(float delta) {
        if (!guardian.isRecoiling()) return;

        // Braced while firing — the beam origin is locked, so the body holds.
        CrystalGuardian.State s = guardian.getState();
        if (s == CrystalGuardian.State.SHOOT || s == CrystalGuardian.State.SHOOT_LOOP) {
            guardian.cancelRecoil();
            return;
        }

        float falloff = guardian.getRecoilTimer() / CrystalGuardian.RECOIL_DURATION;
        float prevX = guardian.getX();
        guardian.setX(prevX + guardian.getRecoilVelX() * falloff * delta);

        boolean hitWall = collider.findOverlappingRect(guardian) != null;
        boolean groundBelow = collider.overlapsAnyRect(
            guardian.getLeft(), guardian.getY() - CLIFF_PROBE_DEPTH,
            guardian.getRight(), guardian.getY() - 1f);
        if (hitWall || !groundBelow) {
            guardian.setX(prevX);
            guardian.cancelRecoil();
            return;
        }

        guardian.tickRecoil(delta);
    }

    private void updateDeath(float delta) {
        guardian.addStateTime(delta);

        if (guardian.getState() == CrystalGuardian.State.DEATH_AIR) {
            guardian.setVelocityY(guardian.getVelocityY() - DEATH_GRAVITY * delta);
            guardian.setX(guardian.getX() + guardian.getVelocityX() * delta);
            guardian.setY(guardian.getY() + guardian.getVelocityY() * delta);

            CollisionRect ground = collider.findOverlappingRect(guardian);
            if (ground != null) {
                int side = pushOut(ground);
                if (side == PUSH_UP && guardian.getVelocityY() <= 0f) {
                    guardian.setVelocityX(0);
                    guardian.setVelocityY(0);
                    guardian.setState(CrystalGuardian.State.DEATH_LAND);
                } else if (side == PUSH_LEFT || side == PUSH_RIGHT) {
                    guardian.setVelocityX(0); // hit a wall: drop straight down
                }
            } else if (guardian.getY() < -400f) {
                guardian.setDeadHandled(true); // fell out of the world
            }
        } else if (guardian.getState() == CrystalGuardian.State.DEATH_LAND) {
            if (guardian.getStateTime() >= DEATH_LAND_DURATION) {
                // Death Land has played once; the corpse settles and stays
                // visible (renderer clamps on the final frame).
                guardian.setDeadHandled(true);
            }
        }

        // Respawn once the knight has moved far away from the spawn point.
        if (guardian.isDeadHandled() && knight != null) {
            float dx = knight.getX() - guardian.getSpawnX();
            float dy = knight.getY() - guardian.getSpawnY();
            if (dx * dx + dy * dy >= RESPAWN_DISTANCE * RESPAWN_DISTANCE) {
                respawn();
            }
        }
    }

    private int pushOut(CollisionRect wall) {
        float pushLeft  = guardian.getRight() - wall.getLeft();  // move -x by this much
        float pushRight = wall.getRight() - guardian.getLeft();  // move +x
        float pushUp    = wall.getTop() - guardian.getBottom();  // move +y
        float pushDown  = guardian.getTop() - wall.getBottom();  // move -y

        float min = Math.min(Math.min(pushLeft, pushRight), Math.min(pushUp, pushDown));
        if (min == pushLeft) {
            guardian.setX(guardian.getX() - pushLeft);
            return PUSH_LEFT;
        } else if (min == pushRight) {
            guardian.setX(guardian.getX() + pushRight);
            return PUSH_RIGHT;
        } else if (min == pushUp) {
            guardian.setY(guardian.getY() + pushUp);
            return PUSH_UP;
        } else {
            guardian.setY(guardian.getY() - pushDown);
            return PUSH_DOWN;
        }
    }

    //  Public API

    @Override
    public void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale) {
        if (!guardian.isAlive()) return;

        guardian.takeDamage(damageAmount);
        guardian.startHitFlash();

        if (!guardian.isAlive()) {
            // Killed: the beam dies with it; pop up with the killing blow.
            guardian.getLaser().deactivate();
            guardian.setState(CrystalGuardian.State.DEATH_AIR);
            guardian.setVelocityX(dirX * DEATH_LAUNCH_VX * knockbackScale);
            guardian.setVelocityY(DEATH_LAUNCH_VY);
        } else if (dirX != 0f) {
            guardian.applyRecoil(dirX, knockbackScale);
        }
    }

    @Override
    public boolean overlapsKnight() {
        if (knight == null || !guardian.isAlive()) return false;
        return AABB.overlaps(guardian, knight.getHurtBox());
    }

    @Override
    public void respawn() {
        guardian.respawn();
        lastNailHitId = -1;
        snapToGround();
    }

    @Override
    public boolean isAlive() { return guardian.isAlive(); }

    @Override
    public AABB getBodyBox() { return guardian; }

    @Override
    public int getLastNailHitId() { return lastNailHitId; }

    @Override
    public void setLastNailHitId(int attackId) { this.lastNailHitId = attackId; }

    public CrystalGuardian getGuardian() { return guardian; }

    public Laser getLaser() { return guardian.getLaser(); }
}
