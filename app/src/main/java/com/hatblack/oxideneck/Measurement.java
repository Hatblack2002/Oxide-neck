package com.hatblack.oxideneck;

import org.json.JSONException;
import org.json.JSONObject;

/** Persistable screen-space sample. Timestamp is monotonic-independent wall time for audit display. */
public final class Measurement {
    public final long timestamp;
    public final double x1,y1,x2,y2,width,height;
    public Measurement(long timestamp,double x1,double y1,double x2,double y2,double width,double height) {
        if(!Double.isFinite(width) || !Double.isFinite(height) || width<=0 || height<=0 ||\n            !Double.isFinite(x1) || !Double.isFinite(y1) || !Double.isFinite(x2) || !Double.isFinite(y2))\n            throw new IllegalArgumentException("Invalid measurement coordinates or viewport");
        this.timestamp=timestamp;this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;this.width=width;this.height=height;
    }
    public GeometryEngine.Metrics metrics(){
        return GeometryEngine.between(new GeometryEngine.Point(x1,y1),new GeometryEngine.Point(x2,y2),width,height);
    }
    public JSONObject toJson() throws JSONException {
        JSONObject o=new JSONObject();o.put("timestamp",timestamp);o.put("x1",x1);o.put("y1",y1);
        o.put("x2",x2);o.put("y2",y2);o.put("width",width);o.put("height",height);return o;
    }
}
