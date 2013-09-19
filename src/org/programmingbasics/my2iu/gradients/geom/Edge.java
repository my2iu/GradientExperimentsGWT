package org.programmingbasics.my2iu.gradients.geom;

public class Edge
{
  public Edge(PointDouble p1, PointDouble p2)
  {
    this.p1 = p1;
    this.p2 = p2;
  }
  public PointDouble p1, p2;
  @Override public int hashCode()
  {
    return p1.hashCode() ^ p2.hashCode();
  }
  @Override public boolean equals(Object obj)
  {
    if (!(obj instanceof Edge)) return false;
    Edge other = (Edge)obj;
    return p1 == other.p1 && p2 == other.p2;
  }
  public double length()
  {
    return Math.sqrt((p2.y - p1.y) * (p2.y - p1.y) + (p2.x - p1.x) * (p2.x - p1.x));
  }
}
