/*
 * Copyright (C) 2015 Benedict Lau
 *
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.themes;

import android.content.Context;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;

/**
 * The {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme} model.
 */
public abstract class Theme {

    /**
     * No icon or background resource.
     */
    public static final int RESOURCE_NONE = Integer.MIN_VALUE;

    /**
     * The {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected PreferencesHelper.PhotoBoothTheme mTheme;

    /**
     * The display name for the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected String mDisplayName;

    /**
     * The icon resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected int mIconResource;

    /**
     * The background resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected int mBackgroundResource;

    /**
     * Gets the {@link com.groundupworks.partyphotobooth.themes.Theme} from the theme name.
     *
     * @param context   the {@link android.content.Context}.
     * @param themeName the theme name.
     * @return the {@link com.groundupworks.partyphotobooth.themes.Theme}.
     */
    public static Theme from(Context context, PreferencesHelper.PhotoBoothTheme themeName) {
        Theme theme = null;
        Context appContext = context.getApplicationContext();
        if (PreferencesHelper.PhotoBoothTheme.STRIPES_BLUE.equals(themeName)) {
            theme = new Blue(appContext);
        } else if (PreferencesHelper.PhotoBoothTheme.STRIPES_PINK.equals(themeName)) {
            theme = new Pink(appContext);
        } else if (PreferencesHelper.PhotoBoothTheme.STRIPES_ORANGE.equals(themeName)) {
            theme = new Orange(appContext);
        } else if (PreferencesHelper.PhotoBoothTheme.STRIPES_GREEN.equals(themeName)) {
            theme = new Green(appContext);
        } else if (PreferencesHelper.PhotoBoothTheme.MINIMALIST.equals(themeName)) {
            theme = new Minimalist(appContext);
        }
        return theme;
    }

    /**
     * Gets the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the theme.
     */
    public PreferencesHelper.PhotoBoothTheme getTheme() {
        return mTheme;
    }

    /**
     * Gets the display name for the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the display name.
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Gets the icon resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the icon resource; or {@link #RESOURCE_NONE}.
     */
    public int getIconResource() {
        return mIconResource;
    }

    /**
     * Gets the background resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the background resource; or {@link #RESOURCE_NONE}.
     */
    public int getBackgroundResource() {
        return mBackgroundResource;
    }

    /**
     * Blue stripes theme.
     */
    public static class Blue extends Theme {

        public Blue(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_BLUE;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__blue_display_name);
            mIconResource = R.drawable.tile_blue;
            mBackgroundResource = R.drawable.bitmap_tile_blue;
        }
    }

    /**
     * Pink stripes theme.
     */
    public static class Pink extends Theme {

        public Pink(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_PINK;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__pink_display_name);
            mIconResource = R.drawable.tile_pink;
            mBackgroundResource = R.drawable.bitmap_tile_pink;
        }
    }

    /**
     * Orange stripes theme.
     */
    public static class Orange extends Theme {

        public Orange(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_ORANGE;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__orange_display_name);
            mIconResource = R.drawable.tile_orange;
            mBackgroundResource = R.drawable.bitmap_tile_orange;
        }
    }

    /**
     * Green stripes theme.
     */
    public static class Green extends Theme {

        public Green(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_GREEN;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__green_display_name);
            mIconResource = R.drawable.tile_green;
            mBackgroundResource = R.drawable.bitmap_tile_green;
        }
    }

    /**
     * Minimalist theme.
     */
    public static class Minimalist extends Theme {

        public Minimalist(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.MINIMALIST;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__minimalist_display_name);
            mIconResource = RESOURCE_NONE;
            mBackgroundResource = RESOURCE_NONE;
        }
    }
}
