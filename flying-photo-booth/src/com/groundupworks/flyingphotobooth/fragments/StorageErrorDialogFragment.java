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
 * Storage error dialog.
 * 
 * @author Benedict Lau
 */
public class StorageErrorDialogFragment extends DialogFragment {

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = null;

        // AlertDialog.THEME_DEVICE_DEFAULT_LIGHT only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity());
        }

        return dialogBuilder.setTitle(getString(R.string.storage_error__dialog_title))
                .setMessage(R.string.storage_error__dialog_message)
                .setPositiveButton(R.string.storage_error__dialog_button_text, new DialogInterface.OnClickListener() {
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
     * Creates a new {@link StorageErrorDialogFragment} instance.
     * 
     * @return the new {@link StorageErrorDialogFragment} instance.
     */
    public static StorageErrorDialogFragment newInstance() {
        StorageErrorDialogFragment fragment = new StorageErrorDialogFragment();
        fragment.setCancelable(false);
        return fragment;
    }
}
