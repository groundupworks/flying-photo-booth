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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.confirm_image__beam_dialog_title))
                .setMessage(R.string.confirm_image__beam_dialog_message)
                .setPositiveButton(R.string.confirm_image__beam_dialog_button_text,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        }).create();
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
