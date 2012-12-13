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
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;

/**
 * Android Beam is only available for JellyBean and above. No action is taken for lower SDK levels or devices with no
 * NFC support. In order for Android Beam to work, both devices must have:
 * 
 * (1) Hardware that supports NFC.
 * 
 * (2) Android JellyBean or above.
 * 
 * (3) NFC enabled in Settings.
 * 
 * (4) Bluetooth enabled in Settings.
 * 
 * (5) Android Beam enabled in Settings.
 * 
 * @author Benedict Lau
 */
@SuppressLint("NewApi")
public class BeamHelper {

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());
                if (nfcAdapter != null) {
                    nfcAdapter.setBeamPushUris(uris, activity);
                }
            }
        }
    }
}
