/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

/**
 * An {@link IPhotoStripHeader} implementation that prints the event title in one line followed by the event date.
 * 
 * @author Benedict Lau
 */
public class WideTitleHeader extends BaseTitleHeader {

    /**
     * Constructor.
     * 
     * @param title
     *            the title of the event; or null to hide.
     * @param date
     *            the date of the event; or null to hide.
     */
    public WideTitleHeader(String title, String date) {
        super(title, null, date);
    }
}
