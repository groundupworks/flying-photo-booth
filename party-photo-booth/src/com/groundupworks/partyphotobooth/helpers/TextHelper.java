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
package com.groundupworks.partyphotobooth.helpers;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.DateFormat;

import java.util.Date;

/**
 * Helper class for rendering text.
 *
 * @author Benedict Lau
 */
public class TextHelper {

    /**
     * The minimum text size that can be returned by {@link #getFittedTextSize(String, int, int, Paint)}.
     */
    private static final float MIN_TEXT_SIZE = 12f;

    /**
     * The factor to fill a bounding box with text.
     */
    private static final float FILL_FACTOR = 0.8f;

    /**
     * The space character.
     */
    private static final String CHAR_SPACE = " ";

    //
    // Public methods.
    //

    /**
     * Converts a date in milliseconds since Jan. 1, 1970, midnight GMT to a string according to the system locale
     * settings.
     *
     * @param context the {@link Context}.
     * @param date    the date of the event in milliseconds.
     * @return the date as a string.
     */
    public static String getDateString(Context context, long date) {
        Date eventDate = new Date(date);
        return DateFormat.getMediumDateFormat(context).format(eventDate);
    }

    /**
     * Gets the optimal text size to use for fitting text inside a bounding box of fixed size.
     *
     * @param text      the text to print.
     * @param fitWidth  the width of the bounding box to fit into.
     * @param fitHeight the height of the bounding box to fit into.
     * @param paint     the {@link Paint} used to render the text.
     * @return the text size to be used in order for the text to fit in the bounding box.
     */
    public static float getFittedTextSize(String text, int fitWidth, int fitHeight, Paint paint) {
        // Determine width and height based on the current text size associated with the paint.
        float baseTextSize = paint.getTextSize();
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float baseWidth = (float) (bounds.right - bounds.left);
        float baseHeight = (float) (bounds.bottom - bounds.top);

        // Determine scale factor to the base text size.
        float scaleX = 0f;
        float scaleY = 0f;
        if (baseWidth > 0f && baseHeight > 0f) {
            scaleX = (float) fitWidth / baseWidth;
            scaleY = (float) fitHeight / baseHeight;
        }
        float scaleFactor = Math.min(scaleX, scaleY);

        return Math.max(baseTextSize * scaleFactor * FILL_FACTOR, MIN_TEXT_SIZE);
    }

    /**
     * Joins two strings.
     *
     * @param stringOne the first string; or null if none.
     * @param stringTwo the second string; or null if none.
     * @return the joined string; or null if both source strings are invalid.
     */
    public static String joinStrings(String stringOne, String stringTwo) {
        String returnString = null;
        StringBuilder stringBuilder = new StringBuilder();
        if (isValid(stringOne)) {
            stringBuilder.append(stringOne);
        }
        if (isValid(stringOne) && isValid(stringTwo)) {
            stringBuilder.append(CHAR_SPACE);
        }
        if (isValid(stringTwo)) {
            stringBuilder.append(stringTwo);
        }

        String joinedString = stringBuilder.toString();
        if (isValid(joinedString)) {
            returnString = joinedString;
        }

        return returnString;
    }

    /**
     * Checks if a string is valid.
     *
     * @param string the string.
     * @return true if the string is non-null or has a length > 0; false otherwise.
     */
    public static boolean isValid(String string) {
        return string != null && string.length() > 0;
    }
}
