package com.hatblack.oxideneck;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/** User-positioned measurement guides. Does not recognize or follow game characters. */
public final class OverlayService extends Service {
    private WindowManager wm; private GuideView guide; private WindowManager.LayoutParams params;
    @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        int type=android.os.Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        params=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,
            type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;guide=new GuideView();wm.addView(guide,params);
    }
    @Override public int onStartCommand(Intent i,int flags,int id){return START_NOT_STICKY;}
    @Override public void onDestroy(){if(guide!=null){wm.removeView(guide);guide=null;}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
    private final class GuideView extends View{
        final Paint p=new Paint(3);float x1=0.5f,y1=0.42f,x2=0.5f,y2=0.50f;int selected=0;
        GuideView(){super(OverlayService.this);setLayerType(View.LAYER_TYPE_HARDWARE,null);}
        @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(0x99f2b84b);p.setStrokeWidth(2);
            c.drawLine(w*x1,0,w*x1,h,p);p.setColor(0xff48c6a8);c.drawCircle(w*x1,h*y1,12,p);
            p.setColor(0xffff7b72);c.drawCircle(w*x2,h*y2,12,p);p.setColor(0xfff2b84b);c.drawLine(w*x1,h*y1,w*x2,h*y2,p);
            p.setTextSize(26);c.drawText("OXIDE-NECK • drag markers • close from app",32,54,p);
        }
        @Override public boolean onTouchEvent(MotionEvent e){float nx=e.getX()/Math.max(1,getWidth()),ny=e.getY()/Math.max(1,getHeight());
            if(e.getAction()==MotionEvent.ACTION_DOWN){selected=Math.hypot(nx-x1,ny-y1)<Math.hypot(nx-x2,ny-y2)?1:2;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){if(selected==1){x1=nx;y1=ny;}else{x2=nx;y2=ny;}invalidate();return true;}return true;}
    }
}
