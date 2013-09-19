package org.programmingbasics.my2iu.gradients.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Triangle
{
  public Triangle(PointDouble p1, PointDouble p2, PointDouble p3)
  {
    this.points.add(p1);
    this.points.add(p2);
    this.points.add(p3);
  }
  public Triangle(List<PointDouble> points)
  {
    assert(points.size() == 3);
    this.points.addAll(points);
  }
  public List<PointDouble> points = new ArrayList<PointDouble>();
  
  public List<Edge> getEdges()
  {
    List<Edge> edges = new ArrayList<Edge>();
    edges.add(new Edge(points.get(0), points.get(1)));
    edges.add(new Edge(points.get(1), points.get(2)));
    edges.add(new Edge(points.get(2), points.get(0)));
    return edges;
  }
  
  public boolean containsEdge(Edge edge)
  {
    return points.contains(edge.p1) && points.contains(edge.p2);
  }

  PointDouble getCircularPoint(int index)
  {
    if (index < 0)
      return points.get(3 + (index % 3));
    else
      return points.get(index % 3);
  }
  
  int indexOfPointOppositeEdge(Edge edge)
  {
    for (int n = 0; n < 3; n++)
    {
      if (points.get(n) != edge.p1 && points.get(n) != edge.p2)
        return n;
    }
    throw new IllegalArgumentException("No point opposite the given edge");
  }
  
  public List<Triangle> splitAlongEdge(Edge edge, PointDouble pointSplit)
  {
    List<Triangle> newTriangles = new ArrayList<Triangle>();
    int idx = indexOfPointOppositeEdge(edge);
    newTriangles.add(new Triangle(
        getCircularPoint(idx), getCircularPoint(idx + 1), pointSplit));
    newTriangles.add(new Triangle(
        getCircularPoint(idx-1), getCircularPoint(idx), pointSplit));
    return newTriangles;
  }
  
  public List<Edge> getAdjacentEdges(PointDouble p)
  {
    if (!points.contains(p)) return Collections.emptyList();
    int idx = points.indexOf(p);
    List<Edge> edges = new ArrayList<Edge>();
    edges.add(new Edge(getCircularPoint(idx), getCircularPoint(idx+1)));
    edges.add(new Edge(getCircularPoint(idx), getCircularPoint(idx-1)));
    return edges;
  }
}
