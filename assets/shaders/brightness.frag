#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
varying vec4 v_color;

uniform sampler2D u_texture;
uniform float u_brightness;

void main() {
    vec4 color = texture2D(u_texture, v_texCoord);

    color.rgb *= v_color.rgb;

    if (u_brightness <= 1.0) {
        // Darken
        gl_FragColor = vec4(color.rgb * u_brightness, color.a * v_color.a);
    } else {
        // Brighten
        float excess = u_brightness - 1.0;
        float brighten = 1.0 - exp(-excess * 2.0);
        vec3 brightened = mix(color.rgb, vec3(1.0), brighten * 0.7);
        gl_FragColor = vec4(brightened, color.a * v_color.a);
    }
}
