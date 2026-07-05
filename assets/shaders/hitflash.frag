#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform sampler2D u_texture;
// 0 = normal sprite, 1 = fully white silhouette.
uniform float u_flash;

void main() {
    vec4 tex = texture2D(u_texture, v_texCoord) * v_color;
    gl_FragColor = vec4(mix(tex.rgb, vec3(1.0), u_flash), tex.a);
}
