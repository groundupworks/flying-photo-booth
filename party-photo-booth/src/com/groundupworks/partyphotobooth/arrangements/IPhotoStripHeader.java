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
