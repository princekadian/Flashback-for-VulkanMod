#version 150

in vec3 Position;

// Unused, but VulkanMod needs at least one uniform to build a valid uniform block
uniform vec4 ColorModulator;

out vec2 texCoord;

void main() {
    gl_Position = vec4(Position.xy * 2.0 - 1.0, 1.0, 1.0);
    texCoord = Position.xy;
}
