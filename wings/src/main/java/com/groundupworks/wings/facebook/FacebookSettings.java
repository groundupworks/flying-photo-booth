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

import android.os.Bundle;

/**
 * A model object to contain Facebook settings. An instance of {@link FacebookSettings} cannot be directly constructed,
 * but through one of its static newInstance() methods, which does validation internally to ensure the validity of the
 * constructed instance.
 *
 * @author Benedict Lau
 */
public class FacebookSettings {

    //
    // Bundle keys.
    //

    private static final String BUNDLE_KEY_ACCOUNT_NAME = "accountName";

    private static final String BUNDLE_KEY_PHOTO_PRIVACY = "photoPrivacy";

    private static final String BUNDLE_KEY_ALBUM_NAME = "albumName";

    private static final String BUNDLE_KEY_ALBUM_GRAPH_PATH = "albumGraphPath";

    //
    // Account settings that must be non-null and non-empty.
    //

    private String mAccountName;

    private String mPhotoPrivacy = null;

    private String mAlbumName;

    private String mAlbumGraphPath;

    /**
     * Private constructor.
     *
     * @param accountName    the user name associated with the account.
     * @param photoPrivacy   the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName      the name of the album to share to.
     * @param albumGraphPath the graph path of the album to share to.
     */
    private FacebookSettings(String accountName, String photoPrivacy, String albumName, String albumGraphPath) {
        mAccountName = accountName;
        mPhotoPrivacy = photoPrivacy;
        mAlbumName = albumName;
        mAlbumGraphPath = albumGraphPath;
    }

    //
    // Package private methods.
    //

    /**
     * Creates a new {@link FacebookSettings} instance.
     *
     * @param accountName    the user name associated with the account.
     * @param photoPrivacy   the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     * @param albumName      the name of the album to share to.
     * @param albumGraphPath the graph path of the album to share to.
     * @return a new {@link FacebookSettings} instance; or null if any of the params are invalid.
     */
    static FacebookSettings newInstance(String accountName, String photoPrivacy, String albumName, String albumGraphPath) {
        FacebookSettings settings = null;

        if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0
                && albumGraphPath != null && albumGraphPath.length() > 0) {
            settings = new FacebookSettings(accountName, photoPrivacy, albumName, albumGraphPath);
        }

        return settings;
    }

    /**
     * Creates a new {@link FacebookSettings} instance from a {@link Bundle} created by the {@link #toBundle()} method.
     *
     * @param bundle the {@link Bundle}.
     * @return a new {@link FacebookSettings} instance; or null if the {@link Bundle} is invalid.
     */
    static FacebookSettings newInstance(Bundle bundle) {
        FacebookSettings settings = null;

        String accountName = bundle.getString(BUNDLE_KEY_ACCOUNT_NAME);
        String photoPrivacy = bundle.getString(BUNDLE_KEY_PHOTO_PRIVACY);
        String albumName = bundle.getString(BUNDLE_KEY_ALBUM_NAME);
        String albumGraphPath = bundle.getString(BUNDLE_KEY_ALBUM_GRAPH_PATH);
        if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0
                && albumGraphPath != null && albumGraphPath.length() > 0) {
            settings = new FacebookSettings(accountName, photoPrivacy, albumName, albumGraphPath);
        }

        return settings;
    }

    /**
     * Creates a {@link Bundle} from the {@link FacebookSettings}.
     *
     * @return the {@link Bundle}.
     */
    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_ACCOUNT_NAME, mAccountName);
        if (mPhotoPrivacy != null && mPhotoPrivacy.length() > 0) {
            bundle.putString(BUNDLE_KEY_PHOTO_PRIVACY, mPhotoPrivacy);
        }
        bundle.putString(BUNDLE_KEY_ALBUM_NAME, mAlbumName);
        bundle.putString(BUNDLE_KEY_ALBUM_GRAPH_PATH, mAlbumGraphPath);

        return bundle;
    }

    /**
     * @return the user name associated with the account.
     */
    String getAccountName() {
        return mAccountName;
    }

    /**
     * @return the privacy level of shared photos. Only used for albums with 'custom' privacy level. May be null.
     */
    String optPhotoPrivacy() {
        return mPhotoPrivacy;
    }

    /**
     * @return the name of the album to share to.
     */
    String getAlbumName() {
        return mAlbumName;
    }

    /**
     * @return the graph path of the album to share to.
     */
    String getAlbumGraphPath() {
        return mAlbumGraphPath;
    }
}
