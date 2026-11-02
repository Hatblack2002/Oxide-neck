package com.hatblack.oxideneck;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeometryEngineTest {
    @Test public void computesDistanceAndNormalizedDelta() {
        GeometryEngine.Metrics m = GeometryEngine.between(new GeometryEngine.Point(0, 0),
            new GeometryEngine.Point(3, 4), 300, 400);
        assertEquals(5.0, m.distancePx, 1e-9);
        assertEquals(.01, m.normalizedDx, 1e-9);
        assertEquals(.01, m.normalizedDy, 1e-9);
        assertEquals(53.130102, m.angleDegrees, 1e-5);
    }
    @Test public void calibrationErrorIsPercentage() {
        assertEquals(10.0, GeometryEngine.calibrationError(100, 110), 1e-9);
    }
    @Test public void stabilizerReturnsMedian() {
        GeometryEngine.Stabilizer s = new GeometryEngine.Stabilizer(3);
        s.add(new GeometryEngine.Point(1, 1));
        s.add(new GeometryEngine.Point(100, 100));
        GeometryEngine.Point p = s.add(new GeometryEngine.Point(3, 3));
        assertEquals(3.0, p.x, 1e-9);
        assertEquals(3.0, p.y, 1e-9);
    }
    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroViewport() {
        GeometryEngine.between(new GeometryEngine.Point(0, 0), new GeometryEngine.Point(1, 1), 0, 10);
    }
}
