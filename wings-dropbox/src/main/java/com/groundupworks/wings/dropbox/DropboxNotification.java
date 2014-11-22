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
package com.groundupworks.wings.dropbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.groundupworks.wings.IWingsNotification;
import com.groundupworks.wings.facebook.R;

/**
 * {@link IWingsNotification} for Dropbox shares.
 *
 * @author Benedict Lau
 */
public class DropboxNotification implements IWingsNotification {

    private int mId;

    private String mTitle;

    private String mMessage;

    private String mTicker;

    private String mIntentUri = null;

    /**
     * Package private constructor.
     *
     * @param context   the {@link Context}.
     * @param id        the id of the notification.
     * @param shareUrl  the share url associated with the account.
     * @param shared    the number of successful shares. Must be larger than 0.
     * @param intentUri the uri to display the Dropbox app folder. May be null.
     */
    DropboxNotification(Context context, int id, String shareUrl, int shared, String intentUri) {
        mId = id;
        mTitle = context.getString(R.string.dropbox__notification_shared_title);
        if (shared > 1) {
            mMessage = context.getString(R.string.dropbox__notification_shared_msg_multi, shared, shareUrl);
        } else {
            mMessage = context.getString(R.string.dropbox__notification_shared_msg_single, shareUrl);
        }
        mTicker = context.getString(R.string.dropbox__notification_shared_ticker);
        mIntentUri = intentUri;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    @Override
    public String getTicker() {
        return mTicker;
    }

    @Override
    public Intent getIntent() {
        Intent intent = null;
        if (mIntentUri != null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mIntentUri));
        } else {
            intent = new Intent();
        }
        return intent;
    }
}
