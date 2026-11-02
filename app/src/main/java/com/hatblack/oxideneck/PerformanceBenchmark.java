package com.hatblack.oxideneck;

/** Repeatable CPU microbenchmark; not a substitute for thermal/device profiling. */
public final class PerformanceBenchmark {
    public final long elapsedNanos; public final int iterations; public final double operationsPerSecond;
    private PerformanceBenchmark(long n,int i){elapsedNanos=n;iterations=i;operationsPerSecond=i/(n/1_000_000_000.0);}
    public static PerformanceBenchmark run(int iterations){
        if(iterations<1000||iterations>5_000_000)throw new IllegalArgumentException("Iterations outside 1,000..5,000,000");
        GeometryEngine.Point a=new GeometryEngine.Point(120,240),b=new GeometryEngine.Point(480,720);
        long start=System.nanoTime();double sink=0;
        for(int i=0;i<iterations;i++){GeometryEngine.Metrics m=GeometryEngine.between(a,b,1080,2400);sink+=m.distancePx+m.normalizedDx;}
        long elapsed=Math.max(1,System.nanoTime()-start);
        if(sink==0)throw new AssertionError("Benchmark was optimized away");
        return new PerformanceBenchmark(elapsed,iterations);
    }
}
