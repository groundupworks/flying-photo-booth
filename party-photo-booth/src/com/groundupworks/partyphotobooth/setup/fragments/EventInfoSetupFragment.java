/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import com.groundupworks.partyphotobooth.R;
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
     * Callbacks for this fragment.
     */
    private WeakReference<EventInfoSetupFragment.ICallbacks> mCallbacks = null;

    /**
     * A {@link PreferencesHelper} instance.
     */
    private PreferencesHelper mPreferencesHelper = new PreferencesHelper();

    //
    // Views.
    //

    private EditText mLineOne;

    private EditText mLineTwo;

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
        mDate = (DatePicker) view.findViewById(R.id.setup_event_info_date);
        mDateHidden = (CheckBox) view.findViewById(R.id.setup_event_info_date_hidden);
        mNext = (Button) view.findViewById(R.id.setup_event_info_button_next);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context appContext = getActivity().getApplicationContext();
        String lineOnePref = mPreferencesHelper.getEventLineOne(appContext);
        String lineTwoPref = mPreferencesHelper.getEventLineTwo(appContext);
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
