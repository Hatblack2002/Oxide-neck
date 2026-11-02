package com.hatblack.oxideneck;
import org.junit.Test;
import static org.junit.Assert.*;
public class GeometryEngineTest {
 @Test public void computesScreenSpaceDistanceAndNormalization(){
  GeometryEngine.Metrics m=GeometryEngine.between(new GeometryEngine.Point(10,20),new GeometryEngine.Point(40,60),100,200);
  assertEquals(30,m.dx,1e-9);assertEquals(40,m.dy,1e-9);assertEquals(50,m.distancePx,1e-9);
  assertEquals(.3,m.normalizedDx,1e-9);assertEquals(.2,m.normalizedDy,1e-9);
 }
 @Test public void stabilizerReturnsMedian(){
  GeometryEngine.Stabilizer s=new GeometryEngine.Stabilizer(3);
  s.add(new GeometryEngine.Point(1,1));s.add(new GeometryEngine.Point(100,100));
  GeometryEngine.Point p=s.add(new GeometryEngine.Point(3,3));
  assertEquals(3,p.x,0);assertEquals(3,p.y,0);
 }
 @Test public void calibrationErrorIsPercent(){assertEquals(10,GeometryEngine.calibrationError(100,110),1e-9);}
 @Test public void rejectsInvalidViewport(){
  try{GeometryEngine.between(new GeometryEngine.Point(0,0),new GeometryEngine.Point(1,1),0,10);fail();}
  catch(IllegalArgumentException expected){}
 }
}
