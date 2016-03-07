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

import com.groundupworks.lib.photobooth.helpers.ImageHelper;

/**
 * Vertical arrangement of bitmaps to create a photo strip.
 *
 * @author Benedict Lau
 */
public class VerticalArrangement extends BaseArrangement {

    @Override
    public Bitmap createPhotoStrip(Bitmap[] srcBitmaps) {
        Bitmap returnBitmap = null;

        // Calculate return bitmap width.
        int srcBitmapWidth = srcBitmaps[0].getWidth();
        int returnBitmapWidth = srcBitmapWidth + PHOTO_STRIP_PANEL_PADDING * 2;

        // Get header bitmap if applied.
        int headerHeight = 0;
        Bitmap header = getHeader(returnBitmapWidth);
        if (header != null) {
            headerHeight = header.getHeight();
        }

        // Calculate return bitmap height.
        int srcBitmapHeight = srcBitmaps[0].getHeight();
        int returnBitmapHeight = srcBitmapHeight * srcBitmaps.length + PHOTO_STRIP_PANEL_PADDING
                * (srcBitmaps.length + 1) + headerHeight;

        returnBitmap = Bitmap.createBitmap(returnBitmapWidth, returnBitmapHeight, ImageHelper.BITMAP_CONFIG);
        if (returnBitmap != null) {
            // Create canvas and draw photo strip.
            Canvas canvas = new Canvas(returnBitmap);
            canvas.drawColor(Color.WHITE);

            // Draw header bitmap.
            if (header != null) {
                canvas.drawBitmap(header, 0, 0, null);
                header.recycle();
                header = null;
            }

            // Draw photo bitmaps.
            int i = 0;
            for (Bitmap bitmap : srcBitmaps) {
                int left = PHOTO_STRIP_PANEL_PADDING;
                int top = (srcBitmapHeight + PHOTO_STRIP_PANEL_PADDING) * i + PHOTO_STRIP_PANEL_PADDING + headerHeight;
                int right = left + srcBitmapWidth - 1;
                int bottom = top + srcBitmapHeight - 1;

                // Draw panel.
                canvas.drawBitmap(bitmap, left, top, null);
                drawPanelBorders(canvas, left, top, right, bottom);

                i++;
            }

            // Draw photo strip borders.
            drawPhotoStripBorders(canvas, 0, 0, returnBitmapWidth - 1, returnBitmapHeight - 1);
        }

        return returnBitmap;
    }
}
