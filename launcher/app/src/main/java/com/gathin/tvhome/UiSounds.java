package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Edgerunners skin: short sounds for moving between tiles and opening an app - the theme's own (sounds/ in the
 * theme folder) or the built-in synthetic blips (res/raw, made by tools/make_sounds.py). On by default; the quick
 * settings panel switches them off ({@link #setEnabled}).
 */
final class UiSounds {
    private static final String PREFS = "edgerunners", KEY = "ui_sounds";
    private static final float VOLUME = 0.6f;

    private final Context context;
    private final SoundPool pool;
    private final int move, open;

    UiSounds(Context context) {
        this.context = context.getApplicationContext();
        pool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        move = load("ui_move", R.raw.ui_move);
        open = load("ui_open", R.raw.ui_open);
    }

    /**
     * The theme's own sound (sounds/<name>.wav, .mp3 or .ogg, pushed by tools\theme.ps1), copied to internal
     * storage like the video wallpaper; the built-in one when the theme has none or it cannot be read.
     */
    private int load(String name, int fallback) {
        java.io.File dir = Config.dir(context);
        if (dir != null) {
            for (String ext : new String[] {"wav", "mp3", "ogg"}) {
                java.io.File file = new java.io.File(dir, ThemeFiles.SOUNDS + "/" + name + "." + ext);
                if (!file.isFile() || file.length() == 0) continue;
                java.io.File local = VideoWallpaper.localCopy(context, file, "sound_" + name + "." + ext);
                if (local != null) return pool.load(local.getPath(), 1);
            }
        }
        return pool.load(context, fallback, 1);
    }

    /** Last move() that played; a second one within MOVE_GAP_MS (the same step reported twice) is dropped. */
    private long lastMove;
    private static final long MOVE_GAP_MS = 40;

    void move() {
        long now = android.os.SystemClock.uptimeMillis();
        if (now - lastMove < MOVE_GAP_MS) return;
        lastMove = now;
        play(move);
    }

    /**
     * Turns off Android's own focus and click sounds for a view and everything under it, so only these play.
     * A view added later needs its own call.
     */
    static void muteTree(android.view.View v) {
        v.setSoundEffectsEnabled(false);
        if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) muteTree(g.getChildAt(i));
        }
    }

    /**
     * Mutes {@code root}'s tree once laid out (views added after this call included) and plays the move sound
     * whenever the focus moves inside its window.
     */
    void attach(android.view.View root) {
        android.view.ViewTreeObserver vto = root.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> muteTree(root));
        vto.addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (oldFocus != null && newFocus != null && root.isShown()) move();
        });
    }

    void open() {
        play(open);
    }

    private void play(int id) {
        if (enabled(context)) pool.play(id, VOLUME, VOLUME, 1, 0, 1f);
    }

    void release() {
        pool.release();
    }

    static boolean enabled(Context context) {
        return prefs(context).getBoolean(KEY, true);
    }

    static void setEnabled(Context context, boolean on) {
        prefs(context).edit().putBoolean(KEY, on).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
