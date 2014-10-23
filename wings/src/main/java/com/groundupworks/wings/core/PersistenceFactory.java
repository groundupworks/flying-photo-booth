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

import javax.inject.Inject;

/**
 * Factory to construct the class that provides the persistence of a
 * {@link com.groundupworks.wings.core.ShareRequest}.
 *
 * @author Benedict Lau
 */
public class PersistenceFactory {

    /**
     * Singleton.
     */
    private static PersistenceFactory sInstance;

    /**
     * Helper to access the database used to provide persistence.
     */
    @Inject
    WingsDbHelper mDbHelper;

    /**
     * Gets the {@link PersistenceFactory} singleton.
     *
     * @return the singleton.
     */
    public synchronized static final PersistenceFactory getInstance() {
        if (sInstance == null) {
            sInstance = new PersistenceFactory();
        }
        return sInstance;
    }

    /**
     * Private constructor.
     */
    private PersistenceFactory() {
        WingsInjector.inject(this);
    }

    /**
     * Gets the class providing the persistence of a {@link com.groundupworks.wings.core.ShareRequest}.
     *
     * @return the class.
     */
    public WingsDbHelper getPersistence() {
        return mDbHelper;
    }
}
