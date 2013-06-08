/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;
import com.groundupworks.lib.photobooth.dropbox.DropboxHelper;
import com.groundupworks.lib.photobooth.facebook.FacebookHelper;
import com.groundupworks.lib.photobooth.framework.BaseController;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper.Arrangement;
import com.groundupworks.lib.photobooth.wings.ShareRequest;
import com.groundupworks.lib.photobooth.wings.WingsDbHelper;
import com.groundupworks.lib.photobooth.wings.WingsService;
import com.groundupworks.partyphotobooth.MyApplication;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.arrangements.TitledBoxArrangement;
import com.groundupworks.partyphotobooth.arrangements.TitledHorizontalArrangement;
import com.groundupworks.partyphotobooth.arrangements.TitledVerticalArrangement;
import com.groundupworks.partyphotobooth.fragments.PhotoStripFragment;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoStripArrangement;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

public class PhotoStripController extends BaseController {

    //
    // Controller events. The ui should be notified of these events.
    //

    public static final int ERROR_JPEG_DATA = -1;

    public static final int ERROR_PHOTO_STRIP_SUBMIT = -2;

    public static final int THUMB_BITMAP_READY = 0;

    public static final int FRAME_REMOVED = 1;

    public static final int PHOTO_STRIP_READY = 2;

    public static final int PHOTO_STRIP_SUBMITTED = 3;

    //
    // Message bundle keys.
    //

    public static final String MESSAGE_BUNDLE_KEY_FACEBOOK_SHARED = "facebookShared";

    public static final String MESSAGE_BUNDLE_KEY_DROPBOX_SHARED = "dropboxShared";

    /**
     * The {@link Application} {@link Context}.
     */
    private Context mContext;

    /**
     * The {@link PreferencesHelper}.
     */
    private PreferencesHelper mPreferencesHelper;

    /**
     * The first line of the event title.
     */
    private String mLineOne = null;

    /**
     * The second line of the event title.
     */
    private String mLineTwo = null;

    /**
     * The date of the event.
     */
    private String mDate = null;

    /**
     * The photo strip arrangement.
     */
    private PhotoStripArrangement mArrangementPref;

    /**
     * The total number of frames to capture.
     */
    private int mFramesTotalPref;

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
        mLineOne = mPreferencesHelper.getEventLineOne(mContext);
        mLineTwo = mPreferencesHelper.getEventLineTwo(mContext);

        long date = mPreferencesHelper.getEventDate(mContext);
        if (date != PreferencesHelper.EVENT_DATE_HIDDEN) {
            mDate = TextHelper.getDateString(mContext, date);
        }

        mArrangementPref = mPreferencesHelper.getPhotoStripArrangement(mContext);
        mFramesTotalPref = mPreferencesHelper.getPhotoStripNumPhotos(mContext);
        mFramesMap = new SparseArray<Bitmap>(mFramesTotalPref);

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
            case PhotoStripFragment.PHOTO_STRIP_SUBMIT:
                processPhotoStripSubmission();
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
     * Processes a photo strip submission request and notifies ui.
     */
    private void processPhotoStripSubmission() {
        /*
         * Create photo strip.
         */
        // Select arrangement.
        Arrangement arrangement = null;
        if (PhotoStripArrangement.HORIZONTAL.equals(mArrangementPref)) {
            arrangement = new TitledHorizontalArrangement(mLineOne, mLineTwo, mDate);
        } else if (PhotoStripArrangement.BOX.equals(mArrangementPref)) {
            arrangement = new TitledBoxArrangement(mLineOne, mLineTwo, mDate);
        } else {
            arrangement = new TitledVerticalArrangement(mLineOne, mLineTwo, mDate);
        }

        // Create photo strip as a single bitmap.
        Bitmap[] bitmaps = new Bitmap[mFramesTotalPref];
        for (int i = 0; i < mFramesTotalPref; i++) {
            bitmaps[i] = mFramesMap.get(i);
        }
        Bitmap photoStrip = ImageHelper.createPhotoStrip(bitmaps, arrangement);

        // Clear frames map.
        mFramesMap.clear();

        /*
         * Save photo strip bitmap as Jpeg.
         */
        Context context = MyApplication.getContext();
        try {
            String imageDirectory = ImageHelper.getCapturedImageDirectory(context
                    .getString(R.string.image_helper__image_folder_name));
            if (imageDirectory != null) {
                String imageName = ImageHelper.generateCapturedImageName(context
                        .getString(R.string.image_helper__image_filename_prefix));
                File file = new File(imageDirectory, imageName);
                final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

                // Convert to Jpeg and writes to file.
                boolean isSuccessful = ImageHelper.toJpegOutputStream(photoStrip, outputStream);
                outputStream.flush();
                outputStream.close();

                if (isSuccessful) {
                    String jpegPath = file.getPath();

                    // Request adding Jpeg to Android Gallery.
                    MediaScannerConnection.scanFile(context, new String[] { jpegPath },
                            new String[] { ImageHelper.JPEG_MIME_TYPE }, null);

                    // Share to Facebook.
                    boolean facebookShared = false;
                    FacebookHelper facebookHelper = new FacebookHelper();
                    if (facebookHelper.isLinked(context)) {
                        facebookShared = share(context, jpegPath, ShareRequest.DESTINATION_FACEBOOK);
                    }

                    // Share to Dropbox.
                    boolean dropboxShared = false;
                    DropboxHelper dropboxHelper = new DropboxHelper();
                    if (dropboxHelper.isLinked(context)) {
                        dropboxShared = share(context, jpegPath, ShareRequest.DESTINATION_DROPBOX);
                    }

                    // Notify ui the Jpeg is saved and shared to linked services.
                    Message uiMsg = Message.obtain();
                    uiMsg.what = PHOTO_STRIP_SUBMITTED;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(MESSAGE_BUNDLE_KEY_FACEBOOK_SHARED, facebookShared);
                    bundle.putBoolean(MESSAGE_BUNDLE_KEY_DROPBOX_SHARED, dropboxShared);
                    uiMsg.setData(bundle);
                    sendUiUpdate(uiMsg);
                } else {
                    reportError(ERROR_PHOTO_STRIP_SUBMIT);
                }
            } else {
                // Invalid external storage state or failed directory creation.
                reportError(ERROR_PHOTO_STRIP_SUBMIT);
            }
        } catch (FileNotFoundException e) {
            reportError(ERROR_PHOTO_STRIP_SUBMIT);
        } catch (IOException e) {
            reportError(ERROR_PHOTO_STRIP_SUBMIT);
        }

        /*
         * Recycle photo strip bitmap.
         */
        if (photoStrip != null) {
            photoStrip.recycle();
            photoStrip = null;
        }
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
        for (key = 0; key < mFramesTotalPref; key++) {
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
        for (int key = 0; key < mFramesTotalPref; key++) {
            if (mFramesMap.get(key) == null) {
                isPhotoStripComplete = false;
                break;
            }
        }
        return isPhotoStripComplete;
    }

    /**
     * Shares a photo strip to a sharing service.
     * 
     * @param context
     *            the {@link Context}.
     * @param jpegPath
     *            The path to the Jpeg to share.
     * @param destination
     *            The sharing service to share to.
     * @return true if successful; false otherwise.
     */
    private boolean share(Context context, String jpegPath, int destination) {
        boolean isSuccessful = false;
        if (jpegPath != null && WingsDbHelper.getInstance(context).createShareRequest(jpegPath, destination)) {
            // Start Wings service.
            WingsService.startWakefulService(context);

            isSuccessful = true;
        }
        return isSuccessful;
    }
}