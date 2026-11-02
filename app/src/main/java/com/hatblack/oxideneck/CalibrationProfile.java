package com.hatblack.oxideneck;

/** User-measured calibration values. FOV remains unknown until externally measured. */
public final class CalibrationProfile {
    public final String name;
    public final int screenWidth, screenHeight;
    public final double sensitivity, referenceX, referenceY, horizontalOffset, verticalOffset, fovDegrees;

    public CalibrationProfile(String name, int width, int height, double sensitivity, double x, double y,
                              double x2, double y2, double fovDegrees) {
        if (name == null || name.trim().isEmpty() || width <= 0 || height <= 0 ||
            !Double.isFinite(sensitivity) || sensitivity <= 0 ||
            !unit(x) || !unit(y) || !unit(x2) || !unit(y2)) {
            throw new IllegalArgumentException("Invalid profile name, resolution, sensitivity, or normalized coordinates");
        }
        if (!Double.isNaN(fovDegrees) && (!Double.isFinite(fovDegrees) || fovDegrees <= 0 || fovDegrees >= 180)) {
            throw new IllegalArgumentException("FOV must be unknown (NaN) or between 0 and 180");
        }
        this.name = name.trim();
        this.screenWidth = width;
        this.screenHeight = height;
        this.sensitivity = sensitivity;
        this.referenceX = x;
        this.referenceY = y;
        this.horizontalOffset = x2;
        this.verticalOffset = y2;
        this.fovDegrees = fovDegrees;
    }

    private static boolean unit(double value) {
        return Double.isFinite(value) && value >= 0.0 && value <= 1.0;
    }

    public double scaleX(int currentWidth) { return (double) currentWidth / screenWidth; }
    public double scaleY(int currentHeight) { return (double) currentHeight / screenHeight; }
}
