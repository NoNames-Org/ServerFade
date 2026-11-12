#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:switch_screen.glsl>

uniform samplerCube Sampler0;

in vec3 texCoord0;
flat in float switchSpin;

out vec4 fragColor;

// Панорама этого пака нужна только экранам перехода между серверами, поэтому вместо неё всегда фон
// (switch_screen.glsl). Пока мира нет (экран перенастройки), здесь же рисуется композиция: логотип с подписью
// лежит на верхней и нижней гранях панорамы (panorama_4/5) — на одной цвет, на другой альфа, — а точки считаются.
// Грани различаются калибровочными квадратами в углах, по ним же видно, как игра их повернула или отразила.

// Направление в верхнюю (+Y) или нижнюю (-Y) грань для точки грани p (0..1, начало слева сверху, как в PNG).
vec3 switchDir(vec2 p, bool top) {
    vec2 f = p * 2.0 - 1.0;
    return top ? vec3(f.x, 1.0, f.y) : vec3(f.x, -1.0, -f.y);
}

// 0..3 — углы грани цвета (TL, TR, BL, BR), 4..7 — грани альфы, -1 — не угол.
int switchCornerId(vec3 color) {
    ivec3 c = ivec3(round(color * 255.0));
    if (c == SWITCH_CAL_TL) return 0;
    if (c == SWITCH_CAL_TR) return 1;
    if (c == SWITCH_CAL_BL) return 2;
    if (c == SWITCH_CAL_BR) return 3;
    if (c == SWITCH_CAL_A_TL) return 4;
    if (c == SWITCH_CAL_A_TR) return 5;
    if (c == SWITCH_CAL_A_BL) return 6;
    if (c == SWITCH_CAL_A_BR) return 7;
    return -1;
}

// Угол в координатах от центра грани, y вниз.
vec2 switchCornerPos(int id) {
    return vec2((id == 1 || id == 3) ? 0.5 : -0.5, id >= 2 ? 0.5 : -0.5);
}

// Какие калибровочные углы оказались слева и справа сверху, задаёт матрицу из «куда смотрим» в «что там видно».
// Возвращает 1 для грани цвета, 2 для грани альфы, 0 если грань не узнана.
int switchCalibrate(bool top, out mat2 m) {
    m = mat2(1.0);
    float e = (SWITCH_CAL * 0.5) / SWITCH_FACE;
    int a = switchCornerId(texture(Sampler0, switchDir(vec2(e, e), top)).rgb);
    int b = switchCornerId(texture(Sampler0, switchDir(vec2(1.0 - e, e), top)).rgb);
    if (a < 0 || b < 0 || a / 4 != b / 4) {
        return 0;
    }
    vec2 pa = switchCornerPos(a % 4);
    vec2 pb = switchCornerPos(b % 4);
    vec2 c0 = pb - pa;
    vec2 c1 = -(pa + pb);
    if (abs(dot(c0, c1)) > 0.01 || abs(length(c0) - 1.0) > 0.01) {
        return 0;
    }
    m = mat2(c0, c1);
    return 1 + a / 4;
}

vec3 switchFace(vec2 p, bool top, mat2 m) {
    return texture(Sampler0, switchDir(transpose(m) * (p - 0.5) + 0.5, top)).rgb;
}

void main() {
    vec3 background = switchBackground(gl_FragCoord.xy, ScreenSize);
    fragColor = vec4(background, 1.0);
    // Мир есть («Загрузка местности»): композицию рисует шейдер текста, здесь только фон.
    if (GameTime * 24000.0 >= 1.0) {
        return;
    }
    // Метка для box_blur: этот кадр не размывать.
    if (gl_FragCoord.x < 1.0 && gl_FragCoord.y < 1.0) {
        fragColor = vec4(vec3(SWITCH_BLUR_KEY) / 255.0, 1.0);
        return;
    }
    vec2 local = (gl_FragCoord.xy / ScreenSize - 0.5) / switchCompSize(ScreenSize) + 0.5;
    if (any(lessThan(local, vec2(0.0))) || any(greaterThan(local, vec2(1.0)))) {
        return;
    }
    // Как и в шейдере текста: точка внутри композиции, y вниз.
    local.y = 1.0 - local.y;
    // Игра крутит панораму на -spin, а spin растёт на 0.1° за тик: минус даёт часы, идущие вперёд.
    float dots = switchDots(local * SWITCH_COMP_SIZE, fract(-switchSpin / SWITCH_DOT_PERIOD_DEG));
    if (dots > 0.0) {
        fragColor = vec4(mix(background, SWITCH_DOT_RGB, dots), 1.0);
        return;
    }
    mat2 mTop;
    mat2 mBottom;
    int top = switchCalibrate(true, mTop);
    int bottom = switchCalibrate(false, mBottom);
    if (top + bottom != 3) {
        return;
    }
    vec2 p = (SWITCH_FACE_POS + local * SWITCH_FACE_SIZE) / SWITCH_FACE;
    vec3 color = top == 1 ? switchFace(p, true, mTop) : switchFace(p, false, mBottom);
    float alpha = (top == 2 ? switchFace(p, true, mTop) : switchFace(p, false, mBottom)).r;
    fragColor = vec4(mix(background, color, alpha), 1.0);
}
