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
        AlertDialog.Builder dialogBuilder = null;

        // AlertDialog.THEME_DEVICE_DEFAULT_LIGHT only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity());

        }

        return dialogBuilder.setTitle(getString(R.string.beam_details__dialog_title))
                .setMessage(R.string.beam_details__dialog_message)
                .setPositiveButton(R.string.beam_details__dialog_button_text, new DialogInterface.OnClickListener() {
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
