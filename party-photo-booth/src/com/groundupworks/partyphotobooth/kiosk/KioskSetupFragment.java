/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;

/**
 * {@link Fragment} containing instructions and configurations for Kiosk mode.
 * 
 * @author Benedict Lau
 */
public class KioskSetupFragment extends Fragment {

    //
    // Views.
    //

    private Button mOkButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_kiosk_setup, container, false);
        mOkButton = (Button) view.findViewById(R.id.kiosk_setup_button_ok);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start photo booth.
                BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
                if (activity != null && !activity.isFinishing()) {
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
