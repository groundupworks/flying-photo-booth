/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.fragments;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.groundupworks.partyphotobooth.R;

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
        // TODO
        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
