package com.hatblack.oxideneck;

/** Describes a user-requested gesture for measurement; it never injects input. */
public final class GesturePlan {
    public final double startX,startY,endX,endY;
    public final long durationMillis;
    public GesturePlan(double sx,double sy,double ex,double ey,long duration) {
        if(!Double.isFinite(sx)||!Double.isFinite(sy)||!Double.isFinite(ex)||!Double.isFinite(ey))
            throw new IllegalArgumentException("Gesture coordinates must be finite");
        if(duration<1 || duration>5000) throw new IllegalArgumentException("Duration must be 1..5000 ms");
        startX=sx;startY=sy;endX=ex;endY=ey;durationMillis=duration;
    }
    public double pathLengthPx(){return Math.hypot(endX-startX,endY-startY);}
    public double velocityPxPerSecond(){return pathLengthPx()*1000.0/durationMillis;}
}
