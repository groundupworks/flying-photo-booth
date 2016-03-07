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
package com.groundupworks.lib.photobooth.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;

/**
 * Android Beam is only available for JellyBean and above. No action is taken for lower SDK levels or devices with no
 * NFC support. In order for Android Beam to work, both devices must have:
 * <p/>
 * (1) Hardware that supports NFC
 * <p/>
 * (2) Android JellyBean or above
 * <p/>
 * (3) Screen on and unlocked
 * <p/>
 * (4) NFC enabled in Settings
 * <p/>
 * (5) Bluetooth enabled in Settings
 * <p/>
 * (6) Android Beam enabled in Settings
 *
 * @author Benedict Lau
 */
@SuppressLint("NewApi")
public class BeamHelper {

    //
    // Private methods.
    //

    /**
     * Gets the default NFC adapter.
     *
     * @param context the {@link Context}.
     * @return the NFC adapter; or null if the device does not support NFC.
     */
    private static NfcAdapter getNfcAdapter(Context context) {
        NfcAdapter nfcAdapter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context.getApplicationContext());
        }

        return nfcAdapter;
    }

    //
    // Public methods.
    //

    /**
     * Checks if the device supports NFC.
     *
     * @param context the {@link Context}.
     * @return true if hardware supports NFC; false otherwise.
     */
    public static boolean supportsBeam(Context context) {
        return getNfcAdapter(context) != null;
    }

    /**
     * Sets up the NFC adapter to send a list of {@link Uri} with 'file' or 'content' scheme. To clear the NFC adapter
     * of the list of {@link Uri}, null should be passed as the second parameter.
     *
     * @param activity the {@link Activity}.
     * @param uris     the list of {@link Uri} to beam. Pass null to clear NFC adapter.
     */
    public static void beamUris(Activity activity, Uri[] uris) {
        if (activity != null && !activity.isFinishing()) {
            NfcAdapter nfcAdapter = getNfcAdapter(activity);
            if (nfcAdapter != null) {
                try {
                    nfcAdapter.setBeamPushUris(uris, activity);
                } catch (Exception e) {
                    // Do nothing. An exception is thrown if a destroyed Activity is passed.
                }
            }
        }
    }
}
