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
package com.groundupworks.lib.photobooth.wings;

import android.os.Handler;

import com.groundupworks.wings.IWingsLogger;
import com.groundupworks.wings.IWingsModule;
import com.groundupworks.wings.core.WingsDbHelper;
import com.groundupworks.wings.core.WingsService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for injecting classes into Wings.
 *
 * @author Benedict Lau
 */
@Module(
        staticInjections = {WingsService.class, WingsDbHelper.class},
        injects = {WingsService.class}
)
public class MyWingsModule implements IWingsModule {

    /**
     * Logger for debug messages.
     */
    private final IWingsLogger mLogger;

    /**
     * The {@link android.os.Handler} to post background tasks.
     */
    private final Handler mWorkerHandler;

    /**
     * Constructor.
     *
     * @param logger        logger for debug messages.
     * @param workerHandler the {@link android.os.Handler} to post background tasks.
     */
    public MyWingsModule(IWingsLogger logger, Handler workerHandler) {
        mLogger = logger;
        mWorkerHandler = workerHandler;
    }

    @Override
    @Singleton
    @Provides
    public IWingsLogger providesLogger() {
        return mLogger;
    }

    @Override
    @Singleton
    @Provides
    public Handler providesWorkerHandler() {
        return mWorkerHandler;
    }
}
