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
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;
import com.groundupworks.partyphotobooth.fragments.ConfirmationFragment;
import com.groundupworks.partyphotobooth.fragments.ErrorDialogFragment;
import com.groundupworks.partyphotobooth.fragments.NoticeFragment;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;

import java.lang.ref.WeakReference;

/**
 * {@link Activity} that puts the device in Kiosk mode. This should only be launched from the {@link KioskService}.
 *
 * @author Benedict Lau
 */
public class KioskActivity extends FragmentActivity implements KioskSetupFragment.ICallbacks,
        PhotoStripFragment.ICallbacks, CaptureFragment.ICallbacks, ConfirmationFragment.ICallbacks,
        NoticeFragment.ICallbacks, ErrorDialogFragment.ICallbacks {

    /**
     * Package private flag to track whether the single instance {@link KioskActivity} is in foreground.
     */
    static boolean sIsInForeground = false;

    /**
     * Handler for key event.
     */
    private WeakReference<KeyEventHandler> mKeyEventHandler = new WeakReference<KeyEventHandler>(null);

    /**
     * The {@link KioskModeHelper}.
     */
    private KioskModeHelper mKioskModeHelper;

    /**
     * The {@link PreferencesHelper}.
     */
    private PreferencesHelper mPreferencesHelper;

    /**
     * The current frame number to capture.
     */
    private int mCurrentFrame;

    /**
     * The total number of frames to capture.
     */
    private int mTotalFrames;

    //
    // Fragments.
    //

    private KioskSetupFragment mKioskSetupFragment = null;

    private PhotoStripFragment mPhotoStripFragment = null;

    private NoticeFragment mNoticeFragment = null;

    //
    // Views.
    //

    private View mFlashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKioskModeHelper = new KioskModeHelper(this);
        mPreferencesHelper = new PreferencesHelper();
        mCurrentFrame = 1;
        mTotalFrames = mPreferencesHelper.getPhotoStripTemplate(this).getNumPhotos();

        // Show on top of lock screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_kiosk);

        // Configure button to exit Kiosk mode.
        ImageView exitButton = (ImageView) findViewById(R.id.kiosk_exit_button);
        mFlashScreen = findViewById(R.id.flash_screen);
        exitButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mKioskModeHelper.isPasswordRequired()) {
                    showDialogFragment(KioskPasswordDialogFragment.newInstance());
                } else {
                    exitKioskMode();
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        sIsInForeground = true;

        // Choose fragments to start with based on whether Kiosk mode setup has completed.
        if (mKioskModeHelper.isSetupCompleted()) {
            launchPhotoBoothUi();

            // Dismiss the notice fragment after resume.
            dismissNoticeFragment();
        } else {
            Toast.makeText(this, getString(R.string.kiosk_mode__start_msg), Toast.LENGTH_SHORT).show();
            launchKioskSetupFragment();
        }
    }

    @Override
    public void onPause() {
        sIsInForeground = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        // Do nothing.
    }

    @Override
    public boolean onSearchRequested() {
        // Block search.
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final KeyEventHandler handler = mKeyEventHandler.get();
        if (handler != null) {
            handler.onKeyEvent(event);
        }

        // Block event.
        return true;
    }

    //
    // Implementation of the KioskSetupFragment callbacks.
    //

    @Override
    public void onKioskSetupComplete(String password) {
        // Set password if used.
        if (password != null) {
            mKioskModeHelper.setPassword(password);
        }

        // Transition to setup completed state.
        mKioskModeHelper.transitionState(State.SETUP_COMPLETED);

        // Remove Kiosk setup fragment.
        dismissKioskSetupFragment();

        // Launch photo booth ui.
        launchPhotoBoothUi();
    }

    //
    // Implementation of the PhotoStripFragment callbacks.
    //

    @Override
    public void onNewPhotoAdded(boolean isPhotoStripComplete) {
        // Update current frame count.
        mCurrentFrame++;

        // Fade out flash screen.
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFlashScreen.setVisibility(View.GONE);
            }
        });
        mFlashScreen.startAnimation(animation);

        // Transition right side fragment while flash animation is running.
        if (isPhotoStripComplete) {
            // Confirm submission of photo strip.
            launchConfirmationFragment();
        } else {
            // Capture next frame.
            launchCaptureFragment();
        }
    }

    @Override
    public void onPhotoRemoved() {
        // Update current frame count.
        mCurrentFrame--;

        // Capture next frame.
        launchCaptureFragment();
    }

    @Override
    public void onPhotoStripSubmitted(boolean facebookShared, boolean dropboxShared, boolean gcpShared) {
        // Reset photo booth ui.
        launchPhotoBoothUi();

        if (facebookShared || dropboxShared || gcpShared) {
            if (mPreferencesHelper.getNoticeEnabled(this)) {
                // Show notice fragment.
                launchNoticeFragment(facebookShared, dropboxShared, gcpShared);
            }
        }
    }

    @Override
    public void onErrorNewPhoto() {
        // An error occurred while trying to add a new photo. Self-recover by relaunching capture fragment.
        Toast.makeText(this, getString(R.string.photostrip__error_new_photo), Toast.LENGTH_LONG).show();
        launchCaptureFragment();
    }

    @Override
    public void onErrorMissingPhoto() {
        // More photos are needed to compile a photo strip. Self-recover by relaunching capture fragment.
        Toast.makeText(this, getString(R.string.photostrip__error_missing_photo), Toast.LENGTH_LONG).show();
        launchCaptureFragment();
    }

    @Override
    public void onErrorPhotoStripSubmit() {
        // An error occurred while trying to submit the current photo strip. Self-recover by relaunching photo booth ui.
        Toast.makeText(this, getString(R.string.photostrip__error_submission), Toast.LENGTH_LONG).show();
        launchPhotoBoothUi();
    }

    //
    // Implementation of the CaptureFragment callbacks.
    //

    @Override
    public void onPictureTaken(byte[] data, float rotation, boolean reflection) {
        if (mPhotoStripFragment != null) {
            mFlashScreen.setVisibility(View.VISIBLE);
            mPhotoStripFragment.addPhoto(data, rotation, reflection);
        }
    }

    @Override
    public void onErrorCameraNone() {
        String title = getString(R.string.capture__error_camera_dialog_title);
        String message = getString(R.string.capture__error_camera_dialog_message_none);
        showDialogFragment(ErrorDialogFragment.newInstance(title, message));
    }

    @Override
    public void onErrorCameraInUse() {
        String title = getString(R.string.capture__error_camera_dialog_title);
        String message = getString(R.string.capture__error_camera_dialog_message_in_use);
        showDialogFragment(ErrorDialogFragment.newInstance(title, message));
    }

    @Override
    public void onErrorCameraCrashed() {
        // The native camera crashes occasionally. Self-recover by relaunching capture fragment.
        Toast.makeText(this, getString(R.string.capture__error_camera_crash), Toast.LENGTH_SHORT).show();
        launchCaptureFragment();
    }

    //
    // Implementation of the ConfirmationFragment callbacks.
    //

    @Override
    public void onSubmit() {
        if (mPhotoStripFragment != null) {
            mPhotoStripFragment.submitPhotoStrip();
        }
    }

    //
    // Implementation of the NoticeFragment callbacks.
    //

    @Override
    public void onNoticeDismissRequested() {
        dismissNoticeFragment();
    }

    //
    // Implementation of the ErrorDialogFragment callbacks.
    //

    @Override
    public void onExitPressed() {
        exitKioskMode();
    }

    //
    // Public methods.
    //

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
    // Private methods.
    //

    /**
     * Launches the {@link KioskSetupFragment}.
     */
    private void launchKioskSetupFragment() {
        mKioskSetupFragment = KioskSetupFragment.newInstance();
        replaceTopFragment(mKioskSetupFragment);
    }

    /**
     * Launches the photo booth ui.
     */
    private void launchPhotoBoothUi() {
        // Reset current frame count whenever the photo booth ui is relaunched.
        mCurrentFrame = 1;

        // Launch photo strip ui in the left side container.
        mPhotoStripFragment = PhotoStripFragment.newInstance();
        replaceLeftFragment(mPhotoStripFragment);

        // Launch capture ui in the right side container.
        launchCaptureFragment();
    }

    /**
     * Launches a new {@link CaptureFragment} in the right side container.
     */
    private void launchCaptureFragment() {
        replaceRightFragment(CaptureFragment.newInstance(mCurrentFrame, mTotalFrames));
    }

    /**
     * Launches a new {@link ConfirmationFragment} in the right side container.
     */
    private void launchConfirmationFragment() {
        replaceRightFragment(ConfirmationFragment.newInstance());
    }

    /**
     * Launches a new {@link NoticeFragment}.
     *
     * @param facebookShared true if the photo strip is marked for Facebook sharing; false otherwise.
     * @param dropboxShared  true if the photo strip is marked for Dropbox sharing; false otherwise.
     * @param gcpShared      true if the photo strip is marked for Google Cloud Print sharing; false otherwise.
     */
    private void launchNoticeFragment(boolean facebookShared, boolean dropboxShared, boolean gcpShared) {
        mNoticeFragment = NoticeFragment.newInstance(facebookShared, dropboxShared, gcpShared);
        replaceTopFragment(mNoticeFragment);
    }

    /**
     * Dismisses the {@link KioskSetupFragment}.
     */
    private void dismissKioskSetupFragment() {
        if (mKioskSetupFragment != null) {
            removeFragment(mKioskSetupFragment);
            mKioskSetupFragment = null;
        }
    }

    /**
     * Dismisses the {@link NoticeFragment}.
     */
    private void dismissNoticeFragment() {
        if (mNoticeFragment != null) {
            removeFragment(mNoticeFragment);
            mNoticeFragment = null;
        }
    }

    /**
     * Replaces the {@link Fragment} in the top fullscreen container.
     *
     * @param fragment the new {@link Fragment} used to replace the current.
     */
    private void replaceTopFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container_top, fragment);
        ft.commit();
    }

    /**
     * Replaces the {@link Fragment} in the left side container.
     *
     * @param fragment the new {@link Fragment} used to replace the current.
     */
    private void replaceLeftFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container_left, fragment);
        ft.commit();
    }

    /**
     * Replaces the {@link Fragment} in the right side container.
     *
     * @param fragment the new {@link Fragment} used to replace the current.
     */
    private void replaceRightFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container_right, fragment);
        ft.commit();
    }

    /**
     * Shows a {@link DialogFragment}.
     *
     * @param fragment the new {@link DialogFragment} to show.
     */
    private void showDialogFragment(DialogFragment fragment) {
        fragment.show(getSupportFragmentManager(), null);
    }

    /**
     * Removes the {@link Fragment}.
     *
     * @param fragment the {@link Fragment} to remove.
     */
    private void removeFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    //
    // Package private methods.
    //

    /**
     * Exits Kiosk mode.
     */
    void exitKioskMode() {
        // Disable Kiosk mode.
        mKioskModeHelper.transitionState(State.DISABLED);

        // Finish KioskActivity.
        finish();
    }

    //
    // Public interfaces.
    //

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
