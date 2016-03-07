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
