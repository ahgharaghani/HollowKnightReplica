package com.sut.hollowknight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.sut.hollowknight.controller.GameController;
import com.sut.hollowknight.model.Knight;
import com.sut.hollowknight.model.collision.TileMapCollider;
import com.sut.hollowknight.view.animator.KnightAnimator;
import com.sut.hollowknight.view.assets.Assets;
import com.sut.hollowknight.view.effects.GlassRainEffect;
import com.sut.hollowknight.view.effects.RainEffect;

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

    private static final int[] BG_LAYERS = { 0, 1, 2, 3, 4 };
    private static final int[] GAMEPLAY_LAYER = { 5 };
    private static final int[] FG_LAYERS = { 7, 8 };

    private ShaderProgram backgroundShader;
    private Texture whiteTexture;
    private float timeElapsed = 0f;

    public GameScreen(Game game) {
        super(game);

        batch = new SpriteBatch();
        loadMap();

        TileMapCollider collider = new TileMapCollider(
            tiledMap, "Gameplay", "Collision");

        float[] spawn = findStartingPoint();
        Knight knight = new Knight(spawn[0], spawn[1]);

        controller = new GameController(game, knight, collider,
            worldCamera, mapWidthPx, mapHeightPx);

        knightAnimator = new KnightAnimator();

        initShader();
        initRainEffect();
        glassRainEffect = new GlassRainEffect(tiledMap, mapHeightPx);
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
    }

    private float[] findStartingPoint() {
        for (MapLayer layer : tiledMap.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if ("Starting Point".equals(obj.getName())) {
                    Float ox = obj.getProperties().get("x", Float.class);
                    Float oy = obj.getProperties().get("y", Float.class);
                    if (ox != null && oy != null) {
                        float spawnX = ox;
                        float spawnY = oy;
                        Gdx.app.log("GameScreen",
                            "Spawn at (" + ox + ", " + spawnY + ")");
                        return new float[]{ spawnX, spawnY };
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

        // Create a 1x1 white texture to act as our fullscreen quad canvas
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
    }

    @Override
    public void renderGraphics() {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        timeElapsed += Gdx.graphics.getDeltaTime();

        worldCamera.update();

        // Background
        Matrix4 screenMat = new Matrix4().setToOrtho2D(
            0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(screenMat);
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
        mapRenderer.render(BG_LAYERS);
        mapRenderer.render(GAMEPLAY_LAYER);

        // Draw Knight
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        Knight knight = controller.getKnight();
        TextureRegion frame = knightAnimator.getCurrentFrame(knight);
        float frameW = frame.getRegionWidth();
        float frameH = frame.getRegionHeight();

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
        batch.end();

        // Foreground layers
        mapRenderer.setView(worldCamera);
        mapRenderer.render(FG_LAYERS);

        // Rain Effect
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        rainEffect.render(batch, worldCamera);
        batch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        backgroundShader.dispose();
        whiteTexture.dispose();
        rainEffect.dispose();
    }
}
