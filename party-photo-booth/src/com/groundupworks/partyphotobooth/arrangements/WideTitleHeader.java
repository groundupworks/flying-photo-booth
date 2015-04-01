/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;
import android.graphics.Typeface;

/**
 * An {@link IPhotoStripHeader} implementation that prints the event title in one line followed by the event date.
 *
 * @author Benedict Lau
 */
public class WideTitleHeader extends BaseTitleHeader {

    /**
     * Constructor.
     *
     * @param title the title of the event; or null to hide.
     * @param date  the date of the event; or null to hide.
     * @param logo  the event logo; or null to hide.
     * @param font  the font.
     */
    public WideTitleHeader(String title, String date, Bitmap logo, Typeface font) {
        super(title, null, date, logo, font);
    }
}
