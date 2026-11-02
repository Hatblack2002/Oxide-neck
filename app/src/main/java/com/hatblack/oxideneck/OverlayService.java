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

/** Non-touchable, pass-through guide positioned from the active manual calibration profile. */
public final class OverlayService extends Service {
    public static final String EXTRA_X1 = "guide_x1", EXTRA_Y1 = "guide_y1";
    public static final String EXTRA_X2 = "guide_x2", EXTRA_Y2 = "guide_y2";
    public static final String ACTION_UPDATE = "com.hatblack.oxideneck.UPDATE_GUIDE";
    private WindowManager manager;
    private GuideView guide;
    private float x1 = .5f, y1 = .44f, x2 = .5f, y2 = .50f;

    @Override public void onCreate() {
        super.onCreate();
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int type = android.os.Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        guide = new GuideView();
        manager.addView(guide, lp);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            x1 = clamp(intent.getFloatExtra(EXTRA_X1, x1)); y1 = clamp(intent.getFloatExtra(EXTRA_Y1, y1));
            x2 = clamp(intent.getFloatExtra(EXTRA_X2, x2)); y2 = clamp(intent.getFloatExtra(EXTRA_Y2, y2));
            if (guide != null) guide.invalidate();
        }
        return START_NOT_STICKY;
    }
    private float clamp(float value) { return Math.max(0f, Math.min(1f, value)); }
    @Override public void onDestroy() { if (guide != null) { manager.removeView(guide); guide = null; } super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }

    private final class GuideView extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        GuideView() { super(OverlayService.this); setLayerType(View.LAYER_TYPE_HARDWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            paint.setColor(0xbff2b84b); paint.setStrokeWidth(2f);
            canvas.drawLine(x1 * w, 0, x1 * w, h, paint);
            paint.setColor(0xff48c6a8); canvas.drawCircle(x1 * w, y1 * h, 10f, paint);
            paint.setColor(0xffff7b72); canvas.drawCircle(x2 * w, y2 * h, 10f, paint);
            paint.setColor(0xfff2b84b); canvas.drawLine(x1 * w, y1 * h, x2 * w, y2 * h, paint);
        }
    }
}
