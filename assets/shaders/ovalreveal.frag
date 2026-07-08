#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
uniform float u_progress; // revealed elliptical radius in uv units (0..~1.45)
uniform float u_feather;  // width of the soft transparent border

void main() {
    vec4 tex = texture2D(u_texture, v_texCoord);
    // Normalised distance from the quad centre. Equal uv distance maps to
    // an oval in screen space because the quad is wider than it is tall,
    // so the shape uncovers evenly from the centre outwards.
    float dist = length((v_texCoord - vec2(0.5)) * 2.0);
    float reveal = 1.0 - smoothstep(u_progress - u_feather,
                                    u_progress + u_feather, dist);
    gl_FragColor = v_color * tex;
    gl_FragColor.a *= reveal;
}
