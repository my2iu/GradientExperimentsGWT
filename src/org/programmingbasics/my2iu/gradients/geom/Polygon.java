package org.programmingbasics.my2iu.gradients.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;
import org.programmingbasics.my2iu.gradients.client.BitmapDouble;

import elemental.client.Browser;

public class Polygon
{
  public List<PointDouble> exteriorPoints = new ArrayList<PointDouble>();
  public Map<PointDouble, ColorDouble> colorMap = new HashMap<PointDouble, ColorDouble>();
  public List<Triangle> triangles = new ArrayList<Triangle>();
  public Set<Edge> exteriorEdges = new HashSet<Edge>();

  public Polygon copy()
  {
    Polygon copy = new Polygon();
    copy.exteriorPoints.addAll(exteriorPoints);
    copy.colorMap.putAll(colorMap);
    copy.triangles.addAll(triangles);
    copy.exteriorEdges.addAll(exteriorEdges);
    return copy;
  }
  
  private Polygon()
  {
  }
  
  public Polygon(List<PointDouble> exteriorPoints, List<ColorDouble> exteriorColours)
  {
    this.exteriorPoints.addAll(exteriorPoints);
    for (int n = 0; n < exteriorPoints.size() - 1; n++)
    {
      exteriorEdges.add(new Edge(exteriorPoints.get(n), exteriorPoints.get(n+1)));
      exteriorEdges.add(new Edge(exteriorPoints.get(n+1), exteriorPoints.get(n)));
    }
    exteriorEdges.add(new Edge(exteriorPoints.get(exteriorPoints.size() - 1), exteriorPoints.get(0)));
    exteriorEdges.add(new Edge(exteriorPoints.get(0), exteriorPoints.get(exteriorPoints.size() - 1)));
    for (int n = 0; n < exteriorPoints.size(); n++)
      colorMap.put(exteriorPoints.get(n), exteriorColours.get(n));
  }
  
  public void triangulate()
  {
    PointDouble [] pointsDouble = new PointDouble[exteriorPoints.size()];
    exteriorPoints.toArray(pointsDouble);
    triangulate(pointsDouble);
  }
  public void triangulate(PointDouble[] pointsDouble)
  {
    // Convert things to poly2tri data structures
    List<PolygonPoint> polypoints = new ArrayList<PolygonPoint>();
    Map<PolygonPoint, PointDouble> pointMap = new HashMap<PolygonPoint, PointDouble>();
    for (int n = 0; n < pointsDouble.length ; n++)
    {
      polypoints.add(new PolygonPoint(pointsDouble[n].x, pointsDouble[n].y));
      pointMap.put(polypoints.get(n), pointsDouble[n]);
    }
    
    // Triangulate the polygon
    org.poly2tri.geometry.polygon.Polygon poly = new org.poly2tri.geometry.polygon.Polygon(polypoints);
    Poly2Tri.triangulate(poly);

    // Convert back to our data structures for triangles for refinement
    for (DelaunayTriangle tri : poly.getTriangles()) 
    {
      List<PointDouble> triPoints = new ArrayList<PointDouble>(); 
      for (int n = 0; n < 3; n++)
        triPoints.add(pointMap.get(tri.points[n]));
      triangles.add(new Triangle(triPoints));
    }
  }

  boolean isExteriorEdge(Edge edge)
  {
    return exteriorEdges.contains(edge);
  }
  
  public PointDouble splitEdge(Edge edge)
  {
    // Create the new point that will split the edge
    PointDouble pointSplit = new PointDouble(
        (edge.p1.x + edge.p2.x) / 2,
        (edge.p1.y + edge.p2.y) / 2);
    pointSplit.isExterior = false;
    colorMap.put(pointSplit, new ColorDouble(
        (colorMap.get(edge.p1).r + colorMap.get(edge.p2).r) / 2, 
        (colorMap.get(edge.p1).g + colorMap.get(edge.p2).g) / 2, 
        (colorMap.get(edge.p1).b + colorMap.get(edge.p2).b) / 2));
    // Find the triangles that contain the edge being split
    List<Triangle> toSplit = new ArrayList<Triangle>();
    for (Triangle tri : triangles)
    {
      if (tri.containsEdge(edge)) toSplit.add(tri);
    }
    // Split those triangles
    for (Triangle tri : toSplit)
    {
      triangles.addAll(tri.splitAlongEdge(edge, pointSplit));
    }
    // Remove the triangles that we've just split
    triangles.removeAll(toSplit);
    return pointSplit;
  }
  
  public List<Edge> getInteriorEdges()
  {
    List<Edge> edges = new ArrayList<Edge>();
    for (Edge edge : getAllEdges())
    {
      if (isExteriorEdge(edge))
        continue;
      edges.add(edge);
    }
    return edges;
  }
  
  public List<Edge> getAllEdges()
  {
    List<Edge> edges = new ArrayList<Edge>();
    for (Triangle tri : triangles)
    {
      edges.addAll(tri.getEdges());
    }
    return edges;
  }
  
  public void subdivideInterior()
  {
    boolean repeat = true;
    while (repeat)
    {
      repeat = false;
      for (Triangle tri : triangles)
      {
        List<Edge> edges = tri.getEdges();
        // Find interior edges that joins two exterior points
        for (Edge edge: edges)
        {
          if (!edge.p1.isExterior || !edge.p2.isExterior) continue;
          if (isExteriorEdge(edge)) continue;
          splitEdge(edge);
          // Start over if we split an edge because the list of triangles
          // will have been modified
          repeat = true;
          break;
        }
        if (repeat) break;
      }
    }
  }
  
  public List<PointDouble> getAdjacentPoints(final PointDouble p)
  {
    Set<Edge> adjacentEdges = new HashSet<Edge>();
    for (Triangle tri: triangles)
      adjacentEdges.addAll(tri.getAdjacentEdges(p));
    List<PointDouble> adjacentPoints = new ArrayList<PointDouble>();
    for (Edge e: adjacentEdges)
    {
      if (e.p1 == p) adjacentPoints.add(e.p2);
      if (e.p2 == p) adjacentPoints.add(e.p1);
    }
    return adjacentPoints;
  }
  
  public void diffuseConvergeTo(double limit)
  {
    int n = 0;
    for (n = 0; n < 1000; n++)
    {
      double delta = diffuseColors();
      if (delta < limit)
        break;
    }
  }
  
  public double diffuseColors()
  {
    Map<PointDouble, ColorDouble> nextColors = new HashMap<PointDouble, ColorDouble>();
    nextColors.putAll(colorMap);
    for (final PointDouble p: colorMap.keySet())
    {
      if (p.isExterior) continue;
      // get adjacent points and sort them clockwise
      List<PointDouble> adjacentPoints = getAdjacentPoints(p);
      Collections.sort(adjacentPoints, new Comparator<PointDouble>(){
        @Override
        public int compare(PointDouble o1, PointDouble o2)
        {
          double angle1 = -Math.atan2(o1.y - p.y, o1.x - p.x);
          double angle2 = -Math.atan2(o2.y - p.y, o2.x - p.x);
          if (angle2 > angle1) return 1;
          if (angle1 < angle2) return -1;
          return 0;
        }});
      // Run mean value coordinates to get a new color value
      PointDouble [] points = new PointDouble[adjacentPoints.size()];
      ColorDouble [] colors = new ColorDouble[adjacentPoints.size()];
      for (int n = 0; n < adjacentPoints.size(); n++)
      {
        points[n] = adjacentPoints.get(n);
        colors[n] = colorMap.get(adjacentPoints.get(n));
      }
      nextColors.put(p, calculateMVC(p.x, p.y, points, colors));
    }

    // Calculate the change in colors
    double maxDelta = 0;
    for (final PointDouble p: colorMap.keySet())
    {
      ColorDouble oldc = colorMap.get(p);
      ColorDouble newc = nextColors.get(p);
      double delta = oldc.absDiff(newc);
      if (delta > maxDelta) maxDelta = delta;
    }
    colorMap = nextColors;
    return maxDelta;
  }

  public static ColorDouble calculateMVC(double x, double y, PointDouble [] pointsDouble, ColorDouble[] colorsDouble)
  {
    double weights[] = new double[pointsDouble.length];
    for (int n = 0; n < pointsDouble.length; n++)
    {
      PointDouble viminus = (n > 0) ? pointsDouble[n-1] : pointsDouble[pointsDouble.length - 1]; 
      PointDouble vi = pointsDouble[n];
      PointDouble viplus = (n < pointsDouble.length - 1) ? pointsDouble[n+1] : pointsDouble[0];
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
    for (int n = 0; n < pointsDouble.length; n++)
    {
      denom += weights[n];
    }
    ColorDouble c = new ColorDouble();
    for (int n = 0; n < pointsDouble.length; n++)
    {
      double lambda = weights[n] / denom;
      c.r += colorsDouble[n].r * lambda;
      c.g += colorsDouble[n].g * lambda;
      c.b += colorsDouble[n].b * lambda;
    }
    return c;
  }

}
