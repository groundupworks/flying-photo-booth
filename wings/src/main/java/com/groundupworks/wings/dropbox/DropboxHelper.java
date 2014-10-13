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
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AppKeyPair;
import com.groundupworks.wings.R;
import com.groundupworks.wings.IWingsNotification;
import com.groundupworks.wings.ShareRequest;
import com.groundupworks.wings.WingsDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * A helper class for linking and sharing to Dropbox.
 *
 * @author Benedict Lau
 */
public class DropboxHelper {

    /**
     * Name of the folder for storing photo strips.
     */
    private static final String FOLDER_NAME_PHOTO_STRIPS = "Photo Strips";

    /**
     * Path to the directory for storing photo strips.
     */
    private static final String PATH_PHOTO_STRIPS = "/" + FOLDER_NAME_PHOTO_STRIPS;

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
     * @return true if linking is successful; false otherwise.
     */
    private boolean finishLinkRequest() {
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
     * @param context the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    private void link(Context context, final Handler workerHandler) {
        synchronized (mDropboxApiLock) {
            if (mDropboxApi != null) {
                final Context appContext = context.getApplicationContext();
                final DropboxAPI<AndroidAuthSession> dropboxApi = mDropboxApi;

                workerHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String accountName = null;
                        String shareUrl = null;
                        String accessToken = null;

                        // Request params.
                        synchronized (mDropboxApiLock) {
                            // Create directory for storing photo strips.
                            if (createPhotoStripFolder(dropboxApi)) {
                                // Get account params.
                                accountName = requestAccountName(dropboxApi);
                                shareUrl = requestShareUrl(dropboxApi);
                                accessToken = dropboxApi.getSession().getOAuth2AccessToken();
                            }
                        }

                        // Validate account settings and store.
                        if (accountName != null && accountName.length() > 0 && shareUrl != null
                                && shareUrl.length() > 0 && accessToken != null) {
                            storeAccountParams(appContext, accountName, shareUrl, accessToken);
                        } else {
                            // Handle error on ui thread.
                            Handler uiHandler = new Handler(Looper.getMainLooper());
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handleLinkError(appContext, workerHandler);
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
     * @param context the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    private void handleLinkError(Context context, Handler workerHandler) {
        Context appContext = context.getApplicationContext();

        // Show toast to indicate error during linking.
        showLinkError(appContext);

        // Unlink account to ensure proper reset.
        unlink(appContext, workerHandler);
    }

    /**
     * Displays the link error message.
     *
     * @param context the {@link Context}.
     */
    private void showLinkError(Context context) {
        Toast.makeText(context, context.getString(R.string.dropbox__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Creates a directory for photo strips if one does not already exist. If the folder already exists, this call will
     * do nothing.
     *
     * @param dropboxApi the {@link DropboxAPI}.
     * @return true if the directory is created or it already exists; false otherwise.
     */
    private boolean createPhotoStripFolder(DropboxAPI<AndroidAuthSession> dropboxApi) {
        boolean folderCreated = false;
        if (dropboxApi != null) {
            try {
                dropboxApi.createFolder(FOLDER_NAME_PHOTO_STRIPS);
                folderCreated = true;
            } catch (DropboxException e) {
                // Consider the folder created if the folder already exists.
                if (e instanceof DropboxServerException) {
                    folderCreated = DropboxServerException._403_FORBIDDEN == ((DropboxServerException) e).error;
                }
            }
        }
        return folderCreated;
    }

    /**
     * Requests the linked account name.
     *
     * @param dropboxApi the {@link DropboxAPI}.
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
     * @param dropboxApi the {@link DropboxAPI}.
     * @return the url; or null if not linked.
     */
    private String requestShareUrl(DropboxAPI<AndroidAuthSession> dropboxApi) {
        String shareUrl = null;
        if (dropboxApi != null) {
            try {
                shareUrl = dropboxApi.share(PATH_PHOTO_STRIPS).url;
            } catch (DropboxException e) {
                // Do nothing.
            }
        }
        return shareUrl;
    }

    /**
     * Stores the account params in persisted storage.
     *
     * @param context     the {@link Context}.
     * @param accountName the user name associated with the account.
     * @param shareUrl    the share url associated with the account.
     * @param accessToken the access token.
     */
    private void storeAccountParams(Context context, String accountName, String shareUrl, String accessToken) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putString(appContext.getString(R.string.dropbox__account_name_key), accountName);
        editor.putString(appContext.getString(R.string.dropbox__share_url_key), shareUrl);
        editor.putString(appContext.getString(R.string.dropbox__access_token_key), accessToken);

        // Set preference to linked.
        editor.putBoolean(appContext.getString(R.string.pref__dropbox_link_key), true);
        editor.apply();
    }

    /**
     * Removes the account params from persisted storage.
     *
     * @param context the {@link Context}.
     */
    private void removeAccountParams(Context context) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.remove(appContext.getString(R.string.dropbox__account_name_key));
        editor.remove(appContext.getString(R.string.dropbox__share_url_key));
        editor.remove(appContext.getString(R.string.dropbox__access_token_key));

        // Set preference to unlinked.
        editor.putBoolean(appContext.getString(R.string.pref__dropbox_link_key), false);
        editor.apply();
    }

    /**
     * Gets the stored access token associated with the linked account.
     *
     * @param context the {@link Context}.
     * @return the access token; or null if unlinked.
     */
    private String getLinkedAccessToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.dropbox__access_token_key), null);
    }

    //
    // Public methods.
    //

    /**
     * Starts a link request. The user will be authenticated through the native Dropbox app or the default web browser.
     *
     * @param context the {@link Context}.
     */
    public void startLinkRequest(Context context) {
        mIsLinkRequested = true;

        AppKeyPair appKeyPair = new AppKeyPair(context.getString(R.string.dropbox__app_key),
                context.getString(R.string.dropbox__app_secret));
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        synchronized (mDropboxApiLock) {
            mDropboxApi = new DropboxAPI<AndroidAuthSession>(session);
            mDropboxApi.getSession().startOAuth2Authentication(context);
        }
    }

    /**
     * Unlinks an account.
     *
     * @param context the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    public void unlink(Context context, Handler workerHandler) {
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
     * @param context the {@link Context}.
     */
    public boolean isLinked(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(appContext.getString(R.string.pref__dropbox_link_key), false);
    }

    /**
     * Checks if the user has auto share turned on for Dropbox.
     *
     * @param context the {@link Context}.
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
     * @param context the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     */
    public void onResumeImpl(Context context, Handler workerHandler) {
        if (mIsLinkRequested) {
            // Check if link request was successful.
            if (finishLinkRequest()) {
                link(context, workerHandler);
            } else {
                handleLinkError(context, workerHandler);
            }
        }
    }

    /**
     * Gets the user name associated with the linked account.
     *
     * @param context the {@link Context}.
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
     * @param context the {@link Context}.
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
     * @param context the {@link Context}.
     * @param workerHandler a {@link android.os.Handler} to post background tasks.
     * @return a {@link IWingsNotification} representing the results of the processed {@link ShareRequest}. May be null.
     */
    public IWingsNotification processShareRequests(Context context, Handler workerHandler) {
        int shared = 0;

        // Get access token associated with the linked account.
        String accessToken = getLinkedAccessToken(context);
        String shareUrl = getLinkedShareUrl(context);
        if (accessToken != null && shareUrl != null) {
            // Get share requests for Dropbox.
            WingsDbHelper wingsDbHelper = WingsDbHelper.getInstance(context);
            List<ShareRequest> shareRequests = wingsDbHelper.checkoutShareRequests(ShareRequest.DESTINATION_DROPBOX);

            if (!shareRequests.isEmpty()) {
                // Start new session with the persisted access token.
                AppKeyPair appKeys = new AppKeyPair(context.getString(R.string.dropbox__app_key),
                        context.getString(R.string.dropbox__app_secret));
                AndroidAuthSession session = new AndroidAuthSession(appKeys);
                session.setOAuth2AccessToken(accessToken);
                DropboxAPI<AndroidAuthSession> dropboxApi = new DropboxAPI<AndroidAuthSession>(session);

                // Process share requests.
                for (ShareRequest shareRequest : shareRequests) {
                    File file = new File(shareRequest.getFilePath());
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);

                        // Upload file.
                        dropboxApi.putFile(PATH_PHOTO_STRIPS + "/" + file.getName(), inputStream, file.length(), null,
                                null);

                        // Mark as successfully processed.
                        wingsDbHelper.markSuccessful(shareRequest.getId());

                        shared++;
                    } catch (DropboxUnlinkedException e) {
                        wingsDbHelper.markFailed(shareRequest.getId());

                        // Update account linking state to unlinked.
                        unlink(context, workerHandler);
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

        // Construct notification representing share results.
        DropboxNotification notification = null;
        if (shared > 0) {
            notification = new DropboxNotification(context, shareUrl, shared, shareUrl);
        }

        return notification;
    }
}