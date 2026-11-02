package com.hatblack.oxideneck;

import android.app.*;
import android.content.*;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.util.Locale;

/** Manual screen-space calibration, frame archive, and profile workspace. */
public final class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST = 701;
    private CalibrationCanvas canvas;
    private TextView status, captureStatus;
    private ImageView framePreview;
    private MeasurementStore measurements;
    private ProfileStore profiles;
    private BroadcastReceiver captureReceiver;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        measurements = new MeasurementStore(this);
        profiles = new ProfileStore(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14, 14, 14, 14);
        root.setBackgroundColor(0xff101820);
        scroll.addView(root);

        TextView heading = new TextView(this);
        heading.setText("OXIDE-NECK / CALIBRATION");
        heading.setTextColor(-1); heading.setTextSize(20); root.addView(heading);
        TextView desc = new TextView(this);
        desc.setText("Manual screen-space measurement. Capture and save frames only when requested. No automatic player recognition or game input.");
        desc.setTextColor(0xffc0ccd6); root.addView(desc);

        canvas = new CalibrationCanvas();
        root.addView(canvas, new LinearLayout.LayoutParams(-1, 430));
        status = new TextView(this); status.setTextColor(-1); root.addView(status);
        row(root, "RESET POINTS", "SAVE SAMPLE", v -> { canvas.reset(); refresh(); }, v -> saveSample());

        row(root, "START CAPTURE", "STOP CAPTURE", v -> requestCapture(),
            v -> stopService(new Intent(this, ScreenCaptureService.class)));
        row(root, "SAVE CURRENT FRAME", "FLOATING GUIDE", v -> requestFrame(), v -> startGuide());
        row(root, "STOP GUIDE", "PROFILES", v -> stopService(new Intent(this, OverlayService.class)), v -> manageProfiles());
        row(root, "SAVE PROFILE", "COMPARE SAMPLES", v -> saveProfile(), v -> compareSamples());
        row(root, "RUN BENCHMARK", "EXPORT HISTORY", v -> benchmark(), v -> exportHistory());

        captureStatus = new TextView(this); captureStatus.setTextColor(0xffc0ccd6);
        captureStatus.setText("No frame saved in this session."); root.addView(captureStatus);
        framePreview = new ImageView(this);
        framePreview.setAdjustViewBounds(true);
        framePreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(framePreview, new LinearLayout.LayoutParams(-1, 220));

        setContentView(scroll);
        refresh();
    }

    private void row(LinearLayout root, String first, String second, View.OnClickListener a, View.OnClickListener b) {
        LinearLayout line = new LinearLayout(this);
        Button left = new Button(this); left.setText(first); left.setOnClickListener(a);
        line.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        Button right = new Button(this); right.setText(second); right.setOnClickListener(b);
        line.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(line);
    }

    @Override protected void onResume() {
        super.onResume();
        if (captureReceiver == null) {
            captureReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
                    String error = intent.getStringExtra("error");
                    String path = intent.getStringExtra(ScreenCaptureService.EXTRA_FRAME_PATH);
                    if (error != null) {
                        captureStatus.setText("Capture error: " + error);
                    } else if (path != null && !path.isEmpty()) {
                        File file = new File(path);
                        if (file.isFile()) {
                            framePreview.setImageBitmap(BitmapFactory.decodeFile(path));
                            captureStatus.setText("Saved local frame: " + file.getName());
                        } else captureStatus.setText("Frame file was not found.");
                    }
                }
            };
            IntentFilter filter = new IntentFilter(ScreenCaptureService.ACTION_FRAME_SAVED);
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(captureReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            else registerReceiver(captureReceiver, filter);
        }
    }

    @Override protected void onPause() {
        if (captureReceiver != null) {
            unregisterReceiver(captureReceiver);
            captureReceiver = null;
        }
        super.onPause();
    }

    private void requestCapture() {
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(), CAPTURE_REQUEST);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != CAPTURE_REQUEST) return;
        if (result != RESULT_OK || data == null) { toast("Screen capture permission was not granted"); return; }
        Intent service = new Intent(this, ScreenCaptureService.class)
            .putExtra(ScreenCaptureService.EXTRA_RESULT, result)
            .putExtra(ScreenCaptureService.EXTRA_DATA, data);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(service); else startService(service);
        toast("Capture session started. Use SAVE CURRENT FRAME to archive a still image.");
    }

    private void requestFrame() {
        Intent request = new Intent(this, ScreenCaptureService.class).setAction(ScreenCaptureService.ACTION_SAVE_FRAME);
        startService(request);
        captureStatus.setText("Waiting for the next screen frame...");
    }

    private void startGuide() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            return;
        }
        CalibrationProfile p = profiles.getActive();
        if (p == null) { toast("Save and activate a profile first"); return; }
        Intent guide = new Intent(this, OverlayService.class)
            .putExtra(OverlayService.EXTRA_X1, (float) p.referenceX)
            .putExtra(OverlayService.EXTRA_Y1, (float) p.referenceY)
            .putExtra(OverlayService.EXTRA_X2, (float) p.horizontalOffset)
            .putExtra(OverlayService.EXTRA_Y2, (float) p.verticalOffset);
        startService(guide);
    }

    private void saveProfile() {
        GeometryEngine.Point[] points = canvas.points();
        if (points.length != 2) { toast("Place two reference points before saving a profile"); return; }
        EditText input = new EditText(this); input.setSingleLine(true); input.setHint("Profile name");
        new AlertDialog.Builder(this).setTitle("Save calibration profile").setView(input)
            .setPositiveButton("SAVE", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) { toast("Profile name is required"); return; }
                int[] location = new int[2]; canvas.getLocationOnScreen(location);
                android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
                double x1 = (location[0] + points[0].x) / Math.max(1, dm.widthPixels);
                double y1 = (location[1] + points[0].y) / Math.max(1, dm.heightPixels);
                double x2 = (location[0] + points[1].x) / Math.max(1, dm.widthPixels);
                double y2 = (location[1] + points[1].y) / Math.max(1, dm.heightPixels);
                try {
                    CalibrationProfile profile = new CalibrationProfile(name, dm.widthPixels, dm.heightPixels,
                        1.0, x1, y1, x2, y2, Double.NaN);
                    profiles.save(profile);
                    profiles.setActive(name);
                    toast("Profile saved and activated: " + name);
                } catch (RuntimeException e) { toast("Profile save failed: " + e.getMessage()); }
            }).setNegativeButton("CANCEL", null).show();
    }

    private void manageProfiles() {
        JSONArray array = profiles.all();
        if (array.length() == 0) { toast("No saved profiles"); return; }
        String[] names = new String[array.length()];
        for (int i = 0; i < array.length(); i++) names[i] = array.optJSONObject(i).optString("name");
        new AlertDialog.Builder(this).setTitle("Select a profile").setItems(names, (dialog, index) -> {
            String name = names[index];
            CalibrationProfile p = profiles.get(name);
            if (p == null) { toast("Profile could not be loaded"); return; }
            String active = profiles.getActiveName();
            CalibrationProfile activeProfile = profiles.getActive();
            String comparison = "";
            if (activeProfile != null && !name.equals(activeProfile.name)) {
                double deltaA = Math.hypot(p.referenceX - activeProfile.referenceX, p.referenceY - activeProfile.referenceY);
                double deltaB = Math.hypot(p.horizontalOffset - activeProfile.horizontalOffset, p.verticalOffset - activeProfile.verticalOffset);
                comparison = String.format(Locale.US, "\nCompared with active %s:\nReference A delta %.5f\nReference B delta %.5f\nResolution ratio %.4f × %.4f",
                    activeProfile.name, deltaA, deltaB, (double) p.screenWidth / activeProfile.screenWidth,
                    (double) p.screenHeight / activeProfile.screenHeight);
            }
            String message = String.format(Locale.US,
                "Name: %s\nResolution: %d × %d\nReference A: %.4f, %.4f\nReference B: %.4f, %.4f\nSensitivity: %.3f\nFOV: %s\nActive: %s%s",
                p.name, p.screenWidth, p.screenHeight, p.referenceX, p.referenceY, p.horizontalOffset,
                p.verticalOffset, p.sensitivity, Double.isNaN(p.fovDegrees) ? "unknown" : p.fovDegrees + "°",
                name.equals(active) ? "yes" : "no", comparison);
            new AlertDialog.Builder(this).setTitle("Calibration profile").setMessage(message)
                .setPositiveButton("ACTIVATE", (d, w) -> { profiles.setActive(name); toast("Active profile: " + name); })
                .setNeutralButton("DELETE", (d, w) -> new AlertDialog.Builder(this)
                    .setTitle("Delete " + name + "?").setMessage("This cannot be undone.")
                    .setPositiveButton("DELETE", (dd, ww) -> { profiles.delete(name); toast("Profile deleted"); })
                    .setNegativeButton("CANCEL", null).show())
                .setNegativeButton("CLOSE", null).show();
        }).setNegativeButton("CLOSE", null).show();
    }

    private void compareSamples() {
        JSONArray samples = new JSONArray(measurements.exportJson());
        if (samples.length() < 2) { toast("At least two saved samples are required"); return; }
        try {
            Measurement first = fromJson(samples.getJSONObject(0));
            Measurement last = fromJson(samples.getJSONObject(samples.length() - 1));
            GeometryEngine.Metrics a = first.metrics(), b = last.metrics();
            double difference = Math.abs(b.distancePx - a.distancePx);
            double percentage = a.distancePx == 0 ? 0 : difference / a.distancePx * 100.0;
            new AlertDialog.Builder(this).setTitle("First vs latest sample")
                .setMessage(String.format(Locale.US, "First distance: %.2f px\nLatest distance: %.2f px\nAbsolute difference: %.2f px\nRelative difference: %.2f%%\nSamples stored: %d",
                    a.distancePx, b.distancePx, difference, percentage, samples.length()))
                .setPositiveButton("OK", null).show();
        } catch (JSONException e) { toast("History data is invalid"); }
    }

    private Measurement fromJson(org.json.JSONObject o) throws JSONException {
        return new Measurement(o.getLong("timestamp"), o.getDouble("x1"), o.getDouble("y1"),
            o.getDouble("x2"), o.getDouble("y2"), o.getDouble("width"), o.getDouble("height"));
    }

    private void exportHistory() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/json");
        share.putExtra(Intent.EXTRA_SUBJECT, "OXIDE-NECK measurement history");
        share.putExtra(Intent.EXTRA_TEXT, measurements.exportJson());
        startActivity(Intent.createChooser(share, "Export measurement history"));
    }

    private void benchmark() {
        PerformanceBenchmark result = PerformanceBenchmark.run(500000);
        toast(String.format(Locale.US, "500k geometry operations: %.0f ops/s; %.1f ms",
            result.operationsPerSecond, result.elapsedNanos / 1e6));
    }

    private void saveSample() {
        GeometryEngine.Point[] points = canvas.points();
        if (points.length != 2) { toast("Select two reference points"); return; }
        try {
            measurements.append(new Measurement(System.currentTimeMillis(), points[0].x, points[0].y,
                points[1].x, points[1].y, canvas.getWidth(), canvas.getHeight()));
            toast("Measurement saved"); refresh();
        } catch (JSONException e) { toast("Measurement save failed"); }
    }

    private void refresh() {
        if (status == null || canvas == null) return;
        GeometryEngine.Metrics m = canvas.metrics();
        status.setText(m == null ? "Tap two points. Stored samples: " + measurements.count()
            : String.format(Locale.US, "ΔX %.1f px | ΔY %.1f px | distance %.1f px | angle %.2f° | normalized %.4f, %.4f | samples %d",
                m.dx, m.dy, m.distancePx, m.angleDegrees, m.normalizedDx, m.normalizedDy, measurements.count()));
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }

    private final class CalibrationCanvas extends View {
        private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private GeometryEngine.Point first, second;
        CalibrationCanvas() { super(MainActivity.this); setBackgroundColor(0xff1d2a35); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            float w = getWidth(), h = getHeight();
            paint.setColor(0xff526575); paint.setStrokeWidth(1);
            for (int i = 1; i < 4; i++) {
                c.drawLine(w * i / 4, 0, w * i / 4, h, paint);
                c.drawLine(0, h * i / 4, w, h * i / 4, paint);
            }
            paint.setColor(0xfff2b84b); paint.setStrokeWidth(2); c.drawLine(w / 2, 0, w / 2, h, paint);
            if (first != null) { paint.setColor(0xff48c6a8); c.drawCircle((float) first.x, (float) first.y, 10, paint); }
            if (second != null) {
                paint.setColor(0xffff7b72); c.drawCircle((float) second.x, (float) second.y, 10, paint);
                paint.setColor(0xfff2b84b); c.drawLine((float) first.x, (float) first.y, (float) second.x, (float) second.y, paint);
            }
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (first == null || second != null) { first = new GeometryEngine.Point(event.getX(), event.getY()); second = null; }
                else second = new GeometryEngine.Point(event.getX(), event.getY());
                invalidate(); refresh(); return true;
            }
            return true;
        }
        void reset() { first = null; second = null; invalidate(); }
        GeometryEngine.Point[] points() {
            return first == null || second == null ? new GeometryEngine.Point[0] : new GeometryEngine.Point[]{first, second};
        }
        GeometryEngine.Metrics metrics() {
            return first == null || second == null ? null : GeometryEngine.between(first, second, getWidth(), getHeight());
        }
    }
}
