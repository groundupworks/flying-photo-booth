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
package com.groundupworks.wings.facebook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * {@link DialogFragment} for Facebook photo privacy level selection.
 *
 * @author Benedict Lau
 */
public class FacebookPrivacyDialogFragment extends DialogFragment {

    //
    // Fragment bundle keys.
    //

    private static final String FRAGMENT_BUNDLE_KEY_ALBUM_NAME = "albumName";

    private static final String FRAGMENT_BUNDLE_KEY_ALBUM_GRAPH_PATH = "albumGraphPath";

    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final String albumName = args.getString(FRAGMENT_BUNDLE_KEY_ALBUM_NAME);
        final String albumGraphPath = args.getString(FRAGMENT_BUNDLE_KEY_ALBUM_GRAPH_PATH);

        AlertDialog.Builder dialogBuilder = null;

        // AlertDialog.THEME_DEVICE_DEFAULT_LIGHT only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity());

        }

        return dialogBuilder.setTitle(getString(R.string.facebook__privacy__dialog_title))
                .setItems(R.array.facebook_privacy__privacies, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handlePhotoPrivacySelection(which, albumName, albumGraphPath);
                    }
                }).create();
    }

    //
    // Private methods.
    //

    /**
     * Handles the photo privacy selection.
     *
     * @param photoPrivacyIndex the index of the selected photo privacy level.
     * @param albumName         the name of the album to share to.
     * @param albumGraphPath    the graph path of the album to share to.
     */
    private void handlePhotoPrivacySelection(int photoPrivacyIndex, String albumName, String albumGraphPath) {
        FacebookSettingsActivity activity = (FacebookSettingsActivity) getActivity();
        if (activity != null && !activity.isFinishing()) {
            // Convert privacy index to privacy setting to store.
            String[] privacyArray = getResources().getStringArray(R.array.facebook_privacy__privacies);
            String privacyString = privacyArray[photoPrivacyIndex];

            String photoPrivacy = null;
            if (getString(R.string.facebook__privacy__photo_privacy_self).equals(privacyString)) {
                photoPrivacy = FacebookEndpoint.PHOTO_PRIVACY_SELF;
            } else if (getString(R.string.facebook__privacy__photo_privacy_friends).equals(privacyString)) {
                photoPrivacy = FacebookEndpoint.PHOTO_PRIVACY_FRIENDS;
            } else if (getString(R.string.facebook__privacy__photo_privacy_friends_of_friends).equals(privacyString)) {
                photoPrivacy = FacebookEndpoint.PHOTO_PRIVACY_FRIENDS_OF_FRIENDS;
            } else if (getString(R.string.facebook__privacy__photo_privacy_everyone).equals(privacyString)) {
                photoPrivacy = FacebookEndpoint.PHOTO_PRIVACY_EVERYONE;
            }

            // Set Facebook settings.
            if (photoPrivacy != null && photoPrivacy.length() > 0 && albumName != null && albumName.length() > 0
                    && albumGraphPath != null && albumGraphPath.length() > 0) {
                activity.mPhotoPrivacy = photoPrivacy;
                activity.mAlbumName = albumName;
                activity.mAlbumGraphPath = albumGraphPath;
            } else {
                activity.mHasErrorOccurred = true;
            }

            // Finish activity with
            activity.tryFinish();
        }
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link FacebookPrivacyDialogFragment} instance.
     *
     * @param albumName      the name of the album to share to.
     * @param albumGraphPath the graph path of the album to share to.
     * @return the new {@link FacebookPrivacyDialogFragment} instance.
     */
    public static FacebookPrivacyDialogFragment newInstance(String albumName, String albumGraphPath) {
        FacebookPrivacyDialogFragment fragment = new FacebookPrivacyDialogFragment();

        Bundle args = new Bundle();
        args.putString(FRAGMENT_BUNDLE_KEY_ALBUM_NAME, albumName);
        args.putString(FRAGMENT_BUNDLE_KEY_ALBUM_GRAPH_PATH, albumGraphPath);
        fragment.setArguments(args);

        return fragment;
    }
}
