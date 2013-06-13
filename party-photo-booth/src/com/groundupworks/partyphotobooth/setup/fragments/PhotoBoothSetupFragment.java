/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.fragments;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothMode;
import com.groundupworks.partyphotobooth.setup.model.PhotoBoothModeAdapter;

/**
 * Ui for setting up the photo booth.
 * 
 * @author Benedict Lau
 */
public class PhotoBoothSetupFragment extends Fragment {

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<PhotoBoothSetupFragment.ICallbacks> mCallbacks = null;

    /**
     * A {@link PreferencesHelper} instance.
     */
    private PreferencesHelper mPreferencesHelper = new PreferencesHelper();

    //
    // Views.
    //

    private Spinner mMode;

    private Spinner mTheme;

    private Spinner mArrangement;

    private Button mNext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<PhotoBoothSetupFragment.ICallbacks>(
                (PhotoBoothSetupFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_photo_booth_setup, container, false);
        mMode = (Spinner) view.findViewById(R.id.setup_photo_booth_mode);
        mTheme = (Spinner) view.findViewById(R.id.setup_photo_booth_theme);
        mArrangement = (Spinner) view.findViewById(R.id.setup_photo_booth_arrangement);
        mNext = (Button) view.findViewById(R.id.setup_photo_booth_button_next);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        final Context appContext = activity.getApplicationContext();

        /*
         * Configure views with saved preferences and functionalize.
         */
        final PhotoBoothModeAdapter modeAdapter = new PhotoBoothModeAdapter(activity);
        mMode.setAdapter(modeAdapter);
        mMode.setSelection(mPreferencesHelper.getPhotoBoothMode(appContext).ordinal());
        mMode.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PhotoBoothMode selectedMode = modeAdapter.getPhotoBoothMode(position);
                mPreferencesHelper.storePhotoBoothMode(appContext, selectedMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // TODO Handle theme and arrangement.

        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onPhotoBoothSetupCompleted();
                }
            }
        });
    }

    //
    // Private methods.
    //

    /**
     * Gets the callbacks for this fragment.
     * 
     * @return the callbacks; or null if not set.
     */
    private PhotoBoothSetupFragment.ICallbacks getCallbacks() {
        PhotoBoothSetupFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link PhotoBoothSetupFragment} instance.
     * 
     * @return the new {@link PhotoBoothSetupFragment} instance.
     */
    public static PhotoBoothSetupFragment newInstance() {
        return new PhotoBoothSetupFragment();
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Setup of the photo booth has completed.
         */
        void onPhotoBoothSetupCompleted();
    }
}
