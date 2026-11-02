package com.gathin.tvhome;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Edgerunners skin: what Spotify (or any other app with a media session) is playing, as a Cyberpunk menu panel under
 * the top bar - the cover, the title in the accent, the artist in cyan, and previous / play-pause / next. It reads the
 * sessions through the GuardRevival notification listener, which must be allowed
 * ({@code cmd notification allow_listener com.gathin.tvhome/.GuardRevival}); without that it stays hidden. It listens
 * only between {@link #start} and {@link #stop} (Home on screen), and polls nothing.
 */
final class NowPlayingPanel extends MediaController.Callback
        implements MediaSessionManager.OnActiveSessionsChangedListener {

    /** The app the idle panel offers when nothing plays. */
    private static final String IDLE_APP = "com.spotify.tv.android";

    private final Activity activity;
    private final float density;
    private final int accent;
    private final MediaSessionManager sessions;
    private final ComponentName listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    /** The top-bar buttons whose Down leads here while the panel shows, and back to the grid while it does not. */
    private final View[] above;
    private final int gridId;

    private final LinearLayout panel;
    private final ImageView art;
    private final TextView source;
    private final TextView title;
    private final TextView artist;
    private final ImageView prev;
    private final ImageView playPause;
    private final ImageView next;

    private MediaController controller;
    private boolean listening;
    /** The song's progress: bar, time line, their row, and the once-a-second tick while playing. */
    private final SegmentBar bar;
    private final TextView time;
    private final View progressRow;
    private final Runnable progressTick = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            PlaybackState s = controller == null ? null : controller.getPlaybackState();
            if (listening && isPlaying(s)) handler.postDelayed(this, 1000);
        }
    };

    NowPlayingPanel(Activity activity, FrameLayout root, int accent, View[] above, int gridId, Runnable toDock) {
        this.activity = activity;
        this.density = activity.getResources().getDisplayMetrics().density;
        this.accent = accent;
        this.above = above;
        this.gridId = gridId;
        sessions = activity.getSystemService(MediaSessionManager.class);
        listener = new ComponentName(activity, GuardRevival.class);

        panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(10);
        panel.setPadding(pad, pad, dp(14), pad);
        // A cut-corner panel with a yellow bar down the left edge, as on the selected entry of a Cyberpunk menu.
        panel.setBackground(new CutCornerDrawable(0xA6082833, 0x8000F0FF, Math.max(1, Math.round(density)),
                dp(14), accent, dp(3)));
        panel.setVisibility(View.GONE);

        art = new ImageView(activity);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);
        art.setBackgroundColor(0xFF061418);
        // A visor around the cover: four cyan corner brackets just inside its edges.
        int inset = dp(5);
        art.setPadding(inset, inset, inset, inset);
        art.setForeground(new VisorDrawable(NeonGlitchView.NEON_CYAN, dp(14), dp(2)));
        panel.addView(art, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.setMarginStart(dp(14));
        panel.addView(column, clp);

        source = text(11, accent, R.string.font_medium);
        source.setLetterSpacing(0.18f);
        source.setAllCaps(true);
        title = text(20, 0xFFFFFFFF, R.string.font_medium);
        artist = text(15, NeonGlitchView.NEON_CYAN, R.string.font_regular);
        column.addView(source);
        column.addView(title);
        column.addView(artist);

        // Progress: a segmented cyan bar and "1:23 / 3:45", ticking once a second only while something plays.
        LinearLayout progressRow = new LinearLayout(activity);
        progressRow.setOrientation(LinearLayout.HORIZONTAL);
        progressRow.setGravity(Gravity.CENTER_VERTICAL);
        bar = new SegmentBar(activity, density);
        progressRow.addView(bar, new LinearLayout.LayoutParams(dp(90), dp(6)));   // with the time, as wide as the buttons
        time = text(12, 0xCC00F0FF, R.string.font_medium);
        time.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.setMarginStart(dp(8));
        progressRow.addView(time, tlp);
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        plp.topMargin = dp(6);
        column.addView(progressRow, plp);
        this.progressRow = progressRow;

        LinearLayout controls = new LinearLayout(activity);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = dp(6);
        column.addView(controls, rlp);
        prev = button(R.drawable.ic_np_prev, R.string.np_previous);
        playPause = button(R.drawable.ic_np_play, R.string.np_play_pause);
        next = button(R.drawable.ic_np_next, R.string.np_next);
        controls.addView(prev, buttonParams(false));
        controls.addView(playPause, buttonParams(true));
        controls.addView(next, buttonParams(true));

        prev.setOnClickListener(v -> { if (controller != null) controller.getTransportControls().skipToPrevious(); });
        next.setOnClickListener(v -> { if (controller != null) controller.getTransportControls().skipToNext(); });
        playPause.setOnClickListener(v -> {
            if (controller == null) {
                // Nothing playing: play-pause opens Spotify.
                android.content.Intent open = activity.getPackageManager().getLeanbackLaunchIntentForPackage(IDLE_APP);
                if (open != null) activity.startActivity(open);
                return;
            }
            if (isPlaying(controller.getPlaybackState())) controller.getTransportControls().pause();
            else controller.getTransportControls().play();
        });
        // Hold OK on play-pause: open the app that is playing.
        playPause.setOnLongClickListener(v -> {
            PendingIntent open = controller == null ? null : controller.getSessionActivity();
            if (open == null) return false;
            try {
                open.send();
                return true;
            } catch (PendingIntent.CanceledException e) {
                return false;
            }
        });
        // Deterministic D-pad: Left and Right stop at the row's ends, Up goes back to the top bar, Down to the dock.
        prev.setNextFocusLeftId(prev.getId());
        next.setNextFocusRightId(next.getId());
        for (View b : new View[] {prev, playPause, next}) {
            b.setNextFocusUpId(above[above.length - 1].getId());
            b.setNextFocusDownId(gridId);
            // Down returns to the dock tile Up came from, as the top bar's buttons do.
            b.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode != android.view.KeyEvent.KEYCODE_DPAD_DOWN) return false;
                if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) toDock.run();
                return true;
            });
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        lp.topMargin = dp(110);
        lp.setMarginEnd(dp(56));
        root.addView(panel, lp);
        // Keep the panel 12dp under the top bar's buttons wherever the header's size puts them.
        View topButton = above[above.length - 1];
        topButton.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            int[] at = new int[2], origin = new int[2];
            v.getLocationInWindow(at);
            root.getLocationInWindow(origin);
            int top = at[1] - origin[1] + v.getHeight() + dp(12);
            if (top > dp(40) && top != lp.topMargin) {
                lp.topMargin = top;
                panel.setLayoutParams(lp);
            }
        });
        showIdle();
    }

    /** Home is on screen: follow the active sessions. Does nothing until the listener is allowed. */
    void start() {
        panel.setVisibility(View.VISIBLE);
        if (listening || sessions == null) return;
        try {
            sessions.addOnActiveSessionsChangedListener(this, listener, handler);
            listening = true;
            onActiveSessionsChanged(sessions.getActiveSessions(listener));
        } catch (SecurityException e) {
            showIdle();
        }
    }

    /** A dropdown menu opens over the panel's place: hide it until the menu closes. */
    void setCovered(boolean covered) {
        panel.setVisibility(covered ? View.INVISIBLE : View.VISIBLE);
    }

    /** Moves the focus to play-pause; false while the panel is hidden. */
    boolean focus() {
        return panel.getVisibility() == View.VISIBLE && playPause.requestFocus();
    }

    /**
     * The bar and "elapsed / length" from the session: the last reported position moved on by the time since, at
     * the playback speed. Hidden when the app gives no length.
     */
    private void updateProgress() {
        MediaController c = controller;
        PlaybackState s = c == null ? null : c.getPlaybackState();
        MediaMetadata m = c == null ? null : c.getMetadata();
        long length = m == null ? 0 : m.getLong(MediaMetadata.METADATA_KEY_DURATION);
        if (s == null || length <= 0) {
            progressRow.setVisibility(View.GONE);
            return;
        }
        long pos = s.getPosition();
        if (s.getState() == PlaybackState.STATE_PLAYING) {
            pos += (long) ((android.os.SystemClock.elapsedRealtime() - s.getLastPositionUpdateTime()) * s.getPlaybackSpeed());
        }
        pos = Math.max(0, Math.min(length, pos));
        progressRow.setVisibility(View.VISIBLE);
        bar.setFraction(pos / (float) length);
        time.setText(clock(pos) + " / " + clock(length));
    }

    private static String clock(long ms) {
        long s = ms / 1000;
        return String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    /** A row of segments, as many lit in cyan as the fraction played; the rest faint. */
    private static final class SegmentBar extends View {
        private static final int SEGMENTS = 12;
        private final android.graphics.Paint paint = new android.graphics.Paint();
        private final float gap;
        private float fraction;

        SegmentBar(android.content.Context context, float density) {
            super(context);
            gap = 2 * density;
        }

        void setFraction(float f) {
            if (Math.abs(f - fraction) < 0.001f) return;
            fraction = f;
            invalidate();
        }

        @Override
        protected void onDraw(android.graphics.Canvas c) {
            float w = (getWidth() - gap * (SEGMENTS - 1)) / SEGMENTS;
            int lit = Math.round(fraction * SEGMENTS);
            for (int i = 0; i < SEGMENTS; i++) {
                paint.setColor(i < lit ? NeonGlitchView.NEON_CYAN : 0x4000F0FF);
                float x = i * (w + gap);
                c.drawRect(x, 0, x + w, getHeight(), paint);
            }
        }
    }

    /** Home left the screen: stop listening. */
    void stop() {
        handler.removeCallbacks(progressTick);
        if (listening) {
            sessions.removeOnActiveSessionsChangedListener(this);
            listening = false;
        }
        attach(null);
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> list) {
        MediaController best = null;
        if (list != null) {
            for (MediaController c : list) {
                PlaybackState s = c.getPlaybackState();
                if (isPlaying(s)) {
                    best = c;
                    break;
                }
                if (best == null && s != null && s.getState() == PlaybackState.STATE_PAUSED) best = c;
            }
        }
        attach(best);
    }

    private void attach(MediaController c) {
        if (controller != null) controller.unregisterCallback(this);
        controller = c;
        if (c != null) c.registerCallback(this, handler);
        refresh();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        refresh();
    }

    @Override
    public void onMetadataChanged(MediaMetadata metadata) {
        refresh();
    }

    @Override
    public void onSessionDestroyed() {
        attach(null);
    }

    private void refresh() {
        MediaController c = controller;
        PlaybackState state = c == null ? null : c.getPlaybackState();
        MediaMetadata meta = c == null ? null : c.getMetadata();
        boolean shown = state != null && meta != null && (isPlaying(state)
                || state.getState() == PlaybackState.STATE_PAUSED || state.getState() == PlaybackState.STATE_BUFFERING);
        if (!shown) {
            showIdle();
            return;
        }
        setSkipEnabled(true);
        CharSequence t = meta.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        if (TextUtils.isEmpty(t)) t = meta.getText(MediaMetadata.METADATA_KEY_TITLE);
        CharSequence a = meta.getText(MediaMetadata.METADATA_KEY_ARTIST);
        if (TextUtils.isEmpty(a)) a = meta.getText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        title.setText(t);
        artist.setText(a);
        artist.setVisibility(TextUtils.isEmpty(a) ? View.GONE : View.VISIBLE);
        String app = appLabel(c.getPackageName());
        source.setText(activity.getString(isPlaying(state) ? R.string.np_playing : R.string.np_paused, app)
                .toUpperCase(Locale.getDefault()));
        Bitmap cover = meta.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (cover == null) cover = meta.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if (cover == null) cover = meta.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        if (cover != null) {
            art.setImageBitmap(cover);
        } else {
            try {
                art.setImageDrawable(activity.getPackageManager().getApplicationIcon(c.getPackageName()));
            } catch (PackageManager.NameNotFoundException e) {
                art.setImageDrawable(null);
            }
        }
        playPause.setImageResource(isPlaying(state) ? R.drawable.ic_np_pause : R.drawable.ic_np_play);
        handler.removeCallbacks(progressTick);
        progressTick.run();   // draws now, and keeps ticking while it plays
        if (panel.getVisibility() != View.VISIBLE) {
            panel.setVisibility(View.VISIBLE);
            for (View v : above) v.setNextFocusDownId(playPause.getId());
        }
    }

    /**
     * Nothing playing (or no access to the sessions): the panel stays in place with Spotify's icon and "Nothing
     * playing"; play-pause opens Spotify, previous and next step aside.
     */
    private void showIdle() {
        handler.removeCallbacks(progressTick);
        progressRow.setVisibility(View.GONE);
        source.setText(activity.getString(R.string.np_idle).toUpperCase(Locale.getDefault()));
        title.setText(appLabel(IDLE_APP));
        artist.setVisibility(View.GONE);
        try {
            art.setImageDrawable(activity.getPackageManager().getApplicationIcon(IDLE_APP));
        } catch (PackageManager.NameNotFoundException e) {
            art.setImageDrawable(null);
        }
        playPause.setImageResource(R.drawable.ic_np_play);
        setSkipEnabled(false);
        if (panel.getVisibility() != View.VISIBLE) {
            panel.setVisibility(View.VISIBLE);
            for (View v : above) v.setNextFocusDownId(playPause.getId());
        }
    }

    /** Previous and next only take focus while something is playing or paused. */
    private void setSkipEnabled(boolean on) {
        for (View b : new View[] {prev, next}) {
            if (!on && b.isFocused()) playPause.requestFocus();
            b.setFocusable(on);
            b.setAlpha(on ? 1f : 0.35f);
        }
    }

    private String appLabel(String pkg) {
        PackageManager pm = activity.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    private static boolean isPlaying(PlaybackState s) {
        return s != null && s.getState() == PlaybackState.STATE_PLAYING;
    }

    private TextView text(int sp, int color, int font) {
        TextView v = new TextView(activity);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setTypeface(UiFont.get(activity, font));
        v.setSingleLine(true);
        v.setEllipsize(TextUtils.TruncateAt.END);
        v.setMaxWidth(dp(320));
        v.setIncludeFontPadding(false);
        return v;
    }

    /** A square Cyberpunk menu button: dark teal with a cyan edge and icon, a solid accent block with a black icon when focused. */
    private ImageView button(int icon, int description) {
        ImageView b = new ImageView(activity);
        b.setId(View.generateViewId());
        b.setImageResource(icon);
        b.setScaleType(ImageView.ScaleType.CENTER);
        b.setContentDescription(activity.getString(description));
        b.setFocusable(true);
        b.setClickable(true);
        Drawable rest = new CutCornerDrawable(0x99061418, 0x9900F0FF, Math.max(1, Math.round(density)), dp(7), 0, 0);
        Drawable focused = new CutCornerDrawable(accent, 0, 0, dp(7), 0, 0);
        StateListDrawable bg = new StateListDrawable();
        bg.addState(new int[] {android.R.attr.state_focused}, focused);
        bg.addState(new int[0], rest);
        b.setBackground(bg);
        b.setImageTintList(new ColorStateList(new int[][] {{android.R.attr.state_focused}, {}},
                new int[] {0xFF0A0A0A, NeonGlitchView.NEON_CYAN}));
        return b;
    }

    private LinearLayout.LayoutParams buttonParams(boolean gap) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(30));
        if (gap) lp.setMarginStart(dp(8));
        return lp;
    }

    private int dp(float v) {
        return Math.round(v * density);
    }

    /** Four L-shaped corner brackets along the bounds' edges, like a targeting visor. */
    private static final class VisorDrawable extends Drawable {
        private final android.graphics.Paint paint = new android.graphics.Paint();
        private final float arm, width;

        VisorDrawable(int color, float arm, float width) {
            paint.setColor(color);
            this.arm = arm;
            this.width = width;
        }

        @Override
        public void draw(android.graphics.Canvas c) {
            android.graphics.Rect b = getBounds();
            float l = b.left, t = b.top, r = b.right, btm = b.bottom;
            c.drawRect(l, t, l + arm, t + width, paint);
            c.drawRect(l, t, l + width, t + arm, paint);
            c.drawRect(r - arm, t, r, t + width, paint);
            c.drawRect(r - width, t, r, t + arm, paint);
            c.drawRect(l, btm - width, l + arm, btm, paint);
            c.drawRect(l, btm - arm, l + width, btm, paint);
            c.drawRect(r - arm, btm - width, r, btm, paint);
            c.drawRect(r - width, btm - arm, r, btm, paint);
        }

        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(android.graphics.ColorFilter filter) {}
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }
}
