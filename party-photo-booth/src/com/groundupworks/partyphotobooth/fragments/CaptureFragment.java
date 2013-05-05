/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.groundupworks.lib.photobooth.helpers.CameraHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.views.CenteredPreview;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.kiosk.KioskActivity;

/**
 * Ui for the camera preview and capture screen.
 * 
 * @author Benedict Lau
 */
public class CaptureFragment extends Fragment {

    /**
     * Manual trigger mode.
     */
    private static final int TRIGGER_MODE_MANUAL = 0;

    /**
     * Countdown trigger mode.
     */
    private static final int TRIGGER_MODE_COUNTDOWN = 1;

    /**
     * Burst trigger mode.
     */
    private static final int TRIGGER_MODE_BURST = 2;

    /**
     * Invalid camera id.
     */
    private static final int INVALID_CAMERA_ID = -1;

    /**
     * Duration to display the review overlay.
     */
    private static final int REVIEW_OVERLAY_WAIT_DURATION = 2000;

    /**
     * Duration to display the review overlay in burst mode.
     */
    private static final int REVIEW_OVERLAY_WAIT_DURATION_BURST = 1000;

    /**
     * Threshold distance to recognize a swipe to remove gesture.
     */
    private static final float REVIEW_REMOVE_GESTURE_THRESHOLD = 100f;

    /**
     * The default captured Jpeg quality.
     */
    private static final int CAPTURED_JPEG_QUALITY = 100;

    /**
     * Flag to indicate whether the fragment is launched with preference to use the front-facing camera.
     */
    private boolean mUseFrontFacing = false;

    /**
     * The selected trigger mode.
     */
    private int mTriggerMode = TRIGGER_MODE_MANUAL;

    /**
     * Flag to track whether {@link #onPause()} is called.
     */
    private boolean mOnPauseCalled = false;

    /**
     * Number of cameras on the device.
     */
    private int mNumCameras = 0;

    /**
     * Id of the selected camera.
     */
    private int mCameraId = INVALID_CAMERA_ID;

    /**
     * Timer for scheduling tasks.
     */
    private Timer mTimer = null;

    /**
     * The selected camera.
     */
    private Camera mCamera = null;

    /**
     * The preview display orientation.
     */
    private int mPreviewDisplayOrientation = CameraHelper.CAMERA_SCREEN_ORIENTATION_0;

    /**
     * Flag to indicate whether the camera image is reflected.
     */
    private boolean mIsReflected = false;

    /**
     * The total number of frames to capture.
     */
    private int mFramesTotal = 0;

    /**
     * The current frame index.
     */
    private int mFrameIndex = 0;

    /**
     * Jpeg frames in byte arrays. The first index is the frame count.
     */
    private byte[][] mFramesData = null;

    //
    // Views.
    //

    private CenteredPreview mPreview;

    private ImageButton mStartButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Get params.
         */
        // TODO Read from SharedPreferences.
        // mUseFrontFacing = args.getBoolean(FRAGMENT_BUNDLE_KEY_CAMERA);

        /*
         * Default to first camera available.
         */
        mNumCameras = Camera.getNumberOfCameras();
        if (mNumCameras > 0) {
            mCameraId = 0;
        }

        /*
         * Try to select camera based on preference.
         */
        int cameraPreference = CameraInfo.CAMERA_FACING_BACK;
        if (mUseFrontFacing) {
            cameraPreference = CameraInfo.CAMERA_FACING_FRONT;
        }

        CameraInfo cameraInfo = new CameraInfo();
        for (int cameraId = 0; cameraId < mNumCameras; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);

            // Set flag to indicate whether the camera image is reflected.
            mIsReflected = isCameraImageReflected(cameraInfo);

            // Break on finding the preferred camera.
            if (cameraInfo.facing == cameraPreference) {
                mCameraId = cameraId;
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        mPreview = (CenteredPreview) view.findViewById(R.id.camera_preview);
        mStartButton = (ImageButton) view.findViewById(R.id.capture_button);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final KioskActivity activity = (KioskActivity) getActivity();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        /*
         * Reset frames.
         */
        String numPhotosPref = preferences.getString(getString(R.string.pref__number_of_photos_key),
                getString(R.string.pref__number_of_photos_default));
        mFramesTotal = Integer.parseInt(numPhotosPref);
        mFrameIndex = 0;
        mFramesData = new byte[mFramesTotal][];

        /*
         * Functionalize views.
         */
        // Get trigger mode preference.
        String triggerPref = preferences.getString(getString(R.string.pref__trigger_key),
                getString(R.string.pref__trigger_default));
        if (triggerPref.equals(getString(R.string.pref__trigger_countdown))) {
            mTriggerMode = TRIGGER_MODE_COUNTDOWN;
        } else if (triggerPref.equals(getString(R.string.pref__trigger_burst))) {
            mTriggerMode = TRIGGER_MODE_BURST;
        } else {
            mTriggerMode = TRIGGER_MODE_MANUAL;
        }

        // Configure start button behaviour.
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mStartButton.setEnabled(false);
                    mStartButton.setVisibility(View.INVISIBLE);

                    // TODO Kick off capture sequence.
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
         * Initialize timer for scheduling tasks.
         */
        mTimer = new Timer("countdownTimer");

        /*
         * Reload the fragment if resuming from onPause().
         */
        KioskActivity activity = (KioskActivity) getActivity();
        if (mOnPauseCalled) {
            // Relaunch fragment with new camera.
            // activity.replaceFragment(CaptureFragment.newInstance(), false, true);
        } else {
            if (mCameraId != INVALID_CAMERA_ID) {
                try {
                    mCamera = Camera.open(mCameraId);

                    /*
                     * Configure camera parameters.
                     */
                    Parameters params = mCamera.getParameters();

                    // Set auto white balance if supported.
                    List<String> whiteBalances = params.getSupportedWhiteBalance();
                    if (whiteBalances != null) {
                        for (String whiteBalance : whiteBalances) {
                            if (whiteBalance.equals(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                                params.setWhiteBalance(whiteBalance);
                            }
                        }
                    }

                    // Set auto antibanding if supported.
                    List<String> antibandings = params.getSupportedAntibanding();
                    if (antibandings != null) {
                        for (String antibanding : antibandings) {
                            if (antibanding.equals(Camera.Parameters.ANTIBANDING_AUTO)) {
                                params.setAntibanding(antibanding);
                            }
                        }
                    }

                    // Set macro focus mode if supported.
                    List<String> focusModes = params.getSupportedFocusModes();
                    if (focusModes != null) {
                        for (String focusMode : focusModes) {
                            if (focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                params.setFocusMode(focusMode);
                            }
                        }
                    }

                    // Set quality for Jpeg capture.
                    params.setJpegQuality(CAPTURED_JPEG_QUALITY);

                    // Set optimal size for Jpeg capture.
                    Size pictureSize = CameraHelper.getOptimalPictureSize(params.getSupportedPreviewSizes(),
                            params.getSupportedPictureSizes(), ImageHelper.IMAGE_SIZE, ImageHelper.IMAGE_SIZE);
                    params.setPictureSize(pictureSize.width, pictureSize.height);

                    mCamera.setParameters(params);

                    /*
                     * Setup preview.
                     */
                    mPreviewDisplayOrientation = CameraHelper.getCameraScreenOrientation(activity, mCameraId);
                    mCamera.setDisplayOrientation(mPreviewDisplayOrientation);
                    mPreview.setCamera(mCamera, pictureSize.width, pictureSize.height, mPreviewDisplayOrientation);
                } catch (RuntimeException e) {
                    String title = getString(R.string.capture__error_camera_dialog_title);
                    String message = getString(R.string.capture__error_camera_dialog_message_in_use);
                    activity.showDialogFragment(ErrorDialogFragment.newInstance(title, message));
                }
            } else {
                String title = getString(R.string.capture__error_camera_dialog_title);
                String message = getString(R.string.capture__error_camera_dialog_message_none);
                activity.showDialogFragment(ErrorDialogFragment.newInstance(title, message));
            }
        }
    }

    @Override
    public void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mCamera != null) {
            mPreview.setCamera(null, 0, 0, CameraHelper.CAMERA_SCREEN_ORIENTATION_0);
            mCamera.release();
            mCamera = null;
        }

        mOnPauseCalled = true;

        // Save the camera preference.
        saveCameraPreference(getActivity());

        super.onPause();
    }

    //
    // Private inner classes.
    //

    /**
     * Callback when focus is ready for capture.
     */
    private class MyAutoFocusCallback implements AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // Capture frame.
            if (mTriggerMode == TRIGGER_MODE_MANUAL) {
                takePicture();
            }
        }
    }

    /**
     * Callback when captured Jpeg is ready.
     */
    private class JpegPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (isActivityAlive()) {
                // Save Jpeg frame in memory.
                mFramesData[mFrameIndex] = data;

                // Setup review overlay for user to review captured frame.
                // TODO Add captured image to review view.
                // mReviewStatus.setText(getString(R.string.capture__review_instructions));
                // mReviewStatus.setTextColor(getResources().getColor(R.color.text_color));
                // Bitmap bitmap = ImageHelper.createImage(data, mPreviewDisplayOrientation, mIsReflected, null);
                // mReviewImage.setImageBitmap(bitmap);

                // Setup task to clear the review overlay after a frame removal event or after timeout.
                final int timeout;
                if (mTriggerMode == TRIGGER_MODE_BURST) {
                    timeout = REVIEW_OVERLAY_WAIT_DURATION_BURST;
                } else {
                    timeout = REVIEW_OVERLAY_WAIT_DURATION;
                }

                final CountDownLatch latch = new CountDownLatch(1);
                if (mTimer != null) {
                    mTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            try {
                                // Wait for user input or a fixed timeout.
                                latch.await(timeout, TimeUnit.MILLISECONDS);

                                // Post task to ui thread to prepare for next capture.
                                final Activity activity = getActivity();
                                if (activity != null && !activity.isFinishing()) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            prepareNextCapture();
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                                // Do nothing.
                            }
                        }
                    }, 0);
                }

                // TODO Review view.
                // mReviewOverlay.setOnTouchListener(new ReviewOverlayOnTouchListener(latch));
                // mReviewOverlay.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * {@link OnTouchListener} for the review overlay. Removes the last captured frame when a remove gesture is
     * detected.
     */
    private class ReviewOverlayOnTouchListener implements OnTouchListener {

        private CountDownLatch mReviewLatch;

        private boolean isEnabled = true;

        private float mDownX = -1f;

        private float mDownY = -1f;

        /**
         * Constructor.
         * 
         * @param latch
         *            the latch to countdown as a signal to proceed with capture sequence.
         */
        private ReviewOverlayOnTouchListener(CountDownLatch latch) {
            mReviewLatch = latch;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isEnabled) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // Set anchor.
                    mDownX = event.getX();
                    mDownY = event.getY();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (mDownX == -1f && mDownY == -1f) {
                        // Set anchor.
                        mDownX = event.getX();
                        mDownY = event.getY();
                    } else {
                        // Check swipe distance for discard gesture recognition.
                        float distance = Math.abs(event.getX() - mDownX) + Math.abs(event.getY() - mDownY);
                        if (distance > REVIEW_REMOVE_GESTURE_THRESHOLD) {
                            // Disable listener.
                            isEnabled = false;

                            // Remove frame by decrementing index.
                            mFrameIndex--;

                            // Indicate removed status.
                            // TODO Review view.
                            // mReviewStatus.setText(getString(R.string.capture__review_discarded));
                            // mReviewStatus.setTextColor(getResources().getColor(R.color.selection_color));

                            // Notify latch to proceed with capture sequence.
                            mReviewLatch.countDown();
                        }
                    }
                }
            }

            // Handle all touch events.
            return true;
        }
    }

    //
    // Private methods.
    //

    /**
     * Checks whether the {@link Activity} is attached and not finishing. This should be used as a validation check in a
     * runnable posted to the ui thread, and the {@link Activity} may be have detached by the time the runnable
     * executes. This method should be called on the ui thread.
     * 
     * @return true if {@link Activity} is still alive; false otherwise.
     */
    private boolean isActivityAlive() {
        Activity activity = getActivity();
        return activity != null && !activity.isFinishing();
    }

    /**
     * Takes picture.
     */
    private void takePicture() {
        if (isActivityAlive() && mCamera != null) {
            try {
                mCamera.takePicture(null, null, new JpegPictureCallback());
            } catch (RuntimeException e) {
                // The native camera crashes occasionally. Self-recover by relaunching fragment.
                final KioskActivity activity = (KioskActivity) getActivity();
                Toast.makeText(activity, getString(R.string.capture__error_camera_crash), Toast.LENGTH_SHORT).show();
                // activity.replaceFragment(CaptureFragment.newInstance(), false, true);
            }
        }
    }

    /**
     * Checks whether the camera image is reflected.
     * 
     * @return true if the camera image is reflected; false otherwise.
     */
    private boolean isCameraImageReflected(CameraInfo cameraInfo) {
        return cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * Prepares for the next capture or jump to next fragment if all frames have been collected.
     */
    private void prepareNextCapture() {
        if (isActivityAlive()) {
            // Increment frame index.
            mFrameIndex++;

            // Check if we need more frames.
            if (mFrameIndex < mFramesTotal) {
                // TODO Update title.
                // mTitle.setText(String.format(getString(R.string.capture__title_frame), mFrameIndex + 1,
                // mFramesTotal));

                // Restart preview.
                if (mCamera != null && mPreview != null) {
                    mPreview.start();
                }
            } else {
                // Transition to next fragment since enough frames have been captured.
                nextFragment();
            }
        }
    }

    /**
     * Launches the next {@link Fragment}.
     */
    private void nextFragment() {
        // TODO Restart capture.
    }

    /**
     * Saves the current camera preference.
     * 
     * @param context
     *            the {@link Context}.
     */
    private void saveCameraPreference(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putBoolean(getString(R.string.pref__camera_key), mUseFrontFacing).apply();
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
