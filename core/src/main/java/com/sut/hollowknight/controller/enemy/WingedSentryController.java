package com.sut.hollowknight.controller.enemy;

import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionResolver;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;

public class WingedSentryController {

    private final WingedSentry sentry;
    private final TileMapCollider collider;
    private Knight knight;

    private JavelinController javelinController;

    // Attack decision thresholds
    private static final float CHARGE_MIN_DISTANCE = 60f;
    private static final float CHARGE_MAX_DISTANCE = 200f;
    private static final float ESCAPE_RANGE_MULTIPLIER = 1.5f;

    // Y-axis lerp speed during charge anticipation
    private static final float CHARGE_Y_LERP_SPEED = 8f;

    // Passive-chase standoff: stop when bodies touch instead of burrowing to
    // the knight's center. Center-stacking + upward contact-knockback made both
    // ratchet skyward every time i-frames expired. Keep a body gap.
    private static final float CHASE_STANDOFF = 46f;

    public WingedSentryController(WingedSentry sentry, TileMapCollider collider) {
        this.sentry = sentry;
        this.collider = collider;
    }

    public void setKnight(Knight knight) {
        this.knight = knight;
        // Forward to an already-spawned javelin controller (if any).
        if (javelinController != null) {
            javelinController.setKnight(knight);
        }
    }

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
            case IDLE:
                updateIdle(delta);
                break;
            case TURN_TO_IDLE:
                updateTurnToIdle(delta);
                break;
            case CHARGE_ANTIC:
                updateChargeAntic(delta);
                break;
            case CHARGE:
                updateCharge(delta);
                break;
            case CHARGE_RECOVER:
                updateChargeRecover(delta);
                break;
            case CHASE:
                updateChase(delta);
                break;
            case THROW_ATTACK:
                updateThrowAttack(delta);
                break;
            default:
                break;
        }
    }

    //  Sentry State Handlers

    private void updateIdle(float delta) {
        if (isPlayerDetected()) {
            sentry.setDetectedPlayer(true);
            beginChargeAntic();
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
        sentry.setChargeStartX(sentry.getX());

        if (knight != null) {
            sentry.setLockedChargeY(knight.getY() + Knight.KNIGHT_HEIGHT / 2f - WingedSentry.HEIGHT / 2f);
            sentry.setFacingRight(knight.getX() > sentry.getX());
        } else {
            sentry.setLockedChargeY(sentry.getY());
        }
    }

    private void updateChargeAntic(float delta) {
        // Track player during anticipation so charge aims correctly
        if (knight != null) {
            sentry.setFacingRight(knight.getX() > sentry.getX());
            sentry.setLockedChargeY(knight.getY() + Knight.KNIGHT_HEIGHT / 2f - WingedSentry.HEIGHT / 2f);
        }

        float targetY = sentry.getLockedChargeY();
        float currentY = sentry.getY();
        float diff = targetY - currentY;

        if (Math.abs(diff) > 0.5f) {
            sentry.setY(currentY + diff * Math.min(1f, CHARGE_Y_LERP_SPEED * delta));
        } else {
            sentry.setY(targetY);
        }

        if (sentry.getStateTime() >= WingedSentry.CHARGE_ANTIC_DURATION) {
            sentry.setY(sentry.getLockedChargeY());
            beginCharge();
        }
    }

    private void beginCharge() {
        sentry.setState(WingedSentry.State.CHARGE);
        float dir = sentry.isFacingRight() ? 1f : -1f;
        sentry.setVelocityX(WingedSentry.CHARGE_SPEED * dir);
        sentry.setChargeStartX(sentry.getX());
    }

    private void updateCharge(float delta) {
        sentry.setX(sentry.getX() + sentry.getVelocityX() * delta);
        sentry.setY(sentry.getLockedChargeY());

        CollisionRect wall = collider.findOverlappingRect(sentry);
        if (wall != null) {
            CollisionResolver.pushOutHorizontally(sentry, wall);
            sentry.setVelocityX(0);
            sentry.setState(WingedSentry.State.CHARGE_RECOVER);
            sentry.resetAttackCooldown();
            return;
        }

        float distTraveled = Math.abs(sentry.getX() - sentry.getChargeStartX());
        if (distTraveled >= WingedSentry.MAX_CHARGE_DISTANCE) {
            sentry.setVelocityX(0);
            sentry.setState(WingedSentry.State.CHARGE_RECOVER);
            sentry.resetAttackCooldown();
        }
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
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        sentry.setFacingRight(dx > 0);

        // Escape detection: Break aggro if too far OR if line of sight is broken
        float escapeRange = WingedSentry.DETECTION_RANGE * ESCAPE_RANGE_MULTIPLIER;
        boolean hasLoS = collider.hasLineOfSight(
            sentry.getX(), sentry.getY() + WingedSentry.HEIGHT / 2f,
            knight.getX(), knight.getY() + Knight.KNIGHT_HEIGHT / 2f
        );

        if (dist > escapeRange || !hasLoS) {
            sentry.setDetectedPlayer(false);
            sentry.setState(WingedSentry.State.TURN_TO_IDLE);
            return;
        }

        // Attack decision
        if (sentry.getAttackCooldown() <= 0) {
            if (dist >= CHARGE_MIN_DISTANCE && dist < CHARGE_MAX_DISTANCE) {
                beginChargeAntic();
                return;
            } else if (dist >= CHARGE_MAX_DISTANCE) {
                sentry.setState(WingedSentry.State.THROW_ATTACK);
                sentry.setVelocityX(0);
                sentry.setVelocityY(0);
                return;
            }
        }

        // Movement toward player — halt at standoff so the sentry never
        // stacks on the knight's center and elevator-launches the pair.
        if (dist > CHASE_STANDOFF) {
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

            // Spawn the javelin so its VISUAL center y lines up with the
            // sentry's visual center y. Javelin now uses bottom-y
            // convention (like everyone else), so we shift the spawn
            // y down by Javelin.HEIGHT/2 to compensate.
            float spawnX = sentry.getX();
            float spawnY = sentry.getY() + WingedSentry.HEIGHT / 2f - Javelin.HEIGHT / 2f;
            Javelin javelin = new Javelin(spawnX, spawnY, dirX);

            javelinController = new JavelinController(javelin, collider);
            javelinController.setKnight(knight);
        }

        if (sentry.getStateTime() >= throwDuration) {
            sentry.setState(WingedSentry.State.CHASE);
            sentry.resetAttackCooldown();
        }
    }

    private void updateDeath(float delta) {
        if (sentry.getState() == WingedSentry.State.DEATH_AIR) {
            sentry.setVelocityY(sentry.getVelocityY() - 400f * delta);
            sentry.setX(sentry.getX() + sentry.getVelocityX() * delta);
            sentry.setY(sentry.getY() + sentry.getVelocityY() * delta);

            float deathAirDuration = 2f / 10f;
            if (sentry.getStateTime() >= deathAirDuration) {
                sentry.setState(WingedSentry.State.DEATH_LAND);
                sentry.setVelocityX(0);
                sentry.setVelocityY(0);
            }
        } else if (sentry.getState() == WingedSentry.State.DEATH_LAND) {
            float deathLandDuration = 4f / 10f;
            if (sentry.getStateTime() >= deathLandDuration) {
                sentry.setDeadHandled(true);
            }
        }
    }

    //  Public API

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

        if (distSq <= rangeSq) {
            // Check Line of Sight before detecting
            return collider.hasLineOfSight(
                sentry.getX(), sentry.getY() + WingedSentry.HEIGHT / 2f,
                knight.getX(), knight.getY() + Knight.KNIGHT_HEIGHT / 2f
            );
        }
        return false;
    }

    public boolean overlapsKnight() {
        if (knight == null || !sentry.isAlive()) return false;
        WingedSentry.State s = sentry.getState();
        if (s == WingedSentry.State.DEATH_AIR || s == WingedSentry.State.DEATH_LAND) return false;

        return AABB.overlaps(sentry, knight.getHurtBox());
    }

    public void respawn() {
        sentry.respawn();
        javelinController = null;
    }

    public WingedSentry getSentry() { return sentry; }

    // Javelin delegation

    public Javelin getJavelin() {
        return javelinController == null ? null : javelinController.getJavelin();
    }

    public boolean isJavelinDamageDealt() {
        return javelinController != null && javelinController.isDamageDealt();
    }

    public void clearJavelinDamageDealt() {
        if (javelinController != null) javelinController.clearDamageDealt();
    }
}
