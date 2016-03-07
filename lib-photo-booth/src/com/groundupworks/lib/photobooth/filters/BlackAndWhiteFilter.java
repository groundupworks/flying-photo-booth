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

import com.groundupworks.lib.photobooth.helpers.ImageHelper.ImageFilter;
import com.jabistudio.androidjhlabs.filter.GrayscaleFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

/**
 * Filter to covert image to black and white.
 *
 * @author Benedict Lau
 */
public class BlackAndWhiteFilter implements ImageFilter {

    @Override
    public Bitmap applyFilter(Bitmap srcBitmap) {
        final int width = srcBitmap.getWidth();
        final int height = srcBitmap.getHeight();
        int[] colors = AndroidUtils.bitmapToIntArray(srcBitmap);

        /*
         * Apply image filters.
         */
        GrayscaleFilter grayscaleFilter = new GrayscaleFilter();
        colors = grayscaleFilter.filter(colors, width, height);

        return Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
    }
}
