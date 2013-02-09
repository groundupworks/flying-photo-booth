/*
 * Copyright (C) 2012 Benedict Lau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groundupworks.flyingphotobooth.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.groundupworks.flyingphotobooth.R;

/**
 * A blocking error dialog that requires the user to exit the application.
 * 
 * @author Benedict Lau
 */
public class ErrorDialogFragment extends DialogFragment {

    private static final String FRAGMENT_BUNDLE_KEY_TITLE = "title";

    private static final String FRAGMENT_BUNDLE_KEY_MESSAGE = "message";

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(FRAGMENT_BUNDLE_KEY_TITLE);
        String message = args.getString(FRAGMENT_BUNDLE_KEY_MESSAGE);

        AlertDialog.Builder dialogBuilder = null;

        // AlertDialog.THEME_DEVICE_DEFAULT_LIGHT only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity());
        }

        return dialogBuilder.setTitle(title).setMessage(message)
                .setPositiveButton(R.string.error__dialog_button_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                }).create();
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ErrorDialogFragment} instance.
     * 
     * @param title
     *            the title of the error dialog.
     * @param message
     *            the message of the error dialog.
     * @return the new {@link ErrorDialogFragment} instance.
     */
    public static ErrorDialogFragment newInstance(String title, String message) {
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setCancelable(false);

        Bundle args = new Bundle();
        args.putString(FRAGMENT_BUNDLE_KEY_TITLE, title);
        args.putString(FRAGMENT_BUNDLE_KEY_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }
}
