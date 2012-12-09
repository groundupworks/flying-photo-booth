package com.groundupworks.flyingphotobooth.filters;

import android.graphics.Bitmap;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.ImageFilter;
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
