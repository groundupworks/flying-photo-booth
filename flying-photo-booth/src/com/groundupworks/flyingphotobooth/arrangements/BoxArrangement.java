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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Box arrangement of bitmaps to create a photo strip.
 * 
 * @author Benedict Lau
 */
public class BoxArrangement extends BaseArrangement {

    @Override
    public Bitmap createPhotoStrip(Bitmap[] srcBitmaps) {
        Bitmap returnBitmap = null;

        // Calculate return bitmap dimensions.
        int boxLength = srcBitmaps.length / 2;
        int srcBitmapWidth = srcBitmaps[0].getWidth();
        int srcBitmapHeight = srcBitmaps[0].getHeight();
        int returnBitmapWidth = srcBitmapWidth * boxLength + PHOTO_STRIP_PANEL_PADDING * (boxLength + 1);
        int returnBitmapHeight = srcBitmapHeight * boxLength + PHOTO_STRIP_PANEL_PADDING * (boxLength + 1);

        returnBitmap = Bitmap.createBitmap(returnBitmapWidth, returnBitmapHeight, Config.RGB_565);
        if (returnBitmap != null) {
            // Create canvas to draw on return bitmap.
            Canvas canvas = new Canvas(returnBitmap);
            canvas.drawColor(Color.WHITE);

            // Draw each bitmap.
            int i = 0;
            for (Bitmap bitmap : srcBitmaps) {
                // Even indices start at first column and odd indices start at second column.
                int left = (srcBitmapWidth + PHOTO_STRIP_PANEL_PADDING) * (i % 2) + PHOTO_STRIP_PANEL_PADDING;
                int top = (srcBitmapHeight + PHOTO_STRIP_PANEL_PADDING) * (i / 2) + PHOTO_STRIP_PANEL_PADDING;
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
