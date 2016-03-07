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
