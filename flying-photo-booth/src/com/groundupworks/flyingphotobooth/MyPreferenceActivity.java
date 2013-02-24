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
package com.groundupworks.flyingphotobooth;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.groundupworks.flyingphotobooth.dropbox.DropboxHelper;
import com.groundupworks.flyingphotobooth.facebook.FacebookHelper;

/**
 * The {@link Activity} to configure user preferences.
 * 
 * @author Benedict Lau
 */
public class MyPreferenceActivity extends PreferenceActivity {

    /**
     * Base uri for Google Play.
     */
    private static final String GOOGLE_PLAY_BASE_URI = "market://details?id=";

    /**
     * Listener for changes in preferences.
     */
    private OnSharedPreferenceChangeListener mPrefChangeListener = new MyOnSharedPreferenceChangeListener();

    /**
     * A {@link FacebookHelper}.
     */
    private FacebookHelper mFacebookHelper = new FacebookHelper();

    /**
     * A {@link DropboxHelper}.
     */
    private DropboxHelper mDropboxHelper = new DropboxHelper();

    //
    // Preference keys.
    //

    private String mArrangementKey;

    private String mNumPhotosKey;

    private String mFilterKey;

    private String mTriggerKey;

    private String mFacebookLinkKey;

    private String mFacebookAutoShareKey;

    private String mDropboxLinkKey;

    private String mDropboxAutoShareKey;

    //
    // Preferences.
    //

    private ListPreference mArrangementPref;

    private ListPreference mNumPhotosPref;

    private ListPreference mFilterPref;

    private ListPreference mTriggerPref;

    private Preference mFacebookLinkPref;

    private CheckBoxPreference mFacebookAutoSharePref;

    private Preference mDropboxLinkPref;

    private CheckBoxPreference mDropboxAutoSharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Get preference keys.
        mArrangementKey = getString(R.string.pref__arrangement_key);
        mNumPhotosKey = getString(R.string.pref__number_of_photos_key);
        mFilterKey = getString(R.string.pref__filter_key);
        mTriggerKey = getString(R.string.pref__trigger_key);
        mFacebookLinkKey = getString(R.string.pref__facebook_link_key);
        mFacebookAutoShareKey = getString(R.string.pref__facebook_auto_share_key);
        mDropboxLinkKey = getString(R.string.pref__dropbox_link_key);
        mDropboxAutoShareKey = getString(R.string.pref__dropbox_auto_share_key);

        // Get preferences.
        mArrangementPref = (ListPreference) findPreference(mArrangementKey);
        mNumPhotosPref = (ListPreference) findPreference(mNumPhotosKey);
        mFilterPref = (ListPreference) findPreference(mFilterKey);
        mTriggerPref = (ListPreference) findPreference(mTriggerKey);
        mFacebookLinkPref = findPreference(mFacebookLinkKey);
        mFacebookAutoSharePref = (CheckBoxPreference) findPreference(mFacebookAutoShareKey);
        mDropboxLinkPref = findPreference(mDropboxLinkKey);
        mDropboxAutoSharePref = (CheckBoxPreference) findPreference(mDropboxAutoShareKey);

        // Launch Facebook link request when clicked.
        mFacebookLinkPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Context appContext = MyPreferenceActivity.this.getApplicationContext();
                if (mFacebookHelper.isLinked(appContext)) {
                    // Unlink from Facebook.
                    mFacebookHelper.unlink(appContext);
                } else {
                    // Start Facebook link request.
                    mFacebookHelper.startLinkRequest(MyPreferenceActivity.this, null);
                }
                return false;
            }
        });

        // Launch Dropbox link request when clicked.
        mDropboxLinkPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Context appContext = MyPreferenceActivity.this.getApplicationContext();
                if (mDropboxHelper.isLinked(appContext)) {
                    // Unlink from Dropbox.
                    mDropboxHelper.unlink(appContext);
                } else {
                    // Start Dropbox link request.
                    mDropboxHelper.startLinkRequest(appContext);
                }
                return false;
            }
        });

        // Launch rating page on Google Play when clicked.
        Preference button = (Preference) findPreference(getString(R.string.pref__rate_key));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Uri uri = Uri.parse(GOOGLE_PLAY_BASE_URI + getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Do nothing.
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Finish Facebook link request.
        mFacebookHelper.onActivityResultImpl(this, null, requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register listener to shared preferences.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(mPrefChangeListener);

        // Refresh preferences.
        updateArrangementPrefAndDependents(preferences);
        updateNumPhotosPref(preferences);
        updateFilterPref(preferences);
        updateTriggerPref(preferences);
        updateFacebookPref(preferences);
        updateDropboxPref(preferences);

        // Finish Dropbox link request.
        mDropboxHelper.onResumeImpl(getApplicationContext());
    }

    @Override
    protected void onPause() {
        // Unregister listener to shared preferences.
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);

        super.onPause();
    }

    //
    // Private inner classes.
    //

    /**
     * Listener that changes {@link ListPreference} summaries when preferences change.
     */
    private class MyOnSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(mArrangementKey)) {
                updateArrangementPrefAndDependents(sharedPreferences);
            } else if (key.equals(mNumPhotosKey)) {
                updateNumPhotosPref(sharedPreferences);
            } else if (key.equals(mFilterKey)) {
                updateFilterPref(sharedPreferences);
            } else if (key.equals(mTriggerKey)) {
                updateTriggerPref(sharedPreferences);
            } else if (key.equals(mFacebookLinkKey) || key.equals(mFacebookAutoShareKey)
                    || key.equals(getString(R.string.facebook__account_name_key))
                    || key.equals(getString(R.string.facebook__album_name_key))) {
                updateFacebookPref(sharedPreferences);
            } else if (key.equals(mDropboxLinkKey) || key.equals(mDropboxAutoShareKey)
                    || key.equals(getString(R.string.dropbox__account_name_key))
                    || key.equals(getString(R.string.dropbox__share_url_key))) {
                updateDropboxPref(sharedPreferences);
            }
        }
    }

    //
    // Private methods.
    //

    /**
     * Updates the summary for the arrangement and the number of photos preferences.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateArrangementPrefAndDependents(SharedPreferences preferences) {
        String summary = "";
        Resources res = getResources();
        String selectedOption = preferences.getString(mArrangementKey, getString(R.string.pref__arrangement_default));

        String[] options = res.getStringArray(R.array.pref__arrangement_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__arrangement_options_summaries);
            summary = summaries[index];
        }

        mArrangementPref.setSummary(summary);

        // If the box arrangement is selected, disable number of photos pref and set its value to 4.
        boolean isBoxArrangement = selectedOption.equals(getString(R.string.pref__arrangement_box));
        mNumPhotosPref.setEnabled(!isBoxArrangement);
        if (isBoxArrangement) {
            mNumPhotosPref.setValue(getString(R.string.pref__number_of_photos_four));
        }
    }

    /**
     * Updates the summary for the number of photos preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateNumPhotosPref(SharedPreferences preferences) {
        String summary = "";
        Resources res = getResources();
        String selectedOption = preferences
                .getString(mNumPhotosKey, getString(R.string.pref__number_of_photos_default));

        String[] options = res.getStringArray(R.array.pref__number_of_photos_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__number_of_photos_options_summaries);
            summary = summaries[index];
        }

        mNumPhotosPref.setSummary(summary);
    }

    /**
     * Updates the summary for the filter preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateFilterPref(SharedPreferences preferences) {
        String summary = "";
        Resources res = getResources();
        String selectedOption = preferences.getString(mFilterKey, getString(R.string.pref__filter_default));

        String[] options = res.getStringArray(R.array.pref__filter_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__filter_options_summaries);
            summary = summaries[index];
        }

        mFilterPref.setSummary(summary);
    }

    /**
     * Updates the summary for the trigger preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateTriggerPref(SharedPreferences preferences) {
        String summary = "";
        Resources res = getResources();
        String selectedOption = preferences.getString(mTriggerKey, getString(R.string.pref__trigger_default));

        String[] options = res.getStringArray(R.array.pref__trigger_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__trigger_options_summaries);
            summary = summaries[index];
        }

        mTriggerPref.setSummary(summary);
    }

    /**
     * Updates the title, summary, and check box for the Facebook preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateFacebookPref(SharedPreferences preferences) {
        int titleRes = R.string.pref__facebook_link_title_default;
        String summary = getString(R.string.pref__facebook_link_summary_default);
        int widgetRes = R.layout.pref_facebook_checkbox_unselected;

        // Check if linked to Facebook account.
        boolean isLinked = mFacebookHelper.isLinked(this);
        if (isLinked) {
            // Get account information.
            String accountName = mFacebookHelper.getLinkedAccountName(this);
            String albumName = mFacebookHelper.getLinkedAlbumName(this);
            if (accountName != null && accountName.length() > 0 && albumName != null && albumName.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__facebook_link_title_linked;
                widgetRes = R.layout.pref_facebook_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mFacebookAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__facebook_link_summary_linked_auto_share, accountName, albumName);
                } else {
                    // Build on
                    summary = getString(R.string.pref__facebook_link_summary_linked_one_click_share, accountName,
                            albumName);
                }
            }

        }

        mFacebookLinkPref.setTitle(titleRes);
        mFacebookLinkPref.setSummary(summary);
        mFacebookLinkPref.setWidgetLayoutResource(widgetRes);

        // Enable auto share pref only if linked.
        mFacebookAutoSharePref.setEnabled(isLinked);
    }

    /**
     * Updates the title, summary, and check box for the Dropbox preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateDropboxPref(SharedPreferences preferences) {
        int titleRes = R.string.pref__dropbox_link_title_default;
        String summary = getString(R.string.pref__dropbox_link_summary_default);
        int widgetRes = R.layout.pref_dropbox_checkbox_unselected;

        // Check if linked to Dropbox account.
        boolean isLinked = mDropboxHelper.isLinked(this);
        if (isLinked) {
            // Get account information.
            String accountName = mDropboxHelper.getLinkedAccountName(this);
            String shareUrl = mDropboxHelper.getLinkedShareUrl(this);
            if (accountName != null && accountName.length() > 0 && shareUrl != null && shareUrl.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__dropbox_link_title_linked;
                widgetRes = R.layout.pref_dropbox_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mDropboxAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__dropbox_link_summary_linked_auto_share, accountName, shareUrl);
                } else {
                    // Build one-click share string.
                    summary = getString(R.string.pref__dropbox_link_summary_linked_one_click_share, accountName,
                            shareUrl);
                }
            }

        }

        mDropboxLinkPref.setTitle(titleRes);
        mDropboxLinkPref.setSummary(summary);
        mDropboxLinkPref.setWidgetLayoutResource(widgetRes);

        // Enable auto share pref only if linked.
        mDropboxAutoSharePref.setEnabled(isLinked);
    }

    /**
     * Finds the index of a selected option from a list of options.
     * 
     * @param selectedOption
     *            the selected option.
     * @param options
     *            the list of options.
     * @return the index of the selected option; or -1 if not found.
     */
    private int findIndex(String selectedOption, String[] options) {
        int returnIndex = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedOption)) {
                returnIndex = i;
                break;
            }
        }
        return returnIndex;
    }
}
