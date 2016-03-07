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
package com.groundupworks.flyingphotobooth.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.groundupworks.flyingphotobooth.LaunchActivity;
import com.groundupworks.flyingphotobooth.MyPreferenceActivity;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.lib.photobooth.framework.BaseApplication;
import com.groundupworks.lib.photobooth.helpers.CameraAudioHelper;
import com.groundupworks.lib.photobooth.helpers.CameraHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.views.CenteredPreview;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
     * Delay between countdown steps in milliseconds.
     */
    private static final int COUNTDOWN_STEP_DELAY = 1000;

    /**
     * Delay between captures in burst mode.
     */
    private static final int BURST_DELAY = 1000;

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
     * Flag to track whether capture sequence is running.
     */
    private boolean mIsCaptureSequenceRunning = false;

    /**
     * The selected camera.
     */
    private Camera mCamera = null;

    /**
     * Helper for audio feedback.
     */
    private CameraAudioHelper mCameraAudioHelper = null;

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
    // Key event handlers.
    //

    private LaunchActivity.BackPressedHandler mBackPressedHandler;

    private LaunchActivity.KeyEventHandler mKeyEventHandler;

    //
    // Views.
    //

    private TextView mTitle;

    private ImageButton mPreferencesButton;

    private ImageButton mSwitchButton;

    private CenteredPreview mPreview;

    private LinearLayout mCountdown;

    private TextView mCountdownThree;

    private TextView mCountdownTwo;

    private TextView mCountdownOne;

    private Button mStartButton;

    private LinearLayout mReviewOverlay;

    private TextView mReviewStatus;

    private ImageView mReviewImage;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        final Handler handler = new Handler(BaseApplication.getWorkerLooper());
        mCameraAudioHelper = new CameraAudioHelper(activity, R.raw.beep_once, handler);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Get params.
         */
        Bundle args = getArguments();
        mUseFrontFacing = args.getBoolean(FRAGMENT_BUNDLE_KEY_CAMERA);

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

        mTitle = (TextView) view.findViewById(R.id.title);
        mPreferencesButton = (ImageButton) view.findViewById(R.id.preferences_button);
        mSwitchButton = (ImageButton) view.findViewById(R.id.switch_button);
        mPreview = (CenteredPreview) view.findViewById(R.id.preview);
        mCountdown = (LinearLayout) view.findViewById(R.id.countdown);
        mCountdownThree = (TextView) view.findViewById(R.id.countdown_three);
        mCountdownTwo = (TextView) view.findViewById(R.id.countdown_two);
        mCountdownOne = (TextView) view.findViewById(R.id.countdown_one);
        mStartButton = (Button) view.findViewById(R.id.start_button);
        mReviewOverlay = (LinearLayout) view.findViewById(R.id.review_overlay);
        mReviewStatus = (TextView) view.findViewById(R.id.review_status);
        mReviewImage = (ImageView) view.findViewById(R.id.review_image);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LaunchActivity activity = (LaunchActivity) getActivity();
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
         * Initialize and set key event handlers.
         */
        mBackPressedHandler = new LaunchActivity.BackPressedHandler() {

            @Override
            public boolean onBackPressed() {
                // If capture sequence is running, exit capture sequence on back pressed event.
                if (mIsCaptureSequenceRunning) {
                    activity.replaceFragment(CaptureFragment.newInstance(mUseFrontFacing), false, true);
                }

                return mIsCaptureSequenceRunning;
            }
        };
        activity.setBackPressedHandler(mBackPressedHandler);

        mKeyEventHandler = new LaunchActivity.KeyEventHandler() {
            @Override
            public boolean onKeyEvent(KeyEvent event) {
                final int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    mStartButton.dispatchKeyEvent(event);
                    return true;
                }
                return false;
            }
        };
        activity.setKeyEventHandler(mKeyEventHandler);

        /*
         * Functionalize views.
         */
        mPreferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent preferencesIntent = new Intent(activity, MyPreferenceActivity.class);
                startActivity(preferencesIntent);
            }
        });

        // Show switch button only if more than one camera is available.
        if (mNumCameras > 1) {
            mSwitchButton.setVisibility(View.VISIBLE);
            mSwitchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Switch camera.
                    boolean useFrontFacing = false;
                    CameraInfo cameraInfo = new CameraInfo();
                    if (mCameraId != INVALID_CAMERA_ID) {
                        Camera.getCameraInfo(mCameraId, cameraInfo);
                        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                            useFrontFacing = true;
                        }

                        // Relaunch fragment with new camera.
                        activity.replaceFragment(CaptureFragment.newInstance(useFrontFacing), false, true);
                    }
                }
            });
        }

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

        // Configure title and start button text.
        mTitle.setText(String.format(getString(R.string.capture__title_frame), mFrameIndex + 1, mFramesTotal));
        if (mTriggerMode == TRIGGER_MODE_MANUAL) {
            // Update title.
            mStartButton.setText(getString(R.string.capture__start_manual_button_text));
        } else {
            mStartButton.setText(getString(R.string.capture__start_countdown_button_text));
        }

        // Configure start button behaviour.
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartButtonPressedImpl();
            }
        });

        mStartButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                onStartButtonPressedImpl();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mCameraAudioHelper.prepare();
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
        LaunchActivity activity = (LaunchActivity) getActivity();
        if (mOnPauseCalled) {
            // Relaunch fragment with new camera.
            activity.replaceFragment(CaptureFragment.newInstance(mUseFrontFacing), false, true);
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
                    mPreview.start(mCamera, pictureSize.width, pictureSize.height, mPreviewDisplayOrientation);
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
            mIsCaptureSequenceRunning = false;
        }

        if (mCamera != null) {
            mPreview.stop();
            mCamera.release();
            mCamera = null;
        }

        mOnPauseCalled = true;

        // Save the camera preference.
        saveCameraPreference(getActivity());

        super.onPause();
    }

    @Override
    public void onStop() {
        mCameraAudioHelper.release();
        super.onStop();
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
                mReviewStatus.setText(getString(R.string.capture__review_instructions));
                mReviewStatus.setTextColor(getResources().getColor(R.color.text_color));
                Bitmap bitmap = ImageHelper.createImage(data, mPreviewDisplayOrientation, mIsReflected, null);
                mReviewImage.setImageBitmap(bitmap);

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

                mReviewOverlay.setOnTouchListener(new ReviewOverlayOnTouchListener(latch));
                mReviewOverlay.setVisibility(View.VISIBLE);
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
         * @param latch the latch to countdown as a signal to proceed with capture sequence.
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
                            mReviewStatus.setText(getString(R.string.capture__review_discarded));
                            mReviewStatus.setTextColor(getResources().getColor(R.color.selection_color));

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
     * Handles the press of the start button.
     */
    private void onStartButtonPressedImpl() {
        if (mCamera != null) {
            mStartButton.setEnabled(false);
            mStartButton.setVisibility(View.INVISIBLE);
            mSwitchButton.setVisibility(View.GONE);
            mPreferencesButton.setVisibility(View.GONE);

            // Set flag to indicate capture sequence is running.
            mIsCaptureSequenceRunning = true;

            // Kick off capture sequence.
            if (mTriggerMode == TRIGGER_MODE_MANUAL) {
                kickoffManualCapture();
            } else {
                kickoffCountdownCapture();
            }
        }
    }

    /**
     * Takes picture.
     */
    private void takePicture() {
        if (isActivityAlive() && mCamera != null) {
            try {
                mCamera.takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        // Setting a listener enables the system shutter sound.
                    }
                }, null, new JpegPictureCallback());
            } catch (RuntimeException e) {
                // The native camera crashes occasionally. Self-recover by relaunching fragment.
                final LaunchActivity activity = (LaunchActivity) getActivity();
                Toast.makeText(activity, getString(R.string.capture__error_camera_crash), Toast.LENGTH_SHORT).show();
                activity.replaceFragment(CaptureFragment.newInstance(mUseFrontFacing), false, true);
            }
        }
    }

    /**
     * Resets countdown timer.
     */
    private void resetCountdownTimer() {
        if (mCountdown != null) {
            mCountdown.setVisibility(View.INVISIBLE);

            // Reset colors.
            int color = getResources().getColor(R.color.text_color);
            mCountdownOne.setTextColor(color);
            mCountdownTwo.setTextColor(color);
            mCountdownThree.setTextColor(color);
        }
    }

    /**
     * Kicks off auto-focus, captures frame at the end.
     */
    private void kickoffManualCapture() {
        // Kick off auto-focus and indicate status.
        if (mCamera != null) {
            mCamera.autoFocus(new MyAutoFocusCallback());
        }
    }

    /**
     * Kicks off countdown and auto-focus, captures frame at the end.
     */
    private void kickoffCountdownCapture() {
        // Set visibility of countdown timer.
        mCountdown.setVisibility(View.VISIBLE);

        if (mTimer != null) {
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    final Activity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (isActivityAlive()) {
                                    if (mCountdownThree != null) {
                                        mCountdownThree.setTextColor(getResources().getColor(R.color.selection_color));
                                    }
                                    mCameraAudioHelper.beep();

                                    // Kick off auto-focus and indicate status.
                                    if (mCamera != null) {
                                        mCamera.autoFocus(new MyAutoFocusCallback());
                                    }
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
                                if (isActivityAlive()) {
                                    if (mCountdownTwo != null) {
                                        mCountdownTwo.setTextColor(getResources().getColor(R.color.selection_color));
                                    }
                                    mCameraAudioHelper.beep();
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
                                if (isActivityAlive()) {
                                    if (mCountdownOne != null) {
                                        mCountdownOne.setTextColor(getResources().getColor(R.color.selection_color));
                                    }
                                    mCameraAudioHelper.beep();
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
                                if (isActivityAlive()) {
                                    // Reset countdown timer to initial state.
                                    resetCountdownTimer();

                                    // Capture frame.
                                    takePicture();
                                }
                            }
                        });
                    }
                }
            }, COUNTDOWN_STEP_DELAY * 5);
        }
    }

    /**
     * Kicks off capture in burst mode, which is basically capturing without auto-focus.
     */
    private void kickoffBurstCapture() {
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            takePicture();
                        }
                    });
                }
            }
        }, BURST_DELAY);
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
            // Cancel review overlay touch listener.
            mReviewOverlay.setOnTouchListener(null);

            // Increment frame index.
            mFrameIndex++;

            // Check if we need more frames.
            if (mFrameIndex < mFramesTotal) {
                // Update title.
                mTitle.setText(String.format(getString(R.string.capture__title_frame), mFrameIndex + 1, mFramesTotal));

                // Restart preview.
                if (mCamera != null && mPreview != null) {
                    try {
                        mPreview.restart();
                    } catch (RuntimeException exception) {
                        final LaunchActivity activity = (LaunchActivity) getActivity();
                        if (activity != null && !activity.isFinishing()) {
                            String title = getString(R.string.capture__error_camera_dialog_title);
                            String message = getString(R.string.capture__error_camera_dialog_message_dead);
                            activity.showDialogFragment(ErrorDialogFragment.newInstance(title, message));
                        }
                    }
                }

                // Fade out review overlay.
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // Do nothing.
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // Do nothing.
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // Hide the review overlay.
                        mReviewOverlay.setVisibility(View.GONE);
                        mReviewImage.setImageBitmap(null);

                        // Capture next frames.
                        if (mTriggerMode == TRIGGER_MODE_COUNTDOWN) {
                            kickoffCountdownCapture();
                        } else if (mTriggerMode == TRIGGER_MODE_BURST) {
                            kickoffBurstCapture();
                        } else {
                            // Enable start button.
                            mStartButton.setEnabled(true);
                            mStartButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
                mReviewOverlay.startAnimation(animation);
            } else {
                // Set flag to indicate capture sequence is no longer running.
                mIsCaptureSequenceRunning = false;

                // Transition to next fragment since enough frames have been captured.
                nextFragment();
            }
        }
    }

    /**
     * Launches the next {@link Fragment}.
     */
    private void nextFragment() {
        ((LaunchActivity) getActivity()).replaceFragment(
                ShareFragment.newInstance(mFramesData, mPreviewDisplayOrientation, mIsReflected), true, false);
    }

    /**
     * Saves the current camera preference.
     *
     * @param context the {@link Context}.
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
     * @param useFrontFacing true to use front facing camera; false otherwise.
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
