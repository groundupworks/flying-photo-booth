/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.controllers;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;
import com.groundupworks.lib.photobooth.framework.BaseController;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.partyphotobooth.MyApplication;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoStripArrangement;

public class PhotoStripController extends BaseController {

    //
    // Controller events. The ui should be notified of these events.
    //

    public static final int ERROR_OCCURRED = -1;

    public static final int THUMB_READY = 0;

    public static final int PHOTO_STRIP_READY = 1;

    /**
     * The {@link Application} {@link Context}.
     */
    private Context mContext;

    /**
     * The {@link PreferencesHelper}.
     */
    private PreferencesHelper mPreferencesHelper;

    /**
     * The photo strip arrangement.
     */
    private PhotoStripArrangement mArrangement;

    /**
     * The total number of frames to capture.
     */
    private int mFramesTotal;

    /**
     * Pixel size of frame thumbnails.
     */
    private int mThumbSize;

    /**
     * Map storing captured frames of bitmap for constructing a photo strip.
     */
    private SparseArray<Bitmap> mFramesMap;

    /**
     * Constructor.
     */
    public PhotoStripController() {
        mContext = MyApplication.getContext();

        // Set params from preferences.
        mPreferencesHelper = new PreferencesHelper();
        mArrangement = mPreferencesHelper.getPhotoStripArrangement(mContext);
        mFramesTotal = mPreferencesHelper.getPhotoStripNumPhotos(mContext);
        mFramesMap = new SparseArray<Bitmap>(mFramesTotal);

        // Set params from resources.
        Resources res = mContext.getResources();
        mThumbSize = res.getDimensionPixelSize(R.dimen.photo_thumb_size);
    }

    @Override
    protected void handleEvent(Message msg) {
        switch (msg.what) {
            case PhotoStripFragment.JPEG_DATA_READY:
                Bundle bundle = msg.getData();
                byte[] jpegData = bundle.getByteArray(PhotoStripFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA);
                float rotation = bundle.getFloat(PhotoStripFragment.MESSAGE_BUNDLE_KEY_ROTATION);
                boolean reflection = bundle.getBoolean(PhotoStripFragment.MESSAGE_BUNDLE_KEY_REFLECTION);
                processJpegData(jpegData, rotation, reflection);
                break;
        }
    }

    //
    // Private methods.
    //

    /**
     * Reports an error event to ui.
     */
    private void reportError() {
        Message uiMsg = Message.obtain();
        uiMsg.what = ERROR_OCCURRED;
        sendUiUpdate(uiMsg);
    }

    /**
     * Process Jpeg data and notify ui.
     * 
     * @param jpegData
     *            byte array of Jpeg data.
     * @param rotation
     *            clockwise rotation applied to image in degrees.
     * @param reflection
     *            horizontal reflection applied to image.
     */
    private void processJpegData(byte[] jpegData, float rotation, boolean reflection) {
        Bitmap frame = ImageHelper.createImage(jpegData, rotation, reflection, null);
        if (frame != null) {
            // Create thumbnail bitmap.
            Bitmap thumb = Bitmap.createScaledBitmap(frame, mThumbSize, mThumbSize, true);
            if (thumb != null) {
                // Store frame bitmap.
                boolean isPhotoStripComplete = storeFrame(frame);

                // Notify ui.
                Message uiMsg = Message.obtain();
                if (isPhotoStripComplete) {
                    // The last thumbnail bitmap is ready. The photo strip is complete.
                    uiMsg.what = PHOTO_STRIP_READY;
                } else {
                    // A thumbnail bitmap is ready. The photo strip still needs more frames.
                    uiMsg.what = THUMB_READY;
                }
                uiMsg.obj = thumb;
                sendUiUpdate(uiMsg);
            } else {
                // An error has occurred.
                reportError();
            }
        } else {
            // An error has occurred.
            reportError();
        }
    }

    /**
     * Store frame bitmap in next available slot in frames map.
     * 
     * @param frame
     *            the bitmap to store. Must not be null.
     * @return true if this is the last frame and the photo strip is complete; false otherwise.
     */
    private boolean storeFrame(Bitmap frame) {
        boolean isPhotoStripComplete = false;
        for (int i = 0; i < mFramesTotal; i++) {
            if (mFramesMap.get(i) == null) {
                mFramesMap.put(i, frame);
                isPhotoStripComplete = i == mFramesTotal - 1;
                break;
            }
        }
        return isPhotoStripComplete;
    }
}