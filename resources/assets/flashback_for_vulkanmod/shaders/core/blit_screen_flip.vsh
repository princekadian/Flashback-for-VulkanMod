#version 150

in vec3 Position;

// Unused, but VulkanMod needs at least one uniform to build a valid uniform block
uniform vec4 ColorModulator;

out vec2 texCoord;

void main() {
    // Full-screen quad from the unit square, mirrored vertically
    gl_Position = vec4(Position.x * 2.0 - 1.0, 1.0 - Position.y * 2.0, 1.0, 1.0);
    texCoord = Position.xy;
}
