/*
 * Copyright (C) 2014 Benedict Lau
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
package com.groundupworks.wings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.groundupworks.wings.core.ShareRequest;

/**
 * An interface to define an endpoint that Wings can share to.
 *
 * @author Benedict Lau
 */
public interface IWingsEndpoint {

    /**
     * Starts a link request.
     *
     * @param activity      the {@link Activity}.
     * @param fragment      the {@link Fragment}. May be null.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    void startLinkRequest(Activity activity, Fragment fragment, Handler workerHandler);

    /**
     * Unlinks an account.
     *
     * @param context       the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    void unlink(Context context, Handler workerHandler);

    /**
     * Checks if the user is linked.
     *
     * @param context the {@link Context}.
     * @return true if an account is linked; false otherwise.
     */
    boolean isLinked(Context context);

    /**
     * A convenience method that must be called in the onResume() of any {@link android.app.Activity}
     * or {@link android.support.v4.app.Fragment} that uses
     * {@link #startLinkRequest(android.app.Activity, android.support.v4.app.Fragment, android.os.Handler)}.
     *
     * @param context       the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    void onResumeImpl(Context context, Handler workerHandler);

    /**
     * A convenience method that must be called in the onActivityResult() of any {@link android.app.Activity}
     * or {@link android.support.v4.app.Fragment} that uses
     * {@link #startLinkRequest(android.app.Activity, android.support.v4.app.Fragment, android.os.Handler)}.
     *
     * @param activity      the {@link android.app.Activity}.
     * @param fragment      the {@link android.support.v4.app.Fragment}. May be null.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     * @param requestCode   the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *                      this result came from.
     * @param resultCode    the integer result code returned by the child activity through its setResult().
     * @param data          an Intent, which can return result data to the caller (various data can be attached to Intent
     *                      "extras").
     */
    void onActivityResultImpl(Activity activity, Fragment fragment, Handler workerHandler, int requestCode, int resultCode, Intent data);

    /**
     * Gets the user name associated with the linked account.
     *
     * @param context the {@link Context}.
     * @return the user name; or null if unlinked.
     */
    String getLinkedAccountName(Context context);

    /**
     * Gets the endpoint description associated with the linked account.
     *
     * @param context the {@link Context}.
     * @return the endpoint description; or null if unlinked.
     */
    String getDestinationDescription(Context context);

    /**
     * Process share requests by sharing to the linked account. This should be called in a background
     * thread.
     *
     * @param context       the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     * @return a {@link IWingsNotification} representing the results of the processed {@link ShareRequest}. May be null.
     */
    IWingsNotification processShareRequests(Context context, Handler workerHandler);
}
