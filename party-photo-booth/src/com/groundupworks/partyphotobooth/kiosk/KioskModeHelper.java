/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.groundupworks.partyphotobooth.R;

/**
 * Helper class to manage flags used by Kiosk mode.
 * 
 * @author Benedict Lau
 */
public class KioskModeHelper {

    /**
     * The {@link Application} context.
     */
    private Context mContext;

    /**
     * Constructor.
     * 
     * @param context
     *            the {@link Context}.
     */
    public KioskModeHelper(Context context) {
        mContext = context.getApplicationContext();
    }

    //
    // Public methods.
    //

    /**
     * Sets the flag to indicate whether Kiosk mode is enabled.
     * 
     * @param isEnabled
     *            true to enable; false to disable.
     */
    public void setKioskMode(boolean isEnabled) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_key), isEnabled);
        editor.commit();
    }

    /**
     * Checks whether Kiosk mode is enabled.
     * 
     * @return true if enabled; false otherwise.
     */
    public boolean isKioskModeEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.pref__kiosk_mode_key), false);
    }
}
