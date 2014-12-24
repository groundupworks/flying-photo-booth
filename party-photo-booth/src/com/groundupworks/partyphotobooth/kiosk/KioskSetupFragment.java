/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.groundupworks.partyphotobooth.R;

import java.lang.ref.WeakReference;

/**
 * {@link Fragment} containing instructions and configurations for Kiosk mode.
 *
 * @author Benedict Lau
 */
public class KioskSetupFragment extends Fragment {

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<KioskSetupFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    private EditText mPassword;

    private Button mOkButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<KioskSetupFragment.ICallbacks>((KioskSetupFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_kiosk_setup, container, false);
        mPassword = (EditText) view.findViewById(R.id.kiosk_setup_password);
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < s.length(); i++) {
                    final String partial = buffer.toString() + s.charAt(i);
                    if (TextUtils.isDigitsOnly(partial)) {
                        buffer.append(s.charAt(i));
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_passcode_numeric,
                                Toast.LENGTH_SHORT).show();
                        s.delete(i, s.length());
                    }
                }
            }
        });
        mOkButton = (Button) view.findViewById(R.id.kiosk_setup_button_ok);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get password.
                String passwordString = null;
                Editable password = mPassword.getText();
                if (password != null) {
                    passwordString = password.toString();
                }

                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onKioskSetupComplete(passwordString);
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
    private KioskSetupFragment.ICallbacks getCallbacks() {
        KioskSetupFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
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
        KioskSetupFragment fragment = new KioskSetupFragment();
        return fragment;
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Kiosk setup completed.
         *
         * @param password the password; or null if not set.
         */
        public void onKioskSetupComplete(String password);
    }
}
