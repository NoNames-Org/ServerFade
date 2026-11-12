#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:switch_screen.glsl>

uniform sampler2D Sampler0;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

flat in int switchKind;
in vec2 switchLocal;

out vec4 fragColor;

void main() {
    // Экран перехода между серверами: фон и композиция (логотип, точки, подпись).
    vec4 switchColor;
    if (switchScreenFragment(switchKind, switchLocal, texture(Sampler0, texCoord0), vertexColor, ColorModulator,
            gl_FragCoord.xy, ScreenSize, GameTime * 24000.0, switchColor)) {
        if (switchColor.a < 0.004) {
            discard;
        }
        fragColor = switchColor;
        return;
    }
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
