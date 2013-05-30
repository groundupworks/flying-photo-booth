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

    public static final int ERROR_JPEG_DATA = -1;

    public static final int THUMB_BITMAP_READY = 0;

    public static final int FRAME_REMOVED = 1;

    public static final int PHOTO_STRIP_READY = 2;

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
            case PhotoStripFragment.FRAME_REMOVAL:
                processFrameRemoval(msg.arg1);
                break;
            default:
                break;
        }
    }

    //
    // Private methods.
    //

    /**
     * Reports an error event to ui.
     * 
     * @param error
     *            the error.
     */
    private void reportError(int error) {
        Message uiMsg = Message.obtain();
        uiMsg.what = error;
        sendUiUpdate(uiMsg);
    }

    /**
     * Processes Jpeg data and notifies ui.
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
                int key = storeFrame(frame);

                // Notify ui.
                Message uiMsg = Message.obtain();
                if (isPhotoStripComplete()) {
                    // The last thumbnail bitmap is ready. The photo strip is complete.
                    uiMsg.what = PHOTO_STRIP_READY;
                } else {
                    // A thumbnail bitmap is ready. The photo strip still needs more frames.
                    uiMsg.what = THUMB_BITMAP_READY;
                }
                uiMsg.arg1 = key;
                uiMsg.obj = thumb;
                sendUiUpdate(uiMsg);
            } else {
                // An error has occurred.
                reportError(ERROR_JPEG_DATA);
            }
        } else {
            // An error has occurred.
            reportError(ERROR_JPEG_DATA);
        }
    }

    /**
     * Processes a frame removal request and notifies ui.
     * 
     * @param key
     *            the key of the frame to remove.
     */
    private void processFrameRemoval(int key) {
        mFramesMap.delete(key);

        // Notify ui.
        Message uiMsg = Message.obtain();
        uiMsg.what = FRAME_REMOVED;
        sendUiUpdate(uiMsg);
    }

    /**
     * Stores frame bitmap in next available slot in frames map.
     * 
     * @param frame
     *            the bitmap to store. Must not be null.
     * @return the key of the stored frame.
     */
    private int storeFrame(Bitmap frame) {
        int key;
        for (key = 0; key < mFramesTotal; key++) {
            if (mFramesMap.get(key) == null) {
                mFramesMap.put(key, frame);
                break;
            }
        }
        return key;
    }

    /**
     * Checks whether the {@link #mFramesMap} has all the frames needed to construct a photo strip.
     * 
     * @return true if the {@link #mFramesMap} is complete; false otherwise.
     */
    private boolean isPhotoStripComplete() {
        boolean isPhotoStripComplete = true;
        for (int key = 0; key < mFramesTotal; key++) {
            if (mFramesMap.get(key) == null) {
                isPhotoStripComplete = false;
                break;
            }
        }
        return isPhotoStripComplete;
    }
}