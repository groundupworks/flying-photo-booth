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
 * Confirmation screen for photo strip submission.
 * 
 * @author Benedict Lau
 */
public class ConfirmationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);

        return view;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ConfirmationFragment} instance.
     * 
     * @return the new {@link ConfirmationFragment} instance.
     */
    public static ConfirmationFragment newInstance() {
        return new ConfirmationFragment();
    }
}
