package com.hatblack.oxideneck;

import java.util.ArrayDeque;
import java.util.Deque;

/** Screen-space geometry only. Results are pixels/normalized units, never inferred metres. */
public final class GeometryEngine {
    private GeometryEngine() {}
    public static final class Point {
        public final double x, y;
        public Point(double x, double y) {
            if (!Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Coordinates must be finite");
            this.x=x; this.y=y;
        }
    }
    public static final class Metrics {
        public final double dx, dy, distancePx, angleDegrees, normalizedDx, normalizedDy;
        private Metrics(double dx,double dy,double w,double h) {
            this.dx=dx; this.dy=dy; distancePx=Math.hypot(dx,dy);
            angleDegrees=Math.toDegrees(Math.atan2(dy,dx));
            normalizedDx=dx/w; normalizedDy=dy/h;
        }
    }
    public static Metrics between(Point a, Point b, double width, double height) {
        if (a==null || b==null || width<=0 || height<=0) throw new IllegalArgumentException("Invalid geometry input");
        return new Metrics(b.x-a.x,b.y-a.y,width,height);
    }
    /** Median filter for noisy manually sampled reference points. */
    public static final class Stabilizer {
        private final int capacity;
        private final Deque<Point> samples=new ArrayDeque<>();
        public Stabilizer(int capacity) {
            if(capacity<1 || capacity>31) throw new IllegalArgumentException("capacity must be 1..31");
            this.capacity=capacity;
        }
        public Point add(Point p) {
            samples.addLast(p); while(samples.size()>capacity) samples.removeFirst();
            double[] xs=new double[samples.size()], ys=new double[samples.size()]; int i=0;
            for(Point s:samples){xs[i]=s.x;ys[i]=s.y;i++;}
            java.util.Arrays.sort(xs); java.util.Arrays.sort(ys);
            return new Point(xs[xs.length/2],ys[ys.length/2]);
        }
        public void reset(){samples.clear();}
    }
    public static double calibrationError(double expectedPx,double observedPx) {
        if(expectedPx<=0 || observedPx<0) throw new IllegalArgumentException("Distances must be non-negative and expected > 0");
        return Math.abs(observedPx-expectedPx)/expectedPx*100.0;
    }
    public static double sensitivityRatio(double referenceDelta,double observedDelta) {
        if(referenceDelta==0) throw new IllegalArgumentException("Reference delta cannot be zero");
        return observedDelta/referenceDelta;
    }
}
