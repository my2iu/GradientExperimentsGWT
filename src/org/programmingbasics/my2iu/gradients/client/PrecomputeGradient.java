package org.programmingbasics.my2iu.gradients.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.programmingbasics.my2iu.gradients.geom.ColorDouble;
import org.programmingbasics.my2iu.gradients.geom.Edge;
import org.programmingbasics.my2iu.gradients.geom.PointDouble;
import org.programmingbasics.my2iu.gradients.geom.Polygon;
import org.programmingbasics.my2iu.gradients.geom.Triangle;

import elemental.client.Browser;
import elemental.dom.TimeoutHandler;
import elemental.html.Window;

public class PrecomputeGradient
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
  public PrecomputeGradient(GLDrawer drawer, boolean doColorTest, boolean bilinearTest)
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
  
  Polygon polyToRefine;
  
  void go()
  {
    // Triangulate the polygon
    List<PointDouble> points = new ArrayList<PointDouble>();
    List<ColorDouble> colors = new ArrayList<ColorDouble>();
//    Map<PointDouble, ColorDouble> colorMap = new HashMap<PointDouble, ColorDouble>();
    for (int n = 0; n < pointsDouble.length - 1; n++)
    {
//      colorMap.put(pointsDouble[n], colorsDouble[n]);
      points.add(pointsDouble[n]);
      colors.add(colorsDouble[n]);
    }
    final Polygon poly = new Polygon(points, colors);
    poly.triangulate();
    poly.subdivideInterior();
    
//    drawTriangulation(poly, true);

    poly.diffuseConvergeTo(DIFFUSE_CONVERGE_LIMIT);
    drawTriangulation(poly, true);
    
    polyToRefine = poly;

    Window win = Browser.getWindow();
    timerId = win.setInterval(new TimeoutHandler() {
      @Override public void onTimeoutHandler() {
        refineGradient();
      }}, 10);

  }
  
  static final double DIFFUSE_CONVERGE_LIMIT = 0.001; 
  static final double REFINE_LIMIT = 0.03; 

  void refineGradient()
  {
    Polygon poly = polyToRefine;
    
    // Arbitrarily sort the interior edges of the polygon from longest to shortest.
    // (This will hopefully reduce the chance of getting long skinny triangles)
    List<Edge> interiorEdges = poly.getInteriorEdges();
    Collections.sort(interiorEdges, new Comparator<Edge>() {
      @Override
      public int compare(Edge o1, Edge o2)
      {
        double diff = o2.length() - o1.length();
        if (diff > 0)
          return 1;
        if (diff < 0)
          return -1;
        return 0;
      }});
    
    // Go through the edges and try subdividing the triangle there and 
    // seeing the effect on the gradient,
    for (Edge edge : interiorEdges)
    {
      Polygon copy = poly.copy();
      PointDouble newPoint = copy.splitEdge(edge);
      ColorDouble oldColor = copy.colorMap.get(newPoint);
      copy.diffuseConvergeTo(DIFFUSE_CONVERGE_LIMIT);
      ColorDouble newColor = copy.colorMap.get(newPoint);
      
      // Compare the color differences in the old and new polygon
      double deltaMax = 0;
      for (Map.Entry<PointDouble, ColorDouble> oldEntry : poly.colorMap.entrySet())
      {
        deltaMax = Math.max(deltaMax, copy.colorMap.get(oldEntry.getKey()).absDiff(oldEntry.getValue()));
      }
      double newDelta = newColor.absDiff(oldColor);
      if (Math.max(newDelta, deltaMax) > REFINE_LIMIT)
      {
        Browser.getWindow().getConsole().log("Refining new-point-delta:" + newDelta + " other-change-max:" + deltaMax);
        poly = copy;
        break;
      }
    }
    if (poly == polyToRefine)
    {
      if (doColorTest)
      {
        for (Map.Entry<PointDouble, ColorDouble> entry : poly.colorMap.entrySet())
        {
          if (entry.getValue().r < 0.09)
            entry.setValue(new ColorDouble(1, 0, 0));
        }
      }
      Browser.getWindow().getConsole().log("Done");
      Browser.getWindow().clearInterval(timerId);
      polyToRefine = poly;
      drawTriangulation(polyToRefine, true);
    } 
    else
    {
      polyToRefine = poly;
      drawTriangulation(polyToRefine, true);
    }
  }
  
  void drawTriangulation(Polygon poly, boolean withOutline)
  {
    // Clear screen
    bitmap = new BitmapDouble(2, 2);
    drawer.drawFullTexture(bitmap);

    Map<PointDouble, ColorDouble> colorMap = poly.colorMap;
    // Draw the polygon triangle gradients
    for (Triangle tri : poly.triangles) 
    {
      List<PointDouble> triPoints = new ArrayList<PointDouble>(); 
      List<ColorDouble> triColors = new ArrayList<ColorDouble>();
      for (int n = 0; n < 3; n++)
      {
        triPoints.add(tri.points.get(n));
        ColorDouble c = colorMap.get(tri.points.get(n));
//        if (c == null)
//          c = calculateMVC(tri.points.get(n).x, tri.points.get(n).y);
        triColors.add(c);
      }
      drawer.drawGradientTriangle(triPoints, triColors);
    }
    
    if (withOutline)
    {
      // Draw the polygon triangulation outlines
      for (Triangle tri : poly.triangles) 
      {
        List<PointDouble> triPoints = new ArrayList<PointDouble>(); 
        for (int n = 0; n < 3; n++)
        {
          triPoints.add(new PointDouble(tri.points.get(n).x, tri.points.get(n).y));
        }
        triPoints.add(new PointDouble(tri.points.get(0).x, tri.points.get(0).y));
        drawer.drawPolygon(triPoints);
      }
    }
  }
}
