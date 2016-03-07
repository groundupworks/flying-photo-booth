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
package com.groundupworks.lib.photobooth.helpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A helper class for producing audio feedback during the photo capture sequence.
 *
 * @author David Marques
 */
public class CameraAudioHelper {

    /**
     * The application context.
     */
    private final Context mContext;

    /**
     * Resource id for the beep tone.
     */
    private final int mBeepId;

    /**
     * Handler for posting to the worker thread.
     */
    private final Handler mWorkerHandler;

    /**
     * The audio player.
     */
    private volatile MediaPlayer mBeepPlayer;

    /**
     * Lock for operating the player.
     */
    private final ReentrantReadWriteLock mLock;

    /**
     * Constructor.
     *
     * @param context       the {@link Context}.
     * @param beepId        resource id for the beep tone.
     * @param workerHandler handler for posting to the worker thread.
     */
    public CameraAudioHelper(final Context context, final int beepId, Handler workerHandler) {
        mContext = context.getApplicationContext();
        mBeepId = beepId;
        mWorkerHandler = workerHandler;
        mLock = new ReentrantReadWriteLock();
    }

    //
    // Public methods.
    //

    /**
     * Prepares the helper in the background thread.
     */
    public void prepare() {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                mBeepPlayer = MediaPlayer.create(mContext, mBeepId);
            }
        });
    }

    /**
     * Releases the helper in the background thread.
     */
    public void release() {
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final MediaPlayer beepPlayer = mBeepPlayer;
                if (beepPlayer == null) {
                    return;
                }

                final ReentrantReadWriteLock.WriteLock writeLock = mLock.writeLock();
                writeLock.lock();
                try {
                    beepPlayer.stop();
                    beepPlayer.release();
                    mBeepPlayer = null;
                } finally {
                    writeLock.unlock();
                }
            }
        });
    }

    /**
     * Plays the beep tone.
     */
    public void beep() {
        final MediaPlayer beepPlayer = mBeepPlayer;
        if (beepPlayer == null) {
            return;
        }

        final ReentrantReadWriteLock.ReadLock readLock = mLock.readLock();
        readLock.lock();
        try {
            if (beepPlayer.isPlaying()) {
                beepPlayer.pause();
            }

            beepPlayer.seekTo(0);
            beepPlayer.start();
        } finally {
            readLock.unlock();
        }
    }
}
