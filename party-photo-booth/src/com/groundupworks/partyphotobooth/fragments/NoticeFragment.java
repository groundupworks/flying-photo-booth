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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.wings.Wings;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.dropbox.DropboxEndpoint;
import com.groundupworks.wings.facebook.FacebookEndpoint;
import com.groundupworks.wings.gcp.GoogleCloudPrintEndpoint;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Notice screen after photo strip submission.
 *
 * @author Benedict Lau
 */
public class NoticeFragment extends Fragment {

    //
    // Fragment bundle keys.
    //

    private static final String FRAGMENT_BUNDLE_KEY_FACEBOOK_SHARED = "facebookShared";

    private static final String FRAGMENT_BUNDLE_KEY_DROPBOX_SHARED = "dropboxShared";

    private static final String FRAGMENT_BUNDLE_KEY_GCP_SHARED = "gcpShared";

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

    /**
     * The screen is valid only if it displays at least one sharing service.
     */
    private boolean mIsScreenValid = false;

    //
    // Views.
    //

    private Button mOkButton;

    private TextView mFacebookNotice;

    private TextView mDropboxNotice;

    private TextView mGcpNotice;

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
        mGcpNotice = (TextView) view.findViewById(R.id.notice_gcp);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Display notices for the linked sharing services.
         */
        Bundle args = getArguments();

        boolean facebookShared = args.getBoolean(FRAGMENT_BUNDLE_KEY_FACEBOOK_SHARED);
        boolean dropboxShared = args.getBoolean(FRAGMENT_BUNDLE_KEY_DROPBOX_SHARED);
        boolean gcpShared = args.getBoolean(FRAGMENT_BUNDLE_KEY_GCP_SHARED);

        WingsEndpoint facebookEndpoint = Wings.getEndpoint(FacebookEndpoint.class);
        if (facebookShared) {
            WingsEndpoint.LinkInfo linkInfo = facebookEndpoint.getLinkInfo();
            if (linkInfo != null) {
                mFacebookNotice.setText(linkInfo.mDestinationDescription);
                mFacebookNotice.setVisibility(View.VISIBLE);
                mIsScreenValid = true;
            }
        }

        WingsEndpoint dropboxEndpoint = Wings.getEndpoint(DropboxEndpoint.class);
        if (dropboxShared) {
            WingsEndpoint.LinkInfo linkInfo = dropboxEndpoint.getLinkInfo();
            if (linkInfo != null) {
                mDropboxNotice.setText(linkInfo.mDestinationDescription);
                mDropboxNotice.setVisibility(View.VISIBLE);
                mIsScreenValid = true;
            }
        }

        WingsEndpoint gcpEndpoint = Wings.getEndpoint(GoogleCloudPrintEndpoint.class);
        if (gcpShared) {
            WingsEndpoint.LinkInfo linkInfo = gcpEndpoint.getLinkInfo();
            if (linkInfo != null) {
                mGcpNotice.setText(linkInfo.mDestinationDescription);
                mGcpNotice.setVisibility(View.VISIBLE);
                mIsScreenValid = true;
            }
        }

        // Set click behaviour of Ok button or send dismissal request depending on whether the screen is valid.
        if (mIsScreenValid) {
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
        } else {
            // Screen is invalid. Send dismissal request to client.
            ICallbacks callbacks = getCallbacks();
            if (callbacks != null) {
                callbacks.onNoticeDismissRequested();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIsScreenValid) {
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
     * @param facebookShared true if the photo strip is marked for Facebook sharing; false otherwise.
     * @param dropboxShared  true if the photo strip is marked for Dropbox sharing; false otherwise.
     * @param gcpShared      true if the photo strip is marked for Google Cloud Print sharing; false otherwise.
     * @return the new {@link NoticeFragment} instance.
     */
    public static NoticeFragment newInstance(boolean facebookShared, boolean dropboxShared, boolean gcpShared) {
        NoticeFragment fragment = new NoticeFragment();

        Bundle args = new Bundle();
        args.putBoolean(FRAGMENT_BUNDLE_KEY_FACEBOOK_SHARED, facebookShared);
        args.putBoolean(FRAGMENT_BUNDLE_KEY_DROPBOX_SHARED, dropboxShared);
        args.putBoolean(FRAGMENT_BUNDLE_KEY_GCP_SHARED, gcpShared);
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
         * Notice dismissal is requested.
         */
        public void onNoticeDismissRequested();
    }
}
