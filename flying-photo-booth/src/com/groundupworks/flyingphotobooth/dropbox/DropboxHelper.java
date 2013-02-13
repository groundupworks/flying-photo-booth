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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
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

/**
 * A helper class for using functionalities from the Dropbox SDK.
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

    //
    // Private methods.
    //

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
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        synchronized (mDropboxApiLock) {
            mDropboxApi = new DropboxAPI<AndroidAuthSession>(session);
            mDropboxApi.getSession().startAuthentication(context);
        }
    }

    /**
     * Finishes a link request.
     * 
     * 
     * @param context
     *            the {@link Context}.
     * @return true if linking is successful; false otherwise.
     */
    public boolean finishLinkRequest(Context context) {
        boolean isSuccessful = false;

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
        return isSuccessful;
    }

    /**
     * Links an account in a background thread.
     * 
     * @param context
     *            the {@link Context}.
     */
    public void link(Context context) {
        synchronized (mDropboxApiLock) {
            if (mDropboxApi != null) {
                final Context appContext = context;
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

                        // Persist account params.
                        if (accountName != null && shareUrl != null && accessTokenPair != null) {
                            storeAccountParams(appContext, accountName, shareUrl, accessTokenPair.key,
                                    accessTokenPair.secret);
                        }

                    }
                });
            }
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
    }

    /**
     * Displays the link error message.
     * 
     * @param context
     *            the {@link Context}.
     */
    public void showLinkError(Context context) {
        Toast.makeText(context, context.getString(R.string.dropbox__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks if the user is linked to Dropbox.
     * 
     * @param context
     *            the {@link Context}.
     */
    public boolean isLinked(Context context) {
        return getLinkedAccessToken(context) != null;
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
     * Shares an image to the linked account.
     * 
     * @param context
     *            the {@link Context}.
     * @param file
     *            the {@link File} to share.
     */
    public void share(Context context, File file) {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);

        // Gets access token associated with the linked account.
        AccessTokenPair accessToken = getLinkedAccessToken(context);
        if (accessToken != null) {
            // Start new session with the persisted access token.
            session.setAccessTokenPair(accessToken);
            DropboxAPI<AndroidAuthSession> dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

            // Upload file.
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                dropboxApi.putFile(file.getName(), inputStream, file.length(), null, null);
                // TODO Mark file as sent in db.
            } catch (DropboxUnlinkedException e) {
                // Do nothing.
            } catch (DropboxException e) {
                // Do nothing.
            } catch (FileNotFoundException e) {
                // Do nothing.
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