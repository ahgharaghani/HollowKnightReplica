package com.sut.hollowknight.view.animator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.view.assets.Assets;

import java.util.Comparator;

public class KnightAnimator {

    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> jumpAnim;
    private Animation<TextureRegion> fallAnim;
    private Animation<TextureRegion> landAnim;

    private static final float IDLE_ANIM_DURATION = (float) 1 / 10;
    private static final float WALK_ANIM_DURATION = (float) 1 / 12;
    private static final float JUMP_ANIM_DURATION = (float) 1 / 12;
    private static final float FALL_ANIM_DURATION = (float) 1 / 12;
    private static final float LAND_ANIM_DURATION = (float) 1 / 12;

    public KnightAnimator() {
        loadAnimations();
    }

    private void loadAnimations() {
        // --- Idle ---
        TextureAtlas idleAtlas = Assets.manager.get(
            "animation/knight/KnightIdle.atlas", TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> idleFrames = idleAtlas.getRegions();
        if (idleFrames.size == 0) {
            throw new IllegalStateException(
                "KnightIdle.atlas loaded but contains 0 regions. Check atlas format.");
        }
        for (Texture t : idleAtlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        idleAnim = new Animation<>(IDLE_ANIM_DURATION, idleFrames, Animation.PlayMode.LOOP);

        // --- Walk ---
        if (Assets.manager.isLoaded("animation/knight/KnightWalk.atlas")) {
            TextureAtlas walkAtlas = Assets.manager.get(
                "animation/knight/KnightWalk.atlas", TextureAtlas.class);
            Array<TextureAtlas.AtlasRegion> walkFrames = walkAtlas.findRegions("KnightWalk");
            if (walkFrames.size == 0) walkFrames = walkAtlas.getRegions();
            walkAnim = new Animation<>(WALK_ANIM_DURATION, walkFrames, Animation.PlayMode.LOOP);
        }

        // --- Airborne (Jump & Fall) ---
        if (Assets.manager.isLoaded("animation/knight/Airborne.atlas")) {
            TextureAtlas airborneAtlas = Assets.manager.get(
                "animation/knight/Airborne.atlas", TextureAtlas.class);
            for (Texture t : airborneAtlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            Array<TextureAtlas.AtlasRegion> jumpFrames = new Array<>();
            Array<TextureAtlas.AtlasRegion> fallFrames = new Array<>();

            for (TextureAtlas.AtlasRegion region : airborneAtlas.getRegions()) {
                if (region.name.startsWith("Airborne_jump")) {
                    jumpFrames.add(region);
                } else if (region.name.startsWith("Airborne_fall")) {
                    fallFrames.add(region);
                }
            }

            Comparator<TextureAtlas.AtlasRegion> indexComparator =
                Comparator.comparingInt(r -> r.index);
            jumpFrames.sort(indexComparator);
            fallFrames.sort(indexComparator);

            if (jumpFrames.size > 0) {
                // NORMAL play mode so it plays once per each jump
                jumpAnim = new Animation<>(JUMP_ANIM_DURATION, jumpFrames, Animation.PlayMode.NORMAL);
            }

            if (fallFrames.size > 0) {
                // LOOP play mode so it plays on repeat while falling
                fallAnim = new Animation<>(FALL_ANIM_DURATION, fallFrames, Animation.PlayMode.LOOP);
            }
        }

        // --- Land ---
        if (Assets.manager.isLoaded("animation/knight/Land.atlas")) {
            TextureAtlas landAtlas = Assets.manager.get(
                "animation/knight/Land.atlas", TextureAtlas.class);
            for (Texture t : landAtlas.getTextures()) {
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }

            Array<TextureAtlas.AtlasRegion> landFrames = landAtlas.findRegions("Land");
            if (landFrames.size == 0) {
                landFrames = landAtlas.getRegions();
                landFrames.sort(Comparator.comparingInt(r -> r.index));
            }

            if (landFrames.size > 0) {
                landAnim = new Animation<>(LAND_ANIM_DURATION, landFrames, Animation.PlayMode.NORMAL);
            }
        }
    }

    public TextureRegion getCurrentFrame(Knight knight) {
        Animation<TextureRegion> anim;
        switch (knight.getState()) {
            case RUN:
                anim = walkAnim != null ? walkAnim : idleAnim;
                break;
            case JUMP:
                anim = jumpAnim != null ? jumpAnim : idleAnim;
                break;
            case FALL:
                anim = fallAnim != null ? fallAnim : idleAnim;
                break;
            case LAND:
                anim = landAnim != null ? landAnim : idleAnim;
                break;
            case HURT:
                // TODO: drop in hurtAnim once the knockback art exists.
                anim = idleAnim;
                break;
            case IDLE:
            default:
                anim = idleAnim;
                break;
        }
        return anim.getKeyFrame(knight.getStateTime());
    }
}
