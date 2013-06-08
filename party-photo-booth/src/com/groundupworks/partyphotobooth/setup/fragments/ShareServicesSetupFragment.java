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
 * Ui for setting up the sharing services.
 * 
 * @author Benedict Lau
 */
public class ShareServicesSetupFragment extends Fragment {

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<ShareServicesSetupFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<ShareServicesSetupFragment.ICallbacks>(
                (ShareServicesSetupFragment.ICallbacks) activity);
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
    private ShareServicesSetupFragment.ICallbacks getCallbacks() {
        ShareServicesSetupFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ShareServicesSetupFragment} instance.
     * 
     * @return the new {@link ShareServicesSetupFragment} instance.
     */
    public static ShareServicesSetupFragment newInstance() {
        return new ShareServicesSetupFragment();
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Setup of the share services has completed.
         */
        void onShareServicesSetupCompleted();
    }
}
