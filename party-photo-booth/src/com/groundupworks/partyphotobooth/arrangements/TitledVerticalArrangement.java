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
    public TitledVerticalArrangement(String lineOne, String lineTwo, String date, Bitmap logo, Typeface font) {
        mLineOne = lineOne;
        mLineTwo = lineTwo;
        mDate = date;
        mLogo = logo;
        mFont = font;
    }

    @Override
    protected Bitmap getHeader(int width) {
        IPhotoStripHeader header = new BaseTitleHeader(mLineOne, mLineTwo, mDate, mLogo, mFont);
        return header.getHeaderBitmap(width);
    }
}
