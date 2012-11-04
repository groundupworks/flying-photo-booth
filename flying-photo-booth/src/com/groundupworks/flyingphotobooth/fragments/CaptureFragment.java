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
package com.groundupworks.flyingphotobooth.fragments;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.groundupworks.flyingphotobooth.LaunchActivity;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.helpers.CameraHelper;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;
import com.groundupworks.flyingphotobooth.views.CenteredPreview;

/**
 * Ui for the camera preview and capture screen.
 * 
 * @author Benedict Lau
 */
public class CaptureFragment extends Fragment {

    //
    // Fragment bundle keys.
    //

    private static final String FRAGMENT_BUNDLE_KEY_CAMERA = "camera";

    /**
     * Invalid camera id.
     */
    private static final int INVALID_CAMERA_ID = -1;

    /**
     * The number of frames to capture.
     */
    private static final int TOTAL_FRAMES_TO_CAPTURE = 4;

    /**
     * Delay between countdown steps in milliseconds.
     */
    private static final int COUNTDOWN_STEP_DELAY = 1000;

    /**
     * The default captured Jpeg quality.
     */
    private static final int CAPTURED_JPEG_QUALITY = 100;

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

    private TextView mTitle;

    private TextView mStatus;

    private ImageButton mSwitchButton;

    private CenteredPreview mPreview;

    private LinearLayout mCountdown;

    private TextView mCountdownThree;

    private TextView mCountdownTwo;

    private TextView mCountdownOne;

    private Button mStartButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Get params.
         */
        Bundle args = getArguments();
        boolean useFrontFacing = args.getBoolean(FRAGMENT_BUNDLE_KEY_CAMERA);

        /*
         * Select camera.
         */
        int cameraPreference = CameraInfo.CAMERA_FACING_BACK;
        if (useFrontFacing) {
            cameraPreference = CameraInfo.CAMERA_FACING_FRONT;
        }

        int numCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int cameraId = 0; cameraId < numCameras; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == cameraPreference) {
                mCameraId = cameraId;
                break;
            }
        }

        /*
         * Initialize timer for scheduling tasks.
         */
        mTimer = new Timer("countdownTimer");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        mTitle = (TextView) view.findViewById(R.id.title);
        mStatus = (TextView) view.findViewById(R.id.status);
        mSwitchButton = (ImageButton) view.findViewById(R.id.switch_button);
        mPreview = (CenteredPreview) view.findViewById(R.id.preview);
        mCountdown = (LinearLayout) view.findViewById(R.id.countdown);
        mCountdownThree = (TextView) view.findViewById(R.id.countdown_three);
        mCountdownTwo = (TextView) view.findViewById(R.id.countdown_two);
        mCountdownOne = (TextView) view.findViewById(R.id.countdown_one);
        mStartButton = (Button) view.findViewById(R.id.start_button);

        /*
         * Functionalize views.
         */
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch camera.
                boolean useFrontFacing = false;
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(mCameraId, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    useFrontFacing = true;
                }

                // Relaunch fragment with new camera.
                ((LaunchActivity) getActivity()).replaceFragment(CaptureFragment.newInstance(useFrontFacing), false,
                        true);
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mStartButton.setEnabled(false);
                    mStartButton.setVisibility(View.INVISIBLE);
                    mSwitchButton.setVisibility(View.GONE);

                    // Kick off capture sequence.
                    kickoffCaptureSequence();
                }
            }
        });

        /*
         * Reset frames.
         */
        mFrameIndex = 0;
        mFramesData = new byte[TOTAL_FRAMES_TO_CAPTURE][];

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

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
                Size pictureSize = CameraHelper.getOptimalPictureSize(mCamera.getParameters()
                        .getSupportedPictureSizes(), ImageHelper.IMAGE_SIZE, ImageHelper.IMAGE_SIZE);
                params.setPictureSize(pictureSize.width, pictureSize.height);

                mCamera.setParameters(params);

                /*
                 * Setup preview.
                 */
                mPreviewDisplayOrientation = CameraHelper.getCameraScreenOrientation(getActivity(), mCameraId);
                mCamera.setDisplayOrientation(mPreviewDisplayOrientation);
                mPreview.setCamera(mCamera, mPreviewDisplayOrientation);
            } catch (RuntimeException e) {
                Toast.makeText(getActivity(), getString(R.string.capture__error_camera_in_use), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.capture__error_no_camera), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        if (mCamera != null) {
            mPreview.setCamera(null, CameraHelper.CAMERA_SCREEN_ORIENTATION_0);
            mCamera.release();
            mCamera = null;
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        mTimer.cancel();

        super.onDestroy();
    }

    //
    // Private inner classes.
    //

    /**
     * Callback when focus is ready for capture.
     */
    private class CaptureFocusCallback implements AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            mStatus.setText("");
        }
    }

    /**
     * Callback when captured Jpeg is ready.
     */
    private class JpegPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Save frame Jpeg data in memory.
            mFramesData[mFrameIndex] = data;

            // Increment frame index.
            mFrameIndex++;

            if (mFrameIndex < TOTAL_FRAMES_TO_CAPTURE) {
                mTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        final Activity activity = getActivity();
                        if (activity != null && !activity.isFinishing()) {
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    // Restart preview and capture next frames.
                                    if (mCamera != null) {
                                        mCamera.startPreview();
                                    }

                                    kickoffCaptureSequence();
                                }
                            });
                        }
                    }
                }, COUNTDOWN_STEP_DELAY);
            } else {
                // Transition to next fragment since enough frames have been captured.
                nextFragment();
            }
        }
    }

    //
    // Private methods.
    //

    /**
     * Reset countdown timer.
     */
    private void resetCountdownTimer() {
        if (mCountdown != null) {
            mCountdown.setVisibility(View.INVISIBLE);

            // Reset colors.
            int color = getResources().getColor(R.color.lt_text_color);
            mCountdownOne.setTextColor(color);
            mCountdownTwo.setTextColor(color);
            mCountdownThree.setTextColor(color);
        }
    }

    /**
     * Kicks off countdown and auto-focus, capture frame at the end.
     */
    private void kickoffCaptureSequence() {
        // Update title.
        mTitle.setText(String.format(getString(R.string.capture__title_frame), mFrameIndex + 1));

        // Set visibility of countdown timer.
        mCountdown.setVisibility(View.VISIBLE);

        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mCountdownThree != null) {
                                mCountdownThree.setTextColor(getResources().getColor(R.color.selection_color));
                            }

                            // Kick off auto-focus and indicate status.
                            if (mStatus != null) {
                                mStatus.setText(String.format(getString(R.string.capture__status_focusing),
                                        mFrameIndex + 1));
                            }

                            if (mCamera != null) {
                                mCamera.autoFocus(new CaptureFocusCallback());
                            }
                        }
                    });
                }
            }
        }, COUNTDOWN_STEP_DELAY * 2);

        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mCountdownTwo != null) {
                                mCountdownTwo.setTextColor(getResources().getColor(R.color.selection_color));
                            }
                        }
                    });
                }
            }
        }, COUNTDOWN_STEP_DELAY * 3);

        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mCountdownOne != null) {
                                mCountdownOne.setTextColor(getResources().getColor(R.color.selection_color));
                            }
                        }
                    });
                }
            }
        }, COUNTDOWN_STEP_DELAY * 4);

        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // Reset countdown timer to initial state.
                            resetCountdownTimer();

                            // Capture frame.
                            if (mCamera != null) {
                                mCamera.takePicture(null, null, new JpegPictureCallback());
                            }
                        }
                    });
                }
            }
        }, COUNTDOWN_STEP_DELAY * 5);
    }

    /**
     * Launches the next {@link Fragment}.
     */
    private void nextFragment() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        boolean isReflected = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;

        ((LaunchActivity) getActivity()).replaceFragment(
                ConfirmImageFragment.newInstance(mFramesData, mPreviewDisplayOrientation, isReflected), true, false);
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link CaptureFragment} instance.
     * 
     * @param useFrontFacing
     *            true to use front facing camera; false otherwise.
     * @return the new {@link CaptureFragment} instance.
     */
    public static CaptureFragment newInstance(boolean useFrontFacing) {
        CaptureFragment fragment = new CaptureFragment();

        Bundle args = new Bundle();
        args.putBoolean(FRAGMENT_BUNDLE_KEY_CAMERA, useFrontFacing);
        fragment.setArguments(args);

        return fragment;
    }
}
