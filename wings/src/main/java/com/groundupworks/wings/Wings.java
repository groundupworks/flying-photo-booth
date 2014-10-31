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

    /**
     * Flag to track whether Wings is initialized.
     */
    private static volatile boolean sIsInitialized = false;

    /**
     * The protected set of endpoint instances that Wings can share to. Though the instances are
     * returned through the APIs, the set itself is never exposed, only copies of it, so the set is
     * essentially immutable after a successful initialization.
     */
    private static volatile Set<AbstractWingsEndpoint> sEndpoints = new HashSet<AbstractWingsEndpoint>();

    /**
     * Initializer used to pass dependencies for Wings. This method must be called in the
     * {@link android.app.Application#onCreate()}.
     *
     * @param module          the Dagger module implementing {@link com.groundupworks.wings.IWingsModule}.
     *                        Pass {@link com.groundupworks.wings.Wings.DefaultModule} to use the default
     *                        components.
     * @param endpointClazzes the endpoints that Wings can share to, passed as {@link java.lang.Class} types.
     * @return {@code true} if Wings is successfully initialized; {@code false} otherwise. Once {@link true}
     * has been returned, calling this method will have no effect, and the return value will always be
     * {@link true}.
     */
    public static synchronized final boolean init(IWingsModule module, Class<? extends AbstractWingsEndpoint>... endpointClazzes) {
        // No-op if already initialized.
        if (!sIsInitialized) {
            WingsInjector.init(module);
            try {
                final Set<AbstractWingsEndpoint> endpoints = new HashSet<AbstractWingsEndpoint>();
                final Set<Integer> endpointIds = new HashSet<Integer>();
                for (Class clazz : endpointClazzes) {
                    AbstractWingsEndpoint endpoint = (AbstractWingsEndpoint) clazz.newInstance();
                    endpoints.add(endpoint);

                    // Ensure that endpoint ids are unique.
                    if (!endpointIds.add(endpoint.getEndpointId())) {
                        return false;
                    }
                }
                sEndpoints = endpoints;
                sIsInitialized = true;
            } catch (InstantiationException e) {
                WingsInjector.getLogger().log(Wings.class, "init", e.toString());
            } catch (IllegalAccessException e) {
                WingsInjector.getLogger().log(Wings.class, "init", e.toString());
            }
        }

        return sIsInitialized;
    }

    /**
     * Gets the set of endpoint instances that Wings can share to.
     *
     * @return the set of endpoint instances.
     * @throws IllegalStateException Wings must be initialized. See {@link Wings#init(IWingsModule, Class[])}.
     */
    public static final Set<AbstractWingsEndpoint> getEndpoints() throws IllegalStateException {
        if (!sIsInitialized) {
            throw new IllegalStateException("Wings must be initialized. See Wings#init().");
        }
        return new HashSet<AbstractWingsEndpoint>(sEndpoints);
    }

    /**
     * Gets the instance of a specific endpoint that Wings can share to.
     *
     * @param endpointClazz the endpoint {@link java.lang.Class}.
     * @return the endpoint instance; or {@code null} if unavailable.
     * @throws IllegalStateException Wings must be initialized. See {@link Wings#init(IWingsModule, Class[])}.
     */
    public static final AbstractWingsEndpoint getEndpoint(Class<? extends AbstractWingsEndpoint> endpointClazz) {
        if (!sIsInitialized) {
            throw new IllegalStateException("Wings must be initialized. See Wings#init().");
        }
        AbstractWingsEndpoint selectedEndpoint = null;
        for (AbstractWingsEndpoint endpoint : sEndpoints) {
            if (endpointClazz.isInstance(endpoint)) {
                selectedEndpoint = endpoint;
                break;
            }
        }

        return selectedEndpoint;
    }

    /**
     * Shares an image to the specified destination.
     *
     * @param filePath    the local path to the file to share.
     * @param destination the destination of the share.
     * @return true if successful; false otherwise.
     * @throws IllegalStateException Wings must be initialized. See {@link Wings#init(IWingsModule, Class[])}.
     */
    public static boolean share(String filePath, WingsDestination destination) {
        if (!sIsInitialized) {
            throw new IllegalStateException("Wings must be initialized. See Wings#init().");
        }
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
