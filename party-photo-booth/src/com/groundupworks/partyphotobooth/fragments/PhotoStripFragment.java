/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.groundupworks.partyphotobooth.R;

/**
 * Display photos in a photo strip format.
 * 
 * @author Benedict Lau
 */
public class PhotoStripFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_photo_strip, container, false);

        // mPreview = (CenteredPreview) view.findViewById(R.id.camera_preview);
        // mStartButton = (Button) view.findViewById(R.id.capture_button);

        return view;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link PhotoStripFragment} instance.
     * 
     * @return the new {@link PhotoStripFragment} instance.
     */
    public static PhotoStripFragment newInstance() {
        return new PhotoStripFragment();
    }
}
