package com.hatblack.oxideneck;

import org.junit.Test;
import static org.junit.Assert.*;

public class GesturePlanTest {
    @Test public void computesBoundedGestureVelocity() {
        GesturePlan plan = new GesturePlan(0, 0, 100, 0, 500);
        assertEquals(200.0, plan.velocityPxPerSecond(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsLongGesture() {
        new GesturePlan(0, 0, 1, 1, 5001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZeroDuration() {
        new GesturePlan(0, 0, 1, 1, 0);
    }
}
