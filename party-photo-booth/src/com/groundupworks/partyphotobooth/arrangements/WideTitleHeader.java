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
