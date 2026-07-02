#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform vec2 u_resolution;
uniform float u_time;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

// Smooth value noise
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Distant/background falling rain streak layer.
// Same idea as the foreground rain shader but tuned to be softer,
// since this is meant to feel far away and hazy, not crisp.
float rainLayer(vec2 uv, float cellSize, float speed, float slant,
        float density, float streakLen, float thickness) {
    uv.x += uv.y * slant;
    vec2 p = uv * cellSize;
    p.y += u_time * speed; // st.y=0 is bottom in gl_FragCoord space, so += falls downward on screen

    vec2 id = floor(p);
    vec2 f = fract(p);

    float r = hash(id);
    if (r > density) return 0.0;

    float xPos = 0.15 + hash(id + 3.7) * 0.7;
    float xDist = abs(f.x - xPos);

    float head = smoothstep(1.0, 1.0 - streakLen, f.y);
    float fade = pow(head, 2.0);

    float line = smoothstep(thickness, 0.0, xDist);
    return line * fade;
}

void main() {
    // Normalize screen coordinates (0.0 to 1.0)
    vec2 st = gl_FragCoord.xy / u_resolution.xy;
    float aspect = u_resolution.x / u_resolution.y;

    // --- Color Palette ---
    vec3 topColor = vec3(0.08, 0.04, 0.15);
    vec3 midColor = vec3(0.04, 0.05, 0.12);
    vec3 bottomColor = vec3(0.01, 0.02, 0.05);

    vec3 col = mix(bottomColor, midColor, smoothstep(0.0, 0.5, st.y));
    col = mix(col, topColor, smoothstep(0.5, 1.0, st.y));

    // --- Atmospheric Fog/Mist ---
    float n = noise(st * 3.0 + vec2(u_time * 0.03, u_time * 0.01));
    n += noise(st * 6.0 + vec2(u_time * 0.08, -u_time * 0.04)) * 0.5;
    n = smoothstep(0.4, 0.9, n);

    vec3 fogColor = vec3(0.12, 0.08, 0.2);
    col = mix(col, fogColor, n * 0.25);

    // --- Distant Rain ---
    // Aspect-correct so streaks don't stretch/skew on wide screens
    vec2 rainUv = st;
    rainUv.x *= aspect;

    float rain = 0.0;
    // far layer: fine, faint, slow — reads as haze texture more than lines
    rain += rainLayer(rainUv, 30.0, 6.0, 0.12, 0.4, 0.8, 0.03) * 0.15;
    // slightly closer layer: a bit bolder, gives some sense of depth
    rain += rainLayer(rainUv, 18.0, 10.0, 0.12, 0.3, 0.75, 0.04) * 0.2;

    rain = clamp(rain, 0.0, 0.5);

    // Tint rain toward the fog color so it blends into the scene
    // instead of looking like a separate overlay
    vec3 rainColor = mix(fogColor, vec3(0.55, 0.6, 0.75), 0.5);
    col = mix(col, rainColor, rain);

    // --- Vignette ---
    // Applied after rain so streaks fade out toward the edges too,
    // keeping focus on the center like the rest of the scene
    float dist = distance(st, vec2(0.5, 0.6));
    col *= smoothstep(1.0, 0.4, dist);

    gl_FragColor = vec4(col, 1.0);
}
