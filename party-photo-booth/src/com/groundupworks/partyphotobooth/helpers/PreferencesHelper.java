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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Date;

/**
 * Helper for storing and retrieving user preferences.
 *
 * @author Benedict Lau
 */
public class PreferencesHelper {

    /**
     * Photo booth modes.
     */
    public enum PhotoBoothMode {

        /**
         * Self-serve mode uses front-facing camera and count down.
         */
        SELF_SERVE,

        /**
         * Automatic mode uses the front-facing camera and auto-triggers count down after capturing
         * the first frame. Discard is disabled in this mode.
         */
        AUTOMATIC,

        /**
         * Photographer mode uses back-facing camera and no count down.
         */
        PHOTOGRAPHER;
    }

    /**
     * Photo booth themes.
     */
    public enum PhotoBoothTheme {

        /**
         * Blue stripes.
         */
        STRIPES_BLUE,

        /**
         * Pink stripes.
         */
        STRIPES_PINK,

        /**
         * Orange stripes.
         */
        STRIPES_ORANGE,

        /**
         * Green stripes.
         */
        STRIPES_GREEN,

        /**
         * Minimalist theme.
         */
        MINIMALIST,

        /**
         * Vintage theme.
         */
        VINTAGE,

        /**
         * Carbon theme.
         */
        CARBON
    }

    /**
     * Photo strip templates.
     */
    public enum PhotoStripTemplate {

        /**
         * Single photo.
         */
        SINGLE(PhotoStripArrangement.VERTICAL, 1),

        /**
         * Vertical arrangement of 2 photos.
         */
        VERTICAL_2(PhotoStripArrangement.VERTICAL, 2),

        /**
         * Horizontal arrangement of 2 photos.
         */
        HORIZONTAL_2(PhotoStripArrangement.HORIZONTAL, 2),

        /**
         * Vertical arrangement of 3 photos.
         */
        VERTICAL_3(PhotoStripArrangement.VERTICAL, 3),

        /**
         * Horizontal arrangement of 3 photos.
         */
        HORIZONTAL_3(PhotoStripArrangement.HORIZONTAL, 3),

        /**
         * Vertical arrangement of 4 photos.
         */
        VERTICAL_4(PhotoStripArrangement.VERTICAL, 4),

        /**
         * Horizontal arrangement of 4 photos.
         */
        HORIZONTAL_4(PhotoStripArrangement.HORIZONTAL, 4),

        /**
         * Box arrangement of 4 photos.
         */
        BOX_4(PhotoStripArrangement.BOX, 4);

        /**
         * The arrangement of the photo strip.
         */
        private PhotoStripArrangement mArrangement;

        /**
         * The number of photos in the photo strip.
         */
        private int mNumPhotos;

        /**
         * Constructor.
         *
         * @param arrangement the arrangement of the photo strip.
         * @param numPhotos   the number of photos in the photo strip.
         */
        private PhotoStripTemplate(PhotoStripArrangement arrangement, int numPhotos) {
            mArrangement = arrangement;
            mNumPhotos = numPhotos;
        }

        /**
         * Gets the arrangement of the photo strip.
         *
         * @return the {@link PhotoStripArrangement}.
         */
        public PhotoStripArrangement getArrangement() {
            return mArrangement;
        }

        /**
         * Gets the number of photos in the photo strip.
         *
         * @return the stored number of photos.
         */
        public int getNumPhotos() {
            return mNumPhotos;
        }
    }

    /**
     * Photo strip arrangements.
     */
    public enum PhotoStripArrangement {

        /**
         * Vertical arrangement of photos.
         */
        VERTICAL,

        /**
         * Horizontal arrangement of photos.
         */
        HORIZONTAL,

        /**
         * Box arrangement of photos.
         */
        BOX
    }

    /**
     * Preference value to hide the event date.
     */
    public static final long EVENT_DATE_HIDDEN = -1L;

    /**
     * Key for the photo booth mode record.
     */
    private static final String KEY_PHOTO_BOOTH_MODE = "photoBoothMode";

    /**
     * Key for the photo booth theme record.
     */
    private static final String KEY_PHOTO_BOOTH_THEME = "photoBoothTheme";

    /**
     * Key for the photo strip template record.
     */
    private static final String KEY_PHOTO_STRIP_TEMPLATE = "photoStripTemplate";

    /**
     * Key for the first line of the event title record.
     */
    private static final String KEY_EVENT_LINE_ONE = "eventLineOne";

    /**
     * Key for the second line of the event title record.
     */
    private static final String KEY_EVENT_LINE_TWO = "eventLineTwo";

    /**
     * Key for the event logo uri record.
     */
    private static final String KEY_EVENT_LOGO_URI = "eventLogoUri";

    /**
     * Key for the event date record.
     */
    private static final String KEY_EVENT_DATE = "eventDate";

    /**
     * Key for whether enabled share services are shown in a notice screen.
     */
    private static final String KEY_NOTICE_ENABLED = "noticeEnabled";

    /**
     * The default preferences for the event title.
     */
    private static final String DEFAULT_EVENT_TITLE_PREFERENCE = "";

    /**
     * The default preferences for the event logo uri.
     */
    private static final String DEFAULT_EVENT_LOGO_URI_PREFERENCE = "";

    //
    // Public methods.
    //

    /**
     * Stores the photo booth mode preference.
     *
     * @param context the {@link Context}.
     * @param mode    one of {@link PhotoBoothMode}. Must not be null.
     */
    public void storePhotoBoothMode(Context context, PhotoBoothMode mode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_BOOTH_MODE, mode.name()).apply();
    }

    /**
     * Reads the photo booth mode preference.
     *
     * @param context the {@link Context}.
     * @return the stored {@link PhotoBoothMode}.
     */
    public PhotoBoothMode getPhotoBoothMode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String mode = preferences.getString(KEY_PHOTO_BOOTH_MODE, PhotoBoothMode.SELF_SERVE.name());
        return PhotoBoothMode.valueOf(mode);
    }

    /**
     * Stores the photo booth theme preference.
     *
     * @param context the {@link Context}.
     * @param theme   one of {@link PhotoBoothTheme}. Must not be null.
     */
    public void storePhotoBoothTheme(Context context, PhotoBoothTheme theme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_BOOTH_THEME, theme.name()).apply();
    }

    /**
     * Reads the photo booth theme preference.
     *
     * @param context the {@link Context}.
     * @return the stored {@link PhotoBoothTheme}.
     */
    public PhotoBoothTheme getPhotoBoothTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String theme = preferences.getString(KEY_PHOTO_BOOTH_THEME, PhotoBoothTheme.STRIPES_BLUE.name());
        return PhotoBoothTheme.valueOf(theme);
    }

    /**
     * Stores the photo strip template preference.
     *
     * @param context  the {@link Context}.
     * @param template one of {@link PhotoStripTemplate}. Must not be null.
     */
    public void storePhotoStripTemplate(Context context, PhotoStripTemplate template) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_STRIP_TEMPLATE, template.name()).apply();
    }

    /**
     * Reads the photo strip template preference.
     *
     * @param context the {@link Context}.
     * @return the stored {@link PhotoStripTemplate}.
     */
    public PhotoStripTemplate getPhotoStripTemplate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String template = preferences.getString(KEY_PHOTO_STRIP_TEMPLATE, PhotoStripTemplate.VERTICAL_3.name());
        return PhotoStripTemplate.valueOf(template);
    }

    /**
     * Stores the first line of the event title.
     *
     * @param context      the {@link Context}.
     * @param eventLineOne the first line of the event title; or an empty string. Pass null to clear.
     */
    public void storeEventLineOne(Context context, String eventLineOne) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        if (eventLineOne != null && eventLineOne.length() > 0) {
            editor.putString(KEY_EVENT_LINE_ONE, eventLineOne).apply();
        } else {
            editor.remove(KEY_EVENT_LINE_ONE).apply();
        }
    }

    /**
     * Reads the first line of the event title.
     *
     * @param context the {@link Context}.
     * @return the first line of the event title; or an empty string.
     */
    public String getEventLineOne(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_LINE_ONE, DEFAULT_EVENT_TITLE_PREFERENCE);
    }

    /**
     * Stores the second line of the event title.
     *
     * @param context      the {@link Context}.
     * @param eventLineTwo the second line of the event title; or an empty string. Pass null to clear.
     */
    public void storeEventLineTwo(Context context, String eventLineTwo) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        if (eventLineTwo != null && eventLineTwo.length() > 0) {
            editor.putString(KEY_EVENT_LINE_TWO, eventLineTwo).apply();
        } else {
            editor.remove(KEY_EVENT_LINE_TWO).apply();
        }
    }

    /**
     * Reads the second line of the event title.
     *
     * @param context the {@link Context}.
     * @return the second line of the event title; or an empty string.
     */
    public String getEventLineTwo(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_LINE_TWO, DEFAULT_EVENT_TITLE_PREFERENCE);
    }

    /**
     * Stores the uri to the event logo image.
     *
     * @param context the {@link Context}.
     * @param uri     the uri to the event logo image; or an empty string. Pass null to clear.
     */
    public void storeEventLogoUri(Context context, String uri) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        if (uri != null && uri.length() > 0) {
            editor.putString(KEY_EVENT_LOGO_URI, uri).apply();
        } else {
            editor.remove(KEY_EVENT_LOGO_URI).apply();
        }
    }

    /**
     * Reads the uri to the event logo image.
     *
     * @param context the {@link Context}.
     * @return the uri to the event logo image; or an empty string.
     */
    public String getEventLogoUri(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_LOGO_URI, DEFAULT_EVENT_LOGO_URI_PREFERENCE);
    }

    /**
     * Stores the date of the event in milliseconds since Jan. 1, 1970, midnight GMT.
     *
     * @param context   the {@link Context}.
     * @param eventDate the event date in milliseconds. Pass {@link PreferencesHelper#EVENT_DATE_HIDDEN} to hide event date.
     */
    public void storeEventDate(Context context, long eventDate) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putLong(KEY_EVENT_DATE, eventDate).apply();
    }

    /**
     * Reads the date of the event in milliseconds since Jan. 1, 1970, midnight GMT.
     *
     * @param context the {@link Context}.
     * @return the event date in milliseconds; or {@link PreferencesHelper#EVENT_DATE_HIDDEN} if hidden. The current
     * date is returned if no record is stored.
     */
    public long getEventDate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getLong(KEY_EVENT_DATE, new Date().getTime());
    }

    /**
     * Stores whether enabled share services are shown in a notice screen.
     *
     * @param context   the {@link Context}.
     * @param isEnabled true to enable; false otherwise.
     */
    public void storeNoticeEnabled(Context context, boolean isEnabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putBoolean(KEY_NOTICE_ENABLED, isEnabled).apply();
    }

    /**
     * Reads whether enabled share services are shown in a notice screen.
     *
     * @param context the {@link Context}.
     * @return true if enabled; false otherwise.
     */
    public boolean getNoticeEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getBoolean(KEY_NOTICE_ENABLED, false);
    }
}
