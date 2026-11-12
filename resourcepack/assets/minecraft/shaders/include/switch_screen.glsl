// Экран перехода между серверами: общий вид для всех его фаз.
//   * затемнение плагина ServerFade (title: глиф U+E000 растянут на экран, глиф U+E1F1 — композиция);
//   * экран перенастройки без мира — рисует панорама (panorama.fsh);
//   * «Загрузка местности» — фон рисует панорама, композицию шейдер текста (глиф U+E1F0 из lang).
// Везде один и тот же фон, одна и та же композиция (логотип + три точки + «ПОДКЛЮЧЕНИЕ») в одном месте,
// поэтому смена фазы не видна. Точки — бегущая волна, единственное, что движется.
// Чистые функции: нужны только switch_defines.glsl и размер экрана.

#moj_import <minecraft:switch_defines.glsl>

#define SWITCH_KIND_NONE 0
#define SWITCH_KIND_COMP 1
#define SWITCH_KIND_FADE 2

// Размер композиции: x — доля ширины экрана, y — доля высоты.
vec2 switchCompSize(vec2 screen) {
    vec2 size = vec2(SWITCH_HEIGHT * SWITCH_ASPECT * screen.y / screen.x, SWITCH_HEIGHT);
    if (size.x > SWITCH_MAX_WIDTH) {
        size *= SWITCH_MAX_WIDTH / size.x;
    }
    return size;
}

float switchHash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// Фон по пикселю экрана (gl_FragCoord, y вверх): тёмная слива к почти чёрным краям и мягкое розовое свечение
// за логотипом. Шум в полшага цвета убирает полосы на тёмном градиенте.
vec3 switchBackground(vec2 frag, vec2 screen) {
    vec2 p = (frag - 0.5 * screen) / screen.y;
    float r = length(p * vec2(0.75, 1.0));
    vec3 color = mix(SWITCH_BG_CENTER, SWITCH_BG_EDGE, smoothstep(0.0, 0.85, r));
    vec2 size = switchCompSize(screen);
    float logoY = size.y * (0.5 - 0.5 * SWITCH_LOGO_H / SWITCH_COMP_SIZE.y);
    vec2 g = (p - vec2(0.0, logoY)) * vec2(0.55, 1.0) / SWITCH_GLOW_RADIUS;
    color += SWITCH_GLOW_RGB * SWITCH_GLOW_STRENGTH * exp(-dot(g, g));
    color += (switchHash(floor(frag)) - 0.5) / 255.0;
    return clamp(color, 0.0, 1.0);
}

// Непрозрачность точек в точке композиции q (пиксели логотипа, y вниз); phase — доля периода волны.
float switchDots(vec2 q, float phase) {
    if (q.y < SWITCH_DOTS_Y || q.y >= SWITCH_DOTS_Y + SWITCH_DOT) {
        return 0.0;
    }
    float i = round((q.x - 0.5 * SWITCH_COMP_SIZE.x) / SWITCH_DOT_STEP);
    if (abs(i) > 1.0) {
        return 0.0;
    }
    float center = 0.5 * SWITCH_COMP_SIZE.x + i * SWITCH_DOT_STEP;
    if (abs(q.x - center) >= 0.5 * SWITCH_DOT) {
        return 0.0;
    }
    float wave = 0.5 + 0.5 * cos(6.2831853 * fract(phase - (i + 1.0) * 0.2));
    return mix(0.22, 1.0, wave * wave);
}

// Шейдер текста: цвет фрагмента фона и композиции (kind и local — из switchScreenVertex); для остального текста
// возвращает false. frag — gl_FragCoord.xy, ticks — GameTime * 24000.
bool switchScreenFragment(int kind, vec2 local, vec4 texel, vec4 tint, vec4 modulator, vec2 frag, vec2 screen,
                          float ticks, out vec4 color) {
    color = vec4(0.0);
    if (kind == SWITCH_KIND_FADE) {
        color = vec4(switchBackground(frag, screen), tint.a * modulator.a);
        return true;
    }
    if (kind != SWITCH_KIND_COMP) {
        return false;
    }
    color = clamp(texel * tint * modulator, 0.0, 1.0);
    float dots = switchDots(local * SWITCH_COMP_SIZE, fract(ticks / SWITCH_DOT_PERIOD_TICKS));
    if (dots > 0.0) {
        color = vec4(SWITCH_DOT_RGB, dots * tint.a / SWITCH_ALPHA_SCALE * modulator.a);
    }
    return true;
}
