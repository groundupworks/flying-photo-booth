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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.IWingsNotification;
import com.groundupworks.wings.WingsDestination;
import com.groundupworks.wings.core.ShareRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Wings endpoint for Facebook.
 *
 * @author Benedict Lau
 */
public class FacebookEndpoint extends WingsEndpoint {

    /**
     * Facebook endpoint id.
     */
    private static final int ENDPOINT_ID = 0;

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

    /**
     * Request code to use for {@link Activity#startActivityForResult(android.content.Intent, int)}.
     */
    private static final int SETTINGS_REQUEST_CODE = ENDPOINT_ID;

    //
    // Link request state machine.
    //

    private static final int STATE_NONE = -1;

    private static final int STATE_OPEN_SESSION_REQUEST = 0;

    private static final int STATE_PUBLISH_PERMISSIONS_REQUEST = 1;

    private static final int STATE_SETTINGS_REQUEST = 2;

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
     * @param activity       the {@link Activity}.
     * @param fragment       the {@link Fragment}. May be null.
     * @param statusCallback callback when the {@link Session} state changes.
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
     * Finishes a {@link com.groundupworks.wings.facebook.FacebookEndpoint#startOpenSessionRequest(android.app.Activity, android.support.v4.app.Fragment, com.facebook.Session.StatusCallback)}.
     *
     * @param activity    the {@link Activity}.
     * @param requestCode the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  the integer result code returned by the child activity through its setResult().
     * @param data        an Intent, which can return result data to the caller (various data can be attached to Intent
     *                    "extras").
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
     * @param activity the {@link Activity}.
     * @param fragment the {@link Fragment}. May be null.
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
     * Finishes a {@link com.groundupworks.wings.facebook.FacebookEndpoint#startPublishPermissionsRequest(android.app.Activity, android.support.v4.app.Fragment)}.
     *
     * @param activity    the {@link Activity}.
     * @param requestCode the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  the integer result code returned by the child activity through its setResult().
     * @param data        an Intent, which can return result data to the caller (various data can be attached to Intent
     *                    "extras").
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
     * @param activity the {@link Activity}.
     * @param fragment the {@link Fragment}. May be null.
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
     * Finishes a {@link com.groundupworks.wings.facebook.FacebookEndpoint#startSettingsRequest(android.app.Activity, android.support.v4.app.Fragment)}.
     *
     * @param requestCode the integer request code originally supplied to startActivityForResult(), allowing you to identify who
     *                    this result came from.
     * @param resultCode  the integer result code returned by the child activity through its setResult().
     * @param data        an Intent, which can return result data to the caller (various data can be attached to Intent
     *                    "extras").
     * @return the settings; or null if failed.
     */
    private FacebookSettings finishSettingsRequest(int requestCode, int resultCode, Intent data) {
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
     * @return true if installed; false otherwise.
     */
    private boolean isFacebookAppInstalled() {
        boolean isInstalled = false;
        try {
            // An exception will be thrown if the package is not found.
            mContext.getPackageManager().getApplicationInfo(KATANA_PACKAGE, 0);
            isInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            // Do nothing.
        }
        return isInstalled;
    }

    /**
     * Links an account.
     *
     * @param accountName    the user name associated with the account.
     * @param photoPrivacy   the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName      the name of the album to share to.
     * @param albumGraphPath the graph path of the album to share to.
     * @return true if successful; false otherwise.
     */
    private boolean link(String accountName, String photoPrivacy, String albumName,
                         String albumGraphPath) {
        boolean isSuccessful = false;

        // Validate account params and store.
        if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0
                && albumGraphPath != null && albumGraphPath.length() > 0) {
            storeAccountParams(accountName, photoPrivacy, albumName, albumGraphPath);
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Handles an error case during the linking process.
     */
    private void handleLinkError() {
        // Reset link request state.
        mLinkRequestState = STATE_NONE;

        // Show toast to indicate error during linking.
        if (isFacebookAppInstalled()) {
            showLinkError();
        } else {
            showFacebookAppError();
        }

        // Unlink account to ensure proper reset.
        unlink();
    }

    /**
     * Displays the link error message.
     */
    private void showLinkError() {
        Toast.makeText(mContext, mContext.getString(R.string.facebook__error_link), Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays the Facebook app error message.
     */
    private void showFacebookAppError() {
        Toast.makeText(mContext, mContext.getString(R.string.facebook__error_facebook_app), Toast.LENGTH_SHORT).show();
    }

    /**
     * Stores the account params in persisted storage.
     *
     * @param accountName    the user name associated with the account.
     * @param photoPrivacy   the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName      the name of the album to share to.
     * @param albumGraphPath the graph path of the album to share to.
     */
    private void storeAccountParams(String accountName, String photoPrivacy, String albumName,
                                    String albumGraphPath) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putString(mContext.getString(R.string.facebook__account_name_key), accountName);
        if (photoPrivacy != null && photoPrivacy.length() > 0) {
            editor.putString(mContext.getString(R.string.facebook__photo_privacy_key), photoPrivacy);
        }
        editor.putString(mContext.getString(R.string.facebook__album_name_key), albumName);
        editor.putString(mContext.getString(R.string.facebook__album_graph_path_key), albumGraphPath);

        // Set preference to linked.
        editor.putBoolean(mContext.getString(R.string.facebook__link_key), true);
        editor.apply();
    }

    /**
     * Removes the account params from persisted storage.
     */
    private void removeAccountParams() {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.remove(mContext.getString(R.string.facebook__account_name_key));
        editor.remove(mContext.getString(R.string.facebook__photo_privacy_key));
        editor.remove(mContext.getString(R.string.facebook__album_name_key));
        editor.remove(mContext.getString(R.string.facebook__album_graph_path_key));

        // Set preference to unlinked.
        editor.putBoolean(mContext.getString(R.string.facebook__link_key), false);
        editor.apply();
    }

    /**
     * Gets the privacy level of shared photos.
     *
     * @return the privacy level; or null if unlinked.
     */
    private String optLinkedPhotoPrivacy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.facebook__photo_privacy_key), null);
    }

    /**
     * Gets the graph path of the album to share to.
     *
     * @return the graph path; or null if unlinked.
     */
    private String getLinkedAlbumGraphPath() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.facebook__album_graph_path_key), null);
    }

    /**
     * Parses the photo id from a {@link GraphObject}.
     *
     * @param graphObject the {@link GraphObject} to parse.
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

    /**
     * Gets the name of the album to share to.
     *
     * @return the album name; or null if unlinked.
     */
    private String getLinkedAlbumName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.facebook__album_name_key), null);
    }

    //
    // Package private methods.
    //

    /**
     * Asynchronously requests the user name associated with the linked account. Requires an opened active
     * {@link Session}.
     *
     * @param graphUserCallback a {@link GraphUserCallback} when the request completes.
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
     * @param callback a {@link Callback} when the request completes.
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

    @Override
    public int getEndpointId() {
        return ENDPOINT_ID;
    }

    @Override
    public void startLinkRequest(final Activity activity, final Fragment fragment) {
        // Construct status callback.
        Session.StatusCallback statusCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (mLinkRequestState == STATE_OPEN_SESSION_REQUEST && state.isOpened()) {
                    // Request publish permissions.
                    if (!startPublishPermissionsRequest(activity, fragment)) {
                        handleLinkError();
                    }
                }
            }
        };

        // Open session.
        startOpenSessionRequest(activity, fragment, statusCallback);
    }

    @Override
    public void unlink() {
        // Unlink in persisted storage.
        removeAccountParams();

        // Unlink any current session.
        Session session = Session.getActiveSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }

        // Remove existing share requests in a background thread.
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mDatabase.deleteShareRequests(new WingsDestination(DestinationId.PROFILE, ENDPOINT_ID));
            }
        });
    }

    @Override
    public boolean isLinked() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getBoolean(mContext.getString(R.string.facebook__link_key), false);
    }

    @Override
    public void onResumeImpl() {
        // Do nothing.
    }

    @Override
    public void onActivityResultImpl(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data) {
        // State machine to handle the linking process.
        switch (mLinkRequestState) {
            case STATE_OPEN_SESSION_REQUEST: {
                // Only handle the error case. If successful, the publish permissions request will be started by a
                // session callback.
                if (!finishOpenSessionRequest(activity, requestCode, resultCode, data)) {
                    handleLinkError();
                }
                break;
            }
            case STATE_PUBLISH_PERMISSIONS_REQUEST: {
                if (finishPublishPermissionsRequest(activity, requestCode, resultCode, data)) {
                    // Start request for settings.
                    if (!startSettingsRequest(activity, fragment)) {
                        handleLinkError();
                    }
                } else {
                    handleLinkError();
                }
                break;
            }
            case STATE_SETTINGS_REQUEST: {
                FacebookSettings settings = finishSettingsRequest(requestCode, resultCode, data);
                if (settings != null) {
                    // Link account.
                    if (link(settings.getAccountName(), settings.optPhotoPrivacy(), settings.getAlbumName(), settings.getAlbumGraphPath())) {
                        // End link request, but persist link tokens.
                        Session session = Session.getActiveSession();
                        if (session != null && !session.isClosed()) {
                            session.close();
                        }
                        mLinkRequestState = STATE_NONE;
                    } else {
                        handleLinkError();
                    }
                } else {
                    handleLinkError();
                }
                break;
            }
            default: {
            }
        }
    }

    @Override
    public String getLinkedAccountName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getString(mContext.getString(R.string.facebook__account_name_key), null);
    }

    @Override
    public String getDestinationDescription(int destinationId) {
        String destinationDescription = null;
        String accountName = getLinkedAccountName();
        String albumName = getLinkedAlbumName();
        if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0) {
            destinationDescription = mContext.getString(R.string.facebook__destination_description, accountName, albumName);
        }
        return destinationDescription;
    }

    @Override
    public Set<IWingsNotification> processShareRequests() {
        Set<IWingsNotification> notifications = new HashSet<IWingsNotification>();

        // Get params associated with the linked account.
        String photoPrivacy = optLinkedPhotoPrivacy();
        String albumName = getLinkedAlbumName();
        String albumGraphPath = getLinkedAlbumGraphPath();
        if (albumName != null && albumGraphPath != null) {
            // Get share requests for Facebook.
            WingsDestination destination = new WingsDestination(DestinationId.PROFILE, ENDPOINT_ID);
            List<ShareRequest> shareRequests = mDatabase.checkoutShareRequests(destination);
            int shared = 0;
            String intentUri = null;

            if (!shareRequests.isEmpty()) {
                // Try open session with cached access token.
                Session session = Session.openActiveSessionFromCache(mContext);
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
                                        mDatabase.markSuccessful(shareRequest.getId());

                                        // Parse photo id to construct notification intent uri.
                                        if (intentUri == null) {
                                            String photoId = parsePhotoId(response.getGraphObject());
                                            if (photoId != null && photoId.length() > 0) {
                                                intentUri = SHARE_NOTIFICATION_INTENT_BASE_URI + photoId;
                                            }
                                        }

                                        shared++;
                                    } else {
                                        mDatabase.markFailed(shareRequest.getId());

                                        Category category = error.getCategory();
                                        if (Category.AUTHENTICATION_RETRY.equals(category)
                                                || Category.PERMISSION.equals(category)) {
                                            // Update account linking state to unlinked.
                                            unlink();
                                        }
                                    }
                                } else {
                                    mDatabase.markFailed(shareRequest.getId());
                                }
                            } else {
                                mDatabase.markFailed(shareRequest.getId());
                            }
                        } catch (FacebookException e) {
                            mDatabase.markFailed(shareRequest.getId());
                        } catch (IllegalArgumentException e) {
                            mDatabase.markFailed(shareRequest.getId());
                        } catch (FileNotFoundException e) {
                            mDatabase.markFailed(shareRequest.getId());
                        } catch (Exception e) {
                            // Safety.
                            mDatabase.markFailed(shareRequest.getId());
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
                        mDatabase.markFailed(shareRequest.getId());
                    }
                }
            }

            // Construct and add notification representing share results.
            if (shared > 0) {
                notifications.add(new FacebookNotification(mContext, destination.getHash(), albumName, shared, intentUri));
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
         * The personal Facebook profile.
         */
        public static final int PROFILE = 0;
    }
}
