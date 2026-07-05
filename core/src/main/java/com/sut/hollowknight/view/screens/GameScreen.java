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
import com.sut.hollowknight.controller.enemy.WingedSentryController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.model.enemy.Javelin;
import com.sut.hollowknight.model.enemy.WingedSentry;
import com.sut.hollowknight.view.animator.KnightAnimator;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.assets.WingedSentryAssets;
import com.sut.hollowknight.view.effects.GlassRainEffect;
import com.sut.hollowknight.view.effects.RainEffect;
import com.sut.hollowknight.view.renderer.enemy.JavelinRenderer;
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

    private static final String[] BG_LAYER_NAMES = {
        "FarFarFarBackgound", "FarFarBackground", "FarBackground",
        "CracksOnTheWall", "MidBackground"
    };
    private static final String[] GAMEPLAY_LAYER_NAMES = { "Gameplay" };
    private static final String[] FG_LAYER_NAMES = { "Foreground", "ForeForeground" };

    private int[] bgLayers;
    private int[] gameplayLayers;
    private int[] fgLayers;

    private final Matrix4 screenMatrix = new Matrix4();

    private ShaderProgram backgroundShader;
    private Texture whiteTexture;
    private float timeElapsed = 0f;

    private WingedSentryAssets wingedSentryAssets;
    private List<WingedSentryController> sentryControllers;
    private WingedSentryRenderer sentryRenderer;
    private JavelinRenderer javelinRenderer;

    private CombatSystem combat;

    // Debug hitbox overlay — toggle with F3. Tune box constants to the art.
    private ShapeRenderer debugShapes;
    private boolean debugBoxes = false;
    private boolean f3WasDown = false;

    public GameScreen(Game game) {
        super(game);

        batch = new SpriteBatch();
        debugShapes = new ShapeRenderer();
        loadMap();

        TileMapCollider collider = new TileMapCollider(
            tiledMap, "Gameplay", "Collision");

        float[] spawn = findStartingPoint();
        Knight knight = new Knight(spawn[0], spawn[1]);

        controller = new GameController(knight, collider,
            worldCamera, mapWidthPx, mapHeightPx);

        knightAnimator = new KnightAnimator();

        initShader();
        initRainEffect();
        glassRainEffect = new GlassRainEffect(tiledMap, mapHeightPx);

        initWingedSentries(collider, knight);
        combat = new CombatSystem(knight, sentryControllers);
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
        for (int i = 0; i < sentryControllers.size(); i++) {
            sentryControllers.get(i).update(delta);
        }

        combat.resolve(delta);
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

        // Effect overlays (slash arc, dash dust, etc.)
        TextureRegion effect = knightAnimator.getCurrentEffectFrame(knight);
        if (effect != null) {
            float eW = effect.getRegionWidth();
            float eH = effect.getRegionHeight();
            // Center the effect on the knight's sprite, matching the body flip.
            if (knight.isFacingRight()) {
                batch.draw(effect,
                    knight.getX() + eW / 2f,
                    knight.getY(),
                    -eW, eH);
            } else {
                batch.draw(effect,
                    knight.getX() - eW / 2f,
                    knight.getY(),
                    +eW, eH);
            }
        }

        for (int i = 0; i < sentryControllers.size(); i++) {
            WingedSentryController sc = sentryControllers.get(i);
            sentryRenderer.draw(batch, sc.getSentry());

            Javelin javelin = sc.getJavelin();
            if (javelin != null) {
                javelinRenderer.draw(batch, javelin);
            }
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

        debugShapes.setColor(Color.RED);
        drawRect(knight.getHurtBox());                     // knight hurtbox

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
    }
}
