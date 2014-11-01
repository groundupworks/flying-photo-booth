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

import android.content.Context;
import android.os.Looper;

/**
 * Dagger module for injecting classes into Wings.
 *
 * @author Benedict Lau
 */
public interface IWingsModule {

    /**
     * Provides the {@link android.content.Context} to run Wings.
     *
     * @return the {@link android.content.Context}.
     */
    public Context provideContext();

    /**
     * Provides the {@link android.os.Looper} to run background tasks.
     *
     * @return the {@link android.os.Looper}.
     */
    public Looper provideLooper();

    /**
     * Provides logger for debug messages.
     *
     * @return the {@link com.groundupworks.wings.IWingsLogger}.
     */
    public IWingsLogger provideLogger();
}
