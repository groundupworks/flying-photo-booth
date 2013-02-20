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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookRequestError.Category;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.groundupworks.flyingphotobooth.R;

/**
 * A helper class for linking and sharing to Facebook.
 * 
 * @author Benedict Lau
 */
public class FacebookHelper {

    //
    // Facebook permissions.
    //

    /**
     * Permission to basic info.
     */
    private static final String PERMISSION_BASIC_INFO = "basic_info";

    /**
     * Permission to user photos.
     */
    private static final String PERMISSION_USER_PHOTOS = "user_photos";

    /**
     * Permission to publish content.
     */
    private static final String PERMISSION_PUBLISH_ACTIONS = "publish_actions";

    //
    // Link request state machine.
    //

    private static final int STATE_NONE = -1;

    private static final int STATE_OPEN_SESSION_REQUEST = 0;

    private static final int STATE_PUBLISH_PERMISSIONS_REQUEST = 1;

    private static final int STATE_SETTINGS_REQUEST = 2;

    private static final int SETTINGS_REQUEST_CODE = 369;

    //
    // Albums listing params.
    //

    /**
     * The graph path to list photo albums.
     */
    private static final String ALBUMS_LISTING_GRAPH_PATH = "me/albums";

    /**
     * The key for params to request.
     */
    private static final String ALBUMS_LISTING_FEILDS_KEY = "fields";

    /**
     * The value for params to request.
     */
    private static final String ALBUMS_LISTING_FIELDS_VALUE = "id,name,type,privacy,can_upload";

    //
    // Album listing results.
    //

    static final String ALBUMS_LISTING_RESULT_DATA_KEY = "data";

    static final String ALBUMS_LISTING_FIELD_ID = "id";

    static final String ALBUMS_LISTING_FIELD_NAME = "name";

    static final String ALBUMS_LISTING_FIELD_TYPE = "type";

    static final String ALBUMS_LISTING_FIELD_PRIVACY = "privacy";

    static final String ALBUMS_LISTING_FIELD_CAN_UPLOAD = "can_upload";

    /**
     * The 'custom' album privacy level.
     */
    static final String ALBUM_PRIVACY_CUSTOM = "custom";

    /**
     * The {@link String} to append to album id to create graph path.
     */
    static final String ALBUM_ID_TO_GRAPH_PATH = "/photos";

    //
    // Default album params.
    //

    /**
     * The id of the default album to share to.
     */
    static final String DEFAULT_ALBUM_NAME = "Flying PhotoBooth Photos";

    /**
     * The graph path of the default album to share to.
     */
    static final String DEFAULT_ALBUM_GRAPH_PATH = "me/photos";

    /**
     * The privacy level of the default album to share to.
     */
    static final String DEFAULT_ALBUM_PRIVACY = ALBUM_PRIVACY_CUSTOM;

    /**
     * The type of the default album to share to.
     */
    static final String DEFAULT_ALBUM_TYPE = "app";

    //
    // Privacy levels for albums with 'custom' privacy level.
    //

    /**
     * Shared photos are visible to 'Only Me'.
     */
    static final String PHOTO_PRIVACY_SELF = "{'value':'SELF'}";

    /**
     * Shared photos are visible to 'Friends'.
     */
    static final String PHOTO_PRIVACY_FRIENDS = "{'value':'ALL_FRIENDS'}";

    /**
     * Shared photos are visible to 'Friends of Friends'.
     */
    static final String PHOTO_PRIVACY_FRIENDS_OF_FRIENDS = "{'value':'FRIENDS_OF_FRIENDS'}";

    /**
     * Shared photos are visible to 'Public'.
     */
    static final String PHOTO_PRIVACY_EVERYONE = "{'value':'EVERYONE'}";

    //
    // Share params.
    //

    private static final String SHARE_KEY_PICTURE = "picture";

    private static final String SHARE_KEY_PHOTO_PRIVACY = "privacy";

    /**
     * Flag to track if a link request is started.
     */
    private volatile int mLinkRequestState = STATE_NONE;

    //
    // Private methods.
    //

    /**
     * Opens a new session with read permissions.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param statusCallback
     *            callback when the {@link Session} state changes.
     */
    private void startOpenSessionRequest(Activity activity, Session.StatusCallback statusCallback) {
        // State transition.
        mLinkRequestState = STATE_OPEN_SESSION_REQUEST;

        // Construct new session.
        Session session = new Session(activity);
        Session.setActiveSession(session);

        // Construct read permissions to request for.
        List<String> readPermissions = new LinkedList<String>();
        readPermissions.add(PERMISSION_BASIC_INFO);
        readPermissions.add(PERMISSION_USER_PHOTOS);

        // Construct open request.
        OpenRequest openRequest = new OpenRequest(activity);
        openRequest.setPermissions(readPermissions);
        openRequest.setCallback(statusCallback);

        // Execute open request.
        session.openForRead(openRequest);
    }

    /**
     * Finishes a {@link #startOpenSessionRequest(Activity, com.facebook.Session.StatusCallback)}.
     * 
     * 
     * @param activity
     *            the {@link Activity}.
     * @param requestCode
     *            the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *            this result came from.
     * @param resultCode
     *            the integer result code returned by the child activity through its setResult().
     * @param data
     *            an Intent, which can return result data to the caller (various data can be attached to Intent
     *            "extras").
     * @return true if open session request is successful; false otherwise.
     */
    private boolean finishOpenSessionRequest(Activity activity, int requestCode, int resultCode, Intent data) {
        boolean isSuccessful = false;

        Session session = Session.getActiveSession();

        // isOpened() must be called after onActivityResult().
        if (session != null && session.onActivityResult(activity, requestCode, resultCode, data) && session.isOpened()
                && session.getPermissions().contains(PERMISSION_USER_PHOTOS)) {
            isSuccessful = true;
        }

        return isSuccessful;
    }

    /**
     * Requests for permissions to publish publicly. Requires an opened active {@link Session}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    private boolean startPublishPermissionsRequest(Activity activity) {
        boolean isSuccessful = false;

        // State transition.
        mLinkRequestState = STATE_PUBLISH_PERMISSIONS_REQUEST;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Construct publish permissions.
            List<String> publishPermissions = new LinkedList<String>();
            publishPermissions.add(PERMISSION_PUBLISH_ACTIONS);

            // Construct permissions request to publish publicly.
            NewPermissionsRequest permissionsRequest = new NewPermissionsRequest(activity, publishPermissions);
            permissionsRequest.setDefaultAudience(SessionDefaultAudience.EVERYONE);

            // Execute publish permissions request.
            session.requestNewPublishPermissions(permissionsRequest);
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Finishes a {@link #startPublishPermissionsRequest(Activity)}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param requestCode
     *            the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *            this result came from.
     * @param resultCode
     *            the integer result code returned by the child activity through its setResult().
     * @param data
     *            an Intent, which can return result data to the caller (various data can be attached to Intent
     *            "extras").
     * @return true if publish permissions request is successful; false otherwise.
     */
    private boolean finishPublishPermissionsRequest(Activity activity, int requestCode, int resultCode, Intent data) {
        boolean isSuccessful = false;

        Session session = Session.getActiveSession();

        // isOpened() must be called after onActivityResult().
        if (session != null && session.onActivityResult(activity, requestCode, resultCode, data) && session.isOpened()
                && session.getPermissions().contains(PERMISSION_PUBLISH_ACTIONS)) {
            isSuccessful = true;
        }

        return isSuccessful;
    }

    /**
     * Requests for permissions to publish publicly. Requires an opened active {@link Session}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    private boolean startSettingsRequest(Activity activity) {
        boolean isSuccessful = false;

        // State transition.
        mLinkRequestState = STATE_SETTINGS_REQUEST;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Start activity for result.
            Intent intent = new Intent(activity, FacebookSettingsActivity.class);
            activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE);

            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Finishes a {@link #startSettingsRequest(Activity)}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param requestCode
     *            the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *            this result came from.
     * @param resultCode
     *            the integer result code returned by the child activity through its setResult().
     * @param data
     *            an Intent, which can return result data to the caller (various data can be attached to Intent
     *            "extras").
     * @return the settings; or null if failed.
     */
    private FacebookSettings finishSettingsRequest(Activity activity, int requestCode, int resultCode, Intent data) {
        FacebookSettings settings = null;

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Construct settings from the extras bundle.
            settings = FacebookSettings.newInstance(data.getExtras());
        }
        return settings;
    }

    /**
     * Links an account.
     * 
     * @param context
     *            the {@link Context}.
     * @param accountName
     *            the user name associated with the account.
     * @param photoPrivacy
     *            the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName
     *            the name of the album to share to.
     * @param albumGraphPath
     *            the graph path of the album to share to.
     * @return true if successful; false otherwise.
     */
    private boolean link(Context context, String accountName, String photoPrivacy, String albumName,
            String albumGraphPath) {
        boolean isSuccessful = false;

        // Validate account params and store.
        if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0
                && albumGraphPath != null && albumGraphPath.length() > 0) {
            storeAccountParams(context.getApplicationContext(), accountName, photoPrivacy, albumName, albumGraphPath);
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Handles an error case during the linking process.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void handleLinkError(Context context) {
        Context appContext = context.getApplicationContext();

        // Reset link request state.
        mLinkRequestState = STATE_NONE;

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
        Toast.makeText(context, context.getString(R.string.facebook__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Stores the account params in persisted storage.
     * 
     * @param context
     *            the {@link Context}.
     * @param accountName
     *            the user name associated with the account.
     * @param photoPrivacy
     *            the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName
     *            the name of the album to share to.
     * @param albumGraphPath
     *            the graph path of the album to share to.
     */
    private void storeAccountParams(Context context, String accountName, String photoPrivacy, String albumName,
            String albumGraphPath) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putString(appContext.getString(R.string.facebook__account_name_key), accountName);
        if (photoPrivacy != null && photoPrivacy.length() > 0) {
            editor.putString(appContext.getString(R.string.facebook__photo_privacy_key), photoPrivacy);
        }
        editor.putString(appContext.getString(R.string.facebook__album_name_key), albumName);
        editor.putString(appContext.getString(R.string.facebook__album_graph_path_key), albumGraphPath);

        // Set preference to linked.
        editor.putBoolean(appContext.getString(R.string.pref__facebook_link_key), true);
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
        editor.remove(appContext.getString(R.string.facebook__account_name_key));
        editor.remove(appContext.getString(R.string.facebook__photo_privacy_key));
        editor.remove(appContext.getString(R.string.facebook__album_name_key));
        editor.remove(appContext.getString(R.string.facebook__album_graph_path_key));

        // Set preference to unlinked.
        editor.putBoolean(appContext.getString(R.string.pref__facebook_link_key), false);
        editor.apply();
    }

    /**
     * Gets the privacy level of shared photos.
     * 
     * @param context
     *            the {@link Context}.
     * @return the privacy level; or null if unlinked.
     */
    private String optLinkedPhotoPrivacy(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.facebook__photo_privacy_key), null);
    }

    /**
     * Gets the graph path of the album to share to.
     * 
     * @param context
     *            the {@link Context}.
     * @return the graph path; or null if unlinked.
     */
    private String getLinkedAlbumGraphPath(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.facebook__album_graph_path_key), null);
    }

    //
    // Package private methods.
    //

    /**
     * Asynchronously requests the user name associated with the linked account. Requires an opened active
     * {@link Session}.
     * 
     * @param graphUserCallback
     *            a {@link GraphUserCallback} when the request completes.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    boolean requestAccountName(GraphUserCallback graphUserCallback) {
        boolean isSuccessful = false;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            Request.executeMeRequestAsync(session, graphUserCallback);
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Asynchronously requests the albums associated with the linked account. Requires an opened active {@link Session}.
     * 
     * @param callback
     *            a {@link Callback} when the request completes.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    boolean requestAlbums(Callback callback) {
        boolean isSuccessful = false;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Construct fields to request.
            Bundle params = new Bundle();
            params.putString(ALBUMS_LISTING_FEILDS_KEY, ALBUMS_LISTING_FIELDS_VALUE);

            // Construct and execute albums listing request.
            Request request = new Request(session, ALBUMS_LISTING_GRAPH_PATH, params, HttpMethod.GET, callback);
            request.executeAsync();

            isSuccessful = true;
        }
        return isSuccessful;
    }

    //
    // Public methods.
    //

    /**
     * Starts a link request. The user will be authenticated through the native Facebook app or the default web browser.
     * 
     * @param activity
     *            the {@link Activity}.
     */
    public void startLinkRequest(final Activity activity) {
        // Construct status callback.
        Session.StatusCallback statusCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (mLinkRequestState == STATE_OPEN_SESSION_REQUEST && state.isOpened()) {
                    // Request publish permissions.
                    if (!startPublishPermissionsRequest(activity)) {
                        handleLinkError(activity);
                    }
                }
            }
        };

        // Open session.
        startOpenSessionRequest(activity, statusCallback);
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
        Session session = Session.getActiveSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    /**
     * Checks if the user is linked to Facebook.
     * 
     * @param context
     *            the {@link Context}.
     */
    public boolean isLinked(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(appContext.getString(R.string.pref__facebook_link_key), false);
    }

    /**
     * A convenience method to be called in the onActivityResult() of any {@link Activity} or {@link Fragment} that uses
     * {@link #startLinkRequest(Context)}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param requestCode
     *            the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *            this result came from.
     * @param resultCode
     *            the integer result code returned by the child activity through its setResult().
     * @param data
     *            an Intent, which can return result data to the caller (various data can be attached to Intent
     *            "extras").
     */
    public void onActivityResultImpl(Activity activity, int requestCode, int resultCode, Intent data) {
        // State machine to handle the linking process.
        switch (mLinkRequestState) {
            case STATE_OPEN_SESSION_REQUEST: {
                // Only handle the error case. If successful, the publish permissions request will be started by a
                // session callback.
                if (!finishOpenSessionRequest(activity, requestCode, resultCode, data)) {
                    handleLinkError(activity);
                }
                break;
            }
            case STATE_PUBLISH_PERMISSIONS_REQUEST: {
                if (finishPublishPermissionsRequest(activity, requestCode, resultCode, data)) {
                    // Start request for settings.
                    if (!startSettingsRequest(activity)) {
                        handleLinkError(activity);
                    }
                } else {
                    handleLinkError(activity);
                }
                break;
            }
            case STATE_SETTINGS_REQUEST: {
                FacebookSettings settings = finishSettingsRequest(activity, requestCode, resultCode, data);
                if (settings != null) {
                    // Link account.
                    if (link(activity, settings.getAccountName(), settings.optPhotoPrivacy(), settings.getAlbumName(),
                            settings.getAlbumGraphPath())) {
                        // End link request, but persist link tokens.
                        Session session = Session.getActiveSession();
                        if (session != null && !session.isClosed()) {
                            session.close();
                        }
                        mLinkRequestState = STATE_NONE;
                    } else {
                        handleLinkError(activity);
                    }
                } else {
                    handleLinkError(activity);
                }
                break;
            }
            default: {
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
        return preferences.getString(appContext.getString(R.string.facebook__account_name_key), null);
    }

    /**
     * Gets the name of the album to share to.
     * 
     * @param context
     *            the {@link Context}.
     * @return the album name; or null if unlinked.
     */
    public String getLinkedAlbumName(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(appContext.getString(R.string.facebook__album_name_key), null);
    }

    /**
     * Shares an image to the linked account. This should be called in a background thread.
     * 
     * @param context
     *            the {@link Context}.
     * @param file
     *            the {@link File} to share.
     */
    public void share(Context context, final File file) {
        // Get params associated with the linked account.
        String photoPrivacy = optLinkedPhotoPrivacy(context);
        String albumGraphPath = getLinkedAlbumGraphPath(context);
        if (photoPrivacy != null && albumGraphPath != null) {
            // Try open session with cached access token.
            Session session = Session.openActiveSessionFromCache(context);
            if (session != null && session.isOpened()) {
                // Upload file.
                try {
                    // Construct graph params.
                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file,
                            ParcelFileDescriptor.MODE_READ_ONLY);
                    Bundle params = new Bundle();
                    params.putParcelable(SHARE_KEY_PICTURE, fileDescriptor);
                    if (photoPrivacy != null && photoPrivacy.length() > 0) {
                        params.putString(SHARE_KEY_PHOTO_PRIVACY, photoPrivacy);
                    }

                    // Execute upload request synchronously.
                    Request request = new Request(session, albumGraphPath, params, HttpMethod.POST, null);
                    Response response = request.executeAndWait();

                    // Process response.
                    if (response != null) {
                        FacebookRequestError error = response.getError();
                        if (error == null) {

                            // TODO Mark file as sent in db.

                        } else {
                            Category category = error.getCategory();
                            if (Category.AUTHENTICATION_RETRY.equals(category) || Category.PERMISSION.equals(category)) {
                                // Update account linking state to unlinked.
                                unlink(context);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    // Do nothing.
                }
            }
        }
    }
}
