package org.programmingbasics.my2iu.gradients.client;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.util.PolygonGenerator;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GradientsGWT implements EntryPoint
{
  /**
   * This is the entry point method.
   */
  public void onModuleLoad()
  {
//    Polygon poly = PolygonGenerator.RandomCircleSweep2( 50, 50000 );
//    Poly2Tri.triangulate(poly);
    
//    TriangulationProcess process = new TriangulationProcess();
//    process.triangulate( poly );
    
    new Main().go();
  }
}
