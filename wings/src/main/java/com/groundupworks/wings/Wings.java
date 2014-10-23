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

import com.groundupworks.wings.core.PersistenceFactory;
import com.groundupworks.wings.core.WingsInjector;

/**
 * The public APIs of the Wings library. The client application must provide the dependencies via
 * {@link Wings#init(IWingsModule)} in its {@link android.app.Application#onCreate()}.
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
     * Initializer used to pass Wings dependencies via a concrete implementation of the
     * {@link com.groundupworks.wings.IWingsModule} interface.
     *
     * @param module the Dagger module implementing {@link com.groundupworks.wings.IWingsModule}.
     */
    public static final void init(IWingsModule module) {
        WingsInjector.init(module);
    }

    /**
     * Shares an image to the specified destination.
     *
     * @param filePath    the local path to the file to share.
     * @param destination the destination of the share.
     * @return true if successful; false otherwise.
     */
    public static boolean share(String filePath, int destination) {
        return PersistenceFactory.getInstance().getPersistence().createShareRequest(filePath, destination);
    }
}
