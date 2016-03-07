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
package com.groundupworks.lib.photobooth.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.groundupworks.lib.photobooth.helpers.CameraHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;

import java.io.IOException;
import java.util.List;

/**
 * Layout containing a centered preview resized to fit inside the layout while preserving the aspect ratio. The view
 * applies a mask to reveal only the center square region of the preview. The view also configures the camera, set
 * through {@link #setCamera(Camera, int)}, to use the optimal supported preview size.
 *
 * @author Benedict Lau
 */
public class CenteredPreview extends ViewGroup implements SurfaceHolder.Callback {

    private static final String TAG = CenteredPreview.class.getSimpleName();

    /**
     * The preview surface dimensions must be an integer multiple of this factor, otherwise we may get a blank line at
     * one of the edges.
     */
    private static final int PREVIEW_SIZE_BLOCK = 8;

    /**
     * Default color of the crop masks.
     */
    private static final int DEFAULT_MASK_COLOR = Color.BLACK;

    /**
     * Flag to indicate whether the surface is ready for preview to start.
     */
    private boolean mSurfaceReady = false;

    /**
     * The preview display orientation. Valid values are {@link #PREVIEW_DISPLAY_ORIENTATION_0},
     * {@link #PREVIEW_DISPLAY_ORIENTATION_90}, {@link #PREVIEW_DISPLAY_ORIENTATION_180}, and
     * {@link #PREVIEW_DISPLAY_ORIENTATION_270}.
     */
    private int mPreviewDisplayOrientation = CameraHelper.CAMERA_SCREEN_ORIENTATION_0;

    /**
     * The list of preview sizes supported by the camera.
     */
    private List<Size> mSupportedPreviewSizes = null;

    /**
     * The selected preview size.
     */
    private Size mPreviewSize = null;

    /**
     * The camera to fill the preview surface.
     */
    private Camera mCamera = null;

    /**
     * The width of the picture the camera is configured to capture.
     */
    private int mPictureWidth = 0;

    /**
     * The height of the picture the camera is configured to capture.
     */
    private int mPictureHeight = 0;

    /**
     * The surface to draw the camera preview.
     */
    private SurfaceView mSurfaceView = null;

    /**
     * The listener for the preview state changes.
     */
    private CenteredPreview.OnPreviewListener mPreviewListener = null;

    //
    // Masks to indicate crop region.
    //

    private View mTopOrLeftMask;

    private View mBottomOrRightMask;

    /**
     * Constructor.
     *
     * @param context the {@link Context} the view is running in, through which it can access the current theme, resources,
     *                etc.
     */
    public CenteredPreview(Context context) {
        super(context);
    }

    /**
     * Constructor.
     *
     * @param context the {@link Context} the view is running in, through which it can access the current theme, resources,
     *                etc.
     * @param attrs   the attributes of the XML tag that is inflating the view.
     */
    public CenteredPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor.
     *
     * @param context  the {@link Context} the view is running in, through which it can access the current theme, resources,
     *                 etc.
     * @param attrs    the attributes of the XML tag that is inflating the view.
     * @param defStyle the default style to apply to this view. If 0, no style will be applied (beyond what is included in
     *                 the theme). This may either be an attribute resource, whose value will be retrieved from the current
     *                 theme, or an explicit style resource.
     */
    public CenteredPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Ignore child measurements and calculate layout size.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        // Set layout size.
        setMeasuredDimension(width, height);

        if (mCamera != null && mSurfaceView != null && mSupportedPreviewSizes != null && mPictureWidth > 0
                && mPictureHeight > 0) {
            // Calculate the optimal supported preview size based on the picture size.
            mPreviewSize = CameraHelper.getOptimalPreviewSize(mSupportedPreviewSizes, mPictureWidth, mPictureHeight);
            if (mPreviewSize != null) {
                // Configure the camera to output in the optimal calculated size.
                Camera.Parameters params = mCamera.getParameters();
                params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
                mCamera.setParameters(params);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mSurfaceView != null) {
            // Calculate the maximum dimensions that the preview can take.
            final int parentWidth = right - left;
            final int parentHeight = bottom - top;

            // Calculate the preview dimensions after any applicable rotations.
            int previewWidth = parentWidth;
            int previewHeight = parentHeight;
            if (mPreviewSize != null) {
                if (mPreviewDisplayOrientation == CameraHelper.CAMERA_SCREEN_ORIENTATION_0
                        || mPreviewDisplayOrientation == CameraHelper.CAMERA_SCREEN_ORIENTATION_180) {
                    previewWidth = mPreviewSize.width;
                    previewHeight = mPreviewSize.height;
                } else if (mPreviewDisplayOrientation == CameraHelper.CAMERA_SCREEN_ORIENTATION_90
                        || mPreviewDisplayOrientation == CameraHelper.CAMERA_SCREEN_ORIENTATION_270) {
                    previewWidth = mPreviewSize.height;
                    previewHeight = mPreviewSize.width;
                } else {
                    throw new IllegalArgumentException("Invalid value specified for preview display orientation");
                }
            }

            /*
             * Layout children views.
             */
            Point previewSurfaceSize = ImageHelper.getAspectFitSize(parentWidth, parentHeight, previewWidth,
                    previewHeight);
            int previewSurfaceWidth = previewSurfaceSize.x;
            int previewSurfaceHeight = previewSurfaceSize.y;

            // Ensure preview dimensions are multiples of a preview size factor.
            previewSurfaceWidth -= previewSurfaceWidth % PREVIEW_SIZE_BLOCK;
            previewSurfaceHeight -= previewSurfaceHeight % PREVIEW_SIZE_BLOCK;

            // Center the preview surface within the parent container.
            mSurfaceView.layout((parentWidth - previewSurfaceWidth) / 2, (parentHeight - previewSurfaceHeight) / 2,
                    (parentWidth + previewSurfaceWidth) / 2, (parentHeight + previewSurfaceHeight) / 2);

            // Set masks to show only a square region at the center.
            if (previewSurfaceWidth > previewSurfaceHeight) {
                // Mask left and right edges.
                mTopOrLeftMask.layout(0, 0, (parentWidth - previewSurfaceHeight) / 2, parentHeight);
                mBottomOrRightMask.layout((parentWidth + previewSurfaceHeight) / 2, 0, parentWidth, parentHeight);
            } else if (previewSurfaceWidth < previewSurfaceHeight) {
                // Mask top and bottom edges.
                mTopOrLeftMask.layout(0, 0, parentWidth, (parentHeight - previewSurfaceWidth) / 2);
                mBottomOrRightMask.layout(0, (parentHeight + previewSurfaceWidth) / 2, parentWidth, parentHeight);
            } else {
                // Do not layout mask.
            }
        }
    }

    //
    // SurfaceHolder.Callback implementation.
    //

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Do nothing.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);

                // Set flag to indicate the surface is created and attached to the camera for preview to start.
                mSurfaceReady = true;

                // Start preview.
                restart();
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
            } catch (RuntimeException exception) {
                Log.e(TAG, "RuntimeException caused by startPreview()", exception);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();

            // Notify preview stop if a listener is set.
            final OnPreviewListener listener = mPreviewListener;
            if (listener != null) {
                listener.onStopped();
            }
        }

        mSurfaceReady = false;
    }

    //
    // Private methods.
    //

    /**
     * Initializes the layout by adding a preview surface and the masking views to indicate a crop region.
     *
     * @param context the Context.
     */
    private void initViews(Context context) {
        /*
         * Init preview surface.
         */
        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Set callbacks to get notifications about surface events.
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /*
         * Init masks.
         */
        Drawable maskColor = getBackground();
        if (maskColor == null) {
            maskColor = new ColorDrawable(DEFAULT_MASK_COLOR);
        }

        mTopOrLeftMask = new View(context);
        mTopOrLeftMask.setBackgroundDrawable(maskColor);
        addView(mTopOrLeftMask);

        mBottomOrRightMask = new View(context);
        mBottomOrRightMask.setBackgroundDrawable(maskColor);
        addView(mBottomOrRightMask);
    }

    //
    // Public methods.
    //

    /**
     * Sets the listener for the preview state changes.
     *
     * @param listener the listener.
     */
    public void setOnPreviewListener(OnPreviewListener listener) {
        mPreviewListener = listener;
    }

    /**
     * Starts preview with the selected {@link Camera}. The client is responsible for locking the camera, and calling
     * {@link CenteredPreview#stop()} before releasing the lock.
     *
     * @param camera                    the Camera to use for preview.
     * @param pictureWidth              the width of the picture the camera is configured to capture.
     * @param pictureHeight             the height of the picture the camera is configured to capture.
     * @param previewDisplayOrientation the display orientation of the preview. Valid values are {@link #PREVIEW_DISPLAY_ORIENTATION_0},
     *                                  {@link #PREVIEW_DISPLAY_ORIENTATION_90}, {@link #PREVIEW_DISPLAY_ORIENTATION_180}, and
     *                                  {@link #PREVIEW_DISPLAY_ORIENTATION_270}.
     */
    public void start(Camera camera, int pictureWidth, int pictureHeight, int previewDisplayOrientation) {
        mCamera = camera;
        mPictureWidth = pictureWidth;
        mPictureHeight = pictureHeight;
        mPreviewDisplayOrientation = previewDisplayOrientation;

        if (camera != null) {
            mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();

            // Initialize the layout and preview surface.
            initViews(getContext());
        }
    }

    /**
     * Restarts the preview.
     *
     * @throws RuntimeException an exception thrown by the native method {@link Camera#startPreview()}.
     */
    public void restart() throws RuntimeException {
        if (mCamera != null && mSurfaceReady) {
            mCamera.startPreview();

            // Notify preview start if a listener is set.
            final OnPreviewListener listener = mPreviewListener;
            if (listener != null) {
                listener.onStarted();
            }
        }
    }

    /**
     * Stops the preview. This should be called before releasing the lock on the {@link Camera}.
     */
    public void stop() {
        mCamera = null;
        mPictureWidth = 0;
        mPictureHeight = 0;
        mPreviewDisplayOrientation = CameraHelper.CAMERA_SCREEN_ORIENTATION_0;
    }


    //
    // Interfaces.
    //

    /**
     * Listener to communicate state changes of the preview.
     */
    public interface OnPreviewListener {

        /**
         * Called when preview is started by this {@link #CenteredPreview(android.content.Context)}.
         */
        void onStarted();

        /**
         * Called when preview is stopped by this {@link #CenteredPreview(android.content.Context)}.
         */
        void onStopped();
    }
}
