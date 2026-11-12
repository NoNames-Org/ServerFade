#version 330

#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:switch_defines.glsl>

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

in vec2 texCoord;

out vec4 fragColor;

// Ванильный блюр: за счёт GL_LINEAR берёт выборки между пикселями с шагом 2, вдвое меньше чтений.
// Последний пиксель идёт с половинным весом, потому что пикселей всегда нечётно (actualRadius * 2 + 1).
void main() {
    // Экран перехода между серверами: панорама пометила кадр пикселем (0,0), логотип остаётся чётким.
    if (ivec3(round(texelFetch(InSampler, ivec2(0), 0).rgb * 255.0)) == SWITCH_BLUR_KEY) {
        fragColor = texture(InSampler, texCoord);
        return;
    }
    vec2 oneTexel = 1.0 / InSize;
    vec2 sampleStep = oneTexel * BlurDir;

    vec4 blurred = vec4(0.0);
    float actualRadius = Radius >= 0.5 ? round(Radius) : float(MenuBlurRadius);
    for (float a = -actualRadius + 0.5; a <= actualRadius; a += 2.0) {
        blurred += texture(InSampler, texCoord + sampleStep * a);
    }
    blurred += texture(InSampler, texCoord + sampleStep * actualRadius) / 2.0;
    fragColor = blurred / (actualRadius + 0.5);
}
