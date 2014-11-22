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

import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Fragment} for Facebook photo album selection.
 *
 * @author Benedict Lau
 */
public class FacebookAlbumListFragment extends ListFragment {

    //
    // Album cursor columns.
    //

    private static final String CURSOR_ID = "_id";

    private static final String CURSOR_ALBUM_NAME = "name";

    private static final String CURSOR_ALBUM_GRAPH_PATH = "graphPath";

    private static final String CURSOR_ALBUM_PRIVACY = "privacy";

    private static final int CURSOR_ID_INDEX = 0;

    private static final int CURSOR_ALBUM_NAME_INDEX = 1;

    private static final int CURSOR_ALBUM_GRAPH_PATH_INDEX = 2;

    private static final int CURSOR_ALBUM_PRIVACY_INDEX = 3;

    /**
     * The cursor id of the app album to share to.
     */
    private static final long APP_ALBUM_CURSOR_ID = 0L;

    /**
     * A {@link FacebookEndpoint} instance.
     */
    private FacebookEndpoint mFacebookEndpoint = new FacebookEndpoint();

    /**
     * Cursor to back the albums list.
     */
    private MatrixCursor mAlbumCursor = new MatrixCursor(new String[]{CURSOR_ID, CURSOR_ALBUM_NAME,
            CURSOR_ALBUM_GRAPH_PATH, CURSOR_ALBUM_PRIVACY});

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make async requests for account information.
        requestAlbums();
        requestAccountName();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FacebookSettingsActivity activity = (FacebookSettingsActivity) getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        // Handle album selection.
        if (mAlbumCursor.moveToFirst()) {
            do {
                long cursorId = mAlbumCursor.getLong(CURSOR_ID_INDEX);
                if (cursorId == id) {
                    String albumPrivacy = mAlbumCursor.getString(CURSOR_ALBUM_PRIVACY_INDEX);
                    String albumName = mAlbumCursor.getString(CURSOR_ALBUM_NAME_INDEX);
                    String albumGraphPath = mAlbumCursor.getString(CURSOR_ALBUM_GRAPH_PATH_INDEX);
                    if (FacebookEndpoint.APP_ALBUM_PRIVACY.equals(albumPrivacy)) {
                        // Request for photo privacy.
                        activity.showDialogFragment(FacebookPrivacyDialogFragment
                                .newInstance(albumName, albumGraphPath));
                    } else {
                        // Finish Activity without photo privacy.
                        activity.mAlbumName = albumName;
                        activity.mAlbumGraphPath = albumGraphPath;
                        activity.tryFinish();
                    }
                    break;
                }
            } while (mAlbumCursor.moveToNext());
        }
    }

    //
    // Private methods.
    //

    /**
     * Asynchronously requests the user name associated with the linked account. Tries to finish the
     * {@link FacebookSettingsActivity} when completed.
     */
    private void requestAccountName() {
        GraphUserCallback callback = new GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                FacebookSettingsActivity activity = (FacebookSettingsActivity) getActivity();
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                if (response != null && response.getError() == null && user != null) {
                    String accountName = user.getFirstName() + " " + user.getLastName();
                    if (accountName != null && accountName.length() > 0) {
                        activity.mAccountName = accountName;
                    } else {
                        activity.mHasErrorOccurred = true;
                    }
                } else {
                    activity.mHasErrorOccurred = true;
                }

                activity.tryFinish();
            }
        };

        mFacebookEndpoint.requestAccountName(callback);
    }

    /**
     * Asynchronously requests the albums associated with the linked account. Sets the {@link ListAdapter} when
     * completed.
     */
    private void requestAlbums() {
        Callback callback = new Callback() {
            @Override
            public void onCompleted(Response response) {
                FacebookSettingsActivity activity = (FacebookSettingsActivity) getActivity();
                if (activity == null || activity.isFinishing()) {
                    return;
                }

                if (response != null && response.getError() == null) {
                    Object[] appAlbum = null;
                    List<Object[]> albums = new ArrayList<Object[]>();

                    GraphObject graphObject = response.getGraphObject();
                    if (graphObject != null) {
                        JSONObject jsonObject = graphObject.getInnerJSONObject();
                        try {
                            JSONArray jsonArray = jsonObject
                                    .getJSONArray(FacebookEndpoint.ALBUMS_LISTING_RESULT_DATA_KEY);
                            long cursorId = 1L;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                try {
                                    // Get data from json.
                                    JSONObject album = jsonArray.getJSONObject(i);
                                    String id = album.getString(FacebookEndpoint.ALBUMS_LISTING_FIELD_ID);
                                    String name = album.getString(FacebookEndpoint.ALBUMS_LISTING_FIELD_NAME);
                                    String type = album.getString(FacebookEndpoint.ALBUMS_LISTING_FIELD_TYPE);
                                    String privacy = album.getString(FacebookEndpoint.ALBUMS_LISTING_FIELD_PRIVACY);
                                    boolean canUpload = album
                                            .getBoolean(FacebookEndpoint.ALBUMS_LISTING_FIELD_CAN_UPLOAD);

                                    // Filter out albums that do not allow upload.
                                    if (canUpload && id != null && id.length() > 0 && name != null && name.length() > 0
                                            && type != null && type.length() > 0 && privacy != null
                                            && privacy.length() > 0) {
                                        String graphPath = id + FacebookEndpoint.ALBUM_ID_TO_GRAPH_PATH;
                                        if (FacebookEndpoint.DEFAULT_ALBUM_TYPE.equals(type)) {
                                            appAlbum = new Object[]{APP_ALBUM_CURSOR_ID, name, graphPath,
                                                    FacebookEndpoint.APP_ALBUM_PRIVACY};
                                        } else {
                                            albums.add(new Object[]{cursorId, name, graphPath, privacy});
                                            cursorId++;
                                        }
                                    }
                                } catch (JSONException e) {
                                    // Do nothing.
                                }
                            }
                        } catch (JSONException e) {
                            // Do nothing.
                        }
                    }

                    // If not already present, construct row to represent the default app album that will be auto
                    // created.
                    if (appAlbum == null) {
                        appAlbum = new Object[]{APP_ALBUM_CURSOR_ID,
                                activity.getString(R.string.facebook__app_album_default_name),
                                FacebookEndpoint.APP_ALBUM_GRAPH_PATH, FacebookEndpoint.APP_ALBUM_PRIVACY};
                    }

                    // Construct matrix cursor.
                    mAlbumCursor.addRow(appAlbum);
                    for (Object[] album : albums) {
                        mAlbumCursor.addRow(album);
                    }

                    // Set adapter.
                    setListAdapter(new SimpleCursorAdapter(activity, R.layout.facebook_album_list_view_item,
                            mAlbumCursor, new String[]{CURSOR_ALBUM_NAME, CURSOR_ALBUM_PRIVACY}, new int[]{
                            R.id.album_name, R.id.album_privacy}
                    ));
                } else {
                    // Finish Activity with error.
                    activity.mHasErrorOccurred = true;
                    activity.tryFinish();
                }
            }
        };

        mFacebookEndpoint.requestAlbums(callback);
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link FacebookAlbumListFragment} instance.
     *
     * @return the new {@link FacebookAlbumListFragment} instance.
     */
    public static FacebookAlbumListFragment newInstance() {
        return new FacebookAlbumListFragment();
    }
}
