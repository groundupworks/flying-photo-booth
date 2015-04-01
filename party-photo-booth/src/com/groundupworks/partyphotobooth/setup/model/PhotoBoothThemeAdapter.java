/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.model;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme;
import com.groundupworks.partyphotobooth.themes.Theme;

/**
 * Adapter for the {@link PhotoBoothTheme} selection ui.
 *
 * @author Benedict Lau
 */
public class PhotoBoothThemeAdapter extends BaseSpinnerAdapter<Theme> {

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     */
    public PhotoBoothThemeAdapter(Context context) {
        super(context, createItems(context.getApplicationContext()));
    }

    @Override
    protected void bindView(Theme item, View view) {
        // Hide unused views.
        view.findViewById(R.id.spinner_item_description).setVisibility(View.GONE);

        // Bind data.
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setText(item.getDisplayName());
        int iconRes = item.getIconResource();
        if (iconRes != Theme.RESOURCE_NONE) {
            ((ImageView) view.findViewById(R.id.spinner_item_icon)).setImageResource(iconRes);
        } else {
            ((ImageView) view.findViewById(R.id.spinner_item_icon)).setImageDrawable(null);
        }
    }

    //
    // Private methods.
    //

    /**
     * Creates the list of {@link com.groundupworks.partyphotobooth.themes.Theme} for the selection ui.
     *
     * @param context the {@link Context}.
     * @return the {@link SparseArray} of {@link com.groundupworks.partyphotobooth.themes.Theme}.
     */
    private static SparseArray<Theme> createItems(Context context) {
        SparseArray<Theme> themes = new SparseArray<>();
        themes.put(PhotoBoothTheme.STRIPES_BLUE.ordinal(), new Theme.Blue(context));
        themes.put(PhotoBoothTheme.STRIPES_PINK.ordinal(), new Theme.Pink(context));
        themes.put(PhotoBoothTheme.STRIPES_ORANGE.ordinal(), new Theme.Orange(context));
        themes.put(PhotoBoothTheme.STRIPES_GREEN.ordinal(), new Theme.Green(context));
        themes.put(PhotoBoothTheme.MINIMALIST.ordinal(), new Theme.Minimalist(context));
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
        return getItem(position).getTheme();
    }
}
