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

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;

/**
 * Base {@link Fragment} class that works with a {@link BaseController} for handling background tasks.
 *
 * @param <T> the {@link BaseController} subclass.
 * @author Benedict Lau
 */
public abstract class ControllerBackedFragment<T extends BaseController> extends Fragment {

    private T mController;

    private Handler mUiHandler;

    /**
     * Constructor.
     */
    public ControllerBackedFragment() {
        super();
        mUiHandler = new Handler(Looper.getMainLooper()) {
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
     * @param msg the event to handle.
     */
    protected void sendEvent(Message msg) {
        T controller = mController;
        if (controller != null) {
            controller.sendToWorkerHandler(msg);
        }
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
     * @param msg the ui update to handle.
     */
    protected abstract void handleUiUpdate(Message msg);

    //
    // Package private methods.
    //

    /**
     * Send update to the ui handler to process.
     *
     * @param msg the ui update to handle.
     */
    void sendToUiHandler(Message msg) {
        mUiHandler.sendMessage(msg);
    }
}
