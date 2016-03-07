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
package com.groundupworks.partyphotobooth.setup.fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.partyphotobooth.MyApplication;
import com.groundupworks.partyphotobooth.PersistedBitmapCache;
import com.groundupworks.partyphotobooth.R;
import com.groundupworks.partyphotobooth.arrangements.BaseTitleHeader;
import com.groundupworks.partyphotobooth.helpers.PreferencesHelper;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Ui for setting up the event information.
 *
 * @author Benedict Lau
 */
public class EventInfoSetupFragment extends Fragment {

    /**
     * Image mime type for event logo selection.
     */
    private static final String EVENT_LOGO_MIME_TYPE = "image/*";

    /**
     * Request code to use for {@link Fragment#startActivityForResult(android.content.Intent, int)}.
     */
    private static final int EVENT_LOGO_REQUEST_CODE = 369;

    /**
     * Callbacks for this fragment.
     */
    private WeakReference<EventInfoSetupFragment.ICallbacks> mCallbacks = null;

    /**
     * A {@link PreferencesHelper} instance.
     */
    private PreferencesHelper mPreferencesHelper = new PreferencesHelper();

    /**
     * The application bitmap cache.
     */
    private PersistedBitmapCache mBitmapCache = MyApplication.getBitmapCache();

    //
    // Views.
    //

    private EditText mLineOne;

    private EditText mLineTwo;

    private TextView mLogoUri;

    private ImageView mLogoClear;

    private DatePicker mDate;

    private CheckBox mDateHidden;

    private Button mNext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = new WeakReference<EventInfoSetupFragment.ICallbacks>((EventInfoSetupFragment.ICallbacks) activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Inflate views from XML.
         */
        View view = inflater.inflate(R.layout.fragment_event_info_setup, container, false);
        mLineOne = (EditText) view.findViewById(R.id.setup_event_info_line_one);
        mLineTwo = (EditText) view.findViewById(R.id.setup_event_info_line_two);
        mLogoUri = (TextView) view.findViewById(R.id.setup_event_info_logo_uri);
        mLogoClear = (ImageView) view.findViewById(R.id.setup_event_info_logo_clear);
        mDate = (DatePicker) view.findViewById(R.id.setup_event_info_date);
        mDateHidden = (CheckBox) view.findViewById(R.id.setup_event_info_date_hidden);
        mNext = (Button) view.findViewById(R.id.setup_event_info_button_next);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Context appContext = getActivity().getApplicationContext();
        String lineOnePref = mPreferencesHelper.getEventLineOne(appContext);
        String lineTwoPref = mPreferencesHelper.getEventLineTwo(appContext);
        String logoUriPref = mPreferencesHelper.getEventLogoUri(appContext);
        long datePref = mPreferencesHelper.getEventDate(appContext);

        /*
         * Configure views with saved preferences and functionalize.
         */
        if (TextHelper.isValid(lineOnePref)) {
            mLineOne.setText(lineOnePref);
            mLineOne.setNextFocusDownId(R.id.setup_event_info_line_two);
        }

        if (TextHelper.isValid(lineTwoPref)) {
            mLineTwo.setText(lineTwoPref);
        }

        if (TextHelper.isValid(logoUriPref)) {
            loadLogo(logoUriPref);
        }

        mLogoUri.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogoSelection(appContext);
            }
        });

        mLogoClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLogo();
            }
        });

        if (datePref != PreferencesHelper.EVENT_DATE_HIDDEN) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(datePref);
            mDate.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            mDate.setEnabled(true);
            mDateHidden.setChecked(false);
        } else {
            mDate.setEnabled(false);
            mDateHidden.setChecked(true);
        }

        mDateHidden.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDate.setEnabled(!isChecked);
            }
        });

        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call to client.
                ICallbacks callbacks = getCallbacks();
                if (callbacks != null) {
                    callbacks.onEventInfoSetupCompleted();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Create new logo from the selected image.
        if (requestCode == EVENT_LOGO_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            newLogo(data.getData());
        }
    }

    @Override
    public void onPause() {
        Context appContext = getActivity().getApplicationContext();

        // Store title.
        String lineOneString = null;
        Editable lineOne = mLineOne.getText();
        if (lineOne != null && lineOne.length() > 0) {
            lineOneString = lineOne.toString();
        }
        mPreferencesHelper.storeEventLineOne(appContext, lineOneString);

        String lineTwoString = null;
        Editable lineTwo = mLineTwo.getText();
        if (lineTwo != null && lineTwo.length() > 0) {
            lineTwoString = lineTwo.toString();
        }
        mPreferencesHelper.storeEventLineTwo(appContext, lineTwoString);

        // Store logo uri.
        String logoUriString = null;
        CharSequence logoUri = mLogoUri.getText();
        if (logoUri != null && logoUri.length() > 0) {
            logoUriString = logoUri.toString();
        }
        mPreferencesHelper.storeEventLogoUri(appContext, logoUriString);

        // Store date.
        if (mDateHidden.isChecked()) {
            mPreferencesHelper.storeEventDate(appContext, PreferencesHelper.EVENT_DATE_HIDDEN);
        } else {
            Calendar calendar = new GregorianCalendar(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth());
            mPreferencesHelper.storeEventDate(appContext, calendar.getTimeInMillis());
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
    private EventInfoSetupFragment.ICallbacks getCallbacks() {
        EventInfoSetupFragment.ICallbacks callbacks = null;
        if (mCallbacks != null) {
            callbacks = mCallbacks.get();
        }
        return callbacks;
    }

    /**
     * Loads the event logo from cache and updates ui when the operation completes. If the logo is unavailable,
     * an attempt will be made to create a new logo based on the source image uri.
     *
     * @param srcUri the {@link Uri} to the source image.
     */
    private void loadLogo(final String srcUri) {
        mBitmapCache.asyncGet(BaseTitleHeader.EVENT_LOGO_CACHE_KEY, new PersistedBitmapCache.IAsyncGetCallbacks() {

            @Override
            public void onSuccess(String key, Bitmap bitmap) {
                if (isActivityAlive()) {
                    mLogoUri.setText(srcUri);

                    // Set thumb as compound drawable, fitted to the text height.
                    final BitmapDrawable thumb = new BitmapDrawable(getResources(), bitmap);
                    int thumbSize = (int) getResources().getDimension(R.dimen.text_size_normal);
                    Point drawableSize = ImageHelper.getAspectFitSize(thumbSize, thumbSize, bitmap.getWidth(), bitmap.getHeight());
                    thumb.setBounds(0, 0, drawableSize.x, drawableSize.y);
                    mLogoUri.setCompoundDrawables(null, null, thumb, null);

                    mLogoClear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String key) {
                if (isActivityAlive()) {
                    newLogo(Uri.parse(srcUri));
                }
            }
        });
    }

    /**
     * Creates and stores a new event logo from an image {@link Uri}. Update ui when the operation completes.
     *
     * @param srcUri the {@link Uri} to the source image.
     */
    private void newLogo(final Uri srcUri) {
        if (TextHelper.isValid(srcUri.toString())) {
            final ContentResolver resolver = getActivity().getContentResolver();
            final Handler workerHandler = new Handler(MyApplication.getWorkerLooper());
            workerHandler.post(new Runnable() {

                @Override
                public void run() {
                    // Load and create new logo.
                    final Bitmap newLogo = ImageHelper.getScaledBitmap(resolver, srcUri, BaseTitleHeader.EVENT_LOGO_MAX_WIDTH, BaseTitleHeader.EVENT_LOGO_MAX_HEIGHT);
                    if (newLogo != null) {
                        // Store new logo in cache.
                        mBitmapCache.asyncPut(BaseTitleHeader.EVENT_LOGO_CACHE_KEY, newLogo, new PersistedBitmapCache.IAsyncPutCallbacks() {

                            @Override
                            public void onSuccess(String key) {
                                if (isActivityAlive()) {
                                    mLogoUri.setText(srcUri.toString());

                                    // tryGet() should always succeed since the cache has just been populated.
                                    final Bitmap logo = mBitmapCache.tryGet(key);
                                    if (logo != null) {
                                        // Set thumb as compound drawable, fitted to the text height.
                                        final BitmapDrawable thumb = new BitmapDrawable(getResources(), logo);
                                        int thumbSize = (int) getResources().getDimension(R.dimen.text_size_normal);
                                        Point drawableSize = ImageHelper.getAspectFitSize(thumbSize, thumbSize, logo.getWidth(), logo.getHeight());
                                        thumb.setBounds(0, 0, drawableSize.x, drawableSize.y);
                                        mLogoUri.setCompoundDrawables(null, null, thumb, null);
                                    }

                                    mLogoClear.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(String key) {
                                postLogoError();
                            }
                        });
                    } else {
                        postLogoError();
                    }
                }
            });
        } else {
            postLogoError();
        }
    }

    /**
     * Clears event logo from the bitmap cache and updates ui when the operation completes.
     */
    private void clearLogo() {
        mLogoClear.setEnabled(false);

        // Remove logo from cache.
        mBitmapCache.asyncRemove(BaseTitleHeader.EVENT_LOGO_CACHE_KEY, new PersistedBitmapCache.IAsyncRemoveCallbacks() {

            @Override
            public void onSuccess(String key) {
                if (isActivityAlive()) {
                    mLogoUri.setText("");
                    mLogoUri.setCompoundDrawables(null, null, null, null);
                    mLogoClear.setVisibility(View.GONE);
                    mLogoClear.setEnabled(true);
                }
            }

            @Override
            public void onFailure(String key) {
                // The operation fails if the bitmap is not found in the disk cache, which may happen
                // when the cache has been cleared by the system. In this case, clearing the ui is sufficient.
                if (isActivityAlive()) {
                    mLogoUri.setText("");
                    mLogoUri.setCompoundDrawables(null, null, null, null);
                    mLogoClear.setVisibility(View.GONE);
                    mLogoClear.setEnabled(true);
                }
            }
        });
    }

    /**
     * Starts the {@link Activity} for event logo selection.
     *
     * @param context the {@link Context}.
     */
    private void launchLogoSelection(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(EVENT_LOGO_MIME_TYPE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooserIntent = Intent.createChooser(intent, context.getString(R.string.event_info_setup__event_logo_chooser_title));
        startActivityForResult(chooserIntent, EVENT_LOGO_REQUEST_CODE);
    }

    /**
     * Posts a toast to indicate that the selected logo failed to load.
     */
    private void postLogoError() {
        if (isActivityAlive()) {
            final Activity activity = getActivity();
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (isActivityAlive()) {
                        Toast.makeText(getActivity(), getString(R.string.event_info_setup__error_logo), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
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

    //
    // Public methods.
    //

    /**
     * Creates a new {@link EventInfoSetupFragment} instance.
     *
     * @return the new {@link EventInfoSetupFragment} instance.
     */
    public static EventInfoSetupFragment newInstance() {
        return new EventInfoSetupFragment();
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for this fragment.
     */
    public interface ICallbacks {

        /**
         * Setup of the event info has completed.
         */
        void onEventInfoSetupCompleted();
    }
}
