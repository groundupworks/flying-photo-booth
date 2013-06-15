/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.groundupworks.lib.photobooth.framework.BaseFragmentActivity;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper;
import com.groundupworks.partyphotobooth.kiosk.KioskModeHelper.State;
import com.groundupworks.partyphotobooth.kiosk.KioskService;
import com.groundupworks.partyphotobooth.setup.fragments.EventInfoSetupFragment;
import com.groundupworks.partyphotobooth.setup.fragments.PhotoBoothSetupFragment;
import com.groundupworks.partyphotobooth.setup.fragments.ShareServicesSetupFragment;

/**
 * The {@link Activity} for setting up the photo booth for an event.
 * 
 * @author Benedict Lau
 */
public class SetupActivity extends BaseFragmentActivity implements EventInfoSetupFragment.ICallbacks,
        PhotoBoothSetupFragment.ICallbacks, ShareServicesSetupFragment.ICallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launch event info setup on startup.
        if (savedInstanceState == null) {
            replaceFragment(EventInfoSetupFragment.newInstance(), false, false);
        }
    }

    @Override
    public void onEventInfoSetupCompleted() {
        replaceFragment(PhotoBoothSetupFragment.newInstance(), true, false);
    }

    @Override
    public void onPhotoBoothSetupCompleted() {
        replaceFragment(ShareServicesSetupFragment.newInstance(), true, false);
    }

    @Override
    public void onShareServicesSetupCompleted() {
        // Enable Kiosk mode.
        KioskModeHelper kioskModeHelper = new KioskModeHelper(this);
        kioskModeHelper.transitionState(State.ENABLED);

        // Launch Kiosk mode.
        startService(new Intent(getApplicationContext(), KioskService.class));
        finish();
    }
}
