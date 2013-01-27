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
package com.groundupworks.flyingphotobooth.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import com.groundupworks.flyingphotobooth.MyApplication;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.arrangements.BoxArrangement;
import com.groundupworks.flyingphotobooth.arrangements.HorizontalArrangement;
import com.groundupworks.flyingphotobooth.arrangements.VerticalArrangement;
import com.groundupworks.flyingphotobooth.filters.BlackAndWhiteFilter;
import com.groundupworks.flyingphotobooth.filters.LineArtFilter;
import com.groundupworks.flyingphotobooth.filters.SepiaFilter;
import com.groundupworks.flyingphotobooth.fragments.ShareFragment;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.Arrangement;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.ImageFilter;

/**
 * Controller class for the {@link ShareFragment}.
 * 
 * @author Benedict Lau
 */
public class ShareController extends BaseController {

    //
    // Controller events. The ui should be notified of these events.
    //

    public static final int ERROR_OCCURRED = -1;

    public static final int THUMB_READY = 0;

    public static final int JPEG_SAVED = 1;

    private Bitmap mBitmap = null;

    private Bitmap mThumb = null;

    //
    // BaseController implementation.
    //

    @Override
    protected void handleEvent(Message msg) {
        switch (msg.what) {
            case ShareFragment.IMAGE_VIEW_READY:
                final Context context = MyApplication.getContext();

                /*
                 * Create an image bitmap from Jpeg data.
                 */
                Bundle bundle = msg.getData();
                byte[] jpegData0 = bundle.getByteArray(ShareFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_0);
                byte[] jpegData1 = bundle.getByteArray(ShareFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_1);
                byte[] jpegData2 = bundle.getByteArray(ShareFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_2);
                byte[] jpegData3 = bundle.getByteArray(ShareFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_3);
                float rotation = bundle.getFloat(ShareFragment.MESSAGE_BUNDLE_KEY_ROTATION);
                boolean reflection = bundle.getBoolean(ShareFragment.MESSAGE_BUNDLE_KEY_REFLECTION);
                String filterPref = bundle.getString(ShareFragment.MESSAGE_BUNDLE_KEY_FILTER);
                String arrangementPref = bundle.getString(ShareFragment.MESSAGE_BUNDLE_KEY_ARRANGEMENT);
                int thumbSize = bundle.getInt(ShareFragment.MESSAGE_BUNDLE_KEY_THUMB_SIZE);

                // Select filter.
                ImageFilter filter0 = null;
                ImageFilter filter1 = null;
                ImageFilter filter2 = null;
                ImageFilter filter3 = null;
                if (filterPref.equals(context.getString(R.string.pref__filter_bw))) {
                    filter0 = new BlackAndWhiteFilter();
                    filter1 = new BlackAndWhiteFilter();
                    filter2 = new BlackAndWhiteFilter();
                    filter3 = new BlackAndWhiteFilter();
                } else if (filterPref.equals(context.getString(R.string.pref__filter_bw_mixed))) {
                    if (arrangementPref.equals(context.getString(R.string.pref__arrangement_box))) {
                        filter1 = new BlackAndWhiteFilter();
                        filter2 = new BlackAndWhiteFilter();
                    } else {
                        filter1 = new BlackAndWhiteFilter();
                        filter3 = new BlackAndWhiteFilter();
                    }
                } else if (filterPref.equals(context.getString(R.string.pref__filter_sepia))) {
                    filter0 = new SepiaFilter();
                    filter1 = new SepiaFilter();
                    filter2 = new SepiaFilter();
                    filter3 = new SepiaFilter();
                } else if (filterPref.equals(context.getString(R.string.pref__filter_sepia_mixed))) {
                    if (arrangementPref.equals(context.getString(R.string.pref__arrangement_box))) {
                        filter1 = new SepiaFilter();
                        filter2 = new SepiaFilter();
                    } else {
                        filter1 = new SepiaFilter();
                        filter3 = new SepiaFilter();
                    }
                } else if (filterPref.equals(context.getString(R.string.pref__filter_line_art))) {
                    filter0 = new LineArtFilter();
                    filter1 = new LineArtFilter();
                    filter2 = new LineArtFilter();
                    filter3 = new LineArtFilter();
                } else {
                    // No filter. Keep filter as null.
                }

                // Select arrangement.
                Arrangement arrangement = null;
                if (arrangementPref.equals(context.getString(R.string.pref__arrangement_horizontal))) {
                    arrangement = new HorizontalArrangement();
                } else if (arrangementPref.equals(context.getString(R.string.pref__arrangement_box))) {
                    arrangement = new BoxArrangement();
                } else {
                    arrangement = new VerticalArrangement();
                }

                // Do the image processing.
                Bitmap[] bitmaps = new Bitmap[4];
                bitmaps[0] = ImageHelper.createImage(jpegData0, rotation, reflection, filter0);
                bitmaps[1] = ImageHelper.createImage(jpegData1, rotation, reflection, filter1);
                bitmaps[2] = ImageHelper.createImage(jpegData2, rotation, reflection, filter2);
                bitmaps[3] = ImageHelper.createImage(jpegData3, rotation, reflection, filter3);

                mBitmap = ImageHelper.createPhotoStrip(bitmaps, arrangement);

                // Recycle original bitmaps.
                for (Bitmap bitmap : bitmaps) {
                    bitmap.recycle();
                }
                bitmaps = null;

                // Notify ui.
                if (mBitmap != null) {
                    // Calculate thumbnail dimensions based on the arrangement.
                    int thumbWidth = 0;
                    int thumbHeight = 0;
                    if (arrangementPref.equals(context.getString(R.string.pref__arrangement_horizontal))) {
                        thumbHeight = thumbSize;
                        float scalingFactor = ((float) thumbHeight) / (float) (mBitmap.getHeight());
                        thumbWidth = (int) (((float) (mBitmap.getWidth())) * scalingFactor);
                    } else if (arrangementPref.equals(context.getString(R.string.pref__arrangement_box))) {
                        thumbWidth = thumbSize * 2;
                        float scalingFactor = ((float) thumbWidth) / (float) (mBitmap.getWidth());
                        thumbHeight = (int) (((float) (mBitmap.getHeight())) * scalingFactor);
                    } else {
                        thumbWidth = thumbSize;
                        float scalingFactor = ((float) thumbWidth) / (float) (mBitmap.getWidth());
                        thumbHeight = (int) (((float) (mBitmap.getHeight())) * scalingFactor);
                    }

                    // Create thumbnail.
                    mThumb = Bitmap.createScaledBitmap(mBitmap, thumbWidth, thumbHeight, true);
                    if (mThumb != null) {
                        // Thumbnail bitmap is ready.
                        Message uiMsg = Message.obtain();
                        uiMsg.what = THUMB_READY;
                        uiMsg.obj = mThumb;
                        sendUiUpdate(uiMsg);
                    } else {
                        // An error has occurred.
                        reportError();
                    }
                } else {
                    // An error has occurred.
                    reportError();
                }

                /*
                 * Save image bitmap as Jpeg.
                 */
                try {
                    String imageDirectory = ImageHelper.getCapturedImageDirectory();
                    if (imageDirectory != null) {
                        String imageName = ImageHelper.generateCapturedImageName();
                        File file = new File(imageDirectory, imageName);
                        final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

                        // Convert to Jpeg and writes to file.
                        boolean isSuccessful = ImageHelper.toJpegOutputStream(mBitmap, outputStream);
                        outputStream.flush();
                        outputStream.close();

                        if (isSuccessful) {
                            // Notify ui the Jpeg is saved.
                            Message uiMsg = Message.obtain();
                            uiMsg.what = JPEG_SAVED;
                            uiMsg.obj = file.getPath();
                            sendUiUpdate(uiMsg);
                        } else {
                            reportError();
                        }
                    } else {
                        // Invalid external storage state or failed directory creation.
                        reportError();
                    }
                } catch (FileNotFoundException e) {
                    reportError();
                } catch (IOException e) {
                    reportError();
                }
                break;
            case ShareFragment.FRAGMENT_DESTROYED:
                /*
                 * Recycle bitmaps.
                 */
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                if (mThumb != null) {
                    mThumb.recycle();
                    mThumb = null;
                }
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
     */
    private void reportError() {
        Message uiMsg = Message.obtain();
        uiMsg.what = ERROR_OCCURRED;
        sendUiUpdate(uiMsg);
    }
}
