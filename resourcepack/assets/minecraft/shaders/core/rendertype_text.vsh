#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:switch_logo.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

// Экран перехода (switch_logo.glsl): что это за глиф и где фрагмент внутри композиции.
flat out int switchKind;
out vec2 switchLocal;

void main() {
    vec3 pos = Position;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    texCoord0 = UV0;
    switchKind = SWITCH_KIND_NONE;
    switchLocal = vec2(0.0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    if (ProjMat[3][3] == 1.0) {
        vec4 clip = gl_Position;
        switchScreenVertex(Sampler0, UV0, Color, clip, vertexColor, switchKind, switchLocal);
        gl_Position = clip;
    }
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
