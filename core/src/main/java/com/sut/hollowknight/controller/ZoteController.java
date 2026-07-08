package com.sut.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.sut.hollowknight.model.npc.Zote;
import com.sut.hollowknight.model.npc.ZotePrecepts;

/**
 * Drives the Zote NPC (spec: NPC Interaction - Zote): the proximity
 * LISTEN prompt, E to listen, Enter to page through dialogue, one
 * precept per repeat visit, and the harmless tantrum when the Knight
 * strikes him. Pure logic - the overlay/renderer only read state.
 */
public class ZoteController {

    /** Dialogue flow. Every phase except INACTIVE freezes the world. */
    public enum Phase {
        INACTIVE,      // roaming; prompt fades in/out with proximity
        PROMPT_HIDING, // E pressed: the oval reveal plays in reverse
        BOX_OPENING,   // box grows from small; fleurs sweep forward
        TYPING,        // words appear one by one, fast
        WAIT_INPUT,    // Arrow Up loops on the bottom fleur
        ARROW_DOWN,    // Enter pressed: Arrow Down blinks
        BOX_CLOSING    // reverse of the pop-up animation
    }

    // ---- Tunables ----
    public static final int   LISTEN_KEY  = Input.Keys.E;
    public static final int   ADVANCE_KEY = Input.Keys.ENTER;
    public static final float TALK_RANGE_X = 150f;
    public static final float TALK_RANGE_Y = 160f;
    /** Oval reveal sweeps fully open (or closed) in a quarter second. */
    public static final float PROMPT_SPEED = 4f;
    /** Box + fleur pop-up length: 9 fleur frames at 24 FPS. */
    public static final float BOX_ANIM_DURATION = 9f / 24f;
    /** "Fast" typewriter (spec): words revealed per second. */
    public static final float WORDS_PER_SECOND = 16f;
    /** Arrow Down blink: 3 frames at 12 FPS. */
    public static final float ARROW_DOWN_DURATION = 3f / 12f;
    // A little knockback, zero damage (spec).
    private static final float HIT_KNOCKBACK_VX = 260f;
    private static final float HIT_KNOCKBACK_VY = 130f;

    /** First-meeting dialogue: three consecutive pages (spec). */
    private static final String[] INTRO = {
        "You there! Why are you skulking about in the shadows?\n"
            + "Yes, your eyes do not deceive you. I am Zote the Mighty, "
            + "a knight of great renown. Tremble before me!",
        "While you were hiding here in your dingy little village, I "
            + "ventured into the dark pit below us and slew a great beast. "
            + "It had sharp mandibles and atrocious manners.",
        "Yes, yes. All glory to me. But I don't have time for your "
            + "adulation! I must rest and prepare for my next journey down.",
    };

    private final Zote zote;
    private final Knight knight;

    private Phase phase = Phase.INACTIVE;
    private float phaseTimer;
    private float promptProgress; // 0 = hidden, 1 = fully revealed

    // ---- Current conversation (reused holders, no per-frame garbage) ----
    private String[] lines = INTRO;
    private int lineIndex;
    private String[] words;      // current line, split once on line start
    private int visibleWords;
    private final StringBuilder visibleText = new StringBuilder(384);
    private final String[] preceptLine = new String[1];

    // ---- Progress (persisted through GameSession write-through) ----
    private boolean met;
    private int preceptIndex;

    public ZoteController(Zote zote, Knight knight) {
        this.zote = zote;
        this.knight = knight;
    }

    /** Restore saved progress (which dialogue plays on the next visit). */
    public void restoreProgress(boolean met, int preceptIndex) {
        this.met = met;
        this.preceptIndex = ((preceptIndex % ZotePrecepts.COUNT)
                + ZotePrecepts.COUNT) % ZotePrecepts.COUNT;
    }

    public void update(float delta) {
        zote.update(delta);
        switch (phase) {
            case INACTIVE:      updateInactive(delta);     break;
            case PROMPT_HIDING: updatePromptHiding(delta); break;
            case BOX_OPENING:
                phaseTimer += delta;
                if (phaseTimer >= BOX_ANIM_DURATION) beginLine(0);
                break;
            case TYPING:        updateTyping(delta);       break;
            case WAIT_INPUT:
                phaseTimer += delta; // drives the Arrow Up loop
                if (Gdx.input.isKeyJustPressed(ADVANCE_KEY)) {
                    setPhase(Phase.ARROW_DOWN);
                }
                break;
            case ARROW_DOWN:
                phaseTimer += delta;
                if (phaseTimer >= ARROW_DOWN_DURATION) {
                    if (lineIndex + 1 < lines.length) beginLine(lineIndex + 1);
                    else setPhase(Phase.BOX_CLOSING);
                }
                break;
            case BOX_CLOSING:
                phaseTimer += delta;
                if (phaseTimer >= BOX_ANIM_DURATION) finishDialogue();
                break;
        }
    }

    private void updateInactive(float delta) {
        if (!zote.isAngry()) {
            zote.setFacingRight(knight.getX() >= zote.getX());
        }
        boolean inRange = !zote.isAngry() && isKnightInRange();
        promptProgress += (inRange ? PROMPT_SPEED : -PROMPT_SPEED) * delta;
        promptProgress = Math.max(0f, Math.min(1f, promptProgress));

        if (inRange && promptProgress > 0.35f
                && Gdx.input.isKeyJustPressed(LISTEN_KEY)) {
            setPhase(Phase.PROMPT_HIDING);
            return;
        }
        checkNailHit();
    }

    private void updatePromptHiding(float delta) {
        promptProgress -= PROMPT_SPEED * delta;
        if (promptProgress <= 0f) {
            promptProgress = 0f;
            lines = met ? currentPrecept() : INTRO;
            zote.setTalking(true);
            setPhase(Phase.BOX_OPENING);
        }
    }

    private String[] currentPrecept() {
        preceptLine[0] = ZotePrecepts.get(preceptIndex);
        return preceptLine;
    }

    private void beginLine(int index) {
        lineIndex = index;
        words = lines[index].split(" ");
        visibleWords = 0;
        visibleText.setLength(0);
        setPhase(Phase.TYPING);
    }

    private void updateTyping(float delta) {
        phaseTimer += delta;
        int target = (int) (phaseTimer * WORDS_PER_SECOND);
        if (Gdx.input.isKeyJustPressed(ADVANCE_KEY)) {
            target = words.length; // impatient reader: reveal the whole line
        }
        if (target > words.length) target = words.length;
        while (visibleWords < target) {
            if (visibleWords > 0) visibleText.append(' ');
            visibleText.append(words[visibleWords]);
            visibleWords++;
        }
        if (visibleWords >= words.length) setPhase(Phase.WAIT_INPUT);
    }

    private void finishDialogue() {
        zote.setTalking(false);
        if (!met) {
            met = true; // intro heard; precepts start on the next visit
        } else {
            preceptIndex = (preceptIndex + 1) % ZotePrecepts.COUNT;
        }
        if (GameSession.isActive()) { // write-through: saving needs no change
            GameSession.getActive().zoteMet = met;
            GameSession.getActive().zotePreceptIndex = preceptIndex;
        }
        setPhase(Phase.INACTIVE);
    }

    /** Nail contact: no damage - just rage plus a little recoil (spec). */
    private void checkNailHit() {
        if (zote.isAngry() || !knight.isAttacking()) return;
        if (zote.getLastNailHitId() == knight.getAttackId()) return;
        CollisionRect slash = knight.getActiveSlashBox();
        if (slash == null || !slash.overlaps(zote.getBodyBox())) return;

        zote.setLastNailHitId(knight.getAttackId());
        zote.beginTantrum();
        promptProgress = 0f; // he is in no mood to be listened to
        float dir = knight.getX() < zote.getX() ? -1f : 1f;
        knight.applyKnockback(dir * HIT_KNOCKBACK_VX, HIT_KNOCKBACK_VY);
    }

    private boolean isKnightInRange() {
        float dx = knight.getX() - zote.getX();
        float dy = knight.getY() - zote.getY();
        // Per-axis squared checks - no sqrt needed.
        return dx * dx <= TALK_RANGE_X * TALK_RANGE_X
            && dy * dy <= TALK_RANGE_Y * TALK_RANGE_Y;
    }

    private void setPhase(Phase next) {
        phase = next;
        phaseTimer = 0f;
    }

    // ---- View accessors (overlay + renderer read, never write) ----

    /** True from the E press until the box has fully closed. */
    public boolean isDialogueActive() { return phase != Phase.INACTIVE; }

    public Phase getPhase()               { return phase; }
    public float getPhaseTimer()          { return phaseTimer; }
    public float getPromptProgress()      { return promptProgress; }
    public CharSequence getVisibleText()  { return visibleText; }

    /** 0..1 pop-up progress; also indexes the fleur sweep frames. */
    public float getBoxProgress() {
        switch (phase) {
            case BOX_OPENING:
                return Math.min(1f, phaseTimer / BOX_ANIM_DURATION);
            case TYPING:
            case WAIT_INPUT:
            case ARROW_DOWN:
                return 1f;
            case BOX_CLOSING:
                return Math.max(0f, 1f - phaseTimer / BOX_ANIM_DURATION);
            default:
                return 0f;
        }
    }
}
