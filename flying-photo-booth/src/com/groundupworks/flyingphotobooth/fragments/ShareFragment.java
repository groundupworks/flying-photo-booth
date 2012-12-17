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
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.groundupworks.flyingphotobooth.LaunchActivity;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.controllers.ShareController;
import com.groundupworks.flyingphotobooth.helpers.BeamHelper;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;

/**
 * Ui for the image confirmation screen.
 * 
 * @author Benedict Lau
 */
public class ShareFragment extends ControllerBackedFragment<ShareController> {

    //
    // Fragment bundle keys.
    //

    private static final String FRAGMENT_BUNDLE_KEY_JPEG_DATA_0 = "jpeg_data_0";

    private static final String FRAGMENT_BUNDLE_KEY_JPEG_DATA_1 = "jpeg_data_1";

    private static final String FRAGMENT_BUNDLE_KEY_JPEG_DATA_2 = "jpeg_data_2";

    private static final String FRAGMENT_BUNDLE_KEY_JPEG_DATA_3 = "jpeg_data_3";

    private static final String FRAGMENT_BUNDLE_KEY_ROTATION = "rotation";

    private static final String FRAGMENT_BUNDLE_KEY_REFLECTION = "reflection";

    //
    // Jpeg data indices.
    //

    private static final int JPEG_DATA_INDEX_0 = 0;

    private static final int JPEG_DATA_INDEX_1 = 1;

    private static final int JPEG_DATA_INDEX_2 = 2;

    private static final int JPEG_DATA_INDEX_3 = 3;

    //
    // Ui events. The controller should be notified of these events.
    //

    public static final int IMAGE_VIEW_READY = 0;

    public static final int FRAGMENT_DESTROYED = 1;

    //
    // Message bundle keys.
    //

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA_0 = FRAGMENT_BUNDLE_KEY_JPEG_DATA_0;

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA_1 = FRAGMENT_BUNDLE_KEY_JPEG_DATA_1;

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA_2 = FRAGMENT_BUNDLE_KEY_JPEG_DATA_2;

    public static final String MESSAGE_BUNDLE_KEY_JPEG_DATA_3 = FRAGMENT_BUNDLE_KEY_JPEG_DATA_3;

    public static final String MESSAGE_BUNDLE_KEY_ROTATION = FRAGMENT_BUNDLE_KEY_ROTATION;

    public static final String MESSAGE_BUNDLE_KEY_REFLECTION = FRAGMENT_BUNDLE_KEY_REFLECTION;

    public static final String MESSAGE_BUNDLE_KEY_FILTER = "filter";

    public static final String MESSAGE_BUNDLE_KEY_ARRANGEMENT = "arrangement";

    /**
     * The uri to the Jpeg stored in the file system.
     */
    private Uri mJpegUri = null;

    //
    // Views.
    //

    private ViewStub mScrollViewStub;

    private ImageButton mShareButton;

    private ImageButton mBeamButton;

    private ImageView mImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_confirm_image, container, false);

        mScrollViewStub = (ViewStub) view.findViewById(R.id.scrollview_stub);
        mShareButton = (ImageButton) view.findViewById(R.id.share_button);
        mBeamButton = (ImageButton) view.findViewById(R.id.beam_button);

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
        byte[] jpegData0 = args.getByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_0);
        byte[] jpegData1 = args.getByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_1);
        byte[] jpegData2 = args.getByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_2);
        byte[] jpegData3 = args.getByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_3);
        float rotation = args.getFloat(FRAGMENT_BUNDLE_KEY_ROTATION);
        boolean reflection = args.getBoolean(FRAGMENT_BUNDLE_KEY_REFLECTION);

        /*
         * Get user preferences.
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String filterPref = preferences.getString(getString(R.string.pref__filter_key),
                getString(R.string.pref__filter_none));
        String arrangementPref = preferences.getString(getString(R.string.pref__arrangement_key),
                getString(R.string.pref__arrangement_vertical));

        /*
         * Inflate view stub.
         */
        if (arrangementPref.equals(getString(R.string.pref__arrangement_horizontal))) {
            mScrollViewStub.setLayoutResource(R.layout.fragment_confirm_image_horizontal);
        } else if (arrangementPref.equals(getString(R.string.pref__arrangement_box))) {
            mScrollViewStub.setLayoutResource(R.layout.fragment_confirm_image_box);
        } else {
            mScrollViewStub.setLayoutResource(R.layout.fragment_confirm_image_vertical);
        }

        View view = mScrollViewStub.inflate();
        mImage = (ImageView) view.findViewById(R.id.image);

        /*
         * Functionalize views.
         */
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri jpegUri = mJpegUri;
                if (jpegUri != null) {
                    // Launch sharing Intent.
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType(ImageHelper.JPEG_MIME_TYPE);
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, jpegUri);
                    startActivity(Intent.createChooser(sharingIntent,
                            getString(R.string.confirm_image__share_chooser_title)));
                }
            }
        });

        mBeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.confirm_image__beam_toast), Toast.LENGTH_LONG).show();
            }
        });

        mBeamButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((LaunchActivity) getActivity()).showDialogFragment(BeamDetailsDialogFragment.newInstance());
                return true;
            }
        });

        // Notify controller the image view is ready.
        Message msg = Message.obtain();
        msg.what = IMAGE_VIEW_READY;
        Bundle bundle = new Bundle();
        bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA_0, jpegData0);
        bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA_1, jpegData1);
        bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA_2, jpegData2);
        bundle.putByteArray(MESSAGE_BUNDLE_KEY_JPEG_DATA_3, jpegData3);
        bundle.putFloat(MESSAGE_BUNDLE_KEY_ROTATION, rotation);
        bundle.putBoolean(MESSAGE_BUNDLE_KEY_REFLECTION, reflection);
        bundle.putString(MESSAGE_BUNDLE_KEY_FILTER, filterPref);
        bundle.putString(MESSAGE_BUNDLE_KEY_ARRANGEMENT, arrangementPref);
        msg.setData(bundle);
        sendEvent(msg);
    }

    @Override
    public void onDestroy() {
        // Notify controller the image is discarded.
        Message msg = Message.obtain();
        msg.what = FRAGMENT_DESTROYED;
        sendEvent(msg);

        // Cancel Android Beam.
        BeamHelper.beamUris(getActivity(), null);

        super.onDestroy();
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
        Context appContext = activity.getApplicationContext();

        switch (msg.what) {
            case ShareController.ERROR_OCCURRED:
                Toast.makeText(activity, getString(R.string.confirm_image__error_generic), Toast.LENGTH_LONG).show();
                break;
            case ShareController.BITMAP_READY:
                mImage.setImageBitmap((Bitmap) msg.obj);
                break;
            case ShareController.JPEG_SAVED:
                mShareButton.setVisibility(View.VISIBLE);
                if (BeamHelper.supportsBeam(appContext)) {
                    mBeamButton.setVisibility(View.VISIBLE);
                }
                mJpegUri = Uri.parse("file://" + (String) msg.obj);

                // Setup Android Beam.
                BeamHelper.beamUris(activity, new Uri[] { mJpegUri });

                // Request adding Jpeg to Android Gallery.
                MediaScannerConnection.scanFile(appContext, new String[] { mJpegUri.getPath() },
                        new String[] { ImageHelper.JPEG_MIME_TYPE }, null);
            default:
                break;
        }
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ShareFragment} instance.
     * 
     * @param jpegData
     *            byte arrays of Jpeg data.
     * @param rotation
     *            clockwise rotation applied to image in degrees.
     * @param reflection
     *            horizontal reflection applied to image.
     * @return the new {@link ShareFragment} instance.
     */
    public static ShareFragment newInstance(byte[][] jpegData, float rotation, boolean reflection) {
        ShareFragment fragment = new ShareFragment();

        Bundle args = new Bundle();
        args.putByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_0, jpegData[JPEG_DATA_INDEX_0]);
        args.putByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_1, jpegData[JPEG_DATA_INDEX_1]);
        args.putByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_2, jpegData[JPEG_DATA_INDEX_2]);
        args.putByteArray(FRAGMENT_BUNDLE_KEY_JPEG_DATA_3, jpegData[JPEG_DATA_INDEX_3]);
        args.putFloat(FRAGMENT_BUNDLE_KEY_ROTATION, rotation);
        args.putBoolean(FRAGMENT_BUNDLE_KEY_REFLECTION, reflection);
        fragment.setArguments(args);

        return fragment;
    }
}
