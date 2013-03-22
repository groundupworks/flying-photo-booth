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
package com.groundupworks.lib.photobooth.helpers;

import java.util.List;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.view.Surface;

/**
 * A helper class containing methods for configuring the camera.
 * 
 * @author Benedict Lau
 */
public class CameraHelper {

    //
    // Valid values of preview display orientation angles.
    //

    public static final int CAMERA_SCREEN_ORIENTATION_0 = 0;

    public static final int CAMERA_SCREEN_ORIENTATION_90 = 90;

    public static final int CAMERA_SCREEN_ORIENTATION_180 = 180;

    public static final int CAMERA_SCREEN_ORIENTATION_270 = 270;

    //
    // Screen rotation values.
    //

    private static final int SCREEN_ROTATION_0 = 0;

    private static final int SCREEN_ROTATION_90 = 90;

    private static final int SCREEN_ROTATION_180 = 180;

    private static final int SCREEN_ROTATION_270 = 270;

    /**
     * The aspect ratio tolerance used to select the optimal preview size.
     */
    private static final double PREVIEW_ASPECT_RATIO_TOLERANCE = 0.1d;

    /**
     * The aspect ratio tolerance used to select the optimal picture size.
     */
    private static final double PICTURE_ASPECT_RATIO_TOLERANCE = 0.4d;

    //
    // Private methods.
    //

    /**
     * Selects the optimal picture size with flags to toggle filtering criteria.
     * 
     * @param previewSizes
     *            the list of supported preview sizes.
     * @param pictureSizes
     *            the list of supported picture sizes.
     * @param targetWidth
     *            the target picture width.
     * @param targetHeight
     *            the target picture height.
     * @param filterMinSize
     *            true to block sizes smaller than target dimensions; false otherwise.
     * @param filterAspectRatio
     *            true to block sizes above the aspect ratio tolerance; false otherwise.
     * @param filterPreviewSizes
     *            true to block sizes without a corresponding preview size; false otherwise.
     * @return the optimal supported picture size; or null if failed.
     */
    private static Size selectPictureSize(List<Size> previewSizes, List<Size> pictureSizes, final int targetWidth,
            final int targetHeight, boolean filterMinSize, boolean filterAspectRatio, boolean filterPreviewSizes) {
        Size optimalSize = null;

        double targetAspectRatio = (double) targetWidth / targetHeight;
        int minArea = Integer.MAX_VALUE;
        for (Size size : pictureSizes) {
            // Block sizes smaller than target dimensions.
            if (filterMinSize && (size.width < targetWidth || size.height < targetHeight)) {
                continue;
            }

            // Block sizes above the aspect ratio tolerance.
            double aspectRatio = (double) size.width / size.height;
            if (filterAspectRatio && (Math.abs(aspectRatio - targetAspectRatio) > PICTURE_ASPECT_RATIO_TOLERANCE)) {
                continue;
            }

            // Block sizes without a corresponding preview size.
            if (filterPreviewSizes && !previewSizes.contains(size)) {
                continue;
            }

            // Select smallest size.
            int area = size.width * size.height;
            if (area < minArea) {
                optimalSize = size;
                minArea = area;
            }
        }

        return optimalSize;
    }

    //
    // Public methods.
    //

    /**
     * Calculates the clockwise rotation applied to the camera such that the picture will be aligned with the screen
     * orientation.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param cameraId
     *            id of the camera.
     * @return the clockwise rotation in degrees.
     */
    public static int getCameraScreenOrientation(Activity activity, int cameraId) {
        int cameraScreenOrientation = CAMERA_SCREEN_ORIENTATION_0;

        // Get camera info.
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // Get screen orientation.
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = SCREEN_ROTATION_0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = SCREEN_ROTATION_0;
                break;
            case Surface.ROTATION_90:
                degrees = SCREEN_ROTATION_90;
                break;
            case Surface.ROTATION_180:
                degrees = SCREEN_ROTATION_180;
                break;
            case Surface.ROTATION_270:
                degrees = SCREEN_ROTATION_270;
                break;
        }

        /*
         * Calculate result based on camera and screen orientation.
         */
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Calculate relative rotation between camera and screen.
            cameraScreenOrientation = (info.orientation + degrees) % 360;

            // Account for mirroring.
            cameraScreenOrientation = (360 - cameraScreenOrientation) % 360;
        } else {
            // Calculate relative rotation between camera and screen.
            cameraScreenOrientation = (info.orientation - degrees + 360) % 360;
        }

        return cameraScreenOrientation;
    }

    /**
     * Selects the optimal preview size based on the target aspect ratio and size.
     * 
     * @param previewSizes
     *            the list of supported preview sizes.
     * @param targetWidth
     *            the target preview width.
     * @param targetHeight
     *            the target preview height.
     * @return the optimal supported preview size; or null if an empty list is passed.
     */
    public static Size getOptimalPreviewSize(List<Size> previewSizes, int targetWidth, int targetHeight) {
        // Check for null or empty list.
        if (previewSizes == null || previewSizes.isEmpty()) {
            return null;
        }

        Size optimalSize = null;

        double targetAspectRatio = (double) targetWidth / targetHeight;
        int minDiff = Integer.MAX_VALUE;

        /*
         * Try to match aspect ratio and size.
         */
        for (Size size : previewSizes) {
            // Block aspect ratios that do not match.
            double aspectRatio = (double) size.width / size.height;
            if (Math.abs(aspectRatio - targetAspectRatio) > PREVIEW_ASPECT_RATIO_TOLERANCE) {
                continue;
            }

            int diff = Math.max(Math.abs(size.width - targetWidth), Math.abs(size.height - targetHeight));
            if (diff < minDiff) {
                optimalSize = size;
                minDiff = diff;
            }
        }

        /*
         * Try to match size.
         */
        if (optimalSize == null) {
            minDiff = Integer.MAX_VALUE;
            for (Size size : previewSizes) {
                int diff = Math.max(Math.abs(size.width - targetWidth), Math.abs(size.height - targetHeight));
                if (diff < minDiff) {
                    optimalSize = size;
                    minDiff = diff;
                }
            }
        }

        LogsHelper.log(CameraHelper.class, "getOptimalPreviewSize", "size=[" + optimalSize.width + ", "
                + optimalSize.height + "]");

        return optimalSize;
    }

    /**
     * Selects the optimal picture size by selecting the smallest supported size with dimensions larger than the target
     * dimensions. The algorithm also prefers picture sizes with a similar aspect ratio as the target dimensions, and
     * have a corresponding preview size. If the ideal case fails, the largest supported size is returned.
     * 
     * @param previewSizes
     *            the list of supported preview sizes.
     * @param pictureSizes
     *            the list of supported picture sizes.
     * @param targetWidth
     *            the target picture width.
     * @param targetHeight
     *            the target picture height.
     * @return the optimal supported picture size; or null if an empty list is passed.
     */
    public static Size getOptimalPictureSize(List<Size> previewSizes, List<Size> pictureSizes, int targetWidth,
            int targetHeight) {
        // Check for null or empty lists.
        if (previewSizes == null || pictureSizes == null || previewSizes.isEmpty() || pictureSizes.isEmpty()) {
            return null;
        }

        /*
         * Try to select an optimal picture size, gradually relaxing the criteria.
         */
        Size optimalSize = selectPictureSize(previewSizes, pictureSizes, targetWidth, targetHeight, true, true, true);
        if (optimalSize == null) {
            // Ignore preview size criteria.
            optimalSize = selectPictureSize(previewSizes, pictureSizes, targetWidth, targetHeight, true, true, false);
        }
        if (optimalSize == null) {
            // Ignore aspect ratio criteria.
            optimalSize = selectPictureSize(previewSizes, pictureSizes, targetWidth, targetHeight, true, false, true);
        }
        if (optimalSize == null) {
            // Ignore both aspect ratio and preview size criteria.
            optimalSize = selectPictureSize(previewSizes, pictureSizes, targetWidth, targetHeight, true, false, false);
        }

        /*
         * Cannot fulfill the requirements. Select the largest size.
         */
        if (optimalSize == null) {
            int maxArea = Integer.MIN_VALUE;
            for (Size size : pictureSizes) {
                // Select max size.
                int area = size.width * size.height;
                if (area > maxArea) {
                    optimalSize = size;
                    maxArea = area;
                }
            }
        }

        LogsHelper.log(CameraHelper.class, "getOptimalPictureSize", "size=[" + optimalSize.width + ", "
                + optimalSize.height + "]");

        return optimalSize;
    }
}
