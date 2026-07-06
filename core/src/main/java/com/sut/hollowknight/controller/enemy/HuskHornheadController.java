package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.HuskHornhead;

/**
 * Drives the Husk Hornhead state machine:
 *
 *   WALK (fixed time) -> REST (fixed time) -> WALK ...
 *   WALK/REST + knight inside the front vision rect -> ATTACK_ANTIC -> LUNGE
 *   LUNGE is blind: it only ends on a wall hit or at a cliff edge,
 *   then ATTACK_COOLDOWN -> WALK.
 */
public class HuskHornheadController implements EnemyController {

    private final HuskHornhead hornhead;
    private final TileMapCollider collider;
    private Knight knight;

    private int lastNailHitId = -1;

    /** Cliff probe: a thin box just past the front foot, reaching below the feet. */
    private static final float CLIFF_PROBE_WIDTH = 6f;
    private static final float CLIFF_PROBE_DEPTH = 14f;

    // Death: pop up with the killing blow, fall, land, settle as a corpse.
    private static final float DEATH_GRAVITY = 900f;
    private static final float DEATH_LAUNCH_VX = 150f;
    private static final float DEATH_LAUNCH_VY = 240f;
    /** Death Land plays 8 frames at 12 fps — once, then the corpse settles. */
    private static final float DEATH_LAND_DURATION = 8f / 12f;
    private static final float RESPAWN_DISTANCE = 2000f;

    // Push-out resolution sides
    private static final int PUSH_LEFT = 0, PUSH_RIGHT = 1, PUSH_UP = 2, PUSH_DOWN = 3;

    public HuskHornheadController(HuskHornhead hornhead, TileMapCollider collider) {
        this.hornhead = hornhead;
        this.collider = collider;
        snapToGround();
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    private void snapToGround() {
        float bestTop = -Float.MAX_VALUE;
        for (CollisionRect rect : collider.getCollisionRects()) {
            boolean overlapsX = rect.getRight() > hornhead.getLeft()
                && rect.getLeft() < hornhead.getRight();
            if (!overlapsX) continue;
            float top = rect.getTop();
            if (top <= hornhead.getY() + 4f && top > bestTop) {
                bestTop = top;
            }
        }
        if (bestTop > -Float.MAX_VALUE) {
            hornhead.setY(bestTop);
        }
    }

    @Override
    public void update(float delta) {
        // Hit flash fades even during death (the killing blow flashes too).
        hornhead.tickHitFlash(delta);

        if (!hornhead.isAlive()) {
            updateDeath(delta);
            return;
        }

        hornhead.addStateTime(delta);

        switch (hornhead.getState()) {
            case WALK:            updateWalk(delta);     break;
            case REST:            updateRest();          break;
            case TURN:            updateTurn();          break;
            case ATTACK_ANTIC:    updateAntic();         break;
            case LUNGE:           updateLunge(delta);    break;
            case ATTACK_COOLDOWN: updateCooldown();      break;
            default: break;
        }

        applyRecoil(delta);
    }

    //  Patrol

    private void updateWalk(float delta) {
        // Spotting the knight takes priority over everything else.
        if (seesKnight()) {
            startAttack();
            return;
        }

        float dir = hornhead.isFacingRight() ? 1f : -1f;
        hornhead.setVelocityX(dir * HuskHornhead.WALK_SPEED);
        hornhead.setX(hornhead.getX() + hornhead.getVelocityX() * delta);

        // Wall ahead: resolve along the least-penetration axis and turn.
        CollisionRect wall = collider.findOverlappingRect(hornhead);
        if (wall != null) {
            int side = pushOut(wall);
            if (side == PUSH_LEFT)  beginTurn(false); // wall on the right
            if (side == PUSH_RIGHT) beginTurn(true);  // wall on the left
            return;
        }

        // Platform edge: probe below the front foot; turn instead of falling.
        if (!groundAhead(dir)) {
            beginTurn(dir < 0);
            return;
        }

        // Walked long enough: stop and rest for a while.
        if (hornhead.getStateTime() >= HuskHornhead.WALK_DURATION) {
            hornhead.setVelocityX(0);
            hornhead.setState(HuskHornhead.State.REST);
        }
    }

    private void updateRest() {
        hornhead.setVelocityX(0);

        // Still watching its front while resting.
        if (seesKnight()) {
            startAttack();
            return;
        }

        if (hornhead.getStateTime() >= HuskHornhead.REST_DURATION) {
            hornhead.setState(HuskHornhead.State.WALK);
        }
    }

    private void updateTurn() {
        hornhead.setVelocityX(0);
        if (hornhead.getStateTime() >= HuskHornhead.TURN_DURATION) {
            hornhead.setFacingRight(hornhead.getTurnTargetRight());
            hornhead.setState(HuskHornhead.State.WALK);
        }
    }

    private void beginTurn(boolean targetRight) {
        hornhead.setVelocityX(0);
        hornhead.setTurnTargetRight(targetRight);
        hornhead.setState(HuskHornhead.State.TURN);
    }

    //  Attack

    private void startAttack() {
        hornhead.setVelocityX(0);
        hornhead.setState(HuskHornhead.State.ATTACK_ANTIC);
    }

    private void updateAntic() {
        hornhead.setVelocityX(0);
        if (hornhead.getStateTime() >= HuskHornhead.ATTACK_ANTIC_DURATION) {
            hornhead.setState(HuskHornhead.State.LUNGE);
        }
    }

    /**
     * The lunge is BLIND: the hornhead charges in its facing direction and
     * completely ignores the knight's position. It only stops when it slams
     * into a wall or reaches the edge of its platform.
     */
    private void updateLunge(float delta) {
        float dir = hornhead.isFacingRight() ? 1f : -1f;
        hornhead.setVelocityX(dir * HuskHornhead.LUNGE_SPEED);
        hornhead.setX(hornhead.getX() + hornhead.getVelocityX() * delta);

        // Slammed into a wall: stop and cool down.
        CollisionRect wall = collider.findOverlappingRect(hornhead);
        if (wall != null) {
            pushOut(wall);
            endLunge();
            return;
        }

        // Reached a cliff edge: skid to a halt right at the lip.
        if (!groundAhead(dir)) {
            endLunge();
        }
    }

    private void endLunge() {
        hornhead.setVelocityX(0);
        hornhead.setState(HuskHornhead.State.ATTACK_COOLDOWN);
    }

    private void updateCooldown() {
        hornhead.setVelocityX(0);
        if (hornhead.getStateTime() >= HuskHornhead.ATTACK_COOLDOWN_DURATION) {
            hornhead.setState(HuskHornhead.State.WALK);
        }
    }

    //  Perception

    /** True when the knight's hurtbox intersects the front vision rectangle. */
    private boolean seesKnight() {
        if (knight == null) return false;
        return AABB.overlaps(hornhead.getVisionRect(), knight.getHurtBox());
    }

    private boolean groundAhead(float dir) {
        float frontX = dir > 0
            ? hornhead.getRight() + CLIFF_PROBE_WIDTH / 2f
            : hornhead.getLeft() - CLIFF_PROBE_WIDTH / 2f;
        return collider.overlapsAnyRect(
            frontX - CLIFF_PROBE_WIDTH / 2f, hornhead.getY() - CLIFF_PROBE_DEPTH,
            frontX + CLIFF_PROBE_WIDTH / 2f, hornhead.getY() - 1f);
    }

    //  Recoil / death (same conventions as the other ground enemies)

    private void applyRecoil(float delta) {
        if (!hornhead.isRecoiling()) return;

        float falloff = hornhead.getRecoilTimer() / HuskHornhead.RECOIL_DURATION;
        float prevX = hornhead.getX();
        hornhead.setX(prevX + hornhead.getRecoilVelX() * falloff * delta);

        boolean hitWall = collider.findOverlappingRect(hornhead) != null;
        boolean groundBelow = collider.overlapsAnyRect(
            hornhead.getLeft(), hornhead.getY() - CLIFF_PROBE_DEPTH,
            hornhead.getRight(), hornhead.getY() - 1f);
        if (hitWall || !groundBelow) {
            hornhead.setX(prevX);
            hornhead.cancelRecoil();
            return;
        }

        hornhead.tickRecoil(delta);
    }

    private void updateDeath(float delta) {
        hornhead.addStateTime(delta);

        if (hornhead.getState() == HuskHornhead.State.DEATH_AIR) {
            hornhead.setVelocityY(hornhead.getVelocityY() - DEATH_GRAVITY * delta);
            hornhead.setX(hornhead.getX() + hornhead.getVelocityX() * delta);
            hornhead.setY(hornhead.getY() + hornhead.getVelocityY() * delta);

            CollisionRect ground = collider.findOverlappingRect(hornhead);
            if (ground != null) {
                int side = pushOut(ground);
                if (side == PUSH_UP && hornhead.getVelocityY() <= 0f) {
                    hornhead.setVelocityX(0);
                    hornhead.setVelocityY(0);
                    hornhead.setState(HuskHornhead.State.DEATH_LAND);
                } else if (side == PUSH_LEFT || side == PUSH_RIGHT) {
                    hornhead.setVelocityX(0); // hit a wall: drop straight down
                }
            } else if (hornhead.getY() < -400f) {
                hornhead.setDeadHandled(true); // fell out of the world
            }
        } else if (hornhead.getState() == HuskHornhead.State.DEATH_LAND) {
            if (hornhead.getStateTime() >= DEATH_LAND_DURATION) {
                // Death Land has played once; the corpse settles and stays
                // visible (renderer clamps on the final frame).
                hornhead.setDeadHandled(true);
            }
        }

        // Respawn once the knight has moved far away from the spawn point.
        if (hornhead.isDeadHandled() && knight != null) {
            float dx = knight.getX() - hornhead.getSpawnX();
            float dy = knight.getY() - hornhead.getSpawnY();
            if (dx * dx + dy * dy >= RESPAWN_DISTANCE * RESPAWN_DISTANCE) {
                respawn();
            }
        }
    }

    private int pushOut(CollisionRect wall) {
        float pushLeft  = hornhead.getRight() - wall.getLeft();  // move -x by this much
        float pushRight = wall.getRight() - hornhead.getLeft();  // move +x
        float pushUp    = wall.getTop() - hornhead.getBottom();  // move +y
        float pushDown  = hornhead.getTop() - wall.getBottom();  // move -y

        float min = Math.min(Math.min(pushLeft, pushRight), Math.min(pushUp, pushDown));
        if (min == pushLeft) {
            hornhead.setX(hornhead.getX() - pushLeft);
            return PUSH_LEFT;
        } else if (min == pushRight) {
            hornhead.setX(hornhead.getX() + pushRight);
            return PUSH_RIGHT;
        } else if (min == pushUp) {
            hornhead.setY(hornhead.getY() + pushUp);
            return PUSH_UP;
        } else {
            hornhead.setY(hornhead.getY() - pushDown);
            return PUSH_DOWN;
        }
    }

    //  Public API

    @Override
    public void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale) {
        if (!hornhead.isAlive()) return;

        hornhead.takeDamage(damageAmount);
        hornhead.startHitFlash();

        if (!hornhead.isAlive()) {
            // Killed: pop up with the killing blow, then fall and settle.
            hornhead.setState(HuskHornhead.State.DEATH_AIR);
            hornhead.setVelocityX(dirX * DEATH_LAUNCH_VX * knockbackScale);
            hornhead.setVelocityY(DEATH_LAUNCH_VY);
        } else if (dirX != 0f) {
            hornhead.applyRecoil(dirX, knockbackScale);
        }
    }

    @Override
    public boolean overlapsKnight() {
        if (knight == null || !hornhead.isAlive()) return false;
        return AABB.overlaps(hornhead, knight.getHurtBox());
    }

    @Override
    public void respawn() {
        hornhead.respawn();
        lastNailHitId = -1;
        snapToGround();
    }

    @Override
    public boolean isAlive() { return hornhead.isAlive(); }

    @Override
    public AABB getBodyBox() { return hornhead; }

    @Override
    public int getLastNailHitId() { return lastNailHitId; }

    @Override
    public void setLastNailHitId(int attackId) { this.lastNailHitId = attackId; }

    public HuskHornhead getHornhead() { return hornhead; }
}
