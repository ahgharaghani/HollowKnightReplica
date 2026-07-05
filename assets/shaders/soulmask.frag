#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
// Atlas UV rect of the current liquid frame: (u1, v1, u2, v2).
uniform vec4 u_uvRect;
// Liquid level, 0 (empty) .. 1 (full vessel).
uniform float u_fill;

void main() {
    vec2 uvMin  = u_uvRect.xy;
    vec2 uvSize = u_uvRect.zw - u_uvRect.xy;
    // Position inside the drawn quad, 0..1 (texture v runs downward).
    vec2 local = (v_texCoord - uvMin) / uvSize;
    float up = 1.0 - local.y; // 0 = vessel bottom, 1 = vessel top

    // Circular vessel mask, inscribed in the quad: the square liquid frames
    // are only visible through the orb's round window.
    if (distance(vec2(local.x, up), vec2(0.5, 0.5)) > 0.48) {
        discard;
    }

    // The liquid sprite slides upward with the fill level: a point `up`
    // above the vessel bottom samples the sprite at `up + (1 - fill)`.
    // Anything above the sprite's wavy top surface is empty vessel.
    float srcUp = up + (1.0 - u_fill);
    if (srcUp > 1.0) {
        discard;
    }

    float v = uvMin.y + (1.0 - srcUp) * uvSize.y;
    gl_FragColor = texture2D(u_texture, vec2(v_texCoord.x, v)) * v_color;
}
