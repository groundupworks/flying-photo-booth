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
import java.util.LinkedList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.groundupworks.flyingphotobooth.R;

/**
 * A helper class for linking and sharing to Facebook.
 * 
 * @author Benedict Lau
 */
public class FacebookHelper {

    //
    // Link request states.
    //

    public static final int LINK_REQUEST_STATE_NONE = -1;

    public static final int LINK_REQUEST_STATE_OPENING = 0;

    public static final int LINK_REQUEST_STATE_OPENED = 1;

    /**
     * The name of the default album to share to.
     */
    private static final String DEFAULT_ALBUM_NAME = "Wall";

    /**
     * The graph path of the default album to share to.
     */
    private static final String DEFAULT_ALBUM_GRAPH_PATH = "me/photos";

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
     * Flag to track if a link request is started.
     */
    private volatile int mLinkRequestState = LINK_REQUEST_STATE_NONE;

    //
    // Private methods.
    //

    /**
     * Finishes an open session request.
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
        if (session != null && session.onActivityResult(activity, requestCode, resultCode, data) && session.isOpened()
                && session.getPermissions().contains(PERMISSION_USER_PHOTOS)) {
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Finishes a publish permissions request.
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
        if (session != null && session.onActivityResult(activity, requestCode, resultCode, data) && session.isOpened()
                && session.getPermissions().contains(PERMISSION_USER_PHOTOS)
                && session.getPermissions().contains(PERMISSION_PUBLISH_ACTIONS)) {
            isSuccessful = true;
        }
        return isSuccessful;
    }

    /**
     * Links an account in a background thread.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void link(Context context) {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            final Context appContext = context;
            Request.executeMeRequestAsync(session, new GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // Check for error.
                    if (response != null && response.getError() == null && user != null) {
                        // Persist account params.
                        String accountName = user.getFirstName() + " " + user.getLastName();
                        storeAccountParams(appContext, accountName, DEFAULT_ALBUM_NAME, DEFAULT_ALBUM_GRAPH_PATH);

                        // End link request, but persist link tokens.
                        mLinkRequestState = LINK_REQUEST_STATE_NONE;
                        Session session = Session.getActiveSession();
                        if (session != null && !session.isClosed()) {
                            session.close();
                        }
                    } else {
                        showLinkError(appContext);

                        // Unlink account to ensure proper reset.
                        unlink(appContext);
                    }
                }
            });
        }
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
     * @param albumName
     *            the name of the album to share to.
     * @param albumGraphPath
     *            the graph path of the album to share to.
     */
    private void storeAccountParams(Context context, String accountName, String albumName, String albumGraphPath) {
        Context appContext = context.getApplicationContext();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putString(appContext.getString(R.string.facebook__account_name_key), accountName);
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
        editor.remove(appContext.getString(R.string.facebook__album_name_key));
        editor.remove(appContext.getString(R.string.facebook__album_graph_path_key));

        // Set preference to unlinked.
        editor.putBoolean(appContext.getString(R.string.pref__facebook_link_key), false);
        editor.apply();
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
    // Public methods.
    //

    /**
     * Starts a link request. The user will be authenticated through the native Facebook app or the default web browser.
     * 
     * @param activity
     *            the {@link Activity}.
     */
    public void startLinkRequest(final Activity activity) {
        // Transition to opening state.
        mLinkRequestState = LINK_REQUEST_STATE_OPENING;

        // Construct new session.
        Session session = new Session(activity);
        Session.setActiveSession(session);

        // Construct read permissions to request for.
        List<String> readPermissions = new LinkedList<String>();
        readPermissions.add(PERMISSION_BASIC_INFO);
        readPermissions.add(PERMISSION_USER_PHOTOS);

        // Construct status callback.
        Session.StatusCallback statusCallback = new Session.StatusCallback() {

            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (mLinkRequestState == LINK_REQUEST_STATE_OPENING && state.isOpened()) {
                    // Transition to opened state.
                    mLinkRequestState = LINK_REQUEST_STATE_OPENED;

                    // Request publish permissions.
                    List<String> publishPermissions = new LinkedList<String>();
                    publishPermissions.add(PERMISSION_PUBLISH_ACTIONS);
                    session.requestNewPublishPermissions(new NewPermissionsRequest(activity, publishPermissions));
                }
            }
        };

        // Construct and make link request.
        OpenRequest openRequest = new OpenRequest(activity);
        openRequest.setPermissions(readPermissions);
        openRequest.setCallback(statusCallback);
        session.openForRead(openRequest);
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
        mLinkRequestState = LINK_REQUEST_STATE_NONE;
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
        Context appContext = activity.getApplicationContext();
        if (mLinkRequestState == LINK_REQUEST_STATE_OPENING
                && !finishOpenSessionRequest(activity, requestCode, resultCode, data)) {
            showLinkError(appContext);

            // Unlink account to ensure proper reset.
            unlink(appContext);
        } else if (mLinkRequestState == LINK_REQUEST_STATE_OPENED) {
            if (finishPublishPermissionsRequest(activity, requestCode, resultCode, data)) {
                // Link account.
                link(appContext);
            } else {
                showLinkError(appContext);

                // Unlink account to ensure proper reset.
                unlink(appContext);
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
     * Shares an image to the linked account.
     * 
     * @param context
     *            the {@link Context}.
     * @param file
     *            the {@link File} to share.
     */
    public void share(Context context, File file) {
        getLinkedAlbumGraphPath(context);

        // AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        // AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        //
        // // Gets access token associated with the linked account.
        // AccessTokenPair accessToken = getLinkedAccessToken(context);
        // if (accessToken != null) {
        // // Start new session with the persisted access token.
        // session.setAccessTokenPair(accessToken);
        // DropboxAPI<AndroidAuthSession> dropboxApi = new DropboxAPI<AndroidAuthSession>(session);
        //
        // // Upload file.
        // FileInputStream inputStream = null;
        // try {
        // inputStream = new FileInputStream(file);
        // dropboxApi.putFile(file.getName(), inputStream, file.length(), null, null);
        // // TODO Mark file as sent in db.
        // } catch (DropboxUnlinkedException e) {
        // // Do nothing.
        // } catch (DropboxException e) {
        // // Do nothing.
        // } catch (FileNotFoundException e) {
        // // Do nothing.
        // } finally {
        // if (inputStream != null) {
        // try {
        // inputStream.close();
        // } catch (IOException e) {
        // // Do nothing.
        // }
        // }
        // }
        // }

    }

    // Facebook.
    // Session session = Session.getActiveSession();

    // List<String> permissions = new LinkedList<String>();
    // permissions.add("publish_actions");
    // Session.NewPermissionsRequest permissionsRequest = new Session.NewPermissionsRequest(getActivity(),
    // permissions);
    // permissionsRequest.setCallback(new StatusCallback() {
    //
    // @Override
    // public void call(Session session, SessionState state, Exception exception) {
    // Log.d("BEN", "StatusCallback2 session=" + session + " state=" + state);
    // }
    // });
    // Session.getActiveSession().requestNewPublishPermissions(permissionsRequest);

    // if (session != null && session.getPermissions().contains("publish_actions")) {
    // try {
    // Request request = Request.newUploadPhotoRequest(Session.getActiveSession(), new File("file://"
    // + (String) msg.obj), new Request.Callback() {
    //
    // @Override
    // public void onCompleted(Response response) {
    // Log.d("BEN", "Published");
    // // showPublishResult(getString(R.string.photo_post), response.getGraphObject(),
    // // response.getError());
    // }
    // });
    // request.executeAsync();
    // } catch (FileNotFoundException e) {
    // Log.d("BEN", "FileNotFoundException");
    // }
    //
    // } else {
    // // pendingAction = PendingAction.POST_PHOTO;
    // if (session == null) {
    // Log.d("BEN", "No active session");
    // } else {
    // Log.d("BEN", "No publish permission");
    // }
    // }
}
