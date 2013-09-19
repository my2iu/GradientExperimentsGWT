varying highp vec2 vTextureCoordinates;

uniform sampler2D uSampler;

void main(void) {
   gl_FragColor = texture2D(uSampler, vTextureCoordinates);
}