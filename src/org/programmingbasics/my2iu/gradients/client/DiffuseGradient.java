package org.programmingbasics.my2iu.gradients.client;

import org.programmingbasics.my2iu.gradients.geom.ColorDouble;
import org.programmingbasics.my2iu.gradients.geom.PointDouble;

import elemental.client.Browser;
import elemental.dom.TimeoutHandler;
import elemental.html.Window;

public class DiffuseGradient
{
  GLDrawer drawer;
  BitmapDouble bitmap;
  BitmapDouble bitmapNext;
  int timerId;
  boolean useLines = true;
  
  PointDouble[] pointsDouble = new PointDouble[] {
      new PointDouble(0.1, 0.1),
      new PointDouble(0.9, 0.1),
      new PointDouble(0.9, 0.9),
      new PointDouble(0.8, 0.2),
      new PointDouble(0.7, 0.3),
      new PointDouble(0.8, 0.6),
      new PointDouble(0.1, 0.1),
  };
  ColorDouble[] colorsDouble = new ColorDouble[] {
      new ColorDouble(1, 0, 0),
      new ColorDouble(0, 1, 0),
      new ColorDouble(0, 0, 1),
      new ColorDouble(0, 1, 1),
      new ColorDouble(1, 1, 1),
      new ColorDouble(0, 0, 0),
      new ColorDouble(1, 0, 0),
  };
  
  public DiffuseGradient(GLDrawer drawer)
  {
    this.drawer = drawer;
  }
  
  void go()
  {
    bitmap = new BitmapDouble(2, 2);
    bitmapNext = new BitmapDouble(2, 2);
    Window win = Browser.getWindow();
    timerId = win.setInterval(new TimeoutHandler() {
      @Override public void onTimeoutHandler() {
        drawNext();
      }}, 10);
  }
  
  void drawNext()
  {
    bitmap.blurTo(bitmapNext);
    BitmapDouble tmp = bitmapNext;
    bitmapNext = bitmap;
    bitmap = tmp;
    
    if (useLines) 
    {
      for (int n = 0; n < pointsDouble.length - 1; n++)
      {
        bitmap.setLinePercentColor(pointsDouble[n], colorsDouble[n],
            pointsDouble[n+1], colorsDouble[n+1]);
      }
    } 
    else 
    {
      for (int n = 0; n < pointsDouble.length - 1; n++)
      {
        bitmap.setPixelPercentColor(pointsDouble[n].x, pointsDouble[n].y,
            colorsDouble[n].r, colorsDouble[n].g, colorsDouble[n].b);
      }
    }
    
    double delta = bitmap.calculateDelta(bitmapNext);
    
    drawer.drawFullTexture(bitmap);
    drawer.drawPolygon(pointsDouble);
    if (delta < 0.001)
    {
      if (bitmap.width >= 128)
      {
        Browser.getWindow().clearInterval(timerId);
        Browser.getWindow().alert("Done: " + delta + " " + bitmap.width);
        return;
      }
      //Browser.getWindow().alert("" + delta + " " + bitmap.width);
      bitmap = bitmap.copyDouble();
      bitmapNext = bitmapNext.copyDouble();
    }
  }

}
