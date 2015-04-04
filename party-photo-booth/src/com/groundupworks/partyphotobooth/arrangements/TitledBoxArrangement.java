/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Typeface;

import com.groundupworks.lib.photobooth.arrangements.BoxArrangement;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

/**
 * Box arrangement with title and date as the header.
 *
 * @author Benedict Lau
 */
public class TitledBoxArrangement extends BoxArrangement {

    /**
     * The title of the event.
     */
    private String mTitle = null;

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
    public TitledBoxArrangement(String lineOne, String lineTwo, String date, Bitmap logo, Typeface font) {
        mTitle = TextHelper.joinStrings(lineOne, lineTwo);
        mDate = date;
        mLogo = logo;
        mFont = font;
    }

    @Override
    protected Bitmap getHeader(int width) {
        IPhotoStripHeader header = new WideTitleHeader(mTitle, mDate, mLogo, mFont);
        return header.getHeaderBitmap(width);
    }
}
