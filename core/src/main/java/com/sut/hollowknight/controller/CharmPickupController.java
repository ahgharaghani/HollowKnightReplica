package com.sut.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.charms.Charm;
import com.sut.hollowknight.model.map.CharmPickup;
import com.sut.hollowknight.model.map.RoomStateRegistry;

import java.util.List;

/**
 * Drives world charm pickups (spec: Void Heart pickable in the secret
 * room). Walking near a pickup reveals the COLLECT prompt (same oval
 * reveal as Zote's LISTEN); pressing the interact key collects it,
 * acquires the charm, and records the pickup so it never respawns this
 * session. No allocations in {@link #update(float)}.
 */
public class CharmPickupController {

    /** Same interact key as talking to Zote, for one consistent verb. */
    public static final int COLLECT_KEY = ZoteController.LISTEN_KEY;

    private static final float RANGE_X = 90f;
    private static final float RANGE_Y = 110f;
    private static final float PROMPT_FADE_SPEED = 4f;   // ~0.25s reveal

    private final List<CharmPickup> pickups;
    private final Knight knight;
    private final String mapPath;   // qualifies registry keys

    public CharmPickupController(List<CharmPickup> pickups, Knight knight,
                                 String mapPath) {
        this.pickups = pickups;
        this.knight = knight;
        this.mapPath = mapPath;
    }

    public void update(float delta) {
        for (int i = 0; i < pickups.size(); i++) {
            CharmPickup pickup = pickups.get(i);
            if (pickup.isCollected()) continue;

            float dx = Math.abs(knight.getX() - pickup.getX());
            float dy = Math.abs(knight.getY() - pickup.getY());
            boolean near = dx <= RANGE_X && dy <= RANGE_Y;

            float p = pickup.getPromptProgress()
                + (near ? delta : -delta) * PROMPT_FADE_SPEED;
            pickup.setPromptProgress(Math.max(0f, Math.min(1f, p)));

            if (near && Gdx.input.isKeyJustPressed(COLLECT_KEY)) {
                collect(pickup);
            }
        }
    }

    private void collect(CharmPickup pickup) {
        pickup.setCollected(true);
        RoomStateRegistry.markPickupCollected(
            mapPath + ":" + pickup.getName());

        if (pickup.getCharm() == Charm.VOID_HEART) {
            RoomStateRegistry.setVoidHeartCollected();
            // Fires the "Charmed" achievement hook inside CharmLoadout.
            knight.getCharms().setVoidHeartAcquired(true);
        }
    }

    public List<CharmPickup> getPickups() { return pickups; }
}
