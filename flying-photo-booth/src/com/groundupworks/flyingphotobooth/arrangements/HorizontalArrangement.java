package com.groundupworks.flyingphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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

        returnBitmap = Bitmap.createBitmap(returnBitmapWidth, returnBitmapHeight, Config.RGB_565);
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
