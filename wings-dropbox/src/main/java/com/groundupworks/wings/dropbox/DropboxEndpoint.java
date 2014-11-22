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
import android.content.Intent;
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
import com.groundupworks.wings.IWingsNotification;
import com.groundupworks.wings.WingsDestination;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.core.ShareRequest;
import com.groundupworks.wings.facebook.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Wings endpoint for Dropbox.
 *
 * @author Benedict Lau
 */
public class DropboxEndpoint extends WingsEndpoint {

    /**
     * Dropbox endpoint id.
     */
    private static final int ENDPOINT_ID = 1;

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
     * Finishes a link request. Does nothing if {@link #mIsLinkRequested} is false prior to this call.
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
     */
    private void link() {
        synchronized (mDropboxApiLock) {
            if (mDropboxApi != null) {
                final DropboxAPI<AndroidAuthSession> dropboxApi = mDropboxApi;

                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        String accountName = null;
                        String shareUrl = null;
                        String accessToken = null;

                        // Request params.
                        synchronized (mDropboxApiLock) {
                            // Create directory for storing photos.
                            if (createPhotoFolder(dropboxApi)) {
                                // Get account params.
                                accountName = requestAccountName(dropboxApi);
                                shareUrl = requestShareUrl(dropboxApi);
                                accessToken = dropboxApi.getSession().getOAuth2AccessToken();
                            }
                        }

                        // Validate account settings and store.
                        if (accountName != null && accountName.length() > 0 && shareUrl != null
                                && shareUrl.length() > 0 && accessToken != null) {
                            storeAccountParams(accountName, shareUrl, accessToken);
                        } else {
                            // Handle error on ui thread.
                            Handler uiHandler = new Handler(Looper.getMainLooper());
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handleLinkError();
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
     */
    private void handleLinkError() {
        // Show toast to indicate error during linking.
        showLinkError();

        // Unlink account to ensure proper reset.
        unlink();
    }

    /**
     * Displays the link error message.
     */
    private void showLinkError() {
        Toast.makeText(mContext, mContext.getString(R.string.dropbox__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Creates a directory for photos if one does not already exist. If the folder already exists, this call will
     * do nothing.
     *
     * @param dropboxApi the {@link DropboxAPI}.
     * @return true if the directory is created or it already exists; false otherwise.
     */
    private boolean createPhotoFolder(DropboxAPI<AndroidAuthSession> dropboxApi) {
        boolean folderCreated = false;
        if (dropboxApi != null) {
            try {
                dropboxApi.createFolder(mContext.getString(R.string.dropbox__photo_folder));
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
                shareUrl = dropboxApi.share("/" + mContext.getString(R.string.dropbox__photo_folder)).url;
            } catch (DropboxException e) {
                // Do nothing.
            }
        }
        return shareUrl;
    }

    /**
     * Stores the account params in persisted storage.
     *
     * @param accountName the user name associated with the account.
     * @param shareUrl    the share url associated with the account.
     * @param accessToken the access token.
     */
    private void storeAccountParams(String accountName, String shareUrl, String accessToken) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString(mContext.getString(R.string.dropbox__account_name_key), accountName);
        editor.putString(mContext.getString(R.string.dropbox__share_url_key), shareUrl);
        editor.putString(mContext.getString(R.string.dropbox__access_token_key), accessToken);

        // Set preference to linked.
        editor.putBoolean(mContext.getString(R.string.dropbox__link_key), true);
        editor.apply();
    }

    /**
     * Removes the account params from persisted storage.
     */
    private void removeAccountParams() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.remove(mContext.getString(R.string.dropbox__account_name_key));
        editor.remove(mContext.getString(R.string.dropbox__share_url_key));
        editor.remove(mContext.getString(R.string.dropbox__access_token_key));

        // Set preference to unlinked.
        editor.putBoolean(mContext.getString(R.string.dropbox__link_key), false);
        editor.apply();
    }

    /**
     * Gets the stored access token associated with the linked account.
     *
     * @return the access token; or null if unlinked.
     */
    private String getLinkedAccessToken() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.dropbox__access_token_key), null);
    }

    /**
     * Gets the share url associated with the linked account.
     *
     * @return the url; or null if unlinked.
     */
    private String getLinkedShareUrl() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.dropbox__share_url_key), null);
    }

    //
    // Public methods.
    //

    @Override
    public int getEndpointId() {
        return ENDPOINT_ID;
    }

    @Override
    public void startLinkRequest(Activity activity, Fragment fragment) {
        mIsLinkRequested = true;

        AppKeyPair appKeyPair = new AppKeyPair(mContext.getString(R.string.dropbox__app_key),
                mContext.getString(R.string.dropbox__app_secret));
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        synchronized (mDropboxApiLock) {
            mDropboxApi = new DropboxAPI<AndroidAuthSession>(session);
            mDropboxApi.getSession().startOAuth2Authentication(mContext);
        }
    }

    @Override
    public void unlink() {
        // Unlink in persisted storage.
        removeAccountParams();

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
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mDatabase.deleteShareRequests(new WingsDestination(DestinationId.APP_FOLDER, ENDPOINT_ID));
            }
        });
    }

    @Override
    public boolean isLinked() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.dropbox__link_key), false);
    }

    @Override
    public void onResumeImpl() {
        if (mIsLinkRequested) {
            // Check if link request was successful.
            if (finishLinkRequest()) {
                link();
            } else {
                handleLinkError();
            }
        }
    }

    @Override
    public void onActivityResultImpl(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data) {
        // Do nothing.
    }

    @Override
    public String getLinkedAccountName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.dropbox__account_name_key), null);
    }

    @Override
    public String getDestinationDescription(int destinationId) {
        String destinationDescription = null;
        String accountName = getLinkedAccountName();
        String shareUrl = getLinkedShareUrl();
        if (accountName != null && accountName.length() > 0 && shareUrl != null && shareUrl.length() > 0) {
            destinationDescription = mContext.getString(R.string.dropbox__destination_description, accountName, shareUrl);
        }
        return destinationDescription;
    }

    @Override
    public Set<IWingsNotification> processShareRequests() {
        Set<IWingsNotification> notifications = new HashSet<IWingsNotification>();

        // Get access token associated with the linked account.
        String accessToken = getLinkedAccessToken();
        String shareUrl = getLinkedShareUrl();
        if (accessToken != null && shareUrl != null) {
            // Get share requests for Dropbox.
            WingsDestination destination = new WingsDestination(DestinationId.APP_FOLDER, ENDPOINT_ID);
            List<ShareRequest> shareRequests = mDatabase.checkoutShareRequests(destination);
            int shared = 0;

            if (!shareRequests.isEmpty()) {
                // Start new session with the persisted access token.
                AppKeyPair appKeys = new AppKeyPair(mContext.getString(R.string.dropbox__app_key),
                        mContext.getString(R.string.dropbox__app_secret));
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
                        dropboxApi.putFile("/" + mContext.getString(R.string.dropbox__photo_folder) + "/" + file.getName(), inputStream, file.length(), null,
                                null);

                        // Mark as successfully processed.
                        mDatabase.markSuccessful(shareRequest.getId());

                        shared++;
                    } catch (DropboxUnlinkedException e) {
                        mDatabase.markFailed(shareRequest.getId());

                        // Update account linking state to unlinked.
                        unlink();
                    } catch (DropboxException e) {
                        mDatabase.markFailed(shareRequest.getId());
                    } catch (IllegalArgumentException e) {
                        mDatabase.markFailed(shareRequest.getId());
                    } catch (FileNotFoundException e) {
                        mDatabase.markFailed(shareRequest.getId());
                    } catch (Exception e) {
                        // Safety.
                        mDatabase.markFailed(shareRequest.getId());
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

            // Construct and add notification representing share results.
            if (shared > 0) {
                notifications.add(new DropboxNotification(mContext, destination.getHash(), shareUrl, shared, shareUrl));
            }
        }

        return notifications;
    }

    //
    // Public interfaces.
    //

    /**
     * The list of destination ids.
     */
    public interface DestinationId {

        /**
         * The Dropbox app folder.
         */
        public static final int APP_FOLDER = 0;
    }
}