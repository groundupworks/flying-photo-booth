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
import android.graphics.Color;

import com.groundupworks.lib.photobooth.helpers.ImageHelper.ImageFilter;
import com.jabistudio.androidjhlabs.filter.EdgeFilter;
import com.jabistudio.androidjhlabs.filter.MedianFilter;
import com.jabistudio.androidjhlabs.filter.ThresholdFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

/**
 * Filter to covert image to line art.
 *
 * @author Benedict Lau
 */
public class LineArtFilter implements ImageFilter {

    @Override
    public Bitmap applyFilter(Bitmap srcBitmap) {
        final int width = srcBitmap.getWidth();
        final int height = srcBitmap.getHeight();
        int[] colors = AndroidUtils.bitmapToIntArray(srcBitmap);

        /*
         * Apply image filters.
         */
        EdgeFilter edgeFilter = new EdgeFilter();
        colors = edgeFilter.filter(colors, width, height);

        ThresholdFilter thresholdFilter = new ThresholdFilter();
        thresholdFilter.setDimensions(width, height);
        thresholdFilter.setLowerThreshold(35);
        thresholdFilter.setUpperThreshold(0);
        thresholdFilter.setWhite(Color.BLACK);
        thresholdFilter.setBlack(Color.WHITE);
        colors = thresholdFilter.filter(colors, width, height);

        MedianFilter medianFilter = new MedianFilter();
        colors = medianFilter.filter(colors, width, height);

        return Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);
    }
}
