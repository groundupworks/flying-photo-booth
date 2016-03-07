/*
 * This file is part of Flying PhotoBooth.
 * 
 * Flying PhotoBooth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Flying PhotoBooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Flying PhotoBooth.  If not, see <http://www.gnu.org/licenses/>.
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
    // Package private methods.
    //

    /**
     * Attaches the {@link Fragment} as the view for this controller.
     *
     * @param fragment the {@link Fragment} to attach.
     */
    void attachFragment(ControllerBackedFragment<?> fragment) {
        mFragment = fragment;
    }

    /**
     * Detaches the {@link Fragment} from this controller.
     */
    void detachFragment() {
        mFragment = null;
    }

    /**
     * Sends an event to the worker handler to process.
     *
     * @param msg the event to handle.
     */
    void sendToWorkerHandler(Message msg) {
        mWorkerHandler.sendMessage(msg);
    }
}
