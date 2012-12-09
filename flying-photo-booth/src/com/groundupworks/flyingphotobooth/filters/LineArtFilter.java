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
package com.groundupworks.flyingphotobooth.filters;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.ImageFilter;
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
