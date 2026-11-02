package com.hatblack.oxideneck;

import org.junit.Test;
import static org.junit.Assert.*;

public class CalibrationProfileTest {
    @Test public void computesResolutionScaling() {
        CalibrationProfile p = new CalibrationProfile("phone", 1000, 2000, 1.0, .25, .25, .75, .75, Double.NaN);
        assertEquals(2.0, p.scaleX(2000), 1e-9);
        assertEquals(.5, p.scaleY(1000), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsCoordinatesOutsideNormalizedViewport() {
        new CalibrationProfile("invalid", 1000, 2000, 1.0, 1.1, .5, .5, .5, Double.NaN);
    }
}
