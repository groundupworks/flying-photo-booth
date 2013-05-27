/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;
import com.groundupworks.partyphotobooth.fragments.ConfirmationFragment;
import com.groundupworks.partyphotobooth.fragments.ErrorDialogFragment;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;

/**
 * {@link Activity} that puts the device in Kiosk mode. This should only be launched from the {@link KioskService}.
 * 
 * @author Benedict Lau
 */
public class KioskActivity extends BaseFragmentActivity implements KioskSetupFragment.ICallbacks,
        PhotoStripFragment.ICallbacks, CaptureFragment.ICallbacks {

    /**
     * Package private flag to track whether the single instance {@link KioskActivity} is in foreground.
     */
    static boolean sIsInForeground = false;

    /**
     * The {@link KioskModeHelper}.
     */
    private KioskModeHelper mKioskModeHelper;

    //
    // Fragments.
    //

    private KioskSetupFragment mKioskSetupFragment = null;

    private PhotoStripFragment mPhotoStripFragment = null;

    //
    // Views.
    //

    private View mFlashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKioskModeHelper = new KioskModeHelper(this);

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
    public void onResume() {
        super.onResume();
        sIsInForeground = true;

        // Choose fragments to start with based on whether Kiosk mode setup has completed.
        if (mKioskModeHelper.isSetupCompleted()) {
            launchPhotoStripFragment();
            launchCaptureFragment();
        } else {
            mKioskSetupFragment = KioskSetupFragment.newInstance();
            replaceFragment(mKioskSetupFragment, false, true);
        }
    }

    @Override
    public void onPause() {
        sIsInForeground = false;
        super.onPause();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        if (mKioskSetupFragment != null) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.remove(mKioskSetupFragment);
            ft.commit();
            mKioskSetupFragment = null;
        }

        // Launch photo booth ui.
        launchPhotoStripFragment();
        launchCaptureFragment();
    }

    //
    // Implementation of the PhotoStripFragment callbacks.
    //

    @Override
    public void onNewPhotoStart() {
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
    }

    @Override
    public void onNewPhotoEnd(boolean isPhotoStripComplete) {
        if (isPhotoStripComplete) {
            // Confirm submission of photo strip.
            launchConfirmationFragment();
        } else {
            // Capture next frame.
            launchCaptureFragment();
        }
    }

    @Override
    public void onPhotoRemoval() {
        // Capture next frame.
        launchCaptureFragment();
    }

    @Override
    public void onErrorNewPhoto() {
        // An error occurred while trying to add a new photo. Self-recover by relaunching capture fragment.
        Toast.makeText(this, getString(R.string.photostrip__error_new_photo), Toast.LENGTH_LONG).show();
        launchCaptureFragment();
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
    // Private methods.
    //

    /**
     * Launches a new {@link PhotoStripFragment} in the left side container.
     */
    private void launchPhotoStripFragment() {
        mPhotoStripFragment = PhotoStripFragment.newInstance();
        replaceLeftFragment(mPhotoStripFragment);
    }

    /**
     * Launches a new {@link CaptureFragment} in the right side container.
     */
    private void launchCaptureFragment() {
        replaceRightFragment(CaptureFragment.newInstance());
    }

    /**
     * Launches a new {@link ConfirmationFragment} in the right side container.
     */
    private void launchConfirmationFragment() {
        replaceRightFragment(ConfirmationFragment.newInstance());
    }

    /**
     * Replaces the {@link Fragment} in the left side container.
     * 
     * @param fragment
     *            the new {@link Fragment} used to replace the current.
     */
    private void replaceLeftFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container_left, fragment);
        ft.commit();
    }

    /**
     * Replaces the {@link Fragment} in the left side container.
     * 
     * @param fragment
     *            the new {@link Fragment} used to replace the current.
     */
    private void replaceRightFragment(Fragment fragment) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fragment_container_right, fragment);
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
}
