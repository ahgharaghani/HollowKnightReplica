package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;

public class WingedSentryController implements EnemyController {

    private final WingedSentry sentry;
    private final TileMapCollider collider;
    private Knight knight;

    private JavelinController javelinController;

    private int lastNailHitId = -1;

    // Attack decision thresholds (world px, scaled to the new detection range)
    private static final float CHARGE_MIN_DISTANCE = 150f;
    private static final float CHARGE_MAX_DISTANCE = 480f;
    private static final float ESCAPE_RANGE_MULTIPLIER = 1.5f;

    // Idle patrol: drift back and forth in a small straight line around spawn.
    private static final float PATROL_RANGE = 120f;
    private static final float PATROL_SPEED = 45f;

    // Passive-chase standoff: stop when bodies roughly touch instead of
    // burrowing to the knight's center (sized for the tuned body boxes).
    private static final float CHASE_STANDOFF = 120f;

    // Death: fall under gravity until the corpse lands on solid ground.
    private static final float DEATH_GRAVITY = 900f;
    /** Death Land plays 4 frames at 8 fps. */
    private static final float DEATH_LAND_DURATION = 0.5f;
    /** Respawn once the knight is at least this far from the spawn point. */
    private static final float RESPAWN_DISTANCE = 2000f;

    public WingedSentryController(WingedSentry sentry, TileMapCollider collider) {
        this.sentry = sentry;
        this.collider = collider;
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
    }

    @Override
    public void update(float delta) {
        if (javelinController != null) {
            javelinController.update(delta);
            if (javelinController.getJavelin().isDone()) {
                javelinController = null;
            }
        }

        if (!sentry.isAlive()) {
            updateDeath(delta);
            return;
        }

        sentry.addStateTime(delta);
        sentry.tickAttackCooldown(delta);

        switch (sentry.getState()) {
            case IDLE: updateIdle(delta); break;
            case TURN_TO_IDLE: updateTurnToIdle(delta); break;
            case CHARGE_ANTIC: updateChargeAntic(delta); break;
            case CHARGE: updateCharge(delta); break;
            case CHARGE_RECOVER: updateChargeRecover(delta); break;
            case CHASE: updateChase(delta); break;
            case THROW_ATTACK: updateThrowAttack(delta); break;
            default: break;
        }
    }

    //  Sentry State Handlers

    private void updateIdle(float delta) {
        if (isPlayerDetected()) {
            sentry.setDetectedPlayer(true);
            beginChargeAntic();
            return;
        }

        // Patrol: fly a small straight line back and forth around the spawn.
        float dir = sentry.isFacingRight() ? 1f : -1f;
        sentry.setVelocityX(dir * PATROL_SPEED);
        sentry.setX(sentry.getX() + sentry.getVelocityX() * delta);

        CollisionRect wall = collider.findOverlappingRect(sentry);
        if (wall != null) {
            CollisionResolver.pushOutHorizontally(sentry, wall);
            sentry.setFacingRight(dir < 0); // bounce off the wall
        } else if (sentry.getX() >= sentry.getSpawnX() + PATROL_RANGE) {
            sentry.setFacingRight(false);
        } else if (sentry.getX() <= sentry.getSpawnX() - PATROL_RANGE) {
            sentry.setFacingRight(true);
        }
    }

    private void updateTurnToIdle(float delta) {
        float turnDuration = 0.9f;
        if (sentry.getStateTime() >= turnDuration) {
            if (sentry.hasDetectedPlayer() && knight != null) {
                sentry.setState(WingedSentry.State.CHASE);
            } else {
                sentry.setState(WingedSentry.State.IDLE);
            }
        }
    }

    private void beginChargeAntic() {
        sentry.setState(WingedSentry.State.CHARGE_ANTIC);
        sentry.setVelocityX(0);
        sentry.setVelocityY(0);
        if (knight != null) {
            sentry.setFacingRight(knight.getX() > sentry.getX());
        }
    }

    private void updateChargeAntic(float delta) {
        // Track player during anticipation so charge aims correctly
        if (knight != null) {
            sentry.setFacingRight(knight.getX() > sentry.getX());
        }

        if (sentry.getStateTime() >= WingedSentry.CHARGE_ANTIC_DURATION) {
            beginCharge();
        }
    }

    private void beginCharge() {
        // Aim at the knight's center at launch time — diagonal charges allowed,
        // like the original game. Direction is locked for the whole charge.
        float dirX = sentry.isFacingRight() ? 1f : -1f;
        float dirY = 0f;
        if (knight != null) {
            float dx = knight.getX() - sentry.getX();
            float dy = (knight.getY() + Knight.KNIGHT_HEIGHT / 2f)
                     - (sentry.getY() + WingedSentry.HEIGHT / 2f);
            float len = (float) Math.sqrt(dx * dx + dy * dy);
            if (len > 1f) {
                dirX = dx / len;
                dirY = dy / len;
            }
        }
        sentry.setFacingRight(dirX > 0);
        sentry.setChargeDir(dirX, dirY);
        sentry.setChargeStartX(sentry.getX());
        sentry.setChargeStartY(sentry.getY());
        sentry.setVelocityX(WingedSentry.CHARGE_SPEED * dirX);
        sentry.setVelocityY(WingedSentry.CHARGE_SPEED * dirY);
        sentry.setState(WingedSentry.State.CHARGE);
    }

    private void updateCharge(float delta) {
        sentry.setX(sentry.getX() + sentry.getVelocityX() * delta);
        float mapH = collider.getMapHeightPx();
        float newY = Math.max(0, Math.min(sentry.getY() + sentry.getVelocityY() * delta,
            mapH - WingedSentry.HEIGHT));
        sentry.setY(newY);

        CollisionRect wall = collider.findOverlappingRect(sentry);
        if (wall != null) {
            CollisionResolver.pushOutHorizontally(sentry, wall);
            endCharge();
            return;
        }

        float dx = sentry.getX() - sentry.getChargeStartX();
        float dy = sentry.getY() - sentry.getChargeStartY();
        if (dx * dx + dy * dy
            >= WingedSentry.MAX_CHARGE_DISTANCE * WingedSentry.MAX_CHARGE_DISTANCE) {
            endCharge();
        }
    }

    private void endCharge() {
        sentry.setVelocityX(0);
        sentry.setVelocityY(0);
        sentry.setState(WingedSentry.State.CHARGE_RECOVER);
        sentry.resetAttackCooldown();
    }

    private void updateChargeRecover(float delta) {
        if (sentry.getStateTime() >= WingedSentry.CHARGE_RECOVER_DURATION) {
            sentry.setState(WingedSentry.State.CHASE);
        }
    }

    private void updateChase(float delta) {
        if (knight == null) {
            sentry.setState(WingedSentry.State.IDLE);
            return;
        }

        float knightCenterX = knight.getX();
        float knightCenterY = knight.getY() + Knight.KNIGHT_HEIGHT / 2f;
        float dx = knightCenterX - sentry.getX();
        float dy = knightCenterY - (sentry.getY() + WingedSentry.HEIGHT / 2f);
        // Compare squared distances; sqrt only when a direction is needed.
        float distSq = dx * dx + dy * dy;

        sentry.setFacingRight(dx > 0);

        // Escape detection: Break aggro if too far OR if line of sight is broken
        float escapeRange = WingedSentry.DETECTION_RANGE * ESCAPE_RANGE_MULTIPLIER;
        boolean hasLoS = collider.hasLineOfSight(
            sentry.getX(), sentry.getY() + WingedSentry.HEIGHT / 2f,
            knight.getX(), knight.getY() + Knight.KNIGHT_HEIGHT / 2f
        );

        if (distSq > escapeRange * escapeRange || !hasLoS) {
            sentry.setDetectedPlayer(false);
            sentry.setState(WingedSentry.State.TURN_TO_IDLE);
            return;
        }

        // Attack decision
        if (sentry.getAttackCooldown() <= 0) {
            if (distSq >= CHARGE_MIN_DISTANCE * CHARGE_MIN_DISTANCE
                && distSq < CHARGE_MAX_DISTANCE * CHARGE_MAX_DISTANCE) {
                beginChargeAntic();
                return;
            } else if (distSq >= CHARGE_MAX_DISTANCE * CHARGE_MAX_DISTANCE) {
                sentry.setState(WingedSentry.State.THROW_ATTACK);
                sentry.setVelocityX(0);
                sentry.setVelocityY(0);
                return;
            }
        }

        // Movement toward player — halt at standoff so the sentry never
        // stacks on the knight's center and elevator-launches the pair.
        if (distSq > CHASE_STANDOFF * CHASE_STANDOFF) {
            float dist = (float) Math.sqrt(distSq); // needed only to normalise
            float moveX = (dx / dist) * WingedSentry.CHASE_SPEED * delta;
            float moveY = (dy / dist) * WingedSentry.CHASE_SPEED * delta;
            sentry.setX(sentry.getX() + moveX);

            float mapH = collider.getMapHeightPx();
            sentry.setY(Math.max(0, Math.min(sentry.getY() + moveY, mapH - WingedSentry.HEIGHT)));
        }
    }

    private void updateThrowAttack(float delta) {
        float throwDuration = 13f / 10f;
        float javelinReleaseTime = 6f / 10f;

        if (sentry.getStateTime() >= javelinReleaseTime && javelinController == null) {
            // Update facing at throw moment
            if (knight != null) {
                sentry.setFacingRight(knight.getX() > sentry.getX());
            }

            float dirX = sentry.isFacingRight() ? 1f : -1f;
            float spawnX = sentry.getX();
            float spawnY = sentry.getY() + WingedSentry.HEIGHT / 2f - Javelin.HEIGHT / 2f;
            Javelin javelin = new Javelin(spawnX, spawnY, dirX);

            javelinController = new JavelinController(javelin, collider);
        }

        if (sentry.getStateTime() >= throwDuration) {
            sentry.setState(WingedSentry.State.CHASE);
            sentry.resetAttackCooldown();
        }
    }

    private void updateDeath(float delta) {
        sentry.addStateTime(delta);

        if (sentry.getState() == WingedSentry.State.DEATH_AIR) {
            // Fall under gravity until the corpse lands on solid ground.
            sentry.setVelocityY(sentry.getVelocityY() - DEATH_GRAVITY * delta);
            sentry.setX(sentry.getX() + sentry.getVelocityX() * delta);
            sentry.setY(sentry.getY() + sentry.getVelocityY() * delta);

            CollisionRect ground = collider.findOverlappingRect(sentry);
            if (ground != null && sentry.getVelocityY() <= 0f) {
                sentry.setY(ground.getTop());
                sentry.setVelocityX(0);
                sentry.setVelocityY(0);
                sentry.setState(WingedSentry.State.DEATH_LAND);
            } else if (sentry.getY() < -400f) {
                // Safety net: fell out of the world — allow a later respawn.
                sentry.setDeadHandled(true);
            }
        } else if (sentry.getState() == WingedSentry.State.DEATH_LAND) {
            if (sentry.getStateTime() >= DEATH_LAND_DURATION) {
                // Corpse has settled; it stays visible (renderer clamps on the
                // last frame) and is non-interactive.
                sentry.setDeadHandled(true);
            }
        }

        // Respawn once the knight has moved far away from the spawn point.
        if (sentry.isDeadHandled() && knight != null) {
            float dx = knight.getX() - sentry.getSpawnX();
            float dy = knight.getY() - sentry.getSpawnY();
            if (dx * dx + dy * dy >= RESPAWN_DISTANCE * RESPAWN_DISTANCE) {
                respawn();
            }
        }
    }

    //  Public API

    @Override
    public void hitByNail(int damageAmount, int knockbackDir, float knockbackForce) {
        if (!sentry.isAlive()) return;

        sentry.takeDamage(damageAmount);

        if (!sentry.isAlive()) {
            sentry.setState(WingedSentry.State.DEATH_AIR);
            sentry.setVelocityX(knockbackDir * knockbackForce * 0.3f);
            sentry.setVelocityY(50f);
        } else {
            WingedSentry.State current = sentry.getState();
            if (current != WingedSentry.State.CHARGE && current != WingedSentry.State.CHARGE_ANTIC) {
                sentry.setX(sentry.getX() + knockbackDir * 15f);
                sentry.setState(WingedSentry.State.CHASE);
            }
        }
    }

    private boolean isPlayerDetected() {
        if (knight == null) return false;
        float dx = knight.getX() - sentry.getX();
        float dy = (knight.getY() + Knight.KNIGHT_HEIGHT / 2f) - (sentry.getY() + WingedSentry.HEIGHT / 2f);
        float distSq = dx * dx + dy * dy;
        float rangeSq = WingedSentry.DETECTION_RANGE * WingedSentry.DETECTION_RANGE;
        if (distSq > rangeSq) return false;

        // Vision matches the facing direction — no eyes in the back.
        boolean inFront = (dx >= 0f) == sentry.isFacingRight();
        if (!inFront) return false;

        // Check Line of Sight before detecting
        return collider.hasLineOfSight(
            sentry.getX(), sentry.getY() + WingedSentry.HEIGHT / 2f,
            knight.getX(), knight.getY() + Knight.KNIGHT_HEIGHT / 2f
        );
    }

    @Override
    public boolean overlapsKnight() {
        if (knight == null || !sentry.isAlive()) return false;
        WingedSentry.State s = sentry.getState();
        if (s == WingedSentry.State.DEATH_AIR || s == WingedSentry.State.DEATH_LAND) return false;

        return AABB.overlaps(sentry, knight.getHurtBox());
    }

    @Override
    public void respawn() {
        sentry.respawn();
        javelinController = null;
        lastNailHitId = -1;
    }

    @Override
    public int getLastNailHitId() { return lastNailHitId; }

    @Override
    public void setLastNailHitId(int attackId) { this.lastNailHitId = attackId; }

    public WingedSentry getSentry() { return sentry; }
    public Javelin getJavelin() { return javelinController == null ? null : javelinController.getJavelin(); }
}
