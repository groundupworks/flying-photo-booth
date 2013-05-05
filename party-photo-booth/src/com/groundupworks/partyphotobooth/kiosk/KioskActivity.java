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
import android.widget.ImageView;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;

/**
 * {@link Activity} that puts the device in Kiosk mode. This should only be launched from the {@link KioskService}.
 * 
 * @author Benedict Lau
 */
public class KioskActivity extends BaseFragmentActivity implements KioskSetupFragment.IFragmentTransition {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKioskModeHelper = new KioskModeHelper(this);

        // Show on top of lock screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_kiosk);

        // Configure button to exit Kiosk mode.

        ImageView exitButton = (ImageView) findViewById(R.id.kiosk_exit_button);
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

        // Choose Fragment to start with based on whether Kiosk mode setup has completed.
        if (mKioskModeHelper.isSetupCompleted()) {
            // replaceRightFragment(CaptureFragment.newInstance());
        } else {
            mKioskSetupFragment = KioskSetupFragment.newInstance();
            replaceFragment(mKioskSetupFragment, false, true);
        }
    }

    @Override
    public void onResume() {
        sIsInForeground = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        sIsInForeground = false;
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
    // Fragment transition interfaces.
    //

    @Override
    public void onKioskSetupComplete() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.remove(mKioskSetupFragment);
        ft.commit();

        // Start photo booth.
        replaceLeftFragment(PhotoStripFragment.newInstance());
        replaceRightFragment(CaptureFragment.newInstance());
    }

    //
    // Private methods.
    //

    /**
     * Replaces the {@link Fragment} in the left side container.
     * 
     * @param fragment
     *            the new {@link Fragment} used to replace the current.
     */
    public void replaceLeftFragment(Fragment fragment) {
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
    public void replaceRightFragment(Fragment fragment) {
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
