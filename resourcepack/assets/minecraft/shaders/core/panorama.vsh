#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;

out vec3 texCoord0;
// Вращение панорамы — единственные часы на экране без мира: по нему бегут точки индикатора.
flat out float switchSpin;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = Position;
    switchSpin = degrees(atan(ModelViewMat[2][0], ModelViewMat[0][0]));
}
