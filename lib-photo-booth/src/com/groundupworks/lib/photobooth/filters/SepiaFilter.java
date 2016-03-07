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
    private static final float[] SEPIA_COLOR_MATRIX = {0.3930000066757202f, 0.7689999938011169f, 0.1889999955892563f,
            0, 0, 0.3490000069141388f, 0.6859999895095825f, 0.1679999977350235f, 0, 0, 0.2720000147819519f,
            0.5339999794960022f, 0.1309999972581863f, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1};

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
