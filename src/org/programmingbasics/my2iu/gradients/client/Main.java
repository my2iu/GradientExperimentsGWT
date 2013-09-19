package org.programmingbasics.my2iu.gradients.client;

import org.programmingbasics.my2iu.gradients.shaders.GLShaders;

import com.google.gwt.core.client.JsDate;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.TimeoutHandler;
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
import elemental.html.Window;
import elemental.util.ArrayOfNumber;
import elemental.util.Collections;

public class Main
{
  
  public void go()
  {
    GLDrawer drawer = new GLDrawer("canvas");
//    DiffuseGradient gradient = new DiffuseGradient(drawer);
//    MeanValueGradient gradient = new MeanValueGradient(drawer, false, true);
    PrecomputeGradient gradient = new PrecomputeGradient(drawer, true, false);
    gradient.go();
//    final ImageElement img = Util.createImage();
//    img.setOnload(new EventListener() {
//      @Override public void handleEvent(Event evt)
//      {
//        evt.preventDefault();
//        evt.stopPropagation();
//        drawer.drawTexturedTriangle(img);
//      }});
//    img.setSrc("resources/testtexture.png");
  }
}
