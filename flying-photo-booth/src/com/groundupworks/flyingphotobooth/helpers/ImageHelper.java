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
package com.groundupworks.flyingphotobooth.helpers;

import java.io.File;
import java.io.OutputStream;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

/**
 * A helper class containing image processing-related methods and configurations.
 * 
 * @author Benedict Lau
 */
public class ImageHelper {

    /**
     * Saved image directory.
     */
    private static final String IMAGE_FOLDER = "/FlyingPhotoBooth";

    /**
     * The prefix for the saved Jpeg filename.
     */
    private static final String JPEG_FILENAME_PREFIX = "fpb";

    /**
     * The Jpeg extension.
     */
    private static final String JPEG_EXTENSION = ".jpg";

    /**
     * Jpeg mime type.
     */
    public static final String JPEG_MIME_TYPE = "image/jpeg";

    /**
     * The default width and height of a image.
     */
    public static final int IMAGE_SIZE = 300;

    /**
     * The default Jpeg quality.
     */
    private static final int JPEG_COMPRESSION = 100;

    /**
     * Error code to indicate an error in the bitmap decoding process.
     */
    private static final int DECODE_ERROR = -1;

    //
    // Public methods.
    //

    /**
     * Gets the path to the writable captured image directory. The directory will be created if it does not exist.
     * 
     * @return the path to the captured image directory; or null if unsuccessful.
     */
    public synchronized static String getCapturedImageDirectory() {
        String directoryPath = null;

        // Check if we currently have read and write access to the external storage.
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Set the path to the default image directory.
            directoryPath = Environment.getExternalStorageDirectory() + IMAGE_FOLDER;
            File directory = new File(directoryPath);

            // Check if directory has already been created.
            if (!directory.exists()) {
                // Create directory if it does not exist.
                if (!directory.mkdir()) {
                    // If directory creation fails, set path to null for return to indicate error.
                    directoryPath = null;
                }
            }
        }

        return directoryPath;
    }

    /**
     * Generates a file name for the captured Jpeg.
     * 
     * @return the automatically generated file name.
     */
    public synchronized static String generateCapturedImageName() {
        return JPEG_FILENAME_PREFIX + Long.toString(System.currentTimeMillis()) + JPEG_EXTENSION;
    }

    /**
     * Compresses a bitmap to Jpeg and write the Jpeg data to an output stream.
     * 
     * @param bitmap
     *            the bitmap to compress.
     * @param outputStream
     *            the outputstream to write the compressed data.
     * @return true if successful; false otherwise.
     */
    public static boolean toJpegOutputStream(Bitmap bitmap, OutputStream outputStream) {
        boolean isSuccessful = false;
        if (bitmap != null) {
            isSuccessful = bitmap.compress(CompressFormat.JPEG, JPEG_COMPRESSION, outputStream);
        }

        return isSuccessful;
    }

    /**
     * Creates a processed bitmap image from Jpeg data in a byte array. Transformations and image filters are applied to
     * the original image in the process.
     * 
     * @param jpegData
     *            byte array of Jpeg data.
     * @param rotation
     *            clockwise rotation applied to image in degrees.
     * @param reflection
     *            horizontal reflection applied to image.
     * @param filter
     *            an optional {@link ImageFilter} to apply. Pass null to disable.
     * @return the image; or null if unsuccessful.
     */
    public static Bitmap createImage(byte[] jpegData, float rotation, boolean reflection, ImageFilter filter) {
        Bitmap returnBitmap = null;

        if (jpegData != null) {
            // Decode Jpeg to create source Bitmap.
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);

            if (decodedBitmap != null && options.outWidth != DECODE_ERROR && options.outHeight != DECODE_ERROR) {
                /*
                 * Scale bitmap.
                 */
                // Determine scale factor.
                float scaleFactor = (float) IMAGE_SIZE / Math.min(options.outWidth, options.outHeight);

                // Create matrix to scale and reflect bitmap.
                float scaleFactorX = scaleFactor;
                float scaleFactorY = scaleFactor;

                if (reflection) {
                    scaleFactorX = -scaleFactorX;
                }

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(scaleFactorX, scaleFactorY);

                // Create new scaled bitmap.
                Bitmap scaledBitmap = Bitmap.createBitmap(decodedBitmap, 0, 0, options.outWidth, options.outHeight,
                        scaleMatrix, true);

                // Recycle old decoded bitmap.
                decodedBitmap.recycle();

                if (scaledBitmap != null) {
                    int scaledBitmapWidth = scaledBitmap.getWidth();
                    int scaledBitmapHeight = scaledBitmap.getHeight();

                    if (scaledBitmapWidth >= IMAGE_SIZE && scaledBitmapHeight >= IMAGE_SIZE) {
                        /*
                         * Crop and rotate bitmap.
                         */
                        // Determine crop region.
                        int cropStartX = (scaledBitmapWidth - IMAGE_SIZE) / 2;
                        int cropStartY = (scaledBitmapHeight - IMAGE_SIZE) / 2;

                        // Create matrix to rotate Bitmap.
                        Matrix rotationMatrix = new Matrix();
                        rotationMatrix.setRotate(rotation);

                        // Create new rotated and cropped bitmap.
                        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, cropStartX, cropStartY, IMAGE_SIZE,
                                IMAGE_SIZE, rotationMatrix, true);

                        if (croppedBitmap != null) {
                            if (filter != null) {
                                returnBitmap = filter.applyFilter(croppedBitmap);

                                // Recycle old cropped bitmap.
                                croppedBitmap.recycle();
                            } else {
                                returnBitmap = croppedBitmap;
                            }
                        }
                    }

                    // Recycle old scaled bitmap.
                    scaledBitmap.recycle();
                }
            }
        }

        return returnBitmap;
    }

    /**
     * Creates a photo strip consisting of an array of bitmaps. The bitmaps must be identical in size.
     * 
     * @param bitmaps
     *            the array of bitmaps to join into one photo strip.
     * @param arrangement
     *            the arrangement of the bitmaps.
     * @return the photo strip as a single bitmap.
     */
    public static Bitmap createPhotoStrip(Bitmap[] bitmaps, Arrangement arrangement) {
        Bitmap returnBitmap = null;
        if (bitmaps != null && bitmaps.length > 0) {
            returnBitmap = arrangement.createPhotoStrip(bitmaps);
        }

        return returnBitmap;
    }

    //
    // Public interfaces.
    //

    /**
     * An image filter to be applied to a bitmap.
     */
    public interface ImageFilter {

        /**
         * Applies filter to the source bitmap.
         * 
         * @param srcBitmap
         *            the source bitmap. Must not be null.
         * @return the filtered bitmap; or null if unsuccessful.
         */
        public Bitmap applyFilter(Bitmap srcBitmap);
    }

    /**
     * An arrangement of bitmaps to create a photo strip. Only supports exactly four bitmaps of the same size.
     */
    public interface Arrangement {

        /**
         * Creates a photo strip.
         * 
         * @param srcBitmaps
         *            the array of bitmaps to join into one photo strip. Must not be null.
         * @return the photo strip; or null if unsuccessful.
         */
        public Bitmap createPhotoStrip(Bitmap[] srcBitmaps);
    }
}
