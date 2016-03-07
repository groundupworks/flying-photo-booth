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


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.groundupworks.lib.photobooth.helpers.ImageHelper;
import com.groundupworks.lib.photobooth.helpers.StorageHelper;
import com.groundupworks.partyphotobooth.helpers.TextHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bitmap in-memory cache backed with disk storage using the cache directory. Note that the disk cache
 * may be cleared when the device runs out of storage space.
 *
 * @author Benedict Lau
 */
public class PersistedBitmapCache {

    /**
     * In-memory cache.
     */
    private final Map<String, Bitmap> mMemCache;

    /**
     * The disk cache directory.
     */
    private final File mDiskCacheDir;

    /**
     * Handler for background tasks.
     */
    private final Handler mWorkerHandler;

    /**
     * Handler for callbacks.
     */
    private final Handler mCallbackHandler;

    /**
     * Constructor.
     *
     * @param context         the {@link Context}.
     * @param workerHandler   a {@link Handler} associated with the background thread that will be used
     *                        to process async commands.
     * @param callbackHandler a {@link Handler} associated with the thread that callbacks will be posted
     *                        to. Pass null to post callbacks to the main thread.
     */
    public PersistedBitmapCache(Context context, Handler workerHandler, Handler callbackHandler) {
        mMemCache = new ConcurrentHashMap<String, Bitmap>();
        mDiskCacheDir = context.getCacheDir();
        mWorkerHandler = workerHandler;
        if (callbackHandler != null) {
            mCallbackHandler = callbackHandler;
        } else {
            mCallbackHandler = new Handler(Looper.getMainLooper());
        }
    }

    //
    // Private methods.
    //

    /**
     * Puts bitmap into the cache.
     *
     * @param key    the key for the bitmap.
     * @param bitmap the bitmap.
     * @return true if successful; false otherwise.
     */
    private boolean put(String key, Bitmap bitmap) {
        boolean isSuccessful = true;

        // Get unique filename based on key.
        final String filename = StorageHelper.generateValidFilename(key);
        if (TextHelper.isValid(filename)) {
            final File file = new File(mDiskCacheDir, filename);

            // Store PNG in disk cache.
            try {
                final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                isSuccessful = ImageHelper.writePng(bitmap, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (FileNotFoundException e) {
                isSuccessful = false;
            } catch (IOException e) {
                isSuccessful = false;
            }

            if (isSuccessful) {
                // Read PNG from disk cache and put bitmap in memory cache.
                Bitmap storedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (storedBitmap != null) {
                    mMemCache.put(key, storedBitmap);
                } else {
                    isSuccessful = false;
                }
            }
        }

        return isSuccessful;
    }

    /**
     * Gets bitmap from the cache.
     *
     * @param key the key for the bitmap.
     * @return the bitmap; or null if failed.
     */
    private Bitmap get(String key) {
        // Try to get bitmap from memory cache.
        Bitmap bitmap = mMemCache.get(key);

        // Try to get PNG from disk cache if bitmap not found in memory cache.
        if (bitmap == null) {
            // Get unique filename based on key.
            String filename = StorageHelper.generateValidFilename(key);
            if (TextHelper.isValid(filename)) {
                final File file = new File(mDiskCacheDir, filename);
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap != null) {
                    // Put bitmap in memory cache.
                    mMemCache.put(key, bitmap);
                }
            }
        }

        return bitmap;
    }

    /**
     * Removes bitmap from the cache.
     *
     * @param key the key for the bitmap.
     * @return true if successful; false otherwise.
     */
    private boolean remove(String key) {
        boolean isSuccessful = true;

        // Get unique filename based on key.
        final String filename = StorageHelper.generateValidFilename(key);
        if (TextHelper.isValid(filename)) {
            // Remove PNG from disk cache.
            final File file = new File(mDiskCacheDir, filename);
            isSuccessful = file.delete();

            // Remove bitmap from memory cache.
            mMemCache.remove(key);
        }

        return isSuccessful;
    }

    //
    // Public methods.
    //

    /**
     * Synchronously tries to get bitmap from the memory cache. No attempt will be made to load from
     * the disk cache. The bitmap will only be available if a prior
     * {@link #asyncPut(String, Bitmap, PersistedBitmapCache.IAsyncPutCallbacks)} or
     * {@link #asyncGet(String, PersistedBitmapCache.IAsyncGetCallbacks)} operation has completed successfully.
     *
     * @param key the key for the bitmap.
     * @return the bitmap; or null if failed.
     */
    public Bitmap tryGet(String key) {
        return mMemCache.get(key);
    }

    /**
     * Asynchronously puts a bitmap into the cache using a background thread.
     *
     * @param key       the key for the bitmap.
     * @param bitmap    the bitmap.
     * @param callbacks the callbacks for this operation.
     */
    public void asyncPut(final String key, final Bitmap bitmap, final IAsyncPutCallbacks callbacks) {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final Runnable runnable;
                final boolean isSuccessful = put(key, bitmap);

                if (isSuccessful) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onSuccess(key);
                        }
                    };
                } else {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onFailure(key);
                        }
                    };
                }

                // Post callback to callback handler thread.
                mCallbackHandler.post(runnable);
            }
        });
    }

    /**
     * Asynchronously gets a bitmap from the cache using a background thread.
     *
     * @param key       the key for the bitmap.
     * @param callbacks the callbacks for this operation.
     */
    public void asyncGet(final String key, final IAsyncGetCallbacks callbacks) {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final Runnable runnable;
                final Bitmap bitmap = get(key);

                if (bitmap != null) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onSuccess(key, bitmap);
                        }
                    };
                } else {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onFailure(key);
                        }
                    };
                }

                // Post callback to callback handler thread.
                mCallbackHandler.post(runnable);
            }
        });
    }

    /**
     * Asynchronously removes a bitmap from the cache using a background thread.
     *
     * @param key       the key for the bitmap.
     * @param callbacks the callbacks for this operation.
     */
    public void asyncRemove(final String key, final IAsyncRemoveCallbacks callbacks) {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final Runnable runnable;
                final boolean isSuccessful = remove(key);

                if (isSuccessful) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onSuccess(key);
                        }
                    };
                } else {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onFailure(key);
                        }
                    };
                }

                // Post callback to callback handler thread.
                mCallbackHandler.post(runnable);
            }
        });
    }

    //
    // Interfaces.
    //

    /**
     * Callbacks for the {@link #asyncPut(String, Bitmap, PersistedBitmapCache.IAsyncPutCallbacks)} command.
     * Only one of {@link #onSuccess(String)} and {@link #onFailure(String)} will be called.
     */
    public interface IAsyncPutCallbacks {

        /**
         * The operation completed successfully. The returned bitmap is available in memory and persisted
         * in the internal application cache.
         *
         * @param key the key for the bitmap.
         */
        public void onSuccess(String key);

        /**
         * The operation failed.
         *
         * @param key the key for the bitmap.
         */
        public void onFailure(String key);
    }

    /**
     * Callbacks for the {@link #asyncGet(String, PersistedBitmapCache.IAsyncGetCallbacks)} command.
     * Only one of {@link #onSuccess(String, Bitmap)} and {@link #onFailure(String)} will be called.
     */
    public interface IAsyncGetCallbacks {

        /**
         * The operation completed successfully.
         *
         * @param key    the key for the bitmap.
         * @param bitmap the bitmap.
         */
        public void onSuccess(String key, Bitmap bitmap);

        /**
         * The operation failed.
         *
         * @param key the key for the bitmap.
         */
        public void onFailure(String key);
    }

    /**
     * Callbacks for the {@link #asyncRemove(String, PersistedBitmapCache.IAsyncRemoveCallbacks)} command.
     * Only one of {@link #onSuccess(String)} and {@link #onFailure(String)} will be called.
     */
    public interface IAsyncRemoveCallbacks {

        /**
         * The operation completed successfully.
         *
         * @param key the key for the bitmap.
         */
        public void onSuccess(String key);

        /**
         * The operation failed.
         *
         * @param key the key for the bitmap.
         */
        public void onFailure(String key);
    }
}
