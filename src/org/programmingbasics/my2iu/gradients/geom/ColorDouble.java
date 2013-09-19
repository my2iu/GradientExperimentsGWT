package org.programmingbasics.my2iu.gradients.geom;

public class ColorDouble
{
  public ColorDouble() {}
  public ColorDouble(double r, double g, double b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }
  public double r;
  public double g;
  public double b;
  
  public double absDiff(ColorDouble other)
  {
    ColorDouble oldc = this;
    ColorDouble newc = other;
    double maxDelta = 0;
    double delta;
    delta = Math.abs(oldc.r - newc.r);
    if (delta > maxDelta) maxDelta = delta;
    delta = Math.abs(oldc.g - newc.g);
    if (delta > maxDelta) maxDelta = delta;
    delta = Math.abs(oldc.b - newc.b);
    if (delta > maxDelta) maxDelta = delta;
    return maxDelta;
  }
}
