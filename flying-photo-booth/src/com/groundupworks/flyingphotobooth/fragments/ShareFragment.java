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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.groundupworks.flyingphotobooth.LaunchActivity;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.controllers.ShareController;
import com.groundupworks.lib.photobooth.framework.ControllerBackedFragment;
import com.groundupworks.lib.photobooth.helpers.BeamHelper;
import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.wings.Wings;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.dropbox.DropboxEndpoint;
import com.groundupworks.wings.facebook.FacebookEndpoint;
import com.groundupworks.wings.gcp.GoogleCloudPrintEndpoint;
import com.squareup.otto.Subscribe;

import java.util.Set;

/**
 * Ui for the image confirmation screen.
 *
 * @author Benedict Lau
 */
public class ShareFragment extends ControllerBackedFragment<ShareController> {

    //
    // Fragment bundle keys.
    //

    private static final String[] FRAGMENT_BUNDLE_KEY_JPEG_DATA = {"jpegData0", "jpegData1", "jpegData2", "jpegData3"};

    private static final String FRAGMENT_BUNDLE_KEY_ROTATION = "rotation";

    private static final String FRAGMENT_BUNDLE_KEY_REFLECTION = "reflection";

    //
    // Ui events. The controller should be notified of these events.
    //

    public static final int IMAGE_VIEW_READY = 0;

    public static final int FRAGMENT_DESTROYED = 1;

    public static final int GCP_SHARE_REQUESTED = 2;

    public static final int FACEBOOK_SHARE_REQUESTED = 3;

    public static final int DROPBOX_SHARE_REQUESTED = 4;

    //
    // Message bundle keys.
    //

    public static final String[] MESSAGE_BUNDLE_KEY_JPEG_DATA = FRAGMENT_BUNDLE_KEY_JPEG_DATA;

    public static final String MESSAGE_BUNDLE_KEY_ROTATION = FRAGMENT_BUNDLE_KEY_ROTATION;

    public static final String MESSAGE_BUNDLE_KEY_REFLECTION = FRAGMENT_BUNDLE_KEY_REFLECTION;

    public static final String MESSAGE_BUNDLE_KEY_FILTER = "filter";

    public static final String MESSAGE_BUNDLE_KEY_ARRANGEMENT = "arrangement";

    public static final String MESSAGE_BUNDLE_KEY_MAX_THUMB_WIDTH = "maxThumbWidth";

    public static final String MESSAGE_BUNDLE_KEY_MAX_THUMB_HEIGHT = "maxThumbHeight";

    /**
     * The uri to the Jpeg stored in the file system.
     */
    private Uri mJpegUri = null;

    //
    // Google Cloud Print share with Wings.
    //

    /**
     * The Google Cloud Print Wings endpoint.
     */
    private WingsEndpoint mGcpEndpoint = Wings.getEndpoint(GoogleCloudPrintEndpoint.class);

    /**
     * Listener for Google Cloud Print linking events. May be null.
     */
    private OnSharedPreferenceChangeListener mGcpLinkListener = null;

    //
    // Facebook share with Wings.
    //

    /**
     * The Facebook Wings endpoint.
     */
    private WingsEndpoint mFacebookEndpoint = Wings.getEndpoint(FacebookEndpoint.class);

    /**
     * Listener for Facebook linking events. May be null.
     */
    private OnSharedPreferenceChangeListener mFacebookLinkListener = null;

    //
    // Dropbox share with Wings.
    //

    /**
     * The Dropbox Wings endpoint.
     */
    private WingsEndpoint mDropboxEndpoint = Wings.getEndpoint(DropboxEndpoint.class);

    /**
     * Listener for Dropbox linking events. May be null.
     */
    private OnSharedPreferenceChangeListener mDropboxLinkListener = null;

    //
    // Views.
    //

    private ImageButton mShareButton;

    private ImageButton mDropboxButton;

    private ImageButton mFacebookButton;

    private ImageButton mGcpButton;

    private ImageButton mBeamButton;

    private FrameLayout mPhotoStripContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_share, container, false);

        mShareButton = (ImageButton) view.findViewById(R.id.share_button);
        mDropboxButton = (ImageButton) view.findViewById(R.id.dropbox_button);
        mFacebookButton = (ImageButton) view.findViewById(R.id.facebook_button);
        mGcpButton = (ImageButton) view.findViewById(R.id.gcp_button);
        mBeamButton = (ImageButton) view.findViewById(R.id.beam_button);
        mPhotoStripContainer = (FrameLayout) view.findViewById(R.id.photostrip_container);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LaunchActivity activity = (LaunchActivity) getActivity();

        /*
         * Get params.
         */
        Bundle args = getArguments();

        int jpegDataLength = 0;
        for (int i = 0; i < FRAGMENT_BUNDLE_KEY_JPEG_DATA.length; i++) {
            if (args.containsKey(FRAGMENT_BUNDLE_KEY_JPEG_DATA[i])) {
                jpegDataLength++;
            } else {
                break;
            }
        }

        byte[][] jpegData = new byte[jpegDataLength][];
        for (int i = 0; i < jpegDataLength; i++) {
            jpegData[i] = args.getByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA[i]);
        }

        float rotation = args.getFloat(FRAGMENT_BUNDLE_KEY_ROTATION);
        boolean reflection = args.getBoolean(FRAGMENT_BUNDLE_KEY_REFLECTION);

        /*
         * Get user preferences.
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String filterPref = preferences.getString(getString(R.string.pref__filter_key),
                getString(R.string.pref__filter_default));
        String arrangementPref = preferences.getString(getString(R.string.pref__arrangement_key),
                getString(R.string.pref__arrangement_default));

        /*
         * Functionalize views.
         */
        mShareButton.setEnabled(false);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri jpegUri = mJpegUri;
                if (jpegUri != null) {
                    // Launch sharing Intent.
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType(ImageHelper.JPEG_MIME_TYPE);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, jpegUri);
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.share__share_chooser_title)));
                }
            }
        });

        mDropboxButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    if (mDropboxEndpoint.isLinked()) {
                        requestShare(DROPBOX_SHARE_REQUESTED);
                    } else {
                        // Listen to Dropbox linking event.
                        mDropboxLinkListener = new DropboxLinkListener();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity
                                .getApplicationContext());
                        preferences.registerOnSharedPreferenceChangeListener(mDropboxLinkListener);

                        // Start Dropbox link request.
                        mDropboxEndpoint.startLinkRequest(activity, ShareFragment.this);
                    }
                }
            }
        });

        mFacebookButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    if (mFacebookEndpoint.isLinked()) {
                        requestShare(FACEBOOK_SHARE_REQUESTED);
                    } else {
                        // Listen to Facebook linking event.
                        mFacebookLinkListener = new FacebookLinkListener();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity
                                .getApplicationContext());
                        preferences.registerOnSharedPreferenceChangeListener(mFacebookLinkListener);

                        // Start Facebook link request.
                        mFacebookEndpoint.startLinkRequest(activity, ShareFragment.this);
                    }
                }
            }
        });

        mGcpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    if (mGcpEndpoint.isLinked()) {
                        requestShare(GCP_SHARE_REQUESTED);
                    } else {
                        // Listen to GCP linking event.
                        mGcpLinkListener = new GcpLinkListener();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity
                                .getApplicationContext());
                        preferences.registerOnSharedPreferenceChangeListener(mGcpLinkListener);

                        // Start GCP link request.
                        mGcpEndpoint.startLinkRequest(activity, ShareFragment.this);
                    }
                }
            }
        });

        mBeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.share__beam_toast), Toast.LENGTH_LONG).show();
            }
        });

        mBeamButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((LaunchActivity) getActivity()).showDialogFragment(BeamDetailsDialogFragment.newInstance());
                return true;
            }
        });

        // Get the max thumbnail size the view can hold.
        Point maxThumbSize = ImageHelper.getMaxThumbSize(getResources(), arrangementPref);

        // Notify controller the image view is ready.
        Message msg = Message.obtain();
        msg.what = IMAGE_VIEW_READY;
        Bundle bundle = new Bundle();
        for (int i = 0; i < jpegDataLength; i++) {
            bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA[i], jpegData[i]);
        }
        bundle.putFloat(MESSAGE_BUNDLE_KEY_ROTATION, rotation);
        bundle.putBoolean(MESSAGE_BUNDLE_KEY_REFLECTION, reflection);
        bundle.putString(MESSAGE_BUNDLE_KEY_FILTER, filterPref);
        bundle.putString(MESSAGE_BUNDLE_KEY_ARRANGEMENT, arrangementPref);
        bundle.putInt(MESSAGE_BUNDLE_KEY_MAX_THUMB_WIDTH, maxThumbSize.x);
        bundle.putInt(MESSAGE_BUNDLE_KEY_MAX_THUMB_HEIGHT, maxThumbSize.y);
        msg.setData(bundle);
        sendEvent(msg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Call Wings APIs.
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            endpoint.onActivityResultImpl(getActivity(), ShareFragment.this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Call Wings APIs.
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            endpoint.onResumeImpl();
        }

        // Subscribe to Wings link events.
        Wings.subscribe(this);
    }

    @Override
    public void onPause() {
        // Unsubscribe to Wings link events.
        Wings.unsubscribe(this);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        Activity activity = getActivity();

        // Unregister listeners to shared preferences.
        if (mFacebookLinkListener != null || mDropboxLinkListener != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity
                    .getApplicationContext());
            if (mFacebookLinkListener != null) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(mFacebookLinkListener);
                mFacebookLinkListener = null;
            }
            if (mDropboxLinkListener != null) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(mDropboxLinkListener);
                mDropboxLinkListener = null;
            }
        }

        // Notify controller the image is discarded.
        Message msg = Message.obtain();
        msg.what = FRAGMENT_DESTROYED;
        sendEvent(msg);

        // Cancel Android Beam.
        BeamHelper.beamUris(activity, null);

        super.onDestroy();
    }

    //
    // Subscriptions.
    //

    @Subscribe
    public void handleLinkEvent(FacebookEndpoint.LinkEvent event) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editor.putBoolean(getString(R.string.pref__facebook_link_key), event.isLinked());
        editor.apply();
    }

    @Subscribe
    public void handleLinkEvent(DropboxEndpoint.LinkEvent event) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editor.putBoolean(getString(R.string.pref__dropbox_link_key), event.isLinked());
        editor.apply();
    }

    @Subscribe
    public void handleLinkEvent(GoogleCloudPrintEndpoint.LinkEvent event) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit();
        editor.putBoolean(getString(R.string.pref__gcp_link_key), event.isLinked());
        editor.apply();
    }

    //
    // ControllerBackedFragment implementation.
    //

    @Override
    protected ShareController initController() {
        return new ShareController();
    }

    @Override
    public void handleUiUpdate(Message msg) {
        Activity activity = getActivity();
        final Context appContext = activity.getApplicationContext();

        switch (msg.what) {
            case ShareController.ERROR_OCCURRED:
                Toast.makeText(activity, getString(R.string.share__error_generic), Toast.LENGTH_LONG).show();
                break;
            case ShareController.THUMB_READY:
                Bitmap thumbBitmap = (Bitmap) msg.obj;

                int bitmapWidth = thumbBitmap.getWidth();
                int bitmapHeight = thumbBitmap.getHeight();
                int containerWidth = mPhotoStripContainer.getWidth();
                int containerHeight = mPhotoStripContainer.getHeight();

                // Select scroll view based on bitmap and container size.
                int scrollViewResource = R.layout.fragment_share_scroll_none;
                if (bitmapWidth > containerWidth && bitmapHeight > containerHeight) {
                    // Bitmap needs scroll in both directions.
                    scrollViewResource = R.layout.fragment_share_scroll_box;
                } else if (bitmapWidth > containerWidth) {
                    // Bitmap needs scroll in x.
                    scrollViewResource = R.layout.fragment_share_scroll_horizontal;
                } else if (bitmapHeight > containerHeight) {
                    // Bitmap needs scroll in y.
                    scrollViewResource = R.layout.fragment_share_scroll_vertical;
                }

                // Inflate container and thumbnail to view.
                LayoutInflater.from(getActivity()).inflate(scrollViewResource, mPhotoStripContainer, true);
                ImageView imageView = (ImageView) mPhotoStripContainer.findViewById(R.id.image);
                imageView.setImageBitmap(thumbBitmap);
                break;
            case ShareController.JPEG_SAVED:
                mJpegUri = Uri.parse("file://" + (String) msg.obj);

                // Enable sharing options.
                mShareButton.setEnabled(true);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
                if (mDropboxEndpoint.isLinked() && preferences.getBoolean(getString(R.string.pref__dropbox_auto_share_key), false)) {
                    requestShare(DROPBOX_SHARE_REQUESTED);
                } else {
                    mDropboxButton.setVisibility(View.VISIBLE);
                }

                if (mFacebookEndpoint.isLinked() && preferences.getBoolean(getString(R.string.pref__facebook_auto_share_key), false)) {
                    requestShare(FACEBOOK_SHARE_REQUESTED);
                } else {
                    mFacebookButton.setVisibility(View.VISIBLE);
                }

                if (mGcpEndpoint.isLinked() && preferences.getBoolean(getString(R.string.pref__gcp_auto_share_key), false)) {
                    requestShare(GCP_SHARE_REQUESTED);
                } else {
                    mGcpButton.setVisibility(View.VISIBLE);
                }

                if (BeamHelper.supportsBeam(appContext)) {
                    mBeamButton.setVisibility(View.VISIBLE);
                }

                // Setup Android Beam.
                BeamHelper.beamUris(activity, new Uri[]{mJpegUri});

                // Request adding Jpeg to Android Gallery.
                MediaScannerConnection.scanFile(appContext, new String[]{mJpegUri.getPath()},
                        new String[]{ImageHelper.JPEG_MIME_TYPE}, null);
                break;
            case ShareController.GCP_SHARE_MARKED:
                mGcpButton.setEnabled(false);
                break;
            case ShareController.FACEBOOK_SHARE_MARKED:
                mFacebookButton.setEnabled(false);
                break;
            case ShareController.DROPBOX_SHARE_MARKED:
                mDropboxButton.setEnabled(false);
                break;
            default:
                break;
        }
    }

    //
    // Private methods.
    //

    /**
     * Notifies controller of a share request.
     *
     * @param event the share request event.
     */
    private void requestShare(int event) {
        Message msg = Message.obtain();
        msg.what = event;
        sendEvent(msg);
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ShareFragment} instance.
     *
     * @param jpegData   byte arrays of Jpeg data.
     * @param rotation   clockwise rotation applied to image in degrees.
     * @param reflection horizontal reflection applied to image.
     * @return the new {@link ShareFragment} instance.
     */
    public static ShareFragment newInstance(byte[][] jpegData, float rotation, boolean reflection) {
        ShareFragment fragment = new ShareFragment();

        Bundle args = new Bundle();
        for (int i = 0; i < jpegData.length; i++) {
            args.putByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA[i], jpegData[i]);
        }
        args.putFloat(FRAGMENT_BUNDLE_KEY_ROTATION, rotation);
        args.putBoolean(FRAGMENT_BUNDLE_KEY_REFLECTION, reflection);
        fragment.setArguments(args);

        return fragment;
    }

    //
    // Private inner classes.
    //

    /**
     * Listener for the Google Cloud Print link event. Deliver deferred share click if linked.
     */
    private class GcpLinkListener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()
                    && key.equals(getString(R.string.pref__gcp_link_key)) && mGcpEndpoint.isLinked()) {
                requestShare(GCP_SHARE_REQUESTED);
            }
        }
    }

    /**
     * Listener for the Facebook link event. Deliver deferred share click if linked.
     */
    private class FacebookLinkListener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()
                    && key.equals(getString(R.string.pref__facebook_link_key)) && mFacebookEndpoint.isLinked()) {
                requestShare(FACEBOOK_SHARE_REQUESTED);
            }
        }
    }

    /**
     * Listener for the Dropbox link event. Deliver deferred share click if linked.
     */
    private class DropboxLinkListener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()
                    && key.equals(getString(R.string.pref__dropbox_link_key)) && mDropboxEndpoint.isLinked()) {
                requestShare(DROPBOX_SHARE_REQUESTED);
            }
        }
    }
}
