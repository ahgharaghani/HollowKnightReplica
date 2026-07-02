#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;

uniform float u_time;
uniform vec2 u_resolution; // size of this glass pane, in pixels
uniform float u_seed;      // per-pane offset so panes don't repeat identically

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7)) + u_seed) * 43758.5453123);
}

void main() {
    // Aspect-correct so drops stay round instead of stretching to the pane's shape
    float aspect = u_resolution.x / u_resolution.y;
    vec2 uv = v_texCoord;
    uv.x *= aspect;

    float cellDensity = 6.0; // roughly how many potential drops fit across the pane
    vec2 p = uv * cellDensity;
    vec2 id = floor(p);
    vec2 f = fract(p);

    float r = hash(id);
    float total = 0.0;

    // ~35% of cells get a drop
    if (r > 0.65) {
        float delay = hash(id + 1.0) * 12.0;
        float duration = 5.0 + hash(id + 2.0) * 5.0;
        float cycle = 16.0 + hash(id + 5.0) * 6.0; // stagger so panes don't sync up
        float t = mod(u_time + delay, cycle);

        if (t < duration) {
            float fadeIn  = smoothstep(0.0, 0.6, t);
            float fadeOut = smoothstep(duration, duration - 1.2, t);
            float alpha = fadeIn * fadeOut;

            vec2 dropPos = vec2(0.25 + hash(id + 3.0) * 0.5,
                    0.25 + hash(id + 4.0) * 0.5);
            float dist = length(f - dropPos);
            float drop = smoothstep(0.22, 0.0, dist);

            // small offset highlight sells "wet glass" rather than a flat blob
            float highlight = smoothstep(0.08, 0.0, length(f - dropPos - vec2(0.04, 0.05))) * 0.5;

            total += (drop + highlight) * alpha;
        }
    }

    total = clamp(total, 0.0, 0.75);

    vec3 dropColor = vec3(0.8, 0.87, 0.95);
    gl_FragColor = vec4(dropColor, total) * v_color.a;
}
