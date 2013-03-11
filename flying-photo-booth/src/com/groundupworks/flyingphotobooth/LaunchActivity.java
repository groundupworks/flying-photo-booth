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
package com.groundupworks.flyingphotobooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.groundupworks.flyingphotobooth.fragments.CaptureFragment;
import com.groundupworks.flyingphotobooth.fragments.ErrorDialogFragment;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.lib.photobooth.helpers.StorageHelper;

/**
 * The launch {@link Activity}.
 * 
 * @author Benedict Lau
 */
public class LaunchActivity extends BaseFragmentActivity {

    /**
     * Worker handler for posting background tasks.
     */
    private Handler mWorkerHandler;

    /**
     * Handler for the back pressed event.
     */
    private BackPressedHandler mBackPressedHandler = null;

    /**
     * Reference to the storage error dialog if shown.
     */
    private ErrorDialogFragment mStorageError = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create worker handler.
        mWorkerHandler = new Handler(MyApplication.getWorkerLooper());

        // Get last used camera preference.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean cameraPref = preferences.getBoolean(getString(R.string.pref__camera_key), true);

        // Start with capture fragment. Use replaceFragment() to ensure only one instance of CaptureFragment is added.
        replaceFragment(CaptureFragment.newInstance(cameraPref), false, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check availability of external storage in background.
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!StorageHelper.isExternalStorageAvailable()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!isFinishing()) {
                                String title = getString(R.string.launch__error_storage_dialog_title);
                                String message = getString(R.string.launch__error_storage_dialog_message);

                                mStorageError = ErrorDialogFragment.newInstance(title, message);
                                showDialogFragment(mStorageError);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        // Dismiss storage error fragment since we will check again onResume().
        if (mStorageError != null) {
            mStorageError.dismiss();
            mStorageError = null;
        }

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mBackPressedHandler == null || !mBackPressedHandler.isHandled()) {
            super.onBackPressed();
        }
    }

    //
    // Public methods.
    //

    /**
     * Sets a handler for the back pressed event.
     * 
     * @param handler
     *            the handler for the back pressed event. Pass null to clear.
     */
    public void setBackPressedHandler(BackPressedHandler handler) {
        mBackPressedHandler = handler;
    }

    //
    // Public interfaces.
    //

    /**
     * Handler interface for the back pressed event.
     */
    public interface BackPressedHandler {

        /**
         * @return true if back press event is handled; false otherwise.
         */
        boolean isHandled();
    }
}