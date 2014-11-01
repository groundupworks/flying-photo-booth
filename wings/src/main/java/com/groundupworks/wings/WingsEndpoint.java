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
import com.groundupworks.wings.core.WingsDbHelper;
import com.groundupworks.wings.core.WingsInjector;

import java.util.Set;

/**
 * An interface to define an endpoint that Wings can share to.
 *
 * @author Benedict Lau
 */
public abstract class WingsEndpoint {

    /**
     * The logger for debug messages.
     */
    protected static final IWingsLogger sLogger = WingsInjector.getLogger();

    /**
     * The {@link android.content.Context} that Wings is running on.
     */
    protected final Context mContext = WingsInjector.getApplicationContext();

    /**
     * The {@link android.os.Handler} to post background tasks.
     */
    protected final Handler mHandler = new Handler(WingsInjector.getWorkerLooper());

    /**
     * The Wings database.
     */
    protected final WingsDbHelper mDatabase = WingsInjector.getDatabase();

    /**
     * The id that is unique to each endpoint.
     *
     * @return the endpoint id.
     */
    public abstract int getEndpointId();

    /**
     * Starts a link request.
     *
     * @param activity the {@link Activity}.
     * @param fragment the {@link Fragment}. May be null.
     */
    public abstract void startLinkRequest(Activity activity, Fragment fragment);

    /**
     * Unlinks an account.
     */
    public abstract void unlink();

    /**
     * Checks if the user is linked.
     *
     * @return true if an account is linked; false otherwise.
     */
    public abstract boolean isLinked();

    /**
     * A convenience method that must be called in the onResume() of any {@link android.app.Activity}
     * or {@link android.support.v4.app.Fragment} that uses
     * {@link #startLinkRequest(android.app.Activity, android.support.v4.app.Fragment)}.
     */
    public abstract void onResumeImpl();

    /**
     * A convenience method that must be called in the onActivityResult() of any {@link android.app.Activity}
     * or {@link android.support.v4.app.Fragment} that uses
     * {@link #startLinkRequest(android.app.Activity, android.support.v4.app.Fragment)}.
     *
     * @param activity    the {@link android.app.Activity}.
     * @param fragment    the {@link android.support.v4.app.Fragment}. May be null.
     * @param requestCode the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  the integer result code returned by the child activity through its setResult().
     * @param data        an Intent, which can return result data to the caller (various data can be attached to Intent
     *                    "extras").
     */
    public abstract void onActivityResultImpl(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data);

    /**
     * Gets the user name associated with the linked account.
     *
     * @return the user name; or null if unlinked.
     */
    public abstract String getLinkedAccountName();

    /**
     * Gets the description associated with the destination.
     *
     * @param destinationId the destination id.
     * @return the destination description; or null if unlinked.
     */
    public abstract String getDestinationDescription(int destinationId);

    /**
     * Process share requests by sharing to the linked account. This should be called in a background
     * thread.
     *
     * @return a set of {@link IWingsNotification}s representing the results of the processed {@link ShareRequest}.
     * May be null or an empty set.
     */
    public abstract Set<IWingsNotification> processShareRequests();
}
