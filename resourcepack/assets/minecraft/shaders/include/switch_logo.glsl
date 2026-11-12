// Экран перехода между серверами (Bungee), часть шейдера текста (вершины). Вид — в switch_screen.glsl.
// Тексты экранов подключения в lang заменены на символ U+E1F0 (шрифт serverfade:logo): его картинка — композиция
// (логотип + подпись), шейдер узнаёт её по меткам в четырёх углах и ставит по центру экрана, где бы игра ни
// поставила строку. Пока мира нет, композицию рисует панорама (panorama.fsh), а здесь она прячется.
// U+E1F1 — та же композиция в title плагина ServerFade, U+E000 (шрифт serverfade:fade) — его фон: глиф весь залит
// меткой, его вершины разносятся по углам экрана, а цвет рисует фрагментный шейдер.
// Нужны projection.glsl и globals.glsl (ProjMat, ScreenSize, GameTime).

#moj_import <minecraft:switch_screen.glsl>

// Мира нет (экран перенастройки): игровые часы стоят на частичном тике.
bool switchNoWorld() {
    return GameTime * 24000.0 < 1.0;
}

int switchGlyphKind(sampler2D atlas, vec2 uv) {
    ivec2 texel = ivec2(uv * vec2(textureSize(atlas, 0)));
    ivec4 mark = ivec4(round(texelFetch(atlas, texel, 0) * 255.0));
    if (mark == SWITCH_MARK || mark == SWITCH_MARK_STEADY) {
        return SWITCH_KIND_COMP;
    }
    return mark == ivec4(SWITCH_FADE_MARK, 255) ? SWITCH_KIND_FADE : SWITCH_KIND_NONE;
}

// Текст кнопки «Отключиться» на экране перенастройки всегда в этой полосе под центром (в пикселях GUI).
bool switchInReconfigButton(vec4 position) {
    vec2 gui = vec2(2.0 / ProjMat[0][0], -2.0 / ProjMat[1][1]);
    vec2 offset = vec2(position.x, -position.y) / position.w * 0.5 * gui;
    return abs(offset.x) <= 76.0 && offset.y >= 3.0 && offset.y <= 27.0;
}

// kind и local уходят во фрагментный шейдер: local — точка внутри композиции (0..1, y вниз).
void switchScreenVertex(sampler2D atlas, vec2 uv, vec4 color, inout vec4 position, inout vec4 tint,
                        out int kind, out vec2 local) {
    kind = SWITCH_KIND_NONE;
    local = vec2(0.0);
    int glyph = switchGlyphKind(atlas, uv);
    if (glyph == SWITCH_KIND_NONE) {
        if (switchNoWorld() && switchInReconfigButton(position)) {
            tint.a = 0.0;
        }
        return;
    }
    // Тень — та же строка цветом ×0.25; без мира композицию рисует панорама.
    if (max(color.r, max(color.g, color.b)) < 0.5 || (glyph == SWITCH_KIND_COMP && switchNoWorld())) {
        position = vec4(2.0, 2.0, 2.0, 1.0);
        return;
    }
    // Край глифа по UV: игра сдвигает UV внутрь на сотую текселя, у левого/верхнего края дробная часть ~0.01.
    vec2 corner = step(0.5, fract(uv * vec2(textureSize(atlas, 0))));
    kind = glyph;
    if (glyph == SWITCH_KIND_FADE) {
        position.xy = vec2(mix(-1.0, 1.0, corner.x), mix(1.0, -1.0, corner.y)) * position.w;
        tint = vec4(1.0, 1.0, 1.0, color.a);
        return;
    }
    // В NDC экран от -1 до 1, так что половина размера в NDC равна доле экрана.
    vec2 size = switchCompSize(ScreenSize);
    position.xy = vec2(mix(-size.x, size.x, corner.x), mix(size.y, -size.y, corner.y)) * position.w;
    local = corner;
    // Картинка глифа почти прозрачная (иначе чужой шейдер текста нарисовал бы её мелко в строке): возвращаем альфу.
    tint = vec4(1.0, 1.0, 1.0, color.a * SWITCH_ALPHA_SCALE);
}
