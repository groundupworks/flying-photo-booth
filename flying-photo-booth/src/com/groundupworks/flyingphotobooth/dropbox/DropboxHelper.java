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
package com.groundupworks.flyingphotobooth.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.groundupworks.flyingphotobooth.MyApplication;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.wings.ShareRequest;
import com.groundupworks.flyingphotobooth.wings.WingsDbHelper;

/**
 * A helper class for linking and sharing to Dropbox.
 * 
 * @author Benedict Lau
 */
public class DropboxHelper {

    /**
     * The unique application key.
     */
    final static private String APP_KEY = "io2pka2ev0f6xwh";

    /**
     * The unique application secret.
     */
    final static private String APP_SECRET = "r76x7h1eva7p3f2";

    /**
     * Limit access level to a specific folder in the user's Dropbox.
     */
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    /**
     * A lock object used to synchronize access on {@link #mDropboxApi}.
     */
    private Object mDropboxApiLock = new Object();

    /**
     * The Dropbox API. Access is synchronized on the {@link #mDropboxApiLock}.
     */
    private DropboxAPI<AndroidAuthSession> mDropboxApi = null;

    /**
     * Flag to track if a link request is started.
     */
    private boolean mIsLinkRequested = false;

    //
    // Private methods.
    //

    /**
     * Finishes a link request. Does nothing if {@link #isLinkRequested()} is false prior to this call.
     * 
     * 
     * @param context
     *            the {@link Context}.
     * @return true if linking is successful; false otherwise.
     */
    private boolean finishLinkRequest(Context context) {
        boolean isSuccessful = false;

        if (mIsLinkRequested) {
            synchronized (mDropboxApiLock) {
                if (mDropboxApi != null) {
                    AndroidAuthSession session = mDropboxApi.getSession();
                    if (session.authenticationSuccessful()) {
                        try {
                            // Set access token on the session.
                            session.finishAuthentication();
                            isSuccessful = true;
                        } catch (IllegalStateException e) {
                            // Do nothing.
                        }
                    }
                }
            }

            // Reset flag.
            mIsLinkRequested = false;
        }
        return isSuccessful;
    }

    /**
     * Links an account in a background thread. If unsuccessful, the link error is handled on a ui thread and a
     * {@link Toast} will be displayed.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void link(Context context) {
        synchronized (mDropboxApiLock) {
            if (mDropboxApi != null) {
                final Context appContext = context.getApplicationContext();
                final DropboxAPI<AndroidAuthSession> dropboxApi = mDropboxApi;

                Handler workerHandler = new Handler(MyApplication.getWorkerLooper());
                workerHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String accountName = null;
                        String shareUrl = null;
                        AccessTokenPair accessTokenPair = null;

                        // Request params.
                        synchronized (mDropboxApiLock) {
                            // Get account params.
                            accountName = requestAccountName(dropboxApi);
                            shareUrl = requestShareUrl(dropboxApi);
                            accessTokenPair = dropboxApi.getSession().getAccessTokenPair();
                        }

                        // Validate account settings and store.
                        if (accountName != null && accountName.length() > 0 && shareUrl != null
                                && shareUrl.length() > 0 && accessTokenPair != null) {
                            storeAccountParams(appContext, accountName, shareUrl, accessTokenPair.key,
                                    accessTokenPair.secret);
                        } else {
                            // Handle error on ui thread.
                            Handler uiHandler = new Handler(Looper.getMainLooper());
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handleLinkError(appContext);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    /**
     * Handles an error case during the linking process.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void handleLinkError(Context context) {
        Context appContext = context.getApplicationContext();

        // Show toast to indicate error during linking.
        showLinkError(appContext);

        // Unlink account to ensure proper reset.
        unlink(appContext);
    }

    /**
     * Displays the link error message.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void showLinkError(Context context) {
        Toast.makeText(context, context.getString(R.string.dropbox__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Requests the linked account name.
     * 
     * @param dropboxApi
     *            the {@link DropboxAPI}.
     * @return the account name; or null if not linked.
     */
    private String requestAccountName(DropboxAPI<AndroidAuthSession> dropboxApi) {
        String accountName = null;
        if (dropboxApi != null) {
            try {
                accountName = dropboxApi.accountInfo().displayName;
            } catch (DropboxException e) {
                // Do nothing.
            }
        }
        return accountName;
    }

    /**
     * Requests the share url of the linked folder.
     * 
     * @param dropboxApi
     *            the {@link DropboxAPI}.
     * @return the url; or null if not linked.
     */
    private String requestShareUrl(DropboxAPI<AndroidAuthSession> dropboxApi) {
        String shareUrl = null;
        if (dropboxApi != null) {
            try {
                shareUrl = dropboxApi.share("").url;
            } catch (DropboxException e) {
                // Do nothing.
            }
        }
        return shareUrl;
    }

    /**
     * Stores the account params in persisted storage.
     * 
     * @param context
     *            the {@link Context}.
     * @param accountName
     *            the user name associated with the account.
     * @param shareUrl
     *            the share url associated with the account.
     * @param accessTokenKey
     *            the access token key.
     * @param accessTokenSecret
     *            the access token secret.
     */
    private void storeAccountParams(Context context, String accountName, String shareUrl, String accessTokenKey,
            String accessTokenSecret) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putString(appContext.getString(R.string.dropbox__account_name_key), accountName);
        editor.putString(appContext.getString(R.string.dropbox__share_url_key), shareUrl);
        editor.putString(appContext.getString(R.string.dropbox__access_token_key_key), accessTokenKey);
        editor.putString(appContext.getString(R.string.dropbox__access_token_secret_key), accessTokenSecret);

        // Set preference to linked.
        editor.putBoolean(appContext.getString(R.string.pref__dropbox_link_key), true);
        editor.apply();
    }

    /**
     * Removes the account params from persisted storage.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void removeAccountParams(Context context) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.remove(appContext.getString(R.string.dropbox__account_name_key));
        editor.remove(appContext.getString(R.string.dropbox__share_url_key));
        editor.remove(appContext.getString(R.string.dropbox__access_token_key_key));
        editor.remove(appContext.getString(R.string.dropbox__access_token_secret_key));

        // Set preference to unlinked.
        editor.putBoolean(appContext.getString(R.string.pref__dropbox_link_key), false);
        editor.apply();
    }

    /**
     * Gets the stored access token associated with the linked account.
     * 
     * @param context
     *            the {@link Context}.
     * @return the access token; or null if unlinked.
     */
    private AccessTokenPair getLinkedAccessToken(Context context) {
        AccessTokenPair accessTokenPair = null;

        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = preferences.getString(appContext.getString(R.string.dropbox__access_token_key_key), null);
        String secret = preferences.getString(appContext.getString(R.string.dropbox__access_token_secret_key), null);
        if (key != null && secret != null) {
            accessTokenPair = new AccessTokenPair(key, secret);
        }
        return accessTokenPair;
    }

    //
    // Public methods.
    //

    /**
     * Starts a link request. The user will be authenticated through the native Dropbox app or the default web browser.
     * 
     * @param context
     *            the {@link Context}.
     */
    public void startLinkRequest(Context context) {
        mIsLinkRequested = true;

        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        synchronized (mDropboxApiLock) {
            mDropboxApi = new DropboxAPI<AndroidAuthSession>(session);
            mDropboxApi.getSession().startAuthentication(context);
        }
    }

    /**
     * Unlinks an account.
     * 
     * @param context
     *            the {@link Context}.
     */
    public void unlink(Context context) {
        // Unlink in persisted storage.
        removeAccountParams(context);

        // Unlink any current session.
        synchronized (mDropboxApiLock) {
            if (mDropboxApi != null) {
                AndroidAuthSession session = mDropboxApi.getSession();
                if (session.isLinked()) {
                    session.unlink();
                    mDropboxApi = null;
                }
            }
        }

        // Remove existing share requests in a background thread.
        final Context appContext = context.getApplicationContext();
        Handler workerHandler = new Handler(MyApplication.getWorkerLooper());
        workerHandler.post(new Runnable() {

            @Override
            public void run() {
                WingsDbHelper.getInstance(appContext).deleteShareRequests(ShareRequest.DESTINATION_DROPBOX);
            }
        });
    }

    /**
     * Checks if the user is linked to Dropbox.
     * 
     * @param context
     *            the {@link Context}.
     */
    public boolean isLinked(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(appContext.getString(R.string.pref__dropbox_link_key), false);
    }

    /**
     * Checks if the user has auto share turned on for Dropbox.
     * 
     * @param context
     *            the {@link Context}.
     */
    public boolean isAutoShare(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(appContext.getString(R.string.pref__dropbox_link_key), false)
                && preferences.getBoolean(appContext.getString(R.string.pref__dropbox_auto_share_key), false);
    }

    /**
     * A convenience method to be called in the onResume() of any {@link Activity} or {@link Fragment} that uses
     * {@link #startLinkRequest(Context)}.
     * 
     * @param context
     *            the {@link Context}.
     */
    public void onResumeImpl(Context context) {
        if (mIsLinkRequested) {
            // Check if link request was successful.
            if (finishLinkRequest(context)) {
                link(context);
            } else {
                handleLinkError(context);
            }
        }
    }

    /**
     * Gets the user name associated with the linked account.
     * 
     * @param context
     *            the {@link Context}.
     * @return the user name; or null if unlinked.
     */
    public String getLinkedAccountName(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.dropbox__account_name_key), null);
    }

    /**
     * Gets the share url associated with the linked account.
     * 
     * @param context
     *            the {@link Context}.
     * @return the url; or null if unlinked.
     */
    public String getLinkedShareUrl(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.dropbox__share_url_key), null);
    }

    /**
     * Process share requests by sharing to the linked account. This should be called in a background thread.
     * 
     * @param context
     *            the {@link Context}.
     * @return the number of successfully shared items.
     */
    public int processShareRequests(Context context) {
        int shared = 0;

        // Get access token associated with the linked account.
        AccessTokenPair accessToken = getLinkedAccessToken(context);
        if (accessToken != null) {
            // Get share requests for Dropbox.
            WingsDbHelper wingsDbHelper = WingsDbHelper.getInstance(context);
            List<ShareRequest> shareRequests = wingsDbHelper.checkoutShareRequests(ShareRequest.DESTINATION_DROPBOX);

            if (!shareRequests.isEmpty()) {
                // Start new session with the persisted access token.
                AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
                AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
                session.setAccessTokenPair(accessToken);
                DropboxAPI<AndroidAuthSession> dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

                // Process share requests.
                for (ShareRequest shareRequest : shareRequests) {
                    File file = new File(shareRequest.getFilePath());
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);

                        // Upload file.
                        dropboxApi.putFile(file.getName(), inputStream, file.length(), null, null);

                        // Mark as successfully processed.
                        wingsDbHelper.markSuccessful(shareRequest.getId());

                        shared++;
                    } catch (DropboxUnlinkedException e) {
                        wingsDbHelper.markFailed(shareRequest.getId());

                        // Update account linking state to unlinked.
                        unlink(context);
                    } catch (DropboxException e) {
                        wingsDbHelper.markFailed(shareRequest.getId());
                    } catch (IllegalArgumentException e) {
                        wingsDbHelper.markFailed(shareRequest.getId());
                    } catch (FileNotFoundException e) {
                        wingsDbHelper.markFailed(shareRequest.getId());
                    } catch (Exception e) {
                        // Safety.
                        wingsDbHelper.markFailed(shareRequest.getId());
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                // Do nothing.
                            }
                        }
                    }
                }
            }
        }

        return shared;
    }
}