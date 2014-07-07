/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth.setup.model;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.groundupworks.partyphotobooth.R;

/**
 * An base class for a {@link SpinnerAdapter} where the model objects are set at construction time and will not change.
 *
 * @param <T> the model object class.
 * @author Benedict Lau
 */
public abstract class BaseSpinnerAdapter<T> extends BaseAdapter {

    /**
     * A {@link LayoutInflater}.
     */
    private LayoutInflater mInflater;

    /**
     * The {@link SparseArray} of model object items. Indices must be continuous.
     */
    private SparseArray<T> mItems;

    /**
     * Color of a list item display name.
     */
    private int mListItemColor;

    /**
     * Color of the selected item display name.
     */
    private int mSelectedItemColor;

    /**
     * Constructor.
     *
     * @param context the {@link Context}.
     * @param items   the {@link SparseArray} of model objects. Indices must be continuous.
     */
    public BaseSpinnerAdapter(Context context, SparseArray<T> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;

        Resources res = context.getResources();
        mListItemColor = res.getColor(R.color.text_light);
        mSelectedItemColor = res.getColor(R.color.text_dark);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.view_spinner_item, parent, false);
        } else {
            view = convertView;
        }

        T item = getItem(position);
        bindView(item, view);

        // Set selected item display name color.
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setTextColor(mSelectedItemColor);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = getView(position, convertView, parent);

        // Set list item display name color.
        ((TextView) view.findViewById(R.id.spinner_item_display_name)).setTextColor(mListItemColor);

        return view;
    }

    //
    // Private methods.
    //

    /**
     * Binds a model object item to a view.
     *
     * @param item the model object item containing data to populate the view.
     * @param view the view to bind to.
     */
    protected abstract void bindView(T item, View view);
}
