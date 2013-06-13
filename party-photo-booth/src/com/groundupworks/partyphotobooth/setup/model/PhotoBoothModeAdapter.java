/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.model;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothMode;

/**
 * Adapter for the {@link PhotoBoothMode} selection ui.
 * 
 * @author Benedict Lau
 */
public class PhotoBoothModeAdapter extends BaseSpinnerAdapter<PhotoBoothModeAdapter.Mode> {

    /**
     * Constructor.
     * 
     * @param context
     *            the {@link Context}.
     */
    public PhotoBoothModeAdapter(Context context) {
        super(context, createItems(context));
    }

    @Override
    protected void bindView(PhotoBoothModeAdapter.Mode item, View view) {
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setText(item.mDisplayName);
        ((TextView) view.findViewById(R.id.spinner_item_description)).setText(item.mDescription);
        view.findViewById(R.id.spinner_item_icon).setVisibility(View.GONE);
    }

    //
    // Private methods.
    //

    /**
     * Creates the list of {@link PhotoBoothModeAdapter.Mode} for the selection ui.
     * 
     * @param context
     *            the {@link Context}.
     * @return the list of {@link PhotoBoothModeAdapter.Mode}.
     */
    private static List<PhotoBoothModeAdapter.Mode> createItems(Context context) {
        List<PhotoBoothModeAdapter.Mode> modes = new ArrayList<PhotoBoothModeAdapter.Mode>();

        // Add self-serve mode.
        PhotoBoothModeAdapter.Mode selfServeMode = new PhotoBoothModeAdapter.Mode();
        selfServeMode.mMode = PhotoBoothMode.SELF_SERVE;
        selfServeMode.mDisplayName = context.getString(R.string.photo_booth_mode_adapter__self_serve_display_name);
        selfServeMode.mDescription = context.getString(R.string.photo_booth_mode_adapter__self_serve_description);
        modes.add(selfServeMode);

        // Add photographer mode.
        PhotoBoothModeAdapter.Mode photographerMode = new PhotoBoothModeAdapter.Mode();
        photographerMode.mMode = PhotoBoothMode.PHOTOGRAPHER;
        photographerMode.mDisplayName = context.getString(R.string.photo_booth_mode_adapter__photographer_display_name);
        photographerMode.mDescription = context.getString(R.string.photo_booth_mode_adapter__photographer_description);
        modes.add(photographerMode);

        return modes;
    }

    //
    // Public methods.
    //

    /**
     * Gets the {@link PhotoBoothMode} based on the item position.
     * 
     * @param position
     *            position of the item whose data we want within the adapter's data set.
     * @return the {@link PhotoBoothMode}.
     */
    public PhotoBoothMode getPhotoBoothMode(int position) {
        return getItem(position).mMode;
    }

    //
    // Package private classes.
    //

    /**
     * An internal model object used by the adapter representing a {@link PhotoBoothMode} and its data for the selection
     * ui.
     */
    static class Mode {

        /**
         * The {@link PhotoBoothMode}.
         */
        private PhotoBoothMode mMode;

        /**
         * The display name for the {@link PhotoBoothMode}.
         */
        private String mDisplayName;

        /**
         * The description of the {@link PhotoBoothMode}.
         */
        private String mDescription;
    }
}
