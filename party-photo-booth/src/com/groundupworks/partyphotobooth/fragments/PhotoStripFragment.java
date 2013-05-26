/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.groundupworks.lib.photobooth.framework.ControllerBackedFragment;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.controllers.PhotoStripController;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper.PhotoBoothTheme;

/**
 * Display photos in a photo strip format.
 * 
 * @author Benedict Lau
 */
public class PhotoStripFragment extends ControllerBackedFragment<PhotoStripController> {

    //
    // Ui events. The controller should be notified of these events.
    //

    public static final int JPEG_DATA_READY = 0;

    public static final int FRAME_REMOVAL = 1;

    //
    // Message bundle keys.
    //

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA = "jpegData";

    public static final String MESSAGE_BUNDLE_KEY_ROTATION = "rotation";

    public static final String MESSAGE_BUNDLE_KEY_REFLECTION = "reflection";

    /**
     * The duration for the {@link TranslateAnimation}.
     */
    private static final long ANIMATION_DURATION = 3000L;

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<PhotoStripFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    private TextView mTitle;

    private ScrollView mScroller;

    private LinearLayout mShadower;

    private LinearLayout mContainer;

    private TextView mEventTitle;

    private TextView mEventDate;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<PhotoStripFragment.ICallbacks>((PhotoStripFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_photo_strip, container, false);

        mTitle = (TextView) view.findViewById(R.id.title);
        mScroller = (ScrollView) view.findViewById(R.id.photo_strip_scroll_container);
        mShadower = (LinearLayout) view.findViewById(R.id.photo_strip_shadow_container);
        mContainer = (LinearLayout) view.findViewById(R.id.photo_strip_container);
        mEventTitle = (TextView) view.findViewById(R.id.event_title);
        mEventDate = (TextView) view.findViewById(R.id.event_date);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context appContext = getActivity().getApplicationContext();
        PreferencesHelper preferencesHelper = new PreferencesHelper();

        // Set the selected theme.
        PhotoBoothTheme theme = preferencesHelper.getPhotoBoothTheme(appContext);
        if (PhotoBoothTheme.STRIPES_BLUE.equals(theme)) {
            mTitle.setBackgroundResource(R.drawable.bitmap_tile_blue);
        } else if (PhotoBoothTheme.STRIPES_PINK.equals(theme)) {
            mTitle.setBackgroundResource(R.drawable.bitmap_tile_pink);
        } else if (PhotoBoothTheme.STRIPES_ORANGE.equals(theme)) {
            mTitle.setBackgroundResource(R.drawable.bitmap_tile_orange);
        } else if (PhotoBoothTheme.STRIPES_GREEN.equals(theme)) {
            mTitle.setBackgroundResource(R.drawable.bitmap_tile_green);
        }

        // Display event title.
        String eventName = preferencesHelper.getEventName(appContext);
        if (eventName == null || eventName.isEmpty()) {
            mEventTitle.setVisibility(View.GONE);
        } else {
            mEventTitle.setText(eventName);
            mEventTitle.setVisibility(View.VISIBLE);
        }

        // Display event date.
        String eventDate = preferencesHelper.getEventDate(appContext);
        if (eventDate == null || eventDate.isEmpty()) {
            mEventDate.setVisibility(View.GONE);
        } else {
            mEventDate.setText(eventDate);
            mEventDate.setVisibility(View.VISIBLE);
        }
    }

    //
    // ControllerBackedFragment implementation.
    //

    @Override
    protected PhotoStripController initController() {
        return new PhotoStripController();
    }

    @Override
    protected void handleUiUpdate(Message msg) {
        Activity activity = getActivity();

        ICallbacks callbacks = getCallbacks();
        switch (msg.what) {
            case PhotoStripController.ERROR_JPEG_DATA:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onNewPhotoError();
                }
                break;
            case PhotoStripController.THUMB_BITMAP_READY:
                addThumb(activity, (Bitmap) msg.obj, msg.arg1, false);
                break;
            case PhotoStripController.FRAME_REMOVED:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onPhotoRemoval();
                }
                break;
            case PhotoStripController.PHOTO_STRIP_READY:
                addThumb(activity, (Bitmap) msg.obj, msg.arg1, true);
                break;
            default:
                break;
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
    private PhotoStripFragment.ICallbacks getCallbacks() {
        PhotoStripFragment.ICallbacks callbacks = null;
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
     * Adds the thumbnail of a frame to the photo strip ui.
     * 
     * @param activity
     *            the {@link Activity}.
     * @param thumb
     *            the thumbnail bitmap.
     * @param key
     *            the key for the frame.
     * @param isPhotoStripComplete
     *            true if this is the last frame and the photo strip is complete; false otherwise.
     */
    private void addThumb(Activity activity, Bitmap thumb, final int key, boolean isPhotoStripComplete) {
        Resources res = getResources();
        int photoSize = res.getDimensionPixelSize(R.dimen.photo_thumb_size);
        int photoPadding = res.getDimensionPixelSize(R.dimen.kiosk_spacing);
        int offset = photoSize + photoPadding;

        BitmapDrawable drawable = new BitmapDrawable(res, thumb);

        // Create view for frame.
        final RelativeLayout frame = (RelativeLayout) LayoutInflater.from(activity).inflate(
                R.layout.view_photo_strip_frame, null);
        ImageView photo = (ImageView) frame.findViewById(R.id.frame_photo);
        Button discardButton = (Button) frame.findViewById(R.id.frame_button_discard);

        // Set thumbnail and click listener.
        photo.setImageDrawable(drawable);
        final Animation animation = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
        discardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable and remove view.
                v.setEnabled(false);

                // Remove view with animation.
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
                        // Make view invisible and post a task to remove it from the photo strip.
                        frame.setVisibility(View.GONE);
                        mContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                mContainer.removeView(frame);
                            }
                        });
                    }
                });
                frame.startAnimation(animation);

                // Notify controller of the frame removal request.
                Message msg = Message.obtain();
                msg.what = FRAME_REMOVAL;
                msg.arg1 = key;
                sendEvent(msg);
            }
        });

        // Create layout params for frame view.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(photoSize, photoSize);
        layoutParams.setMargins(0, 0, 0, photoPadding);

        // Add view to hierarchy and start animation.
        mContainer.addView(frame, layoutParams);
        mScroller.fullScroll(ScrollView.FOCUS_DOWN);
        mShadower.startAnimation(getTranslateAnimation((float) offset, isPhotoStripComplete));
    }

    /**
     * Gets a {@link TranslateAnimation} for animating the photo strip when a new photo is added.
     * 
     * @param offset
     *            the starting offset in pixels.
     * @param isPhotoStripComplete
     *            true if this is the last frame and the photo strip is complete; false otherwise.
     * @return the animation.
     */
    private TranslateAnimation getTranslateAnimation(float offset, final boolean isPhotoStripComplete) {
        TranslateAnimation animation = new TranslateAnimation(0f, 0f, offset, 0f);
        animation.setDuration(ANIMATION_DURATION);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mScroller.fullScroll(ScrollView.FOCUS_DOWN);

                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onNewPhotoStart();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScroller.fullScroll(ScrollView.FOCUS_DOWN);

                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onNewPhotoEnd(isPhotoStripComplete);
                }
            }
        });

        return animation;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link PhotoStripFragment} instance.
     * 
     * @return the new {@link PhotoStripFragment} instance.
     */
    public static PhotoStripFragment newInstance() {
        return new PhotoStripFragment();
    }

    /**
     * Adds a new photo to the photo strip.
     * 
     * @param data
     *            the picture data.
     * @param rotation
     *            clockwise rotation applied to image in degrees.
     * @param reflection
     *            horizontal reflection applied to image.
     */
    public void addPhoto(byte[] data, float rotation, boolean reflection) {
        if (isActivityAlive()) {
            // Notify controller Jpeg data is ready.
            Message msg = Message.obtain();
            msg.what = JPEG_DATA_READY;
            Bundle bundle = new Bundle();
            bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA, data);
            bundle.putFloat(MESSAGE_BUNDLE_KEY_ROTATION, rotation);
            bundle.putBoolean(MESSAGE_BUNDLE_KEY_REFLECTION, reflection);
            msg.setData(bundle);
            sendEvent(msg);
        }
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * A new photo animation started.
         */
        public void onNewPhotoStart();

        /**
         * A new photo animation ended.
         * 
         * @param isPhotoStripComplete
         *            true if this is the last frame and the photo strip is complete; false otherwise.
         */
        public void onNewPhotoEnd(boolean isPhotoStripComplete);

        /**
         * A photo is removed from the photo strip.
         */
        public void onPhotoRemoval();

        /**
         * An error occurred while attempting to add a new photo.
         */
        public void onNewPhotoError();
    }
}
