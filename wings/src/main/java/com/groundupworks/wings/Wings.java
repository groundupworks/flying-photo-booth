/*
 * Copyright (C) 2014 Benedict Lau
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
package com.groundupworks.wings;

import android.app.Application;
import android.content.Context;
import android.os.Looper;

import com.groundupworks.wings.core.WingsDbHelper;
import com.groundupworks.wings.core.WingsInjector;
import com.groundupworks.wings.core.WingsService;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * The public APIs of the Wings library. The client application must provide the dependencies via
 * {@link Wings#init(IWingsModule, Class[])} in its {@link android.app.Application#onCreate()}.
 *
 * @author Benedict Lau
 */
public final class Wings {

    //
    // Valid values for destination.
    //

    /**
     * Destination for the Facebook endpoint.
     */
    public static final int DESTINATION_FACEBOOK = 0;

    /**
     * Destination for the Dropbox endpoint.
     */
    public static final int DESTINATION_DROPBOX = 1;

    /**
     * The set of endpoints that Wings can share to.
     */
    private static Set<AbstractWingsEndpoint> sEndpoints;

    /**
     * Initializer used to pass Wings dependencies via a concrete implementation of the
     * {@link com.groundupworks.wings.IWingsModule} interface.
     *
     * @param module the Dagger module implementing {@link com.groundupworks.wings.IWingsModule}.
     * @param Ts     the endpoints that Wings can share to.
     * @param <Ts>   the concrete types of an {@link com.groundupworks.wings.AbstractWingsEndpoint}.
     * @return {@code true} if Wings is successfully initialized; {@code false} otherwise.
     */
    public synchronized static final <Ts extends AbstractWingsEndpoint> boolean init(IWingsModule module, Class... Ts) {
        boolean isSuccessful = false;
        WingsInjector.init(module);
        try {
            final Set<AbstractWingsEndpoint> endpoints = new HashSet<AbstractWingsEndpoint>();
            for (Class T : Ts) {
                endpoints.add((AbstractWingsEndpoint) T.newInstance());
            }
            sEndpoints = endpoints;
            isSuccessful = true;
        } catch (InstantiationException e) {
            WingsInjector.getLogger().log(Wings.class, "init", e.toString());
        } catch (IllegalAccessException e) {
            WingsInjector.getLogger().log(Wings.class, "init", e.toString());
        }

        return isSuccessful;
    }

    /**
     * Gets the set of endpoints that Wings can share to.
     *
     * @return the set of endpoints that subclass {@link com.groundupworks.wings.AbstractWingsEndpoint}.
     */
    public synchronized static final Set<AbstractWingsEndpoint> getEndpoints() {
        return new HashSet<AbstractWingsEndpoint>(sEndpoints);
    }

    /**
     * Shares an image to the specified destination.
     *
     * @param filePath    the local path to the file to share.
     * @param destination the destination of the share.
     * @return true if successful; false otherwise.
     */
    public synchronized static boolean share(String filePath, int destination) {
        return WingsInjector.getDatabase().createShareRequest(filePath, destination);
    }

    /**
     * The default implementation of {@link com.groundupworks.wings.IWingsModule}.
     */
    @Module(
            staticInjections = {WingsService.class, WingsDbHelper.class},
            injects = {Context.class, Looper.class, IWingsLogger.class, WingsService.class, WingsDbHelper.class}
    )
    public static class DefaultModule implements IWingsModule {

        /**
         * The {@link android.content.Context} to run Wings.
         */
        private final Context mContext;

        /**
         * The {@link android.os.Looper} to run background tasks.
         */
        private final Looper mLooper;

        /**
         * THe logger for debug messages.
         */
        private final IWingsLogger mLogger;

        /**
         * Constructor.
         *
         * @param application the {@link android.app.Application} running Wings.
         * @param looper      the {@link android.os.Looper} to run background tasks.
         * @param logger      the logger for debug messages.
         */
        public DefaultModule(Application application, Looper looper, IWingsLogger logger) {
            mContext = application.getApplicationContext();
            mLooper = looper;
            mLogger = logger;
        }

        @Singleton
        @Provides
        public Context provideContext() {
            return mContext;
        }

        @Singleton
        @Provides
        public Looper provideLooper() {
            return mLooper;
        }

        @Singleton
        @Provides
        public IWingsLogger provideLogger() {
            return mLogger;
        }
    }
}
