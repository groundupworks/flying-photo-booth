package com.groundupworks.flyingphotobooth.facebook;

import android.os.Bundle;

/**
 * A model object to contain Facebook account settings. An instance of {@link AccountSettings} cannot be directly
 * constructed, but through one of its static newInstance() methods, which does validation internally to ensure the
 * validity of the constructed instance.
 * 
 * @author Benedict Lau
 */
public class AccountSettings {

    //
    // Bundle keys.
    //

    private static final String ACCOUNT_SETTINGS_BUNDLE_KEY_ACCOUNT_NAME = "accountName";

    private static final String ACCOUNT_SETTINGS_BUNDLE_KEY_PHOTO_PRIVACY = "photoPrivacy";

    private static final String ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_NAME = "albumName";

    private static final String ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_GRAPH_PATH = "albumGraphPath";

    //
    // Account settings that must be non-null and non-empty.
    //

    private String mAccountName;

    private String mPhotoPrivacy;

    private String mAlbumName;

    private String mAlbumGraphPath;

    /**
     * Private constructor.
     * 
     * @param accountName
     *            the user name associated with the account.
     * @param photoPrivacy
     *            the privacy level of shared photos.
     * @param albumName
     *            the name of the album to share to.
     * @param albumGraphPath
     *            the graph path of the album to share to.
     */
    private AccountSettings(String accountName, String photoPrivacy, String albumName, String albumGraphPath) {
        mAccountName = accountName;
        mPhotoPrivacy = photoPrivacy;
        mAlbumName = albumName;
        mAlbumGraphPath = albumGraphPath;
    }

    //
    // Package private methods.
    //

    /**
     * Creates a new {@link AccountSettings} instance.
     * 
     * @param accountName
     *            the user name associated with the account.
     * @param photoPrivacy
     *            the privacy level of shared photos.
     * @param albumName
     *            the name of the album to share to.
     * @param albumGraphPath
     *            the graph path of the album to share to.
     * @return a new {@link AccountSettings} instance; or null if any of the params are invalid.
     */
    static AccountSettings newInstance(String accountName, String photoPrivacy, String albumName, String albumGraphPath) {
        AccountSettings accountSettings = null;

        if (accountName != null && accountName.length() > 0 && photoPrivacy != null && photoPrivacy.length() > 0
                && albumName != null && albumName.length() > 0 && albumGraphPath != null && albumGraphPath.length() > 0) {
            accountSettings = new AccountSettings(accountName, photoPrivacy, albumName, albumGraphPath);
        }

        return accountSettings;
    }

    /**
     * Creates a new {@link AccountSettings} instance from a {@link Bundle} created by the {@link #toBundle()} method.
     * 
     * @param bundle
     *            the {@link Bundle}.
     * @return a new {@link AccountSettings} instance; or null if the {@link Bundle} is invalid.
     */
    static AccountSettings newInstance(Bundle bundle) {
        AccountSettings accountSettings = null;

        String accountName = bundle.getString(ACCOUNT_SETTINGS_BUNDLE_KEY_ACCOUNT_NAME);
        String photoPrivacy = bundle.getString(ACCOUNT_SETTINGS_BUNDLE_KEY_PHOTO_PRIVACY);
        String albumName = bundle.getString(ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_NAME);
        String albumGraphPath = bundle.getString(ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_GRAPH_PATH);
        if (accountName != null && accountName.length() > 0 && photoPrivacy != null && photoPrivacy.length() > 0
                && albumName != null && albumName.length() > 0 && albumGraphPath != null && albumGraphPath.length() > 0) {
            accountSettings = new AccountSettings(accountName, photoPrivacy, albumName, albumGraphPath);
        }

        return accountSettings;
    }

    /**
     * Creates a {@link Bundle} from the {@link AccountSettings}.
     * 
     * @return the {@link Bundle}.
     */
    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(ACCOUNT_SETTINGS_BUNDLE_KEY_ACCOUNT_NAME, mAccountName);
        bundle.putString(ACCOUNT_SETTINGS_BUNDLE_KEY_PHOTO_PRIVACY, mPhotoPrivacy);
        bundle.putString(ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_NAME, mAlbumName);
        bundle.putString(ACCOUNT_SETTINGS_BUNDLE_KEY_ALBUM_GRAPH_PATH, mAlbumGraphPath);

        return bundle;
    }

    /**
     * @return the user name associated with the account.
     */
    String getAccountName() {
        return mAccountName;
    }

    /**
     * @return the privacy level of shared photos.
     */
    String getPhotoPrivacy() {
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
