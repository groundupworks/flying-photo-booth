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
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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

    /**
     * Handler for a key event.
     */
    private KioskActivity.KeyEventHandler mKeyEventHandler;

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
        mOkButton = (Button) view.findViewById(R.id.kiosk_setup_button_ok);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final KioskActivity activity = (KioskActivity) getActivity();

        /*
         * Initialize and set key event handlers.
         */
        mKeyEventHandler = new KioskActivity.KeyEventHandler() {
            @Override
            public boolean onKeyEvent(KeyEvent event) {
                mPassword.dispatchKeyEvent(event);
                return true;
            }
        };
        activity.setKeyEventHandler(mKeyEventHandler);

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
