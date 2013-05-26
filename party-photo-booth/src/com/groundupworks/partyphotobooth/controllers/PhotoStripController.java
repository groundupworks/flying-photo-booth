/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.controllers;

import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;
import android.os.Message;
import com.groundupworks.lib.photobooth.framework.BaseController;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;

public class PhotoStripController extends BaseController {

    /**
     * Map storing captured frames of bitmap for constructing a photo strip.
     */
    private Map<Integer, Bitmap> mFrames = new HashMap<Integer, Bitmap>();

    @Override
    protected void handleEvent(Message msg) {
        switch (msg.what) {
            case PhotoStripFragment.JPEG_DATA_READY:
                break;
        }
    }
}