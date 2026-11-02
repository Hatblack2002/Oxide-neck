package com.hatblack.oxideneck;

import android.app.*;
import android.content.*;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;

/** Explicitly consented local capture. Captured frames are not uploaded or used for player recognition. */
public final class ScreenCaptureService extends Service {
    public static final String EXTRA_RESULT="resultCode", EXTRA_DATA="projectionData";
    private MediaProjection projection; private VirtualDisplay display; private ImageReader reader;
    @Override public void onCreate(){super.onCreate();Notification n=notification();
        if(Build.VERSION.SDK_INT>=29)startForeground(71,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        else startForeground(71,n);
    }
    private Notification notification(){
        String channel="oxide_capture";
        if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(channel,"Screen capture",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}
        if(Build.VERSION.SDK_INT>=26)return new Notification.Builder(this,channel).setContentTitle("OXIDE-NECK capture active")
            .setContentText("Local screen measurement session").setSmallIcon(android.R.drawable.ic_menu_camera).build();
        return new Notification.Builder(this).setContentTitle("OXIDE-NECK capture active").setSmallIcon(android.R.drawable.ic_menu_camera).build();
    }
    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i==null){stopSelf();return START_NOT_STICKY;}
        int result=i.getIntExtra(EXTRA_RESULT,Activity.RESULT_CANCELED);
        Intent data=i.getParcelableExtra(EXTRA_DATA);
        if(result!=Activity.RESULT_OK||data==null){stopSelf();return START_NOT_STICKY;}
        MediaProjectionManager m=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        projection=m.getMediaProjection(result,data);
        projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}},null);
        android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
        reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,android.graphics.PixelFormat.RGBA_8888,2);
        reader.setOnImageAvailableListener(source->{android.media.Image frame=source.acquireLatestImage();if(frame!=null)frame.close();},null);
        display=projection.createVirtualDisplay("OxideNeckCapture",dm.widthPixels,dm.heightPixels,dm.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,null);
        return START_NOT_STICKY;
    }
    @Override public void onDestroy(){if(display!=null)display.release();if(reader!=null)reader.close();if(projection!=null)projection.stop();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
