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

/**
 * The {@link Activity} for setting up the photo booth.
 * 
 * @author Benedict Lau
 */
public class SetupActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable Kiosk mode.
        KioskModeHelper kioskModeHelper = new KioskModeHelper(this);
        kioskModeHelper.transitionState(State.ENABLED);

        // Launch Kiosk mode.
        startService(new Intent(getApplicationContext(), KioskService.class));
        finish();
    }
}
