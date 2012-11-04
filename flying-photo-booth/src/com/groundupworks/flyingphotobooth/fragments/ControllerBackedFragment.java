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
package com.groundupworks.flyingphotobooth.fragments;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import com.groundupworks.flyingphotobooth.MyApplication;
import com.groundupworks.flyingphotobooth.controllers.BaseController;

/**
 * Base {@link Fragment} class that works with a {@link BaseController} for handling background tasks.
 * 
 * @author Benedict Lau
 * 
 * @param <T>
 *            the {@link BaseController} subclass.
 */
public abstract class ControllerBackedFragment<T extends BaseController> extends Fragment {

    private T mController;

    private Handler mUiHandler;

    /**
     * Constructor.
     */
    public ControllerBackedFragment() {
        super();
        mUiHandler = new Handler(MyApplication.getUiLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleUiUpdate(msg);
            }
        };
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mController = initController();
        mController.attachFragment(this);
    }

    @Override
    public void onDetach() {
        mController.detachFragment();
        mController = null;
        super.onDetach();
    }

    //
    // Private methods.
    //

    /**
     * Requests controller to handle event on the background thread.
     * 
     * @param msg
     *            the event to handle.
     */
    protected void sendEvent(Message msg) {
        mController.sendToWorkerHandler(msg);
    }

    /**
     * Gets the controller instance associated with this {@link ControllerBackedFragment}.
     * 
     * @return the {@link BaseController} subclass backing this {@link ControllerBackedFragment}.
     */
    protected T getController() {
        return mController;
    }

    /**
     * Initializes a controller instance to back this {@link ControllerBackedFragment}.
     * 
     * @return the {@link BaseController} subclass backing this {@link ControllerBackedFragment}.
     */
    protected abstract T initController();

    /**
     * Handles ui update messages on the ui thread.
     * 
     * @param msg
     *            the ui update to handle.
     */
    protected abstract void handleUiUpdate(Message msg);

    //
    // Public methods.
    //

    /**
     * Send update to the ui handler to process.
     * 
     * @param msg
     *            the ui update to handle.
     */
    public void sendToUiHandler(Message msg) {
        mUiHandler.sendMessage(msg);
    }
}
