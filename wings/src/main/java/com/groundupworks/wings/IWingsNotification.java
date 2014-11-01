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
package com.groundupworks.wings;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Intent;

/**
 * An interface for a {@link Notification} published by Wings.
 *
 * @author Benedict Lau
 */
public interface IWingsNotification {

    /**
     * @return a unique identifier for the notification within the {@link Application}.
     */
    int getId();

    /**
     * @return the title for the notification. Must not be null.
     */
    String getTitle();

    /**
     * @return the message for the notification. Must not be null.
     */
    String getMessage();

    /**
     * @return the ticker text for the notification. Must not be null.
     */
    String getTicker();

    /**
     * @return the {@link Intent} to launch an {@link Activity} when the notification is clicked. Must not be null.
     */
    Intent getIntent();
}
