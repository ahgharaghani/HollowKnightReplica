package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Tiktik;

public class TiktikController implements EnemyController {

    private final Tiktik tiktik;
    private final TileMapCollider collider;
    private Knight knight;

    private int lastNailHitId = -1;

    /** Cliff probe: a thin box just past the front foot, reaching below the feet. */
    // used for detecting end of the platform
    private static final float CLIFF_PROBE_WIDTH = 6f;
    private static final float CLIFF_PROBE_DEPTH = 14f;

    // Death: pop up with the killing blow, fall, land, settle as a corpse.
    private static final float DEATH_GRAVITY = 900f;
    private static final float DEATH_LAUNCH_VX = 150f;
    private static final float DEATH_LAUNCH_VY = 240f;
    /** Death Land plays 3 frames at 15 fps — once, then the corpse settles. */
    private static final float DEATH_LAND_DURATION = 0.2f;
    private static final float RESPAWN_DISTANCE = 2000f;

    // Push-out resolution sides
    private static final int PUSH_LEFT = 0, PUSH_RIGHT = 1, PUSH_UP = 2, PUSH_DOWN = 3;

    public TiktikController(Tiktik tiktik, TileMapCollider collider) {
        this.tiktik = tiktik;
        this.collider = collider;
        snapToGround();
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    private void snapToGround() {
        float bestTop = -Float.MAX_VALUE;
        for (CollisionRect rect : collider.getCollisionRects()) {
            boolean overlapsX = rect.getRight() > tiktik.getLeft()
                && rect.getLeft() < tiktik.getRight();
            if (!overlapsX) continue;
            float top = rect.getTop();
            if (top <= tiktik.getY() + 4f && top > bestTop) {
                bestTop = top;
            }
        }
        if (bestTop > -Float.MAX_VALUE) {
            tiktik.setY(bestTop);
        }
    }

    @Override
    public void update(float delta) {
        // Hit flash fades even during death (the killing blow flashes too).
        tiktik.tickHitFlash(delta);

        if (!tiktik.isAlive()) {
            updateDeath(delta);
            return;
        }

        tiktik.addStateTime(delta);
        updateWalk(delta);
        applyRecoil(delta);
    }

    private void updateWalk(float delta) {
        float dir = tiktik.isFacingRight() ? 1f : -1f;
        tiktik.setVelocityX(dir * Tiktik.WALK_SPEED);
        tiktik.setX(tiktik.getX() + tiktik.getVelocityX() * delta);

        // Wall ahead: resolve along the least-penetration axis and turn.
        CollisionRect wall = collider.findOverlappingRect(tiktik);
        if (wall != null) {
            int side = pushOut(wall);
            if (side == PUSH_LEFT)  tiktik.setFacingRight(false); // wall on the right
            if (side == PUSH_RIGHT) tiktik.setFacingRight(true);  // wall on the left
            return;
        }

        // Platform edge: probe below the front foot; when the platform's
        // collision ends, turn around instead of walking off.
        float frontX = dir > 0
            ? tiktik.getRight() + CLIFF_PROBE_WIDTH / 2f
            : tiktik.getLeft() - CLIFF_PROBE_WIDTH / 2f;
        boolean groundAhead = collider.overlapsAnyRect(
            frontX - CLIFF_PROBE_WIDTH / 2f, tiktik.getY() - CLIFF_PROBE_DEPTH,
            frontX + CLIFF_PROBE_WIDTH / 2f, tiktik.getY() - 1f);
        if (!groundAhead) {
            tiktik.setFacingRight(dir < 0);
        }
    }

    private void applyRecoil(float delta) {
        if (!tiktik.isRecoiling()) return;

        float falloff = tiktik.getRecoilTimer() / Tiktik.RECOIL_DURATION;
        float prevX = tiktik.getX();
        tiktik.setX(prevX + tiktik.getRecoilVelX() * falloff * delta);

        boolean hitWall = collider.findOverlappingRect(tiktik) != null;
        boolean groundBelow = collider.overlapsAnyRect(
            tiktik.getLeft(), tiktik.getY() - CLIFF_PROBE_DEPTH,
            tiktik.getRight(), tiktik.getY() - 1f);
        if (hitWall || !groundBelow) {
            tiktik.setX(prevX);
            tiktik.cancelRecoil();
            return;
        }

        tiktik.tickRecoil(delta);
    }

    private void updateDeath(float delta) {
        tiktik.addStateTime(delta);

        if (tiktik.getState() == Tiktik.State.DEATH_AIR) {
            tiktik.setVelocityY(tiktik.getVelocityY() - DEATH_GRAVITY * delta);
            tiktik.setX(tiktik.getX() + tiktik.getVelocityX() * delta);
            tiktik.setY(tiktik.getY() + tiktik.getVelocityY() * delta);

            CollisionRect ground = collider.findOverlappingRect(tiktik);
            if (ground != null) {
                int side = pushOut(ground);
                if (side == PUSH_UP && tiktik.getVelocityY() <= 0f) {
                    tiktik.setVelocityX(0);
                    tiktik.setVelocityY(0);
                    tiktik.setState(Tiktik.State.DEATH_LAND);
                } else if (side == PUSH_LEFT || side == PUSH_RIGHT) {
                    tiktik.setVelocityX(0); // hit a wall: drop straight down
                }
            } else if (tiktik.getY() < -400f) {
                tiktik.setDeadHandled(true); // fell out of the world
            }
        } else if (tiktik.getState() == Tiktik.State.DEATH_LAND) {
            if (tiktik.getStateTime() >= DEATH_LAND_DURATION) {
                // Death Land has played once; the corpse settles and stays
                // visible (renderer clamps on the final frame).
                tiktik.setDeadHandled(true);
            }
        }

        // Respawn once the knight has moved far away from the spawn point.
        if (tiktik.isDeadHandled() && knight != null) {
            float dx = knight.getX() - tiktik.getSpawnX();
            float dy = knight.getY() - tiktik.getSpawnY();
            if (dx * dx + dy * dy >= RESPAWN_DISTANCE * RESPAWN_DISTANCE) {
                respawn();
            }
        }
    }

    private int pushOut(CollisionRect wall) {
        float pushLeft  = tiktik.getRight() - wall.getLeft();  // move -x by this much
        float pushRight = wall.getRight() - tiktik.getLeft();  // move +x
        float pushUp    = wall.getTop() - tiktik.getBottom();  // move +y
        float pushDown  = tiktik.getTop() - wall.getBottom();  // move -y

        float min = Math.min(Math.min(pushLeft, pushRight), Math.min(pushUp, pushDown));
        if (min == pushLeft) {
            tiktik.setX(tiktik.getX() - pushLeft);
            return PUSH_LEFT;
        } else if (min == pushRight) {
            tiktik.setX(tiktik.getX() + pushRight);
            return PUSH_RIGHT;
        } else if (min == pushUp) {
            tiktik.setY(tiktik.getY() + pushUp);
            return PUSH_UP;
        } else {
            tiktik.setY(tiktik.getY() - pushDown);
            return PUSH_DOWN;
        }
    }

    //  Public API

    @Override
    public void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale) {
        if (!tiktik.isAlive()) return;

        tiktik.takeDamage(damageAmount);
        tiktik.startHitFlash();

        if (!tiktik.isAlive()) {
            // Killed (second slash): pop up with the killing blow. Rendered
            // with "Death Air Old", played once, then "Death Land" once.
            tiktik.setState(Tiktik.State.DEATH_AIR);
            tiktik.setVelocityX(dirX * DEATH_LAUNCH_VX * knockbackScale);
            tiktik.setVelocityY(DEATH_LAUNCH_VY);
        } else if (dirX != 0f) {
            tiktik.applyRecoil(dirX, knockbackScale);
        }
    }

    @Override
    public boolean overlapsKnight() {
        if (knight == null || !tiktik.isAlive()) return false;
        return AABB.overlaps(tiktik, knight.getHurtBox());
    }

    @Override
    public void respawn() {
        tiktik.respawn();
        lastNailHitId = -1;
        snapToGround();
    }

    @Override
    public boolean isAlive() { return tiktik.isAlive(); }

    @Override
    public AABB getBodyBox() { return tiktik; }

    @Override
    public int getLastNailHitId() { return lastNailHitId; }

    @Override
    public void setLastNailHitId(int attackId) { this.lastNailHitId = attackId; }

    public Tiktik getTiktik() { return tiktik; }
}
