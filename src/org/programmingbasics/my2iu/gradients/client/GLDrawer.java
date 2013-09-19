package org.programmingbasics.my2iu.gradients.client;

import java.util.List;

import org.programmingbasics.my2iu.gradients.geom.ColorDouble;
import org.programmingbasics.my2iu.gradients.geom.PointDouble;
import org.programmingbasics.my2iu.gradients.shaders.GLShaders;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.CanvasElement;
import elemental.html.Float32Array;
import elemental.html.ImageElement;
import elemental.html.Uint8Array;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLProgram;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLShader;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;
import elemental.html.Window;
import elemental.util.ArrayOfNumber;
import elemental.util.Collections;

public class GLDrawer
{
  CanvasElement canvas;
  WebGLRenderingContext gl;
  Document doc;
  Window win = Browser.getWindow();
  WebGLProgram simpleTextureProgram;
  WebGLProgram simpleUniformColorProgram;
  WebGLProgram simpleGradientTrianglesProgram;

  public GLDrawer(String canvasName)
  {
    doc = Browser.getDocument();
    Element el = doc.getElementById(canvasName);
    canvas = (CanvasElement)el;
    gl = (WebGLRenderingContext)canvas.getContext("webgl");
    if (gl == null)
      gl = (WebGLRenderingContext)canvas.getContext("experimental-webgl");
    loadPrograms();
  }

  
  void loadPrograms()
  {
    // Create a simple vertex shader
    //
    String vertCode = GLShaders.INSTANCE.vertexPassthroughWithTexture().getText();
    String fragCode = GLShaders.INSTANCE.fragmentTexture().getText();

    simpleTextureProgram = createShaderProgram(vertCode, fragCode);

    vertCode = GLShaders.INSTANCE.vertexPassthrough().getText();
    fragCode = GLShaders.INSTANCE.fragmentColor().getText();
    simpleUniformColorProgram = createShaderProgram(vertCode, fragCode);
    
    vertCode = GLShaders.INSTANCE.vertexPassthroughWithColours().getText();
    fragCode = GLShaders.INSTANCE.fragmentGradient().getText();
    simpleGradientTrianglesProgram = createShaderProgram(vertCode, fragCode);
  }
  
  private WebGLProgram createShaderProgram(String vertCode, String fragCode)
      throws Error
  {
    WebGLShader vertShader = gl.createShader(gl.VERTEX_SHADER);
    gl.shaderSource(vertShader, vertCode);
    gl.compileShader(vertShader);
    if (gl.getShaderParameter(vertShader, gl.COMPILE_STATUS) == null)
      throw new Error(gl.getShaderInfoLog(vertShader));

    // Create a simple fragment shader
    //
    WebGLShader fragShader = gl.createShader(gl.FRAGMENT_SHADER);
    gl.shaderSource(fragShader, fragCode);
    gl.compileShader(fragShader);
    if (gl.getShaderParameter(fragShader, gl.COMPILE_STATUS) == null)
      throw new Error(gl.getShaderInfoLog(fragShader));

    // Put the vertex shader and fragment shader together into
    // a complete program
    //
    WebGLProgram shaderProgram = gl.createProgram();
    gl.attachShader(shaderProgram, vertShader);
    gl.attachShader(shaderProgram, fragShader);
    gl.linkProgram(shaderProgram);
    if (gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)  == null)
      throw new Error(gl.getProgramInfoLog(shaderProgram));
    return shaderProgram;
  }

  public static native Float32Array float32ArrayFromArray(ArrayOfNumber arr) /*-{
    return new Float32Array(arr);
  }-*/;

  public static native Uint8Array uint8ArrayFromArray(ArrayOfNumber arr) /*-{
    return new Uint8Array(arr);
  }-*/;

  public static ArrayOfNumber doubleArrayToJsArray(double [] array)
  {
    ArrayOfNumber toReturn = Collections.arrayOfNumber();
    for (int n = 0; n < array.length; n++)
      toReturn.set(n, array[n]);
    return toReturn;
  }

  public void drawFullTexture(BitmapDouble bitmap)
  {
    int height = bitmap.height;
    int width = bitmap.width;
    double[] imgData = new double[width * height * 4];
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        imgData[(y * width + x) * 4] = bitmap.getPixel(x, y, 0) * 255;
        imgData[(y * width + x) * 4 + 1] = bitmap.getPixel(x, y, 1) * 255;
        imgData[(y * width + x) * 4 + 2] = bitmap.getPixel(x, y, 2) * 255;
        imgData[(y * width + x) * 4 + 3] = 255;
      }
    }
    for (int n = 0; n < imgData.length; n++)
    {
      imgData[n] = Math.floor(imgData[n]);
      if (imgData[n] < 0) imgData[n] = 0;
      if (imgData[n] > 255) imgData[n] = 255;
    }
    Uint8Array img = uint8ArrayFromArray(doubleArrayToJsArray(imgData));
    
    // Create a texture from the image
    WebGLTexture texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, width, height, 0, gl.RGBA, gl.UNSIGNED_BYTE, img);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_NEAREST);
    gl.generateMipmap(gl.TEXTURE_2D);
    gl.bindTexture(gl.TEXTURE_2D, null);
    
    drawFullTexture(texture);

    gl.deleteTexture(texture);
  }

  public void drawFullTexture(WebGLTexture texture)
  {
    // Copy an array of data points forming a triangle to the
    // graphics hardware
    //
    ArrayOfNumber vertices = doubleArrayToJsArray(new double[] {
      -1, 1, 0, 1, 
      1, -1,  1, 0,
      -1, -1, 0, 0,

      -1, 1, 0, 1, 
      1, 1,  1, 1,
      1, -1,  1, 0,
      });
    WebGLBuffer buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, float32ArrayFromArray(vertices), gl.STATIC_DRAW);

    // Everything we need has now been copied to the graphics
    // hardware, so we can start drawing

    // Clear the drawing surface
    //
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.clear(gl.COLOR_BUFFER_BIT);

    // Tell WebGL which shader program to use
    //
    gl.useProgram(simpleTextureProgram);

    // Tell WebGL that the data from the array of triangle
    // coordinates that we've already copied to the graphics
    // hardware should be fed to the vertex shader as the
    // parameter "coordinates"
    //
    int coordinatesVar = gl.getAttribLocation(simpleTextureProgram, "coordinates");
    int textureCoordinatesVar = gl.getAttribLocation(simpleTextureProgram, "textureCoordinates");
    gl.enableVertexAttribArray(coordinatesVar);
    gl.enableVertexAttribArray(textureCoordinatesVar);
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(coordinatesVar, 2, gl.FLOAT, false, 16, 0);
    gl.vertexAttribPointer(textureCoordinatesVar, 2, gl.FLOAT, false, 16, 8);

    // Now we can tell WebGL to draw the 3 points that make 
    // up the triangle
    //
    gl.activeTexture(gl.TEXTURE0);
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.uniform1i(gl.getUniformLocation(simpleTextureProgram, "uSampler"), 0);
    gl.drawArrays(gl.TRIANGLES, 0, 6);
  }
  
  void drawTriangle()
  {
    // Copy an array of data points forming a triangle to the
    // graphics hardware
    //
    ArrayOfNumber vertices = doubleArrayToJsArray(new double[] {
      0.0, 0.5,
      0.5, -0.5,
      -0.5, -0.5,});
    WebGLBuffer buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, float32ArrayFromArray(vertices), gl.STATIC_DRAW);

    // Create a simple vertex shader
    //
    String vertCode = GLShaders.INSTANCE.vertexPassthrough().getText();
    String fragCode = GLShaders.INSTANCE.fragmentWhite().getText();

    WebGLProgram shaderProgram = createShaderProgram(vertCode, fragCode);

    // Everything we need has now been copied to the graphics
    // hardware, so we can start drawing

    // Clear the drawing surface
    //
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.clear(gl.COLOR_BUFFER_BIT);

    // Tell WebGL which shader program to use
    //
    gl.useProgram(shaderProgram);

    // Tell WebGL that the data from the array of triangle
    // coordinates that we've already copied to the graphics
    // hardware should be fed to the vertex shader as the
    // parameter "coordinates"
    //
    int coordinatesVar = gl.getAttribLocation(shaderProgram, "coordinates");
    gl.enableVertexAttribArray(coordinatesVar);
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(coordinatesVar, 2, gl.FLOAT, false, 0, 0);

    // Now we can tell WebGL to draw the 3 points that make 
    // up the triangle
    //
    gl.drawArrays(gl.TRIANGLES, 0, 3);
  }

  void drawTexturedTriangle(ImageElement img)
  {
    // Create a texture from the image
    WebGLTexture texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, img);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_NEAREST);
    gl.generateMipmap(gl.TEXTURE_2D);
    gl.bindTexture(gl.TEXTURE_2D, null);
    drawFullTexture(texture);
  }

  void drawTexturedTriangle()
  {
    double []rawImageData = new double[] {
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
        255, 0, 0, 255,
    };
    Uint8Array img = uint8ArrayFromArray(doubleArrayToJsArray(rawImageData));
    
    // Create a texture from the image
    WebGLTexture texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, 4, 4, 0, gl.RGBA, gl.UNSIGNED_BYTE, img);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR_MIPMAP_NEAREST);
    gl.generateMipmap(gl.TEXTURE_2D);
    gl.bindTexture(gl.TEXTURE_2D, null);
    drawTexturedTriangle(texture);
  }

  void drawPolygon(List<PointDouble> points)
  {
    double [] points2 = new double[points.size() * 2];
    for (int n = 0; n < points.size(); n++)
    {
      points2[n * 2] = points.get(n).x;
      points2[n * 2 + 1] = points.get(n).y;
    }
    drawPolygon(points2);
  }

  void drawPolygon(PointDouble [] points)
  {
    double [] points2 = new double[points.length * 2];
    for (int n = 0; n < points.length; n++)
    {
      points2[n * 2] = points[n].x;
      points2[n * 2 + 1] = points[n].y;
    }
    drawPolygon(points2);
  }
  void drawPolygon(double[] points)
  {
    // Copy an array of data points forming a triangle to the
    // graphics hardware
    //
    ArrayOfNumber vertices = Collections.arrayOfNumber();
    for (int n = 0; n < (points.length / 2) - 1; n++)
    {
      vertices.push(points[n * 2] * 2 - 1);
      vertices.push(points[n * 2 + 1] * 2 - 1);
      vertices.push(points[(n + 1) * 2] * 2 - 1);
      vertices.push(points[(n + 1) * 2 + 1] * 2 - 1);
    }
    WebGLBuffer buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, float32ArrayFromArray(vertices), gl.STATIC_DRAW);

    // Everything we need has now been copied to the graphics
    // hardware, so we can start drawing

    // Tell WebGL which shader program to use
    //
    gl.useProgram(simpleUniformColorProgram);

    // Tell WebGL that the data from the array of triangle
    // coordinates that we've already copied to the graphics
    // hardware should be fed to the vertex shader as the
    // parameter "coordinates"
    //
    int coordinatesVar = gl.getAttribLocation(simpleUniformColorProgram, "coordinates");
    gl.enableVertexAttribArray(coordinatesVar);
    WebGLUniformLocation colorUniform = gl.getUniformLocation(simpleUniformColorProgram, "uColor");
    gl.uniform4f(colorUniform, 1.0f, 1.0f, 1.0f, 1.0f);
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(coordinatesVar, 2, gl.FLOAT, false, 0, 0);

    // Now we can tell WebGL to draw the 3 points that make 
    // up the triangle
    //
    gl.lineWidth(1.0f);
    gl.drawArrays(gl.LINES, 0, vertices.length() / 2);
  }

  void drawGradientTriangle(List<PointDouble> points, List<ColorDouble> colors)
  {
    // Copy an array of data points forming a triangle to the
    // graphics hardware
    //
    ArrayOfNumber vertices = Collections.arrayOfNumber();
    for (int n = 0; n < 3; n++)
    {
      PointDouble p = points.get(n);
      ColorDouble c = colors.get(n);
      vertices.push(p.x * 2 - 1);
      vertices.push(p.y * 2 - 1);
      vertices.push(c.r);
      vertices.push(c.g);
      vertices.push(c.b);
    }
    WebGLBuffer buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, float32ArrayFromArray(vertices), gl.STATIC_DRAW);

    // Everything we need has now been copied to the graphics
    // hardware, so we can start drawing

    // Tell WebGL which shader program to use
    //
    gl.useProgram(simpleGradientTrianglesProgram);

    // Tell WebGL that the data from the array of triangle
    // coordinates that we've already copied to the graphics
    // hardware should be fed to the vertex shader as the
    // parameter "coordinates"
    //
    int coordinatesVar = gl.getAttribLocation(simpleGradientTrianglesProgram, "coordinates");
    gl.enableVertexAttribArray(coordinatesVar);
    int coloursVar = gl.getAttribLocation(simpleGradientTrianglesProgram, "colours");
    gl.enableVertexAttribArray(coloursVar );
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(coordinatesVar, 2, gl.FLOAT, false, 20, 0);
    gl.vertexAttribPointer(coloursVar, 3, gl.FLOAT, false, 20, 8);

    gl.drawArrays(gl.TRIANGLES, 0, 3);
  }
  
  void drawTexturedTriangle(WebGLTexture texture)
  {
    // Copy an array of data points forming a triangle to the
    // graphics hardware
    //
    ArrayOfNumber vertices = doubleArrayToJsArray(new double[] {
      -0.5, 0.5, -1, 1, 
      0.5, -0.5,  1, -1,
      -0.5, -0.5, -1, -1,});
    WebGLBuffer buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(gl.ARRAY_BUFFER, float32ArrayFromArray(vertices), gl.STATIC_DRAW);

    // Create a simple vertex shader
    //
    String vertCode = GLShaders.INSTANCE.vertexPassthroughWithTexture().getText();
    String fragCode = GLShaders.INSTANCE.fragmentTexture().getText();

    WebGLProgram shaderProgram = createShaderProgram(vertCode, fragCode);

    // Everything we need has now been copied to the graphics
    // hardware, so we can start drawing

    // Clear the drawing surface
    //
    gl.clearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.clear(gl.COLOR_BUFFER_BIT);

    // Tell WebGL which shader program to use
    //
    gl.useProgram(shaderProgram);

    // Tell WebGL that the data from the array of triangle
    // coordinates that we've already copied to the graphics
    // hardware should be fed to the vertex shader as the
    // parameter "coordinates"
    //
    int coordinatesVar = gl.getAttribLocation(shaderProgram, "coordinates");
    int textureCoordinatesVar = gl.getAttribLocation(shaderProgram, "textureCoordinates");
    gl.enableVertexAttribArray(coordinatesVar);
    gl.enableVertexAttribArray(textureCoordinatesVar);
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.vertexAttribPointer(coordinatesVar, 2, gl.FLOAT, false, 16, 0);
    gl.vertexAttribPointer(textureCoordinatesVar, 2, gl.FLOAT, false, 16, 8);

    // Now we can tell WebGL to draw the 3 points that make 
    // up the triangle
    //
    gl.activeTexture(gl.TEXTURE0);
    gl.bindTexture(gl.TEXTURE_2D, texture);
    gl.uniform1i(gl.getUniformLocation(shaderProgram, "uSampler"), 0);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
  }


}
