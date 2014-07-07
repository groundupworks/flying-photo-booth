/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;

import com.groundupworks.lib.photobooth.arrangements.VerticalArrangement;

/**
 * Vertical arrangement with title and date as the header.
 *
 * @author Benedict Lau
 */
public class TitledVerticalArrangement extends VerticalArrangement {

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
    public TitledVerticalArrangement(String lineOne, String lineTwo, String date) {
        mLineOne = lineOne;
        mLineTwo = lineTwo;
        mDate = date;
    }

    @Override
    protected Bitmap getHeader(int width) {
        IPhotoStripHeader header = new BaseTitleHeader(mLineOne, mLineTwo, mDate);
        return header.getHeaderBitmap(width);
    }
}
