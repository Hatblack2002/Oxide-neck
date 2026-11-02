package com.hatblack.oxideneck;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

/** Consent-based local screen capture. Frames are saved only after an explicit user request. */
public final class ScreenCaptureService extends Service {
    public static final String EXTRA_RESULT = "resultCode";
    public static final String EXTRA_DATA = "projectionData";
    public static final String ACTION_SAVE_FRAME = "com.hatblack.oxideneck.SAVE_FRAME";
    public static final String ACTION_FRAME_SAVED = "com.hatblack.oxideneck.FRAME_SAVED";
    public static final String EXTRA_FRAME_PATH = "framePath";

    private MediaProjection projection;
    private VirtualDisplay display;
    private ImageReader reader;
    private HandlerThread captureThread;
    private Handler captureHandler;
    private volatile boolean saveNextFrame;

    @Override public void onCreate() {
        super.onCreate();
        Notification n = notification();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(71, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else startForeground(71, n);
        captureThread = new HandlerThread("oxide-frame-capture");
        captureThread.start();
        captureHandler = new Handler(captureThread.getLooper());
    }

    private Notification notification() {
        String channel = "oxide_capture";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(channel, "Screen capture", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            return new Notification.Builder(this, channel).setContentTitle("OXIDE-NECK capture active")
                .setContentText("Local capture; save frames manually").setSmallIcon(android.R.drawable.ic_menu_camera).build();
        }
        return new Notification.Builder(this).setContentTitle("OXIDE-NECK capture active")
            .setSmallIcon(android.R.drawable.ic_menu_camera).build();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { stopSelf(); return START_NOT_STICKY; }
        if (ACTION_SAVE_FRAME.equals(intent.getAction())) {
            if (projection == null) broadcastError("Capture is not active");
            else saveNextFrame = true;
            return START_NOT_STICKY;
        }
        int result = intent.getIntExtra(EXTRA_RESULT, Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra(EXTRA_DATA);
        if (result != Activity.RESULT_OK || data == null) { stopSelf(); return START_NOT_STICKY; }
        try {
            MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            projection = manager.getMediaProjection(result, data);
            projection.registerCallback(new MediaProjection.Callback() {
                @Override public void onStop() { stopSelf(); }
            }, captureHandler);
            android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
            reader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, android.graphics.PixelFormat.RGBA_8888, 3);
            reader.setOnImageAvailableListener(source -> {
                Image frame = null;
                try {
                    frame = source.acquireLatestImage();
                    if (frame != null && saveNextFrame) {
                        saveNextFrame = false;
                        java.io.File saved = FrameArchive.save(this, frame);
                        Intent event = new Intent(ACTION_FRAME_SAVED).setPackage(getPackageName())
                            .putExtra(EXTRA_FRAME_PATH, saved.getAbsolutePath());
                        sendBroadcast(event);
                    }
                } catch (Exception e) {
                    saveNextFrame = false;
                    broadcastError(e.getMessage() == null ? "Frame save failed" : e.getMessage());
                } finally {
                    if (frame != null) frame.close();
                }
            }, captureHandler);
            display = projection.createVirtualDisplay("OxideNeckCapture", dm.widthPixels, dm.heightPixels, dm.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.getSurface(), null, captureHandler);
            if (display == null) throw new IllegalStateException("Virtual display creation failed");
        } catch (RuntimeException e) {
            broadcastError(e.getMessage() == null ? "Capture initialization failed" : e.getMessage());
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void broadcastError(String message) {
        sendBroadcast(new Intent(ACTION_FRAME_SAVED).setPackage(getPackageName()).putExtra(EXTRA_FRAME_PATH, "").putExtra("error", message));
    }

    @Override public void onDestroy() {
        if (display != null) { display.release(); display = null; }
        if (reader != null) { reader.setOnImageAvailableListener(null, null); reader.close(); reader = null; }
        if (projection != null) { projection.stop(); projection = null; }
        if (captureThread != null) { captureThread.quitSafely(); captureThread = null; }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
