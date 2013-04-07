/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.CaptureFragment;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;

/**
 * {@link Activity} that puts the device in Kiosk mode. This should only be launched from the {@link KioskService}.
 * 
 * @author Benedict Lau
 */
public class KioskActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show on top of lock screen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_kiosk);

        // Configure button to exit Kiosk mode.
        final KioskModeHelper helper = new KioskModeHelper(this);
        ImageView exitButton = (ImageView) findViewById(R.id.kiosk_exit_button);
        exitButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (helper.isPasswordRequired()) {
                    showDialogFragment(KioskPasswordDialogFragment.newInstance());
                } else {
                    exitKioskMode(helper);
                }
                return true;
            }
        });

        // Choose Fragment to start with based on whether Kiosk mode setup has completed.
        if (helper.isSetupCompleted()) {
            replaceFragment(CaptureFragment.newInstance(), false, true);
        } else {
            replaceFragment(KioskSetupFragment.newInstance(), false, true);
        }
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
    // Package private methods.
    //

    /**
     * Exits Kiosk mode.
     * 
     * @param helper
     *            a {@link KioskModeHelper}.
     */
    void exitKioskMode(KioskModeHelper helper) {
        // Disable Kiosk mode.
        helper.transitionState(State.DISABLED);

        // Finish KioskActivity.
        finish();
    }
}
