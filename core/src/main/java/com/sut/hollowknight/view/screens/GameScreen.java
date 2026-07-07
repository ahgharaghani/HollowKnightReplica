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
import com.sut.hollowknight.controller.CombatSystem;
import com.sut.hollowknight.controller.GameController;
import com.sut.hollowknight.controller.enemy.CrystalGuardianController;
import com.sut.hollowknight.controller.enemy.EnemyController;
import com.sut.hollowknight.controller.enemy.HuskHornheadController;
import com.sut.hollowknight.controller.enemy.TiktikController;
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.controller.spell.VengefulSpiritController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.CrystalGuardian;
import com.sut.hollowknight.model.enemy.HuskHornhead;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.Laser;
import com.sut.hollowknight.model.enemy.Tiktik;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.view.animator.KnightAnimator;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.assets.HudAssets;
import com.sut.hollowknight.view.assets.CrystalGuardianAssets;
import com.sut.hollowknight.view.assets.HuskHornheadAssets;
import com.sut.hollowknight.view.assets.TiktikAssets;
import com.sut.hollowknight.view.assets.WingedSentryAssets;
import com.sut.hollowknight.view.assets.VengefulSpiritAssets;
import com.sut.hollowknight.view.effects.GlassRainEffect;
import com.sut.hollowknight.view.hud.HudRenderer;
import com.sut.hollowknight.view.effects.RainEffect;
import com.sut.hollowknight.model.spell.VengefulSpirit;
import com.sut.hollowknight.view.renderer.enemy.CrystalGuardianRenderer;
import com.sut.hollowknight.view.renderer.spell.VengefulSpiritRenderer;
import com.sut.hollowknight.view.renderer.enemy.HuskHornheadRenderer;
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

    private CrystalGuardianAssets guardianAssets;
    private List<CrystalGuardianController> guardianControllers;
    private CrystalGuardianRenderer guardianRenderer;

    private VengefulSpiritRenderer spiritRenderer;

    private List<EnemyController> enemyControllers;

    private HudRenderer hudRenderer;

    private CombatSystem combat;

    private TileMapCollider collider;

    // Debug hitbox overlay — toggle with F3. Tune box constants to the art.
    private ShapeRenderer debugShapes;
    private boolean debugBoxes = false;
    private boolean f3WasDown = false;

    public GameScreen(Game game) {
        super(game);

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

        float[] spawn = findStartingPoint();
        Knight knight = new Knight(spawn[0], spawn[1]);

        controller = new GameController(knight, collider,
            worldCamera, mapWidthPx, mapHeightPx);

        knightAnimator = new KnightAnimator();

        initShader();
        initRainEffect();
        glassRainEffect = new GlassRainEffect(tiledMap, mapHeightPx);

        initWingedSentries(collider, knight);
        initTiktiks(collider, knight);
        initHornheads(collider, knight);
        initGuardians(collider, knight);

        enemyControllers = new ArrayList<>();
        enemyControllers.addAll(sentryControllers);
        enemyControllers.addAll(tiktikControllers);
        enemyControllers.addAll(hornheadControllers);
        enemyControllers.addAll(guardianControllers);
        combat = new CombatSystem(knight, enemyControllers);

        spiritRenderer = new VengefulSpiritRenderer(new VengefulSpiritAssets(Assets.manager));

        hudRenderer = new HudRenderer(new HudAssets(Assets.manager));
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
        tiledMap = Assets.manager.get("CityOfTears.tmx", TiledMap.class);
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);

        MapProperties props = tiledMap.getProperties();
        int mapW = props.get("width", Integer.class);
        int mapH = props.get("height", Integer.class);
        int tileW = props.get("tilewidth", Integer.class);
        int tileH = props.get("tileheight", Integer.class);
        mapWidthPx  = mapW * tileW;
        mapHeightPx = mapH * tileH;

        bgLayers       = resolveLayerIndices(BG_LAYER_NAMES);
        gameplayLayers = resolveLayerIndices(GAMEPLAY_LAYER_NAMES);
        fgLayers       = resolveLayerIndices(FG_LAYER_NAMES);
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
        controller.update(delta);
        rainEffect.update(delta);
        glassRainEffect.update(delta);

        // Index-based loop: no Iterator allocation per frame.
        for (int i = 0; i < enemyControllers.size(); i++) {
            enemyControllers.get(i).update(delta);
        }

        combat.resolve(delta);
        combat.resolveSpiritHits(controller.getSpirits());
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
        batch.setShader(backgroundShader);
        batch.begin();
        backgroundShader.setUniformf("u_time", timeElapsed);
        backgroundShader.setUniformf("u_resolution",
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.draw(whiteTexture, 0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        batch.setShader(null);

        // Glass Rain
        glassRainEffect.render(worldCamera);

        // Tiled background and gameplay
        mapRenderer.setView(worldCamera);
        mapRenderer.render(bgLayers);
        mapRenderer.render(gameplayLayers);

        // Draw Knight, Sentries, and Javelins
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        Knight knight = controller.getKnight();
        TextureRegion frame = knightAnimator.getCurrentFrame(knight);
        float frameW = frame.getRegionWidth();
        float frameH = frame.getRegionHeight();

        // Flash while invincible: blink the sprite alpha ~10x/sec.
        boolean flashOff = knight.isInvincible()
            && ((int) (knight.getInvincibleTimer() * INVINCIBLE_FLASH_HZ) & 1) == 0;
        if (flashOff) batch.setColor(1f, 1f, 1f, 0.3f);

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

        if (flashOff) batch.setColor(Color.WHITE);

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

        // Vengeful Spirit fireballs (index-based: no Iterator allocation).
        List<VengefulSpiritController> spirits = controller.getSpirits();
        for (int i = 0; i < spirits.size(); i++) {
            spiritRenderer.draw(batch, spirits.get(i).getSpirit());
        }

        batch.end();

        // Foreground layers
        mapRenderer.setView(worldCamera);
        mapRenderer.render(fgLayers);

        // Rain Effect
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        rainEffect.render(batch, worldCamera);
        batch.end();

        // HUD (health masks + soul orb) in screen space
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        hudRenderer.draw(batch, controller.getKnight(), Gdx.graphics.getDeltaTime());
        batch.end();

        renderDebugBoxes();
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
        guardianRenderer.dispose();
        hudRenderer.dispose();
    }
}
