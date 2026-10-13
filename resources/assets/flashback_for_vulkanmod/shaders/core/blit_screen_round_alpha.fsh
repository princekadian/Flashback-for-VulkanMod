#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 texel = texture(InSampler, texCoord);
    // Any partially transparent pixel becomes fully opaque; fully transparent stays transparent
    fragColor = vec4(texel.rgb, texel.a > 0.0 ? 1.0 : 0.0);
}
