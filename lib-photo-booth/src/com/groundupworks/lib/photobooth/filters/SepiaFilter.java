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
package com.groundupworks.lib.photobooth.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper.ImageFilter;

/**
 * Filter to apply sepia effect to image.
 * 
 * @author Benedict Lau
 */
public class SepiaFilter implements ImageFilter {

    /**
     * Values for sepia color matrix.
     */
    private static final float[] SEPIA_COLOR_MATRIX = { 0.3930000066757202f, 0.7689999938011169f, 0.1889999955892563f,
            0, 0, 0.3490000069141388f, 0.6859999895095825f, 0.1679999977350235f, 0, 0, 0.2720000147819519f,
            0.5339999794960022f, 0.1309999972581863f, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1 };

    @Override
    public Bitmap applyFilter(Bitmap srcBitmap) {
        Bitmap returnBitmap = null;

        /*
         * Apply image filters.
         */
        returnBitmap = srcBitmap.copy(ImageHelper.BITMAP_CONFIG, true);
        if (returnBitmap != null) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.set(SEPIA_COLOR_MATRIX);

            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

            Canvas canvas = new Canvas(returnBitmap);
            canvas.drawBitmap(returnBitmap, 0, 0, paint);
        }

        return returnBitmap;
    }
}
