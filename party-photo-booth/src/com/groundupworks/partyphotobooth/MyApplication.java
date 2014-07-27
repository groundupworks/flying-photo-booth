/*
 * Copyright (C) 2013 Benedict Lau
 * 
 * All rights reserved.
 */
package com.groundupworks.partyphotobooth;

import android.os.Handler;

import com.groundupworks.lib.photobooth.framework.BaseApplication;

/**
 * A concrete {@link BaseApplication} class.
 *
 * @author Benedict Lau
 */
public class MyApplication extends BaseApplication {

    /**
     * Bitmap cache with application scope.
     */
    private static PersistedBitmapCache mCache;

    @Override
    public void onCreate() {
        super.onCreate();
        mCache = new PersistedBitmapCache(this, new Handler(getWorkerLooper()), new Handler(getMainLooper()));
    }

    //
    // Public methods.
    //

    /**
     * Gets the bitmap cache with application scope.
     *
     * @return the bitmap cache.
     */
    public static PersistedBitmapCache getBitmapCache() {
        return mCache;
    }
}
