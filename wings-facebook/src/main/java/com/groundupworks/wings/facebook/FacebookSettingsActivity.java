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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * {@link Activity} to configure how a Facebook account is linked.
 *
 * @author Benedict Lau
 */
public class FacebookSettingsActivity extends FragmentActivity {

    /**
     * Flag to track if any error has occurred.
     */
    boolean mHasErrorOccurred = false;

    //
    // Required Facebook settings.
    //

    String mAccountName = null;

    String mPhotoPrivacy = null;

    String mAlbumName = null;

    String mAlbumGraphPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebook_activity_settings);
        replaceFragment(FacebookAlbumListFragment.newInstance(), false, true);
    }

    //
    // Package private methods.
    //

    /**
     * Replaces a {@link android.support.v4.app.Fragment} in the container.
     *
     * @param fragment         the new {@link android.support.v4.app.Fragment} used to replace the current.
     * @param addToBackStack   true to add transaction to back stack; false otherwise.
     * @param popPreviousState true to pop the previous state from the back stack; false otherwise.
     */
    void replaceFragment(Fragment fragment, boolean addToBackStack, boolean popPreviousState) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (popPreviousState) {
            fragmentManager.popBackStack();
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }
        ft.commit();
    }

    /**
     * Shows a {@link android.support.v4.app.DialogFragment}.
     *
     * @param fragment the new {@link android.support.v4.app.DialogFragment} to show.
     */
    void showDialogFragment(DialogFragment fragment) {
        fragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Finish the {@link FacebookSettingsActivity} if the conditions are met.
     */
    void tryFinish() {
        // Return the RESULT_CANCELED result code if an has error occurred.
        if (mHasErrorOccurred) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Return the results if all settings are filled.
        if (mAccountName != null && mAlbumName != null && mAlbumGraphPath != null) {
            FacebookSettings settings = FacebookSettings.newInstance(mAccountName, mPhotoPrivacy, mAlbumName,
                    mAlbumGraphPath);
            if (settings != null) {
                Intent result = new Intent();
                result.putExtras(settings.toBundle());
                setResult(RESULT_OK, result);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
