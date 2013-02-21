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
package com.groundupworks.flyingphotobooth.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.groundupworks.flyingphotobooth.BaseFragmentActivity;

/**
 * {@link Activity} to configure how a Facebook account is linked.
 * 
 * @author Benedict Lau
 */
public class FacebookSettingsActivity extends BaseFragmentActivity {

    /**
     * Flag to track if any error has occurred.
     */
    boolean mHasErrorOccurred = false;

    //
    // Required Facebook settings.
    //

    String mAccountName = null;

    String mPhotoPrivacy = null;

    String mAlbumName = null;

    String mAlbumGraphPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        replaceFragment(FacebookAlbumListFragment.newInstance(), false, true);
    }

    //
    // Package private methods.
    //

    /**
     * Finish the {@link FacebookSettingsActivity} if the conditions are met.
     */
    void tryFinish() {
        // Return the RESULT_CANCELED result code if an has error occurred.
        if (mHasErrorOccurred) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Return the results if all settings are filled.
        if (mAccountName != null && mAlbumName != null && mAlbumGraphPath != null) {
            FacebookSettings settings = FacebookSettings.newInstance(mAccountName, mPhotoPrivacy, mAlbumName,
                    mAlbumGraphPath);
            if (settings != null) {
                Intent result = new Intent();
                result.putExtras(settings.toBundle());
                setResult(RESULT_OK, result);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
