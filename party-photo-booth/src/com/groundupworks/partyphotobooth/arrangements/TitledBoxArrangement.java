/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;

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
     * Constructor.
     *
     * @param lineOne the first line of the event title; or null to hide.
     * @param lineTwo the second line of the event title; or null to hide.
     * @param date    the date of the event; or null to hide.
     * @param logo    the event logo; or null to hide.
     */
    public TitledBoxArrangement(String lineOne, String lineTwo, String date, Bitmap logo) {
        mTitle = TextHelper.joinStrings(lineOne, lineTwo);
        mDate = date;
        mLogo = logo;
    }

    @Override
    protected Bitmap getHeader(int width) {
        IPhotoStripHeader header = new WideTitleHeader(mTitle, mDate, mLogo);
        return header.getHeaderBitmap(width);
    }
}
