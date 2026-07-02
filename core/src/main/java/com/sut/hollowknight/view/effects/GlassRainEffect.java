package com.sut.hollowknight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;

public class GlassRainEffect implements Disposable {

    private static final String VERT = Gdx.files.internal("shaders/default.vert").readString();
    private static final String FRAG = Gdx.files.internal("shaders/cityoftears/glassrain.frag").readString();

    private static final String GLASS_LAYER_NAME = "Glasses";
    private static final String RAIN_PROPERTY = "rainDrops";

    private static class GlassArea {
        Mesh mesh;
        float boundsX, boundsY, boundsWidth, boundsHeight;
        float seed;
        float[] debugVerts;
    }

    private final ShaderProgram shader;
    private final Array<GlassArea> glassAreas = new Array<>();
    private float time = 0f;

    public GlassRainEffect(TiledMap map, float mapHeightPx) {
        shader = new ShaderProgram(VERT, FRAG);
        if (!shader.isCompiled()) {
            Gdx.app.error("GlassRainEffect", "Shader compilation failed:\n" + shader.getLog());
        }
        loadFromMap(map, mapHeightPx);
    }

    private void loadFromMap(TiledMap map, float mapHeightPx) {
        MapLayer layer = map.getLayers().get(GLASS_LAYER_NAME);
        if (layer == null) return;

        for (MapObject obj : layer.getObjects()) {
            MapProperties props = obj.getProperties();

            float[] worldVerts;
            if (obj instanceof PolygonMapObject) {
                worldVerts = extractPolygonWorldVertices((PolygonMapObject) obj, mapHeightPx);
            } else if (obj instanceof RectangleMapObject) {
                worldVerts = extractRectangleWorldVertices((RectangleMapObject) obj);
            } else {
                continue;
            }

            Integer id = props.get("id", Integer.class);
            int seedSource = (id != null) ? id : (int) (worldVerts[0] * 13.7f + worldVerts[1] * 91.3f);
            float seed = (seedSource % 1000) / 1000f * 100f;

            glassAreas.add(buildGlassArea(worldVerts, seed));
        }
    }

    private float[] extractRectangleWorldVertices(RectangleMapObject obj) {
        Rectangle r = obj.getRectangle();
        return new float[] {
            r.x,         r.y,
            r.x + r.width, r.y,
            r.x + r.width, r.y + r.height,
            r.x,         r.y + r.height
        };
    }

    private float[] extractPolygonWorldVertices(PolygonMapObject obj, float mapHeightPx) {
        Polygon polygon = obj.getPolygon();
        return polygon.getTransformedVertices();
    }

    private GlassArea buildGlassArea(float[] worldVerts, float seed) {
        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        ShortArray triIndices = triangulator.computeTriangles(worldVerts);

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < worldVerts.length; i += 2) {
            minX = Math.min(minX, worldVerts[i]);
            maxX = Math.max(maxX, worldVerts[i]);
            minY = Math.min(minY, worldVerts[i + 1]);
            maxY = Math.max(maxY, worldVerts[i + 1]);
        }
        float boundsW = Math.max(maxX - minX, 0.0001f);
        float boundsH = Math.max(maxY - minY, 0.0001f);

        int vertCount = worldVerts.length / 2;
        float colorBits = Color.WHITE.toFloatBits();
        float[] meshVerts = new float[vertCount * 5]; // x, y, colorPacked, u, v

        int vi = 0;
        for (int i = 0; i < worldVerts.length; i += 2) {
            float x = worldVerts[i];
            float y = worldVerts[i + 1];
            meshVerts[vi++] = x;
            meshVerts[vi++] = y;
            meshVerts[vi++] = colorBits;
            meshVerts[vi++] = (x - minX) / boundsW;
            meshVerts[vi++] = (y - minY) / boundsH;
        }

        Mesh mesh = new Mesh(true, vertCount, triIndices.size,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(meshVerts);
        mesh.setIndices(triIndices.toArray());

        GlassArea area = new GlassArea();
        area.mesh = mesh;
        area.boundsX = minX;
        area.boundsY = minY;
        area.boundsWidth = boundsW;
        area.boundsHeight = boundsH;
        area.seed = seed;
        area.debugVerts = worldVerts;
        return area;
    }

    public void update(float delta) {
        time += delta;
    }

    public void render(Camera camera) {
        if (glassAreas.isEmpty()) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", camera.combined);

        for (GlassArea area : glassAreas) {
            shader.setUniformf("u_time", time);
            shader.setUniformf("u_resolution", area.boundsWidth, area.boundsHeight);
            shader.setUniformf("u_seed", area.seed);
            area.mesh.render(shader, GL20.GL_TRIANGLES);
        }
    }

    public void renderDebugOutline(ShapeRenderer shapeRenderer, Camera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        for (GlassArea area : glassAreas) {
            float[] v = area.debugVerts;
            for (int i = 0; i < v.length; i += 2) {
                int j = (i + 2) % v.length;
                shapeRenderer.line(v[i], v[i+1], v[j], v[j+1]);
            }
        }
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shader.dispose();
        for (GlassArea area : glassAreas) {
            area.mesh.dispose();
        }
    }
}
