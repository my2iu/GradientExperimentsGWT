attribute vec2 coordinates;
attribute vec3 colours;
varying lowp vec3 vColours;

void main(void) {
  gl_Position = vec4(coordinates, 0.0, 1.0);
  vColours = colours;
}
