/*
 * Copyright (C) 2012 Benedict Lau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
