package org.programmingbasics.my2iu.gradients.client;

import org.programmingbasics.my2iu.gradients.geom.ColorDouble;
import org.programmingbasics.my2iu.gradients.geom.PointDouble;

public class BitmapDouble
{
  double[] data;
  int width;
  int height;
  int numIndices = 3;
  
  public BitmapDouble(int width, int height)
  {
    this.width = width;
    this.height = height;
    data = new double[width * height * numIndices];
  }

  public final void setPixelPercent(double x, double y, double val)
  {
    setPixelPercent(x, y, 0, val);
  }
  public final void setPixelPercent(double x, double y, int index, double val)
  {
    setPixel((int)Math.floor(x*width), (int)Math.floor(y * height), index, val);
  }
  
  public final void setPixelPercentColor(double x, double y, double r, double g, double b)
  {
    setPixelPercent(x, y, 0, r);
    setPixelPercent(x, y, 1, g);
    setPixelPercent(x, y, 2, b);
  }
  
  public void setLinePercentColor(PointDouble p1, ColorDouble c1, PointDouble p2, ColorDouble c2)
  {
    double dx = Math.ceil(Math.abs(p1.x * width - p2.x * width));
    double dy = Math.ceil(Math.abs(p1.y * height - p2.y * height));
    double len = Math.sqrt(dx * dx + dy * dy);
    for (int n = 0; n <= len; n++)
    {
      double alpha = n / len;
      double notalpha = 1 - alpha;
      setPixelPercentColor(
          p1.x * alpha + p2.x * notalpha,
          p1.y * alpha + p2.y * notalpha,
          c1.r * alpha + c2.r * notalpha,
          c1.g * alpha + c2.g * notalpha,
          c1.b * alpha + c2.b * notalpha);
    }
  }

  public final void setPixel(int x, int y, double val)
  {
    setPixel(x, y, 0, val);
  }
  public final void setPixel(int x, int y, int index, double val)
  {
    data[(y * width + x) * numIndices + index] = val;
  }
  public final double getPixel(int x, int y)
  {
    return getPixel(x, y, 0);
  }
  public final double getPixel(int x, int y, int index)
  {
    return data[(y * width + x) * numIndices + index];
  }

  public BitmapDouble copyDouble()
  {
    BitmapDouble bigger = new BitmapDouble(width * 2, height * 2);
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        for (int idx = 0; idx < numIndices; idx++)
        {
          double val = getPixel(x, y, idx);
          bigger.setPixel(x*2, y*2, idx, val);
          bigger.setPixel(x*2+1, y*2, idx, val);
          bigger.setPixel(x*2+1, y*2+1, idx, val);
          bigger.setPixel(x*2, y*2+1, idx, val);
        }
      }
    }
    return bigger;
  }

  public double calculateDelta(BitmapDouble next)
  {
    double maxDelta = 0;
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        for (int idx = 0; idx < numIndices; idx++)
        {
          double newValue = getPixel(x,  y, idx);
          double delta = Math.abs(next.getPixel(x, y, idx) - newValue);
          if (delta > maxDelta)
            maxDelta = delta;
        }
      }
    }
    return maxDelta;
  }

  public void blurTo(BitmapDouble next)
  {
    for (int y = 0; y < height; y++)
    {
      for (int x = 0; x < width; x++)
      {
        for (int idx = 0; idx < numIndices; idx++)
        {
          int count = 0;
          double accum = 0;
          if (x > 0)
          {
            accum += getPixel(x-1, y, idx);
            count++;
          }
          if (y > 0)
          {
            accum += getPixel(x, y-1, idx);
            count++;
          }
          if (x < width-1)
          {
            accum += getPixel(x+1, y, idx);
            count++;
          }
          if (y < height - 1)
          {
            accum += getPixel(x, y+1, idx);
            count++;
          }
          double newValue = accum / count;
          next.setPixel(x, y, idx, newValue);
        }
      }
    }
  }
}
