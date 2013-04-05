/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import android.os.Message;
import com.groundupworks.lib.photobooth.framework.ControllerBackedFragment;
import com.groundupworks.partyphotobooth.controllers.CaptureController;

/**
 * Ui for the capture screen.
 * 
 * @author Benedict Lau
 */
public class CaptureFragment extends ControllerBackedFragment<CaptureController> {

    @Override
    protected CaptureController initController() {
        return new CaptureController();
    }

    @Override
    protected void handleUiUpdate(Message msg) {

    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link CaptureFragment} instance.
     * 
     * @return the new {@link CaptureFragment} instance.
     */
    public static CaptureFragment newInstance() {
        return new CaptureFragment();
    }
}
