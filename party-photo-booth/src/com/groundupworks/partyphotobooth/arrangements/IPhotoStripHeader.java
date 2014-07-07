/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.arrangements;

import android.graphics.Bitmap;

/**
 * Interface for a photo strip header.
 *
 * @author Benedict Lau
 */
public interface IPhotoStripHeader {

    /**
     * Gets the header bitmap to be drawn.
     *
     * @param width the width of the header bitmap.
     * @return the header bitmap.
     */
    Bitmap getHeaderBitmap(int width);
}