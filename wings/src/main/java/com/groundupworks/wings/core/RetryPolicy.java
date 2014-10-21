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
package com.groundupworks.wings.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.groundupworks.wings.R;

/**
 * A class that calculates how far in the future to schedule the next attempt to share. The logic will space out retries
 * in incrementally larger time intervals.
 *
 * @author Benedict Lau
 */
public class RetryPolicy {

    /**
     * Multiplier to convert a minute to milliseconds.
     */
    private static final long MINUTE_TO_MILLIS = 60000L;

    //
    // Private methods.
    //

    /**
     * Gets the Fibonacci number.
     *
     * @param n the index.
     * @return the Fibonacci number.
     */
    private static long getFibonacci(int n) {
        if (n == 0) {
            return 0L;
        } else if (n == 1) {
            return 1L;
        }

        return getFibonacci(n - 1) + getFibonacci(n - 2);
    }

    //
    // Package private methods.
    //

    /**
     * Gets how far in the future to schedule the next attempt to share. An internal counter is incremented every time
     * this method gets called, so a subsequent call will return a different time.
     *
     * @param context the {@link Context}.
     * @return the time in milliseconds.
     */
    static long incrementAndGetTime(Context context) {
        Context appContext = context.getApplicationContext();
        String key = appContext.getString(R.string.retry_policy__consecutive_fails);

        // Get the number of consecutive fails.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        int consecutiveFails = preferences.getInt(key, 0);

        // Synchronously increment the consecutive fail count.
        Editor editor = preferences.edit();
        editor.putInt(key, consecutiveFails + 1);
        editor.commit();

        return getFibonacci(consecutiveFails) * MINUTE_TO_MILLIS;
    }

    /**
     * Resets the internal counter. This should be called whenever a new record is added, and whenever all
     * {@link ShareRequest} completed successfully.
     *
     * @param context the {@link Context}.
     */
    static void reset(Context context) {
        Context appContext = context.getApplicationContext();
        String key = appContext.getString(R.string.retry_policy__consecutive_fails);

        // Synchronously reset consecutive fail count.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        Editor editor = preferences.edit();
        editor.putInt(key, 0);
        editor.commit();
    }
}
