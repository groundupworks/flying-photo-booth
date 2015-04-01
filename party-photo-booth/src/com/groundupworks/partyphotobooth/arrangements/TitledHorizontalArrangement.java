/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.groundupworks.lib.photobooth.arrangements.BaseArrangement;
import com.groundupworks.lib.photobooth.arrangements.HorizontalArrangement;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

/**
 * Horizontal arrangement with title and date as the header.
 *
 * @author Benedict Lau
 */
public class TitledHorizontalArrangement extends HorizontalArrangement {

    /**
     * The threshold width to switch from using the {@link BaseTitleHeader} to using the {@link WideTitleHeader}. Use
     * {@link WideTitleHeader} if there is more than one frame in the horizontal dimension.
     */
    private static final int WIDE_TITLE_THRESHOLD = ImageHelper.IMAGE_SIZE + BaseArrangement.PHOTO_STRIP_PANEL_PADDING
            * 2;

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
    public TitledHorizontalArrangement(String lineOne, String lineTwo, String date, Bitmap logo, Typeface font) {
        mLineOne = lineOne;
        mLineTwo = lineTwo;
        mDate = date;
        mLogo = logo;
        mFont = font;
    }

    @Override
    protected Bitmap getHeader(int width) {
        IPhotoStripHeader header;
        if (width > WIDE_TITLE_THRESHOLD) {
            String title = TextHelper.joinStrings(mLineOne, mLineTwo);
            header = new WideTitleHeader(title, mDate, mLogo, mFont);
        } else {
            header = new BaseTitleHeader(mLineOne, mLineTwo, mDate, mLogo, mFont);
        }
        return header.getHeaderBitmap(width);
    }
}
