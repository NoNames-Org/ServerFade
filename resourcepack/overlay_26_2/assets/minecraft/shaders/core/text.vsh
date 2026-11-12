#version 330

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#endif

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:switch_logo.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
in ivec2 UV2;
#endif

uniform sampler2D Sampler0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
#endif
out vec4 vertexColor;
out vec2 texCoord0;

// Экран перехода (switch_logo.glsl): что это за глиф и где фрагмент внутри композиции.
flat out int switchKind;
out vec2 switchLocal;

void main() {
    vec3 pos = Position;
    vertexColor = Color;
    texCoord0 = UV0;
    switchKind = SWITCH_KIND_NONE;
    switchLocal = vec2(0.0);
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
#endif
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
#if defined(IS_GUI)
    if (ProjMat[3][3] == 1.0) {
        vec4 clip = gl_Position;
        switchScreenVertex(Sampler0, UV0, Color, clip, vertexColor, switchKind, switchLocal);
        gl_Position = clip;
    }
#endif
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
#endif
}
