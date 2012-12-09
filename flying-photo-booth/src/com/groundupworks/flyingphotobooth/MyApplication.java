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

import android.app.Application;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Main {@link Application} class.
 * 
 * @author Benedict Lau
 */
public class MyApplication extends Application {

    private static final String WORKER_THREAD_NAME = "workerThread";

    private static Context sInstance;

    private static HandlerThread sWorkerThread = null;

    private static Looper sUiLooper = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set a static reference to the Application Context.
        sInstance = this;

        // Start a worker thread that has a {@link Looper} to execute background tasks.
        sWorkerThread = new HandlerThread(WORKER_THREAD_NAME);
        sWorkerThread.start();

        // Get a static reference to the ui {@link Looper}.
        sUiLooper = getMainLooper();
    }

    //
    // Public methods.
    //

    /**
     * @return the Application {@link Context}; or null if {@link Application#onCreate()} has not been called.
     */
    public static Context getContext() {
        return sInstance;
    }

    /**
     * @return the {@link Looper} to process background tasks.
     */
    public static Looper getWorkerLooper() {
        return sWorkerThread.getLooper();
    }

    /**
     * @return the {@link Looper} to process ui updates.
     */
    public static Looper getUiLooper() {
        return sUiLooper;
    }
}
