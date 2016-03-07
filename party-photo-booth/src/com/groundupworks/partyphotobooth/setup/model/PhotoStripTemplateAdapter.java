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
package com.groundupworks.partyphotobooth.setup.model;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoStripTemplate;

/**
 * Adapter for the {@link PhotoStripTemplate} selection ui.
 *
 * @author Benedict Lau
 */
public class PhotoStripTemplateAdapter extends BaseSpinnerAdapter<PhotoStripTemplateAdapter.Template> {

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     */
    public PhotoStripTemplateAdapter(Context context) {
        super(context, createItems(context));
    }

    @Override
    protected void bindView(PhotoStripTemplateAdapter.Template item, View view) {
        // Hide unused views.
        view.findViewById(R.id.spinner_item_icon).setVisibility(View.GONE);

        // Bind data.
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setText(item.mDisplayName);
        ((TextView) view.findViewById(R.id.spinner_item_description)).setText(item.mDescription);
    }

    //
    // Private methods.
    //

    /**
     * Creates the list of {@link PhotoStripTemplateAdapter.Template} for the selection ui.
     *
     * @param context the {@link Context}.
     * @return the {@link SparseArray} of {@link PhotoStripTemplateAdapter.Template}.
     */
    private static SparseArray<PhotoStripTemplateAdapter.Template> createItems(Context context) {
        SparseArray<PhotoStripTemplateAdapter.Template> modes = new SparseArray<PhotoStripTemplateAdapter.Template>();

        // Add Single.
        PhotoStripTemplateAdapter.Template single = new PhotoStripTemplateAdapter.Template();
        single.mTemplate = PhotoStripTemplate.SINGLE;
        single.mDisplayName = context.getString(R.string.photo_strip_template_adapter__single_display_name);
        single.mDescription = context.getString(R.string.photo_strip_template_adapter__single_description);
        modes.put(PhotoStripTemplate.SINGLE.ordinal(), single);

        // Add Vertical 2.
        PhotoStripTemplateAdapter.Template vertical2 = new PhotoStripTemplateAdapter.Template();
        vertical2.mTemplate = PhotoStripTemplate.VERTICAL_2;
        vertical2.mDisplayName = context.getString(R.string.photo_strip_template_adapter__2_vertical_display_name);
        vertical2.mDescription = context.getString(R.string.photo_strip_template_adapter__2_vertical_description);
        modes.put(PhotoStripTemplate.VERTICAL_2.ordinal(), vertical2);

        // Add Horizontal 2.
        PhotoStripTemplateAdapter.Template horizontal2 = new PhotoStripTemplateAdapter.Template();
        horizontal2.mTemplate = PhotoStripTemplate.HORIZONTAL_2;
        horizontal2.mDisplayName = context.getString(R.string.photo_strip_template_adapter__2_horizontal_display_name);
        horizontal2.mDescription = context.getString(R.string.photo_strip_template_adapter__2_horizontal_description);
        modes.put(PhotoStripTemplate.HORIZONTAL_2.ordinal(), horizontal2);

        // Add Vertical 3.
        PhotoStripTemplateAdapter.Template vertical3 = new PhotoStripTemplateAdapter.Template();
        vertical3.mTemplate = PhotoStripTemplate.VERTICAL_3;
        vertical3.mDisplayName = context.getString(R.string.photo_strip_template_adapter__3_vertical_display_name);
        vertical3.mDescription = context.getString(R.string.photo_strip_template_adapter__3_vertical_description);
        modes.put(PhotoStripTemplate.VERTICAL_3.ordinal(), vertical3);

        // Add Horizontal 3.
        PhotoStripTemplateAdapter.Template horizontal3 = new PhotoStripTemplateAdapter.Template();
        horizontal3.mTemplate = PhotoStripTemplate.HORIZONTAL_3;
        horizontal3.mDisplayName = context.getString(R.string.photo_strip_template_adapter__3_horizontal_display_name);
        horizontal3.mDescription = context.getString(R.string.photo_strip_template_adapter__3_horizontal_description);
        modes.put(PhotoStripTemplate.HORIZONTAL_3.ordinal(), horizontal3);

        // Add Vertical 4.
        PhotoStripTemplateAdapter.Template vertical4 = new PhotoStripTemplateAdapter.Template();
        vertical4.mTemplate = PhotoStripTemplate.VERTICAL_4;
        vertical4.mDisplayName = context.getString(R.string.photo_strip_template_adapter__4_vertical_display_name);
        vertical4.mDescription = context.getString(R.string.photo_strip_template_adapter__4_vertical_description);
        modes.put(PhotoStripTemplate.VERTICAL_4.ordinal(), vertical4);

        // Add Horizontal 4.
        PhotoStripTemplateAdapter.Template horizontal4 = new PhotoStripTemplateAdapter.Template();
        horizontal4.mTemplate = PhotoStripTemplate.HORIZONTAL_4;
        horizontal4.mDisplayName = context.getString(R.string.photo_strip_template_adapter__4_horizontal_display_name);
        horizontal4.mDescription = context.getString(R.string.photo_strip_template_adapter__4_horizontal_description);
        modes.put(PhotoStripTemplate.HORIZONTAL_4.ordinal(), horizontal4);

        // Add Box 4.
        PhotoStripTemplateAdapter.Template box4 = new PhotoStripTemplateAdapter.Template();
        box4.mTemplate = PhotoStripTemplate.BOX_4;
        box4.mDisplayName = context.getString(R.string.photo_strip_template_adapter__4_box_display_name);
        box4.mDescription = context.getString(R.string.photo_strip_template_adapter__4_box_description);
        modes.put(PhotoStripTemplate.BOX_4.ordinal(), box4);

        return modes;
    }

    //
    // Public methods.
    //

    /**
     * Gets the {@link PhotoStripTemplate} based on the item position.
     *
     * @param position position of the item whose data we want within the adapter's data set.
     * @return the {@link PhotoStripTemplate}.
     */
    public PhotoStripTemplate getPhotoStripTemplate(int position) {
        return getItem(position).mTemplate;
    }

    //
    // Package private classes.
    //

    /**
     * An internal model object used by the adapter representing a {@link PhotoStripTemplate} and its data for the
     * selection ui.
     */
    static class Template {

        /**
         * The {@link PhotoStripTemplate}.
         */
        private PhotoStripTemplate mTemplate;

        /**
         * The display name for the {@link PhotoStripTemplate}.
         */
        private String mDisplayName;

        /**
         * The description of the {@link PhotoStripTemplate}.
         */
        private String mDescription;
    }
}
