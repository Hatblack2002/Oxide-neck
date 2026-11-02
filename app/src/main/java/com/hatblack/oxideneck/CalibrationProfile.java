package com.hatblack.oxideneck;

/** User-measured calibration values. FOV remains unknown until externally measured. */
public final class CalibrationProfile {
    public final String name;
    public final int screenWidth, screenHeight;
    public final double sensitivity, referenceX, referenceY, horizontalOffset, verticalOffset, fovDegrees;
    public CalibrationProfile(String name,int width,int height,double sensitivity,double x,double y,
                              double horizontalOffset,double verticalOffset,double fovDegrees) {
        if(name==null || name.trim().isEmpty() || width<=0 || height<=0 || !Double.isFinite(sensitivity) || sensitivity<=0 ||\n            !unit(x) || !unit(y) || !unit(horizontalOffset) || !unit(verticalOffset))
            throw new IllegalArgumentException("Profile name, resolution and sensitivity are required");
        if(!Double.isNaN(fovDegrees) && (fovDegrees<=0 || fovDegrees>=180))
            throw new IllegalArgumentException("FOV must be unknown (NaN) or between 0 and 180");
        this.name=name;screenWidth=width;screenHeight=height;this.sensitivity=sensitivity;
        referenceX=x;referenceY=y;this.horizontalOffset=horizontalOffset;this.verticalOffset=verticalOffset;
        this.fovDegrees=fovDegrees;
    }
    private static boolean unit(double value) { return Double.isFinite(value) && value >= 0.0 && value <= 1.0; }\n    public double scaleX(int currentWidth){return (double)currentWidth/screenWidth;}
    public double scaleY(int currentHeight){return (double)currentHeight/screenHeight;}
}
