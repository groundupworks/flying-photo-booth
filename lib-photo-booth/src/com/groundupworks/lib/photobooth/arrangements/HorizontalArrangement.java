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
 * Horizontal arrangement of bitmaps to create a photo strip.
 * 
 * @author Benedict Lau
 */
public class HorizontalArrangement extends BaseArrangement {

    @Override
    public Bitmap createPhotoStrip(Bitmap[] srcBitmaps) {
        Bitmap returnBitmap = null;

        // Calculate return bitmap dimensions.
        int srcBitmapWidth = srcBitmaps[0].getWidth();
        int srcBitmapHeight = srcBitmaps[0].getHeight();
        int returnBitmapWidth = srcBitmapWidth * srcBitmaps.length + PHOTO_STRIP_PANEL_PADDING
                * (srcBitmaps.length + 1);
        int returnBitmapHeight = srcBitmapHeight + PHOTO_STRIP_PANEL_PADDING * 2;

        returnBitmap = Bitmap.createBitmap(returnBitmapWidth, returnBitmapHeight, ImageHelper.BITMAP_CONFIG);
        if (returnBitmap != null) {
            // Create canvas and draw photo strip.
            Canvas canvas = new Canvas(returnBitmap);
            canvas.drawColor(Color.WHITE);
            drawPhotoStripBorders(canvas, 0, 0, returnBitmapWidth - 1, returnBitmapHeight - 1);

            // Draw each bitmap.
            int i = 0;
            for (Bitmap bitmap : srcBitmaps) {
                int left = (srcBitmapWidth + PHOTO_STRIP_PANEL_PADDING) * i + PHOTO_STRIP_PANEL_PADDING;
                int top = PHOTO_STRIP_PANEL_PADDING;
                int right = left + srcBitmapWidth - 1;
                int bottom = top + srcBitmapHeight - 1;

                // Draw panel.
                canvas.drawBitmap(bitmap, left, top, null);
                drawPanelBorders(canvas, left, top, right, bottom);

                i++;
            }
        }

        return returnBitmap;
    }

}
