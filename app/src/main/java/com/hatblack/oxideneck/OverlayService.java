package com.hatblack.oxideneck;

import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** Non-interactive, pass-through visual guide; game touch input is never intercepted. */
public final class OverlayService extends Service {
    private WindowManager manager; private GuideView guide;
    @Override public void onCreate(){super.onCreate();manager=(WindowManager)getSystemService(WINDOW_SERVICE);
        int type=android.os.Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.START;guide=new GuideView();manager.addView(guide,lp);
    }
    @Override public int onStartCommand(Intent i,int flags,int id){return START_NOT_STICKY;}
    @Override public void onDestroy(){if(guide!=null){manager.removeView(guide);guide=null;}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
    private final class GuideView extends View{
        final Paint p=new Paint(3);
        GuideView(){super(OverlayService.this);setLayerType(View.LAYER_TYPE_HARDWARE,null);}
        @Override protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(0xbff2b84b);p.setStrokeWidth(2);
            c.drawLine(w*.5f,0,w*.5f,h,p);p.setColor(0xff48c6a8);c.drawCircle(w*.5f,h*.44f,10,p);
            p.setColor(0xffff7b72);c.drawCircle(w*.5f,h*.50f,10,p);
        }
    }
}
