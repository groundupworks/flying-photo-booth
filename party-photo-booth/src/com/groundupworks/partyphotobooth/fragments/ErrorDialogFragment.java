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
package com.groundupworks.partyphotobooth.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.groundupworks.partyphotobooth.R;

import java.lang.ref.WeakReference;

/**
 * A blocking error dialog that requires the user to exit the application.
 *
 * @author Benedict Lau
 */
public class ErrorDialogFragment extends DialogFragment {

    //
    // Fragment bundle keys.
    //

    private static final String FRAGMENT_BUNDLE_KEY_TITLE = "title";

    private static final String FRAGMENT_BUNDLE_KEY_MESSAGE = "message";

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<ErrorDialogFragment.ICallbacks> mCallbacks = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<ErrorDialogFragment.ICallbacks>((ErrorDialogFragment.ICallbacks) activity);
    }

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
                        // Call to client.
                        ICallbacks callbacks = getCallbacks();
                        if (callbacks != null) {
                            callbacks.onExitPressed();
                        }
                    }
                }).create();
    }

    //
    // Private methods.
    //

    /**
     * Gets the callbacks for this fragment.
     *
     * @return the callbacks; or null if not set.
     */
    private ErrorDialogFragment.ICallbacks getCallbacks() {
        ErrorDialogFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ErrorDialogFragment} instance.
     *
     * @param title   the title of the error dialog.
     * @param message the message of the error dialog.
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

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Exit button is pressed.
         */
        public void onExitPressed();
    }
}
