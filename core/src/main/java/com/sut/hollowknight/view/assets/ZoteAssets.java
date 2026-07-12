package com.sut.hollowknight.view.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Lookup helper for Zote's animations and the dialogue UI art
 * (spec: NPC Interaction - Zote). Frame arrays are resolved once at
 * construction time - never inside the render loop.
 */
public final class ZoteAssets {

    public static final String IDLE_ATLAS         = "animation/zote/Idle.atlas";
    public static final String TALK_ATLAS         = "animation/zote/Talk.atlas";
    public static final String ATTACK_ATLAS       = "animation/zote/Attack.atlas";
    public static final String ARROW_UP_ATLAS     = "ui/dialogue/Arrow Up.atlas";
    public static final String ARROW_DOWN_ATLAS   = "ui/dialogue/Arrow Down.atlas";
    public static final String FLEUR_TOP_ATLAS    = "ui/dialogue/Fleur Top.atlas";
    public static final String FLEUR_BOTTOM_ATLAS = "ui/dialogue/Fleur Bottom.atlas";
    public static final String PROMPT_PNG         = "ui/dialogue/prompt_idle0000.png";

    /** Zote's grunts (spec: Zote Voice SFX) - one plays at random. */
    public static final String[] VOICE_SFX = {
            "sfx/Zote_01.wav", "sfx/Zote_02.wav",
        "sfx/Zote_03.wav", "sfx/Zote_031.wav",
            "sfx/Zote_032.wav", "sfx/Zote_04.wav", "sfx/Zote_05.wav",
    };

    private final AssetManager manager;

    private final TextureRegion[] idleFrames;
    private final TextureRegion[] talkFrames;
    private final TextureRegion[] attackFrames;
    private final TextureRegion[] arrowUpFrames;
    private final TextureRegion[] arrowDownFrames;
    private final TextureRegion[] fleurTopFrames;
    private final TextureRegion[] fleurBottomFrames;
    private final Sound[] voices;

    public ZoteAssets(AssetManager manager) {
        this.manager      = manager;
        voices = new Sound[VOICE_SFX.length];
        for (int i = 0; i < VOICE_SFX.length; i++) {
            voices[i] = manager.get(VOICE_SFX[i], Sound.class);
        }
        idleFrames        = frames(IDLE_ATLAS,         "Idle");
        talkFrames        = frames(TALK_ATLAS,         "Talk");
        attackFrames      = frames(ATTACK_ATLAS,       "Attack");
        arrowUpFrames     = frames(ARROW_UP_ATLAS,     "Arrow Up");
        arrowDownFrames   = frames(ARROW_DOWN_ATLAS,   "Arrow Down");
        fleurTopFrames    = frames(FLEUR_TOP_ATLAS,    "Fleur Top Up");
        fleurBottomFrames = frames(FLEUR_BOTTOM_ATLAS, "Fleur Bot Up");
    }

    /** Atlas file order is scrambled; sort regions by their frame index. */
    /** Resolved once; the array is shared, never copied per call. */
    public Sound[] getVoices() { return voices; }

    private TextureRegion[] frames(String atlasPath, String regionName) {
        Array<TextureAtlas.AtlasRegion> regions =
                manager.get(atlasPath, TextureAtlas.class).findRegions(regionName);
        TextureRegion[] out = new TextureRegion[regions.size];
        for (int i = 0; i < regions.size; i++) {
            TextureAtlas.AtlasRegion region = regions.get(i);
            out[region.index] = region;
        }
        return out;
    }

    public TextureRegion[] getIdleFrames()        { return idleFrames; }
    public TextureRegion[] getTalkFrames()        { return talkFrames; }
    public TextureRegion[] getAttackFrames()      { return attackFrames; }
    public TextureRegion[] getArrowUpFrames()     { return arrowUpFrames; }
    public TextureRegion[] getArrowDownFrames()   { return arrowDownFrames; }
    public TextureRegion[] getFleurTopFrames()    { return fleurTopFrames; }
    public TextureRegion[] getFleurBottomFrames() { return fleurBottomFrames; }

    public Texture getPrompt() { return manager.get(PROMPT_PNG, Texture.class); }
}
