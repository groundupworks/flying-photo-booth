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
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;

import com.groundupworks.lib.photobooth.arrangements.BaseArrangement;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

/**
 * An {@link IPhotoStripHeader} implementation that prints two lines of event title text followed by the event date.
 *
 * @author Benedict Lau
 */
public class BaseTitleHeader implements IPhotoStripHeader {

    /**
     * The max pixel width of the stored event logo.
     */
    public static final int EVENT_LOGO_MAX_WIDTH = ImageHelper.IMAGE_SIZE;

    /**
     * The max pixel height of the stored event logo.
     */
    public static final int EVENT_LOGO_MAX_HEIGHT = EVENT_LOGO_MAX_WIDTH * 2 / 3;

    /**
     * The cache key used to store to event logo.
     */
    public static final String EVENT_LOGO_CACHE_KEY = "eventLogo";

    /**
     * The top and bottom padding of the header.
     */
    private static final int HEADER_PADDING = BaseArrangement.PHOTO_STRIP_PANEL_PADDING;

    /**
     * The height of each line.
     */
    private static final int TEXT_LINE_HEIGHT = 100;

    /**
     * The radius of the text shadow.
     */
    private static final float TEXT_SHADOW_RADIUS = 3f;

    /**
     * The dx of the text shadow.
     */
    private static final float TEXT_SHADOW_DX = 0f;

    /**
     * The dy of the text shadow.
     */
    private static final float TEXT_SHADOW_DY = 3f;

    /**
     * The first line of the event title.
     */
    private String mLineOne = null;

    /**
     * The second line of the event title.
     */
    private String mLineTwo = null;

    /**
     * The date of the event.
     */
    private String mDate = null;

    /**
     * The event logo.
     */
    private Bitmap mLogo = null;

    /**
     * The font.
     */
    private Typeface mFont = null;

    /**
     * Constructor.
     *
     * @param lineOne the first line of the event title; or null to hide.
     * @param lineTwo the second line of the event title; or null to hide.
     * @param date    the date of the event; or null to hide.
     * @param logo    the event logo; or null to hide.
     * @param font    the font.
     */
    public BaseTitleHeader(String lineOne, String lineTwo, String date, Bitmap logo, Typeface font) {
        mLineOne = lineOne;
        mLineTwo = lineTwo;
        mDate = date;
        mLogo = logo;
        mFont = font;
    }

    @Override
    public Bitmap getHeaderBitmap(int width) {
        Bitmap bitmap = null;

        boolean hasLineOne = TextHelper.isValid(mLineOne);
        boolean hasLineTwo = TextHelper.isValid(mLineTwo);
        boolean hasDate = TextHelper.isValid(mDate);
        boolean hasLogo = mLogo != null;

        if (hasLineOne || hasLineTwo || hasDate || hasLogo) {
            // Configure paint for drawing text.
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTypeface(mFont);
            paint.setShadowLayer(TEXT_SHADOW_RADIUS, TEXT_SHADOW_DX, TEXT_SHADOW_DY, Color.GRAY);
            paint.setTextAlign(Align.CENTER);

            // Calculate height and text sizes.
            int height = HEADER_PADDING * 2;
            float lineOneTextSize = 0f;
            float lineTwoTextSize = 0f;
            float dateTextSize = 0f;
            if (hasLineOne) {
                height += TEXT_LINE_HEIGHT;
                lineOneTextSize = TextHelper.getFittedTextSize(mLineOne, width, TEXT_LINE_HEIGHT, paint);
            }
            if (hasLineTwo) {
                height += TEXT_LINE_HEIGHT;
                lineTwoTextSize = TextHelper.getFittedTextSize(mLineTwo, width, TEXT_LINE_HEIGHT, paint);
            }
            if (hasDate) {
                height += TEXT_LINE_HEIGHT;
                dateTextSize = TextHelper.getFittedTextSize(mDate, width, TEXT_LINE_HEIGHT, paint);
            }
            if (hasLogo) {
                height += HEADER_PADDING + mLogo.getHeight();
            }

            // Calculate optimal size by using the smallest non-zero text size.
            float optimalTextSize = Float.MAX_VALUE;
            if (lineOneTextSize > 0f && lineOneTextSize < optimalTextSize) {
                optimalTextSize = lineOneTextSize;
            }
            if (lineTwoTextSize > 0f && lineTwoTextSize < optimalTextSize) {
                optimalTextSize = lineTwoTextSize;
            }
            if (dateTextSize > 0f && dateTextSize < optimalTextSize) {
                optimalTextSize = dateTextSize;
            }

            // Create header bitmap.
            bitmap = Bitmap.createBitmap(width, height, ImageHelper.BITMAP_CONFIG);
            if (bitmap != null) {
                Canvas canvas = new Canvas(bitmap);
                int yOffset = HEADER_PADDING;

                // Proceed only if the optimal text size is valid.
                if (optimalTextSize < Float.MAX_VALUE) {
                    paint.setTextSize(optimalTextSize);

                    // Draw line one in black.
                    if (hasLineOne) {
                        yOffset += TEXT_LINE_HEIGHT;
                        paint.setColor(Color.BLACK);
                        canvas.drawText(mLineOne, width / 2, yOffset, paint);
                    }

                    // Draw line two in black.
                    if (hasLineTwo) {
                        yOffset += TEXT_LINE_HEIGHT;
                        paint.setColor(Color.BLACK);
                        canvas.drawText(mLineTwo, width / 2, yOffset, paint);
                    }

                    // Draw date in grey with no shadow.
                    if (hasDate) {
                        yOffset += TEXT_LINE_HEIGHT;
                        paint.setShadowLayer(0f, 0f, 0f, Color.WHITE);
                        paint.setColor(Color.GRAY);
                        canvas.drawText(mDate, width / 2, yOffset, paint);
                    }
                }

                // Draw event logo.
                if (hasLogo) {
                    yOffset += HEADER_PADDING;
                    canvas.drawBitmap(mLogo, (width - mLogo.getWidth()) / 2, yOffset, null);
                }
            }
        }

        return bitmap;
    }
}
