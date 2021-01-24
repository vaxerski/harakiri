#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec3 Gray = vec3(0.3, 0.59, 0.11);
uniform vec3 RedMatrix   = vec3(1.0, 0.0, 0.0);
uniform vec3 GreenMatrix = vec3(0.0, 1.0, 0.0);
uniform vec3 BlueMatrix  = vec3(0.0, 0.0, 1.0);
uniform vec3 Offset = vec3(0.0, 0.0, 0.0);
uniform vec3 ColorScale = vec3(1.0, 1.0, 1.0);
uniform float Saturation = 1.0;

bool islow(int rgb) {
    int red = (color >> 16 & 255);
    int green = (color >> 8 & 255);
    int blue = (color & 255);

    if(red < 50 && green < 50 && blue < 50)
    {
        return true;
    }
    return false;
}

void main() {
    vec4 InTexel = texture2D(DiffuseSampler, texCoord);

    // Color Matrix
    float RedValue = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue = dot(InTexel.rgb, BlueMatrix);

    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    if(islow(InTexel.rgb)) {
        gl_FragColor = vec4(OutColor, 0.0);
    } else {
        gl_FragColor = vec4(OutColor, 1.0);
    }
}
