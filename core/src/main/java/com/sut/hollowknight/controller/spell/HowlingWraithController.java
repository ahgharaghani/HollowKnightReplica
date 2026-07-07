package com.sut.hollowknight.controller.spell;

import com.sut.hollowknight.model.spell.HowlingWraith;

/**
 * Advances a Howling Wraiths blast. The blast is stationary (no physics, no
 * collision probes) — this controller only drives its clock; damage ticks
 * are consumed by the CombatSystem.
 */
public class HowlingWraithController {

    private final HowlingWraith wraith;

    public HowlingWraithController(HowlingWraith wraith) {
        this.wraith = wraith;
    }

    public void update(float delta) {
        wraith.addAge(delta);
    }

    public HowlingWraith getWraith() {
        return wraith;
    }

    public boolean isDone() {
        return wraith.isDone();
    }
}
