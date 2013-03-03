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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookRequestError.Category;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.groundupworks.flyingphotobooth.MyApplication;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.wings.IWingsNotification;
import com.groundupworks.flyingphotobooth.wings.ShareRequest;
import com.groundupworks.flyingphotobooth.wings.WingsDbHelper;

/**
 * A helper class for linking and sharing to Facebook.
 * 
 * @author Benedict Lau
 */
public class FacebookHelper {

    /**
     * Timeout value for http requests.
     */
    private static final int HTTP_REQUEST_TIMEOUT = 120000;

    /**
     * Facebook app package name.
     */
    private static final String KATANA_PACKAGE = "com.facebook.katana";

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
     * The key for max number of albums to request.
     */
    private static final String ALBUMS_LISTING_LIMIT_KEY = "limit";

    /**
     * The value for max number of albums to request.
     */
    private static final String ALBUMS_LISTING_LIMIT_VALUE = "200";

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
     * The {@link String} to append to album id to create graph path.
     */
    static final String ALBUM_ID_TO_GRAPH_PATH = "/photos";

    //
    // Default album params.
    //

    /**
     * The default name of the app album. Note that the current app album name may have been changed by the user.
     */
    static final String APP_ALBUM_DEFAULT_NAME = "Flying PhotoBooth Photos";

    /**
     * The graph path of the app album.
     */
    static final String APP_ALBUM_GRAPH_PATH = "me/photos";

    /**
     * The configurable privacy level of the app album.
     */
    static final String APP_ALBUM_PRIVACY = "select privacy level";

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

    private static final String SHARE_KEY_PHOTO_ID = "id";

    private static final String SHARE_NOTIFICATION_INTENT_BASE_URI = "fb://photo/";

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
     * @param fragment
     *            the {@link Fragment}. May be null.
     * @param statusCallback
     *            callback when the {@link Session} state changes.
     */
    private void startOpenSessionRequest(Activity activity, Fragment fragment, Session.StatusCallback statusCallback) {
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
        OpenRequest openRequest;
        if (fragment == null) {
            openRequest = new OpenRequest(activity);
        } else {
            openRequest = new OpenRequest(fragment);
        }

        // Allow SSO login only because the web login does not allow PERMISSION_USER_PHOTOS to be bundled with the
        // first openForRead() call.
        openRequest.setLoginBehavior(SessionLoginBehavior.SSO_ONLY);
        openRequest.setPermissions(readPermissions);
        openRequest.setDefaultAudience(SessionDefaultAudience.EVERYONE);
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
    private boolean finishOpenSessionRequest(final Activity activity, int requestCode, int resultCode, Intent data) {
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
     * @param fragment
     *            the {@link Fragment}. May be null.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    private boolean startPublishPermissionsRequest(Activity activity, Fragment fragment) {
        boolean isSuccessful = false;

        // State transition.
        mLinkRequestState = STATE_PUBLISH_PERMISSIONS_REQUEST;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Construct publish permissions.
            List<String> publishPermissions = new LinkedList<String>();
            publishPermissions.add(PERMISSION_PUBLISH_ACTIONS);

            // Construct permissions request to publish publicly.
            NewPermissionsRequest permissionsRequest;
            if (fragment == null) {
                permissionsRequest = new NewPermissionsRequest(activity, publishPermissions);
            } else {
                permissionsRequest = new NewPermissionsRequest(fragment, publishPermissions);
            }
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
     * @param fragment
     *            the {@link Fragment}. May be null.
     * @return true if the request is made; false if no opened {@link Session} is active.
     */
    private boolean startSettingsRequest(Activity activity, Fragment fragment) {
        boolean isSuccessful = false;

        // State transition.
        mLinkRequestState = STATE_SETTINGS_REQUEST;

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            // Start activity for result.
            Intent intent = new Intent(activity, FacebookSettingsActivity.class);
            if (fragment == null) {
                activity.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            } else {
                fragment.startActivityForResult(intent, SETTINGS_REQUEST_CODE);
            }

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
     * Checks if the Facebook native app is installed on the device.
     * 
     * @param context
     *            the {@link Context}.
     * @return true if installed; false otherwise.
     */
    private boolean isFacebookAppInstalled(Context context) {
        boolean isInstalled = false;
        try {
            // An exception will be thrown if the package is not found.
            context.getPackageManager().getApplicationInfo(KATANA_PACKAGE, 0);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            // Do nothing.
        }
        return isInstalled;
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
        if (isFacebookAppInstalled(context)) {
            showLinkError(appContext);
        } else {
            showFacebookAppError(appContext);
        }

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
     * Displays the Facebook app error message.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void showFacebookAppError(Context context) {
        Toast.makeText(context, context.getString(R.string.facebook__error_facebook_app), Toast.LENGTH_SHORT).show();
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

    /**
     * Parses the photo id from a {@link GraphObject}.
     * 
     * @param graphObject
     *            the {@link GraphObject} to parse.
     * 
     * @return the photo id; or null if not found.
     */
    private String parsePhotoId(GraphObject graphObject) {
        String photoId = null;

        if (graphObject != null) {
            JSONObject jsonObject = graphObject.getInnerJSONObject();
            if (jsonObject != null) {
                photoId = jsonObject.optString(SHARE_KEY_PHOTO_ID, null);
            }
        }
        return photoId;
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
            params.putString(ALBUMS_LISTING_LIMIT_KEY, ALBUMS_LISTING_LIMIT_VALUE);
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
     * @param fragment
     *            the {@link Fragment}. May be null.
     */
    public void startLinkRequest(final Activity activity, final Fragment fragment) {
        // Construct status callback.
        Session.StatusCallback statusCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (mLinkRequestState == STATE_OPEN_SESSION_REQUEST && state.isOpened()) {
                    // Request publish permissions.
                    if (!startPublishPermissionsRequest(activity, fragment)) {
                        handleLinkError(activity);
                    }
                }
            }
        };

        // Open session.
        startOpenSessionRequest(activity, fragment, statusCallback);
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

        // Remove existing share requests in a background thread.
        final Context appContext = context.getApplicationContext();
        Handler workerHandler = new Handler(MyApplication.getWorkerLooper());
        workerHandler.post(new Runnable() {

            @Override
            public void run() {
                WingsDbHelper.getInstance(appContext).deleteShareRequests(ShareRequest.DESTINATION_FACEBOOK);
            }
        });
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
     * Checks if the user has auto share turned on for Facebook.
     * 
     * @param context
     *            the {@link Context}.
     */
    public boolean isAutoShare(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(appContext.getString(R.string.pref__facebook_link_key), false)
                && preferences.getBoolean(appContext.getString(R.string.pref__facebook_auto_share_key), false);
    }

    /**
     * A convenience method to be called in the onActivityResult() of any {@link Activity} or {@link Fragment} that uses
     * {@link #startLinkRequest(Context)}.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param fragment
     *            the {@link Fragment}. May be null.
     * @param requestCode
     *            the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *            this result came from.
     * @param resultCode
     *            the integer result code returned by the child activity through its setResult().
     * @param data
     *            an Intent, which can return result data to the caller (various data can be attached to Intent
     *            "extras").
     */
    public void onActivityResultImpl(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data) {
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
                    if (!startSettingsRequest(activity, fragment)) {
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
     * Process share requests by sharing to the linked account. This should be called in a background thread.
     * 
     * @param context
     *            the {@link Context}.
     * @return a {@link IWingsNotification} representing the results of the processed {@link ShareRequest}. May be null.
     */
    public IWingsNotification processShareRequests(Context context) {
        int shared = 0;
        String intentUri = null;

        // Get params associated with the linked account.
        String photoPrivacy = optLinkedPhotoPrivacy(context);
        String albumName = getLinkedAlbumName(context);
        String albumGraphPath = getLinkedAlbumGraphPath(context);
        if (albumName != null && albumGraphPath != null) {
            // Get share requests for Facebook.
            WingsDbHelper wingsDbHelper = WingsDbHelper.getInstance(context);
            List<ShareRequest> shareRequests = wingsDbHelper.checkoutShareRequests(ShareRequest.DESTINATION_FACEBOOK);

            if (!shareRequests.isEmpty()) {
                // Try open session with cached access token.
                Session session = Session.openActiveSessionFromCache(context);
                if (session != null && session.isOpened()) {
                    // Process share requests.
                    for (ShareRequest shareRequest : shareRequests) {
                        File file = new File(shareRequest.getFilePath());
                        ParcelFileDescriptor fileDescriptor = null;
                        try {
                            // Construct graph params.
                            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                            Bundle params = new Bundle();
                            params.putParcelable(SHARE_KEY_PICTURE, fileDescriptor);
                            if (photoPrivacy != null && photoPrivacy.length() > 0) {
                                params.putString(SHARE_KEY_PHOTO_PRIVACY, photoPrivacy);
                            }

                            // Execute upload request synchronously. Need to use RequestBatch to set connection timeout.
                            Request request = new Request(session, albumGraphPath, params, HttpMethod.POST, null);
                            RequestBatch requestBatch = new RequestBatch(request);
                            requestBatch.setTimeout(HTTP_REQUEST_TIMEOUT);
                            List<Response> responses = requestBatch.executeAndWait();
                            if (responses != null && !responses.isEmpty()) {
                                // Process response.
                                Response response = responses.get(0);
                                if (response != null) {
                                    FacebookRequestError error = response.getError();
                                    if (error == null) {
                                        // Mark as successfully processed.
                                        wingsDbHelper.markSuccessful(shareRequest.getId());

                                        // Parse photo id to construct notification intent uri.
                                        if (intentUri == null) {
                                            String photoId = parsePhotoId(response.getGraphObject());
                                            if (photoId != null && photoId.length() > 0) {
                                                intentUri = SHARE_NOTIFICATION_INTENT_BASE_URI + photoId;
                                            }
                                        }

                                        shared++;
                                    } else {
                                        wingsDbHelper.markFailed(shareRequest.getId());

                                        Category category = error.getCategory();
                                        if (Category.AUTHENTICATION_RETRY.equals(category)
                                                || Category.PERMISSION.equals(category)) {
                                            // Update account linking state to unlinked.
                                            unlink(context);
                                        }
                                    }
                                } else {
                                    wingsDbHelper.markFailed(shareRequest.getId());
                                }
                            } else {
                                wingsDbHelper.markFailed(shareRequest.getId());
                            }
                        } catch (FacebookException e) {
                            wingsDbHelper.markFailed(shareRequest.getId());
                        } catch (IllegalArgumentException e) {
                            wingsDbHelper.markFailed(shareRequest.getId());
                        } catch (FileNotFoundException e) {
                            wingsDbHelper.markFailed(shareRequest.getId());
                        } catch (Exception e) {
                            // Safety.
                            wingsDbHelper.markFailed(shareRequest.getId());
                        } finally {
                            if (fileDescriptor != null) {
                                try {
                                    fileDescriptor.close();
                                } catch (IOException e) {
                                    // Do nothing.
                                }
                            }
                        }
                    }
                } else {
                    // Mark all share requests as failed to process since we failed to open an active session.
                    for (ShareRequest shareRequest : shareRequests) {
                        wingsDbHelper.markFailed(shareRequest.getId());
                    }
                }
            }
        }

        // Construct notification representing share results.
        FacebookNotification notification = null;
        if (shared > 0) {
            notification = new FacebookNotification(context, albumName, shared, intentUri);
        }

        return notification;
    }
}
