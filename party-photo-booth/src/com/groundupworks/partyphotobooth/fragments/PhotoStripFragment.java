/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.groundupworks.partyphotobooth.R;

/**
 * Display photos in a photo strip format.
 * 
 * @author Benedict Lau
 */
public class PhotoStripFragment extends Fragment {

    private ScrollView mScroller;

    private LinearLayout mShadower;

    private LinearLayout mContainer;

    private TextView mEventTitle;

    private TextView mEventDate;

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

        // TODO Dummy event title.
        mEventTitle.setText("Hello World!");
        mEventDate.setText("June 29, 2013");

        // TODO Dummy photos.
        Timer timer = new Timer();

        for (int i = 1; i < 5; i++) {
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            ImageView photo = new ImageView(getActivity());
                            int size = getResources().getDimensionPixelSize(R.dimen.photo_thumb_size);
                            mContainer.addView(photo, new LayoutParams(size, size));
                            photo.setScaleType(ScaleType.CENTER_CROP);
                            photo.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
                            mScroller.fullScroll(ScrollView.FOCUS_DOWN);

                            TranslateAnimation animation = new TranslateAnimation(0f, 0f, size, 0f);
                            animation.setDuration(5000L);
                            animation.setAnimationListener(new AnimationListener() {

                                @Override
                                public void onAnimationStart(Animation animation) {
                                    mScroller.fullScroll(ScrollView.FOCUS_DOWN);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    // TODO Auto-generated method stub

                                }
                            });
                            mShadower.startAnimation(animation);
                        }
                    });
                }
            }, i * 8000);
        }

        return view;
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
}
