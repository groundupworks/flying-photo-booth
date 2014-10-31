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
package com.groundupworks.wings.core;

import com.groundupworks.wings.WingsDestination;

/**
 * A model object representing a share request.
 *
 * @author Benedict Lau
 */
public class ShareRequest {

    //
    // Valid values for state. Package private because they are internally used.
    //

    static final int STATE_PENDING = 0;

    static final int STATE_PROCESSING = 1;

    static final int STATE_PROCESSED = 2;

    /**
     * The record id.
     */
    private int mId;

    /**
     * The local path to the file to share.
     */
    private String mFilePath;

    /**
     * The destination of the share.
     */
    private WingsDestination mDestination;

    /**
     * Package private constructor.
     *
     * @param id          the record id.
     * @param filePath    the local path to the file to share.
     * @param destination the destination of the share.
     */
    ShareRequest(int id, String filePath, WingsDestination destination) {
        mId = id;
        mFilePath = filePath;
        mDestination = destination;
    }

    //
    // Public methods.
    //

    /**
     * @return the record id.
     */
    public int getId() {
        return mId;
    }

    /**
     * @return the local path to the file to share.
     */
    public String getFilePath() {
        return mFilePath;
    }

    /**
     * @return the destination of the share.
     */
    public WingsDestination getDestination() {
        return mDestination;
    }
}
