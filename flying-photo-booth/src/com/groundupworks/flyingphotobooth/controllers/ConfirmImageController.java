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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import com.groundupworks.flyingphotobooth.fragments.ConfirmImageFragment;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper.ImageFilter;
import com.jabistudio.androidjhlabs.filter.GrayscaleFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

/**
 * Controller class for the {@link ConfirmImageFragment}.
 * 
 * @author Benedict Lau
 */
public class ConfirmImageController extends BaseController {

    //
    // Controller events. The ui should be notified of these events.
    //

    public static final int ERROR_OCCURRED = -1;

    public static final int BITMAP_READY = 0;

    public static final int JPEG_SAVED = 1;

    private Bitmap mBitmap = null;

    //
    // BaseController implementation.
    //

    @Override
    protected void handleEvent(Message msg) {
        switch (msg.what) {
            case ConfirmImageFragment.IMAGE_VIEW_READY:
                /*
                 * Create an image bitmap from Jpeg data.
                 */
                Bundle bundle = msg.getData();
                byte[] jpegData0 = bundle.getByteArray(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_0);
                byte[] jpegData1 = bundle.getByteArray(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_1);
                byte[] jpegData2 = bundle.getByteArray(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_2);
                byte[] jpegData3 = bundle.getByteArray(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_JPEG_DATA_3);
                float rotation = bundle.getFloat(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_ROTATION);
                boolean reflection = bundle.getBoolean(ConfirmImageFragment.MESSAGE_BUNDLE_KEY_REFLECTION);

                // Do the image processing.
                Bitmap[] bitmaps = new Bitmap[4];
                bitmaps[0] = ImageHelper.createImage(jpegData0, rotation, reflection, new MyImageFilter());
                bitmaps[1] = ImageHelper.createImage(jpegData1, rotation, reflection, null);
                bitmaps[2] = ImageHelper.createImage(jpegData2, rotation, reflection, new MyImageFilter());
                bitmaps[3] = ImageHelper.createImage(jpegData3, rotation, reflection, null);

                mBitmap = ImageHelper.createPhotoStrip(bitmaps);

                // Recycle original bitmaps.
                for (Bitmap bitmap : bitmaps) {
                    bitmap.recycle();
                }
                bitmaps = null;

                // Notify ui.
                if (mBitmap != null) {
                    // Bitmap is ready.
                    Message uiMsg = Message.obtain();
                    uiMsg.what = BITMAP_READY;
                    uiMsg.obj = mBitmap;
                    sendUiUpdate(uiMsg);
                } else {
                    // An error has occurred.
                    reportError();
                }
                break;
            case ConfirmImageFragment.IMAGE_CONFIRMED:
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
            case ConfirmImageFragment.FRAGMENT_DESTROYED:
                /*
                 * Recycle bitmap.
                 */
                if (mBitmap != null) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
                break;
            default:
                break;
        }
    }

    //
    // Private inner classes.
    //

    /**
     * A series of image filters to apply to the original image.
     */
    private class MyImageFilter implements ImageFilter {

        @Override
        public Bitmap applyFilter(Bitmap srcBitmap) {
            Bitmap filteredBitmap = null;

            final int width = srcBitmap.getWidth();
            final int height = srcBitmap.getHeight();
            int[] colors = AndroidUtils.bitmapToIntArray(srcBitmap);

            /*
             * Apply image filters.
             */
            GrayscaleFilter f = new GrayscaleFilter();
            colors = f.filter(colors, width, height);

            // EdgeFilter edgeFilter = new EdgeFilter();
            // colors = edgeFilter.filter(colors, width, height);
            //
            // ThresholdFilter thresholdFilter = new ThresholdFilter();
            // thresholdFilter.setDimensions(width, height);
            // thresholdFilter.setLowerThreshold(35);
            // thresholdFilter.setUpperThreshold(0);
            // thresholdFilter.setWhite(Color.BLACK);
            // thresholdFilter.setBlack(Color.WHITE);
            // colors = thresholdFilter.filter(colors, width, height);
            //
            // MedianFilter medianFilter = new MedianFilter();
            // colors = medianFilter.filter(colors, width, height);

            filteredBitmap = Bitmap.createBitmap(colors, 0, width, width, height, Bitmap.Config.ARGB_8888);

            return filteredBitmap;
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
