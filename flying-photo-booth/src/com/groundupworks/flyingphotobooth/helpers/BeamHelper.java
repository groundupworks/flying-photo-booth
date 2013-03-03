/*
 * Copyright (C) 2012 Benedict Lau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groundupworks.flyingphotobooth.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;

/**
 * Android Beam is only available for JellyBean and above. No action is taken for lower SDK levels or devices with no
 * NFC support. In order for Android Beam to work, both devices must have:
 * 
 * (1) Hardware that supports NFC
 * 
 * (2) Android JellyBean or above
 * 
 * (3) Screen on and unlocked
 * 
 * (4) NFC enabled in Settings
 * 
 * (5) Bluetooth enabled in Settings
 * 
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
     * @param context
     *            the {@link Context}.
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
     * @param context
     *            the {@link Context}.
     * @return true if hardware supports NFC; false otherwise.
     */
    public static boolean supportsBeam(Context context) {
        return getNfcAdapter(context) != null;
    }

    /**
     * Sets up the NFC adapter to send a list of {@link Uri} with 'file' or 'content' scheme. To clear the NFC adapter
     * of the list of {@link Uri}, null should be passed as the second parameter.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param uris
     *            the list of {@link Uri} to beam. Pass null to clear NFC adapter.
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
