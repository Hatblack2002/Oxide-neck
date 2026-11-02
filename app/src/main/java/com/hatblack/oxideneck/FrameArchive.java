package com.hatblack.oxideneck;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Saves only explicitly requested screen frames to app-private storage. */
public final class FrameArchive {
    private static final int MAX_FRAMES = 30;
    private FrameArchive() {}

    public static File save(Context context, Image image) throws Exception {
        Image.Plane[] planes = image.getPlanes();
        if (planes == null || planes.length == 0) throw new IllegalStateException("No image plane");
        Image.Plane plane = planes[0];
        int width = image.getWidth(), height = image.getHeight();
        int pixelStride = plane.getPixelStride(), rowStride = plane.getRowStride();
        if (pixelStride < 4 || rowStride < pixelStride * width) throw new IllegalStateException("Unsupported image layout");
        int paddedWidth = rowStride / pixelStride;
        ByteBuffer buffer = plane.getBuffer();
        buffer.rewind();
        Bitmap padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888);
        padded.copyPixelsFromBuffer(buffer);
        Bitmap cropped = Bitmap.createBitmap(padded, 0, 0, width, height);
        File dir = new File(context.getFilesDir(), "frames");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create frame directory");
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(new Date());
        File target = new File(dir, "frame_" + stamp + ".jpg");
        try (FileOutputStream out = new FileOutputStream(target)) {
            if (!cropped.compress(Bitmap.CompressFormat.JPEG, 92, out)) throw new IllegalStateException("JPEG encode failed");
            out.flush();
        } finally {
            if (cropped != padded) cropped.recycle();
            padded.recycle();
        }
        prune(dir);
        return target;
    }

    private static void prune(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg"));
        if (files == null || files.length <= MAX_FRAMES) return;
        java.util.Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
        for (int i = 0; i < files.length - MAX_FRAMES; i++) files[i].delete();
    }
}
