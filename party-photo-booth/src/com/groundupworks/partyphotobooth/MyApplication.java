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
