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
package com.groundupworks.flyingphotobooth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.groundupworks.flyingphotobooth.fragments.CaptureFragment;
import com.groundupworks.flyingphotobooth.fragments.ErrorDialogFragment;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.lib.photobooth.helpers.StorageHelper;

import java.lang.ref.WeakReference;

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
    private WeakReference<BackPressedHandler> mBackPressedHandler = new WeakReference<BackPressedHandler>(null);

    /**
     * Handler for key event.
     */
    private WeakReference<KeyEventHandler> mKeyEventHandler = new WeakReference<KeyEventHandler>(null);

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
        final BackPressedHandler handler = mBackPressedHandler.get();
        if (handler == null || !handler.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final KeyEventHandler handler = mKeyEventHandler.get();
        if (handler != null && handler.onKeyEvent(event)) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    //
    // Public methods.
    //

    /**
     * Sets a handler for the back pressed event.
     *
     * @param handler the handler for the back pressed event. Pass null to clear. The reference is weakly
     *                held, so the client is responsible for holding onto a strong reference to prevent
     *                the handler from being garbage collected.
     */
    public void setBackPressedHandler(BackPressedHandler handler) {
        mBackPressedHandler = new WeakReference<BackPressedHandler>(handler);
    }

    /**
     * Sets a handler for the key event.
     *
     * @param handler the handler for the key event. Pass null to clear. The reference is weakly
     *                held, so the client is responsible for holding onto a strong reference to prevent
     *                the handler from being garbage collected.
     */
    public void setKeyEventHandler(KeyEventHandler handler) {
        mKeyEventHandler = new WeakReference<KeyEventHandler>(handler);
    }

    //
    // Public interfaces.
    //

    /**
     * Handler interface for a back pressed event.
     */
    public interface BackPressedHandler {

        /**
         * @return true if back pressed event is handled; false otherwise.
         */
        boolean onBackPressed();
    }

    /**
     * Handler interface for a key event.
     */
    public interface KeyEventHandler {

        /**
         * @param event the key event.
         * @return true if key event is handled; false otherwise.
         */
        boolean onKeyEvent(KeyEvent event);
    }
}
