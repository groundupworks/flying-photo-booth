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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * The {@link Activity} to configure user preferences.
 * 
 * @author Benedict Lau
 */
public class MyPreferenceActivity extends PreferenceActivity {

    //
    // FIXME Mock data.
    //

    private static final String MOCK_FACEBOOK_ACCOUNT_NAME = "Benedict";

    private static final String MOCK_FACEBOOK_PATH = "Wall";

    private static final String MOCK_DROPBOX_ACCOUNT_NAME = "ben.hy.lau@gmail.com";

    private static final String MOCK_DROPBOX_PATH = "Public/Photos/";

    /**
     * Base uri for Google Play.
     */
    private static final String GOOGLE_PLAY_BASE_URI = "market://details?id=";

    /**
     * Listener for changes in preferences.
     */
    private OnSharedPreferenceChangeListener mPrefChangeListener = new MyOnSharedPreferenceChangeListener();

    //
    // Preference keys.
    //

    private String mArrangementKey;

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

    private ListPreference mFilterPref;

    private ListPreference mTriggerPref;

    private CheckBoxPreference mFacebookLinkPref;

    private CheckBoxPreference mDropboxLinkPref;

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        // android.R.style.Theme_DeviceDefault_Light only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            theme.applyStyle(android.R.style.Theme_DeviceDefault_Light, true);
        } else {
            super.onApplyThemeResource(theme, resid, first);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Get preference keys.
        mArrangementKey = getString(R.string.pref__arrangement_key);
        mFilterKey = getString(R.string.pref__filter_key);
        mTriggerKey = getString(R.string.pref__trigger_key);
        mFacebookLinkKey = getString(R.string.pref__facebook_link_key);
        mFacebookAutoShareKey = getString(R.string.pref__facebook_auto_share_key);
        mDropboxLinkKey = getString(R.string.pref__dropbox_link_key);
        mDropboxAutoShareKey = getString(R.string.pref__dropbox_auto_share_key);

        // Get preferences.
        mArrangementPref = (ListPreference) findPreference(mArrangementKey);
        mFilterPref = (ListPreference) findPreference(mFilterKey);
        mTriggerPref = (ListPreference) findPreference(mTriggerKey);
        mFacebookLinkPref = (CheckBoxPreference) findPreference(mFacebookLinkKey);
        mDropboxLinkPref = (CheckBoxPreference) findPreference(mDropboxLinkKey);

        // Set summaries associated with selected options.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateArrangementPref(preferences);
        updateFilterPref(preferences);
        updateTriggerPref(preferences);
        updateFacebookPref(preferences);
        updateDropboxPref(preferences);

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
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .registerOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    @Override
    protected void onPause() {
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
                updateArrangementPref(sharedPreferences);
            } else if (key.equals(mFilterKey)) {
                updateFilterPref(sharedPreferences);
            } else if (key.equals(mTriggerKey)) {
                updateTriggerPref(sharedPreferences);
            } else if (key.equals(mFacebookLinkKey) || key.equals(mFacebookAutoShareKey)) {
                updateFacebookPref(sharedPreferences);
            } else if (key.equals(mDropboxLinkKey) || key.equals(mDropboxAutoShareKey)) {
                updateDropboxPref(sharedPreferences);
            }
        }
    }

    //
    // Private methods.
    //

    /**
     * Updates the summary for the arrangement preference.
     * 
     * @param preferences
     *            the {@link SharedPreferences} storing the preference.
     */
    private void updateArrangementPref(SharedPreferences preferences) {
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
        boolean isLinked = preferences.getBoolean(mFacebookLinkKey, false);

        // Check if linked to Facebook account.
        if (isLinked) {
            // Get account information.
            String accountName = preferences.getString(getString(R.string.pref__facebook_account_name_key),
                    MOCK_FACEBOOK_ACCOUNT_NAME);
            String path = preferences.getString(getString(R.string.pref__facebook_path_key), MOCK_FACEBOOK_PATH);
            if (accountName != null && accountName.length() > 0 && path != null && path.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__facebook_link_title_linked;
                widgetRes = R.layout.pref_facebook_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mFacebookAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__facebook_link_summary_linked_auto_share, accountName, path);
                } else {
                    // Build on
                    summary = getString(R.string.pref__facebook_link_summary_linked_one_click_share, accountName, path);
                }
            }

        }

        mFacebookLinkPref.setTitle(titleRes);
        mFacebookLinkPref.setSummary(summary);
        mFacebookLinkPref.setWidgetLayoutResource(widgetRes);
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
        boolean isLinked = preferences.getBoolean(mDropboxLinkKey, false);

        // Check if linked to Dropbox account.
        if (isLinked) {
            // Get account information.
            String accountName = preferences.getString(getString(R.string.pref__dropbox_account_name_key),
                    MOCK_DROPBOX_ACCOUNT_NAME);
            String path = preferences.getString(getString(R.string.pref__dropbox_path_key), MOCK_DROPBOX_PATH);
            if (accountName != null && accountName.length() > 0 && path != null && path.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__dropbox_link_title_linked;
                widgetRes = R.layout.pref_dropbox_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mDropboxAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__dropbox_link_summary_linked_auto_share, accountName, path);
                } else {
                    // Build one-click share string.
                    summary = getString(R.string.pref__dropbox_link_summary_linked_one_click_share, accountName, path);
                }
            }

        }

        mDropboxLinkPref.setTitle(titleRes);
        mDropboxLinkPref.setSummary(summary);
        mDropboxLinkPref.setWidgetLayoutResource(widgetRes);
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
