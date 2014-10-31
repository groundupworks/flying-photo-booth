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

/**
 * A destination that Wings can share to.
 *
 * @author Benedict Lau
 */
public class WingsDestination {

    private static final int FOUR_BYTES = 0xffff;

    /**
     * The destination id for the share.
     */
    private final int mId;

    /**
     * The endpoint id for the share.
     */
    private final int mEndpointId;

    /**
     * Creates a new instance of {@link com.groundupworks.wings.WingsDestination} from a hash generated
     * using {@link WingsDestination#getHash()}.
     *
     * @param hash the hash.
     */
    public static final WingsDestination from(int hash) {
        final int id = (hash >> FOUR_BYTES) & FOUR_BYTES;
        final int endpointId = hash & FOUR_BYTES;
        return new WingsDestination(id, endpointId);
    }


    /**
     * Constructor.
     *
     * @param id         the destination id for the share.
     * @param endpointId the endpoint id for the share.
     */
    public WingsDestination(int id, int endpointId) {
        mId = id;
        mEndpointId = endpointId;
    }

    /**
     * Gets the destination id.
     *
     * @return the destination id.
     */
    public int getId() {
        return mId;
    }

    /**
     * Gets the endpoint id.
     *
     * @return the endpoint id.
     */
    public int getEndpointId() {
        return mEndpointId;
    }

    /**
     * Gets the hash that uniquely identifies the destination across all endpoints.
     *
     * @return the destination hash.
     */
    public int getHash() {
        return (mId << FOUR_BYTES) | mEndpointId;
    }
}
