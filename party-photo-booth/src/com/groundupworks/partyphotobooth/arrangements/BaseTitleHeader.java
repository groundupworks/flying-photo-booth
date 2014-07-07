/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
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
     * Constructor.
     *
     * @param lineOne the first line of the event title; or null to hide.
     * @param lineTwo the second line of the event title; or null to hide.
     * @param date    the date of the event; or null to hide.
     */
    public BaseTitleHeader(String lineOne, String lineTwo, String date) {
        mLineOne = lineOne;
        mLineTwo = lineTwo;
        mDate = date;
    }

    @Override
    public Bitmap getHeaderBitmap(int width) {
        Bitmap bitmap = null;

        boolean hasLineOne = TextHelper.isValid(mLineOne);
        boolean hasLineTwo = TextHelper.isValid(mLineTwo);
        boolean hasDate = TextHelper.isValid(mDate);

        if (hasLineOne || hasLineTwo || hasDate) {
            // Configure paint for drawing text.
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTypeface(Typeface.SANS_SERIF);
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

            // Proceed only if the optimal text size is valid.
            if (optimalTextSize < Float.MAX_VALUE) {
                // Create header bitmap.
                bitmap = Bitmap.createBitmap(width, height, ImageHelper.BITMAP_CONFIG);
                if (bitmap != null) {
                    Canvas canvas = new Canvas(bitmap);
                    int yOffset = HEADER_PADDING;
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
            }
        }

        return bitmap;
    }
}
