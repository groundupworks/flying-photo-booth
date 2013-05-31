/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.fragments;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.groundupworks.lib.photobooth.dropbox.DropboxHelper;
import com.groundupworks.lib.photobooth.facebook.FacebookHelper;
import com.groundupworks.partyphotobooth.R;

/**
 * Notice screen after photo strip submission.
 * 
 * @author Benedict Lau
 */
public class NoticeFragment extends Fragment {

    /**
     * The name of the auto-dismissal timer.
     */
    private static final String AUTO_DISMISSAL_TIMER_NAME = "dismissTimer";

    /**
     * The timeout for auto-dismissal to trigger in milliseconds.
     */
    private static final long AUTO_DISMISSAL_TIMEOUT = 40000L;

    /**
     * Timer for scheduling auto-dismissal of this {@link Fragment}.
     */
    private Timer mDismissalTimer = null;

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<NoticeFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    private Button mOkButton;

    private TextView mFacebookNotice;

    private TextView mDropboxNotice;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<NoticeFragment.ICallbacks>((NoticeFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        mOkButton = (Button) view.findViewById(R.id.notice_button_ok);
        mFacebookNotice = (TextView) view.findViewById(R.id.notice_facebook);
        mDropboxNotice = (TextView) view.findViewById(R.id.notice_dropbox);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mOkButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onNoticeDismissRequested();
                }
            }
        });

        /*
         * Display notices for the linked sharing services.
         */
        Activity activity = getActivity();

        FacebookHelper facebookHelper = new FacebookHelper();
        if (facebookHelper.isLinked(activity)) {
            String name = facebookHelper.getLinkedAccountName(activity);
            String album = facebookHelper.getLinkedAlbumName(activity);
            mFacebookNotice.setText(getString(R.string.notice__facebook_text, name, album));
            mFacebookNotice.setVisibility(View.VISIBLE);
        }

        DropboxHelper dropboxHelper = new DropboxHelper();
        if (dropboxHelper.isLinked(activity)) {
            String name = dropboxHelper.getLinkedAccountName(activity);
            String url = dropboxHelper.getLinkedShareUrl(activity);
            mDropboxNotice.setText(getString(R.string.notice__dropbox_text, name, url));
            mDropboxNotice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Schedule auto-dismissal of the fragment.
        mDismissalTimer = new Timer(AUTO_DISMISSAL_TIMER_NAME);
        mDismissalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Post dismissal request to ui thread.
                final Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Call to client.
                            ICallbacks callbacks = getCallbacks();
                            if (callbacks != null) {
                                callbacks.onNoticeDismissRequested();
                            }
                        }
                    });
                }
            }
        }, AUTO_DISMISSAL_TIMEOUT);
    }

    @Override
    public void onPause() {
        // Cancel timer for auto-dimissal.
        if (mDismissalTimer != null) {
            mDismissalTimer.cancel();
            mDismissalTimer = null;
        }

        super.onPause();
    }

    //
    // Private methods.
    //

    /**
     * Gets the callbacks for this fragment.
     * 
     * @return the callbacks; or null if not set.
     */
    private NoticeFragment.ICallbacks getCallbacks() {
        NoticeFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link NoticeFragment} instance.
     * 
     * @return the new {@link NoticeFragment} instance.
     */
    public static NoticeFragment newInstance() {
        return new NoticeFragment();
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Notice dismissal is requested.
         */
        public void onNoticeDismissRequested();
    }
}
