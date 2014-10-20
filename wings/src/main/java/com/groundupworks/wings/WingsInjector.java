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

import dagger.ObjectGraph;

/**
 * Injector for passing dependencies to Wings. The client application must provide the dependencies
 * via {@link com.groundupworks.wings.WingsInjector#init(IWingsModule)} in its
 * {@link android.app.Application#onCreate()}.
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
}
