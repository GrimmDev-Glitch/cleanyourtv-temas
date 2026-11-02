package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;

/**
 * The Edgerunners skin's greeting for the first Home after the launcher starts: a transparent full-screen overlay
 * with CRT scanlines, a bright neon bar sweeping down the screen and torn slices of cyan and magenta flickering
 * over it, all fading out at the end, then the callback runs on the main thread. Like {@link BatFlightView} it
 * takes no focus, clicks or accessibility and allocates nothing per frame.
 */
public final class NeonGlitchView extends View {

    static final int NEON_YELLOW = 0xFFFCEE0A;
    static final int NEON_CYAN = 0xFF00F0FF;
    static final int NEON_MAGENTA = 0xFFFF2A6D;

    /** Torn slices drawn per frame. */
    private static final int SLICES = 7;

    private final float density;
    private final Random rnd = new Random();
    private final Paint line = new Paint();
    private final Paint bar = new Paint();
    private final Paint slice = new Paint();

    /** Boot lines typed out in the middle, one after another; the last in yellow. Empty: none. */
    private String[] bootLines = new String[0];
    private final Paint bootText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint band = new Paint();

    private boolean playing;
    private long start, duration;
    private Runnable onDone;
    private final Runnable finish = this::finish;

    public NeonGlitchView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        line.setColor(0xFF000000);
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setWillNotDraw(false);
    }

    /** Lines typed out in the middle while it plays, like a system booting; call before {@link #play}. */
    void setBootLines(String[] lines, android.graphics.Typeface font) {
        bootLines = lines;
        bootText.setTextSize(22 * density);
        bootText.setLetterSpacing(0.18f);
        if (font != null) bootText.setTypeface(font);
    }

    /** Plays for {@code durationMs}, then runs {@code done} on the main thread. */
    public void play(long durationMs, Runnable done) {
        if (playing) {
            removeCallbacks(finish);
            finish();
        }
        duration = Math.max(1, durationMs);
        onDone = done;
        playing = true;
        start = SystemClock.uptimeMillis();
        postDelayed(finish, duration);
        postInvalidateOnAnimation();
    }

    private void finish() {
        if (!playing) return;
        playing = false;
        invalidate();
        Runnable done = onDone;
        onDone = null;
        if (done != null) done.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (playing) {
            removeCallbacks(finish);
            finish();
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        if (!playing) return;
        float p = (SystemClock.uptimeMillis() - start) / (float) duration;
        if (p >= 1f) return;
        float w = getWidth(), h = getHeight();
        // Everything fades over the last third.
        float fade = p < 0.66f ? 1f : 1f - (p - 0.66f) / 0.34f;

        // Scanlines: a dark line every 4dp over the whole screen.
        line.setAlpha((int) (70 * fade));
        float step = 4 * density;
        for (float y = 0; y < h; y += step) c.drawRect(0, y, w, y + density, line);

        // The sweep: a yellow bar with a cyan trail, top to bottom over the first 70%.
        float sp = Math.min(1f, p / 0.7f);
        float by = -20 * density + (h + 40 * density) * sp;
        bar.setColor(NEON_CYAN);
        bar.setAlpha((int) (60 * fade));
        c.drawRect(0, by - 60 * density, w, by, bar);
        bar.setColor(NEON_YELLOW);
        bar.setAlpha((int) (200 * fade));
        c.drawRect(0, by, w, by + 3 * density, bar);

        // Torn slices: short bands offset sideways, flickering, densest at the start.
        int n = (int) (SLICES * (1f - p) + 1);
        for (int i = 0; i < n; i++) {
            if (rnd.nextFloat() < 0.35f) continue;
            float sy = rnd.nextFloat() * h;
            float sh = (2 + rnd.nextFloat() * 18) * density;
            float sx = (rnd.nextFloat() - 0.5f) * w * 0.3f;
            float sw = w * (0.2f + rnd.nextFloat() * 0.6f);
            slice.setColor(rnd.nextBoolean() ? NEON_CYAN : NEON_MAGENTA);
            slice.setAlpha((int) ((50 + rnd.nextInt(90)) * fade));
            float left = (w - sw) / 2f + sx;
            c.drawRect(left, sy, left + sw, sy + sh, slice);
        }
        drawBootLines(c, p, fade, w, h);
        postInvalidateOnAnimation();
    }

    /**
     * The boot lines over the first 70%: each takes its share of that time, types itself in with a block cursor,
     * then stays; the last one in yellow. Everything fades with the rest.
     */
    private void drawBootLines(Canvas c, float p, float fade, float w, float h) {
        int n = bootLines.length;
        if (n == 0) return;
        float lineH = 38 * density;
        float top = h / 2f - lineH * n / 2f;
        float span = 0.7f / n;
        // A dark band behind the lines, so they read over whatever Home shows there.
        band.setColor(0x05080C);
        band.setAlpha((int) (0xD9 * fade));
        c.drawRect(0, top - 12 * density, w, top + lineH * n + 18 * density, band);
        for (int i = 0; i < n; i++) {
            float lp = (p - i * span) / span;
            if (lp <= 0f) break;
            String line = bootLines[i];
            int shown = Math.min(line.length(), (int) Math.ceil(Math.min(1f, lp * 1.4f) * line.length()));
            String text = line.substring(0, shown);
            boolean typing = shown < line.length() || (i == n - 1 && (int) (p * 12) % 2 == 0);
            bootText.setColor(i == n - 1 ? NEON_YELLOW : NEON_CYAN);
            bootText.setAlpha((int) (255 * fade));
            float y = top + lineH * (i + 1);
            // Centred on the whole line, so a line does not shift sideways while it types.
            float left = (w - bootText.measureText(line)) / 2f;
            c.drawText(typing ? text + "_" : text, left, y, bootText);
        }
    }
}
