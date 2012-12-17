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
package com.groundupworks.flyingphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;

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
            // Create canvas to draw on return bitmap.
            Canvas canvas = new Canvas(returnBitmap);
            canvas.drawColor(Color.WHITE);

            // Draw each bitmap.
            int i = 0;
            for (Bitmap bitmap : srcBitmaps) {
                int left = (srcBitmapWidth + PHOTO_STRIP_PANEL_PADDING) * i + PHOTO_STRIP_PANEL_PADDING;
                int top = PHOTO_STRIP_PANEL_PADDING;
                int right = left + srcBitmapWidth - 1;
                int bottom = top + srcBitmapHeight - 1;

                // Draw bitmaps.
                canvas.drawBitmap(bitmap, left, top, null);

                // Draw panel borders.
                Paint paint = new Paint();

                paint.setColor(Color.DKGRAY);
                drawRectOutline(canvas, left, top, right, bottom, paint);

                paint.setColor(Color.LTGRAY);
                drawRectOutline(canvas, left - 1, top - 1, right + 1, bottom + 1, paint);

                i++;
            }
        }

        return returnBitmap;
    }

}
