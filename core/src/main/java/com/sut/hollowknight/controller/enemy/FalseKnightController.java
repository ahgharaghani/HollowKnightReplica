package com.sut.hollowknight.controller.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.sut.hollowknight.controller.GameController;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.FalseKnight;
import com.sut.hollowknight.model.enemy.FalseKnightShockwave;

/**
 * Drives the False Knight boss (spec: Boss Fight).
 *
 * AI DECISION SYSTEM (spec):
 *  - Distance-based weighting: each of the five moves has a chance table
 *    indexed by the knight's distance band (close / mid / far).
 *  - Randomization: the move is rolled from the weighted table, so the
 *    pattern is never a fixed loop.
 *  - Anti-spam: the previous move's weight is zeroed, so the same move can
 *    never be picked twice in a row.
 *  - Reactive defense: taking several nail hits within a short window
 *    forces the Defensive Leap regardless of the roll (spec).
 *
 * STUN & PHASE 2 (spec): at 50% HP the boss collapses once, the armor opens
 * and only the maggot head is hittable (double damage). Afterwards run
 * speed, attack animation speed and decision rate all increase, and the
 * Power Mace Slam (accelerating shockwave, double damage) unlocks.
 *
 * Zero allocations in the game loop: all boxes and the shockwave instance
 * are reused; the weight table is a preallocated float[].
 */
public class FalseKnightController implements EnemyController {

    // ---- Distance bands for the decision table ----
    // Bands scale with the doubled mace reach (480px ground slam).
    private static final float CLOSE_RANGE = 440f;
    private static final float FAR_RANGE   = 900f;

    // Weight tables: [close, mid, far] chance weight per move.
    private static final float[] W_MACE       = { 55f, 18f,  3f };
    private static final float[] W_CHARGE     = {  3f, 18f, 50f };
    private static final float[] W_LEAP       = { 12f, 40f, 22f };
    private static final float[] W_LEAP_BACK  = { 12f,  8f,  3f };
    private static final float[] W_POWER_SLAM = {  8f, 28f, 24f }; // phase 2 only

    // Idle gap between moves; divided by the decision multiplier in phase 2.
    private static final float IDLE_WAIT_MIN = 0.55f;
    private static final float IDLE_WAIT_MAX = 0.95f;

    // ---- Camera shake amplitudes/durations per impact ----
    private static final float SHAKE_MACE_AMP  = 9f,  SHAKE_MACE_DUR  = 0.25f;
    private static final float SHAKE_LAND_AMP  = 7f,  SHAKE_LAND_DUR  = 0.20f;
    private static final float SHAKE_POWER_AMP = 15f, SHAKE_POWER_DUR = 0.40f;
    private static final float SHAKE_STUN_AMP  = 10f, SHAKE_STUN_DUR  = 0.30f;
    private static final float SHAKE_DEATH_AMP = 12f, SHAKE_DEATH_DUR = 0.35f;

    /** The power hit box stays live only for the impact frames. */
    private static final float POWER_STRIKE_WINDOW = 0.25f;

    // Knockback the shockwave applies to the knight.
    private static final float WAVE_KNOCKBACK_X = 460f;
    private static final float WAVE_KNOCKBACK_Y = 300f;

    // Push-out resolution sides (same convention as the other enemies).
    private static final int PUSH_LEFT = 0, PUSH_RIGHT = 1, PUSH_UP = 2, PUSH_DOWN = 3;

    private final FalseKnight boss;
    private final TileMapCollider collider;
    private final Knight knight;
    private final GameController gameController;
    private final FalseKnightShockwave shockwave = new FalseKnightShockwave();

    /** The boss stands idle until the gate seals the arena. */
    private boolean activated;

    private int lastNailHitId = -1;

    private FalseKnight.Move pendingMove;
    private FalseKnight.Move jumpMove;      // which move launched the airborne arc
    private FalseKnight.Move lastMove;      // anti-spam memory
    private float idleWait = IDLE_WAIT_MAX;
    private float chargeTargetX;            // captured at charge start (spec)

    /** Horizontal leash so the boss never leaves the arena. */
    private float arenaMinX = -Float.MAX_VALUE;
    private float arenaMaxX = Float.MAX_VALUE;

    public FalseKnightController(FalseKnight boss, TileMapCollider collider,
                                 Knight knight, GameController gameController) {
        this.boss = boss;
        this.collider = collider;
        this.knight = knight;
        this.gameController = gameController;
        snapToGround();
    }

    public void setActivated(boolean activated) { this.activated = activated; }

    public void setArenaBounds(float minX, float maxX) {
        this.arenaMinX = minX;
        this.arenaMaxX = maxX;
    }

    private void snapToGround() {
        float bestTop = -Float.MAX_VALUE;
        for (int i = 0; i < collider.getCollisionRects().size(); i++) {
            CollisionRect rect = collider.getCollisionRects().get(i);
            boolean overlapsX = rect.getRight() > boss.getLeft()
                && rect.getLeft() < boss.getRight();
            if (!overlapsX) continue;
            float top = rect.getTop();
            if (top <= boss.getY() + 4f && top > bestTop) bestTop = top;
        }
        if (bestTop > -Float.MAX_VALUE) boss.setY(bestTop);
    }

    // ================================================================
    //  Frame update
    // ================================================================

    @Override
    public void update(float delta) {
        boss.tickTimers(delta);
        boss.tickDamageWindow(delta);
        updateShockwave(delta);

        if (!boss.isAlive()) {
            updateDeath(delta);
            return;
        }

        // Phase 2 speeds up the animation clock, which also shortens every
        // stateTime-based attack duration - one knob drives both (spec).
        boss.addStateTime(delta * boss.getAnimMult());

        switch (boss.getState()) {
            case IDLE:           updateIdle();             break;
            case TURN:           updateTurn();             break;
            case RAGE:           updateRage();             break;
            case RUN:            updateRun(delta);         break;
            case ATTACK_ANTIC:   updateAttackAntic();      break;
            case ATTACK:         updateAttack();           break;
            case ATTACK_RECOVER: updateAttackRecover();    break;
            case JUMP_ANTIC:     updateJumpAntic();        break;
            case JUMP:
            case POWER_JUMP:     updateAirborne(delta);    break;
            case LAND:           updateLand();             break;
            case POWER_HIT:      updatePowerHit();         break;
            case STUN_FALL:      updateStunFall(delta);    break;
            case STUN_GROUND:    updateStunGround();       break;
            case STUN_OPEN:      updateStunOpen();         break;
            case STUN_OPENED:    updateStunOpened();       break;
            case STUN_RECOVER:   updateStunRecover();      break;
            default: break;
        }
    }

    // ================================================================
    //  Decision making
    // ================================================================

    private void updateIdle() {
        boss.setVelocityX(0f);
        if (!activated) return; // waits for the gate to seal the arena
        if (boss.getStateTime() >= idleWait) decideNextMove();
    }

    /**
     * The heart of the AI decision system: distance-banded weights,
     * random roll, and anti-spam (last move weight = 0).
     */
    private void decideNextMove() {
        float dx = knight.getX() - boss.getX();
        float dist = Math.abs(dx);
        int band = dist < CLOSE_RANGE ? 0 : (dist > FAR_RANGE ? 2 : 1);

        float wMace  = lastMove == FalseKnight.Move.MACE_SLAM   ? 0f : W_MACE[band];
        float wCharge= lastMove == FalseKnight.Move.CHARGE      ? 0f : W_CHARGE[band];
        float wLeap  = lastMove == FalseKnight.Move.LEAP_ATTACK ? 0f : W_LEAP[band];
        float wBack  = lastMove == FalseKnight.Move.LEAP_BACK   ? 0f : W_LEAP_BACK[band];
        float wPower = !boss.isPhase2() ? 0f
            : (lastMove == FalseKnight.Move.POWER_SLAM ? 0f : W_POWER_SLAM[band]);

        float total = wMace + wCharge + wLeap + wBack + wPower;
        FalseKnight.Move move = FalseKnight.Move.MACE_SLAM;
        if (total > 0f) {
            float r = MathUtils.random() * total;
            if ((r -= wMace) < 0f)        move = FalseKnight.Move.MACE_SLAM;
            else if ((r -= wCharge) < 0f) move = FalseKnight.Move.CHARGE;
            else if ((r -= wLeap) < 0f)   move = FalseKnight.Move.LEAP_ATTACK;
            else if ((r -= wBack) < 0f)   move = FalseKnight.Move.LEAP_BACK;
            else                          move = FalseKnight.Move.POWER_SLAM;
        }

        pendingMove = move;
        boolean shouldFaceRight = dx >= 0f;
        if (boss.isFacingRight() != shouldFaceRight) {
            boss.setTurnTargetRight(shouldFaceRight);
            boss.setState(FalseKnight.State.TURN);
        } else {
            startMove(move);
        }
    }

    private void updateTurn() {
        if (boss.getStateTime() >= FalseKnight.TURN_DURATION) {
            boss.setFacingRight(boss.getTurnTargetRight());
            startMove(pendingMove);
        }
    }

    private void startMove(FalseKnight.Move move) {
        switch (move) {
            case MACE_SLAM:
                boss.setState(FalseKnight.State.ATTACK_ANTIC);
                break;
            case CHARGE:
                boss.setState(FalseKnight.State.RAGE);
                break;
            default: // all three jump moves share the crouch antic
                jumpMove = move;
                boss.setState(FalseKnight.State.JUMP_ANTIC);
                break;
        }
    }

    /** Move finished: remember it (anti-spam) and roll a fresh idle gap. */
    private void endMove(FalseKnight.Move move) {
        lastMove = move;
        boss.setVelocityX(0f);
        float decisionMult = boss.isPhase2() ? FalseKnight.PHASE2_DECISION_MULT : 1f;
        idleWait = MathUtils.random(IDLE_WAIT_MIN, IDLE_WAIT_MAX) / decisionMult;
        boss.setState(FalseKnight.State.IDLE);
    }

    // ================================================================
    //  Move 1: Mace Slam
    // ================================================================

    private void updateAttackAntic() {
        boss.setVelocityX(0f);
        if (boss.getStateTime() >= FalseKnight.ATTACK_ANTIC_DURATION) {
            boss.setState(FalseKnight.State.ATTACK);
            boss.setStrike(true, false);
            gameController.shakeCamera(SHAKE_MACE_AMP, SHAKE_MACE_DUR);
        }
    }

    private void updateAttack() {
        if (boss.getStateTime() >= FalseKnight.ATTACK_DURATION) {
            boss.setStrike(false, false);
            boss.setState(FalseKnight.State.ATTACK_RECOVER);
        }
    }

    private void updateAttackRecover() {
        if (boss.getStateTime() >= FalseKnight.ATTACK_RECOVER_DURATION) {
            endMove(FalseKnight.Move.MACE_SLAM);
        }
    }

    // ================================================================
    //  Move 2: Charge Run
    // ================================================================

    private void updateRage() {
        boss.setVelocityX(0f);
        if (boss.getStateTime() >= FalseKnight.RAGE_DURATION) {
            // The target is the knight's horizontal position at launch (spec).
            chargeTargetX = knight.getX();
            boss.setState(FalseKnight.State.RUN);
        }
    }

    private void updateRun(float delta) {
        float dir = boss.isFacingRight() ? 1f : -1f;
        boss.setVelocityX(dir * FalseKnight.CHARGE_SPEED * boss.getMoveMult());
        boss.setX(boss.getX() + boss.getVelocityX() * delta);
        clampToArena();

        CollisionRect wall = collider.findOverlappingRect(boss);
        if (wall != null) {
            pushOut(wall);
            endMove(FalseKnight.Move.CHARGE);
            return;
        }
        boolean reached = dir > 0
            ? boss.getX() >= chargeTargetX
            : boss.getX() <= chargeTargetX;
        if (reached || boss.getStateTime() >= FalseKnight.CHARGE_MAX_TIME) {
            endMove(FalseKnight.Move.CHARGE);
        }
    }

    // ================================================================
    //  Moves 3/4/5: the jumps
    // ================================================================

    private void updateJumpAntic() {
        boss.setVelocityX(0f);
        if (boss.getStateTime() >= FalseKnight.JUMP_ANTIC_DURATION) {
            launchJump();
        }
    }

    private void launchJump() {
        float dx = knight.getX() - boss.getX();
        switch (jumpMove) {
            case LEAP_ATTACK: {
                // Solve vx so the arc lands on the knight: vx = dx / airtime.
                float airtime = 2f * FalseKnight.LEAP_VY / FalseKnight.GRAVITY;
                float vx = MathUtils.clamp(dx / airtime,
                    -FalseKnight.LEAP_VX_MAX, FalseKnight.LEAP_VX_MAX);
                boss.setVelocityX(vx * boss.getMoveMult());
                boss.setVelocityY(FalseKnight.LEAP_VY);
                boss.setState(FalseKnight.State.JUMP);
                break;
            }
            case LEAP_BACK: {
                // Away from the knight - a defensive hop, no aim needed.
                float away = dx >= 0f ? -1f : 1f;
                boss.setVelocityX(away * FalseKnight.LEAP_BACK_VX);
                boss.setVelocityY(FalseKnight.LEAP_BACK_VY);
                boss.setState(FalseKnight.State.JUMP);
                break;
            }
            default: { // POWER_SLAM
                float airtime = 2f * FalseKnight.POWER_JUMP_VY / FalseKnight.GRAVITY;
                float vx = MathUtils.clamp(dx / airtime,
                    -FalseKnight.POWER_JUMP_VX_MAX, FalseKnight.POWER_JUMP_VX_MAX);
                boss.setVelocityX(vx);
                boss.setVelocityY(FalseKnight.POWER_JUMP_VY);
                boss.setState(FalseKnight.State.POWER_JUMP);
                break;
            }
        }
    }

    private void updateAirborne(float delta) {
        boss.setVelocityY(boss.getVelocityY() - FalseKnight.GRAVITY * delta);
        boss.setX(boss.getX() + boss.getVelocityX() * delta);
        boss.setY(boss.getY() + boss.getVelocityY() * delta);
        clampToArena();

        CollisionRect hit = collider.findOverlappingRect(boss);
        if (hit == null) return;

        int side = pushOut(hit);
        if (side == PUSH_UP && boss.getVelocityY() <= 0f) {
            landFromJump();
        } else if (side == PUSH_LEFT || side == PUSH_RIGHT) {
            boss.setVelocityX(0f); // wall: drop straight down
        } else if (side == PUSH_DOWN) {
            boss.setVelocityY(Math.min(boss.getVelocityY(), 0f)); // ceiling bonk
        }
    }

    private void landFromJump() {
        boss.setVelocityX(0f);
        boss.setVelocityY(0f);
        if (boss.getState() == FalseKnight.State.POWER_JUMP) {
            // Move 5 impact: strike box + shockwave toward the knight (spec).
            boss.setState(FalseKnight.State.POWER_HIT);
            boss.setStrike(true, true);
            gameController.shakeCamera(SHAKE_POWER_AMP, SHAKE_POWER_DUR);
            float waveDir = knight.getX() >= boss.getX() ? 1f : -1f;
            shockwave.spawn(boss.getX(), boss.getY(), waveDir);
        } else {
            boss.setState(FalseKnight.State.LAND);
            gameController.shakeCamera(SHAKE_LAND_AMP, SHAKE_LAND_DUR);
        }
    }

    private void updateLand() {
        if (boss.getStateTime() >= FalseKnight.LAND_DURATION) {
            endMove(jumpMove);
        }
    }

    private void updatePowerHit() {
        if (boss.getStateTime() >= POWER_STRIKE_WINDOW) {
            boss.setStrike(false, false);
        }
        if (boss.getStateTime() >= FalseKnight.POWER_HIT_DURATION) {
            endMove(FalseKnight.Move.POWER_SLAM);
        }
    }

    // ================================================================
    //  Shockwave (phase 2, move 5)
    // ================================================================

    private void updateShockwave(float delta) {
        if (!shockwave.isActive()) return;
        shockwave.update(delta);
        if (!shockwave.isActive()) return;

        CollisionRect box = shockwave.getDamageBox();

        // Dies against walls or at the arena edge (it travels along the floor,
        // so the probe is lifted a hair to ignore the ground rect itself).
        if (shockwave.getX() < arenaMinX || shockwave.getX() > arenaMaxX
            || collider.overlapsAnyRect(box.getLeft(), box.getBottom() + 4f,
                box.getRight(), box.getTop())) {
            shockwave.deactivate();
            return;
        }

        if (!knight.isInvincible()
            && AABB.overlaps(box, knight.getHurtBox())) {
            knight.takeDamage(FalseKnightShockwave.DAMAGE); // 2x mace damage
            knight.applyKnockback(shockwave.getDir() * WAVE_KNOCKBACK_X,
                WAVE_KNOCKBACK_Y);
        }
    }

    // ================================================================
    //  Stun (one-time, at 50% HP) and phase transition
    // ================================================================

    private void beginStun() {
        boss.setStunTriggered(true);
        boss.setStrike(false, false);
        boss.setVelocityX(0f);
        boss.clearDamageWindow();
        shockwave.deactivate();
        // "Immediately stops and falls" (spec) - even out of mid-air arcs.
        boss.setState(FalseKnight.State.STUN_FALL);
    }

    private void updateStunFall(float delta) {
        boss.setVelocityY(boss.getVelocityY() - FalseKnight.GRAVITY * delta);
        boss.setY(boss.getY() + boss.getVelocityY() * delta);

        CollisionRect ground = collider.findOverlappingRect(boss);
        if (ground != null && pushOut(ground) == PUSH_UP
            && boss.getVelocityY() <= 0f) {
            boss.setVelocityY(0f);
            gameController.shakeCamera(SHAKE_STUN_AMP, SHAKE_STUN_DUR);
            boss.setState(FalseKnight.State.STUN_GROUND);
        }
    }

    private void updateStunGround() {
        if (boss.getStateTime() >= FalseKnight.STUN_GROUND_DURATION) {
            boss.setState(FalseKnight.State.STUN_OPEN);
        }
    }

    private void updateStunOpen() {
        if (boss.getStateTime() >= FalseKnight.STUN_OPEN_DURATION) {
            boss.setState(FalseKnight.State.STUN_OPENED);
        }
    }

    private void updateStunOpened() {
        // The armor lies open; the maggot head is the only hittable box.
        if (boss.getStateTime() >= FalseKnight.STUN_OPENED_DURATION) {
            boss.setState(FalseKnight.State.STUN_RECOVER);
        }
    }

    private void updateStunRecover() {
        if (boss.getStateTime() >= FalseKnight.STUN_RECOVER_DURATION) {
            boss.setPhase2(true); // the fight escalates from here (spec)
            boss.clearDamageWindow();
            endMove(lastMove != null ? lastMove : FalseKnight.Move.MACE_SLAM);
        }
    }

    // ================================================================
    //  Death
    // ================================================================

    private void beginDeath() {
        boss.setStrike(false, false);
        boss.setVelocityX(0f);
        boss.setVelocityY(Math.max(boss.getVelocityY(), 0f));
        shockwave.deactivate();
        boss.setState(FalseKnight.State.DEATH_FALL);
        // Falsehood/Completion achievements & the gate watch this flag.
        if (GameSession.isActive()) {
            GameSession.getActive().bossDefeated = true;
        }
    }

    private void updateDeath(float delta) {
        boss.addStateTime(delta); // death plays at natural speed

        if (boss.getState() == FalseKnight.State.DEATH_FALL) {
            boss.setVelocityY(boss.getVelocityY() - FalseKnight.GRAVITY * delta);
            boss.setY(boss.getY() + boss.getVelocityY() * delta);
            CollisionRect ground = collider.findOverlappingRect(boss);
            if (ground != null && pushOut(ground) == PUSH_UP
                && boss.getVelocityY() <= 0f) {
                boss.setVelocityY(0f);
                gameController.shakeCamera(SHAKE_DEATH_AMP, SHAKE_DEATH_DUR);
                boss.setState(FalseKnight.State.DEATH_LAND);
            }
        } else if (boss.getState() == FalseKnight.State.DEATH_LAND) {
            if (boss.getStateTime() >= FalseKnight.DEATH_LAND_DURATION) {
                // The armor spazzes out and the head spills; the corpse stays.
                boss.setState(FalseKnight.State.DEATH_SPAZ);
                boss.setDeadHandled(true);
            }
        }
        // No distance respawn: a beaten boss stays down (see respawn()).
    }

    // ================================================================
    //  Helpers
    // ================================================================

    private void clampToArena() {
        float half = FalseKnight.WIDTH / 2f;
        if (boss.getX() - half < arenaMinX) boss.setX(arenaMinX + half);
        if (boss.getX() + half > arenaMaxX) boss.setX(arenaMaxX - half);
    }

    /** Min-penetration push-out, same convention as the other enemies. */
    private int pushOut(CollisionRect wall) {
        float pushLeft  = boss.getRight() - wall.getLeft();
        float pushRight = wall.getRight() - boss.getLeft();
        float pushUp    = wall.getTop() - boss.getBottom();
        float pushDown  = boss.getTop() - wall.getBottom();

        float min = Math.min(Math.min(pushLeft, pushRight), Math.min(pushUp, pushDown));
        if (min == pushLeft) {
            boss.setX(boss.getX() - pushLeft);
            return PUSH_LEFT;
        } else if (min == pushRight) {
            boss.setX(boss.getX() + pushRight);
            return PUSH_RIGHT;
        } else if (min == pushUp) {
            boss.setY(boss.getY() + pushUp);
            return PUSH_UP;
        } else {
            boss.setY(boss.getY() - pushDown);
            return PUSH_DOWN;
        }
    }

    private boolean isInterruptible() {
        switch (boss.getState()) {
            case IDLE: case TURN: case RAGE: case RUN:
            case LAND: case ATTACK_RECOVER:
                return true;
            default:
                return false;
        }
    }

    // ================================================================
    //  EnemyController
    // ================================================================

    @Override
    public void hitByNail(int damageAmount, float dirX, float dirY, float knockbackScale) {
        if (!boss.isAlive()) return;

        if (boss.isStunnedState()) {
            // Only the exposed maggot is hit here (getBodyBox routes the
            // slash test to the head box) - and it hurts double (spec).
            boss.takeDamage(damageAmount * FalseKnight.STUN_DAMAGE_MULT);
            boss.startStunHit();
        } else {
            boss.takeDamage(damageAmount);
            boss.registerHit(); // feeds the defensive-leap damage window
        }
        boss.startHitFlash();
        // Massive armor: no recoil - the boss never gets stun-locked.

        if (!boss.isAlive()) {
            beginDeath();
            return;
        }
        if (boss.shouldStun()) {
            beginStun();
            return;
        }
        // Heavy hits in a short window force the Defensive Leap (spec).
        if (!boss.isStunnedState() && boss.isUnderHeavyFire() && isInterruptible()) {
            boss.clearDamageWindow();
            jumpMove = FalseKnight.Move.LEAP_BACK;
            lastMove = FalseKnight.Move.LEAP_BACK;
            boss.setState(FalseKnight.State.JUMP_ANTIC);
        }
    }

    @Override
    public boolean overlapsKnight() {
        if (!boss.isAlive() || !activated) return false;
        if (boss.isStunnedState()) return false; // a stunned armor is harmless
        if (AABB.overlaps(boss, knight.getHurtBox())) return true;
        CollisionRect strike = boss.getStrikeBox();
        return strike != null && AABB.overlaps(strike, knight.getHurtBox());
    }

    @Override
    public void respawn() {
        // A defeated boss stays defeated for the session; the achievement
        // and the open gate must survive later knight deaths.
        if (GameSession.isActive() && GameSession.getActive().bossDefeated) return;
        boss.respawn();
        lastNailHitId = -1;
        lastMove = null;
        pendingMove = null;
        activated = false; // the gate re-arms the fight on re-entry
        shockwave.deactivate();
        snapToGround();
    }

    @Override
    public boolean isAlive() { return boss.isAlive(); }

    /** While stunned/dying only the maggot head is a valid nail target. */
    @Override
    public AABB getBodyBox() {
        return boss.isStunnedState() ? boss.getHeadBox() : boss;
    }

    @Override
    public int getLastNailHitId() { return lastNailHitId; }

    @Override
    public void setLastNailHitId(int attackId) { this.lastNailHitId = attackId; }

    public FalseKnight getFalseKnight() { return boss; }

    public FalseKnightShockwave getShockwave() { return shockwave; }
}
