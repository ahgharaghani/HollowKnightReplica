package com.sut.hollowknight.view.ui;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.sut.hollowknight.view.assets.Assets;

public class AnimatedPointerButton extends TextButton {

    private final TextureAtlas pointerAtlas;
    private final Image leftPointer;
    private final Image rightPointer;
    private final Animation<TextureRegion> leftAnim;
    private final Animation<TextureRegion> rightAnim;

    private float animTime = 0f;
    private float maxAnimTime;
    private boolean isHovered = false;

    public AnimatedPointerButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);

        pointerAtlas = Assets.manager.get("ui/menu/pointer/MainMenuPointer.atlas");
        Array<TextureAtlas.AtlasRegion> atlasFrames = pointerAtlas.findRegions("main_menu_pointer_anim");
        Array<TextureRegion> rightPointerFrames = new Array<>();
        Array<TextureRegion> leftPointerFrames = new Array<>();

        for (TextureAtlas.AtlasRegion frame : atlasFrames) {
            leftPointerFrames.add(new TextureRegion(frame));

            TextureRegion flipped = new TextureRegion(frame);
            flipped.flip(true, false);
            rightPointerFrames.add(flipped);
        }

        Animation<TextureRegion> rightPointerAnim = new Animation<>(
            0.01f, rightPointerFrames, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> leftPointerAnim = new Animation<>(
            0.01f, leftPointerFrames, Animation.PlayMode.NORMAL);

        this.leftAnim = leftPointerAnim;
        this.rightAnim = rightPointerAnim;
        this.maxAnimTime = leftAnim.getAnimationDuration();

        // Create pointer images with first frame (invisible initially)
        leftPointer = new Image(leftAnim.getKeyFrame(0));
        rightPointer = new Image(rightAnim.getKeyFrame(0));
        leftPointer.setVisible(false);
        rightPointer.setVisible(false);

        addActor(leftPointer);
        addActor(rightPointer);

        // Hover detection
        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y,
                              int pointer, Actor fromActor) {
                isHovered = true;
                if (animTime <= 0) {
                    leftPointer.setVisible(true);
                    rightPointer.setVisible(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y,
                             int pointer, Actor toActor) {
                isHovered = false;
            }
        });
    }

    @Override
    public void layout() {
        super.layout();

        float gap = 0.5f;

        Label label = getLabel();

        // Get ACTUAL text dimensions (not the label widget size)
        float textWidth = label.getPrefWidth();

        // Get label widget position and size
        float labelX = label.getX();
        float labelWidth = label.getWidth();
        float labelCenterY = label.getY() + label.getHeight() / 2f;

        // Calculate actual text X position based on alignment
        float textX = calculateTextX(labelX, labelWidth, textWidth, label.getLabelAlign());

        // Position left pointer (just left of actual text)
        leftPointer.setPosition(
            textX - leftPointer.getPrefWidth() - gap,
            labelCenterY - leftPointer.getPrefHeight() / 2f
        );

        // Position right pointer (just right of actual text)
        rightPointer.setPosition(
            textX + textWidth + gap,
            labelCenterY - rightPointer.getPrefHeight() / 2f
        );
    }

    private float calculateTextX(float labelX, float labelWidth, float textWidth, int align) {
        if ((align & Align.center) != 0) {
            return labelX + (labelWidth - textWidth) / 2f;
        } else if ((align & Align.right) != 0) {
            return labelX + labelWidth - textWidth;
        } else {
            return labelX;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean wasZero = animTime <= 0;

        if (isHovered) {
            // Play forward
            animTime = Math.min(animTime + delta, maxAnimTime);
        } else {
            // Play backward (reverse animation for unhover)
            animTime = Math.max(animTime - delta, 0);
        }

        // Update frames
        if (animTime > 0 || wasZero) {
            leftPointer.setDrawable(new TextureRegionDrawable(
                leftAnim.getKeyFrame(animTime, false)));
            rightPointer.setDrawable(new TextureRegionDrawable(
                rightAnim.getKeyFrame(animTime, false)));

            // Hide when fully rewound
            if (animTime <= 0) {
                leftPointer.setVisible(false);
                rightPointer.setVisible(false);
            }
        }
    }
}
