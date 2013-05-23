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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.groundupworks.lib.photobooth.framework.ControllerBackedFragment;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.controllers.PhotoStripController;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;

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

    public static final int FACEBOOK_SHARE_REQUESTED = 2;

    public static final int DROPBOX_SHARE_REQUESTED = 3;

    //
    // Message bundle keys.
    //

    public static final String MESSAGE_BUNDLE_KEY_THUMB_JPEG_DATA = "jpegData";

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

    /**
     * The total number of frames to capture.
     */
    private int mFramesTotal = 0;

    /**
     * The current frame index.
     */
    private int mFrameIndex = 0;

    //
    // Views.
    //

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

        // Set number of photos from preferences.
        mFramesTotal = preferencesHelper.getPhotoStripNumPhotos(appContext);
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
     * Gets a {@link TranslateAnimation} for animating the photo strip when a new photo is added.
     * 
     * @param offset
     *            the starting offset in pixels.
     * @return the animation.
     */
    private TranslateAnimation getTranslateAnimation(float offset) {
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
                    callbacks.onNewPhotoEnd(mFrameIndex, mFramesTotal);
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
            Activity activity = getActivity();
            Resources res = getResources();

            int photoSize = res.getDimensionPixelSize(R.dimen.photo_thumb_size);
            int photoPadding = res.getDimensionPixelSize(R.dimen.kiosk_spacing);
            int offset = photoSize + photoPadding;

            // TODO Perform background tasks.
            Bitmap bitmap = ImageHelper.createImage(data, rotation, reflection, null);
            BitmapDrawable drawable = new BitmapDrawable(res, bitmap);

            // Create view.
            ImageView imageView = new ImageView(activity);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            imageView.setImageDrawable(drawable);

            // Create layout params.
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(photoSize, photoSize);
            layoutParams.setMargins(0, 0, 0, photoPadding);

            // Add view to hierarchy and start animation.
            mContainer.addView(imageView, layoutParams);
            mScroller.fullScroll(ScrollView.FOCUS_DOWN);
            mShadower.startAnimation(getTranslateAnimation((float) offset));
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
         * @param count
         *            the count of the newly added photo.
         * @param totalNumPhotos
         *            the total number of photos expected in the photo strip.
         */
        public void onNewPhotoEnd(int count, int totalNumPhotos);
    }
}
