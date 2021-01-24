#version 120

uniform sampler2D DiffuseSampler;

uniform vec4 ColorModulate;

varying vec2 texCoord;

void main(){
    if(!islow(texture2D(DiffuseSampler, texCoord).rgb)) {
        gl_FragColor = texture2D(DiffuseSampler, texCoord) * ColorModulate;
    }
    else {
        gl_FragColor = texture2D(DiffuseSampler, texCoord) * vec4(0,0,0,0);
    }
}

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