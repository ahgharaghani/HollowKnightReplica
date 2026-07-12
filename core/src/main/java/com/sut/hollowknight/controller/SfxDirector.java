package com.sut.hollowknight.controller;

import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.view.assets.Sfx;

/**
 * Watches the Knight's state once per frame and fires hero-side sound
 * effects on state EDGES (spec: Audio SFX - nail slash, damage, soul
 * gain, focus).
 *
 * <p>Architecture: rather than sprinkling play() calls through the Knight
 * model (which must stay audio/render-agnostic - MVC), this director
 * observes the model exactly like a renderer does, comparing this frame's
 * state against last frame's. Every trigger below is edge-based, so a
 * held state never re-fires its sound. Zero allocations per frame.</p>
 *
 * <p>It also owns the three hero LOOPS (footsteps, focus charge, low-HP
 * heartbeat) and their handles; stopLoops() is the panic switch the
 * GameScreen calls on pause, room switches and victory.</p>
 */
public final class SfxDirector {

    /** Minimum gap between soul-gain dings - Howling Wraiths grants soul in
     *  rapid multi-ticks and machine-gunning the pickup sound is grating. */
    private static final float SOUL_SFX_COOLDOWN = 0.12f;

    // Per-sound mix gains (multiplied onto the user's SFX volume). The raw
    // wavs are mastered hot; these seat them under the combat foley.
    private static final float FOOTSTEPS_GAIN = 0.5f;
    private static final float SOUL_GAIN      = 0.25f;

    private final Knight knight;

    // ---- Previous-frame snapshot (edge detection) ----
    private int     prevAttackId;
    private boolean prevDashing;
    private boolean prevScreaming;
    private boolean prevFocusing;
    private boolean prevDead;
    private int     prevHp;
    private int     prevSoul;
    private boolean focusReadyFired;
    private float   soulSfxTimer;

    /** One-way latch: a shut-down director never plays anything again. */
    private boolean shutdown;

    // ---- Loop handles ----
    private long footstepsHandle = -1L;
    private long focusHandle     = -1L;
    private long heartbeatHandle = -1L;

    public SfxDirector(Knight knight) {
        this.knight = knight;
        // Seed the snapshot so loading into a game fires no phantom edges.
        prevAttackId  = knight.getAttackId();
        prevDashing   = knight.isDashing();
        prevScreaming = knight.isScreaming();
        prevFocusing  = knight.isFocusing();
        prevDead      = knight.isDead();
        prevHp        = knight.getHpMasks();
        prevSoul      = knight.getSoulAmount();
    }

    /** Call once per unpaused gameplay frame, after all combat resolution. */
    public void update(float delta) {
        if (shutdown) return;
        if (GameSettings.getInstance().isSfxMuted()) {
            // Mute must also silence already-running loops immediately.
            stopLoops();
            snapshot();
            return;
        }

        soulSfxTimer -= delta;

        // -- Nail slash (spec): attackId increments once per swing.
        if (knight.getAttackId() != prevAttackId) {
            Sfx.playRandom(Sfx.sword);
        }

        // -- Dash: rising edge only.
        if (knight.isDashing() && !prevDashing) {
            Sfx.play(Sfx.heroDash);
        }

        // -- Scream cast: rising edge.
        if (knight.isScreaming() && !prevScreaming) {
            Sfx.play(Sfx.heroScream);
        }

        // -- Player damage / heal (spec): drive off the HP delta so every
        //    source (contact, javelin, laser, shockwave) is covered without
        //    hooking each one. Heals only come from Focus, so an HP gain is
        //    the heal moment.
        int hp = knight.getHpMasks();
        if (hp < prevHp && !knight.isDead()) {
            Sfx.play(Sfx.heroDamage);
        } else if (hp > prevHp) {
            Sfx.play(Sfx.focusHeal);
        }

        // -- Death: rising edge (plays over the damage grunt).
        if (knight.isDead() && !prevDead) {
            Sfx.play(Sfx.heroDeath);
            stopLoops();
        }

        // -- Soul gain (spec): throttled ding per vessel fill.
        if (knight.getSoulAmount() > prevSoul && soulSfxTimer <= 0f) {
            Sfx.playRandom(Sfx.soulPickup, SOUL_GAIN);
            soulSfxTimer = SOUL_SFX_COOLDOWN;
        }

        // -- Focus (spec): charging loop while held, a "ready" chime when
        //    the charge completes (the heal lands via the HP edge above).
        boolean focusing = knight.isFocusing();
        if (focusing && !prevFocusing) {
            focusHandle = Sfx.loop(Sfx.focusCharging);
            focusReadyFired = false;
        } else if (!focusing && prevFocusing) {
            Sfx.stopLoop(Sfx.focusCharging, focusHandle);
            focusHandle = -1L;
        }
        if (focusing && !focusReadyFired
                && knight.getFocusTimer() >= knight.getFocusRequiredDuration()) {
            Sfx.play(Sfx.focusReady);
            focusReadyFired = true;
        }

        // -- Footsteps: loop only while actually running on the ground.
        boolean running = knight.isGrounded() && !knight.isDead()
            && knight.getState() == Knight.State.RUN;
        if (running && footstepsHandle == -1L) {
            footstepsHandle = Sfx.loop(Sfx.footsteps, FOOTSTEPS_GAIN);
        } else if (!running && footstepsHandle != -1L) {
            Sfx.stopLoop(Sfx.footsteps, footstepsHandle);
            footstepsHandle = -1L;
        }

        // -- Low-HP heartbeat: tension loop at one mask (handy extra).
        boolean critical = hp == 1 && !knight.isDead();
        if (critical && heartbeatHandle == -1L) {
            heartbeatHandle = Sfx.loop(Sfx.heartbeat);
        } else if (!critical && heartbeatHandle != -1L) {
            Sfx.stopLoop(Sfx.heartbeat, heartbeatHandle);
            heartbeatHandle = -1L;
        }

        snapshot();
    }

    /**
     * Permanent kill switch for screen teardown (room switches, dispose).
     *
     * <p>Why not just stopLoops(): on the transition frame the OLD screen
     * can still run one more updateLogic() pass AFTER switchRoom() resets
     * the fade phase - the director would happily restart the footsteps
     * loop, and that instance is orphaned once the screen swaps (the bug
     * where footsteps from the last room looped forever). The latch stops
     * update() from ever looping again, and the global stops below kill
     * any instance regardless of which handle (or screen) started it.</p>
     */
    public void shutdown() {
        shutdown = true;
        stopLoops();
        Sfx.stopAll(Sfx.footsteps);
        Sfx.stopAll(Sfx.focusCharging);
        Sfx.stopAll(Sfx.heartbeat);
    }

    /** Stops every owned loop - pause menu, room switches, victory, death. */
    public void stopLoops() {
        if (footstepsHandle != -1L) {
            Sfx.stopLoop(Sfx.footsteps, footstepsHandle);
            footstepsHandle = -1L;
        }
        if (focusHandle != -1L) {
            Sfx.stopLoop(Sfx.focusCharging, focusHandle);
            focusHandle = -1L;
        }
        if (heartbeatHandle != -1L) {
            Sfx.stopLoop(Sfx.heartbeat, heartbeatHandle);
            heartbeatHandle = -1L;
        }
    }

    private void snapshot() {
        prevAttackId  = knight.getAttackId();
        prevDashing   = knight.isDashing();
        prevScreaming = knight.isScreaming();
        prevFocusing  = knight.isFocusing();
        prevDead      = knight.isDead();
        prevHp        = knight.getHpMasks();
        prevSoul      = knight.getSoulAmount();
    }
}
