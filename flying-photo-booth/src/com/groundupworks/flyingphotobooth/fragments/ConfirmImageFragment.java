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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.controllers.ConfirmImageController;
import com.groundupworks.flyingphotobooth.helpers.ImageHelper;

/**
 * Ui for the image confirmation screen.
 * 
 * @author Benedict Lau
 */
public class ConfirmImageFragment extends ControllerBackedFragment<ConfirmImageController> {

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

    public static final int IMAGE_CONFIRMED = 1;

    public static final int FRAGMENT_DESTROYED = 2;

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

    private ImageButton mSaveButton;

    private ImageButton mShareButton;

    private ImageView mImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String filterPref = preferences.getString(getString(R.string.pref__filter_key),
                getString(R.string.pref__filter_none));
        String arrangementPref = preferences.getString(getString(R.string.pref__arrangement_key),
                getString(R.string.pref__arrangement_vertical));

        /*
         * Inflate views from XML.
         */
        View view = null;
        if (arrangementPref.equals(getString(R.string.pref__arrangement_horizontal))) {
            view = inflater.inflate(R.layout.fragment_confirm_image_horizontal, container, false);
        } else if (arrangementPref.equals(getString(R.string.pref__arrangement_box))) {
            view = inflater.inflate(R.layout.fragment_confirm_image_box, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_confirm_image_vertical, container, false);
        }

        mSaveButton = (ImageButton) view.findViewById(R.id.save_button);
        mShareButton = (ImageButton) view.findViewById(R.id.share_button);
        mImage = (ImageView) view.findViewById(R.id.image);

        /*
         * Functionalize views.
         */
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveButton.setEnabled(false);

                // Notify controller the image is confirmed.
                Message msg = Message.obtain();
                msg.what = IMAGE_CONFIRMED;
                sendEvent(msg);
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveButton.setEnabled(false);

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

        return view;
    }

    @Override
    public void onDestroy() {
        // Notify controller the image is discarded.
        Message msg = Message.obtain();
        msg.what = FRAGMENT_DESTROYED;
        sendEvent(msg);
        super.onDestroy();
    }

    //
    // ControllerBackedFragment implementation.
    //

    @Override
    protected ConfirmImageController initController() {
        return new ConfirmImageController();
    }

    @Override
    public void handleUiUpdate(Message msg) {
        switch (msg.what) {
            case ConfirmImageController.ERROR_OCCURRED:
                Toast.makeText(getActivity(), getString(R.string.confirm_image__error_generic), Toast.LENGTH_LONG)
                        .show();
                break;
            case ConfirmImageController.BITMAP_READY:
                mImage.setImageBitmap((Bitmap) msg.obj);
                mShareButton.setEnabled(true);
                break;
            case ConfirmImageController.JPEG_SAVED:
                mSaveButton.setVisibility(View.INVISIBLE);
                mShareButton.setVisibility(View.VISIBLE);
                mJpegUri = Uri.parse("file://" + (String) msg.obj);

                // Request adding Jpeg to Android Gallery.
                MediaScannerConnection.scanFile(getActivity().getApplicationContext(),
                        new String[] { mJpegUri.getPath() }, new String[] { ImageHelper.JPEG_MIME_TYPE }, null);
            default:
                break;
        }
    }

    //
    // Public methods.
    //

    /**
     * Creates a new {@link ConfirmImageFragment} instance.
     * 
     * @param jpegData
     *            byte arrays of Jpeg data.
     * @param rotation
     *            clockwise rotation applied to image in degrees.
     * @param reflection
     *            horizontal reflection applied to image.
     * @return the new {@link ConfirmImageFragment} instance.
     */
    public static ConfirmImageFragment newInstance(byte[][] jpegData, float rotation, boolean reflection) {
        ConfirmImageFragment fragment = new ConfirmImageFragment();

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
