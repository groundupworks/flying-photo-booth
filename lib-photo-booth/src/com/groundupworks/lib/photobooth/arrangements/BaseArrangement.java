/*
 * This file is part of Flying PhotoBooth.
 * 
 * Flying PhotoBooth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Flying PhotoBooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Flying PhotoBooth.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.groundupworks.lib.photobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.groundupworks.lib.photobooth.helpers.ImageHelper.Arrangement;

/**
 * Base class where other {@link Arrangement} implementations extend from.
 *
 * @author Benedict Lau
 */
public abstract class BaseArrangement implements Arrangement {

    /**
     * Photo strip panel padding.
     */
    public static final int PHOTO_STRIP_PANEL_PADDING = 50;

    //
    // Private methods.
    //

    /**
     * Gets the header bitmap for the photo strip. The base implementation returns null.
     *
     * @param width the width of the header bitmap.
     * @return a bitmap to be drawn as the photo strip header; or null if no header is applied.
     */
    protected Bitmap getHeader(int width) {
        return null;
    }

    /**
     * Draws the border for the photo strip.
     *
     * @param canvas the canvas to draw on.
     * @param left   the left side of the photo strip.
     * @param top    the top of the photo strip.
     * @param right  the right side of the photo strip.
     * @param bottom the bottom of the photo strip.
     */
    protected static void drawPhotoStripBorders(Canvas canvas, float left, float top, float right, float bottom) {
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        drawRectOutline(canvas, left, top, right, bottom, paint);
    }

    /**
     * Draws the border for a panel.
     *
     * @param canvas the canvas to draw on.
     * @param left   the left side of the panel.
     * @param top    the top of the panel.
     * @param right  the right side of the panel.
     * @param bottom the bottom of the panel.
     */
    protected static void drawPanelBorders(Canvas canvas, float left, float top, float right, float bottom) {
        Paint paint = new Paint();

        paint.setColor(Color.DKGRAY);
        drawRectOutline(canvas, left, top, right, bottom, paint);

        paint.setColor(Color.LTGRAY);
        drawRectOutline(canvas, left - 1, top - 1, right + 1, bottom + 1, paint);
    }

    /**
     * Draws the outline of a rectangle.
     *
     * @param canvas the canvas to draw on.
     * @param left   the left side of the rectangle to be drawn.
     * @param top    the top of the rectangle to be drawn.
     * @param right  the right side of the rectangle to be drawn.
     * @param bottom the bottom of the rectangle to be drawn.
     * @param paint  the {@link Paint} to use for drawing.
     */
    protected static void drawRectOutline(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(right, top, right, bottom, paint);
        canvas.drawLine(right, bottom, left, bottom, paint);
        canvas.drawLine(left, bottom, left, top, paint);
    }
}
