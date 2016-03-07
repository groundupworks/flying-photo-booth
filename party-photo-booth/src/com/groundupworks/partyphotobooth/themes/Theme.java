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
package com.groundupworks.partyphotobooth.themes;

import android.content.Context;
import android.graphics.Typeface;

import com.groundupworks.lib.photobooth.filters.BlackAndWhiteFilter;
import com.groundupworks.lib.photobooth.filters.SepiaFilter;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
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
    protected PreferencesHelper.PhotoBoothTheme mTheme = PreferencesHelper.PhotoBoothTheme.MINIMALIST;

    /**
     * The display name for the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected String mDisplayName = null;

    /**
     * The icon resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected int mIconResource = RESOURCE_NONE;

    /**
     * The background resource of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected int mBackgroundResource = RESOURCE_NONE;

    /**
     * The font of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected Typeface mFont = Typeface.SANS_SERIF;

    /**
     * The image filter of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     */
    protected ImageHelper.ImageFilter mImageFilter = null;

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
        } else if (PreferencesHelper.PhotoBoothTheme.VINTAGE.equals(themeName)) {
            theme = new Vintage(appContext);
        } else if (PreferencesHelper.PhotoBoothTheme.CARBON.equals(themeName)) {
            theme = new Carbon(appContext);
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
     * Gets the font of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the font.
     */
    public Typeface getFont() {
        return mFont;
    }

    /**
     * Gets the image filter of the {@link com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme}.
     *
     * @return the image filter; or {@code null}.
     */
    public ImageHelper.ImageFilter getFilter() {
        return mImageFilter;
    }

    /**
     * Blue stripes theme.
     */
    private static class Blue extends Theme {

        private Blue(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_BLUE;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__blue_display_name);
            mIconResource = R.drawable.tile_blue;
            mBackgroundResource = R.drawable.bitmap_tile_blue;
            mFont = Typeface.SANS_SERIF;
            mImageFilter = null;
        }
    }

    /**
     * Pink stripes theme.
     */
    private static class Pink extends Theme {

        private Pink(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_PINK;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__pink_display_name);
            mIconResource = R.drawable.tile_pink;
            mBackgroundResource = R.drawable.bitmap_tile_pink;
            mFont = Typeface.SANS_SERIF;
            mImageFilter = null;
        }
    }

    /**
     * Orange stripes theme.
     */
    private static class Orange extends Theme {

        private Orange(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_ORANGE;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__orange_display_name);
            mIconResource = R.drawable.tile_orange;
            mBackgroundResource = R.drawable.bitmap_tile_orange;
            mFont = Typeface.SANS_SERIF;
            mImageFilter = null;
        }
    }

    /**
     * Green stripes theme.
     */
    private static class Green extends Theme {

        private Green(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.STRIPES_GREEN;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__green_display_name);
            mIconResource = R.drawable.tile_green;
            mBackgroundResource = R.drawable.bitmap_tile_green;
            mFont = Typeface.SANS_SERIF;
            mImageFilter = null;
        }
    }

    /**
     * Minimalist theme.
     */
    private static class Minimalist extends Theme {

        private Minimalist(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.MINIMALIST;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__minimalist_display_name);
            mIconResource = RESOURCE_NONE;
            mBackgroundResource = RESOURCE_NONE;
            mFont = Typeface.SANS_SERIF;
            mImageFilter = null;
        }
    }

    /**
     * Vintage theme.
     */
    private static class Vintage extends Theme {

        private Vintage(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.VINTAGE;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__vintage_display_name);
            mIconResource = RESOURCE_NONE;
            mBackgroundResource = RESOURCE_NONE;
            mFont = Typeface.SERIF;
            mImageFilter = new SepiaFilter();
        }
    }

    /**
     * Carbon theme.
     */
    private static class Carbon extends Theme {

        private Carbon(Context context) {
            mTheme = PreferencesHelper.PhotoBoothTheme.CARBON;
            mDisplayName = context.getString(R.string.photo_booth_theme_adapter__carbon_display_name);
            mIconResource = RESOURCE_NONE;
            mBackgroundResource = RESOURCE_NONE;
            mFont = Typeface.MONOSPACE;
            mImageFilter = new BlackAndWhiteFilter();
        }
    }
}
