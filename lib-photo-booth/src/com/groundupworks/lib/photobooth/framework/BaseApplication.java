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
package com.groundupworks.lib.photobooth.framework;

import android.app.Application;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;

import com.groundupworks.lib.photobooth.R;
import com.groundupworks.lib.photobooth.helpers.LogsHelper;
import com.groundupworks.wings.IWingsModule;
import com.groundupworks.wings.Wings;
import com.groundupworks.wings.dropbox.DropboxEndpoint;
import com.groundupworks.wings.facebook.FacebookEndpoint;
import com.groundupworks.wings.gcp.GoogleCloudPrintEndpoint;

/**
 * Main {@link Application} class.
 *
 * @author Benedict Lau
 */
public abstract class BaseApplication extends Application {

    private static final String WORKER_THREAD_NAME = "workerThread";

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

        // Initialize Wings.
        IWingsModule module = new Wings.DefaultModule(this, getWorkerLooper(), new LogsHelper());
        Wings.init(module, FacebookEndpoint.class, DropboxEndpoint.class, GoogleCloudPrintEndpoint.class);
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
