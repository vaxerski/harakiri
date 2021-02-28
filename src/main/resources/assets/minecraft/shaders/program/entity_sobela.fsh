#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

//uniform vec3 color;
uniform float outlineAlpha;
uniform float filledAlpha;
uniform float width;

void main(){
    vec4 center = texture2D(DiffuseSampler, texCoord);
    vec4 left = texture2D(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right = texture2D(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up = texture2D(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down = texture2D(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));
    vec3 outColor = center.rgb * center.a + left.rgb * left.a + right.rgb * right.a + up.rgb * up.a + down.rgb * down.a;
    //gl_FragColor = vec4(outColor * 0.4, total);

    int intWidth = int(width);

    vec4 result = vec4(0,0,0,0);

    if (center.a == 0.0) {
        // Scan pixels nearby
        for (int sampleX = -intWidth; sampleX <= intWidth; sampleX++) {
            for (int sampleY = -intWidth; sampleY <= intWidth; sampleY++) {
                vec2 sampleCoord = vec2(float(sampleX), float(sampleY)) * oneTexel;
                vec4 sampleColor = texture2D(DiffuseSampler, texCoord + sampleCoord);
                if (sampleColor.a > 0.0) {
                    // If we find a pixel that isn't transparent, set the frag color to it and replace the alpha.
                    result = vec4(sampleColor.rgb, outlineAlpha);
                }
            }
        }
    }

    gl_FragColor = result;
}
