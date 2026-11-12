#version 330

// Копия dynamictransforms.glsl: moj_import тут нельзя, шейдер нужен ещё до загрузки паков.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

// Копия globals.glsl: moj_import тут нельзя, шейдер нужен ещё до загрузки паков.
layout(std140) uniform Globals {
    ivec3 CameraBlockPos;
    vec3 CameraOffset;
    vec2 ScreenSize;
    float GlintAlpha;
    float GameTime;
    int MenuBlurRadius;
    int UseRgss;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 texel = texture(Sampler0, texCoord0);
    // Экран перенастройки (мира нет, часы стоят): прячем неактивную кнопку «Отключиться».
    // Её спрайт в этом паке отличается от ванильного только альфой 254.
    if (GameTime * 24000.0 < 1.0 && abs(texel.a * 255.0 - 254.0) < 0.5) {
        discard;
    }
    vec4 color = texel * vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
