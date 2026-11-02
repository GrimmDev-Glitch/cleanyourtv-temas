package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Edgerunners skin: a panel with its bottom-right corner cut off at 45 degrees, as the Cyberpunk menus draw them - a
 * fill, an optional edge line following the cut, and an optional bar down the left side. The path is rebuilt only
 * when the bounds change.
 */
final class CutCornerDrawable extends Drawable {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bar = new Paint();
    private final float cut, barWidth;
    private final Path path = new Path();
    private final RectF box = new RectF();

    /**
     * @param fillColor fill, 0 for none
     * @param edgeColor edge line, 0 for none
     * @param edgeWidth edge line width in px
     * @param cut       the cut's size along each side, in px
     * @param barColor  bar down the left side, 0 for none
     * @param barWidth  its width in px
     */
    CutCornerDrawable(int fillColor, int edgeColor, float edgeWidth, float cut, int barColor, float barWidth) {
        fill.setColor(fillColor);
        edge.setStyle(Paint.Style.STROKE);
        edge.setColor(edgeColor);
        edge.setStrokeWidth(edgeWidth);
        bar.setColor(barColor);
        this.cut = cut;
        this.barWidth = barColor == 0 ? 0 : barWidth;
    }

    /** The outline with the bottom-right corner cut, for {@code r}. */
    static void cutPath(Path p, RectF r, float cut) {
        float c = Math.min(cut, Math.min(r.width(), r.height()) / 2f);
        p.reset();
        p.moveTo(r.left, r.top);
        p.lineTo(r.right, r.top);
        p.lineTo(r.right, r.bottom - c);
        p.lineTo(r.right - c, r.bottom);
        p.lineTo(r.left, r.bottom);
        p.close();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        float inset = edge.getColor() != 0 ? edge.getStrokeWidth() / 2f : 0f;
        box.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
        cutPath(path, box, cut);
    }

    @Override
    public void draw(Canvas canvas) {
        if (fill.getColor() != 0) canvas.drawPath(path, fill);
        if (barWidth > 0) {
            Rect b = getBounds();
            canvas.drawRect(b.left, b.top, b.left + barWidth, b.bottom, bar);
        }
        if (edge.getColor() != 0) canvas.drawPath(path, edge);
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(ColorFilter filter) {}
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
