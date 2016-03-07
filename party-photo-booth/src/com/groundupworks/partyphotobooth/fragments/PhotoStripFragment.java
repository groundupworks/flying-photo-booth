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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
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
import com.groundupworks.partyphotobooth.MyApplication;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.arrangements.BaseTitleHeader;
import com.groundupworks.partyphotobooth.controllers.PhotoStripController;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.TextHelper;
import com.groundupworks.partyphotobooth.themes.Theme;

import java.lang.ref.WeakReference;

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

    public static final int PHOTO_STRIP_SUBMIT = 2;

    //
    // Message bundle keys.
    //

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA = "jpegData";

    public static final String MESSAGE_BUNDLE_KEY_ROTATION = "rotation";

    public static final String MESSAGE_BUNDLE_KEY_REFLECTION = "reflection";

    /**
     * The duration for the {@link TranslateAnimation} when a new photo is added.
     */
    private static final long TRANSLATE_ANIMATION_DURATION = 3000L;

    /**
     * The delay between the photo removal request and the start of the fade animation.
     */
    private static final long FADE_ANIMATION_DELAY = 500L;

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<PhotoStripFragment.ICallbacks> mCallbacks = null;

    /**
     * Flag to track whether the photo strip is auto-scrolling.
     */
    private boolean mIsAutoScrolling = false;

    //
    // Views.
    //

    private TextView mTitle;

    private ScrollView mScroller;

    private LinearLayout mShadower;

    private LinearLayout mContainer;

    private TextView mEventLineOne;

    private TextView mEventLineTwo;

    private TextView mEventDate;

    private ImageView mEventLogo;

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
        mEventLineOne = (TextView) view.findViewById(R.id.event_line_one);
        mEventLineTwo = (TextView) view.findViewById(R.id.event_line_two);
        mEventDate = (TextView) view.findViewById(R.id.event_date);
        mEventLogo = (ImageView) view.findViewById(R.id.event_logo);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context appContext = getActivity().getApplicationContext();
        PreferencesHelper preferencesHelper = new PreferencesHelper();

        /*
         * Set the selected theme.
         */
        Theme theme = Theme.from(appContext, preferencesHelper.getPhotoBoothTheme(appContext));
        int backgroundRes = theme.getBackgroundResource();
        if (backgroundRes != Theme.RESOURCE_NONE) {
            mTitle.setBackgroundResource(backgroundRes);
        }

        /*
         * Calculate optimal text size.
         */
        float optimalTextSize = Float.MAX_VALUE;

        // Get bounding box for a line of header text.
        Resources res = getResources();
        int lineWidth = res.getDimensionPixelSize(R.dimen.photo_thumb_size);
        int lineHeight = res.getDimensionPixelSize(R.dimen.photo_thumb_text_line_height);

        // Calculate based on first line of event title.
        String eventLineOne = preferencesHelper.getEventLineOne(appContext);
        if (TextHelper.isValid(eventLineOne)) {
            mEventLineOne.setTypeface(theme.getFont());
            Paint paint = mEventLineOne.getPaint();
            float fittedTextSize = TextHelper.getFittedTextSize(eventLineOne, lineWidth, lineHeight, paint);
            if (fittedTextSize < optimalTextSize) {
                optimalTextSize = fittedTextSize;
            }
        }

        // Calculate based on second line of event title.
        String eventLineTwo = preferencesHelper.getEventLineTwo(appContext);
        if (TextHelper.isValid(eventLineTwo)) {
            mEventLineTwo.setTypeface(theme.getFont());
            Paint paint = mEventLineTwo.getPaint();
            float fittedTextSize = TextHelper.getFittedTextSize(eventLineTwo, lineWidth, lineHeight, paint);
            if (fittedTextSize < optimalTextSize) {
                optimalTextSize = fittedTextSize;
            }
        }

        // Calculate based on event date.
        String eventDateString = null;
        long eventDate = preferencesHelper.getEventDate(appContext);
        if (eventDate != PreferencesHelper.EVENT_DATE_HIDDEN) {
            mEventDate.setTypeface(theme.getFont());
            Paint paint = mEventDate.getPaint();
            eventDateString = TextHelper.getDateString(appContext, eventDate);
            float fittedTextSize = TextHelper.getFittedTextSize(eventDateString, lineWidth, lineHeight, paint);
            if (fittedTextSize < optimalTextSize) {
                optimalTextSize = fittedTextSize;
            }
        }

        /*
         * Display header.
         */
        // Display event title.
        if (TextHelper.isValid(eventLineOne)) {
            mEventLineOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, optimalTextSize);
            mEventLineOne.setText(eventLineOne);
            mEventLineOne.setVisibility(View.VISIBLE);
        } else {
            mEventLineOne.setVisibility(View.GONE);
        }
        if (TextHelper.isValid(eventLineTwo)) {
            mEventLineTwo.setTextSize(TypedValue.COMPLEX_UNIT_PX, optimalTextSize);
            mEventLineTwo.setText(eventLineTwo);
            mEventLineTwo.setVisibility(View.VISIBLE);
        } else {
            mEventLineTwo.setVisibility(View.GONE);
        }

        // Display event date.
        if (TextHelper.isValid(eventDateString)) {
            mEventDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, optimalTextSize);
            mEventDate.setText(eventDateString);
            mEventDate.setVisibility(View.VISIBLE);
        } else {
            mEventDate.setVisibility(View.GONE);
        }

        // Display event logo.
        String eventLogoUri = preferencesHelper.getEventLogoUri(appContext);
        Bitmap eventLogo = MyApplication.getBitmapCache().tryGet(BaseTitleHeader.EVENT_LOGO_CACHE_KEY);
        if (TextHelper.isValid(eventLogoUri) && eventLogo != null) {
            mEventLogo.setImageBitmap(eventLogo);
            mEventLogo.setVisibility(View.VISIBLE);
        } else {
            mEventLogo.setVisibility(View.GONE);
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
            case PhotoStripController.THUMB_BITMAP_READY:
                addThumb(activity, (Bitmap) msg.obj, msg.arg1, false);
                break;
            case PhotoStripController.FRAME_REMOVED:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onPhotoRemoved();
                }
                break;
            case PhotoStripController.PHOTO_STRIP_READY:
                addThumb(activity, (Bitmap) msg.obj, msg.arg1, true);
                break;
            case PhotoStripController.PHOTO_STRIP_SUBMITTED:
                Bundle bundle = msg.getData();
                boolean facebookShared = bundle.getBoolean(PhotoStripController.MESSAGE_BUNDLE_KEY_FACEBOOK_SHARED);
                boolean dropboxShared = bundle.getBoolean(PhotoStripController.MESSAGE_BUNDLE_KEY_DROPBOX_SHARED);
                boolean gcpShared = bundle.getBoolean(PhotoStripController.MESSAGE_BUNDLE_KEY_GCP_SHARED);

                // Call to client.
                if (callbacks != null) {
                    callbacks.onPhotoStripSubmitted(facebookShared, dropboxShared, gcpShared);
                }
                break;
            case PhotoStripController.ERROR_JPEG_DATA:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onErrorNewPhoto();
                }
                break;
            case PhotoStripController.ERROR_PHOTO_MISSING:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onErrorMissingPhoto();
                }
                break;
            case PhotoStripController.ERROR_PHOTO_STRIP_SUBMIT:
                // Call to client.
                if (callbacks != null) {
                    callbacks.onErrorPhotoStripSubmit();
                }
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
     * @param activity             the {@link Activity}.
     * @param thumb                the thumbnail bitmap.
     * @param key                  the key for the frame.
     * @param isPhotoStripComplete true if this is the last frame and the photo strip is complete; false otherwise.
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

        // Configure discard button.
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        PreferencesHelper.PhotoBoothMode mode = preferencesHelper.getPhotoBoothMode(activity);
        if (!PreferencesHelper.PhotoBoothMode.AUTOMATIC.equals(mode)) {
            discardButton.setTypeface(Theme.from(activity, preferencesHelper.getPhotoBoothTheme(activity)).getFont());
            discardButton.setVisibility(View.VISIBLE);
            discardButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Temporarily disable discard button when the photo strip auto-scrolls.
                    if (!mIsAutoScrolling) {
                        // Disable and remove view.
                        v.setEnabled(false);

                        // Post runnable to start animation.
                        mContainer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
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
                                        // Make view invisible and post a runnable to remove it from the photo strip.
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
                            }
                        }, FADE_ANIMATION_DELAY);

                        // Notify controller of the frame removal request.
                        Message msg = Message.obtain();
                        msg.what = FRAME_REMOVAL;
                        msg.arg1 = key;
                        sendEvent(msg);
                    }
                }
            });
        }

        // Create layout params for frame view.
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(photoSize, photoSize);
        layoutParams.setMargins(0, 0, 0, photoPadding);

        // Add view to hierarchy and start animation.
        mContainer.addView(frame, layoutParams);
        mScroller.fullScroll(ScrollView.FOCUS_DOWN);
        mShadower.startAnimation(getTranslateAnimation((float) offset, isPhotoStripComplete));
    }

    /**
     * Submits the current photo strip.
     */
    public void submitPhotoStrip() {
        // Notify controller of the photo strip submission request.
        Message msg = Message.obtain();
        msg.what = PHOTO_STRIP_SUBMIT;
        sendEvent(msg);
    }

    /**
     * Gets a {@link TranslateAnimation} for animating the photo strip when a new photo is added.
     *
     * @param offset               the starting offset in pixels.
     * @param isPhotoStripComplete true if this is the last frame and the photo strip is complete; false otherwise.
     * @return the animation.
     */
    private TranslateAnimation getTranslateAnimation(float offset, final boolean isPhotoStripComplete) {
        TranslateAnimation animation = new TranslateAnimation(0f, 0f, offset, 0f);
        animation.setDuration(TRANSLATE_ANIMATION_DURATION);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mIsAutoScrolling = true;
                mScroller.fullScroll(ScrollView.FOCUS_DOWN);

                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onNewPhotoAdded(isPhotoStripComplete);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing.
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mScroller.fullScroll(ScrollView.FOCUS_DOWN);
                mIsAutoScrolling = false;
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
     * @param data       the picture data.
     * @param rotation   clockwise rotation applied to image in degrees.
     * @param reflection horizontal reflection applied to image.
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
         * A new photo is added.
         *
         * @param isPhotoStripComplete true if this is the last frame and the photo strip is complete; false otherwise.
         */
        public void onNewPhotoAdded(boolean isPhotoStripComplete);

        /**
         * A photo is removed from the photo strip.
         */
        public void onPhotoRemoved();

        /**
         * The current photo strip is submitted.
         *
         * @param facebookShared true if the photo strip is marked for Facebook sharing; false otherwise.
         * @param dropboxShared  true if the photo strip is marked for Dropbox sharing; false otherwise.
         * @param gcpShared      true if the photo strip is marked for Google Cloud Print sharing; false otherwise.
         */
        public void onPhotoStripSubmitted(boolean facebookShared, boolean dropboxShared, boolean gcpShared);

        /**
         * An error occurred while attempting to add a new photo.
         */
        public void onErrorNewPhoto();

        /**
         * An error occurred while attempting to compile a photo strip. One or more photos are needed.
         */
        public void onErrorMissingPhoto();

        /**
         * An error occurred while attempting to submit the current photo strip.
         */
        public void onErrorPhotoStripSubmit();
    }
}
