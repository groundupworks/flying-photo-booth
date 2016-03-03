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
package com.groundupworks.lib.photobooth.helpers;

import android.util.Log;

import com.groundupworks.wings.IWingsLogger;
import com.groundupworks.wings.core.WingsService;

import java.util.Map;

/**
 * Wrapper for {@link Log} to log only in debug builds.
 *
 * @author Benedict Lau
 */
public class LogsHelper implements IWingsLogger {

    /**
     * Flag that controls whether debug logs are printed. Set to true to enable logging.
     */
    private static final boolean DEBUG = false;

    /**
     * Tag that appears in debug logs.
     */
    private static final String LOGS_TAG = "PB";

    //
    // Public methods.
    //

    /**
     * Logs to logcat only in debug builds.
     *
     * @param clazz      the {@link Class}.
     * @param methodName the name of the method.
     * @param msg        the debug message.
     */
    public static void slog(Class<?> clazz, String methodName, String msg) {
        if (DEBUG) {
            Log.d(LOGS_TAG, clazz.getSimpleName() + "#" + methodName + "() " + msg);
        }
    }

    @Override
    public void log(Class<?> clazz, String methodName, String msg) {
        slog(clazz, methodName, msg);
    }

    @Override
    public void log(String eventName, Map<String, String> eventParameters) {
        if (DEBUG) {
            Log.d(LOGS_TAG, eventName + ": " + eventParameters.toString());
        }
    }

    @Override
    public void log(String eventName) {
        if (DEBUG) {
            Log.d(LOGS_TAG, eventName);
        }
    }

    @Override
    public void onWingsServiceCreated(WingsService service) {
        // Do nothing.
    }

    @Override
    public void onWingsServiceDestroyed(WingsService service) {
        // Do nothing.
    }
}
