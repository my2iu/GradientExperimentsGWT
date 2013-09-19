package org.programmingbasics.my2iu.gradients.client;

import org.programmingbasics.my2iu.gradients.geom.ColorDouble;
import org.programmingbasics.my2iu.gradients.geom.PointDouble;

import elemental.client.Browser;
import elemental.dom.TimeoutHandler;
import elemental.html.Window;

public class MeanValueGradient
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
  
  boolean doColorTest;
  boolean bilinearTest;
  public MeanValueGradient(GLDrawer drawer, boolean doColorTest, boolean bilinearTest)
  {
    this.drawer = drawer;
    this.bilinearTest = bilinearTest;
    this.doColorTest = doColorTest;
    if (doColorTest) {
      colorsDouble = new ColorDouble[] {
        new ColorDouble(0.1, 0.1, 0.1),
        new ColorDouble(0.1, 0.1, 0.1),
        new ColorDouble(0.1, 0.1, 0.1),
        new ColorDouble(0.1, 0.1, 0.1),
        new ColorDouble(1, 1, 1),
        new ColorDouble(1, 1, 1),
        new ColorDouble(0.1, 0.1, 0.1),
      };
    }
    if (bilinearTest) {
      pointsDouble = new PointDouble[] {
          new PointDouble(0.1, 0.1),
          new PointDouble(0.8, 0.1),
          new PointDouble(0.9, 0.5),
          new PointDouble(0.2, 0.5),
          new PointDouble(0.1, 0.1),
      };
      colorsDouble = new ColorDouble[] {
          new ColorDouble(0, 0, 0),
          new ColorDouble(1, 0, 0),
          new ColorDouble(1, 1, 0),
          new ColorDouble(0, 1, 0),
          new ColorDouble(0, 0, 0),
      };      
    }
  }
  
  void go()
  {
    bitmap = new BitmapDouble(256, 256);
    
    double weights[] = new double[pointsDouble.length - 1];
    for (double x = 0; x <= 1; x += 1.0 / 256)
    {
      for (double y = 0; y <= 1; y += 1.0 / 256)
      {
        for (int n = 0; n < pointsDouble.length - 1; n++)
        {
          PointDouble viminus = (n > 0) ? pointsDouble[n-1] : pointsDouble[pointsDouble.length - 2]; 
          PointDouble vi = pointsDouble[n];
          PointDouble viplus = pointsDouble[n+1];
          double dx = vi.x - x;
          double dy = vi.y - y;
          double dxminus = viminus.x - x;
          double dyminus = viminus.y - y;
          double dxplus = viplus.x - x;
          double dyplus = viplus.y - y;
          weights[n] = Math.tan((Math.atan2(dy, dx) - Math.atan2(dyminus, dxminus)) / 2);
          weights[n] += Math.tan((Math.atan2(dyplus, dxplus) - Math.atan2(dy, dx)) / 2);
          weights[n] /= Math.sqrt(dy * dy + dx * dx);
        }
        double denom = 0;
        for (int n = 0; n < pointsDouble.length - 1; n++)
        {
          denom += weights[n];
        }
        ColorDouble c = new ColorDouble();
        for (int n = 0; n < pointsDouble.length - 1; n++)
        {
          double lambda = weights[n] / denom;
          c.r += colorsDouble[n].r * lambda;
          c.g += colorsDouble[n].g * lambda;
          c.b += colorsDouble[n].b * lambda;
        }
        if (doColorTest && c.r < 0.09) 
          bitmap.setPixelPercentColor(x, y, 1, 0, 0);
        else if (bilinearTest) 
        {
          int num = (int)(Math.floor(c.r * 11) + Math.floor(c.g * 11));
          if ((num %2) > 0)
            bitmap.setPixelPercentColor(x, y, 0.5, 0.5, 0.5);
          else
            bitmap.setPixelPercentColor(x, y, 0, 0, 0);

        }
        else
          bitmap.setPixelPercentColor(x, y, c.r, c.g, c.b);
        
      }
    }
    
    drawer.drawFullTexture(bitmap);
    drawer.drawPolygon(pointsDouble);
    //bitmapNext = new BitmapDouble(2, 2);
    //Window win = Browser.getWindow();
    //timerId = win.setInterval(new TimeoutHandler() {
    //  @Override public void onTimeoutHandler() {
    //    drawNext();
    //  }}, 10);
  }
}
