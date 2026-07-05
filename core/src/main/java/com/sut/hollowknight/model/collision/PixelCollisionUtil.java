package com.sut.hollowknight.model.collision;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;

// performance expensive
@Deprecated
public class PixelCollisionUtil {

    private static final HashMap<Texture, Pixmap> pixmaps = new HashMap<>();

    // Scratch objects — this class must not allocate per query (GC pressure).
    private static final Rectangle TMP_A = new Rectangle();
    private static final Rectangle TMP_B = new Rectangle();
    private static final Rectangle TMP_INTERSECTION = new Rectangle();

    private static Pixmap getPixmap(Texture texture) {
        Pixmap cached = pixmaps.get(texture);
        if (cached == null) {
            if (!texture.getTextureData().isPrepared()) {
                texture.getTextureData().prepare();
            }
            cached = texture.getTextureData().consumePixmap();
            pixmaps.put(texture, cached);
        }
        return cached;
    }

    public static void dispose() {
        for (Pixmap pixmap : pixmaps.values()) {
            pixmap.dispose();
        }
        pixmaps.clear();
    }

    // Checks if two sprites (TextureRegions) overlap based on their actual opaque pixels.
    public static boolean overlaps(TextureRegion reg1, float x1, float y1, float w1, float h1, boolean flipX1,
                                   TextureRegion reg2, float x2, float y2, float w2, float h2, boolean flipX2) {

        // 1. Fast AABB bounding box check first
        TMP_A.set(x1, y1, w1, h1);
        TMP_B.set(x2, y2, w2, h2);

        if (!TMP_A.overlaps(TMP_B)) return false;

        // 2. Find the intersection rectangle in world space
        Intersector.intersectRectangles(TMP_A, TMP_B, TMP_INTERSECTION);

        Pixmap pix1 = getPixmap(reg1.getTexture());
        Pixmap pix2 = getPixmap(reg2.getTexture());

        // 3. Check pixels only within the intersection area
        for (float py = TMP_INTERSECTION.y; py < TMP_INTERSECTION.y + TMP_INTERSECTION.height; py += 1f) {
            for (float px = TMP_INTERSECTION.x; px < TMP_INTERSECTION.x + TMP_INTERSECTION.width; px += 1f) {

                // Map world coordinates to texture coordinates for Sprite 1
                float localX1 = (px - x1) / w1;
                float localY1 = (py - y1) / h1;

                int texX1 = reg1.getRegionX() + (int)(localX1 * reg1.getRegionWidth());
                int texY1 = reg1.getRegionY() + reg1.getRegionHeight() - (int)(localY1 * reg1.getRegionHeight());
                if (flipX1) {
                    texX1 = reg1.getRegionX() + reg1.getRegionWidth() - (int)(localX1 * reg1.getRegionWidth()) - 1;
                }

                // Map world coordinates to texture coordinates for Sprite 2
                float localX2 = (px - x2) / w2;
                float localY2 = (py - y2) / h2;

                int texX2 = reg2.getRegionX() + (int)(localX2 * reg2.getRegionWidth());
                int texY2 = reg2.getRegionY() + reg2.getRegionHeight() - (int)(localY2 * reg2.getRegionHeight());
                if (flipX2) {
                    texX2 = reg2.getRegionX() + reg2.getRegionWidth() - (int)(localX2 * reg2.getRegionWidth()) - 1;
                }

                // Bounds safety check
                if (texX1 < 0 || texX1 >= pix1.getWidth() || texY1 < 0 || texY1 >= pix1.getHeight() ||
                    texX2 < 0 || texX2 >= pix2.getWidth() || texY2 < 0 || texY2 >= pix2.getHeight()) {
                    continue;
                }

                int pixel1 = pix1.getPixel(texX1, texY1);
                int pixel2 = pix2.getPixel(texX2, texY2);

                // If both pixels are non-transparent (alpha > 0), collision!
                if ((pixel1 & 0x000000FF) > 0 && (pixel2 & 0x000000FF) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
