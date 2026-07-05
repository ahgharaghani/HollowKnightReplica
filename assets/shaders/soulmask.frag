#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

// Full soul orb (bound to texture unit 0 by the SpriteBatch draw call).
uniform sampler2D u_texture;
// Liquid tile — only its alpha is used, as a rising fill mask (texture unit 1).
uniform sampler2D u_mask;

// Atlas UV rect of the orb region: (u1, v1, u2, v2).
uniform vec4 u_orbRect;
// Atlas UV rect of the current liquid tile.
uniform vec4 u_maskRect;
// Liquid level, 0 (empty) .. 1 (full vessel).
uniform float u_fill;

// The liquid tiles are inset inside their region: transparent margin above the
// wave crest, and a small transparent margin below the body. Map the vessel's
// filled band onto the tile's OPAQUE span so fill=1 reaches the top and a given
// fill fraction covers exactly that fraction of the vessel.
const float TOP_INSET = 0.09;   // transparent rows above the wave crest
const float BOT_INSET = 0.05;   // transparent rows below the body

void main() {
    vec2 orbMin  = u_orbRect.xy;
    vec2 orbSize = u_orbRect.zw - u_orbRect.xy;
    // Position inside the orb quad, 0..1 (texture v runs downward).
    vec2 local = (v_texCoord - orbMin) / orbSize;
    float up = 1.0 - local.y; // 0 = vessel bottom, 1 = vessel top

    // Above the liquid surface = empty vessel.
    if (up > u_fill) {
        discard;
    }

    // 0 at vessel bottom, 1 at the liquid surface.
    float t = up / max(u_fill, 0.0001);
    // Tile row: t=0 -> opaque bottom, t=1 -> wave crest.
    float tileLocalY = (1.0 - BOT_INSET) - t * ((1.0 - BOT_INSET) - TOP_INSET);

    vec2 maskMin  = u_maskRect.xy;
    vec2 maskSize = u_maskRect.zw - u_maskRect.xy;
    vec2 maskUV = vec2(
        maskMin.x + local.x * maskSize.x,
        maskMin.y + tileLocalY * maskSize.y);

    // Tile alpha carries the wavy surface: opaque body reveals the orb, the
    // transparent region above the wave leaves the vessel empty.
    if (texture2D(u_mask, maskUV).a < 0.5) {
        discard;
    }

    // Reveal the underlying full orb. Its own alpha defines the round shape.
    gl_FragColor = texture2D(u_texture, v_texCoord) * v_color;
}
