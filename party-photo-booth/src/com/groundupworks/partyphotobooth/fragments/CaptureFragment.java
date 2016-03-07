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
package com.groundupworks.partyphotobooth.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable.Callback;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.groundupworks.lib.photobooth.framework.BaseApplication;
import com.groundupworks.lib.photobooth.helpers.CameraAudioHelper;
import com.groundupworks.lib.photobooth.helpers.CameraHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.views.AnimationDrawableCallback;
import com.groundupworks.lib.photobooth.views.CenteredPreview;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothMode;
import com.groundupworks.partyphotobooth.kiosk.KioskActivity;
import com.groundupworks.partyphotobooth.themes.Theme;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Ui for the camera preview and capture screen.
 *
 * @author Benedict Lau
 */
public class CaptureFragment extends Fragment {

    //
    // Fragment bundle keys.
    //

    public static final String FRAGMENT_BUNDLE_KEY_CURRENT_FRAME = "currentFrame";

    public static final String FRAGMENT_BUNDLE_KEY_TOTAL_FRAMES = "totalFrames";

    /**
     * Invalid camera id.
     */
    private static final int INVALID_CAMERA_ID = -1;

    /**
     * The default captured Jpeg quality.
     */
    private static final int CAPTURED_JPEG_QUALITY = 100;

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<CaptureFragment.ICallbacks> mCallbacks = null;

    /**
     * Handler for a key event.
     */
    private KioskActivity.KeyEventHandler mKeyEventHandler;

    /**
     * Id of the selected camera.
     */
    private int mCameraId = INVALID_CAMERA_ID;

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

    //
    // Views.
    //

    private CenteredPreview mPreview;

    private Button mStartButton;

    private TextView mFrameCount;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<CaptureFragment.ICallbacks>((CaptureFragment.ICallbacks) activity);

        final Handler handler = new Handler(BaseApplication.getWorkerLooper());
        mCameraAudioHelper = new CameraAudioHelper(activity, R.raw.beep_once, handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        mPreview = (CenteredPreview) view.findViewById(R.id.camera_preview);
        mStartButton = (Button) view.findViewById(R.id.capture_button);
        mFrameCount = (TextView) view.findViewById(R.id.frame_count);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final KioskActivity activity = (KioskActivity) getActivity();
        final Context appContext = activity.getApplicationContext();
        final Bundle args = getArguments();
        final int totalFrames = args.getInt(FRAGMENT_BUNDLE_KEY_TOTAL_FRAMES);
        final int currentFrame = args.getInt(FRAGMENT_BUNDLE_KEY_CURRENT_FRAME);

        /*
         * Select camera from preference.
         */
        // Get from preference.
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        PhotoBoothMode mode = preferencesHelper.getPhotoBoothMode(appContext);

        int cameraPreference = CameraInfo.CAMERA_FACING_FRONT;
        if (PhotoBoothMode.PHOTOGRAPHER.equals(mode)) {
            cameraPreference = CameraInfo.CAMERA_FACING_BACK;
        }

        // Default to first camera available.
        final int numCameras = Camera.getNumberOfCameras();
        if (numCameras > 0) {
            mCameraId = 0;
        }

        // Select preferred camera.
        CameraInfo cameraInfo = new CameraInfo();
        for (int cameraId = 0; cameraId < numCameras; cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);

            // Break on finding the preferred camera.
            if (cameraInfo.facing == cameraPreference) {
                mCameraId = cameraId;
                break;
            }
        }

         /*
         * Initialize and set key event handlers.
         */
        mKeyEventHandler = new KioskActivity.KeyEventHandler() {
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
        // Configure start button and trigger behaviour.
        switch (mode) {
            case PHOTOGRAPHER:
                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initiateCapture();
                    }
                });
                mStartButton.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        initiateCapture();
                        return true;
                    }
                });
                linkStartButton();
                break;
            case AUTOMATIC:
                if (currentFrame > 1) {
                    // Auto-trigger when camera is ready and preview has started.
                    mPreview.setOnPreviewListener(new CenteredPreview.OnPreviewListener() {
                        @Override
                        public void onStarted() {
                            if (isActivityAlive()) {
                                mPreview.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isActivityAlive()) {
                                            initiateCountdownCapture();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onStopped() {
                            // Do nothing.
                        }
                    });
                    break;
                } else {
                    // Fall through to self-serve behaviour. Do not break.
                }
            case SELF_SERVE:
            default:
                mStartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        initiateCountdownCapture();
                    }
                });
                mStartButton.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        initiateCountdownCapture();
                        return true;
                    }
                });
                linkStartButton();
        }

        // Show frame count only if more than one frame is to be captured.
        if (totalFrames > 1) {
            String frameCountText = getString(R.string.capture__frame_count, currentFrame, totalFrames);
            mFrameCount.setTypeface(Theme.from(appContext, preferencesHelper.getPhotoBoothTheme(appContext)).getFont());
            mFrameCount.setText(frameCountText);
            mFrameCount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mCameraAudioHelper.prepare();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Dim system ui for more immersive ui experience.
        dimSystemUi();

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
                mPreviewDisplayOrientation = CameraHelper.getCameraScreenOrientation(getActivity(), mCameraId);
                mCamera.setDisplayOrientation(mPreviewDisplayOrientation);
                mPreview.start(mCamera, pictureSize.width, pictureSize.height, mPreviewDisplayOrientation);
            } catch (RuntimeException e) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onErrorCameraInUse();
                }
            }
        } else {
            // Call to client.
            ICallbacks callbacks = getCallbacks();
            if (callbacks != null) {
                callbacks.onErrorCameraNone();
            }
        }
    }

    @Override
    public void onPause() {
        if (mCamera != null) {
            mPreview.stop();
            mCamera.release();
            mCamera = null;
        }

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
     * Take picture when count down completes.
     */
    private class TakePictureAnimationDrawableCallback extends AnimationDrawableCallback {

        /**
         * Constructor.
         *
         * @param animationDrawable the {@link AnimationDrawable}.
         * @param callback          the client's {@link Callback} implementation. This is usually the {@link View} the has the
         *                          {@link AnimationDrawable} as background.
         */
        public TakePictureAnimationDrawableCallback(AnimationDrawable animationDrawable, Callback callback) {
            super(animationDrawable, callback);
        }

        @Override
        public void onAnimationAdvanced(int currentFrame, int totalFrames) {
            if (currentFrame > 0) {
                mCameraAudioHelper.beep();
            }
        }

        @Override
        public void onAnimationCompleted() {
            takePicture();
        }
    }

    /**
     * Take picture when focus is ready.
     */
    private class TakePictureAutoFocusCallback implements AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            takePicture();
        }
    }

    /**
     * Callback when captured Jpeg is ready.
     */
    private class JpegPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (isActivityAlive()) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onPictureTaken(data, mPreviewDisplayOrientation, false);
                }
            }
        }
    }

    //
    // Private methods.
    //

    /**
     * Gets the callbacks for this fragment.
     *
     * @return the callbacks; or null if not set.
     */
    private CaptureFragment.ICallbacks getCallbacks() {
        CaptureFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

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
     * Requests system ui to enter low profile mode.
     */
    @SuppressLint("NewApi")
    private void dimSystemUi() {
        // View.SYSTEM_UI_FLAG_LOW_PROFILE only available in ICS and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mStartButton.post(new Runnable() {
                @Override
                public void run() {
                    if (isActivityAlive()) {
                        mStartButton.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
            });
        }
    }

    /**
     * Sets the enabled state of the start button based on the preview state.
     */
    private void linkStartButton() {
        mPreview.setOnPreviewListener(new CenteredPreview.OnPreviewListener() {
            @Override
            public void onStarted() {
                if (isActivityAlive()) {
                    mStartButton.setEnabled(true);
                }
            }

            @Override
            public void onStopped() {
                if (isActivityAlive()) {
                    mStartButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * Initiates the capture sequence.
     */
    private void initiateCapture() {
        if (mCamera != null) {
            mStartButton.setEnabled(false);

            // Start auto-focus. Take picture when auto-focus completes.
            mCamera.autoFocus(new TakePictureAutoFocusCallback());
        }
    }

    /**
     * Initiates the capture sequence with count down.
     */
    private void initiateCountdownCapture() {
        if (mCamera != null) {
            mStartButton.setEnabled(false);

            // Start auto-focus.
            mCamera.autoFocus(null);

            // Start animation. Take picture when count down animation completes.
            final AnimationDrawable countdownAnimation = (AnimationDrawable) mStartButton.getBackground();
            countdownAnimation.setCallback(new TakePictureAnimationDrawableCallback(countdownAnimation,
                    mStartButton));

            mStartButton.post(new Runnable() {
                @Override
                public void run() {
                    countdownAnimation.start();
                }
            });
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
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onErrorCameraCrashed();
                }
            }
        }
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link CaptureFragment} instance.
     *
     * @param currentFrame the current frame number to capture.
     * @param totalFrames  the total number of frames to capture.
     * @return the new {@link CaptureFragment} instance.
     */
    public static CaptureFragment newInstance(int currentFrame, int totalFrames) {
        CaptureFragment fragment = new CaptureFragment();

        Bundle args = new Bundle();
        args.putInt(FRAGMENT_BUNDLE_KEY_CURRENT_FRAME, currentFrame);
        args.putInt(FRAGMENT_BUNDLE_KEY_TOTAL_FRAMES, totalFrames);
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * A picture is taken.
         *
         * @param data       the picture data.
         * @param rotation   clockwise rotation applied to image in degrees.
         * @param reflection horizontal reflection applied to image.
         */
        public void onPictureTaken(byte[] data, float rotation, boolean reflection);

        /**
         * No camera.
         */
        public void onErrorCameraNone();

        /**
         * Camera in use.
         */
        public void onErrorCameraInUse();

        /**
         * Camera crashed.
         */
        public void onErrorCameraCrashed();
    }
}
