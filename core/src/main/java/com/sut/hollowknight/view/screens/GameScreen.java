package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sut.hollowknight.model.collision.AABB;
import com.sut.hollowknight.model.collision.CollisionRect;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.sut.hollowknight.controller.CheatController;
import com.sut.hollowknight.controller.ZoteController;
import com.sut.hollowknight.controller.CombatSystem;
import com.sut.hollowknight.controller.GameController;
import com.sut.hollowknight.controller.PauseController;
import com.sut.hollowknight.controller.enemy.CrystalGuardianController;
import com.sut.hollowknight.controller.enemy.EnemyController;
import com.sut.hollowknight.controller.enemy.HuskHornheadController;
import com.sut.hollowknight.controller.enemy.TiktikController;
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.controller.spell.HowlingWraithController;
import com.sut.hollowknight.controller.spell.VengefulSpiritController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.GameSession;
import com.sut.hollowknight.model.AchievementsRegistry;
import com.sut.hollowknight.model.GameSettings;
import com.sut.hollowknight.model.charms.Charm;
import com.sut.hollowknight.model.npc.Zote;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.map.BreakableWall;
import com.sut.hollowknight.model.map.CharmPickup;
import com.sut.hollowknight.model.map.DarknessZone;
import com.sut.hollowknight.model.map.RoomStateRegistry;
import com.sut.hollowknight.model.map.RoomTransition;
import com.sut.hollowknight.controller.BreakableWallsController;
import com.sut.hollowknight.controller.CharmPickupController;
import com.sut.hollowknight.view.effects.ParticleHook;
import com.sut.hollowknight.view.ui.CharmPickupOverlay;
import com.sut.hollowknight.model.enemy.CrystalGuardian;
import com.sut.hollowknight.model.enemy.HuskHornhead;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.Laser;
import com.sut.hollowknight.model.enemy.Tiktik;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.view.animator.KnightAnimator;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.assets.HudAssets;
import com.sut.hollowknight.view.assets.InventoryAssets;
import com.sut.hollowknight.view.assets.ZoteAssets;
import com.sut.hollowknight.view.assets.CrystalGuardianAssets;
import com.sut.hollowknight.view.assets.HuskHornheadAssets;
import com.sut.hollowknight.view.assets.TiktikAssets;
import com.sut.hollowknight.view.assets.WingedSentryAssets;
import com.sut.hollowknight.view.assets.HowlingWraithAssets;
import com.sut.hollowknight.view.assets.VengefulSpiritAssets;
import com.sut.hollowknight.view.effects.GlassRainEffect;
import com.sut.hollowknight.view.hud.HudRenderer;
import com.sut.hollowknight.view.ui.PauseOverlay;
import com.sut.hollowknight.view.ui.InventoryOverlay;
import com.sut.hollowknight.view.ui.AchievementToastOverlay;
import com.sut.hollowknight.view.ui.ZoteDialogueOverlay;
import com.sut.hollowknight.view.effects.RainEffect;
import com.sut.hollowknight.model.spell.HowlingWraith;
import com.sut.hollowknight.model.spell.VengefulSpirit;
import com.sut.hollowknight.view.renderer.enemy.CrystalGuardianRenderer;
import com.sut.hollowknight.view.renderer.spell.HowlingWraithRenderer;
import com.sut.hollowknight.view.renderer.spell.VengefulSpiritRenderer;
import com.sut.hollowknight.view.renderer.ZoteRenderer;
import com.sut.hollowknight.view.renderer.enemy.HuskHornheadRenderer;
import com.sut.hollowknight.controller.enemy.FalseKnightController;
import com.sut.hollowknight.model.enemy.FalseKnight;
import com.sut.hollowknight.model.map.BossGate;
import com.sut.hollowknight.view.assets.FalseKnightAssets;
import com.sut.hollowknight.view.renderer.enemy.FalseKnightRenderer;
import com.sut.hollowknight.view.renderer.enemy.JavelinRenderer;
import com.sut.hollowknight.view.renderer.enemy.TiktikRenderer;
import com.sut.hollowknight.view.renderer.enemy.WingedSentryRenderer;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends AbstractScreen {

    private GameController controller;
    private KnightAnimator knightAnimator;
    private RainEffect rainEffect;
    private GlassRainEffect glassRainEffect;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private SpriteBatch batch;
    private float mapWidthPx;
    private float mapHeightPx;

    private static final float INVINCIBLE_FLASH_HZ = 20f;   // toggle rate → ~10 visible blinks/sec

    // Effect overlay anchors. Up/down slash and dash effects live on canvases
    // that differ from the body canvas (169x192, 182x209, 401x217 vs 349x186),
    // so they need explicit offsets measured from the art's opaque pixels.
    private static final float UP_SLASH_EFFECT_OFFSET_X   = 10f;   // forward, along facing
    private static final float UP_SLASH_EFFECT_OFFSET_Y   = 60f;   // above the feet
    private static final float DOWN_SLASH_EFFECT_OFFSET_Y = -150f; // below the feet
    private static final float DASH_EFFECT_OFFSET_X       = -175f;
    private static final float DASH_EFFECT_OFFSET_Y       = -75f;

    private static final String[] BG_LAYER_NAMES = {
        "FarFarFarBackgound", "FarFarBackground", "FarBackground",
        "CracksOnTheWall", "MidBackground"
    };
    private static final String[] GAMEPLAY_LAYER_NAMES = { "Gameplay" };
    private static final String[] FG_LAYER_NAMES = { "Foreground", "ForeForeground" };

    private int[] bgLayers;
    private int[] gameplayLayers;
    private int[] fgLayers;

    // Cached screen-space projection — rebuilt in place, never reallocated.
    private final Matrix4 screenMatrix = new Matrix4();

    private ShaderProgram backgroundShader;
    private Texture whiteTexture;
    private float timeElapsed = 0f;

    private WingedSentryAssets wingedSentryAssets;
    private List<WingedSentryController> sentryControllers;
    private WingedSentryRenderer sentryRenderer;
    private JavelinRenderer javelinRenderer;

    private TiktikAssets tiktikAssets;
    private List<TiktikController> tiktikControllers;
    private TiktikRenderer tiktikRenderer;

    private HuskHornheadAssets hornheadAssets;
    private List<HuskHornheadController> hornheadControllers;
    private HuskHornheadRenderer hornheadRenderer;

    // Boss fight (spec): the False Knight and the arena gate.
    private FalseKnightController falseKnightController;
    private FalseKnightRenderer falseKnightRenderer;
    private BossGate bossGate;
    private TiledMapTileLayer bossGateLayer;
    /** Camera clamp before the fight seals the arena, for restore. */
    private float camMinX, camMinY, camMaxX, camMaxY;

    private CrystalGuardianAssets guardianAssets;
    private List<CrystalGuardianController> guardianControllers;
    private CrystalGuardianRenderer guardianRenderer;

    private VengefulSpiritRenderer spiritRenderer;

    /** Draws Howling Wraiths blasts (plume + base shockwave). */
    private HowlingWraithRenderer wraithRenderer;
    private InventoryOverlay inventoryOverlay;

    // ---- Zote NPC (spec: NPC Interaction - Zote) ----
    private Zote zote;
    private ZoteController zoteController;
    private ZoteRenderer zoteRenderer;
    private ZoteDialogueOverlay zoteDialogue;
    private AchievementToastOverlay achievementToasts;

    // ---- Death fade (spec: fade to black, reset at spawn point) ----
    private static final float DEATH_FADE_OUT_DURATION = 1f;
    private static final float DEATH_FADE_IN_DURATION  = 0.5f;
    /** 0 = inactive, 1 = fading to black, 2 = fading back in. */
    private int deathFadePhase;
    private float deathFadeTimer;

    // ---- Breakable walls, darkness zones & room transitions (spec) ----
    /** Spec: 0.4s fade out, then 1s of full darkness - the room swap and
     *  the actors' ground snap happen under it - then 0.4s fade in. */
    private static final float ROOM_FADE_OUT_DURATION = 0.4f;
    private static final float ROOM_DARKNESS_DURATION = 1f;
    private static final float ROOM_FADE_IN_DURATION  = 0.4f;
    /** Map file this screen runs and the entry point we arrived through. */
    private final String mapPath;
    private final String entrySpawnName;   // null = fresh start
    private BreakableWallsController wallsController;
    private CharmPickupController charmPickupController;
    private CharmPickupOverlay charmPickupOverlay;
    private List<RoomTransition> roomTransitions;
    private List<DarknessZone> darknessZones;
    private List<CharmPickup> charmPickups;
    /** "BreakableWalls" tile layer, drawn apart so hits can shake it. */
    private int[] breakableWallLayers = new int[0];
    private TiledMapTileLayer breakableWallLayer;
    /** Future particle emitter seam - NO_OP until one is plugged in. */
    private ParticleHook particleHook = ParticleHook.NO_OP;
    /** Spec: the secret room draws over pure black, no shader backdrop. */
    private boolean blackBackground;
    /** 0 = inactive, 1 = fading out (exit), 2 = fading in (arrival),
     *  3 = darkness hold on arrival (screen stays fully black). */
    private int roomFadePhase;
    private float roomFadeTimer;
    private RoomTransition pendingTransition;

    private List<EnemyController> enemyControllers;

    private HudRenderer hudRenderer;

    // ---- Pause menu (ESC) ----
    private PauseController pauseController;
    private PauseOverlay pauseOverlay;
    /** Set when "Settings" is clicked; consumed in renderGraphics after the
     *  world is drawn (so the captured frame excludes the dim overlay). */
    private boolean settingsRequested = false;

    private CheatController cheatController;

    private CombatSystem combat;

    private TileMapCollider collider;

    // Debug hitbox overlay — toggle with F3. Tune box constants to the art.
    private ShapeRenderer debugShapes;
    private boolean debugBoxes = false;
    private boolean f3WasDown = false;

    public GameScreen(Game game) {
        this(game, "CityOfTears.tmx", null, -1, -1);
    }

    /**
     * Room-transition entry (spec: Room Transitions): load {@code mapPath},
     * spawn at the point object named {@code spawnName}, and carry the
     * knight's masks/soul across so crossing a door never heals or hurts.
     * Negative carried values mean "fresh start defaults".
     */
    public GameScreen(Game game, String mapPath, String spawnName,
                      int carriedMasks, int carriedSoul) {
        super(game);
        this.mapPath = mapPath;
        this.entrySpawnName = spawnName;
        if (spawnName == null) {
            // Fresh start from the menu: broken walls, revealed darkness
            // and collected pickups from the last run are forgotten.
            RoomStateRegistry.clear();
        }

        batch = new SpriteBatch();
        debugShapes = new ShapeRenderer();
        loadMap();

        collider = new TileMapCollider(
            tiledMap,
            "Gameplay",
            "Collision",
            "Damaging",
            "SafeSpots"
        );

        float[] spawn = findSpawnPoint(entrySpawnName);
        Knight knight = new Knight(spawn[0], spawn[1]);
        knight.restoreVitals(carriedMasks, carriedSoul);

        controller = new GameController(knight, collider,
            worldCamera, mapWidthPx, mapHeightPx);

        initMapFeatures(knight);
        if (entrySpawnName != null) {
            // Arrived through a door: hold full darkness first (spec: 1s),
            // then fade the new room in.
            roomFadePhase = 3;
            roomFadeTimer = 0f;
            controller.snapCameraToKnight();
        }

        knightAnimator = new KnightAnimator();

        initShader();
        initRainEffect();
        glassRainEffect = new GlassRainEffect(tiledMap, mapHeightPx);

        initWingedSentries(collider, knight);
        initTiktiks(collider, knight);
        initHornheads(collider, knight);
        initGuardians(collider, knight);
        initFalseKnight(collider, knight);
        initBossGate();

        enemyControllers = new ArrayList<>();
        enemyControllers.addAll(sentryControllers);
        enemyControllers.addAll(tiktikControllers);
        enemyControllers.addAll(hornheadControllers);
        enemyControllers.addAll(guardianControllers);
        if (falseKnightController != null) {
            enemyControllers.add(falseKnightController);
        }
        combat = new CombatSystem(knight, enemyControllers);

        float[] bossArena = findBossArena();
        cheatController = new CheatController(knight, enemyControllers,
            bossArena[0], bossArena[1]);

        spiritRenderer = new VengefulSpiritRenderer(new VengefulSpiritAssets(Assets.manager));
        wraithRenderer = new HowlingWraithRenderer(new HowlingWraithAssets(Assets.manager));

        hudRenderer = new HudRenderer(new HudAssets(Assets.manager));

        pauseController = new PauseController(game);
        pauseOverlay = new PauseOverlay(
            pauseController.getCheatCodesText(),
            () -> {                       // Continue
                pauseController.resume(this);
                pauseOverlay.setVisible(false);
            },
            () -> settingsRequested = true, // Settings (deferred to render)
            () -> pauseController.saveAndQuit(this));
        uiStage.addActor(pauseOverlay);

        // ---- Achievements (spec: Achievements) ----
        if (GameSession.isActive()) {
            AchievementsRegistry.getInstance()
                .syncFrom(GameSession.getActive().unlockedAchievementIds);
        }
        achievementToasts = new AchievementToastOverlay();
        AchievementsRegistry.setUnlockListener(achievementToasts);

        // ---- Charm inventory (spec: Charms & Inventory System) ----
        if (GameSession.isActive()) {
            knight.getCharms().loadFromNames(GameSession.getActive().equippedCharms);
            knight.getCharms().setVoidHeartAcquired(GameSession.getActive().bossDefeated
                || RoomStateRegistry.isVoidHeartCollected());
        }
        inventoryOverlay = new InventoryOverlay(
            knight, new InventoryAssets(Assets.manager),
            uiStage.getViewport().getWorldWidth(),
            uiStage.getViewport().getWorldHeight());
        uiStage.addActor(inventoryOverlay);

        // ---- Zote the Mighty at his ZoteSpawn marker ----
        float[] zoteSpawn = findZoteSpawn();
        zote = new Zote(zoteSpawn[0], zoteSpawn[1]);
        zoteController = new ZoteController(zote, knight);
        if (GameSession.isActive()) {
            zoteController.restoreProgress(
                    GameSession.getActive().zoteMet,
                    GameSession.getActive().zotePreceptIndex);
        }
        ZoteAssets zoteAssets = new ZoteAssets(Assets.manager);
        zoteController.setVoices(zoteAssets.getVoices());
        zoteRenderer = new ZoteRenderer(zoteAssets);
        zoteDialogue = new ZoteDialogueOverlay(zoteController, zote, zoteAssets);

        // ---- World charm pickups (spec: Void Heart, COLLECT prompt) ----
        // Same prompt art as Zote's LISTEN, caption swapped to COLLECT.
        charmPickupOverlay = new CharmPickupOverlay(charmPickupController,
            zoteAssets.getPrompt(), new InventoryAssets(Assets.manager));

        if (entrySpawnName != null) {
            // Spec: everyone stands on the ground before the reveal, even
            // if the knight flew through the door mid-jump.
            snapActorsToGround();
            controller.snapCameraToKnight();
        }
    }

    private void initWingedSentries(TileMapCollider collider, Knight knight) {
        wingedSentryAssets = new WingedSentryAssets(Assets.manager);
        sentryRenderer = new WingedSentryRenderer(wingedSentryAssets);
        javelinRenderer = new JavelinRenderer(wingedSentryAssets);

        sentryControllers = new ArrayList<>();

        MapLayer spawnLayer = tiledMap.getLayers().get("SentrySpawns");
        if (spawnLayer == null) {
            Gdx.app.log("GameScreen", "SentrySpawns layer not found! No sentries spawned.");
            return;
        }

        for (MapObject obj : spawnLayer.getObjects()) {
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                Gdx.app.log("GameScreen", "Spawning sentry at (" + x + ", " + y + ")");
                WingedSentry sentry = new WingedSentry(x, y);
                WingedSentryController sentryController = new WingedSentryController(sentry, collider);
                sentryController.setKnight(knight); // Allow controller to read knight position/HP
                sentryControllers.add(sentryController);
            }
        }
    }

    private void initTiktiks(TileMapCollider collider, Knight knight) {
        tiktikAssets = new TiktikAssets(Assets.manager);
        tiktikRenderer = new TiktikRenderer(tiktikAssets);

        tiktikControllers = new ArrayList<>();

        MapLayer spawnLayer = tiledMap.getLayers().get("TiktikSpawns");
        if (spawnLayer == null) {
            Gdx.app.log("GameScreen", "TiktikSpawns layer not found! No tiktiks spawned.");
            return;
        }

        for (MapObject obj : spawnLayer.getObjects()) {
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                Gdx.app.log("GameScreen", "Spawning tiktik at (" + x + ", " + y + ")");
                Tiktik tiktik = new Tiktik(x, y);
                TiktikController tiktikController = new TiktikController(tiktik, collider);
                tiktikController.setKnight(knight);
                tiktikControllers.add(tiktikController);
            }
        }
    }

    private void initHornheads(TileMapCollider collider, Knight knight) {
        hornheadAssets = new HuskHornheadAssets(Assets.manager);
        hornheadRenderer = new HuskHornheadRenderer(hornheadAssets);

        hornheadControllers = new ArrayList<>();

        MapLayer spawnLayer = tiledMap.getLayers().get("HornheadSpawns");
        if (spawnLayer == null) {
            Gdx.app.log("GameScreen", "HornheadSpawns layer not found! No hornheads spawned.");
            return;
        }

        for (MapObject obj : spawnLayer.getObjects()) {
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                Gdx.app.log("GameScreen", "Spawning hornhead at (" + x + ", " + y + ")");
                HuskHornhead hornhead = new HuskHornhead(x, y);
                HuskHornheadController hornheadController = new HuskHornheadController(hornhead, collider);
                hornheadController.setKnight(knight);
                hornheadControllers.add(hornheadController);
            }
        }
    }

    private void initGuardians(TileMapCollider collider, Knight knight) {
        guardianAssets = new CrystalGuardianAssets(Assets.manager);
        guardianRenderer = new CrystalGuardianRenderer(guardianAssets);

        guardianControllers = new ArrayList<>();

        MapLayer spawnLayer = tiledMap.getLayers().get("GuardianSpawns");
        if (spawnLayer == null) {
            Gdx.app.log("GameScreen", "GuardianSpawns layer not found! No guardians spawned.");
            return;
        }

        for (MapObject obj : spawnLayer.getObjects()) {
            Float x = obj.getProperties().get("x", Float.class);
            Float y = obj.getProperties().get("y", Float.class);
            if (x != null && y != null) {
                // Optional per-spawn "facingRight" bool set in Tiled
                // (native art faces left, so the default watch is leftward).
                Boolean facingRight = obj.getProperties().get("facingRight", Boolean.class);
                boolean faceRight = facingRight != null && facingRight;
                Gdx.app.log("GameScreen", "Spawning guardian at (" + x + ", " + y + ")");
                CrystalGuardian guardian = new CrystalGuardian(x, y, faceRight);
                CrystalGuardianController guardianController = new CrystalGuardianController(guardian, collider);
                guardianController.setKnight(knight);
                guardianControllers.add(guardianController);
            }
        }
    }

    private void loadMap() {
        tiledMap = Assets.manager.get(mapPath, TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

        MapProperties props = tiledMap.getProperties();
        int mapW = props.get("width", Integer.class);
        int mapH = props.get("height", Integer.class);
        int tileW = props.get("tilewidth", Integer.class);
        int tileH = props.get("tileheight", Integer.class);
        mapWidthPx  = mapW * tileW;
        mapHeightPx = mapH * tileH;

        if (tiledMap.getLayers().get("Gameplay") != null) {
            bgLayers       = resolveLayerIndices(BG_LAYER_NAMES);
            gameplayLayers = resolveLayerIndices(GAMEPLAY_LAYER_NAMES);
            fgLayers       = resolveLayerIndices(FG_LAYER_NAMES);
        } else {
            // Rooms that do not follow the CityOfTears layer names
            // (e.g. secretRoom.tmx) get a generic split instead.
            classifyLayersGenerically();
        }

        // The breakable-wall tile layer renders separately so a per-layer
        // offset can shake it (spec: Breakable Walls).
        int wallLayerIndex = tiledMap.getLayers().getIndex("BreakableWalls");
        if (wallLayerIndex >= 0) {
            breakableWallLayers = new int[]{ wallLayerIndex };
            breakableWallLayer =
                (TiledMapTileLayer) tiledMap.getLayers().get(wallLayerIndex);
        } else {
            breakableWallLayers = new int[0];
            breakableWallLayer = null;
        }

        // Per-map presentation, authored as custom map properties in Tiled
        // (spec: secret room renders 1.5x bigger over a pure black bg).
        Float viewScale = tiledMap.getProperties().get("viewScale", Float.class);
        worldCamera.zoom = (viewScale != null && viewScale > 0f)
            ? 1f / viewScale : 1f;
        worldCamera.update();
        Boolean black = tiledMap.getProperties().get("blackBackground", Boolean.class);
        blackBackground = black != null && black;
    }

    /**
     * Fallback layer routing for rooms without the CityOfTears layer names:
     * tile layers whose name starts with "Fore" draw in front of the
     * knight, every other tile layer draws behind.
     */
    private void classifyLayersGenerically() {
        IntArray bg = new IntArray();
        IntArray fg = new IntArray();
        for (int i = 0; i < tiledMap.getLayers().getCount(); i++) {
            MapLayer layer = tiledMap.getLayers().get(i);
            if (!(layer instanceof TiledMapTileLayer)) continue;
            String name = layer.getName() == null ? "" : layer.getName();
            if (name.startsWith("BreakableWalls")) continue; // drawn apart
            if (name.startsWith("Fore")) fg.add(i);
            else                         bg.add(i);
        }
        bgLayers       = bg.toArray();
        gameplayLayers = new int[0];
        fgLayers       = fg.toArray();
    }

    private int[] resolveLayerIndices(String[] names) {
        IntArray indices = new IntArray(names.length);
        for (String name : names) {
            int index = tiledMap.getLayers().getIndex(name);
            if (index >= 0) {
                indices.add(index);
            } else {
                Gdx.app.error("GameScreen", "Map layer not found: " + name);
            }
        }
        return indices.toArray();
    }

    /** Locate the "Boss Arena" marker object (Ctrl+B teleport target). */

    /** Spawn the False Knight at the map's "FalseKnightSpawn" marker. */
    private void initFalseKnight(TileMapCollider collider, Knight knight) {
        float[] p = findNamedPoint("FalseKnightSpawn");
        if (p == null) return; // not a boss map
        falseKnightRenderer =
            new FalseKnightRenderer(new FalseKnightAssets(Assets.manager));
        FalseKnight boss = new FalseKnight(p[0], p[1]);
        falseKnightController =
            new FalseKnightController(boss, collider, knight, controller);
        Gdx.app.log("GameScreen",
            "False Knight spawned at (" + p[0] + ", " + p[1] + ")");
    }

    /**
     * Boss arena gate (spec): the Gate TILE layer draws the raised gate art;
     * the Gate OBJECT layer masks it with one rectangle. When the knight
     * steps past the gate the tiles come down (tile-layer offset) and the
     * same rectangle becomes solid terrain, sealing the arena until the
     * boss falls or the knight dies.
     */
    private void initBossGate() {
        if (falseKnightController == null) return;

        TiledMapTileLayer tileLayer = null;
        MapLayer objLayer = null;
        // The tile layer and the object layer share the name "Gate":
        // separate them by type.
        for (MapLayer layer : tiledMap.getLayers()) {
            if (!"Gate".equals(layer.getName())) continue;
            if (layer instanceof TiledMapTileLayer) {
                tileLayer = (TiledMapTileLayer) layer;
            } else {
                objLayer = layer;
            }
        }
        if (tileLayer == null || objLayer == null) return;

        Rectangle mask = null;
        for (MapObject obj : objLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                mask = ((RectangleMapObject) obj).getRectangle();
                break;
            }
        }
        if (mask == null) return;

        // Drop distance: down to the highest ground top under the mask.
        float floorY = 0f;
        List<CollisionRect> rects = collider.getCollisionRects();
        for (int i = 0; i < rects.size(); i++) {
            CollisionRect r = rects.get(i);
            if (r.getRight() <= mask.x || r.getLeft() >= mask.x + mask.width) continue;
            if (r.getTop() <= mask.y && r.getTop() > floorY) floorY = r.getTop();
        }

        bossGateLayer = tileLayer;
        bossGate = new BossGate(
            new CollisionRect(mask.x, mask.y, mask.width, mask.height), floorY);
        // Leash the boss between the left wall and the gate.
        falseKnightController.setArenaBounds(camMinX + 60f, mask.x - 20f);
    }

    /** Gate state machine: seal on entry, reopen on boss death/knight death. */
    private void updateBossGate(float delta) {
        if (bossGate == null || falseKnightController == null) return;
        Knight knight = controller.getKnight();

        switch (bossGate.getState()) {
            case OPEN:
                if (falseKnightController.isAlive()
                    && !knight.isDead() && !knight.isNoclip()
                    && knight.getX() < bossGate.getTriggerX()) {
                    bossGate.setState(BossGate.State.CLOSING);
                    falseKnightController.setActivated(true);
                    // Camera locks onto the arena for the fight (spec).
                    controller.setCameraWorldBounds(camMinX, camMinY,
                        bossGate.getGateRect().getRight(), camMaxY);
                }
                break;
            case CLOSING:
                if (bossGate.advanceClose(delta)) {
                    // Slammed shut: the dropped rectangle is now solid terrain.
                    CollisionRect solid = bossGate.getSolidRect();
                    collider.getCollisionRects().add(solid);
                    collider.setSolidRegion(solid.getX(), solid.getY(),
                        solid.getWidth(), solid.getHeight(), true);
                    controller.shakeCamera(10f, 0.3f);
                    bossGate.setState(BossGate.State.SEALED);
                }
                break;
            case SEALED:
                if (!falseKnightController.isAlive() || knight.isDead()) {
                    CollisionRect solid = bossGate.getSolidRect();
                    collider.setSolidRegion(solid.getX(), solid.getY(),
                        solid.getWidth(), solid.getHeight(), false);
                    collider.getCollisionRects().remove(solid);
                    controller.setCameraWorldBounds(camMinX, camMinY,
                        camMaxX, camMaxY);
                    bossGate.setState(BossGate.State.OPENING);
                }
                break;
            case OPENING:
                if (bossGate.advanceOpen(delta)) {
                    bossGate.setState(BossGate.State.OPEN);
                }
                break;
        }

        // The Gate tile layer follows the drop (positive offset = down).
        bossGateLayer.setOffsetY(bossGate.getOffset());
    }

    private float[] findBossArena() {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if ("Boss Arena".equals(obj.getName())) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) {
                        return new float[]{ ox, oy };
                    }
                }
            }
        }
        Gdx.app.log("GameScreen", "Boss Arena not found, using Starting Point");
        return findStartingPoint();
    }

    private float[] findStartingPoint() {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if ("Starting Point".equals(obj.getName())) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) {
                        Gdx.app.log("GameScreen", "Spawn at (" + ox + ", " + oy + ")");
                        return new float[]{ ox, oy };
                    }
                }
            }
        }
        Gdx.app.log("GameScreen", "Starting Point not found, using (100,100)");
        return new float[]{ 100, 100 };
    }

    /** Locate the "ZoteSpawn" point in the map's Objects layer. */
    /**
     * Watches game state for achievement conditions (spec: Achievements).
     * AchievementsRegistry.unlock() de-duplicates, so calling these every
     * frame is a cheap no-op once earned. No allocations here.
     */
    private void checkAchievements() {
        AchievementsRegistry reg = AchievementsRegistry.getInstance();

        // Falsehood + Completion (+ Speedrun under 10 min): the False
        // Knight is this build's final boss, so beating him finishes
        // the game. Boss logic only has to set bossDefeated = true.
        if (GameSession.isActive() && GameSession.getActive().bossDefeated) {
            reg.onFalseKnightDefeated();
            reg.onGameFinished(GameSession.getActive().playTimeSeconds);
        }

        // True Hunter: every enemy in the level dead at once.
        boolean anyAlive = false;
        for (int i = 0; i < enemyControllers.size(); i++) {
            if (enemyControllers.get(i).isAlive()) { anyAlive = true; break; }
        }
        if (!anyAlive && !enemyControllers.isEmpty()) {
            reg.onAllEnemiesKilled();
        }
    }

    /**
     * Death flow (spec): the death animation plays out untouched; once it
     * ends the screen fades to black over a second, the world resets at
     * the spawn point behind full black, then the scene fades back in.
     */
    private void updateDeathFade(float delta) {
        if (deathFadePhase == 0) {
            if (controller.getKnight().deathAnimationFinished()) {
                deathFadePhase = 1;
                deathFadeTimer = 0f;
            }
            return;
        }
        deathFadeTimer += delta;
        if (deathFadePhase == 1 && deathFadeTimer >= DEATH_FADE_OUT_DURATION) {
            resetAtSpawnPoint(); // the swap happens while fully black
            deathFadePhase = 2;
            deathFadeTimer = 0f;
        } else if (deathFadePhase == 2
                && deathFadeTimer >= DEATH_FADE_IN_DURATION) {
            deathFadePhase = 0;
        }
    }

    /** Fresh run state: knight at the spawn point, enemies respawned. */
    private void resetAtSpawnPoint() {
        // In side rooms (no "Starting Point") death returns the knight to
        // the door we came in through.
        float[] spawn = findSpawnPoint(entrySpawnName);
        controller.getKnight().respawn(spawn[0], spawn[1]);
        for (int i = 0; i < enemyControllers.size(); i++) {
            enemyControllers.get(i).respawn();
        }
        controller.snapCameraToKnight();
    }

    /** Fullscreen black quad over everything, driven by the fade phase. */
    private void drawDeathFade(float uiW, float uiH) {
        if (deathFadePhase == 0) return;
        float alpha = deathFadePhase == 1
            ? Math.min(1f, deathFadeTimer / DEATH_FADE_OUT_DURATION)
            : 1f - Math.min(1f, deathFadeTimer / DEATH_FADE_IN_DURATION);
        batch.setColor(0f, 0f, 0f, alpha);
        batch.draw(whiteTexture, 0f, 0f, uiW, uiH);
        batch.setColor(Color.WHITE);
    }

    // ================================================================
    // Breakable walls, darkness zones, pickups & room transitions (spec)
    // ================================================================

    /** Parse the map's wall/darkness/pickup/transition object layers. */
    private void initMapFeatures(Knight knight) {
        List<BreakableWall> walls = new ArrayList<>();
        MapLayer wallObjects = tiledMap.getLayers().get("BreakableWallColliders");
        if (wallObjects != null) {
            for (MapObject obj : wallObjects.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) continue;
                // TmxMapLoader already flips object rects to y-up world space.
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                Integer hp     = obj.getProperties().get("hp", Integer.class);
                Boolean debris = obj.getProperties().get("debris", Boolean.class);
                BreakableWall wall = new BreakableWall(obj.getName(), r.x, r.y,
                    r.width, r.height,
                    hp != null ? hp : 3,
                    debris == null || debris);
                if (RoomStateRegistry.isWallBroken(mapPath + ":" + wall.getName())) {
                    // Broken earlier this session: stays open, no solids.
                    wall.markBrokenSilently();
                } else {
                    // A standing wall blocks movement via the solid grid.
                    collider.setSolidRegion(r.x, r.y, r.width, r.height, true);
                }
                walls.add(wall);
            }
        }

        darknessZones = new ArrayList<>();
        MapLayer darkObjects = tiledMap.getLayers().get("DarknessZone");
        if (darkObjects == null) darkObjects = tiledMap.getLayers().get("DarknessZones");
        if (darkObjects != null) {
            for (MapObject obj : darkObjects.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) continue;
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                String revealedBy = obj.getProperties().get("revealedBy", String.class);
                Float fade        = obj.getProperties().get("fadeDuration", Float.class);
                DarknessZone zone = new DarknessZone(r.x, r.y, r.width, r.height,
                    revealedBy, fade != null ? fade : 0.8f);
                if (revealedBy != null
                        && RoomStateRegistry.isWallBroken(mapPath + ":" + revealedBy)) {
                    // Its wall fell earlier this session: never re-darken.
                    zone.revealInstantly();
                }
                darknessZones.add(zone);
            }
        }

        roomTransitions = new ArrayList<>();
        MapLayer transitionObjects = tiledMap.getLayers().get("Transitions");
        if (transitionObjects != null) {
            for (MapObject obj : transitionObjects.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) continue;
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                String targetMap   = obj.getProperties().get("targetMap", String.class);
                String targetSpawn = obj.getProperties().get("targetSpawn", String.class);
                if (targetMap == null) {
                    Gdx.app.error("GameScreen",
                        "Transition without targetMap skipped in " + mapPath);
                    continue;
                }
                roomTransitions.add(new RoomTransition(r.x, r.y,
                    r.width, r.height, targetMap, targetSpawn));
            }
        }

        // World charm pickups (spec: Void Heart in the secret room).
        charmPickups = new ArrayList<>();
        MapLayer charmObjects = tiledMap.getLayers().get("Charms");
        if (charmObjects != null) {
            for (MapObject obj : charmObjects.getObjects()) {
                Float ox = obj.getProperties().get("x", Float.class);
                Float oy = obj.getProperties().get("y", Float.class);
                if (ox == null || oy == null) continue;
                Charm charm = CharmPickup.resolveCharm(obj.getName());
                if (charm == null) {
                    Gdx.app.error("GameScreen",
                        "Unknown charm pickup '" + obj.getName() + "' in " + mapPath);
                    continue;
                }
                if (RoomStateRegistry.isPickupCollected(mapPath + ":" + obj.getName())) {
                    continue; // already taken this session
                }
                charmPickups.add(new CharmPickup(obj.getName(), charm, ox, oy));
            }
        }

        wallsController = new BreakableWallsController(mapPath, walls,
            darknessZones, collider, knight, breakableWallLayer,
            controller, particleHook);
        charmPickupController =
            new CharmPickupController(charmPickups, knight, mapPath);

        // Rooms whose collision extends past the tile grid still frame
        // correctly: widen the camera clamp to the union of the grid and
        // every collision rectangle.
        float minX = 0f, minY = 0f, maxX = mapWidthPx, maxY = mapHeightPx;
        List<CollisionRect> rects = collider.getCollisionRects();
        for (int i = 0; i < rects.size(); i++) {
            CollisionRect rect = rects.get(i);
            if (rect.getLeft()   < minX) minX = rect.getLeft();
            if (rect.getBottom() < minY) minY = rect.getBottom();
            if (rect.getRight()  > maxX) maxX = rect.getRight();
            if (rect.getTop()    > maxY) maxY = rect.getTop();
        }
        controller.setCameraWorldBounds(minX, minY, maxX, maxY);
        // Remembered so the boss-arena camera lock can be undone.
        camMinX = minX; camMinY = minY; camMaxX = maxX; camMaxY = maxY;
    }

    /** Plug in a real dust emitter later without touching game code. */
    public void setParticleHook(ParticleHook hook) {
        particleHook = hook != null ? hook : ParticleHook.NO_OP;
        if (wallsController != null) wallsController.setParticleHook(particleHook);
    }

    /**
     * Resolve the arrival point: the named spawn object first, then any
     * point in a "Spawn" layer, then the map's "Starting Point".
     */
    private float[] findSpawnPoint(String spawnName) {
        if (spawnName != null) {
            float[] named = findNamedPoint(spawnName);
            if (named != null) return named;
            Gdx.app.error("GameScreen", "Spawn '" + spawnName
                + "' not found in " + mapPath + "; trying the Spawn layer");
            MapLayer spawnLayer = tiledMap.getLayers().get("Spawn");
            if (spawnLayer != null) {
                for (MapObject obj : spawnLayer.getObjects()) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) return new float[]{ ox, oy };
                }
            }
        }
        return findStartingPoint();
    }

    /** Find a point object by name anywhere in the map (null if absent). */
    private float[] findNamedPoint(String name) {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if (name.equals(obj.getName())) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) return new float[]{ ox, oy };
                }
            }
        }
        return null;
    }

    /** Begin the original-game style exit fade once per trigger overlap. */
    private void checkRoomTransitions() {
        if (roomFadePhase != 0 || deathFadePhase != 0) return;
        Knight knight = controller.getKnight();
        if (knight.isDead()) return;
        CollisionRect hurtBox = knight.getHurtBox();
        for (int i = 0; i < roomTransitions.size(); i++) {
            RoomTransition t = roomTransitions.get(i);
            if (AABB.overlaps(hurtBox, t)) {
                pendingTransition = t;
                roomFadePhase = 1;
                roomFadeTimer = 0f;
                return;
            }
        }
    }

    private void updateRoomFade(float delta) {
        roomFadeTimer += delta;
        if (roomFadePhase == 1 && roomFadeTimer >= ROOM_FADE_OUT_DURATION) {
            switchRoom(); // behind full black
        } else if (roomFadePhase == 3
                && roomFadeTimer >= ROOM_DARKNESS_DURATION) {
            roomFadePhase = 2;   // darkness over: reveal the new room
            roomFadeTimer = 0f;
        } else if (roomFadePhase == 2
                && roomFadeTimer >= ROOM_FADE_IN_DURATION) {
            roomFadePhase = 0;
        }
    }

    /** Swap to the destination room while the screen is fully black. */
    private void switchRoom() {
        RoomTransition t = pendingTransition;
        pendingTransition = null;
        roomFadePhase = 0;   // this screen is done; the next one fades in

        Knight knight = controller.getKnight();
        if (GameSession.isActive()) {
            GameSession.getActive().currentRoom = t.getTargetMap();
        }
        game.setScreen(new GameScreen(game, t.getTargetMap(),
            t.getTargetSpawn(), knight.getHpMasks(), knight.getSoulAmount()));
        // Dispose after the frame unwinds (same pattern as saveAndQuit).
        final GameScreen self = this;
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() { self.dispose(); }
        });
    }

    /** Fullscreen black quad for the room transition fade. */
    private void drawRoomFade(float uiW, float uiH) {
        if (roomFadePhase == 0) return;
        float alpha;
        if (roomFadePhase == 1) {
            alpha = Math.min(1f, roomFadeTimer / ROOM_FADE_OUT_DURATION);
        } else if (roomFadePhase == 3) {
            alpha = 1f;   // darkness hold: fully black
        } else {
            alpha = 1f - Math.min(1f, roomFadeTimer / ROOM_FADE_IN_DURATION);
        }
        batch.setColor(0f, 0f, 0f, alpha);
        batch.draw(whiteTexture, 0f, 0f, uiW, uiH);
        batch.setColor(Color.WHITE);
    }

    /** World-space black overlays hiding unrevealed areas (spec). */
    private void drawDarknessZones() {
        for (int i = 0; i < darknessZones.size(); i++) {
            DarknessZone zone = darknessZones.get(i);
            if (zone.getAlpha() <= 0f) continue;
            batch.setColor(0f, 0f, 0f, zone.getAlpha());
            batch.draw(whiteTexture, zone.getX(), zone.getY(),
                zone.getWidth(), zone.getHeight());
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Land every actor on the terrain below it (spec: during the 1s
     * darkness the player and other actors snap to the ground, so nobody
     * is caught mid-fall when the new room fades in). Enemies stand on
     * authored spawn points already and keep their own placement.
     */
    private void snapActorsToGround() {
        Knight knight = controller.getKnight();
        float ground = findGroundY(knight.getX(), knight.getHalfWidth(),
            knight.getTop());
        if (ground != Float.NEGATIVE_INFINITY) {
            knight.setPosition(knight.getX(), ground);
            knight.setVelocityY(0f);
            knight.setGrounded(true);
        }
        // Zote, unless he is parked off-world in this room.
        if (zote != null && zote.getX() > -90000f) {
            float zoteGround = findGroundY(zote.getX(), Zote.WIDTH * 0.12f,
                zote.getY() + Zote.HEIGHT);
            if (zoteGround != Float.NEGATIVE_INFINITY) {
                zote.setPosition(zote.getX(), zoteGround);
            }
        }
    }

    /**
     * Highest ground top at or below {@code fromY} under a body of
     * {@code halfWidth} centred on {@code centerX}, checking both the
     * solid tile grid and the Collision rectangles.
     * @return the ground's top y, or {@code Float.NEGATIVE_INFINITY}.
     */
    private float findGroundY(float centerX, float halfWidth, float fromY) {
        float best = Float.NEGATIVE_INFINITY;

        List<CollisionRect> rects = collider.getCollisionRects();
        for (int i = 0; i < rects.size(); i++) {
            CollisionRect r = rects.get(i);
            if (centerX + halfWidth <= r.getLeft()
                || centerX - halfWidth >= r.getRight()) continue;
            if (r.getTop() <= fromY + 1f && r.getTop() > best) {
                best = r.getTop();
            }
        }

        int tileHeight = collider.getTileHeight();
        int colStart = collider.worldXToTile(centerX - halfWidth + 0.5f);
        int colEnd   = collider.worldXToTile(centerX + halfWidth - 0.5f);
        int rowStart = Math.min(collider.getRows() - 1,
            collider.worldYToTile(fromY + 1f));
        for (int col = colStart; col <= colEnd; col++) {
            if (col < 0 || col >= collider.getCols()) continue;
            for (int row = rowStart; row >= 0; row--) {
                if (!collider.isSolid(col, row)) continue;
                float top = (row + 1) * tileHeight;
                if (top <= fromY + 1f && top > best) best = top;
                break; // highest solid tile in this column found
            }
        }
        return best;
    }

    private float[] findZoteSpawn() {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if ("ZoteSpawn".equals(obj.getName())) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) {
                        return new float[]{ ox, oy };
                    }
                }
            }
        }
        // Maps without a marker (e.g. secretRoom): park Zote far off-world
        // so all his systems stay inert without null checks anywhere.
        Gdx.app.log("GameScreen", "ZoteSpawn not found; parking Zote off-world");
        return new float[]{ -100000f, -100000f };
    }

    private void initShader() {
        ShaderProgram.pedantic = false;
        backgroundShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/cityoftears/background.frag")
        );
        if (!backgroundShader.isCompiled()) {
            Gdx.app.error("GameScreen", "Background shader compilation failed:\n" + backgroundShader.getLog());
        }

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    private void initRainEffect() {
        rainEffect = new RainEffect();
    }

    public void setPaused(boolean paused) { controller.setPaused(paused); }
    public boolean isPaused()             { return controller.isPaused(); }
    public Knight getKnight()             { return controller.getKnight(); }
    public GameController getController() { return controller; }

    @Override
    public void updateLogic(float delta) {
        // ---- Room transition fade (spec: Room Transitions) ----
        if (roomFadePhase != 0) {
            updateRoomFade(delta);
            // The world freezes while fading out and through the darkness
            // hold; the 0.4s arrival fade-in plays over live gameplay.
            if (roomFadePhase == 1 || roomFadePhase == 3) return;
        }

        // ---- Inventory menu (spec: 'i' pauses the game at any moment
        //      outside animation-lock frames and opens the charm menu) ----
        if (inventoryOverlay.isShown()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                    || Gdx.input.isKeyJustPressed(
                        GameSettings.getInstance().getInventoryKey())) {
                inventoryOverlay.beginClose();
            }
            uiStage.act(delta);
            if (inventoryOverlay.isHidden()) {
                controller.setPaused(false); // reverse sweep finished
            }
            return;
        }
        if (!controller.isPaused()
                && Gdx.input.isKeyJustPressed(
                    GameSettings.getInstance().getInventoryKey())
                && canOpenInventory()) {
            controller.setPaused(true);
            inventoryOverlay.open();
            uiStage.act(delta);
            return;
        }

        // ---- Zote dialogue (spec: world stays frozen while talking) ----
        if (zoteController.isDialogueActive()) {
            zoteController.update(delta);
            return;
        }

        // ---- Pause menu (spec: opens with ESC during gameplay) ----
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            boolean nowPaused = !controller.isPaused();
            controller.setPaused(nowPaused);
            pauseOverlay.setVisible(nowPaused);
        }
        if (controller.isPaused()) {
            // World stays frozen; only the overlay widgets animate.
            uiStage.act(delta);
            return;
        }

        // Cheat combos (Left Ctrl + key) — active only while unpaused.
        cheatController.update();

        GameSession.addPlayTime(delta);

        controller.update(delta);
        zoteController.update(delta);
        checkAchievements();
        updateDeathFade(delta);
        wallsController.update(delta);
        charmPickupController.update(delta);
        checkRoomTransitions();
        if (!blackBackground) { // indoor rooms have no rain
            rainEffect.update(delta);
        }
        glassRainEffect.update(delta);

        // Index-based loop: no Iterator allocation per frame.
        for (int i = 0; i < enemyControllers.size(); i++) {
            enemyControllers.get(i).update(delta);
        }

        updateBossGate(delta);

        combat.resolve(delta);
        combat.resolveSpiritHits(controller.getSpirits());
        combat.resolveWraithHits(controller.getWraiths());
    }

    /** Spec: the inventory cannot open during animation-lock frames. */
    private boolean canOpenInventory() {
        Knight knight = controller.getKnight();
        return !knight.isDead() && !knight.isCasting() && !knight.isScreaming();
    }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        timeElapsed += Gdx.graphics.getDeltaTime();

        worldCamera.update();

        // Background
        screenMatrix.setToOrtho2D(
            0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(screenMatrix);
        if (blackBackground) {
            // Spec: the secret room draws over complete black.
            batch.begin();
            batch.setColor(Color.BLACK);
            batch.draw(whiteTexture, 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(Color.WHITE);
            batch.end();
        } else {
            batch.setShader(backgroundShader);
            batch.begin();
            backgroundShader.setUniformf("u_time", timeElapsed);
            backgroundShader.setUniformf("u_resolution",
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.draw(whiteTexture, 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
            batch.setShader(null);
        }

        // Glass Rain
        glassRainEffect.render(worldCamera);

        // Tiled background and gameplay
        mapRenderer.setView(worldCamera);
        mapRenderer.render(bgLayers);
        mapRenderer.render(gameplayLayers);
        if (breakableWallLayers.length > 0) {
            // Rendered apart so hits can shake it via the layer offset.
            mapRenderer.render(breakableWallLayers);
        }

        // Draw Knight, Sentries, and Javelins
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        Knight knight = controller.getKnight();
        TextureRegion frame = knightAnimator.getCurrentFrame(knight);
        float frameW = frame.getRegionWidth();
        float frameH = frame.getRegionHeight();

        // Zote draws behind the Knight so slash effects stay visible.
        zoteRenderer.draw(batch, zote);
        zoteDialogue.drawPrompt(batch);
        // World charm pickups + their COLLECT prompt (Zote treatment).
        charmPickupOverlay.draw(batch);

        // Enemies draw BEFORE the knight (painter's algorithm): the hero
        // is the gameplay focus and must always read in front of the
        // False Knight's huge sprite and every other enemy.
        for (WingedSentryController sc : sentryControllers) {
            sentryRenderer.draw(batch, sc.getSentry());

            Javelin javelin = sc.getJavelin();
            if (javelin != null) {
                javelinRenderer.draw(batch, javelin);
            }
        }

        for (TiktikController tiktikController : tiktikControllers) {
            tiktikRenderer.draw(batch, tiktikController.getTiktik());
        }

        for (HuskHornheadController hornheadController : hornheadControllers) {
            hornheadRenderer.draw(batch, hornheadController.getHornhead());
        }

        for (CrystalGuardianController guardianController : guardianControllers) {
            guardianRenderer.draw(batch, guardianController.getGuardian());
        }

        if (falseKnightController != null) {
            falseKnightRenderer.draw(batch,
                falseKnightController.getFalseKnight(),
                falseKnightController.getShockwave());
        }


        // Flash while invincible: blink the sprite alpha ~10x/sec.
        boolean flashOff = !knight.isDead() && knight.isInvincible()
            && ((int) (knight.getInvincibleTimer() * INVINCIBLE_FLASH_HZ) & 1) == 0;
        // Sharp Shadow: the dash reads as a living shade - tint it void-dark.
        boolean shadowDash = knight.isDashing() && knight.hasCharm(Charm.SHARP_SHADOW);
        if (flashOff) batch.setColor(1f, 1f, 1f, 0.3f);
        else if (shadowDash) batch.setColor(0.30f, 0.22f, 0.42f, 1f);

        if (knight.isFacingRight()) {
            batch.draw(frame,
                knight.getX() + frameW / 2f,
                knight.getY(),
                -frameW, frameH);
        } else {
            batch.draw(frame,
                knight.getX() - frameW / 2f,
                knight.getY(),
                +frameW, frameH);
        }

        if (flashOff || shadowDash) batch.setColor(Color.WHITE);

        // Effect overlays (slash arcs, dash dust).
        TextureRegion effect = knightAnimator.getCurrentEffectFrame(knight);
        if (effect != null) {
            float eW = effect.getRegionWidth();
            float eH = effect.getRegionHeight();

            float offsetX = 0f;          // along the facing direction
            float offsetY = 0f;
            boolean invertFlip = false;  // dash trail art faces the other way

            switch (knight.getState()) {
                case UP_SLASH:
                    offsetX = UP_SLASH_EFFECT_OFFSET_X;
                    offsetY = UP_SLASH_EFFECT_OFFSET_Y;
                    break;
                case DOWN_SLASH:
                    offsetY = DOWN_SLASH_EFFECT_OFFSET_Y;
                    break;
                case DASH:
                    invertFlip = true;
                    offsetX = DASH_EFFECT_OFFSET_X;
                    offsetY = DASH_EFFECT_OFFSET_Y;
                    break;
                default:
                    break;
            }

            float dir = knight.isFacingRight() ? 1f : -1f;
            float centerX = knight.getX() + dir * offsetX;
            float drawY = knight.getY() + offsetY;
            boolean flip = knight.isFacingRight() ^ invertFlip;
            if (flip) {
                batch.draw(effect, centerX + eW / 2f, drawY, -eW, eH);
            } else {
                batch.draw(effect, centerX - eW / 2f, drawY, +eW, eH);
            }
        }

        // Vengeful Spirit fireballs (index-based: no Iterator allocation).
        // Void Heart: the knight's spells surge with void - tint them dark.
        boolean voidSpells = knight.hasCharm(Charm.VOID_HEART);
        if (voidSpells) batch.setColor(0.42f, 0.28f, 0.58f, 1f);
        List<VengefulSpiritController> spirits = controller.getSpirits();
        for (int i = 0; i < spirits.size(); i++) {
            spiritRenderer.draw(batch, spirits.get(i).getSpirit());
        }

        // Howling Wraiths blasts (stationary; plume above, base under feet).
        List<HowlingWraithController> wraiths = controller.getWraiths();
        for (int i = 0; i < wraiths.size(); i++) {
            wraithRenderer.draw(batch, wraiths.get(i).getWraith());
        }
        if (voidSpells) batch.setColor(Color.WHITE);

        batch.end();

        // Foreground layers
        mapRenderer.setView(worldCamera);
        mapRenderer.render(fgLayers);

        // Rain Effect + darkness zones (world space)
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        if (!blackBackground) { // indoor rooms have no rain
            rainEffect.render(batch, worldCamera);
        }
        // Darkness zones cover everything in the world - terrain, rain
        // and foreground - until their wall crumbles (spec: hidden path).
        drawDarknessZones();
        batch.end();

        // HUD (health masks + soul orb) in screen space
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        hudRenderer.draw(batch, controller.getKnight(), Gdx.graphics.getDeltaTime());
        zoteDialogue.drawDialogue(batch,
                uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        achievementToasts.draw(batch,
                uiViewport.getWorldWidth(), uiViewport.getWorldHeight(),
                Gdx.graphics.getDeltaTime());
        drawDeathFade(uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        drawRoomFade(uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
        batch.end();

        renderDebugBoxes();

        // ---- Pause menu overlay ----
        if (settingsRequested) {
            settingsRequested = false;
            // Capture BEFORE the dim overlay is drawn: the Settings screen
            // applies its own darkening to the raw frame.
            pauseController.openSettings(this, captureFrame());
            return;
        }
        if (controller.isPaused()) {
            uiViewport.apply(true);
            uiStage.draw();
        }
    }

    /**
     * Grab the current framebuffer as a texture. Used as the backdrop of the
     * Settings screen when it is opened from the pause menu. Ownership of the
     * returned texture passes to that screen.
     */
    private Texture captureFrame() {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0,
            Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    /** F3 toggles an overlay of every collision/hurt/damage box, in world space. */
    private void renderDebugBoxes() {
        boolean f3 = Gdx.input.isKeyPressed(Input.Keys.F3);
        if (f3 && !f3WasDown) debugBoxes = !debugBoxes;
        f3WasDown = f3;
        if (!debugBoxes) return;

        Knight knight = controller.getKnight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        debugShapes.setProjectionMatrix(worldCamera.combined);
        debugShapes.begin(ShapeRenderer.ShapeType.Line);

        // Physics boxes (green) vs combat boxes (red/yellow)
        debugShapes.setColor(Color.LIME);
        drawBox(knight);                                   // knight physics box
        for (WingedSentryController sc : sentryControllers) {
            drawBox(sc.getSentry());                       // sentry body box
        }
        for (TiktikController tiktikController : tiktikControllers) {
            drawBox(tiktikController.getTiktik()); // tiktik body box
        }
        for (HuskHornheadController hornheadController : hornheadControllers) {
            drawBox(hornheadController.getHornhead()); // hornhead body box
        }
        for (CrystalGuardianController guardianController : guardianControllers) {
            drawBox(guardianController.getGuardian()); // guardian body box
        }
        if (falseKnightController != null) {
            FalseKnight boss = falseKnightController.getFalseKnight();
            if (boss.isAlive() && !boss.isStunnedState()) {
                drawBox(boss);                         // boss armor box
            }
            debugShapes.setColor(Color.ORANGE);
            CollisionRect bossStrike = boss.getStrikeBox();
            if (bossStrike != null) drawRect(bossStrike);  // live mace strike
            if (boss.isStunnedState()) {
                drawRect(boss.getHeadBox());           // vulnerable maggot
            }
            if (falseKnightController.getShockwave().isActive()) {
                drawRect(falseKnightController.getShockwave().getDamageBox());
            }
            debugShapes.setColor(Color.LIME);
        }

        debugShapes.setColor(Color.RED);
        drawRect(knight.getHurtBox());                     // knight hurtbox

        debugShapes.setColor(Color.MAGENTA);
        List<CollisionRect> spikes = collider.getDamagingRects();
        for (int i = 0; i < spikes.size(); i++) {
            drawRect(spikes.get(i));                       // spike hazard boxes
        }

        CollisionRect slash = knight.getActiveSlashBox();
        if (slash != null) {
            debugShapes.setColor(Color.ORANGE);
            drawRect(slash);                               // active nail slash
        }

        for (WingedSentryController sc : sentryControllers) {
            Javelin javelin = sc.getJavelin();
            if (javelin != null) {
                debugShapes.setColor(Color.CYAN);
                drawBox(javelin);                              // javelin physics box
                debugShapes.setColor(Color.YELLOW);
                drawRect(javelin.getDamageBox());              // javelin damage box
            }
        }

        // Hornhead vision rectangles (cyan) — only meaningful while alive
        debugShapes.setColor(Color.CYAN);
        for (HuskHornheadController hornheadController : hornheadControllers) {
            HuskHornhead hornhead = hornheadController.getHornhead();
            if (hornhead.isAlive()) {
                drawRect(hornhead.getVisionRect());
            }
        }

        // Guardian laser beams (yellow line along the beam core)
        debugShapes.setColor(Color.YELLOW);
        for (CrystalGuardianController guardianController : guardianControllers) {
            Laser laser = guardianController.getLaser();
            if (laser.isActive()) {
                debugShapes.line(laser.getOriginX(), laser.getOriginY(),
                    laser.getEndX(), laser.getEndY());
            }
        }

        // Vengeful Spirit damage boxes (orange) while flying
        debugShapes.setColor(Color.ORANGE);
        List<VengefulSpiritController> spiritControllers = controller.getSpirits();
        for (int i = 0; i < spiritControllers.size(); i++) {
            VengefulSpirit spirit = spiritControllers.get(i).getSpirit();
            if (spirit.isFlying()) {
                drawRect(spirit.getDamageBox());
            }
        }

        // Howling Wraiths damage boxes (orange) while the ticks are live
        List<HowlingWraithController> wraithControllers = controller.getWraiths();
        for (int i = 0; i < wraithControllers.size(); i++) {
            HowlingWraith wraith = wraithControllers.get(i).getWraith();
            if (!wraith.isDone()) {
                drawRect(wraith.getDamageBox());
            }
        }

        debugShapes.end();
    }

    private void drawBox(AABB b) {
        debugShapes.rect(b.getLeft(), b.getBottom(),
            b.getRight() - b.getLeft(), b.getTop() - b.getBottom());
    }

    private void drawRect(CollisionRect r) {
        debugShapes.rect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        backgroundShader.dispose();
        whiteTexture.dispose();
        rainEffect.dispose();
        debugShapes.dispose();
        sentryRenderer.dispose();
        tiktikRenderer.dispose();
        hornheadRenderer.dispose();
        if (falseKnightRenderer != null) falseKnightRenderer.dispose();
        guardianRenderer.dispose();
        hudRenderer.dispose();
        pauseOverlay.dispose();
        inventoryOverlay.dispose();
        zoteDialogue.dispose();
        // A room transition installs the next screen's toast listener
        // before this dispose runs - only clear it if it is still ours.
        if (AchievementsRegistry.getUnlockListener() == achievementToasts) {
            AchievementsRegistry.setUnlockListener(null);
        }
        achievementToasts.dispose();
        charmPickupOverlay.dispose();
    }
}
