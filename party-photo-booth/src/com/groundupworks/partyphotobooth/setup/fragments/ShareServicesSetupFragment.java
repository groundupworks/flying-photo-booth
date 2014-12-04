/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.wings.Wings;
import com.groundupworks.wings.WingsEndpoint;
import com.groundupworks.wings.dropbox.DropboxEndpoint;
import com.groundupworks.wings.facebook.FacebookEndpoint;
import com.groundupworks.wings.gcp.GoogleCloudPrintEndpoint;
import com.squareup.otto.Subscribe;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Ui for setting up the sharing services.
 *
 * @author Benedict Lau
 */
public class ShareServicesSetupFragment extends Fragment {

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<ShareServicesSetupFragment.ICallbacks> mCallbacks = null;

    //
    // Views.
    //

    private LinearLayout mFacebook;

    private ImageView mFacebookIcon;

    private TextView mFacebookStatus;

    private LinearLayout mDropbox;

    private ImageView mDropboxIcon;

    private TextView mDropboxStatus;

    private LinearLayout mGcp;

    private ImageView mGcpIcon;

    private TextView mGcpStatus;

    private CheckBox mNoticeEnabled;

    private Button mNext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<ShareServicesSetupFragment.ICallbacks>(
                (ShareServicesSetupFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_share_services_setup, container, false);
        mFacebook = (LinearLayout) view.findViewById(R.id.setup_share_services_facebook);
        mFacebookIcon = (ImageView) view.findViewById(R.id.setup_share_services_facebook_icon);
        mFacebookStatus = (TextView) view.findViewById(R.id.setup_share_services_facebook_status);
        mDropbox = (LinearLayout) view.findViewById(R.id.setup_share_services_dropbox);
        mDropboxIcon = (ImageView) view.findViewById(R.id.setup_share_services_dropbox_icon);
        mDropboxStatus = (TextView) view.findViewById(R.id.setup_share_services_dropbox_status);
        mGcp = (LinearLayout) view.findViewById(R.id.setup_share_services_gcp);
        mGcpIcon = (ImageView) view.findViewById(R.id.setup_share_services_gcp_icon);
        mGcpStatus = (TextView) view.findViewById(R.id.setup_share_services_gcp_status);
        mNoticeEnabled = (CheckBox) view.findViewById(R.id.setup_share_services_notice_enabled);
        mNext = (Button) view.findViewById(R.id.setup_share_services_button_next);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context appContext = getActivity().getApplicationContext();

        /*
         * Configure views with saved preferences and functionalize.
         */
        mFacebook.setOnClickListener(new MyLinkOnClickListener(FacebookEndpoint.class));
        mDropbox.setOnClickListener(new MyLinkOnClickListener(DropboxEndpoint.class));
        mGcp.setOnClickListener(new MyLinkOnClickListener(GoogleCloudPrintEndpoint.class));

        final PreferencesHelper preferencesHelper = new PreferencesHelper();
        boolean isChecked = preferencesHelper.getNoticeEnabled(appContext);
        mNoticeEnabled.setChecked(isChecked);
        mNoticeEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity activity = getActivity();
                if (activity != null && !activity.isFinishing()) {
                    preferencesHelper.storeNoticeEnabled(appContext, isChecked);
                }
            }
        });

        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onShareServicesSetupCompleted();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Call Wings APIs.
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            endpoint.onActivityResultImpl(getActivity(), this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Context appContext = getActivity().getApplicationContext();

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

    //
    // Subscriptions.
    //

    @Subscribe
    public void handleLinkEvent(FacebookEndpoint.LinkEvent event) {
        WingsEndpoint endpoint = Wings.getEndpoint(event.getEndpoint());
        if (event.isLinked()) {
            String destinationDescription = endpoint.getDestinationDescription(FacebookEndpoint.DestinationId.PROFILE);
            mFacebookStatus.setText(destinationDescription);
            mFacebookStatus.setTextColor(getResources().getColor(R.color.text_dark));
            mFacebookIcon.setEnabled(true);
        } else {
            mFacebookStatus.setText(R.string.share_services_setup__disabled);
            mFacebookStatus.setTextColor(getResources().getColor(R.color.text_light));
            mFacebookIcon.setEnabled(false);
        }

        updateNoticeEnabled();
    }

    @Subscribe
    public void handleLinkEvent(DropboxEndpoint.LinkEvent event) {
        WingsEndpoint endpoint = Wings.getEndpoint(event.getEndpoint());
        if (event.isLinked()) {
            String destinationDescription = endpoint.getDestinationDescription(DropboxEndpoint.DestinationId.APP_FOLDER);
            mDropboxStatus.setText(destinationDescription);
            mDropboxStatus.setTextColor(getResources().getColor(R.color.text_dark));
            mDropboxIcon.setEnabled(true);
        } else {
            mDropboxStatus.setText(R.string.share_services_setup__disabled);
            mDropboxStatus.setTextColor(getResources().getColor(R.color.text_light));
            mDropboxIcon.setEnabled(false);
        }

        updateNoticeEnabled();
    }

    @Subscribe
    public void handleLinkEvent(GoogleCloudPrintEndpoint.LinkEvent event) {
        WingsEndpoint endpoint = Wings.getEndpoint(event.getEndpoint());
        if (event.isLinked()) {
            String destinationDescription = endpoint.getDestinationDescription(GoogleCloudPrintEndpoint.DestinationId.PRINT_QUEUE);
            mGcpStatus.setText(destinationDescription);
            mGcpStatus.setTextColor(getResources().getColor(R.color.text_dark));
            mGcpIcon.setEnabled(true);
        } else {
            mGcpStatus.setText(R.string.share_services_setup__disabled);
            mGcpStatus.setTextColor(getResources().getColor(R.color.text_light));
            mGcpIcon.setEnabled(false);
        }

        updateNoticeEnabled();
    }

    //
    // Private methods.
    //

    /**
     * Gets the callbacks for this fragment.
     *
     * @return the callbacks; or null if not set.
     */
    private ShareServicesSetupFragment.ICallbacks getCallbacks() {
        ShareServicesSetupFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    /**
     * Updates the notice enabled {@link CheckBox} ui.
     */
    private void updateNoticeEnabled() {
        // The notice screen is only relevant if there is at least one share service enabled.
        boolean isEnabled = false;
        Set<WingsEndpoint> endpoints = Wings.getEndpoints();
        for (WingsEndpoint endpoint : endpoints) {
            if (endpoint.isLinked()) {
                isEnabled = true;
                break;
            }
        }

        mNoticeEnabled.setEnabled(isEnabled);
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ShareServicesSetupFragment} instance.
     *
     * @return the new {@link ShareServicesSetupFragment} instance.
     */
    public static ShareServicesSetupFragment newInstance() {
        return new ShareServicesSetupFragment();
    }

    //
    // Private inner classes.
    //

    /**
     * Listener that handles linking and unlinking with a Wings endpoint.
     */
    private class MyLinkOnClickListener implements OnClickListener {

        /**
         * The Wings endpoint.
         */
        private WingsEndpoint mEndpoint;

        /**
         * Constructor.
         *
         * @param endpointClazz the {@link java.lang.Class} of the Wings endpoint to toggle linking.
         */
        private MyLinkOnClickListener(Class<? extends WingsEndpoint> endpointClazz) {
            mEndpoint = Wings.getEndpoint(endpointClazz);
        }

        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing()) {
                if (mEndpoint.isLinked()) {
                    // Unlink from endpoint.
                    mEndpoint.unlink();
                } else {
                    // Start link request.
                    mEndpoint.startLinkRequest(activity, ShareServicesSetupFragment.this);
                }
            }
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
         * Setup of the share services has completed.
         */
        void onShareServicesSetupCompleted();
    }
}
