attribute vec2 coordinates;
attribute vec2 textureCoordinates;

varying highp vec2 vTextureCoordinates;

void main(void) {
  gl_Position = vec4(coordinates, 0.0, 1.0);
  vTextureCoordinates = textureCoordinates;
}
