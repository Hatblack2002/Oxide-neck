package com.hatblack.oxideneck;

import android.app.*;
import android.content.*;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Locale;

/** User-controlled screen calibration workspace. */
public final class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST=701;
    private CalibrationCanvas canvas; private TextView status;
    private MeasurementStore measurements; private ProfileStore profiles;
    @Override public void onCreate(Bundle b){
        super.onCreate(b); measurements=new MeasurementStore(this); profiles=new ProfileStore(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(14,14,14,14);root.setBackgroundColor(0xff101820);
        TextView heading=new TextView(this);heading.setText("OXIDE-NECK / CALIBRATION");heading.setTextColor(-1);heading.setTextSize(20);root.addView(heading);
        TextView desc=new TextView(this);desc.setText("Manual screen-space measurements. Screen capture requires Android consent. Reference guides are positioned by touch.");desc.setTextColor(0xffc0ccd6);root.addView(desc);
        canvas=new CalibrationCanvas();root.addView(canvas,new LinearLayout.LayoutParams(-1,0,1));
        status=new TextView(this);status.setTextColor(-1);root.addView(status);
        row(root,"RESET","SAVE SAMPLE",v->{canvas.reset();refresh();},v->saveSample());
        row(root,"START CAPTURE","STOP CAPTURE",v->requestCapture(),v->stopService(new Intent(this,ScreenCaptureService.class)));
        row(root,"FLOATING GUIDE","STOP GUIDE",v->startGuide(),v->stopService(new Intent(this,OverlayService.class)));
        row(root,"SAVE PROFILE","COMPARE PROFILES",v->saveProfile(),v->compareProfiles());
        row(root,"RUN BENCHMARK","HISTORY",v->benchmark(),v->toast("Stored samples: "+measurements.count()));
        setContentView(root);refresh();
    }
    private void row(LinearLayout root,String a,String b,View.OnClickListener ca,View.OnClickListener cb){
        LinearLayout r=new LinearLayout(this);Button x=new Button(this);x.setText(a);x.setOnClickListener(ca);r.addView(x,new LinearLayout.LayoutParams(0,-2,1));
        Button y=new Button(this);y.setText(b);y.setOnClickListener(cb);r.addView(y,new LinearLayout.LayoutParams(0,-2,1));root.addView(r);
    }
    private void requestCapture(){MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);startActivityForResult(m.createScreenCaptureIntent(),CAPTURE_REQUEST);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);
        if(req==CAPTURE_REQUEST&&result==RESULT_OK&&data!=null){Intent s=new Intent(this,ScreenCaptureService.class).putExtra(ScreenCaptureService.EXTRA_RESULT,result).putExtra(ScreenCaptureService.EXTRA_DATA,data);
            if(Build.VERSION.SDK_INT>=26)startForegroundService(s);else startService(s);toast("Capture session started");}}
    private void startGuide(){if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));return;}
        startService(new Intent(this,OverlayService.class));}
    private void saveProfile(){EditText field=new EditText(this);field.setHint("Profile name");
        new AlertDialog.Builder(this).setTitle("Save profile").setView(field).setPositiveButton("SAVE",(d,w)->{
            String n=field.getText().toString().trim();if(n.isEmpty()){toast("Profile name required");return;}
            android.util.DisplayMetrics dm=getResources().getDisplayMetrics();GeometryEngine.Point[] p=canvas.points();
            double x=p.length==2?p[0].x/Math.max(1,canvas.getWidth()):.5,y=p.length==2?p[0].y/Math.max(1,canvas.getHeight()):.5;
            profiles.save(new CalibrationProfile(n,dm.widthPixels,dm.heightPixels,1,x,y,0,0,Double.NaN));toast("Profile saved");}).setNegativeButton("CANCEL",null).show();}
    private void compareProfiles(){JSONArray a=profiles.all();if(a.length()==0){toast("No profiles saved");return;}
        String[] labels=new String[a.length()];for(int i=0;i<a.length();i++)labels[i]=a.optJSONObject(i).optString("name");
        new AlertDialog.Builder(this).setTitle("Saved profiles").setItems(labels,(d,k)->{CalibrationProfile p=profiles.get(labels[k]);android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
            String msg=String.format(Locale.US,"%s\nStored: %d×%d\nCurrent: %d×%d\nScale: %.4f / %.4f\nSensitivity: %.3f\nFOV: %s",
                p.name,p.screenWidth,p.screenHeight,dm.widthPixels,dm.heightPixels,p.scaleX(dm.widthPixels),p.scaleY(dm.heightPixels),p.sensitivity,
                Double.isNaN(p.fovDegrees)?"unknown":p.fovDegrees+"°");
            new AlertDialog.Builder(this).setTitle("Profile comparison").setMessage(msg).setPositiveButton("OK",null).show();}).setNegativeButton("CLOSE",null).show();}
    private void benchmark(){PerformanceBenchmark b=PerformanceBenchmark.run(500000);toast(String.format(Locale.US,"500k geometry ops: %.0f ops/s; %.1f ms",b.operationsPerSecond,b.elapsedNanos/1e6));}
    private void saveSample(){GeometryEngine.Point[] p=canvas.points();if(p.length!=2){toast("Select two points");return;}
        try{measurements.append(new Measurement(System.currentTimeMillis(),p[0].x,p[0].y,p[1].x,p[1].y,canvas.getWidth(),canvas.getHeight()));toast("Measurement saved");refresh();}
        catch(JSONException e){toast("Save failed");}}
    private void refresh(){if(status==null||canvas==null)return;GeometryEngine.Metrics m=canvas.metrics();
        status.setText(m==null?"Tap two reference points. Samples: "+measurements.count():
            String.format(Locale.US,"ΔX %.1f px | ΔY %.1f px | %.1f px | %.2f° | normalized %.4f, %.4f",
                m.dx,m.dy,m.distancePx,m.angleDegrees,m.normalizedDx,m.normalizedDy));}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private final class CalibrationCanvas extends View{
        private final android.graphics.Paint p=new android.graphics.Paint(3);private GeometryEngine.Point a,b;
        CalibrationCanvas(){super(MainActivity.this);setBackgroundColor(0xff1d2a35);}
        @Override protected void onDraw(android.graphics.Canvas c){float w=getWidth(),h=getHeight();p.setColor(0xff526575);p.setStrokeWidth(1);
            for(int i=1;i<4;i++){c.drawLine(w*i/4,0,w*i/4,h,p);c.drawLine(0,h*i/4,w,h*i/4,p);}
            p.setColor(0xfff2b84b);p.setStrokeWidth(2);c.drawLine(w/2,0,w/2,h,p);
            if(a!=null){p.setColor(0xff48c6a8);c.drawCircle((float)a.x,(float)a.y,10,p);}
            if(b!=null){p.setColor(0xffff7b72);c.drawCircle((float)b.x,(float)b.y,10,p);p.setColor(0xfff2b84b);c.drawLine((float)a.x,(float)a.y,(float)b.x,(float)b.y,p);}}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==0){if(a==null||b!=null){a=new GeometryEngine.Point(e.getX(),e.getY());b=null;}else b=new GeometryEngine.Point(e.getX(),e.getY());invalidate();refresh();return true;}return true;}
        void reset(){a=null;b=null;invalidate();}
        GeometryEngine.Point[] points(){return a==null||b==null?new GeometryEngine.Point[0]:new GeometryEngine.Point[]{a,b};}
        GeometryEngine.Metrics metrics(){return a==null||b==null?null:GeometryEngine.between(a,b,getWidth(),getHeight());}
    }
}
