/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.groundupworks.partyphotobooth.R;

/**
 * Confirmation screen for photo strip submission.
 * 
 * @author Benedict Lau
 */
public class ConfirmationFragment extends Fragment {

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<ConfirmationFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    private TextView mMessage;

    private Button mSubmit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<ConfirmationFragment.ICallbacks>((ConfirmationFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_confirmation, container, false);
        mMessage = (TextView) view.findViewById(R.id.confirmation_message);
        mSubmit = (Button) view.findViewById(R.id.confirmation_button_submit);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String message = getString(R.string.confirmation__message, getString(R.string.app_name));
        mMessage.setText(message);

        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onSubmit();
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
    private ConfirmationFragment.ICallbacks getCallbacks() {
        ConfirmationFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
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

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * The submit button is clicked.
         */
        public void onSubmit();
    }
}
