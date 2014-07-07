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
package com.groundupworks.lib.photobooth.framework;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

/**
 * Base class for controller that works with a {@link ControllerBackedFragment}. Used to handle background tasks.
 *
 * @author Benedict Lau
 */
public abstract class BaseController {

    private Handler mWorkerHandler;

    private ControllerBackedFragment<?> mFragment = null;

    /**
     * Constructor.
     */
    public BaseController() {
        mWorkerHandler = new Handler(BaseApplication.getWorkerLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleEvent(msg);
            }
        };
    }

    //
    // Private methods.
    //

    /**
     * Requests view to handle ui update on the ui thread.
     *
     * @param msg the ui update to handle.
     */
    protected void sendUiUpdate(Message msg) {
        ControllerBackedFragment<?> fragment = mFragment;
        if (fragment != null) {
            fragment.sendToUiHandler(msg);
        }
    }

    /**
     * Handles events on the worker thread.
     *
     * @param msg the event to handle.
     */
    protected abstract void handleEvent(Message msg);

    //
    // Public methods.
    //

    /**
     * Attaches the {@link Fragment} as the view for this controller.
     *
     * @param fragment the {@link Fragment} to attach.
     */
    public void attachFragment(ControllerBackedFragment<?> fragment) {
        mFragment = fragment;
    }

    /**
     * Detaches the {@link Fragment} from this controller.
     */
    public void detachFragment() {
        mFragment = null;
    }

    /**
     * Sends an event to the worker handler to process.
     *
     * @param msg the event to handle.
     */
    public void sendToWorkerHandler(Message msg) {
        mWorkerHandler.sendMessage(msg);
    }
}
