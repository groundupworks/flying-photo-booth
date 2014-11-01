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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for system boot event schedule a trigger to the {@link WingsService}.
 *
 * @author Benedict Lau
 */
public class BootReceiver extends BroadcastReceiver {

    /**
     * Delay after boot to schedule {@link com.groundupworks.wings.core.WingsService}. 5 minutes in milliseconds.
     */
    private static final long AFTER_BOOT_DELAY = 300000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        WingsService.scheduleWingsService(context, AFTER_BOOT_DELAY);
    }
}
