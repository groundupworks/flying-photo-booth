/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Helper for storing and retrieving user preferences.
 * 
 * @author Benedict Lau
 */
public class PreferencesHelper {

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
        STRIPES_GREEN
    }

    /**
     * Photo booth modes.
     */
    public enum PhotoBoothMode {

        /**
         * Self-serve mode uses front-facing camera and count down.
         */
        SELF_SERVE,

        /**
         * Photographer mode uses back-facing camera and no count down.
         */
        PHOTOGRAPHER
    };

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
    };

    /**
     * Key for the photo booth theme record.
     */
    private static final String KEY_PHOTO_BOOTH_THEME = "photoBoothTheme";

    /**
     * Key for the photo booth mode record.
     */
    private static final String KEY_PHOTO_BOOTH_MODE = "photoBoothMode";

    /**
     * Key for the photo strip arrangement record.
     */
    private static final String KEY_PHOTO_STRIP_ARRANGEMENT = "photoStripArrangement";

    /**
     * Key for the number of photos in a photo strip record.
     */
    private static final String KEY_PHOTO_STRIP_NUM_PHOTOS = "photoStripNumPhotos";

    /**
     * Key for the first line of the event title record.
     */
    private static final String KEY_EVENT_LINE_ONE = "eventLineOne";

    /**
     * Key for the second line of the event title record.
     */
    private static final String KEY_EVENT_LINE_TWO = "eventLineTwo";

    /**
     * Key for the event date record.
     */
    private static final String KEY_EVENT_DATE = "eventDate";

    /**
     * The default number of photos in a photo strip.
     */
    private static final int DEFAULT_NUM_PHOTOS = 4;

    /**
     * The default preferences for the event name and date.
     */
    private static final String DEFAULT_EVENT_PREFERENCE = "";

    //
    // Public methods.
    //

    /**
     * Stores the photo booth theme preference.
     * 
     * @param context
     *            the {@link Context}.
     * @param mode
     *            one of {@link PhotoBoothTheme}. Must not be null.
     */
    public void storePhotoBoothTheme(Context context, PhotoBoothTheme theme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_BOOTH_THEME, theme.name()).apply();
    }

    /**
     * Reads the photo booth theme preference.
     * 
     * @param context
     *            the {@link Context}.
     * @return the stored {@link PhotoBoothTheme}.
     */
    public PhotoBoothTheme getPhotoBoothTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String theme = preferences.getString(KEY_PHOTO_BOOTH_THEME, PhotoBoothTheme.STRIPES_BLUE.name());
        return PhotoBoothTheme.valueOf(theme);
    }

    /**
     * Stores the photo booth mode preference.
     * 
     * @param context
     *            the {@link Context}.
     * @param mode
     *            one of {@link PhotoBoothMode}. Must not be null.
     */
    public void storePhotoBoothMode(Context context, PhotoBoothMode mode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_BOOTH_MODE, mode.name()).apply();
    }

    /**
     * Reads the photo booth mode preference.
     * 
     * @param context
     *            the {@link Context}.
     * @return the stored {@link PhotoBoothMode}.
     */
    public PhotoBoothMode getPhotoBoothMode(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String mode = preferences.getString(KEY_PHOTO_BOOTH_MODE, PhotoBoothMode.SELF_SERVE.name());
        return PhotoBoothMode.valueOf(mode);
    }

    /**
     * Stores the photo strip arrangement preference.
     * 
     * @param context
     *            the {@link Context}.
     * @param arrangement
     *            one of {@link PhotoStripArrangement}. Must not be null.
     */
    public void storePhotoStripArrangement(Context context, PhotoStripArrangement arrangement) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_PHOTO_STRIP_ARRANGEMENT, arrangement.name()).apply();
    }

    /**
     * Reads the photo strip arrangement preference.
     * 
     * @param context
     *            the {@link Context}.
     * @return the stored {@link PhotoStripArrangement}.
     */
    public PhotoStripArrangement getPhotoStripArrangement(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String arrangement = preferences.getString(KEY_PHOTO_STRIP_ARRANGEMENT, PhotoStripArrangement.VERTICAL.name());
        return PhotoStripArrangement.valueOf(arrangement);
    }

    /**
     * Stores the number of photos in a photo strip.
     * 
     * @param context
     *            the {@link Context}.
     * @param numPhotos
     *            the number of photos.
     */
    public void storePhotoStripNumPhotos(Context context, int numPhotos) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putInt(KEY_PHOTO_STRIP_NUM_PHOTOS, numPhotos).apply();
    }

    /**
     * Reads the number of photos in a photo strip.
     * 
     * @param context
     *            the {@link Context}.
     * @return the stored number of photos.
     */
    public int getPhotoStripNumPhotos(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getInt(KEY_PHOTO_STRIP_NUM_PHOTOS, DEFAULT_NUM_PHOTOS);
    }

    /**
     * Stores the first line of the event title.
     * 
     * @param context
     *            the {@link Context}.
     * @param eventLineOne
     *            the first line of the event title; or an empty string. Pass null to clear.
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
     * @param context
     *            the {@link Context}.
     * @return the first line of the event title; or an empty string.
     */
    public String getEventLineOne(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_LINE_ONE, DEFAULT_EVENT_PREFERENCE);
    }

    /**
     * Stores the second line of the event title.
     * 
     * @param context
     *            the {@link Context}.
     * @param eventLineTwo
     *            the second line of the event title; or an empty string. Pass null to clear.
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
     * @param context
     *            the {@link Context}.
     * @return the second line of the event title; or an empty string.
     */
    public String getEventLineTwo(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_LINE_TWO, DEFAULT_EVENT_PREFERENCE);
    }

    /**
     * Stores the date of the event.
     * 
     * @param context
     *            the {@link Context}.
     * @param eventDate
     *            the event date; or an empty string. Pass null to clear.
     */
    public void storeEventDate(Context context, String eventDate) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(KEY_EVENT_DATE, eventDate).apply();
    }

    /**
     * Reads the date of the event.
     * 
     * @param context
     *            the {@link Context}.
     * @return the event date; or an empty string.
     */
    public String getEventDate(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(KEY_EVENT_DATE, DEFAULT_EVENT_PREFERENCE);
    }
}
