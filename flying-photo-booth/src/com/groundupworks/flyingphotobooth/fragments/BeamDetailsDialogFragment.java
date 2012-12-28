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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import com.groundupworks.flyingphotobooth.R;

/**
 * Android Beam details dialog.
 * 
 * @author Benedict Lau
 */
public class BeamDetailsDialogFragment extends DialogFragment {

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = null;
        // AlertDialog.THEME_DEVICE_DEFAULT_LIGHT only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dialog = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setTitle(getString(R.string.beam_details__dialog_title))
                    .setMessage(R.string.beam_details__dialog_message)
                    .setPositiveButton(R.string.beam_details__dialog_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            }).create();
        } else {
            dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.beam_details__dialog_title))
                    .setMessage(R.string.beam_details__dialog_message)
                    .setPositiveButton(R.string.beam_details__dialog_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            }).create();
        }

        return dialog;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link BeamDetailsDialogFragment} instance.
     * 
     * @return the new {@link BeamDetailsDialogFragment} instance.
     */
    public static BeamDetailsDialogFragment newInstance() {
        return new BeamDetailsDialogFragment();
    }
}
