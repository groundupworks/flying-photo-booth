/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;

/**
 * {@link Fragment} containing instructions and configurations for Kiosk mode.
 * 
 * @author Benedict Lau
 */
public class KioskSetupFragment extends Fragment {

    //
    // Views.
    //

    private EditText mPassword;

    private Button mOkButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_kiosk_setup, container, false);
        mPassword = (EditText) view.findViewById(R.id.kiosk_setup_password);
        mOkButton = (Button) view.findViewById(R.id.kiosk_setup_button_ok);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
                if (activity != null && !activity.isFinishing()) {
                    KioskModeHelper helper = new KioskModeHelper(activity);

                    // Get password if used.
                    Editable password = mPassword.getText();
                    if (password != null) {
                        helper.setPassword(password.toString());
                    }

                    // Transition to setup completed states.
                    helper.transitionState(State.SETUP_COMPLETED);

                    // Start photo booth.
                    activity.replaceFragment(CaptureFragment.newInstance(), false, true);
                }
            }
        });
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link KioskSetupFragment} instance.
     * 
     * @return the new {@link KioskSetupFragment} instance.
     */
    public static KioskSetupFragment newInstance() {
        return new KioskSetupFragment();
    }
}
