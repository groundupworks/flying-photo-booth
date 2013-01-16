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
import android.os.Bundle;
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

    //
    // Preferences.
    //

    private ListPreference mArrangementPref;

    private ListPreference mFilterPref;

    private ListPreference mTriggerPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Get preference keys.
        mArrangementKey = getString(R.string.pref__arrangement_key);
        mFilterKey = getString(R.string.pref__filter_key);
        mTriggerKey = getString(R.string.pref__trigger_key);

        // Get preferences.
        mArrangementPref = (ListPreference) findPreference(mArrangementKey);
        mFilterPref = (ListPreference) findPreference(mFilterKey);
        mTriggerPref = (ListPreference) findPreference(mTriggerKey);

        // Get selected options.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String arrangement = preferences.getString(mArrangementKey, getString(R.string.pref__arrangement_default));
        String filter = preferences.getString(mFilterKey, getString(R.string.pref__filter_default));
        String trigger = preferences.getString(mTriggerKey, getString(R.string.pref__trigger_default));

        // Set summaries associated with selected options.
        mArrangementPref.setSummary(getArrangementSummary(arrangement));
        mFilterPref.setSummary(getFilterSummary(filter));
        mTriggerPref.setSummary(getTriggerSummary(trigger));

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
                String arrangement = sharedPreferences.getString(mArrangementKey,
                        getString(R.string.pref__arrangement_default));
                mArrangementPref.setSummary(getArrangementSummary(arrangement));
            } else if (key.equals(mFilterKey)) {
                String filter = sharedPreferences.getString(mFilterKey, getString(R.string.pref__filter_default));
                mFilterPref.setSummary(getFilterSummary(filter));
            } else if (key.equals(mTriggerKey)) {
                String trigger = sharedPreferences.getString(mTriggerKey, getString(R.string.pref__trigger_default));
                mTriggerPref.setSummary(getTriggerSummary(trigger));
            }
        }
    }

    //
    // Private methods.
    //

    /**
     * Gets the summary for the arrangement preference.
     * 
     * @param selectedOption
     *            the selected option.
     * @return the summary of the selected option; or an empty string if not found.
     */
    private String getArrangementSummary(String selectedOption) {
        String returnSummary = "";
        Resources res = getResources();
        String[] options = res.getStringArray(R.array.pref__arrangement_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__arrangement_options_summaries);
            returnSummary = summaries[index];
        }
        return returnSummary;
    }

    /**
     * Gets the summary for the filter preference.
     * 
     * @param selectedOption
     *            the selected option.
     * @return the summary of the selected option; or an empty string if not found.
     */
    private String getFilterSummary(String selectedOption) {
        String returnSummary = "";
        Resources res = getResources();
        String[] options = res.getStringArray(R.array.pref__filter_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__filter_options_summaries);
            returnSummary = summaries[index];
        }
        return returnSummary;
    }

    /**
     * Gets the summary for the trigger preference.
     * 
     * @param selectedOption
     *            the selected option.
     * @return the summary of the selected option; or an empty string if not found.
     */
    private String getTriggerSummary(String selectedOption) {
        String returnSummary = "";
        Resources res = getResources();
        String[] options = res.getStringArray(R.array.pref__trigger_options);
        int index = findIndex(selectedOption, options);
        if (index != -1) {
            String[] summaries = res.getStringArray(R.array.pref__trigger_options_summaries);
            returnSummary = summaries[index];
        }
        return returnSummary;
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
