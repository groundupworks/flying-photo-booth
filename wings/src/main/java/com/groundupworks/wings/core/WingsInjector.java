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
package com.groundupworks.wings.core;

import android.content.Context;
import android.os.Looper;

import com.groundupworks.wings.IWingsLogger;
import com.groundupworks.wings.IWingsModule;

import dagger.ObjectGraph;

/**
 * Injector for passing dependencies to Wings.
 *
 * @author Benedict Lau
 */
public final class WingsInjector {

    /**
     * The {@link dagger.ObjectGraph} to hold all Wings dependencies.
     */
    private static ObjectGraph sObjectGraph;

    /**
     * Initializer used to pass Wings dependencies via a concrete implementation of the
     * {@link com.groundupworks.wings.IWingsModule} interface.
     *
     * @param module the Dagger module implementing {@link com.groundupworks.wings.IWingsModule}.
     */
    public static final void init(IWingsModule module) {
        sObjectGraph = ObjectGraph.create(module);
    }

    /**
     * Injects dependencies into the static fields and methods of the class.
     */
    public static final void injectStatics() {
        sObjectGraph.injectStatics();
    }

    /**
     * Injects dependencies into the fields and methods of {@code instance}.
     *
     * @param <T> the type of {@code instance}.
     */
    public static final <T> void inject(T instance) {
        sObjectGraph.inject(instance);
    }

    /**
     * Gets the {@link android.content.Context} that Wings is running on.
     *
     * @return the {@link android.content.Context}.
     */
    public static final Context getApplicationContext() {
        return sObjectGraph.get(Context.class);
    }

    /**
     * Gets the {@link android.os.Looper} to run background tasks.
     *
     * @return the {@link android.os.Looper}.
     */
    public static final Looper getWorkerLooper() {
        return sObjectGraph.get(Looper.class);
    }

    /**
     * Gets the logger for debug messages.
     *
     * @return the {@link com.groundupworks.wings.IWingsLogger}.
     */
    public static final IWingsLogger getLogger() {
        return sObjectGraph.get(IWingsLogger.class);
    }

    /**
     * Gets the Wings database.
     *
     * @return the {@link com.groundupworks.wings.core.WingsDbHelper}.
     */
    public static final WingsDbHelper getDatabase() {
        return sObjectGraph.get(WingsDbHelper.class);
    }
}
