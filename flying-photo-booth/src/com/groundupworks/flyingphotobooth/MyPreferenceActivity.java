/*
 * This file is part of Flying PhotoBooth.
 * 
 * Flying PhotoBooth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Flying PhotoBooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Flying PhotoBooth.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.groundupworks.flyingphotobooth;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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

import com.groundupworks.wings.Wings;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.dropbox.DropboxEndpoint;
import com.groundupworks.wings.facebook.FacebookEndpoint;
import com.groundupworks.wings.gcp.GoogleCloudPrintEndpoint;
import com.squareup.otto.Subscribe;

import java.util.Set;

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

    private String mGcpLinkKey;

    private String mGcpAutoShareKey;

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

    private Preference mGcpLinkPref;

    private CheckBoxPreference mGcpAutoSharePref;

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
        mGcpLinkKey = getString(R.string.pref__gcp_link_key);
        mGcpAutoShareKey = getString(R.string.pref__gcp_auto_share_key);

        // Get preferences.
        mArrangementPref = (ListPreference) findPreference(mArrangementKey);
        mNumPhotosPref = (ListPreference) findPreference(mNumPhotosKey);
        mFilterPref = (ListPreference) findPreference(mFilterKey);
        mTriggerPref = (ListPreference) findPreference(mTriggerKey);
        mFacebookLinkPref = findPreference(mFacebookLinkKey);
        mFacebookAutoSharePref = (CheckBoxPreference) findPreference(mFacebookAutoShareKey);
        mDropboxLinkPref = findPreference(mDropboxLinkKey);
        mDropboxAutoSharePref = (CheckBoxPreference) findPreference(mDropboxAutoShareKey);
        mGcpLinkPref = findPreference(mGcpLinkKey);
        mGcpAutoSharePref = (CheckBoxPreference) findPreference(mGcpAutoShareKey);

        // Launch Facebook link request when clicked.
        mFacebookLinkPref.setOnPreferenceClickListener(new MyLinkPreferenceClickListener(FacebookEndpoint.class));

        // Launch Dropbox link request when clicked.
        mDropboxLinkPref.setOnPreferenceClickListener(new MyLinkPreferenceClickListener(DropboxEndpoint.class));

        // Launch GCP link request when clicked.
        mGcpLinkPref.setOnPreferenceClickListener(new MyLinkPreferenceClickListener(GoogleCloudPrintEndpoint.class));

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

        // Call Wings APIs.
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            endpoint.onActivityResultImpl(this, null, requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call Wings APIs.
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            endpoint.onResumeImpl();
        }

        // Register listener to shared preferences.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(mPrefChangeListener);

        // Refresh preferences.
        updateArrangementPrefAndDependents(preferences);
        updateNumPhotosPref(preferences);
        updateFilterPref(preferences);
        updateTriggerPref(preferences);

        // Subscribe to Wings link events.
        Wings.subscribe(this);
    }

    @Override
    protected void onPause() {
        // Unsubscribe to Wings link events.
        Wings.unsubscribe(this);

        // Unregister listener to shared preferences.
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //
    // Subscriptions.
    //

    @Subscribe
    public void handleLinkEvent(FacebookEndpoint.LinkEvent event) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateFacebookPref(preferences);
    }

    @Subscribe
    public void handleLinkEvent(DropboxEndpoint.LinkEvent event) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateDropboxPref(preferences);
    }

    @Subscribe
    public void handleLinkEvent(GoogleCloudPrintEndpoint.LinkEvent event) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateGcpPref(preferences);
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
            } else if (key.equals(mFacebookLinkKey) || key.equals(mFacebookAutoShareKey)) {
                updateFacebookPref(sharedPreferences);
            } else if (key.equals(mDropboxLinkKey) || key.equals(mDropboxAutoShareKey)) {
                updateDropboxPref(sharedPreferences);
            } else if (key.equals(mGcpLinkKey) || key.equals(mGcpAutoShareKey)) {
                updateGcpPref(sharedPreferences);
            }
        }
    }

    /**
     * Listener that handles linking and unlinking with a Wings endpoint.
     */
    private class MyLinkPreferenceClickListener implements Preference.OnPreferenceClickListener {

        /**
         * The Wings endpoint.
         */
        private WingsEndpoint mEndpoint;

        /**
         * Constructor.
         *
         * @param endpointClazz the {@link java.lang.Class} of the Wings endpoint to toggle linking.
         */
        private MyLinkPreferenceClickListener(Class<? extends WingsEndpoint> endpointClazz) {
            mEndpoint = Wings.getEndpoint(endpointClazz);
        }

        @Override
        public boolean onPreferenceClick(android.preference.Preference arg0) {
            if (mEndpoint.isLinked()) {
                // Unlink from endpoint.
                mEndpoint.unlink();
            } else {
                // Start link request.
                mEndpoint.startLinkRequest(MyPreferenceActivity.this, null);
            }
            return false;
        }
    }

    //
    // Private methods.
    //

    /**
     * Updates the summary for the arrangement and the number of photos preferences.
     *
     * @param preferences the {@link SharedPreferences} storing the preference.
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
     * @param preferences the {@link SharedPreferences} storing the preference.
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
     * @param preferences the {@link SharedPreferences} storing the preference.
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
     * @param preferences the {@link SharedPreferences} storing the preference.
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
     * @param preferences the {@link SharedPreferences} storing the preference.
     */
    private void updateFacebookPref(SharedPreferences preferences) {
        int titleRes = R.string.pref__wings_endpoint_link_title_default;
        String summary = getString(R.string.pref__wings_endpoint_link_summary_default);
        int widgetRes = R.layout.pref_facebook_checkbox_unselected;

        // Check if linked to Facebook account.
        WingsEndpoint endpoint = Wings.getEndpoint(FacebookEndpoint.class);
        boolean isLinked = endpoint.isLinked();
        if (isLinked) {
            // Get account information.
            String destinationDescription = endpoint.getLinkInfo().mDestinationDescription;
            if (destinationDescription != null && destinationDescription.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__wings_endpoint_link_title_linked;
                widgetRes = R.layout.pref_facebook_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mFacebookAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_auto_share, destinationDescription);
                } else {
                    // Build one-click share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_one_click_share, destinationDescription);
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
     * @param preferences the {@link SharedPreferences} storing the preference.
     */
    private void updateDropboxPref(SharedPreferences preferences) {
        int titleRes = R.string.pref__wings_endpoint_link_title_default;
        String summary = getString(R.string.pref__wings_endpoint_link_summary_default);
        int widgetRes = R.layout.pref_dropbox_checkbox_unselected;

        // Check if linked to Dropbox account.
        WingsEndpoint endpoint = Wings.getEndpoint(DropboxEndpoint.class);
        boolean isLinked = endpoint.isLinked();
        if (isLinked) {
            // Get account information.
            String destinationDescription = endpoint.getLinkInfo().mDestinationDescription;
            if (destinationDescription != null && destinationDescription.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__wings_endpoint_link_title_linked;
                widgetRes = R.layout.pref_dropbox_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mDropboxAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_auto_share, destinationDescription);
                } else {
                    // Build one-click share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_one_click_share, destinationDescription);
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
     * Updates the title, summary, and check box for the Google Cloud Print preference.
     *
     * @param preferences the {@link SharedPreferences} storing the preference.
     */
    private void updateGcpPref(SharedPreferences preferences) {
        int titleRes = R.string.pref__wings_endpoint_link_title_default;
        String summary = getString(R.string.pref__wings_endpoint_link_summary_default);
        int widgetRes = R.layout.pref_gcp_checkbox_unselected;

        // Check if linked to GCP account.
        WingsEndpoint endpoint = Wings.getEndpoint(GoogleCloudPrintEndpoint.class);
        boolean isLinked = endpoint.isLinked();
        if (isLinked) {
            // Get account information.
            String destinationDescription = endpoint.getLinkInfo().mDestinationDescription;
            if (destinationDescription != null && destinationDescription.length() > 0) {
                // Select linked title and widget resource.
                titleRes = R.string.pref__wings_endpoint_link_title_linked;
                widgetRes = R.layout.pref_gcp_checkbox_selected;

                // Check if auto share is enabled.
                boolean isAutoShared = preferences.getBoolean(mGcpAutoShareKey, false);
                if (isAutoShared) {
                    // Build auto share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_auto_share, destinationDescription);
                } else {
                    // Build one-click share string.
                    summary = getString(R.string.pref__wings_endpoint_link_summary_linked_one_click_share, destinationDescription);
                }
            }

        }

        mGcpLinkPref.setTitle(titleRes);
        mGcpLinkPref.setSummary(summary);
        mGcpLinkPref.setWidgetLayoutResource(widgetRes);

        // Enable auto share pref only if linked.
        mGcpAutoSharePref.setEnabled(isLinked);
    }

    /**
     * Finds the index of a selected option from a list of options.
     *
     * @param selectedOption the selected option.
     * @param options        the list of options.
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
