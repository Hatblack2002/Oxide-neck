package com.hatblack.oxideneck;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;

/** Manual, user-operated screen-space calibration workspace. */
public final class MainActivity extends Activity {
    private CalibrationCanvas canvas;
    private TextView status;
    private MeasurementStore store;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        store=new MeasurementStore(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18,18,18,18);root.setBackgroundColor(0xff101820);
        TextView title=new TextView(this);title.setText("OXIDE-NECK  /  CALIBRATION");
        title.setTextColor(0xffe8edf2);title.setTextSize(19);root.addView(title);
        TextView instructions=new TextView(this);
        instructions.setText("Tap two points on the preview to measure screen-space displacement. No automatic target recognition or input injection.");
        instructions.setTextColor(0xffaab7c4);instructions.setPadding(0,10,0,10);root.addView(instructions);
        canvas=new CalibrationCanvas();root.addView(canvas,new LinearLayout.LayoutParams(-1,0,1));
        status=new TextView(this);status.setTextColor(0xffdce6ee);status.setPadding(0,12,0,12);root.addView(status);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);
        Button reset=new Button(this);reset.setText("RESET");reset.setOnClickListener(v->{canvas.reset();updateStatus();});actions.addView(reset);
        Button save=new Button(this);save.setText("SAVE SAMPLE");save.setOnClickListener(v->saveSample());actions.addView(save);
        Button export=new Button(this);export.setText("HISTORY");export.setOnClickListener(v->Toast.makeText(this,"Saved samples: "+store.count(),Toast.LENGTH_LONG).show());actions.addView(export);
        root.addView(actions);setContentView(root);updateStatus();
    }
    private void updateStatus(){
        if(canvas==null||status==null)return;
        GeometryEngine.Metrics m=canvas.metrics();
        status.setText(m==null?"Select point 1, then point 2.  |  Samples: "+store.count():
            String.format(java.util.Locale.US,"ΔX %.1f px   ΔY %.1f px   Distance %.1f px   Angle %.1f°\nNormalized Δ: %.4f, %.4f   |   Samples: %d",
                m.dx,m.dy,m.distancePx,m.angleDegrees,m.normalizedDx,m.normalizedDy,store.count()));
    }
    private void saveSample(){
        GeometryEngine.Point[] p=canvas.points();
        if(p.length!=2){Toast.makeText(this,"Select two points first",Toast.LENGTH_SHORT).show();return;}
        try{store.append(new Measurement(System.currentTimeMillis(),p[0].x,p[0].y,p[1].x,p[1].y,canvas.getWidth(),canvas.getHeight()));
            Toast.makeText(this,"Measurement saved locally",Toast.LENGTH_SHORT).show();updateStatus();
        }catch(JSONException e){Toast.makeText(this,"Could not save sample",Toast.LENGTH_SHORT).show();}
    }
    private final class CalibrationCanvas extends View {
        private final Paint paint=new Paint(3); private GeometryEngine.Point first,second;
        CalibrationCanvas(){super(MainActivity.this);setBackgroundColor(0xff1d2a35);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
            paint.setColor(0xff526575);paint.setStrokeWidth(1);
            for(int i=1;i<4;i++){c.drawLine(w*i/4,0,w*i/4,h,paint);c.drawLine(0,h*i/4,w,h*i/4,paint);}
            paint.setColor(0xffe7edf2);paint.setTextSize(28);c.drawText("MANUAL REFERENCE VIEW",22,38,paint);
            paint.setColor(0xfff2b84b);paint.setStrokeWidth(3);c.drawLine(w/2,0,w/2,h,paint);
            if(first!=null)drawPoint(c,first,0xff48c6a8);
            if(second!=null){drawPoint(c,second,0xffff7b72);paint.setColor(0xfff2b84b);paint.setStrokeWidth(2);
                c.drawLine((float)first.x,(float)first.y,(float)second.x,(float)second.y,paint);}
        }
        private void drawPoint(Canvas c,GeometryEngine.Point p,int color){paint.setColor(color);paint.setStrokeWidth(3);
            c.drawCircle((float)p.x,(float)p.y,9,paint);c.drawLine((float)p.x-16,(float)p.y,(float)p.x+16,(float)p.y,paint);
            c.drawLine((float)p.x,(float)p.y-16,(float)p.x,(float)p.y+16,paint);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==android.view.MotionEvent.ACTION_DOWN){
            if(first==null||second!=null){first=new GeometryEngine.Point(e.getX(),e.getY());second=null;}
            else second=new GeometryEngine.Point(e.getX(),e.getY());invalidate();updateStatus();return true;}return true;}
        void reset(){first=null;second=null;invalidate();}
        GeometryEngine.Point[] points(){if(first==null||second==null)return new GeometryEngine.Point[0];return new GeometryEngine.Point[]{first,second};}
        GeometryEngine.Metrics metrics(){return first==null||second==null?null:GeometryEngine.between(first,second,getWidth(),getHeight());}
    }
}
