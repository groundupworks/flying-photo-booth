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
 * Helper class to manage Kiosk mode.
 *
 * @author Benedict Lau
 */
public class KioskModeHelper {

    /**
     * States in the Kiosk mode lifecycle.
     */
    public static enum State {
        ENABLED, SETUP_COMPLETED, DISABLED
    }

    /**
     * The {@link Application} context.
     */
    private Context mContext;

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     */
    public KioskModeHelper(Context context) {
        mContext = context.getApplicationContext();
    }

    //
    // Public methods.
    //

    /**
     * Initiates a Kiosk mode state transition.
     *
     * @param state the {@link KioskModeHelper.State} to transition to.
     */
    public void transitionState(State state) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();

        switch (state) {
            case ENABLED:
                editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_enabled_key), true);
                editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_setup_completed_key), false);
                editor.putString(mContext.getString(R.string.pref__kiosk_mode_password_key), "");
                break;
            case SETUP_COMPLETED:
                editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_setup_completed_key), true);
                break;
            case DISABLED:
                editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_enabled_key), false);
                editor.putBoolean(mContext.getString(R.string.pref__kiosk_mode_setup_completed_key), false);
                editor.putString(mContext.getString(R.string.pref__kiosk_mode_password_key), "");
                break;
            default:
        }

        editor.apply();
    }

    /**
     * Checks whether Kiosk mode is enabled.
     *
     * @return true if enabled; false otherwise.
     */
    public boolean isEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.pref__kiosk_mode_enabled_key), false);
    }

    /**
     * Checks whether Kiosk mode setup is completed.
     *
     * @return true if completed; false otherwise.
     */
    public boolean isSetupCompleted() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.pref__kiosk_mode_setup_completed_key), false);
    }

    /**
     * Sets the Kiosk mode password.
     *
     * @param password the password to use.
     */
    public void setPassword(String password) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString(mContext.getString(R.string.pref__kiosk_mode_password_key), password);
        editor.apply();
    }

    /**
     * Checks whether Kiosk mode requires a password to unlock.
     *
     * @return true if password is required; false otherwise.
     */
    public boolean isPasswordRequired() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return !preferences.getString(mContext.getString(R.string.pref__kiosk_mode_password_key), "").equals("");
    }

    /**
     * Verifies the Kiosk mode password.
     *
     * @param password the password to verify.
     * @return true if password matches; false otherwise.
     */
    public boolean verifyPassword(String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.pref__kiosk_mode_password_key), "").equals(password);
    }
}
