package com.groundupworks.flyingphotobooth.arrangements;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.Arrangement;

/**
 * Base class where other {@link Arrangement} implementations extend from.
 * 
 * @author Benedict Lau
 */
public abstract class BaseArrangement implements Arrangement {

    /**
     * Comic panel padding.
     */
    public static final int PHOTO_STRIP_PANEL_PADDING = 25;

    //
    // Private methods.
    //

    /**
     * Draws the outline of a rectangle.
     * 
     * @param canvas
     *            the canvas to draw on.
     * @param left
     *            the left side of the rectangle to be drawn.
     * @param top
     *            the top of the rectangle to be drawn.
     * @param right
     *            the right side of the rectangle to be drawn.
     * @param bottom
     *            the bottom of the rectangle to be drawn.
     * @param paint
     *            the {@link Paint} to use for drawing.
     */
    protected static void drawRectOutline(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(right, top, right, bottom, paint);
        canvas.drawLine(right, bottom, left, bottom, paint);
        canvas.drawLine(left, bottom, left, top, paint);
    }
}
