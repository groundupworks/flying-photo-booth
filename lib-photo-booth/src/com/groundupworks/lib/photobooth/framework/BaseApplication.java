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
package com.groundupworks.lib.photobooth.framework;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.groundupworks.lib.photobooth.helpers.LogsHelper;
import com.groundupworks.lib.photobooth.wings.MyWingsModule;
import com.groundupworks.wings.IWingsInjector;

import dagger.ObjectGraph;

/**
 * Main {@link Application} class.
 *
 * @author Benedict Lau
 */
public abstract class BaseApplication extends Application implements IWingsInjector {

    private static final String WORKER_THREAD_NAME = "workerThread";

    private static ObjectGraph sObjectGraph;

    private static Context sInstance;

    private static HandlerThread sWorkerThread = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // Set a static reference to the Application Context.
        sInstance = this;

        // Start a worker thread that has a {@link Looper} to execute background tasks.
        sWorkerThread = new HandlerThread(WORKER_THREAD_NAME);
        sWorkerThread.start();

        sObjectGraph = ObjectGraph.create(new MyWingsModule(new LogsHelper(), new Handler(getWorkerLooper())));
    }


    @Override
    public void injectStatics() {
        sObjectGraph.injectStatics();
    }

    @Override
    public <T> void inject(T instance) {
        sObjectGraph.inject(instance);
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
}
