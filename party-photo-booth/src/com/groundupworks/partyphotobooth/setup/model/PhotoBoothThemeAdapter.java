/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme;

/**
 * Adapter for the {@link PhotoBoothTheme} selection ui.
 *
 * @author Benedict Lau
 */
public class PhotoBoothThemeAdapter extends BaseSpinnerAdapter<PhotoBoothThemeAdapter.Theme> {

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     */
    public PhotoBoothThemeAdapter(Context context) {
        super(context, createItems(context));
    }

    @Override
    protected void bindView(PhotoBoothThemeAdapter.Theme item, View view) {
        // Hide unused views.
        view.findViewById(R.id.spinner_item_description).setVisibility(View.GONE);

        // Bind data.
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setText(item.mDisplayName);
        ((ImageView) view.findViewById(R.id.spinner_item_icon)).setImageDrawable(item.mIcon);
    }

    //
    // Private methods.
    //

    /**
     * Creates the list of {@link PhotoBoothThemeAdapter.Theme} for the selection ui.
     *
     * @param context the {@link Context}.
     * @return the {@link SparseArray} of {@link PhotoBoothThemeAdapter.Theme}.
     */
    private static SparseArray<PhotoBoothThemeAdapter.Theme> createItems(Context context) {
        SparseArray<PhotoBoothThemeAdapter.Theme> themes = new SparseArray<PhotoBoothThemeAdapter.Theme>();

        // Add blue stripes theme.
        PhotoBoothThemeAdapter.Theme blue = new PhotoBoothThemeAdapter.Theme();
        blue.mTheme = PhotoBoothTheme.STRIPES_BLUE;
        blue.mDisplayName = context.getString(R.string.photo_booth_theme_adapter__blue_display_name);
        blue.mIcon = context.getResources().getDrawable(R.drawable.tile_blue);
        themes.put(PhotoBoothTheme.STRIPES_BLUE.ordinal(), blue);

        // Add pink stripes theme.
        PhotoBoothThemeAdapter.Theme pink = new PhotoBoothThemeAdapter.Theme();
        pink.mTheme = PhotoBoothTheme.STRIPES_PINK;
        pink.mDisplayName = context.getString(R.string.photo_booth_theme_adapter__pink_display_name);
        pink.mIcon = context.getResources().getDrawable(R.drawable.tile_pink);
        themes.put(PhotoBoothTheme.STRIPES_PINK.ordinal(), pink);

        // Add orange stripes theme.
        PhotoBoothThemeAdapter.Theme orange = new PhotoBoothThemeAdapter.Theme();
        orange.mTheme = PhotoBoothTheme.STRIPES_ORANGE;
        orange.mDisplayName = context.getString(R.string.photo_booth_theme_adapter__orange_display_name);
        orange.mIcon = context.getResources().getDrawable(R.drawable.tile_orange);
        themes.put(PhotoBoothTheme.STRIPES_ORANGE.ordinal(), orange);

        // Add green stripes theme.
        PhotoBoothThemeAdapter.Theme green = new PhotoBoothThemeAdapter.Theme();
        green.mTheme = PhotoBoothTheme.STRIPES_GREEN;
        green.mDisplayName = context.getString(R.string.photo_booth_theme_adapter__green_display_name);
        green.mIcon = context.getResources().getDrawable(R.drawable.tile_green);
        themes.put(PhotoBoothTheme.STRIPES_GREEN.ordinal(), green);

        // Add minimalist theme.
        PhotoBoothThemeAdapter.Theme minimalist = new PhotoBoothThemeAdapter.Theme();
        minimalist.mTheme = PhotoBoothTheme.MINIMALIST;
        minimalist.mDisplayName = context.getString(R.string.photo_booth_theme_adapter__minimalist_display_name);
        minimalist.mIcon = null;
        themes.put(PhotoBoothTheme.MINIMALIST.ordinal(), minimalist);

        return themes;
    }

    //
    // Public methods.
    //

    /**
     * Gets the {@link PhotoBoothTheme} based on the item position.
     *
     * @param position position of the item whose data we want within the adapter's data set.
     * @return the {@link PhotoBoothTheme}.
     */
    public PhotoBoothTheme getPhotoBoothTheme(int position) {
        return getItem(position).mTheme;
    }

    //
    // Package private classes.
    //

    /**
     * An internal model object used by the adapter representing a {@link PhotoBoothTheme} and its data for the
     * selection ui.
     */
    static class Theme {

        /**
         * The {@link PhotoBoothTheme}.
         */
        private PhotoBoothTheme mTheme;

        /**
         * The display name for the {@link PhotoBoothTheme}.
         */
        private String mDisplayName;

        /**
         * The icon of the {@link PhotoBoothTheme}.
         */
        private Drawable mIcon;
    }
}
