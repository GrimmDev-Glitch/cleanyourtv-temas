package com.gathin.tvhome;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Edgerunners skin: the network pill's icon as four segmented bars rising to the right, as many lit as the Wi-Fi
 * signal is strong (0 offline, 4 on Ethernet). Lit bars take the pill's icon tint for its state, cyan at rest and
 * black on the focused yellow pill; unlit ones the same colour, faint.
 */
final class SignalBarsDrawable extends Drawable {
    private static final int BARS = 4;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private ColorStateList tint;
    private int color = 0xFF00F0FF;
    private int lit;

    SignalBarsDrawable(float density) {
        this.density = density;
    }

    /** Bars lit, 0 to 4. */
    void setBars(int bars) {
        int n = Math.max(0, Math.min(BARS, bars));
        if (n == lit) return;
        lit = n;
        invalidateSelf();
    }

    @Override public int getIntrinsicWidth() { return Math.round(22 * density); }
    @Override public int getIntrinsicHeight() { return Math.round(18 * density); }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        float gap = 2f * density;
        float w = (b.width() - gap * (BARS - 1)) / BARS;
        for (int i = 0; i < BARS; i++) {
            float h = b.height() * (i + 1) / (float) BARS;
            float left = b.left + i * (w + gap);
            paint.setColor(i < lit ? color : (color & 0xFFFFFF) | 0x47000000);
            canvas.drawRect(left, b.bottom - h, left + w, b.bottom, paint);
        }
    }

    @Override
    public void setTintList(ColorStateList tint) {
        this.tint = tint;
        onStateChange(getState());
        invalidateSelf();
    }

    @Override public boolean isStateful() { return tint != null && tint.isStateful(); }

    @Override
    protected boolean onStateChange(int[] state) {
        int next = tint != null ? tint.getColorForState(state, tint.getDefaultColor()) : color;
        if (next == color) return false;
        color = next;
        return true;
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(ColorFilter filter) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
