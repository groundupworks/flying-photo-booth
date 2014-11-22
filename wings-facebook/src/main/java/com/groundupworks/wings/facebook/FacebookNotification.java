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
package com.groundupworks.wings.facebook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.groundupworks.wings.IWingsNotification;

/**
 * {@link IWingsNotification} for Facebook shares.
 *
 * @author Benedict Lau
 */
public class FacebookNotification implements IWingsNotification {

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
     * @param albumName the name of the album to share to.
     * @param shared    the number of successful shares. Must be larger than 0.
     * @param intentUri the uri to deep link into the Facebook native app. May be null.
     */
    FacebookNotification(Context context, int id, String albumName, int shared, String intentUri) {
        mId = id;
        mTitle = context.getString(R.string.facebook__notification_shared_title);
        if (shared > 1) {
            mMessage = context.getString(R.string.facebook__notification_shared_msg_multi, shared, albumName);
        } else {
            mMessage = context.getString(R.string.facebook__notification_shared_msg_single, albumName);
        }
        mTicker = context.getString(R.string.facebook__notification_shared_ticker);
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
