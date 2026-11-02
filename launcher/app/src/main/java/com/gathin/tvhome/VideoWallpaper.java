package com.gathin.tvhome;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;

/**
 * Edgerunners skin: a theme's {@code wallpaper.mp4} as a silent, looping background behind everything on Home.
 * The decoder only exists while Home is on screen: {@link #stop} releases the player (an app or the screensaver
 * opened) and {@link #start} builds it again and resumes where it was. wallpaper_dim darkens it through the view's
 * alpha over the root's background colour, so no extra full-screen layer is drawn.
 */
final class VideoWallpaper implements TextureView.SurfaceTextureListener {
    static final String FILE_NAME = "wallpaper.mp4";
    private static final String TAG = "TvHome.Video";

    private final File file;
    private final TextureView view;
    private Surface surface;
    private MediaPlayer player;
    /** Home is on screen. */
    private boolean active;
    /** Where the loop was when Home left, to resume there. */
    private int position;

    /** The video wallpaper for this theme, or null when it has no wallpaper.mp4. */
    static VideoWallpaper create(Context context, FrameLayout root, int dimPercent) {
        File dir = Config.dir(context);
        File file = dir == null ? null : new File(dir, FILE_NAME);
        if (file == null || !file.isFile() || file.length() == 0) return null;
        File local = localCopy(context, file, FILE_NAME);
        return local == null ? null : new VideoWallpaper(context, root, local, dimPercent);
    }

    /**
     * The video copied into internal storage, refreshed when the theme's file changes. The theme folder is on
     * FUSE, and the media server reads nothing from a descriptor there ("source has no track") on this firmware.
     */
    static File localCopy(Context context, File source, String name) {
        File copy = new File(context.getNoBackupFilesDir(), name);
        if (copy.length() == source.length() && copy.lastModified() >= source.lastModified()) return copy;
        File tmp = new File(copy.getPath() + ".tmp");
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new java.io.FileOutputStream(tmp)) {
            byte[] buf = new byte[64 * 1024];
            for (int n; (n = in.read(buf)) > 0; ) out.write(buf, 0, n);
        } catch (java.io.IOException e) {
            Log.w(TAG, "cannot copy " + source, e);
            tmp.delete();
            return copy.isFile() ? copy : null;
        }
        return tmp.renameTo(copy) ? copy : null;
    }

    private VideoWallpaper(Context context, FrameLayout root, File file, int dimPercent) {
        this.file = file;
        view = new TextureView(context);
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        view.setFocusable(false);
        view.setAlpha(1f - Math.max(0, Math.min(90, dimPercent)) / 100f);
        view.setSurfaceTextureListener(this);
        root.addView(view, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    void start() {
        active = true;
        open();
    }

    void stop() {
        active = false;
        release();
    }

    private void open() {
        if (!active || surface == null || player != null) return;
        MediaPlayer mp = new MediaPlayer();
        player = mp;
        // Opened here and handed over as a descriptor: the media server cannot open a path inside this app's
        // Android/data folder itself (scoped storage), and fails with MEDIA_ERROR_SYSTEM.
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            mp.setDataSource(in.getFD());
            mp.setSurface(surface);
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            mp.setOnPreparedListener(p -> {
                if (p != player || !active) return;
                if (position > 0) p.seekTo(position);
                p.start();
            });
            mp.setOnErrorListener((p, what, extra) -> {
                Log.w(TAG, "video wallpaper error " + what + "/" + extra);
                if (p == player) release();
                return true;
            });
            mp.prepareAsync();
        } catch (Exception e) {
            Log.w(TAG, "cannot play " + file, e);
            release();
        }
    }

    private void release() {
        MediaPlayer mp = player;
        player = null;
        if (mp == null) return;
        try {
            if (mp.isPlaying()) position = mp.getCurrentPosition();
        } catch (IllegalStateException ignored) {
            // not prepared yet: keep the last position
        }
        mp.release();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        surface = new Surface(texture);
        open();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        release();
        if (surface != null) surface.release();
        surface = null;
        return true;
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {}
    @Override public void onSurfaceTextureUpdated(SurfaceTexture texture) {}
}
